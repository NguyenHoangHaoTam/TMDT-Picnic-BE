package com.java.TMDTPicnic.dto.response;

import com.java.TMDTPicnic.enums.SharedCartStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SharedCartListResponse {
    private Long id;
    private String title;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime expiresAt;
    private SharedCartStatus status;
    private LocalDateTime createdAt;
    private Integer totalItems;
    private Integer totalParticipants;
    private Double totalAmount;

    // Gom SharedCartCreateResponse và SharedCartCloseResponse vào đây
    @Data
    @Builder
    public static class CreateResponse {
        private Long id;
        private String title;
        private Long ownerId;
        private String message;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
        private SharedCartStatus status;
    }

    @Data
    @Builder
    public static class CloseResponse {
        private Long id;
        private String title;
        private String status;
        private String message;
    }
}

