package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.ApplyCouponRequest;
import com.java.TMDTPicnic.dto.request.CouponCreateRequest;
import com.java.TMDTPicnic.dto.response.ApplyCouponResponse;
import com.java.TMDTPicnic.dto.response.CouponCreateResponse;
import com.java.TMDTPicnic.dto.response.CouponDTOResponse;
import com.java.TMDTPicnic.service.CouponService;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;


@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /**
     * [Admin] Tạo mã giảm giá
     */
    @PostMapping("/create")
    @Operation(summary = "Admin tạo mã giảm giá")
    public ResponseEntity<ApiResponse<CouponCreateResponse>> createCoupon(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CouponCreateRequest request) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));

        CouponCreateResponse response = couponService.createCoupon(userId, request);

        return ResponseEntity.ok(
                ApiResponse.<CouponCreateResponse>builder()
                        .message("Tạo mã giảm giá thành công")
                        .data(response)
                        .build()
        );
    }


    /**
     * Lấy thông tin mã giảm giá
     */
    @GetMapping("/{code}")
    @Operation(summary = "Lấy thông tin mã giảm giá")
    public ResponseEntity<ApiResponse<CouponDTOResponse>> getCouponInfo(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String code) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));

        CouponDTOResponse dto = couponService.getCouponInfo(code);

        if (dto == null) {
            return ResponseEntity.status(404).body(
                    ApiResponse.<CouponDTOResponse>builder()
                            .message("Không tìm thấy mã giảm giá")
                            .data(null)
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponse.<CouponDTOResponse>builder()
                        .message("Lấy thông tin mã giảm giá thành công")
                        .data(dto)
                        .build()
        );
    }
}

