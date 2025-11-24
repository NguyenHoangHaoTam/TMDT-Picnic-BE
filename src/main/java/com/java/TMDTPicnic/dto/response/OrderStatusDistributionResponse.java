package com.java.TMDTPicnic.dto.response;

import com.java.TMDTPicnic.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusDistributionResponse {
    private OrderStatus status; // PENDING, PAID, SHIPPED, DELIVERED, CANCELLED, COMPLETED
    private Long count;
}

