package com.java.TMDTPicnic.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRequest {
    @NotNull(message = "Từ ngày không được để trống ")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @NotNull(message = "Đến ngày không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;
}




