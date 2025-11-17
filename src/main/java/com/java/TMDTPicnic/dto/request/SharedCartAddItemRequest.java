package com.java.TMDTPicnic.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SharedCartAddItemRequest {
    private Long sharedCartId;
    private Long productId;
    private Long addByUserId;
    private Integer quantity;
    private BigDecimal priceAtAdd;

    // Gom các request nhỏ vào đây
    @Data
    @Builder
    public static class UpdateQuantityRequest {
        private Long sharedCartId;
        private Long productId;
        private Integer quantity;
    }

    @Data
    @Builder
    public static class RemoveItemRequest {
        private Long sharedCartId;
        private Long productId;
    }

    @Data
    @Builder
    public static class UpdateContributionRequest {
        private Long sharedCartId;
        private Long userId;
        private BigDecimal contributionAmount;
    }
}