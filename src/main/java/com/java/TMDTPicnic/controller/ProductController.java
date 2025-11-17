package com.java.TMDTPicnic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.TMDTPicnic.dto.request.ProductRequest;
import com.java.TMDTPicnic.dto.response.ProductPageResponse;
import com.java.TMDTPicnic.dto.response.ProductResponse;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper mapper = new ObjectMapper();

    // === TẠO SẢN PHẨM (ADMIN) ===
    @PostMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "ROLE-ADMIN Tạo mới sản phẩm")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {

        if (!"ROLE_ADMIN".equals(jwt.getClaimAsString("scope"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ProductResponse>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        ProductRequest request = mapper.readValue(productJson, ProductRequest.class);
        ProductResponse createdProduct = productService.createProduct(request, images);

        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Tạo sản phẩm thành công")
                        .data(createdProduct)
                        .build()
        );
    }
    // === LẤY CHI TIẾT SẢN PHẨM THEO SLUG (DẠNG CHỮ) ===
    @GetMapping("/detail/{slug}")
    @Operation(summary = "Lấy chi tiết sản phẩm theo Slug")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySlug(@PathVariable String slug) { // <-- Nhận String
        ProductResponse product = productService.getProductBySlug(slug); // Giả sử bạn có hàm này
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Lấy sản phẩm thành công")
                        .data(product)
                        .build()
        );
    }
    // === LẤY DANH SÁCH SẢN PHẨM (Phân trang) ===
    @GetMapping("/all")
    @Operation(summary = "Lấy tất cả sản phẩm (phân trang, rút gọn metadata)")
    public ResponseEntity<ApiResponse<ProductPageResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductResponse> productsPage = productService.getAllProducts(page, size);

        ProductPageResponse response = new ProductPageResponse(
                productsPage.getContent(),
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isFirst(),
                productsPage.isLast()
        );

        return ResponseEntity.ok(
                ApiResponse.<ProductPageResponse>builder()
                        .message("Lấy tất cả sản phẩm thành công")
                        .data(response)
                        .build()
        );
    }

    // === LỌC SẢN PHẨM THEO LOẠI (mới nhất, giảm giá, bán chạy) ===
    @GetMapping("/filter")
    @Operation(summary = "Lọc sản phẩm theo loại (newest, discount, best-seller)")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByFilter(
            @RequestParam String filter) {

        List<ProductResponse> products = productService.getProductsByFilter(filter);

        String message = switch (filter.toLowerCase()) {
            case "newest" -> "mới nhất";
            case "discount" -> "giảm giá";
            case "best-seller" -> "bán chạy";
            case "featured" -> "nổi bật";
            default -> "";
        };
        return ResponseEntity.ok(
                ApiResponse.<List<ProductResponse>>builder()
                        .message("Lấy danh sách sản phẩm " + message + " thành công")
                        .data(products)
                        .build()
        );
    }
    // Lấy sản phẩm theo categoryId
    @Operation(summary = "Lấy danh sách sản phẩm theo Category ID (phân trang, rút gọn metadata)")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<ProductPageResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductResponse> productsPage = productService.getProductsByCategoryId(categoryId, page, size);

        ProductPageResponse response = new ProductPageResponse(
                productsPage.getContent(),
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isFirst(),
                productsPage.isLast()
        );

        return ResponseEntity.ok(
                ApiResponse.<ProductPageResponse>builder()
                        .message("Lấy danh sách sản phẩm theo thể loại thành công")
                        .data(response)
                        .build()
        );
    }

    // === LẤY CHI TIẾT SẢN PHẨM THEO ID ===
    @GetMapping("/detail/{id}")
    @Operation(summary = "Lấy chi tiết sản phẩm theo ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Lấy sản phẩm thành công")
                        .data(product)
                        .build()
        );
    }

    // === CẬP NHẬT SẢN PHẨM (ADMIN) ===
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Operation(summary = "ROLE-ADMIN Cập nhật thông tin sản phẩm")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {

        if (!"ROLE_ADMIN".equals(jwt.getClaimAsString("scope"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ProductResponse>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        ProductRequest request = mapper.readValue(productJson, ProductRequest.class);
        ProductResponse updatedProduct = productService.updateProduct(id, request, images);

        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Cập nhật sản phẩm thành công")
                        .data(updatedProduct)
                        .build()
        );
    }

    // === XÓA SẢN PHẨM (ADMIN) ===
    @DeleteMapping("/{id}")
    @Operation(summary = "ROLE-ADMIN Xóa sản phẩm theo id")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        if (!"ROLE_ADMIN".equals(jwt.getClaimAsString("scope"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Void>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Xoá sản phẩm thành công")
                        .build()
        );
    }
    // =============================
    // ẨN SẢN PHẨM
    // =============================
    @PatchMapping("/{id}/hide")
    @Operation(summary = "ROLE-ADMIN Ẩn sản phẩm theo id")
    public ResponseEntity<ApiResponse<ProductResponse>> hideProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        if (!"ROLE_ADMIN".equals(jwt.getClaimAsString("scope"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ProductResponse>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        ProductResponse response = productService.hideProduct(id);
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Ẩn sản phẩm thành công")
                        .data(response)
                        .build()
        );
    }

    // =============================
    // HIỆN SẢN PHẨM
    // =============================
    @PatchMapping("/{id}/unhide")
    @Operation(summary = "ROLE-ADMIN Hiện lại sản phẩm theo id")
    public ResponseEntity<ApiResponse<ProductResponse>> unhideProduct(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        if (!"ROLE_ADMIN".equals(jwt.getClaimAsString("scope"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<ProductResponse>builder()
                            .message("Không có quyền thực hiện")
                            .build());
        }

        ProductResponse response = productService.unhideProduct(id);
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .message("Hiện sản phẩm thành công")
                        .data(response)
                        .build()
        );
    }
}
