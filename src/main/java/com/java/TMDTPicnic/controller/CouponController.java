package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.CouponCreateRequest;
import com.java.TMDTPicnic.dto.request.CouponUpdateRequest;
import com.java.TMDTPicnic.dto.response.CouponCreateResponse;
import com.java.TMDTPicnic.dto.response.CouponDTOResponse;
import com.java.TMDTPicnic.dto.response.CouponPageResponse;
import com.java.TMDTPicnic.service.CouponService;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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


    @GetMapping("/admin")
    @Operation(summary = "Admin lấy danh sách mã giảm giá")
    public ResponseEntity<ApiResponse<CouponPageResponse>> getCoupons(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "validTo"));
        CouponPageResponse response = couponService.getCoupons(pageable, search, status);

        return ResponseEntity.ok(
                ApiResponse.<CouponPageResponse>builder()
                        .message("Lấy danh sách mã giảm giá thành công")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Admin cập nhật mã giảm giá")
    public ResponseEntity<ApiResponse<CouponDTOResponse>> updateCoupon(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody CouponUpdateRequest request) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        CouponDTOResponse dto = couponService.updateCoupon(id, request);

        return ResponseEntity.ok(
                ApiResponse.<CouponDTOResponse>builder()
                        .message("Cập nhật mã giảm giá thành công")
                        .data(dto)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Admin xóa mã giảm giá")
    public ResponseEntity<ApiResponse<String>> deleteCoupon(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        couponService.deleteCoupon(id);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Xóa mã giảm giá thành công")
                        .data("OK")
                        .build()
        );
    }

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

