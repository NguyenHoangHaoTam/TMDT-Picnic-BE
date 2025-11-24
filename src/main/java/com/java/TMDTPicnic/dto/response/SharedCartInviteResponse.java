package com.java.TMDTPicnic.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SharedCartInviteResponse  {
    private Long id;
    private Long sharedCartId;
    private Long userId;
    private BigDecimal contributionAmount;
    private LocalDateTime joinedAt;
}

