package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ReviewStatsResponse {
    private Long newReviewsThisWeek;
    private Long newReviewsThisMonth;
    private Long hiddenReviews;
    private List<TopProductResponse> topRatedProducts;
    private List<TopProductResponse> lowRatedProducts;
}

