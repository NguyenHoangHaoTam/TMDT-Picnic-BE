package com.java.TMDTPicnic.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String unit;
    private Boolean isActive;
    private Long categoryId;
    private Boolean isFeatured;
    private BigDecimal discountRate;

//    private List<ProductImageRequest> images;
}