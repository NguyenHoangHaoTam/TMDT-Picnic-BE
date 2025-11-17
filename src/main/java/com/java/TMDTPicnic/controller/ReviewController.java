package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.ReviewAdminActionRequest;
import com.java.TMDTPicnic.dto.request.ReviewCreateRequest;
import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * User tạo đánh giá sản phẩm
     */
    @PostMapping
    @Operation(summary = "Tạo đánh giá sản phẩm")
    public ResponseEntity<ApiResponse<ReviewCreateResponse>> createReview(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReviewCreateRequest request) {

        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        ReviewCreateResponse response = reviewService.createReview(userId, request);

        return ResponseEntity.ok(
                ApiResponse.<ReviewCreateResponse>builder()
                        .message(response.getMessage())
                        .data(response)
                        .build()
        );
    }

    /**
     * Xem danh sách đánh giá của sản phẩm
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Xem danh sách đánh giá của sản phẩm")
    public ResponseEntity<ApiResponse<ProductReviewListResponse>> getProductReviews(
            @PathVariable Long productId) {

        ProductReviewListResponse response = reviewService.getProductReviews(productId);

        return ResponseEntity.ok(
                ApiResponse.<ProductReviewListResponse>builder()
                        .message("Lấy danh sách đánh giá thành công")
                        .data(response)
                        .build()
        );
    }

    /**
     * Admin: Ẩn hoặc xóa đánh giá
     */
    @PutMapping("/admin/action")
    @Operation(summary = "ROLE-ADMIN Ẩn hoặc xóa đánh giá")
    public ResponseEntity<ApiResponse<ReviewAdminActionResponse>> adminAction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReviewAdminActionRequest request) {

        // Kiểm tra quyền ADMIN
        String scope = jwt.getClaimAsString("scope");
        if (scope == null || !scope.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ReviewAdminActionResponse>builder()
                            .code(403)
                            .message("Không có quyền truy cập")
                            .build());
        }

        ReviewAdminActionResponse response = reviewService.adminAction(request);

        return ResponseEntity.ok(
                ApiResponse.<ReviewAdminActionResponse>builder()
                        .message(response.getMessage())
                        .data(response)
                        .build()
        );
    }
}

