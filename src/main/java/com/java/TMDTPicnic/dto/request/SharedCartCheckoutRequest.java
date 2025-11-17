package com.java.TMDTPicnic.dto.request;

import com.java.TMDTPicnic.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SharedCartCheckoutRequest {
    private Long sharedCartId;
    private PaymentMethod paymentMethod; // COD, VNPAY, MOMO
    private String couponCode;
}

