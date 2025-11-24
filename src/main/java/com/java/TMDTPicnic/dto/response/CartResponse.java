package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class CartResponse {
    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private Double totalAmount;
    private Integer totalItems;
}
