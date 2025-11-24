package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusUpdateResponse {
    private Long orderId;
    private String oldStatus;
    private String newStatus;
    private String message;
}
