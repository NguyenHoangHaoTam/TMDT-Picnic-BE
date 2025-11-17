package com.java.TMDTPicnic.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class CartRequest {
    private Long userId;
    private List<CartItemRequest> cartItem;
}
