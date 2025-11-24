package com.java.TMDTPicnic.dto.response;

import java.math.BigDecimal;

public class ProductSummaryResponse {
    private Long id;
    private String name;
    private String slug;
    private String thumbnail; // lấy từ ProductImage đầu tiên (nếu có)
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private String unit;
}