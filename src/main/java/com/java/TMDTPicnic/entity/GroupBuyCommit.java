package com.java.TMDTPicnic.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_buy_commits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupBuyCommit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private GroupBuyCampaign campaign;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Integer qtyCommitted;
    private BigDecimal amountPaid; // if pre-paid

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime committedAt;
}
