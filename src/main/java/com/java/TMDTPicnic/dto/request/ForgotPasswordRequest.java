package com.java.TMDTPicnic.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//Quên mật khẩu
public class ForgotPasswordRequest {
    private String email;
}
