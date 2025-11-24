package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.AddressRequest;
import com.java.TMDTPicnic.dto.response.AddressResponse;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    private boolean isAdmin(Jwt jwt) {
        return jwt != null && "ADMIN".equalsIgnoreCase(jwt.getClaimAsString("role"));
    }

    private Long getCurrentUserId(Jwt jwt) {
        // JWT claim “userId” chứa id người dùng khi đăng nhập
        if (jwt == null) return null;
        Object idClaim = jwt.getClaim("userId");
        if (idClaim == null) idClaim = jwt.getClaim("id");
        if (idClaim == null) idClaim = jwt.getSubject(); // "sub"
        return idClaim != null ? Long.valueOf(idClaim.toString()) : null;
    }

    // === CREATE ADDRESS (USER chỉ được tạo cho chính mình) ===
    @PostMapping
    @Operation(summary = "Tạo địa chỉ mới cho người dùng")
    public ResponseEntity<ApiResponse<AddressResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long userId,
            @RequestBody AddressRequest request
    ) {
        Long currentUserId = getCurrentUserId(jwt);

        if (currentUserId == null && userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<AddressResponse>builder()
                            .message("Không xác định được người dùng")
                            .build());
        }

        boolean admin = isAdmin(jwt);
        Long targetUserId = admin ? (userId != null ? userId : currentUserId) : currentUserId;

        if (!admin && targetUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<AddressResponse>builder()
                            .message("Không xác định được người dùng")
                            .build());
        }

        if (!admin && !targetUserId.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<AddressResponse>builder()
                            .message("Bạn không có quyền tạo địa chỉ cho người khác")
                            .build());
        }

        AddressResponse response = addressService.createAddress(targetUserId, request);
        return ResponseEntity.ok(
                ApiResponse.<AddressResponse>builder()
                        .message("Tạo địa chỉ thành công")
                        .data(response)
                        .build()
        );
    }

    // === UPDATE ADDRESS (Chỉ chủ sở hữu mới được cập nhật) ===
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin địa chỉ (Chỉ chủ sở hữu)")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody AddressRequest request
    ) {
        Long currentUserId = getCurrentUserId(jwt);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<AddressResponse>builder()
                            .message("Không xác định được người dùng")
                            .build());
        }

        // Chỉ cho phép user cập nhật địa chỉ của chính mình (không cho admin cập nhật địa chỉ của user khác)
        AddressResponse response = addressService.updateAddressIfOwnedByUser(id, currentUserId, request);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<AddressResponse>builder()
                            .message("Bạn không có quyền cập nhật địa chỉ này")
                            .build());
        }

        return ResponseEntity.ok(
                ApiResponse.<AddressResponse>builder()
                        .message("Cập nhật địa chỉ thành công")
                        .data(response)
                        .build()
        );
    }

    // === DELETE ADDRESS (ADMIN only) ===
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa địa chỉ theo ID (Admin hoặc chủ sở hữu)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        // Admin: được xoá mọi địa chỉ
        if (isAdmin(jwt)) {
            addressService.deleteAddress(id);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .message("Xóa địa chỉ thành công")
                            .build()
            );
        }
        // User thường: chỉ được xoá địa chỉ của chính mình
        Long currentUserId = getCurrentUserId(jwt);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Void>builder()
                            .message("Không xác định được người dùng")
                            .build());
        }
        boolean deleted = addressService.deleteAddressIfOwnedByUser(id, currentUserId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Void>builder()
                            .message("Bạn không có quyền xoá địa chỉ này")
                            .build());
        }
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Xóa địa chỉ thành công")
                        .build()
        );
    }

    // === GET USER ADDRESSES (USER chỉ xem của chính mình) ===
    @GetMapping
    @Operation(summary = "Lấy danh sách địa chỉ của người dùng")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long userId
    ) {
        Long currentUserId = getCurrentUserId(jwt);

        if (currentUserId == null && userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<List<AddressResponse>>builder()
                            .message("Không xác định được người dùng")
                            .build());
        }

        boolean admin = isAdmin(jwt);
        Long targetUserId = admin ? (userId != null ? userId : currentUserId) : currentUserId;

        if (!admin && targetUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<List<AddressResponse>>builder()
                            .message("Không xác định được người dùng")
                            .build());
        }

        if (!admin && !targetUserId.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<AddressResponse>>builder()
                            .message("Bạn không có quyền xem địa chỉ của người khác")
                            .build());
        }

        List<AddressResponse> addresses = addressService.getUserAddresses(targetUserId);
        return ResponseEntity.ok(
                ApiResponse.<List<AddressResponse>>builder()
                        .message("Lấy danh sách địa chỉ thành công")
                        .data(addresses)
                        .build()
        );
    }

    // === SET DEFAULT ADDRESS (Admin hoặc chủ sở hữu) ===
    @PatchMapping("/{id}/default")
    @Operation(summary = "Đặt địa chỉ mặc định")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        Long currentUserId = getCurrentUserId(jwt);
        boolean admin = isAdmin(jwt);
        if (!admin && currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<AddressResponse>builder()
                            .message("Không xác định được người dùng")
                            .build());
        }

        try {
            AddressResponse resp = addressService.setDefaultAddress(id, currentUserId, admin);
            return ResponseEntity.ok(
                    ApiResponse.<AddressResponse>builder()
                            .message("Cập nhật địa chỉ mặc định thành công")
                            .data(resp)
                            .build()
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<AddressResponse>builder()
                            .message(ex.getMessage())
                            .build());
        }
    }
}