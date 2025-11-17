package com.java.TMDTPicnic.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponDTOResponse {
    private Long id;
    private String code;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private BigDecimal discountValue;
    private Boolean isPercent;      // true = giảm theo %, false = giảm theo số tiền
    private Integer usageLimit;     // số lượt tối đa
    private Integer usedCount;      // số lượt đã sử dụng
}
