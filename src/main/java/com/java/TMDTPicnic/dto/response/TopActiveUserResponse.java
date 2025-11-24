package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopActiveUserResponse {
    private Long userId;
    private String username;
    private String email;
    private Long orderCount;
    private Long reviewCount;
}

