package com.java.TMDTPicnic.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

public class GroupBuyCommitRequest {
    private Long campaignId;
    private Integer qtyCommitted;
    private BigDecimal amountPaid; // optional nếu có thanh toán trước
}