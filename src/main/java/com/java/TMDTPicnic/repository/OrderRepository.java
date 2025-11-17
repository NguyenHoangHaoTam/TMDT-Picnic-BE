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

import java.util.Date;
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
    //COALESCE(a, b, c) if(a=null) return b, b=null return c
    @Query("SELECT COALESCE(SUM(order.totalAmount), 0) FROM Order order")
    Double totalRevenue();

    @Query("SELECT COUNT(order) FROM Order order")
    Long totalOrders();

    @Query("SELECT COUNT(order) FROM Order order WHERE order.status = 'COMPLETED'")
    Long completedOrders();

    // ===== REVENUE BY DAY =====
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COALESCE(SUM(total_amount), 0) as revenue
        FROM orders
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getRevenueByDayRaw();

    // ===== REVENUE BY MONTH =====
    @Query(value = """
        SELECT 
            STR_TO_DATE(CONCAT(YEAR(created_at), '-', LPAD(MONTH(created_at), 2, '0'), '-01'), '%Y-%m-%d') as month,
            COALESCE(SUM(total_amount), 0) as revenue
        FROM orders
        GROUP BY month
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> getRevenueByMonthRaw();


    // ===== ORDERS BY DAY =====
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COUNT(*) as orders
        FROM orders
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getOrdersByDayRaw();

    // ===== ORDERS BY MONTH =====
    @Query(value = """
        SELECT 
            DATE_FORMAT(created_at, '%Y-%m') as month,
            COUNT(*) as orders
        FROM orders
        GROUP BY DATE_FORMAT(created_at, '%Y-%m')
        ORDER BY DATE_FORMAT(created_at, '%Y-%m')
    """, nativeQuery = true)
    List<Object[]> getOrdersByMonthRaw();

    // ===== ORDER STATUS =====
    @Query("""
        SELECT new com.java.TMDTPicnic.dto.response.OrderStatusResponse(
            SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END)
        )
        FROM Order o
    """)
    OrderStatusResponse getOrderStatus();
}
