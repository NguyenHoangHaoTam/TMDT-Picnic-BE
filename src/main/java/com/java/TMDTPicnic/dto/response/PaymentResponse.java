package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private String status;
    private String message;

}
