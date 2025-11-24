package com.java.TMDTPicnic.dto.request;

import java.time.LocalDateTime;

public class CouponUsageDTORequest {
    private Long id;
    private String couponCode;
    private Long userId;
    private String username;
    private LocalDateTime usedAt;
    private Long orderId; // liên kết với đơn hàng đã dùng
}
