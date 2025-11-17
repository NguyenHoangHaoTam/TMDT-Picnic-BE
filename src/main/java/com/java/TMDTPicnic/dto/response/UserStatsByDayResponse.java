package com.java.TMDTPicnic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class UserStatsByDayResponse {
    private Date date;
    private Long newUsers;
    private Long returningUsers;
}