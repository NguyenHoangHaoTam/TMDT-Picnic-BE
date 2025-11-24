package com.java.TMDTPicnic.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat; // cần cho @JsonFormat
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime; // cần cho LocalDateTime

@Getter
@Setter

public class GroupBuyCampaignCreateRequest {
    private Long productId;
    private Integer minQtyToUnlock;
    private BigDecimal discountedPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
}

