package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.Product;
import com.java.TMDTPicnic.entity.Review;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Tìm review theo product và user
    Optional<Review> findByProductAndUser(Product product, User user);
    
    // Lấy tất cả review của một product (không bị ẩn)
    List<Review> findByProductAndIsHiddenFalseOrderByCreatedAtDesc(Product product);
    
    // Lấy tất cả review của một product (bao gồm cả bị ẩn - cho admin)
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    
    // Kiểm tra user đã review sản phẩm chưa
    boolean existsByProductAndUser(Product product, User user);
    
    // Tính average rating của product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product AND r.isHidden = false")
    Double calculateAverageRating(@Param("product") Product product);
    
    // Đếm số review của product (không bị ẩn)
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product AND r.isHidden = false")
    Long countByProductAndIsHiddenFalse(@Param("product") Product product);
}

