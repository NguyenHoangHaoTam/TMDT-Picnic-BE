package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemDetailResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productThumbnail;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}

