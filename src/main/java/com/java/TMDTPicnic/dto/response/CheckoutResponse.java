package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CheckoutResponse {
    private Long orderId;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String message; // e.g. "Order created successfully"
}
