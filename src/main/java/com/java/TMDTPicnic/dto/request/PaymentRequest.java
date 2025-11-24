package com.java.TMDTPicnic.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentRequest {
    private Long orderId;
    private String paymentMethod; // VNPAY
    private BigDecimal amount;
}
