package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayResponse {
    private String paymentUrl; // Redirect URL hoặc QR link
    private String gateway; // MOMO / VNPAY
    private String transactionId;
    private String message;
}
