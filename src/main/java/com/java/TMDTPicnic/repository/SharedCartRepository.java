package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.SharedCart;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SharedCartRepository extends JpaRepository<SharedCart, Long> {
    // Tìm các giỏ hàng mà user là owner
    List<SharedCart> findByOwner(User owner);

    // Tìm các giỏ hàng mà user tham gia (qua SharedCartParticipant)
    @Query("SELECT DISTINCT sc FROM SharedCart sc " +
            "JOIN SharedCartParticipant scp ON scp.sharedCart = sc " +
            "WHERE scp.user = :user")
    List<SharedCart> findByParticipant(@Param("user") User user);

    // Tìm tất cả giỏ hàng mà user tham gia (owner hoặc participant)
    @Query("SELECT DISTINCT sc FROM SharedCart sc " +
            "LEFT JOIN SharedCartParticipant scp ON scp.sharedCart = sc " +
            "WHERE sc.owner = :user OR scp.user = :user")
    List<SharedCart> findByOwnerOrParticipant(@Param("user") User user);
}
