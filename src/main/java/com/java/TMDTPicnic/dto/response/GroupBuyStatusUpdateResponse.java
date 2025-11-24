package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyStatusUpdateResponse {
    private Long campaignId;
    private Integer totalCommittedQty;
    private Integer minQtyToUnlock;
    private String oldStatus;
    private String newStatus;
}
