package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SharedCartAddItemResponse {
    private Long id;
    private Long sharedCartId;
    private Long productId;
    private Long userId;
    private Integer quantity;
    private BigDecimal priceAtAdd;
    private String message; // e.g. "Item added to shared cart"
}
