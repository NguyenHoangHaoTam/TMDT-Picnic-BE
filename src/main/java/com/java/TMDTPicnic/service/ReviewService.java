package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.ReviewAdminActionRequest;
import com.java.TMDTPicnic.dto.request.ReviewCreateRequest;
import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.entity.*;
import com.java.TMDTPicnic.enums.OrderStatus;
import com.java.TMDTPicnic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * User tạo review cho sản phẩm
     * Kiểm tra user đã mua sản phẩm chưa
     */
    @Transactional
    public ReviewCreateResponse createReview(Long userId, ReviewCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra user đã review sản phẩm này chưa
        if (reviewRepository.existsByProductAndUser(product, user)) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }

        // Kiểm tra user đã mua sản phẩm này chưa
        boolean hasPurchased = checkUserHasPurchasedProduct(userId, request.getProductId());
        if (!hasPurchased) {
            throw new RuntimeException("Bạn cần mua sản phẩm này trước khi đánh giá");
        }

        // Validate rating
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating phải từ 1 đến 5");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .isHidden(false)
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        return ReviewCreateResponse.builder()
                .reviewId(savedReview.getId())
                .message("Đánh giá sản phẩm thành công")
                .createdAt(savedReview.getCreatedAt())
                .build();
    }

    /**
     * Lấy danh sách review của sản phẩm
     */
    public ProductReviewListResponse getProductReviews(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Lấy tất cả review không bị ẩn
        List<Review> reviews = reviewRepository.findByProductAndIsHiddenFalseOrderByCreatedAtDesc(product);

        // Tính average rating
        Double averageRating = reviewRepository.calculateAverageRating(product);
        if (averageRating == null) {
            averageRating = 0.0;
        }

        // Đếm số review
        Long totalReviews = reviewRepository.countByProductAndIsHiddenFalse(product);

        // Map sang DTO
        List<ReviewDTO> reviewDTOs = reviews.stream()
                .map(review -> ReviewDTO.builder()
                        .id(review.getId())
                        .userId(review.getUser().getId())
                        .userName(review.getUser().getFullName() != null ? 
                                review.getUser().getFullName() : review.getUser().getUsername())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ProductReviewListResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .averageRating(averageRating)
                .totalReviews(totalReviews.intValue())
                .reviews(reviewDTOs)
                .build();
    }

    /**
     * Admin ẩn hoặc xóa review
     */
    @Transactional
    public ReviewAdminActionResponse adminAction(ReviewAdminActionRequest request) {
        Review review = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new RuntimeException("Review not found"));

        String action = request.getAction().toUpperCase();
        LocalDateTime processedAt = LocalDateTime.now();

        if ("HIDE".equals(action)) {
            review.setIsHidden(true);
            reviewRepository.save(review);

            return ReviewAdminActionResponse.builder()
                    .reviewId(review.getId())
                    .action("HIDE")
                    .message("Đã ẩn đánh giá thành công")
                    .processedAt(processedAt)
                    .build();
        } else if ("DELETE".equals(action)) {
            reviewRepository.delete(review);

            return ReviewAdminActionResponse.builder()
                    .reviewId(review.getId())
                    .action("DELETE")
                    .message("Đã xóa đánh giá thành công")
                    .processedAt(processedAt)
                    .build();
        } else {
            throw new RuntimeException("Action không hợp lệ. Chỉ chấp nhận HIDE hoặc DELETE");
        }
    }

    /**
     * Kiểm tra user đã mua sản phẩm chưa
     * Kiểm tra trong OrderItem của các đơn hàng đã hoàn thành
     */
    private boolean checkUserHasPurchasedProduct(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lấy tất cả đơn hàng của user có status COMPLETED hoặc SHIPPED
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        
        for (Order order : orders) {
            // Chỉ kiểm tra đơn hàng đã thanh toán, đã giao hoặc đã hoàn thành
            OrderStatus status = order.getStatus();
            if (status == OrderStatus.PAID || 
                status == OrderStatus.SHIPPED ||
                status == OrderStatus.COMPLETED) {
                
                List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                for (OrderItem item : orderItems) {
                    if (item.getProduct().getId().equals(productId)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
}

