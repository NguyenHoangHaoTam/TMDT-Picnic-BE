package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupBuyStatsResponse {
    private Long activeCampaigns;
    private Long endingSoonCount; // ends within 7 days
    private Long successfulCampaigns;
    private Long failedCampaigns;
}

