package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersKpiResponse {
    private Long totalOrders;
    private Long completedOrders;
    private Long pendingOrders;
    private Long shippedOrders;
    private Long paidOrders;
    private Double averageOrdersPerDay;
}




