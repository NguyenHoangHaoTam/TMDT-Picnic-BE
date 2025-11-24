package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.dto.response.UserRoleDistributionResponse;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long userId);
    Optional<User> findByUsername(String userName);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    // Thống kê số user mới mỗi ngày với date range
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COUNT(*) as newUsers,
            0 as returningUsers
        FROM users
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getUserStatsByDayRawWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

}
