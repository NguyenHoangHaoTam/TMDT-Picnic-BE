package com.java.TMDTPicnic.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CouponCreateResponse {
    private Long id;
    private String code;
    private String message;
    private LocalDateTime createdAt;
}

