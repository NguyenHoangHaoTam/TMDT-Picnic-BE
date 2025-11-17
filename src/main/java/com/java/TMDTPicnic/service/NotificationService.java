package com.java.TMDTPicnic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.TMDTPicnic.dto.request.AcceptInvitationRequest;
import com.java.TMDTPicnic.dto.request.RejectInvitationRequest;
import com.java.TMDTPicnic.dto.response.NotificationResponse;
import com.java.TMDTPicnic.entity.Notification;
import com.java.TMDTPicnic.entity.SharedCart;
import com.java.TMDTPicnic.entity.SharedCartParticipant;
import com.java.TMDTPicnic.entity.User;
import com.java.TMDTPicnic.enums.NotificationType;
import com.java.TMDTPicnic.repository.NotificationRepository;
import com.java.TMDTPicnic.repository.SharedCartParticipantRepository;
import com.java.TMDTPicnic.repository.SharedCartRepository;
import com.java.TMDTPicnic.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SharedCartRepository sharedCartRepository;
    private final SharedCartParticipantRepository sharedCartParticipantRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<NotificationResponse> getNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByUserAndReadFlag(user, false);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setReadFlag(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationRepository.markAllAsReadByUser(user);
    }

    @Transactional
    public void acceptInvitation(AcceptInvitationRequest request, Long userId) {
        Notification notification = notificationRepository.findById(request.getNotificationId())
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!"SHARED_CART_INVITATION".equals(notification.getActionType())) {
            throw new RuntimeException("Invalid notification type");
        }

        SharedCart cart = sharedCartRepository.findById(request.getSharedCartId())
                .orElseThrow(() -> new RuntimeException("Shared Cart not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra đã là participant chưa
        boolean exists = sharedCartParticipantRepository.existsBySharedCartAndUser(cart, user);
        if (!exists) {
            // Lấy contributionAmount từ metadata nếu có
            java.math.BigDecimal contributionAmount = null;
            if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
                try {
                    Map<String, Object> metadata = objectMapper.readValue(notification.getMetadata(), Map.class);
                    if (metadata.containsKey("contributionAmount") && metadata.get("contributionAmount") != null) {
                        contributionAmount = new java.math.BigDecimal(metadata.get("contributionAmount").toString());
                    }
                } catch (JsonProcessingException e) {
                    // Ignore
                }
            }

            SharedCartParticipant participant = SharedCartParticipant.builder()
                    .sharedCart(cart)
                    .user(user)
                    .contributionAmount(contributionAmount)
                    .joinedAt(LocalDateTime.now())
                    .build();
            sharedCartParticipantRepository.save(participant);
        }

        notification.setReadFlag(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void rejectInvitation(RejectInvitationRequest request, Long userId) {
        Notification notification = notificationRepository.findById(request.getNotificationId())
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setReadFlag(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        notificationRepository.delete(notification);
    }

    // Helper method để tạo notification cho shared cart invitation
    public void createSharedCartInvitationNotification(
            User invitedUser,
            User inviter,
            SharedCart cart,
            java.math.BigDecimal contributionAmount) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sharedCartId", cart.getId());
            metadata.put("sharedCartTitle", cart.getTitle());
            metadata.put("inviterName", inviter.getFullName() != null ? inviter.getFullName() : inviter.getUsername());
            metadata.put("inviterId", inviter.getId());
            if (contributionAmount != null) {
                metadata.put("contributionAmount", contributionAmount);
            }

            String metadataJson = objectMapper.writeValueAsString(metadata);

            Notification notification = Notification.builder()
                    .user(invitedUser)
                    .title("Lời mời tham gia giỏ hàng chung")
                    .message(String.format("%s đã mời bạn tham gia giỏ hàng chung '%s'",
                            inviter.getFullName() != null ? inviter.getFullName() : inviter.getUsername(),
                            cart.getTitle()))
                    .type(NotificationType.INFO)
                    .readFlag(false)
                    .createdAt(LocalDateTime.now())
                    .actionType("SHARED_CART_INVITATION")
                    .metadata(metadataJson)
                    .build();

            notificationRepository.save(notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create notification", e);
        }
    }

    // Helper method để tạo notification cho shared cart checkout
    public void createSharedCartCheckoutNotification(
            User participant,
            SharedCart cart,
            User paidByUser,
            java.math.BigDecimal paidAmount,
            String paymentMethod) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sharedCartId", cart.getId());
            metadata.put("sharedCartTitle", cart.getTitle());
            metadata.put("paidBy", paidByUser.getId());
            metadata.put("paidByName", paidByUser.getFullName() != null ? paidByUser.getFullName() : paidByUser.getUsername());
            metadata.put("paidAmount", paidAmount);
            metadata.put("paymentMethod", paymentMethod);

            String metadataJson = objectMapper.writeValueAsString(metadata);

            String paymentMethodName = paymentMethod.equals("VNPAY") ? "VNPay"
                    : paymentMethod.equals("COD") ? "COD"
                    : paymentMethod.equals("MOMO") ? "MoMo" : paymentMethod;

            Notification notification = Notification.builder()
                    .user(participant)
                    .title("Giỏ hàng chung đã được thanh toán")
                    .message(String.format("Giỏ hàng chung '%s' đã được thanh toán bởi %s với số tiền %sđ qua %s",
                            cart.getTitle(),
                            paidByUser.getFullName() != null ? paidByUser.getFullName() : paidByUser.getUsername(),
                            paidAmount,
                            paymentMethodName))
                    .type(NotificationType.SUCCESS)
                    .readFlag(false)
                    .createdAt(LocalDateTime.now())
                    .actionType("SHARED_CART_CHECKOUT")
                    .metadata(metadataJson)
                    .build();

            notificationRepository.save(notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create checkout notification", e);
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        Map<String, Object> metadata = null;
        if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
            try {
                metadata = objectMapper.readValue(notification.getMetadata(), Map.class);
            } catch (JsonProcessingException e) {
                // Ignore parsing errors
            }
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .readFlag(notification.getReadFlag())
                .createdAt(notification.getCreatedAt().toString())
                .actionType(notification.getActionType())
                .metadata(metadata)
                .build();
    }
}

