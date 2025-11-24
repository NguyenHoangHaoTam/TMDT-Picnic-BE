package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.DashboardRequest;
import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // ==================== FULL DASHBOARD ====================
    @PostMapping("/1-bieu-do-tron-Order-Summary")
    @Operation(summary = "ROLE-ADMIN Lấy Order-Summary-Dashboard thành công")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @Valid @RequestBody DashboardRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Kiểm tra quyền ADMIN
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || !scope.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<DashboardResponse>builder()
                            .code(403)
                            .message("Không có quyền truy cập")
                            .build());
        }

        DashboardResponse data = dashboardService.getOrderSummary(request);
        return ResponseEntity.ok(
                ApiResponse.<DashboardResponse>builder()
                        .message("Lấy Order-Summary-Dashboard từ " + request.getFromDate() + " đến " + request.getToDate() +" thành công")
                        .data(data)
                        .build()
        );
    }

    // ==================== REVENUE ====================
    @PostMapping("/2-bieu-do-cot-revenue-chart")
    @Operation(summary = "ROLE-ADMIN Lấy biểu đồ doanh thu")
    public ResponseEntity<ApiResponse<List<RevenueByDayResponse>>> getRevenueChart(
            @Valid @RequestBody DashboardRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Kiểm tra quyền ADMIN
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || !scope.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<RevenueByDayResponse>>builder()
                            .code(403)
                            .message("Không có quyền truy cập")
                            .build());
        }

        List<RevenueByDayResponse> data = dashboardService.getRevenueChart(request);
        return ResponseEntity.ok(
                ApiResponse.<List<RevenueByDayResponse>>builder()
                        .message("Lấy dữ liệu biểu đồ doanh thu thành công")
                        .data(data)
                        .build()
        );
    }

    // ==================== ORDERS ====================
    @PostMapping("/3-bieu-do-cot-orders-chart")
    @Operation(summary = "ROLE-ADMIN Lấy biểu đồ đơn hàng")
    public ResponseEntity<ApiResponse<List<OrdersByDayResponse>>> getOrdersChart(
            @Valid @RequestBody DashboardRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Kiểm tra quyền ADMIN
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || !scope.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<OrdersByDayResponse>>builder()
                            .code(403)
                            .message("Không có quyền truy cập")
                            .build());
        }

        List<OrdersByDayResponse> data = dashboardService.getOrdersChart(request);
        return ResponseEntity.ok(
                ApiResponse.<List<OrdersByDayResponse>>builder()
                        .message("Lấy dữ liệu biểu đồ đơn hàng thành công")
                        .data(data)
                        .build()
        );
    }

    // ==================== USERS ====================
    @PostMapping("/4-bieu-do-cot-users-chart")
    @Operation(summary = "ROLE-ADMIN Lấy biểu đồ người dùng")
    public ResponseEntity<ApiResponse<List<UserStatsByDayResponse>>> getUsersChart(
            @Valid @RequestBody DashboardRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Kiểm tra quyền ADMIN
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || !scope.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<UserStatsByDayResponse>>builder()
                            .code(403)
                            .message("Không có quyền truy cập")
                            .build());
        }

        List<UserStatsByDayResponse> data = dashboardService.getUsersChart(request);
        return ResponseEntity.ok(
                ApiResponse.<List<UserStatsByDayResponse>>builder()
                        .message("Lấy dữ liệu biểu đồ người dùng thành công")
                        .data(data)
                        .build()
        );
    }

    // ==================== TOP CATEGORIES ====================
    @PostMapping("/5-bieu-do-cot-top-categories")
    @Operation(summary = "ROLE-ADMIN Lấy top danh mục")
    public ResponseEntity<ApiResponse<List<TopCategoryResponse>>> getTopCategories(
            @AuthenticationPrincipal Jwt jwt) {

        // Kiểm tra quyền ADMIN
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || !scope.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<TopCategoryResponse>>builder()
                            .code(403)
                            .message("Không có quyền truy cập")
                            .build());
        }

        List<TopCategoryResponse> data = dashboardService.getTopCategories();
        return ResponseEntity.ok(
                ApiResponse.<List<TopCategoryResponse>>builder()
                        .message("Lấy top danh mục thành công")
                        .data(data)
                        .build()
        );
    }

    // ==================== TOP PRODUCTS ====================
    @PostMapping("/6--bieu-do-cot-top-products")
    @Operation(summary = "ROLE-ADMIN Lấy top sản phẩm")
    public ResponseEntity<ApiResponse<List<TopProductResponse>>> getTopProducts(
            @AuthenticationPrincipal Jwt jwt) {

        // Kiểm tra quyền ADMIN
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || !scope.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<TopProductResponse>>builder()
                            .code(403)
                            .message("Không có quyền truy cập")
                            .build());
        }

        List<TopProductResponse> data = dashboardService.getTopProducts();
        return ResponseEntity.ok(
                ApiResponse.<List<TopProductResponse>>builder()
                        .message("Lấy top sản phẩm thành công")
                        .data(data)
                        .build()
        );
    }
}

