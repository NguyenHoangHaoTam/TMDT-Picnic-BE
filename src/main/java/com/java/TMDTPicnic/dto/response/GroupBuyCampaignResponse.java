package com.java.TMDTPicnic.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.java.TMDTPicnic.entity.GroupBuyCommit;
import com.java.TMDTPicnic.enums.GroupBuyStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class GroupBuyCampaignResponse {
    private Long id;
    private ProductSummaryResponse product;
    private Integer minQtyToUnlock;
    private BigDecimal discountedPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;

    private GroupBuyStatus status;

    private Integer totalCommittedQty; // tổng số lượng đã commit
    private List<GroupBuyCommit> commits; // danh sách người tham gia
}