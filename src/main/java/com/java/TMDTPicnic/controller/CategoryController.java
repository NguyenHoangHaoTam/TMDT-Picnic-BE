package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.CategoryRequest;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.dto.response.CategoryResponse;
import com.java.TMDTPicnic.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // === CREATE CATEGORY (ADMIN only) ===
    @PostMapping
    @Operation(summary = "ROLE-ADMIN Thêm thể loại sản phẩm")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CategoryRequest request) {

        if (!jwt.getClaimAsString("scope").equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<CategoryResponse>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        CategoryResponse createdCategory = categoryService.createCategory(request);
        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .message("Tạo danh mục thành công")
                        .data(createdCategory)
                        .build()
        );
    }

    // === GET ALL CATEGORIES (public) ===
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(
                ApiResponse.<List<CategoryResponse>>builder()
                        .message("Lấy danh sách danh mục thành công")
                        .data(categories)
                        .build()
        );
    }

    // === GET CATEGORY BY ID (public) ===
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .message("Lấy danh mục thành công")
                        .data(category)
                        .build()
        );
    }

    // === UPDATE CATEGORY (ADMIN only) ===
    @PutMapping("/{id}")
    @Operation(summary = "ROLE-ADMIN Cập nhật thể loại sản phẩm")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {

        if (!jwt.getClaimAsString("scope").equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<CategoryResponse>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        CategoryResponse updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .message("Cập nhật danh mục thành công")
                        .data(updatedCategory)
                        .build()
        );
    }

    // === DELETE CATEGORY (ADMIN only) ===
    @DeleteMapping("/{id}")
    @Operation(summary = "ROLE-ADMIN Xóa loại sản phẩm theo id")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        if (!jwt.getClaimAsString("scope").equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Void>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Xoá danh mục thành công")
                        .build()
        );
    }
}
