package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class OrderStatusResponse {
    private Long totalOrders;
    private Long completedOrders;
    private Long pendingOrders;
    private Long paidOrders;
    private Long shippedOrders;


    public OrderStatusResponse(Number totalorders, Number completedOrders, Number processingOrders, Number paidOrders, Number shippedOrders) {
        this.totalOrders = totalorders != null ? totalorders.longValue() : 0L;
        this.completedOrders = completedOrders != null ? completedOrders.longValue() : 0L;
        this.pendingOrders = processingOrders != null ? processingOrders.longValue() : 0L;
        this.paidOrders = paidOrders != null ? paidOrders.longValue() : 0L;
        this.shippedOrders = shippedOrders != null ? shippedOrders.longValue() : 0L;
    }
}
