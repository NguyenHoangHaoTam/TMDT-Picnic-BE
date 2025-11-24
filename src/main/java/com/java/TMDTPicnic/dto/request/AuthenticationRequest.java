package com.java.TMDTPicnic.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//Đăng nhập
public class AuthenticationRequest {
    private String username;
    private String password;
}
