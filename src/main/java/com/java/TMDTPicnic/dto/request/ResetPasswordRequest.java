package com.java.TMDTPicnic.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String email;
    private String code;       // code gửi qua mail
    private String newPassword;
}
