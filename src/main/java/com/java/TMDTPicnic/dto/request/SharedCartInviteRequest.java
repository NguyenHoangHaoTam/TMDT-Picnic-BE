package com.java.TMDTPicnic.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SharedCartInviteRequest  {
    private Long sharedCartId;
    private List<String> identifiers;
    private BigDecimal contributionAmount;
}