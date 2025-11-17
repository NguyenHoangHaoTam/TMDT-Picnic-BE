package com.java.TMDTPicnic.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CheckoutItemRequest {
    private Long productId;
    private Integer qty;
    private BigDecimal unitPrice;
}
