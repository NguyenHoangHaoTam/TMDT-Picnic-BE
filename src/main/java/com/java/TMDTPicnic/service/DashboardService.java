package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.DashboardRequest;
import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    // ==================== DASHBOARD SUMMARY ONLY ====================
    public DashboardResponse getOrderSummary(DashboardRequest request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();

        logger.info("DashboardService.getDashboard called with fromDate={}, toDate={}", fromDate, toDate);

        DashboardResponse response = new DashboardResponse();

        try {
            response.setSummary(buildOrderSummary(fromDate, toDate));
            return response;
        } catch (Exception e) {
            logger.error("Error building dashboard response", e);
            throw e;
        }
    }

    // ==================== SUMMARY BUILDER ====================
    private OrderSummaryDashboardResponse buildOrderSummary(LocalDate fromDate, LocalDate toDate) {
        OrderSummaryDashboardResponse orderSummary = new OrderSummaryDashboardResponse();

        orderSummary.setOrdersStatus(orderRepository.getOrderStatusWithDayRange(fromDate, toDate));
        return orderSummary;
    }

    // ==================== MAPPERS ====================
    private List<RevenueByDayResponse> mapToRevenueByDay(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new RevenueByDayResponse(
                        (Date) row[0],
                        safeBigDecimal(row[1])
                ))
                .collect(Collectors.toList());
    }

    private List<OrdersByDayResponse> mapToOrdersByDay(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new OrdersByDayResponse(
                        ((Date) row[0]).toLocalDate(),
                        safeLong(row[1])
                ))
                .collect(Collectors.toList());
    }


    private List<UserStatsByDayResponse> mapToUserStatsByDay(List<Object[]> raw) {
        return raw.stream()
                .map(row -> new UserStatsByDayResponse(
                        (Date) row[0],
                        safeLong(row[1])
                ))
                .collect(Collectors.toList());
    }

    // ==================== HELPERS ====================
    private BigDecimal safeBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        try {
            return new BigDecimal(val.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }


    private Long safeLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long) return (Long) val;
        try {
            return Long.valueOf(val.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // ==================== OTHER CHART API ====================
    public List<RevenueByDayResponse> getRevenueChart(DashboardRequest request) {
        return mapToRevenueByDay(orderRepository.getRevenueByDayRawWithDateRange(request.getFromDate(), request.getToDate()));
    }

    public List<OrdersByDayResponse> getOrdersChart(DashboardRequest request) {
        return mapToOrdersByDay(orderRepository.getOrdersByDayRawWithDateRange(request.getFromDate(), request.getToDate()));
    }

    public List<UserStatsByDayResponse> getUsersChart(DashboardRequest request) {
        return mapToUserStatsByDay(userRepository.getUserStatsByDayRawWithDateRange(request.getFromDate(), request.getToDate()));
    }

    public List<TopCategoryResponse> getTopCategories() {
        Pageable top10 = PageRequest.of(0, 10);
        return productRepository.getTopCategories(top10);
    }

    public List<TopProductResponse> getTopProducts() {
        Pageable top10 = PageRequest.of(0, 10);
        List<TopProductResponse> topProducts = productRepository.getTopProducts(top10);
        return topProducts.size() > 10 ? topProducts.subList(0, 10) : topProducts;
    }

    private Integer safeInteger(Object val) {
        if (val == null) return 0;
        if (val instanceof Integer) return (Integer) val;
        try {
            return Integer.valueOf(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
