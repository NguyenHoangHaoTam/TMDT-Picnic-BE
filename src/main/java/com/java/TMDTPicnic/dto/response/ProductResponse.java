package com.java.TMDTPicnic.dto.response;

import jdk.jshell.Snippet;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String unit;
    private Boolean isActive;
    private Long categoryId;
    private String categoryName;
    private List<ProductImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private BigDecimal discountRate;
    private Boolean isFeatured;
    private Integer soldQuantity;
}
