package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductStatsResponse {
    private Long totalActive;
    private Long totalInactive;
    private Long lowStockCount; // stockQuantity <= threshold (default 10)
    private Long highRatedCount; // rating > 4
    private Long lowRatedCount; // rating < 3
    private List<TopProductResponse> topRatedProducts;
    private List<TopProductResponse> lowRatedProducts;
}

