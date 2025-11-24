package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);

    @Query("SELECT c FROM Coupon c " +
            "WHERE (:keyword IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (" +
            "   :status IS NULL " +
            "   OR (:status = 'ACTIVE' AND :now BETWEEN c.validFrom AND c.validTo AND c.usedCount < c.usageLimit) " +
            "   OR (:status = 'UPCOMING' AND :now < c.validFrom) " +
            "   OR (:status = 'EXPIRED' AND (:now > c.validTo OR c.usedCount >= c.usageLimit))" +
            ")")
    Page<Coupon> searchCoupons(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
