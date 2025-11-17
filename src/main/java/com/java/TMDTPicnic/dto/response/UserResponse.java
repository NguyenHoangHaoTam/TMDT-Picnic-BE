package com.java.TMDTPicnic.dto.response;

import com.java.TMDTPicnic.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Boolean isActive;
    private String avatar; // nếu có
    private List<AddressResponse> addresses;
}
