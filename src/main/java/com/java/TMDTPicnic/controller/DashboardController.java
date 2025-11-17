package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.dto.response.DashboardResponse;
import com.java.TMDTPicnic.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "ROLE-ADMIN Lấy dữ liệu dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
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

        DashboardResponse dashboardData = dashboardService.getDashboardData();
        return ResponseEntity.ok(
                ApiResponse.<DashboardResponse>builder()
                        .message("Lấy dữ liệu dashboard thành công")
                        .data(dashboardData)
                        .build()
        );
    }
}

