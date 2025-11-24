package com.java.TMDTPicnic.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String type; // "INFO", "WARNING", "SUCCESS"
    private Boolean readFlag;
    private String createdAt;
    private String actionType; // "SHARED_CART_INVITATION"
    private Map<String, Object> metadata; // {"sharedCartId": 1, "sharedCartTitle": "...", "inviterName": "...", "inviterId": 1}
}

