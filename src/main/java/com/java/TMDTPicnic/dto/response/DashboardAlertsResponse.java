package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardAlertsResponse {
    private Long overdueOrders; // orders with status PENDING older than 24 hours
    private Long lowStockProducts;
    private Long endingGroupBuyCampaigns;
    private Long expiringSharedCarts;
    private List<AlertDetail> alertDetails;

    @Data
    @AllArgsConstructor
    public static class AlertDetail {
        private String type; // ORDER_OVERDUE, LOW_STOCK, GROUP_BUY_ENDING, SHARED_CART_EXPIRING
        private String message;
        private Long count;
    }
}

