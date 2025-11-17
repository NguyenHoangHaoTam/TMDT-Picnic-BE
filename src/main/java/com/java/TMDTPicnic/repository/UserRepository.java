package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.dto.response.UserStatsByDayResponse;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long userId);
    Optional<User> findByUsername(String userName);
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Tổng số người dùng
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();

    // Người dùng mới trong tháng
    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE MONTH(u.createdAt) = MONTH(CURRENT_DATE)
              AND YEAR(u.createdAt) = YEAR(CURRENT_DATE)
            """)
    long countUsersCreatedThisMonth();

    // Người dùng mới trong 7 ngày gần nhất — dùng tham số ngày truyền vào
    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE u.createdAt >= :fromDate
            """)
    long countUsersCreatedSince(@Param("fromDate") LocalDateTime fromDate);

    // Thống kê số user mới mỗi ngày (có thể tính returningUser nếu có dữ liệu)
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COUNT(*) as newUsers,
            0 as returningUsers
        FROM users
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getUserStatsByDayRaw();

}
