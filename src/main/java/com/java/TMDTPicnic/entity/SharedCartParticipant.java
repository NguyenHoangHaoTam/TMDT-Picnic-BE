package com.java.TMDTPicnic.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shared_cart_participants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SharedCartParticipant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shared_cart_id")
    private SharedCart sharedCart;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal contributionAmount; // optional

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedAt;
}
