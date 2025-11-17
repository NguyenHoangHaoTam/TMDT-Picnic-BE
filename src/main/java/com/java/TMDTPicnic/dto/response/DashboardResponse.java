package com.java.TMDTPicnic.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DashboardResponse {

    private SummaryResponse summary;

    private List<RevenueByDayResponse> revenueByDay;
    private List<RevenueByMonthResponse> revenueByMonth;

    private List<OrdersByDayResponse> ordersByDay;
    private List<OrdersByMonthResponse> ordersByMonth;

    private List<UserStatsByDayResponse> userStatsByDay;

    private List<TopCategoryResponse> topCategories;
    private List<TopProductResponse> topProducts;

    private OrderStatusResponse orderStatus;
}
