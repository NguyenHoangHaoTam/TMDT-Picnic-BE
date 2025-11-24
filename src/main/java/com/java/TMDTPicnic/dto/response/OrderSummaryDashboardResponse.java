package com.java.TMDTPicnic.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSummaryDashboardResponse {
    private OrderStatusResponse ordersStatus;
}
