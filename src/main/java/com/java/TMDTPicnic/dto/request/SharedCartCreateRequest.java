package com.java.TMDTPicnic.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.java.TMDTPicnic.entity.User;
import com.java.TMDTPicnic.enums.SharedCartStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SharedCartCreateRequest {
    private String title;
    private Long ownerId; //userId
    private LocalDateTime expiresAt;
}

