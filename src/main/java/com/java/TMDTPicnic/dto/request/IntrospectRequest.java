package com.java.TMDTPicnic.dto.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntrospectRequest {
    private String token;
    private String refreshToken;
}
