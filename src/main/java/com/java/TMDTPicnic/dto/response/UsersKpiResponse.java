package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersKpiResponse {
    private Long totalUsers;
    private Long newUsers;
    private Long activeUsers;
    private Double averageNewUsersPerDay;
}




