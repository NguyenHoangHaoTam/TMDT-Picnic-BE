package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.*;
import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.service.SharedCartService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/api/shared-carts")
@RequiredArgsConstructor
public class SharedCartController {

    private final SharedCartService sharedCartService;

    // 1. Tạo giỏ hàng chia sẻ
    @Operation(summary = "Tạo giỏ hàng chia sẻ mới")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SharedCartListResponse.CreateResponse>> createSharedCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SharedCartCreateRequest request) {

        Long ownerId = Long.valueOf(jwt.getClaimAsString("sub")); // Lấy userId từ token
        request.setOwnerId(ownerId); // Gán vào request

        SharedCartListResponse.CreateResponse response = sharedCartService.createSharedCart(request);

        return ResponseEntity.ok(
                ApiResponse.<SharedCartListResponse.CreateResponse>builder()
                        .message("Tạo giỏ hàng chia sẻ thành công")
                        .data(response)
                        .build()
        );
    }

    // 2. Thêm sản phẩm vào giỏ
    @Operation(summary = "Thêm sản phẩm vào giỏ chia sẻ")
    @PostMapping("/add-item")
    public ResponseEntity<ApiResponse<SharedCartAddItemResponse>> addItemToCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SharedCartAddItemRequest request) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub")); // Lấy userId từ token
        request.setAddByUserId(userId);

        SharedCartAddItemResponse response = sharedCartService.addItemToCart(request);

        return ResponseEntity.ok(
                ApiResponse.<SharedCartAddItemResponse>builder()
                        .message("Thêm sản phẩm vào giỏ chia sẻ thành công")
                        .data(response)
                        .build()
        );
    }

    // 3. Mời người dùng tham gia giỏ hàng
    @Operation(summary = "Mời người tham gia giỏ chia sẻ (bằng email hoặc username)")
    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<List<SharedCartInviteResponse>>> inviteParticipants(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SharedCartInviteRequest request) {
        Long inviterId = Long.valueOf(jwt.getClaimAsString("sub"));
        List<SharedCartInviteResponse> responses = sharedCartService.addParticipant(request, inviterId);
        return ResponseEntity.ok(
                ApiResponse.<List<SharedCartInviteResponse>>builder()
                        .message("Mời người tham gia giỏ chia sẻ thành công")
                        .data(responses)
                        .build()
        );
    }

    // 4. Lấy danh sách giỏ hàng chia sẻ của tôi
    @Operation(summary = "Lấy danh sách giỏ hàng chia sẻ của user (owner hoặc participant)")
    @GetMapping("/my-carts")
    public ResponseEntity<ApiResponse<List<SharedCartListResponse>>> getMySharedCarts(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        List<SharedCartListResponse> response = sharedCartService.getMySharedCarts(userId);
        return ResponseEntity.ok(
                ApiResponse.<List<SharedCartListResponse>>builder()
                        .message("Lấy danh sách giỏ hàng chia sẻ thành công")
                        .data(response)
                        .build()
        );
    }

    // 5. Lấy chi tiết giỏ chia sẻ
    @Operation(summary = "Lấy chi tiết giỏ hàng chia sẻ (bao gồm sản phẩm và người tham gia)")
    @GetMapping("/{cartId}")
    public ResponseEntity<ApiResponse<SharedCartDetailResponse>> getCartDetails(
            @PathVariable Long cartId) {
        SharedCartDetailResponse response = sharedCartService.getCartDetails(cartId);
        return ResponseEntity.ok(
                ApiResponse.<SharedCartDetailResponse>builder()
                        .message("Lấy chi tiết giỏ hàng chia sẻ thành công")
                        .data(response)
                        .build()
        );
    }

    // 6. Cập nhật số lượng sản phẩm
    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ chia sẻ")
    @PutMapping("/update-quantity")
    public ResponseEntity<ApiResponse<SharedCartAddItemResponse>> updateItemQuantity(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SharedCartAddItemRequest.UpdateQuantityRequest request) {
        SharedCartAddItemResponse response = sharedCartService.updateItemQuantity(request);
        return ResponseEntity.ok(
                ApiResponse.<SharedCartAddItemResponse>builder()
                        .message("Cập nhật số lượng thành công")
                        .data(response)
                        .build()
        );
    }

    // 7. Xóa sản phẩm khỏi giỏ
    @Operation(summary = "Xóa sản phẩm khỏi giỏ chia sẻ")
    @DeleteMapping("/remove-item")
    public ResponseEntity<ApiResponse<String>> removeItem(
            @RequestBody SharedCartAddItemRequest.RemoveItemRequest request) {
        sharedCartService.removeItem(request);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Xóa sản phẩm khỏi giỏ chia sẻ thành công")
                        .data("Item removed")
                        .build()
        );
    }

    // 8. Đóng giỏ hàng (chỉ owner)
    @Operation(summary = "Đóng giỏ hàng chia sẻ (chỉ owner mới được đóng)")
    @PutMapping("/{cartId}/close")
    public ResponseEntity<ApiResponse<SharedCartListResponse.CreateResponse>> closeCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long cartId) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        SharedCartListResponse.CreateResponse response = sharedCartService.closeCart(cartId, userId);
        return ResponseEntity.ok(
                ApiResponse.<SharedCartListResponse.CreateResponse>builder()
                        .message("Đóng giỏ hàng chia sẻ thành công")
                        .data(response)
                        .build()
        );
    }

    // 9. Hủy giỏ hàng (chỉ owner)
    @Operation(summary = "Hủy giỏ hàng chia sẻ (chỉ owner mới được hủy)")
    @PutMapping("/{cartId}/cancel")
    public ResponseEntity<ApiResponse<SharedCartListResponse.CreateResponse>> cancelCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long cartId) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        SharedCartListResponse.CreateResponse response = sharedCartService.cancelCart(cartId, userId);
        return ResponseEntity.ok(
                ApiResponse.<SharedCartListResponse.CreateResponse>builder()
                        .message("Hủy giỏ hàng chia sẻ thành công")
                        .data(response)
                        .build()
        );
    }

    // 10. Rời khỏi giỏ hàng (participant)
    @Operation(summary = "Rời khỏi giỏ hàng chia sẻ (participant tự rời)")
    @DeleteMapping("/{cartId}/leave")
    public ResponseEntity<ApiResponse<String>> leaveCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long cartId) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        sharedCartService.leaveCart(cartId, userId);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Rời khỏi giỏ hàng chia sẻ thành công")
                        .data("Left cart successfully")
                        .build()
        );
    }

    // 11. Xóa participant (chỉ owner)
    @Operation(summary = "Xóa người tham gia khỏi giỏ hàng (chỉ owner mới được xóa)")
    @DeleteMapping("/{cartId}/participants/{participantUserId}")
    public ResponseEntity<ApiResponse<String>> removeParticipant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long cartId,
            @PathVariable Long participantUserId) {
        Long ownerId = Long.valueOf(jwt.getClaimAsString("sub"));
        sharedCartService.removeParticipant(cartId, participantUserId, ownerId);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Xóa người tham gia thành công")
                        .data("Participant removed")
                        .build()
        );
    }

    // 12. Cập nhật contribution amount
    @Operation(summary = "Cập nhật số tiền đóng góp của participant")
    @PutMapping("/update-contribution")
    public ResponseEntity<ApiResponse<SharedCartInviteResponse>> updateContribution(
            @RequestBody SharedCartAddItemRequest.UpdateContributionRequest request) {
        SharedCartInviteResponse response = sharedCartService.updateContribution(request);
        return ResponseEntity.ok(
                ApiResponse.<SharedCartInviteResponse>builder()
                        .message("Cập nhật số tiền đóng góp thành công")
                        .data(response)
                        .build()
        );
    }

    // 13. Checkout shared cart (thanh toán giỏ hàng chung)
    @Operation(summary = "Thanh toán giỏ hàng chung (chỉ owner mới được thanh toán)")
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<String>> checkoutSharedCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody SharedCartCheckoutRequest request,
            HttpServletRequest httpRequest) throws UnsupportedEncodingException {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));

        // Lấy IP client chính xác, hỗ trợ proxy
        String ipAddress = extractClientIp(httpRequest);

        // Gọi service checkout
        String paymentUrl = sharedCartService.checkoutSharedCart(userId, request, ipAddress);

        if (paymentUrl == null || paymentUrl.isEmpty()) {
            // COD - thanh toán thành công ngay
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Thanh toán thành công! Đơn hàng đã được tạo.")
                            .data("success")
                            .build()
            );
        } else {
            // VNPay - trả về URL thanh toán
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Tạo đơn hàng thành công. Chuyển hướng đến VNPay để thanh toán.")
                            .data(paymentUrl)
                            .build()
            );
        }
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