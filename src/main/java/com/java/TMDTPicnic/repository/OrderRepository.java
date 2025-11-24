package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.entity.Order;
import com.java.TMDTPicnic.entity.SharedCart;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findBySharedCartAndOrderType(SharedCart sharedCart, String orderType);
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    // ===== ORDER HISTORY =====
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUserAndOrderTypeIsOrderByCreatedAtDesc(User user, String orderType);
    List<Order> findByUserAndOrderTypeIsNotOrderByCreatedAtDesc(User user, String orderType);

    // ===== SUMMARY =====
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal totalRevenue();

    @Query("SELECT COUNT(o) FROM Order o")
    Long totalOrders();

    @Query(value = """
        SELECT 
            COUNT(*) as totalOrders,
            SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completedCount,
            SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pendingCount,
            SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) as paidCount,
            SUM(CASE WHEN status = 'SHIPPED' THEN 1 ELSE 0 END) as shippedCount
        FROM orders 
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
    """, nativeQuery = true)
    OrderStatusResponse getOrderStatusWithDayRange(@Param("fromDate") LocalDate fromDate,
                                                   @Param("toDate") LocalDate toDate);


    // ===== REVENUE BY DAY WITH DATE RANGE =====
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COALESCE(SUM(total_amount), 0) as revenue
        FROM orders
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getRevenueByDayRawWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    // ===== ORDERS BY DAY WITH DATE RANGE =====
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COUNT(*) as orders
        FROM orders
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getOrdersByDayRawWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

}
