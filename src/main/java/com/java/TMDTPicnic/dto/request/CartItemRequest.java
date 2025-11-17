package com.java.TMDTPicnic.dto.request;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CartItemRequest {
    private Long id;
    private Long productId;
    private Integer quantity;
}
