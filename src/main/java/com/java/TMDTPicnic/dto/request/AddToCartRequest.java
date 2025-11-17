package com.java.TMDTPicnic.dto.request;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AddToCartRequest {
    private Long productId;
    private Integer quantity;
}
