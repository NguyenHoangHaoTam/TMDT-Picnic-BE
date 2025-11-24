package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyCampaignCreateResponse {
    private Long id;
    private String productName;
    private String status; // e.g. "ACTIVE"
    private String message; // "Group buy campaign created successfully"
}
