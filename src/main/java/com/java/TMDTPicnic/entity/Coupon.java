package com.java.TMDTPicnic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private BigDecimal discountValue; // absolute or percent
    private Boolean isPercent;
    private Integer usageLimit; // total uses
    private Integer usedCount = 0; // đếm số lượt đã dùng
}
