package com.java.TMDTPicnic.dto.request;

import com.java.TMDTPicnic.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckoutRequest {
    // 1 trong 2 danh sách này sẽ có dữ liệu, còn lại có thể null hoặc rỗng
    private List<CheckoutItemRequest> directItems;  // Đặt hàng trực tiếp
    private List<CartItemRequest> cartItems;        // Đặt hàng từ giỏ

    private String orderType;                        // "SINGLE" hoặc "GROUP"
    private PaymentMethod paymentMethod;             // COD, VNPAY, MOMO
    private String couponCode;
}