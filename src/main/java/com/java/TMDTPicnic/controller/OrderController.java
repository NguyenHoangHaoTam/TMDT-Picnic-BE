package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.CheckoutRequest;
import com.java.TMDTPicnic.dto.request.OrderStatusUpdateRequest;
import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.enums.OrderStatus;
import com.java.TMDTPicnic.service.OrderService;
import com.java.TMDTPicnic.service.VNPayService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final VNPayService vnPayService;

    /**
     * Đặt hàng (checkout) - thanh toán bằng VNPay
     * Hỗ trợ đặt hàng từ:
     * - Giỏ hàng (orderType = "GROUP", dùng cartItems)
     * - Đặt trực tiếp (orderType = "SINGLE", dùng directItems)
     *
     * @param jwt: lấy userId từ token
     * @param request: dữ liệu đặt hàng
     * @param httpRequest: lấy IP client
     * @return URL thanh toán VNPay
     */
    @PostMapping("/checkout-vnpay")
    public ResponseEntity<ApiResponse<String>> checkoutVNPay(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CheckoutRequest request,
            HttpServletRequest httpRequest) throws UnsupportedEncodingException {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));

        // Lấy IP client chính xác, hỗ trợ proxy
        String ipAddress = extractClientIp(httpRequest);

        // Gọi service tạo đơn và lấy URL thanh toán
        String paymentUrl = orderService.createOrder(userId, request, ipAddress);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Tạo đơn hàng thành công. Chuyển hướng đến VNPay để thanh toán.")
                        .data(paymentUrl)
                        .build()
        );
    }

    /**
     * Callback khi người dùng thanh toán xong và VNPay redirect về returnUrl
     * VD: /api/orders/vnpay-return?vnp_Amount=...&vnp_ResponseCode=00&vnp_SecureHash=...
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<String>> handleVNPayReturn(@RequestParam Map<String, String> vnpParams) {
        boolean valid = vnPayService.validateReturn(vnpParams);
        String responseCode = vnpParams.get("vnp_ResponseCode");
        Long orderId = Long.valueOf(vnpParams.get("vnp_TxnRef"));

        if (!valid) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<String>builder()
                            .message("Xác minh chữ ký thất bại.")
                            .data(null)
                            .build()
            );
        }

        if ("00".equals(responseCode)) {
            // Thanh toán thành công, cập nhật trạng thái
            orderService.updatePaymentStatusAfterSuccess(orderId);

            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Thanh toán thành công cho đơn hàng #" + orderId)
                            .data("success")
                            .build()
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Thanh toán thất bại hoặc bị hủy cho đơn hàng #" + orderId)
                            .data("failed")
                            .build()
            );
        }
    }




    /**
     * Lấy lịch sử đơn hàng cá nhân của user (SINGLE, GROUP)
     */
    @GetMapping("/history/personal")
    @Operation(summary = "Lấy lịch sử đơn hàng cá nhân của user")
    public ResponseEntity<ApiResponse<OrderHistoryResponse>> getPersonalOrderHistory(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        OrderHistoryResponse orderHistory = orderService.getPersonalOrderHistory(userId);

        return ResponseEntity.ok(
                ApiResponse.<OrderHistoryResponse>builder()
                        .message("Lấy lịch sử đơn hàng cá nhân thành công")
                        .data(orderHistory)
                        .build()
        );
    }

    /**
     * Lấy lịch sử đơn hàng từ shared cart của user
     */
    @GetMapping("/history/shared")
    @Operation(summary = "Lấy lịch sử đơn hàng từ shared cart của user")
    public ResponseEntity<ApiResponse<OrderHistoryResponse>> getSharedCartOrderHistory(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        OrderHistoryResponse orderHistory = orderService.getSharedCartOrderHistory(userId);

        return ResponseEntity.ok(
                ApiResponse.<OrderHistoryResponse>builder()
                        .message("Lấy lịch sử đơn hàng shared cart thành công")
                        .data(orderHistory)
                        .build()
        );
    }

    /**
     * Admin: Lấy tất cả đơn hàng với pagination và filter
     */
    @GetMapping("/admin")
    @Operation(summary = "ROLE-ADMIN Lấy tất cả đơn hàng với pagination và filter")
    public ResponseEntity<ApiResponse<OrderPageResponse>> getAllOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String orderType) {

        String scope = jwt.getClaimAsString("scope");
        if (scope == null || !scope.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<OrderPageResponse>builder()
                            .code(403)
                            .message("Không có quyền truy cập")
                            .build());
        }

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderSummaryResponse> orderPage = orderService.getAllOrders(pageable, status, orderType);

        OrderPageResponse response = new OrderPageResponse(
                orderPage.getContent(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isFirst(),
                orderPage.isLast()
        );

        return ResponseEntity.ok(
                ApiResponse.<OrderPageResponse>builder()
                        .message("Lấy danh sách đơn hàng thành công")
                        .data(response)
                        .build()
        );
    }

    /**
     * Admin: Cập nhật trạng thái đơn hàng từ PAID sang SHIPPED
     */
    @PutMapping("/{orderId}/status")
    @Operation(summary = "ROLE-ADMIN Cập nhật trạng thái đơn hàng (PAID -> SHIPPED)")
    public ResponseEntity<ApiResponse<OrderStatusUpdateResponse>> updateOrderStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequest request) {

        // Kiểm tra quyền ADMIN
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || !scope.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<OrderStatusUpdateResponse>builder()
                            .code(403)
                            .message("Không có quyền truy cập")
                            .build());
        }

        OrderStatusUpdateResponse response = orderService.updateOrderStatusByAdmin(orderId, request);

        return ResponseEntity.ok(
                ApiResponse.<OrderStatusUpdateResponse>builder()
                        .message(response.getMessage())
                        .data(response)
                        .build()
        );
    }

    /**
     * User: Xác nhận đã nhận hàng (SHIPPED -> COMPLETED)
     */
    @PutMapping("/{orderId}/confirm-received")
    @Operation(summary = "Xác nhận đã nhận hàng (SHIPPED -> COMPLETED)")
    public ResponseEntity<ApiResponse<OrderStatusUpdateResponse>> confirmReceived(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        OrderStatusUpdateResponse response = orderService.updateOrderStatusByUser(orderId, userId);

        return ResponseEntity.ok(
                ApiResponse.<OrderStatusUpdateResponse>builder()
                        .message(response.getMessage())
                        .data(response)
                        .build()
        );
    }

    /**
     * Lấy IP client hỗ trợ trường hợp qua proxy
     */
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Trường hợp IP có nhiều IP (do proxy chain), lấy IP đầu tiên
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}