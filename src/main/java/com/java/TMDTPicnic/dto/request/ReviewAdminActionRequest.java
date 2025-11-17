package com.java.TMDTPicnic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAdminActionRequest {
    @NotNull(message = "Review ID không được để trống")
    private Long reviewId;

    @NotBlank(message = "Action không được để trống")
    private String action; // "HIDE" hoặc "DELETE"

    private String reason; // tùy chọn
}

