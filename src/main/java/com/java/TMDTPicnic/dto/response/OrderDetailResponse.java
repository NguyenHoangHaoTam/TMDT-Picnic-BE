package com.java.TMDTPicnic.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.java.TMDTPicnic.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDetailResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String orderType;
    private Long couponId;
    private String couponCode;
    private BigDecimal discountAmount;
    private Long sharedCartId;
    private String sharedCartTitle;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    private PaymentDetailResponse payment;
    private List<OrderItemDetailResponse> items;
    private AddressResponse shippingAddress;
}

