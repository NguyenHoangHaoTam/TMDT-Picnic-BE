package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private List<CategoryResponse> children; // để load cây danh mục
}
