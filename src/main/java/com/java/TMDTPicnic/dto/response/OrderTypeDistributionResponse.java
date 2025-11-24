package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderTypeDistributionResponse {
    private String orderType; // SINGLE, GROUP, SHARED_CART
    private Long count;
}

