package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.request.*;
import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.entity.*;
import com.java.TMDTPicnic.enums.OrderStatus;
import com.java.TMDTPicnic.enums.PaymentMethod;
import com.java.TMDTPicnic.enums.PaymentStatus;
import com.java.TMDTPicnic.enums.SharedCartStatus;
import com.java.TMDTPicnic.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SharedCartService {

    private final SharedCartRepository sharedCartRepository;
    private final SharedCartItemRepository sharedCartItemRepository;
    private final SharedCartParticipantRepository sharedCartParticipantRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final VNPayService vnPayService;
    private final CouponService couponService;
    private String couponCode;

    // 1. Tạo giỏ chia sẻ
    public SharedCartListResponse.CreateResponse createSharedCart(SharedCartCreateRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        SharedCart cart = SharedCart.builder()
                .title(request.getTitle())
                .owner(owner)
                .status(SharedCartStatus.OPEN)
                .expiresAt(request.getExpiresAt())
                .createdAt(LocalDateTime.now())
                .build();

        sharedCartRepository.save(cart);

        return toCartResponse(cart);
    }

    // 2. Thêm sản phẩm vào giỏ
    public SharedCartAddItemResponse addItemToCart(SharedCartAddItemRequest request) {
        SharedCart cart = sharedCartRepository.findById(request.getSharedCartId())
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        // Kiểm tra giỏ hàng đã đóng chưa
        if (cart.getStatus() != SharedCartStatus.OPEN) {
            throw new RuntimeException("Cannot add items to closed or cancelled cart");
        }

        User user = userRepository.findById(request.getAddByUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Tự động lấy giá từ Product nếu priceAtAdd không được cung cấp
        java.math.BigDecimal priceAtAdd = request.getPriceAtAdd();
        if (priceAtAdd == null) {
            priceAtAdd = product.getPrice();
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa (cùng user thêm)
        Optional<SharedCartItem> existingItem = sharedCartItemRepository
                .findBySharedCartIdAndProductId(request.getSharedCartId(), request.getProductId())
                .filter(item -> item.getUser().getId().equals(request.getAddByUserId()));

        SharedCartItem item;
        if (existingItem.isPresent()) {
            // Nếu đã có, cập nhật số lượng
            item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            // Nếu chưa có, tạo mới
            item = SharedCartItem.builder()
                    .sharedCart(cart)
                    .product(product)
                    .user(user)
                    .quantity(request.getQuantity())
                    .priceAtAdd(priceAtAdd)
                    .build();
        }

        sharedCartItemRepository.save(item);
        return toItemResponse(item);
    }

    // 3. Mời người tham gia giỏ bằng email hoặc username
    public List<SharedCartInviteResponse> addParticipant(SharedCartInviteRequest request, Long inviterId) {
        SharedCart cart = sharedCartRepository.findById(request.getSharedCartId())
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        List<SharedCartInviteResponse> responses = new ArrayList<>();

        // Lặp qua từng username/email để tìm user tương ứng
        for (String identifier : request.getIdentifiers()) {
            Optional<User> userOpt = userRepository.findByEmail(identifier);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsername(identifier);
            }

            User invitedUser = userOpt.orElseThrow(() ->
                    new RuntimeException("User not found with username/email: " + identifier));

            // Kiểm tra trùng lặp - nếu đã là participant thì bỏ qua
            boolean exists = sharedCartParticipantRepository.existsBySharedCartAndUser(cart, invitedUser);
            if (exists) {
                // Nếu đã là participant, vẫn trả về response
                Optional<SharedCartParticipant> existingOpt = sharedCartParticipantRepository
                        .findBySharedCartAndUser(cart, invitedUser);
                if (existingOpt.isPresent()) {
                    responses.add(toParticipantResponse(existingOpt.get()));
                }
                continue;
            }

            // TẠO NOTIFICATION thay vì thêm participant trực tiếp
            notificationService.createSharedCartInvitationNotification(
                    invitedUser,
                    inviter,
                    cart,
                    request.getContributionAmount()
            );

            // Tạo response giả (vì chưa có participant thật, chỉ có notification)
            SharedCartInviteResponse response = SharedCartInviteResponse.builder()
                    .sharedCartId(cart.getId())
                    .userId(invitedUser.getId())
                    .contributionAmount(request.getContributionAmount())
                    .joinedAt(null) // Chưa join, chỉ mới được mời
                    .build();

            responses.add(response);
        }

        if (responses.isEmpty()) {
            throw new RuntimeException("No valid users to invite");
        }

        return responses;
    }

    // 4. Lấy danh sách giỏ hàng của user
    public List<SharedCartListResponse> getMySharedCarts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SharedCart> carts = sharedCartRepository.findByOwnerOrParticipant(user);
        return carts.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    // 5. Lấy chi tiết giỏ (cải thiện với đầy đủ thông tin)
    public SharedCartDetailResponse getCartDetails(Long cartId) {
        SharedCart cart = sharedCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        List<SharedCartItem> items = sharedCartItemRepository.findBySharedCartId(cartId);
        List<SharedCartParticipant> participants = sharedCartParticipantRepository.findBySharedCartId(cartId);

        // Tính tổng
        double totalAmount = items.stream()
                .mapToDouble(item -> item.getPriceAtAdd().doubleValue() * item.getQuantity())
                .sum();
        int totalItems = items.stream()
                .mapToInt(SharedCartItem::getQuantity)
                .sum();

        // Lấy thông tin thanh toán nếu cart đã COMPLETED
        SharedCartDetailResponse.PaymentInfo paymentInfo = null;
        if (cart.getStatus() == SharedCartStatus.COMPLETED) {
            Optional<Order> orderOpt = orderRepository.findBySharedCartAndOrderType(cart, "SHARED_CART");
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                Optional<Payment> paymentOpt = paymentRepository.findByOrderId(order.getId());
                if (paymentOpt.isPresent() && paymentOpt.get().getStatus() == PaymentStatus.SUCCESS) {
                    Payment payment = paymentOpt.get();
                    User paidByUser = order.getUser();
                    paymentInfo = SharedCartDetailResponse.PaymentInfo.builder()
                            .paidBy(paidByUser.getId())
                            .paidByName(paidByUser.getFullName() != null ? paidByUser.getFullName() : paidByUser.getUsername())
                            .paidAmount(payment.getAmount())
                            .paidAt(payment.getPaidAt())
                            .paymentMethod(payment.getPaymentMethod())
                            .build();
                }
            }
        }

        return SharedCartDetailResponse.builder()
                .id(cart.getId())
                .title(cart.getTitle())
                .ownerId(cart.getOwner().getId())
                .ownerName(cart.getOwner().getFullName() != null ? cart.getOwner().getFullName() : cart.getOwner().getUsername())
                .expiresAt(cart.getExpiresAt())
                .status(cart.getStatus())
                .createdAt(cart.getCreatedAt())
                .items(items.stream().map(this::toItemDetailResponse).collect(Collectors.toList()))
                .participants(participants.stream().map(this::toParticipantDetailResponse).collect(Collectors.toList()))
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .paymentInfo(paymentInfo)
                .build();
    }

    // 6. Cập nhật số lượng sản phẩm
    @Transactional
    public SharedCartAddItemResponse updateItemQuantity(SharedCartAddItemRequest.UpdateQuantityRequest request) {
        SharedCartItem item = sharedCartItemRepository
                .findBySharedCartIdAndProductId(request.getSharedCartId(), request.getProductId())
                .orElseThrow(() -> new RuntimeException("Item not found in shared cart"));

        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        item.setQuantity(request.getQuantity());
        sharedCartItemRepository.save(item);

        return toItemResponse(item);
    }

    // 7. Xóa sản phẩm khỏi giỏ
    @Transactional
    public void removeItem(SharedCartAddItemRequest.RemoveItemRequest request) {
        SharedCart cart = sharedCartRepository.findById(request.getSharedCartId())
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        sharedCartItemRepository.deleteBySharedCartIdAndProductId(
                request.getSharedCartId(),
                request.getProductId()
        );
    }

    // 8. Đóng giỏ hàng (chỉ owner mới được đóng)
    @Transactional
    public SharedCartListResponse.CreateResponse closeCart(Long cartId, Long userId) {
        SharedCart cart = sharedCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        if (!cart.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only owner can close the cart");
        }

        if (cart.getStatus() != SharedCartStatus.OPEN) {
            throw new RuntimeException("Cart is already closed or cancelled");
        }

        cart.setStatus(SharedCartStatus.COMPLETED);
        sharedCartRepository.save(cart);

        return toCartResponse(cart);
    }

    // 9. Hủy giỏ hàng (chỉ owner mới được hủy)
    @Transactional
    public SharedCartListResponse.CreateResponse cancelCart(Long cartId, Long userId) {
        SharedCart cart = sharedCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        if (!cart.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only owner can cancel the cart");
        }

        cart.setStatus(SharedCartStatus.CANCELLED);
        sharedCartRepository.save(cart);

        return toCartResponse(cart);
    }

    // 10. Rời khỏi giỏ hàng (participant tự rời)
    @Transactional
    public void leaveCart(Long cartId, Long userId) {
        SharedCart cart = sharedCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        // Owner không thể rời, chỉ có thể đóng hoặc hủy
        if (cart.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Owner cannot leave. Please close or cancel the cart instead.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SharedCartParticipant participant = sharedCartParticipantRepository
                .findBySharedCartId(cartId)
                .stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("You are not a participant of this cart"));

        // Xóa tất cả sản phẩm mà user này đã thêm
        List<SharedCartItem> userItems = sharedCartItemRepository.findBySharedCartId(cartId)
                .stream()
                .filter(item -> item.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        sharedCartItemRepository.deleteAll(userItems);

        // Xóa participant
        sharedCartParticipantRepository.delete(participant);
    }

    // 11. Xóa participant (chỉ owner mới được xóa)
    @Transactional
    public void removeParticipant(Long cartId, Long participantUserId, Long ownerId) {
        SharedCart cart = sharedCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        if (!cart.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Only owner can remove participants");
        }

        User participantUser = userRepository.findById(participantUserId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        SharedCartParticipant participant = sharedCartParticipantRepository
                .findBySharedCartId(cartId)
                .stream()
                .filter(p -> p.getUser().getId().equals(participantUserId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Participant not found in this cart"));

        // Xóa tất cả sản phẩm mà participant này đã thêm
        List<SharedCartItem> participantItems = sharedCartItemRepository.findBySharedCartId(cartId)
                .stream()
                .filter(item -> item.getUser().getId().equals(participantUserId))
                .collect(Collectors.toList());
        sharedCartItemRepository.deleteAll(participantItems);

        // Xóa participant
        sharedCartParticipantRepository.delete(participant);
    }

    // 12. Cập nhật contribution amount
    @Transactional
    public SharedCartInviteResponse updateContribution(SharedCartAddItemRequest.UpdateContributionRequest request) {
        SharedCart cart = sharedCartRepository.findById(request.getSharedCartId())
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        SharedCartParticipant participant = sharedCartParticipantRepository
                .findBySharedCartId(request.getSharedCartId())
                .stream()
                .filter(p -> p.getUser().getId().equals(request.getUserId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setContributionAmount(request.getContributionAmount());
        sharedCartParticipantRepository.save(participant);

        return toParticipantResponse(participant);
    }

    // 13. Checkout shared cart (thanh toán giỏ hàng chung)
    @Transactional
    public String checkoutSharedCart(Long userId, SharedCartCheckoutRequest request, String ipAddress)
            throws UnsupportedEncodingException {
        // Kiểm tra shared cart tồn tại
        SharedCart cart = sharedCartRepository.findById(request.getSharedCartId())
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        // Chỉ owner mới được checkout
        if (!cart.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only owner can checkout the shared cart");
        }

        // Kiểm tra cart status
        if (cart.getStatus() != SharedCartStatus.OPEN) {
            throw new RuntimeException("Cart is not open. Cannot checkout.");
        }

        // Lấy tất cả items trong shared cart
        List<SharedCartItem> cartItems = sharedCartItemRepository.findBySharedCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Shared cart is empty. Cannot checkout.");
        }

        // Tính tổng tiền và kiểm tra stock
        BigDecimal total = BigDecimal.ZERO;
        for (SharedCartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Product " + product.getName() + " out of stock. Available: "
                        + product.getStockQuantity() + ", Required: " + item.getQuantity());
            }
            total = total.add(item.getPriceAtAdd().multiply(BigDecimal.valueOf(item.getQuantity())));
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


        // Lấy user (owner)
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tạo Order
        Order order = Order.builder()
                .user(owner)
                .totalAmount(finalTotal)
                .status(OrderStatus.PENDING)
                .orderType("SHARED_CART")
                .sharedCart(cart)
                .createdAt(LocalDateTime.now())
                .build();
        orderRepository.save(order);

        // Tạo OrderItems và giảm stock
        for (SharedCartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Giảm stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Tạo OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .qty(cartItem.getQuantity())
                    .unitPrice(cartItem.getPriceAtAdd())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Tạo Payment
        Payment payment = Payment.builder()
                .order(order)
                .amount(finalTotal)
                .paymentMethod(request.getPaymentMethod().name())
                .status(PaymentStatus.PENDING)
                .paidAt(null)
                .build();
        paymentRepository.save(payment);

        // Xử lý thanh toán
        if (request.getPaymentMethod() == PaymentMethod.COD) {
            // COD: Complete ngay và đóng shared cart
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Đóng shared cart và xóa items
            cart.setStatus(SharedCartStatus.COMPLETED);
            sharedCartRepository.save(cart);
            sharedCartItemRepository.deleteAll(cartItems);

            // Tạo notification cho tất cả participants (trừ người thanh toán)
            List<SharedCartParticipant> participants = sharedCartParticipantRepository.findBySharedCartId(cart.getId());
            for (SharedCartParticipant participant : participants) {
                if (!participant.getUser().getId().equals(owner.getId())) {
                    notificationService.createSharedCartCheckoutNotification(
                            participant.getUser(),
                            cart,
                            owner,
                            finalTotal,
                            request.getPaymentMethod().name()
                    );
                }
            }
            // Tạo notification cho owner (người thanh toán)
            notificationService.createSharedCartCheckoutNotification(
                    owner,
                    cart,
                    owner,
                    finalTotal,
                    request.getPaymentMethod().name()
            );

            return ""; // Không cần URL thanh toán
        } else if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            // VNPay: Tạo payment URL, sẽ xử lý callback sau
            return vnPayService.createPaymentUrl(order.getId(), finalTotal, ipAddress);
        } else if (request.getPaymentMethod() == PaymentMethod.MOMO) {
            // MOMO: (hiện để trống, có thể implement sau)
            throw new RuntimeException("MOMO payment method is not yet implemented");
        } else {
            throw new RuntimeException("Unsupported payment method");
        }
    }

    // ====== CONVERT METHODS ======
    private SharedCartListResponse.CreateResponse toCartResponse(SharedCart cart) {
        return SharedCartListResponse.CreateResponse.builder()
                .id(cart.getId())
                .title(cart.getTitle())
                .ownerId(cart.getOwner().getId())
                .expiresAt(cart.getExpiresAt())
                .status(cart.getStatus())
                .createdAt(cart.getCreatedAt())
                .build();
    }

    private SharedCartListResponse toListResponse(SharedCart cart) {
        List<SharedCartItem> items = sharedCartItemRepository.findBySharedCartId(cart.getId());
        List<SharedCartParticipant> participants = sharedCartParticipantRepository.findBySharedCartId(cart.getId());

        double totalAmount = items.stream()
                .mapToDouble(item -> item.getPriceAtAdd().doubleValue() * item.getQuantity())
                .sum();
        int totalItems = items.stream()
                .mapToInt(SharedCartItem::getQuantity)
                .sum();

        return SharedCartListResponse.builder()
                .id(cart.getId())
                .title(cart.getTitle())
                .ownerId(cart.getOwner().getId())
                .ownerName(cart.getOwner().getFullName() != null ? cart.getOwner().getFullName() : cart.getOwner().getUsername())
                .expiresAt(cart.getExpiresAt())
                .status(cart.getStatus())
                .createdAt(cart.getCreatedAt())
                .totalItems(totalItems)
                .totalParticipants(participants.size())
                .totalAmount(totalAmount)
                .build();
    }

    private SharedCartAddItemResponse toItemResponse(SharedCartItem item) {
        return SharedCartAddItemResponse.builder()
                .id(item.getId())
                .sharedCartId(item.getSharedCart().getId())
                .productId(item.getProduct().getId())
                .userId(item.getUser().getId())
                .quantity(item.getQuantity())
                .priceAtAdd(item.getPriceAtAdd())
                .build();
    }

    private SharedCartDetailResponse.ItemDetail toItemDetailResponse(SharedCartItem item) {
        Product product = item.getProduct();
        BigDecimal subtotal = item.getPriceAtAdd().multiply(BigDecimal.valueOf(item.getQuantity()));

        return SharedCartDetailResponse.ItemDetail.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImages() != null && !product.getImages().isEmpty()
                        ? product.getImages().get(0).getUrl()
                        : null)
                .addedByUserId(item.getUser().getId())
                .addedByUserName(item.getUser().getFullName() != null
                        ? item.getUser().getFullName()
                        : item.getUser().getUsername())
                .quantity(item.getQuantity())
                .priceAtAdd(item.getPriceAtAdd())
                .subtotal(subtotal)
                .build();
    }

    private SharedCartInviteResponse toParticipantResponse(SharedCartParticipant p) {
        return SharedCartInviteResponse.builder()
                .id(p.getId())
                .sharedCartId(p.getSharedCart().getId())
                .userId(p.getUser().getId())
                .contributionAmount(p.getContributionAmount())
                .joinedAt(p.getJoinedAt())
                .build();
    }

    private SharedCartDetailResponse.ParticipantDetail toParticipantDetailResponse(SharedCartParticipant p) {
        return SharedCartDetailResponse.ParticipantDetail.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .userName(p.getUser().getFullName() != null ? p.getUser().getFullName() : p.getUser().getUsername())
                .userEmail(p.getUser().getEmail())
                .contributionAmount(p.getContributionAmount())
                .joinedAt(p.getJoinedAt())
                .build();
    }
}