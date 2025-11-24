package com.java.TMDTPicnic.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class ApplyCouponResponse {
    private String code;
    private Boolean valid;
    private String message;
    private BigDecimal discountAmount; // số tiền được giảm
    private BigDecimal finalTotal;     // tổng tiền sau khi giảm
    public boolean isValid() {
        return valid;
    }

}
