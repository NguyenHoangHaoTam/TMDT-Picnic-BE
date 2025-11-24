package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SharedCartStatsResponse {
    private Long openCarts;
    private Long closedCarts;
    private Long expiringSoonCount; // expires within 24 hours
    private List<TopProductResponse> topProductsInSharedCarts;
}

