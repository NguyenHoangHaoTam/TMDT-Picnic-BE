package com.java.TMDTPicnic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "shared_cart_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SharedCartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shared_cart_id")
    private SharedCart sharedCart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private Integer quantity;
    private BigDecimal priceAtAdd;
}