package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.config.CookieProperties;
import com.java.TMDTPicnic.dto.request.AuthenticationRequest;
import com.java.TMDTPicnic.dto.request.ForgotPasswordRequest;
import com.java.TMDTPicnic.dto.request.IntrospectRequest;
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
            @RequestBody AuthenticationRequest request,
            HttpServletResponse response) {

        AuthenticationResponse authenticationResponse = authenticationService.login(request);

        ResponseCookie refreshCookie = buildCookie(
                authenticationResponse.getRefreshToken(),
                30 * 24 * 60 * 60 // 30 ngày
        );

        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

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
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) throws ParseException, JOSEException {

        if (refreshToken == null) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        AuthenticationResponse newAccessToken = authenticationService.refreshAccessToken(refreshToken);

        ResponseCookie refreshCookie = buildCookie(
                newAccessToken.getRefreshToken(),
                30 * 24 * 60 * 60
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
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
