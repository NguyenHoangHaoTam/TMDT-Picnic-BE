package com.java.TMDTPicnic.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyCouponRequest {
    private String code;           // mã người dùng nhập
    private BigDecimal orderTotal; // tổng giá trị đơn hàng trước khi giảm
}
