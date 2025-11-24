package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.Order;
import com.java.TMDTPicnic.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    
    @Query("SELECT oi FROM OrderItem oi " +
           "JOIN FETCH oi.product p " +
           "LEFT JOIN FETCH p.images " +
           "WHERE oi.order = :order")
    List<OrderItem> findByOrderWithProductAndImages(@Param("order") Order order);

}
