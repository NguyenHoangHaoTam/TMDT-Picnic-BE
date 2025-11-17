package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OrdersByDayResponse {
    private LocalDate date;
    private Long orders;
}
