package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.dto.response.TopCategoryResponse;
import com.java.TMDTPicnic.dto.response.TopProductResponse;
import com.java.TMDTPicnic.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySlug(String slug);
    Optional<Product> findBySlug(String slug);
    List<Product> findByIsFeaturedTrue();
    List<Product> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);
    List<Product> findByDiscountRateGreaterThan(BigDecimal rate);
    List<Product> findByCategoryIdOrderBySoldQuantityDesc(Long categoryId);
    List<Product> findByCategoryIdAndDiscountRateGreaterThanOrderByDiscountRateDesc(Long categoryId, BigDecimal discountRate);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Tổng số sản phẩm
    @Query("SELECT COUNT(p) FROM Product p")
    long countTotalProducts();

    @Query("SELECT SUM(p.soldQuantity) FROM Product p")
    Long countTotalSold();

    @Query("""
           SELECT COUNT(p) 
           FROM Product p 
           WHERE MONTH(p.createdAt) = MONTH(CURRENT_DATE)
             AND YEAR(p.createdAt) = YEAR(CURRENT_DATE)
           """)
    long countProductsCreatedThisMonth();

    // Số sản phẩm tạo từ ngày truyền vào
    @Query("""
           SELECT COUNT(p)
           FROM Product p
           WHERE p.createdAt >= :fromDate
           """)
    long countProductsCreatedSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("""
        SELECT new com.java.TMDTPicnic.dto.response.TopCategoryResponse(
            p.category.name,
            SUM(p.soldQuantity)
        )
        FROM Product p
        GROUP BY p.category.name
        ORDER BY SUM(p.soldQuantity) DESC
    """)
    List<TopCategoryResponse> getTopCategories();

    @Query("""
        SELECT new com.java.TMDTPicnic.dto.response.TopProductResponse(
            p.id,
            p.name,
            p.soldQuantity,
            p.soldQuantity * p.price
        )
        FROM Product p
        ORDER BY p.soldQuantity DESC
    """)
    List<TopProductResponse> getTopProducts();
}
