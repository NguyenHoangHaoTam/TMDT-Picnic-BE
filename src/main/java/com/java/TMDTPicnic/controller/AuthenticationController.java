package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.config.CookieProperties;
import com.java.TMDTPicnic.dto.request.AuthenticationRequest;
import com.java.TMDTPicnic.dto.request.ForgotPasswordRequest;
import com.java.TMDTPicnic.dto.request.IntrospectRequest;
import com.java.TMDTPicnic.dto.request.RefreshTokenRequest;
import com.java.TMDTPicnic.dto.request.RegisterRequest;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.dto.response.AuthenticationResponse;
import com.java.TMDTPicnic.exception.AppException;
import com.java.TMDTPicnic.exception.ErrorCode;
import com.java.TMDTPicnic.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final CookieProperties cookieProperties;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
        authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Đăng ký thành công, vui lòng đăng nhập để tiếp tục.")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @RequestBody AuthenticationRequest request) {

        AuthenticationResponse authenticationResponse = authenticationService.login(request);

        // Không set cookie để tránh xung đột giữa user và admin
        // Frontend sẽ lưu refresh token trong sessionStorage và gửi qua body khi refresh
        // Điều này cho phép nhiều user/admin đăng nhập cùng lúc trên cùng máy (nhiều tab/trình duyệt)

        return ResponseEntity.ok(ApiResponse
                .<AuthenticationResponse>builder()
                .data(authenticationResponse)
                .message("Đăng nhập thành công")
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody IntrospectRequest request,
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) throws ParseException, JOSEException {

        authenticationService.logout(request, refreshToken);

        ResponseCookie clearCookie = buildCookie("", 0);
        response.setHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Đăng xuất thành công")
                .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = "refresh_token", required = false) String refreshTokenFromCookie
    ) throws ParseException, JOSEException {
        // Ưu tiên đọc refresh token từ body, nếu không có thì mới đọc từ cookie (backward compatibility)
        // Điều này cho phép nhiều user/admin đăng nhập cùng lúc trên cùng máy (nhiều tab/trình duyệt)
        String refreshToken = (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isEmpty())
                ? request.getRefreshToken()
                : refreshTokenFromCookie;

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        AuthenticationResponse newAccessToken = authenticationService.refreshAccessToken(refreshToken);

        // Không set cookie để tránh xung đột giữa user và admin
        // Frontend sẽ lưu refresh token mới trong sessionStorage và gửi qua body khi refresh tiếp theo
        // Refresh token mới (nếu có) sẽ được trả về trong response body

        return ResponseEntity.ok()
                .body(ApiResponse.<AuthenticationResponse>builder()
                        .data(newAccessToken)
                        .message("Cập nhật access token thành công")
                        .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody ForgotPasswordRequest request) throws MessagingException, IOException {
        authenticationService.generateVerificationCode(request);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Mật khẩu mới đã được gửi.")
                .build());
    }

    private ResponseCookie buildCookie(String value, long maxAge) {
        return ResponseCookie.from("refresh_token", value)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
