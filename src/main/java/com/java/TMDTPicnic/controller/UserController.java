package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.ChangePasswordRequest;
import com.java.TMDTPicnic.dto.request.ForgotPasswordRequest;
import com.java.TMDTPicnic.dto.request.ResetPasswordRequest;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.dto.response.UserResponse;
import com.java.TMDTPicnic.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            Principal principal) {
        userService.changePassword(request, principal);
        return ResponseEntity.ok("Password changed successfully");
    }

    // Lấy thông tin người dùng hiện tại dựa trên JWT
    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub")); // hoặc claim bạn đặt là username
        UserResponse userResponse = userService.getUserByUsername(userId);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .message("Lấy thông tin người dùng thành công")
                        .data(userResponse)
                        .build()
        );
    }

    // Chỉ ADMIN mới có thể xem toàn bộ danh sách user
    @GetMapping
    @Operation(summary = "ROLE-ADMIN Lấy tất cả người dùng")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(@AuthenticationPrincipal Jwt jwt) {
        String role = jwt.getClaimAsString("scope");

        if (!"ROLE_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<UserResponse>>builder()
                            .message("Không có quyền truy cập danh sách người dùng")
                            .build());
        }

        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.<List<UserResponse>>builder()
                        .message("Lấy danh sách người dùng thành công")
                        .data(users)
                        .build()
        );
    }

    // Ẩn user
    @PutMapping("/{id}/block")
    public ResponseEntity<ApiResponse<Void>> hideUser(@PathVariable Long id) {
        boolean result = userService.blockUserById(id);
        if (result) {
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .message("User " + id + " đã bị chặn.")
                            .build()
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .message("User không tồn tại.")
                            .build());
        }
    }

    // Hiện lại user
    @PutMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<Void>> unhideUser(@PathVariable Long id) {
        boolean result = userService.unblockUserById(id);
        if (result) {
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .message("User " + id + " đã được bỏ chặn.")
                            .build()
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .message("User không tồn tại.")
                            .build());
        }
    }

}

