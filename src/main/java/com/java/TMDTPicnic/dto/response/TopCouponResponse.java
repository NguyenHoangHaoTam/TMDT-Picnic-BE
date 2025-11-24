package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopCouponResponse {
    private Long couponId;
    private String code;
    private Integer usedCount;
    private Integer usageLimit;
}

