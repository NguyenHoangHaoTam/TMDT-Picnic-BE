package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopCategoryResponse {
    private String categoryName;
    private Long productsSold;
}
