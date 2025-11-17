package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
public class RevenueByMonthResponse {
    private Date month;
    private BigDecimal revenue;
}
