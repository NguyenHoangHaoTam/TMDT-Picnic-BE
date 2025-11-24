package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CouponStatsResponse {
    private Long activeCoupons; // validFrom <= now <= validTo
    private Long expiredCoupons;
    private Long fullyUsedCoupons; // usedCount >= usageLimit
    private Double usageRate; // percentage of orders using coupons
    private List<TopCouponResponse> topUsedCoupons;
}

