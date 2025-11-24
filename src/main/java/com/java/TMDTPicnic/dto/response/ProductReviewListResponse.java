package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewListResponse {
    private Long productId;
    private String productName;
    private Double averageRating;
    private Integer totalReviews;
    private List<ReviewDTO> reviews;
}

