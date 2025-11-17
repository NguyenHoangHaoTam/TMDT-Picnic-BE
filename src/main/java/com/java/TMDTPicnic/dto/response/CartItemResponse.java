package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Double price;
    private Integer quantity;
    private Double subtotal;
}
