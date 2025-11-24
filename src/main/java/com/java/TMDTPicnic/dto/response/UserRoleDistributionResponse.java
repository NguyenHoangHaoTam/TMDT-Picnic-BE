package com.java.TMDTPicnic.dto.response;

import com.java.TMDTPicnic.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRoleDistributionResponse {
    private Role role; // ADMIN, USER
    private Long count;


}

