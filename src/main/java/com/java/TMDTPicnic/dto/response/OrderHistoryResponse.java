package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderHistoryResponse {
    private List<OrderSummaryResponse> orders;
}
