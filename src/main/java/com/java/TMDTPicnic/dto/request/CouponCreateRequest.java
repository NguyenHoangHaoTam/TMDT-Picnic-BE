package com.java.TMDTPicnic.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponCreateRequest {
    private String code;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private BigDecimal discountValue;
    private Boolean isPercent;
    private Integer usageLimit;
}
