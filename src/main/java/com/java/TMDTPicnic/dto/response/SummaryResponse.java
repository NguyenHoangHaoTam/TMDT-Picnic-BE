package com.java.TMDTPicnic.dto.response;

import lombok.Data;

@Data
public class SummaryResponse {
    private Double totalRevenue;
    private Long totalOrders;
    private Long ordersCompleted;
    private Long totalProducts;
    private Long productsSold;
//    private Double totalProfit;
    private Long totalUsers;
    private Long newUsers;
}
