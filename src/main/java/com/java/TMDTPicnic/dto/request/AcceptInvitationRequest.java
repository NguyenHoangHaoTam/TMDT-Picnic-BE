package com.java.TMDTPicnic.dto.request;

import lombok.Data;

@Data
public class AcceptInvitationRequest {
    private Long notificationId;
    private Long sharedCartId;
}

