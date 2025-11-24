package com.java.TMDTPicnic.dto.response;

import com.java.TMDTPicnic.enums.SharedCartStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SharedCartCreateResponse {
    private Long id;
    private String title;
    private Long ownerId;
    private String message; // e.g. "Created share cart"
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private SharedCartStatus status; //OPEN, COMPLETE, CANCELLED
}
