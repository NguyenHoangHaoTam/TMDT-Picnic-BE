package com.java.TMDTPicnic.dto.request;

import lombok.*;

@Data @Builder
public class CategoryRequest {
    private String name;
    private String description;
    private Long parentId; // null nếu là danh mục gốc
}