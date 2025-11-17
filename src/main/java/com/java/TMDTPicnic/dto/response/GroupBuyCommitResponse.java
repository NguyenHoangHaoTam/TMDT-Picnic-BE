package com.java.TMDTPicnic.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Setter
@Getter
@AllArgsConstructor   // ✅ Tạo constructor có tất cả tham số
@NoArgsConstructor    // ✅ Tạo constructor mặc định (rỗng)
public class GroupBuyCommitResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private Integer qtyCommitted;
    private BigDecimal amountPaid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime committedAt;
}
