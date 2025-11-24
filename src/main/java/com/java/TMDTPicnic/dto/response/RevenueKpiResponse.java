package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueKpiResponse {
    private BigDecimal totalRevenue;
    private BigDecimal averageDailyRevenue;
    private BigDecimal maxDailyRevenue;
    private BigDecimal minDailyRevenue;
    private Long totalOrders;
}




