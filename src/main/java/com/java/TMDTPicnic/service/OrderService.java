package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.ApplyCouponRequest;
import com.java.TMDTPicnic.dto.request.CheckoutRequest;
import com.java.TMDTPicnic.dto.request.OrderStatusUpdateRequest;
import com.java.TMDTPicnic.dto.response.OrderHistoryResponse;
import com.java.TMDTPicnic.dto.response.OrderStatusUpdateResponse;
import com.java.TMDTPicnic.dto.response.OrderSummaryResponse;
import com.java.TMDTPicnic.entity.*;
import com.java.TMDTPicnic.enums.OrderStatus;
import com.java.TMDTPicnic.enums.PaymentMethod;
import com.java.TMDTPicnic.enums.PaymentStatus;
import com.java.TMDTPicnic.enums.SharedCartStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.java.TMDTPicnic.repository.*;
import com.java.TMDTPicnic.service.NotificationService;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final VNPayService vnPayService;
    private final SharedCartRepository sharedCartRepository;
    private final SharedCartItemRepository sharedCartItemRepository;
    private final SharedCartParticipantRepository sharedCartParticipantRepository;
    private final NotificationService notificationService;
    private final CouponService couponService;
    private String couponCode;


    @Transactional
    public String createOrder(Long userId, CheckoutRequest request, String ipAddress) throws UnsupportedEncodingException {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal total = BigDecimal.ZERO;

        if ("GROUP".equalsIgnoreCase(request.getOrderType())) {
            List<Long> cartItemIds = new ArrayList<>();
            if (request.getCartItems() != null) {
                for (var cartItem : request.getCartItems()) {
                    cartItemIds.add(cartItem.getId());
                }
            }

            if (cartItemIds.isEmpty()) {
                throw new RuntimeException("No items selected in cart");
            }

            var cartItems = cartItemRepository.findAllById(cartItemIds);

            for (var item : cartItems) {
                var product = item.getProduct();
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Product " + product.getName() + " out of stock");
                }
                total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
                BigDecimal finalTotal = total;
                if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
                    var applyRequest = new ApplyCouponRequest(request.getCouponCode(), total);
                    var applyResult = couponService.applyCoupon(applyRequest);
                    if (!applyResult.isValid()) {
                        throw new RuntimeException(applyResult.getMessage());
                    }
                    finalTotal = applyResult.getFinalTotal(); // finalTotal = total * giảm giá (nếu có)
                }


            Order order = Order.builder()
                    .user(user)
                    .totalAmount(finalTotal)
                    .status(OrderStatus.PENDING)
                    .orderType("GROUP")
                    .createdAt(LocalDateTime.now())
                    .build();
            orderRepository.save(order);

            for (var item : cartItems) {
                var product = item.getProduct();

                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .qty(item.getQuantity())
                        .unitPrice(product.getPrice())
                        .build();
                orderItemRepository.save(orderItem);
            }

            Payment payment = Payment.builder()
                    .order(order)
                    .amount(finalTotal)
                    .paymentMethod(request.getPaymentMethod().name())
                    .status(PaymentStatus.PENDING)
                    .paidAt(null)
                    .build();
            paymentRepository.save(payment);

            cartItemRepository.deleteAll(cartItems);

            if (request.getPaymentMethod() == PaymentMethod.COD) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
                return ""; // Không cần url thanh toán
            } else if (request.getPaymentMethod() == PaymentMethod.MOMO) {
                // Xử lý MOMO ở đây (hiện để trống)
                return "";
            } else if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
                return vnPayService.createPaymentUrl(order.getId(), finalTotal, ipAddress);
            } else {
                throw new RuntimeException("Unsupported payment method");
            }

        } else if ("SINGLE".equalsIgnoreCase(request.getOrderType())) {
            if (request.getDirectItems() == null || request.getDirectItems().isEmpty()) {
                throw new RuntimeException("No items to checkout");
            }

            for (var item : request.getDirectItems()) {
                var product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + item.getProductId()));
                if (product.getStockQuantity() < item.getQty()) {
                    throw new RuntimeException("Product " + product.getName() + " out of stock");
                }
                total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
            }
            BigDecimal finalTotal = total;
            if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
                var applyRequest = new ApplyCouponRequest(request.getCouponCode(), total);
                var applyResult = couponService.applyCoupon(applyRequest);
                if (!applyResult.isValid()) {
                    throw new RuntimeException(applyResult.getMessage());
                }
                finalTotal = applyResult.getFinalTotal(); // finalTotal = total * giảm giá (nếu có)
            }


            Order order = Order.builder()
                    .user(user)
                    .totalAmount(finalTotal)
                    .status(OrderStatus.PENDING)
                    .orderType("SINGLE")
                    .createdAt(LocalDateTime.now())
                    .build();
            orderRepository.save(order);

            for (var item : request.getDirectItems()) {
                var product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + item.getProductId()));

                product.setStockQuantity(product.getStockQuantity() - item.getQty());
                productRepository.save(product);

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .qty(item.getQty())
                        .unitPrice(product.getPrice())
                        .build();
                orderItemRepository.save(orderItem);
            }

            Payment payment = Payment.builder()
                    .order(order)
                    .amount(finalTotal)
                    .paymentMethod(request.getPaymentMethod().name())
                    .status(PaymentStatus.PENDING)
                    .paidAt(null)
                    .build();
            paymentRepository.save(payment);

            if (request.getPaymentMethod() == PaymentMethod.COD) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
                return "";
            } else if (request.getPaymentMethod() == PaymentMethod.MOMO) {
                // Xử lý MOMO ở đây (hiện để trống)
                return "";
            } else if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
                return vnPayService.createPaymentUrl(order.getId(), finalTotal, ipAddress);
            } else {
                throw new RuntimeException("Unsupported payment method");
            }
        } else {
            throw new RuntimeException("Có 2 tham số orderType là SINGLE VÀ GROUP. Nếu mua hàng trực tiếp thì truyền SINGLE, nếu mua nhiều sản phẩm từ giỏ hàng (CartItem) thì truyền GROUP");
        }
    }

    @Transactional
    public void updatePaymentStatusAfterSuccess(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order id: " + orderId));

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Nếu là shared cart order, đóng shared cart và xóa items
        if ("SHARED_CART".equalsIgnoreCase(order.getOrderType()) && order.getSharedCart() != null) {
            SharedCart sharedCart = order.getSharedCart();
            sharedCart.setStatus(SharedCartStatus.COMPLETED);
            sharedCartRepository.save(sharedCart);

            // Xóa tất cả items trong shared cart
            List<SharedCartItem> cartItems = sharedCartItemRepository.findBySharedCartId(sharedCart.getId());
            sharedCartItemRepository.deleteAll(cartItems);

            // Tạo notification cho tất cả participants (trừ người thanh toán)
            User paidByUser = order.getUser();
            List<SharedCartParticipant> participants = sharedCartParticipantRepository.findBySharedCartId(sharedCart.getId());
            for (SharedCartParticipant participant : participants) {
                if (!participant.getUser().getId().equals(paidByUser.getId())) {
                    notificationService.createSharedCartCheckoutNotification(
                            participant.getUser(),
                            sharedCart,
                            paidByUser,
                            payment.getAmount(),
                            payment.getPaymentMethod()
                    );
                }
            }
            // Tạo notification cho người thanh toán
            notificationService.createSharedCartCheckoutNotification(
                    paidByUser,
                    sharedCart,
                    paidByUser,
                    payment.getAmount(),
                    payment.getPaymentMethod()
            );

            logger.info("Closed shared cart #{} and removed all items after successful payment", sharedCart.getId());
        }

        logger.info("Updated order #{} status to COMPLETED and payment SUCCESS", orderId);
    }

    public OrderHistoryResponse getPersonalOrderHistory(Long userId) {
        logger.info("=== [OrderHistory] Bắt đầu lấy lịch sử mua hàng cá nhân cho userId = {} ===", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        logger.info("User tìm thấy: {}", user.getEmail());

        List<Order> orders = orderRepository.findByUserAndOrderTypeIsNotOrderByCreatedAtDesc(user, "SHARED_CART");
        logger.info("Tổng số order cá nhân tìm thấy: {}", orders.size());

        List<OrderSummaryResponse> orderSummaries = orders.stream()
                .map(order -> {
                    logger.info("Xử lý Order ID = {}, orderType = {}", order.getId(), order.getOrderType());

                    List<OrderItem> orderItems = orderItemRepository.findByOrderWithProductAndImages(order);
                    logger.info(" - Số lượng OrderItem: {}", orderItems.size());

                    String firstProductThumbnail = null;
                    if (!orderItems.isEmpty() && orderItems.get(0).getProduct() != null) {
                        Product product = orderItems.get(0).getProduct();
                        firstProductThumbnail = product.getThumbnail();
                        logger.info(" - Thumbnail của OrderItem đầu tiên: {}", firstProductThumbnail);
                    } else {
                        logger.info(" - KHÔNG tìm thấy product hoặc thumbnail");
                    }

                    return OrderSummaryResponse.builder()
                            .id(order.getId())
                            .totalAmount(order.getTotalAmount())
                            .status(order.getStatus())
                            .orderType(order.getOrderType() != null ? order.getOrderType() : "SINGLE")
                            .firstProductThumbnail(firstProductThumbnail)
                            .createdAt(order.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        logger.info("=== [OrderHistory] Hoàn tất xử lý lịch sử cá nhân cho userId = {} ===", userId);
        return OrderHistoryResponse.builder()
                .orders(orderSummaries)
                .build();
    }


    public OrderHistoryResponse getSharedCartOrderHistory(Long userId) {
        logger.info("=== [OrderHistory] Bắt đầu lấy lịch sử SharedCart cho userId = {} ===", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        logger.info("User tìm thấy: {}", user.getEmail());

        List<Order> orders = orderRepository.findByUserAndOrderTypeIsOrderByCreatedAtDesc(user, "SHARED_CART");
        logger.info("Tổng số order SharedCart tìm thấy: {}", orders.size());

        List<OrderSummaryResponse> orderSummaries = orders.stream()
                .map(order -> {
                    logger.info("Xử lý SharedCart Order ID = {}, orderType = {}", order.getId(), order.getOrderType());

                    List<OrderItem> orderItems = orderItemRepository.findByOrderWithProductAndImages(order);
                    logger.info(" - Số lượng OrderItem: {}", orderItems.size());

                    String firstProductThumbnail = null;
                    if (!orderItems.isEmpty() && orderItems.get(0).getProduct() != null) {
                        Product product = orderItems.get(0).getProduct();
                        firstProductThumbnail = product.getThumbnail();
                        logger.info(" - Thumbnail orderItem đầu tiên: {}", firstProductThumbnail);
                    } else {
                        logger.info(" - KHÔNG có product hoặc thumbnail");
                    }

                    return OrderSummaryResponse.builder()
                            .id(order.getId())
                            .totalAmount(order.getTotalAmount())
                            .status(order.getStatus())
                            .orderType(order.getOrderType() != null ? order.getOrderType() : "SHARED_CART")
                            .firstProductThumbnail(firstProductThumbnail)
                            .createdAt(order.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        logger.info("=== [OrderHistory] Hoàn tất xử lý SharedCart cho userId = {} ===", userId);
        return OrderHistoryResponse.builder()
                .orders(orderSummaries)
                .build();
    }

    /**
     * Lấy tất cả đơn hàng cho admin (với pagination và filter)
     */
    public Page<OrderSummaryResponse> getAllOrders(Pageable pageable, OrderStatus status, String orderType) {
        Specification<Order> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (orderType != null && !orderType.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("orderType"), orderType));
        }

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        return orders.map(order -> {
            // Lấy OrderItem đầu tiên để lấy thumbnail
            List<OrderItem> orderItems = orderItemRepository.findByOrderWithProductAndImages(order);
            String firstProductThumbnail = null;
            if (!orderItems.isEmpty() && orderItems.get(0).getProduct() != null) {
                Product product = orderItems.get(0).getProduct();
                firstProductThumbnail = product.getThumbnail();
            }

            return OrderSummaryResponse.builder()
                    .id(order.getId())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus())
                    .orderType(order.getOrderType() != null ? order.getOrderType() : "SINGLE")
                    .firstProductThumbnail(firstProductThumbnail)
                    .createdAt(order.getCreatedAt())
                    .build();
        });
    }

    /**
     * Admin cập nhật trạng thái đơn hàng từ PAID sang SHIPPED
     */
    @Transactional
    public OrderStatusUpdateResponse updateOrderStatusByAdmin(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        // Validate: Admin chỉ có thể chuyển từ PAID sang SHIPPED
        if (oldStatus != OrderStatus.PAID) {
            throw new RuntimeException("Chỉ có thể cập nhật đơn hàng có trạng thái PAID. Trạng thái hiện tại: " + oldStatus);
        }

        if (newStatus != OrderStatus.SHIPPED) {
            throw new RuntimeException("Admin chỉ có thể chuyển trạng thái từ PAID sang SHIPPED");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        logger.info("Admin updated order #{} status from {} to {}", orderId, oldStatus, newStatus);

        return OrderStatusUpdateResponse.builder()
                .orderId(orderId)
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .message("Cập nhật trạng thái đơn hàng thành công")
                .build();
    }

    /**
     * User cập nhật trạng thái đơn hàng từ SHIPPED sang COMPLETED
     */
    @Transactional
    public OrderStatusUpdateResponse updateOrderStatusByUser(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Kiểm tra quyền: User chỉ có thể cập nhật đơn hàng của chính mình
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật đơn hàng này");
        }

        OrderStatus oldStatus = order.getStatus();

        // Validate: User chỉ có thể chuyển từ SHIPPED sang COMPLETED
        if (oldStatus != OrderStatus.SHIPPED) {
            throw new RuntimeException("Chỉ có thể xác nhận đơn hàng đã được giao (SHIPPED). Trạng thái hiện tại: " + oldStatus);
        }

        OrderStatus newStatus = OrderStatus.COMPLETED;
        order.setStatus(newStatus);
        orderRepository.save(order);

        logger.info("User {} updated order #{} status from {} to {}", userId, orderId, oldStatus, newStatus);

        return OrderStatusUpdateResponse.builder()
                .orderId(orderId)
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .message("Xác nhận nhận hàng thành công")
                .build();
    }

}