package com.java.TMDTPicnic.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.java.TMDTPicnic.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderSummaryResponse {
    private Long id;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String orderType;
    private String firstProductThumbnail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
