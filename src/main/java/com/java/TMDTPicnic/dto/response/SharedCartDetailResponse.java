package com.java.TMDTPicnic.dto.response;

import com.java.TMDTPicnic.enums.SharedCartStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SharedCartDetailResponse {
    private Long id;
    private String title;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime expiresAt;
    private SharedCartStatus status;
    private LocalDateTime createdAt;
    private List<ItemDetail> items;
    private List<ParticipantDetail> participants;
    private Double totalAmount;
    private Integer totalItems;
    private PaymentInfo paymentInfo; // Thông tin thanh toán (chỉ có khi status = COMPLETED)

    // Inner class cho Item Detail
    @Data
    @Builder
    public static class ItemDetail {
        private Long id;
        private Long productId;
        private String productName;
        private String productImageUrl;
        private Long addedByUserId;
        private String addedByUserName;
        private Integer quantity;
        private BigDecimal priceAtAdd;
        private BigDecimal subtotal;
    }

    // Inner class cho Participant Detail
    @Data
    @Builder
    public static class ParticipantDetail {
        private Long id;
        private Long userId;
        private String userName;
        private String userEmail;
        private BigDecimal contributionAmount;
        private LocalDateTime joinedAt;
    }

    // Inner class cho Payment Info
    @Data
    @Builder
    public static class PaymentInfo {
        private Long paidBy; // User ID người thanh toán
        private String paidByName; // Tên người thanh toán
        private BigDecimal paidAmount; // Số tiền đã thanh toán
        private LocalDateTime paidAt; // Thời gian thanh toán
        private String paymentMethod; // Phương thức thanh toán (VNPAY, COD, MOMO)
    }
}

