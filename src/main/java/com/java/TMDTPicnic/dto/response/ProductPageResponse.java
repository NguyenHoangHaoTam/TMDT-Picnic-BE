package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductPageResponse {
    private List<ProductResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
