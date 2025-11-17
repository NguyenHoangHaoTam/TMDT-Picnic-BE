package com.java.TMDTPicnic.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.java.TMDTPicnic.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private Boolean readFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String actionType; // "SHARED_CART_INVITATION", etc.

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string: {"sharedCartId": 1, "sharedCartTitle": "...", "inviterName": "...", "inviterId": 1}
}
