package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//Đăng nhập
public class AuthenticationResponse {
    private String token;           // JWT token chính
    private String refreshToken;    // Token để làm mới phiên
    private boolean authenticated;  // Cờ xác thực thành công
}
