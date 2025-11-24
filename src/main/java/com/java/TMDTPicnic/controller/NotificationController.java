package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.AcceptInvitationRequest;
import com.java.TMDTPicnic.dto.request.RejectInvitationRequest;
import com.java.TMDTPicnic.dto.response.ApiResponse;
import com.java.TMDTPicnic.dto.response.NotificationResponse;
import com.java.TMDTPicnic.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Lấy danh sách thông báo")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<NotificationResponse>>>> getNotifications(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        List<NotificationResponse> notifications = notificationService.getNotifications(userId);

        Map<String, List<NotificationResponse>> data = new HashMap<>();
        data.put("notifications", notifications);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, List<NotificationResponse>>>builder()
                        .message("Lấy danh sách thông báo thành công")
                        .data(data)
                        .build()
        );
    }

    @Operation(summary = "Lấy số lượng thông báo chưa đọc")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        Long unreadCount = notificationService.getUnreadCount(userId);

        Map<String, Long> data = new HashMap<>();
        data.put("unreadCount", unreadCount);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, Long>>builder()
                        .message("Lấy số lượng thông báo chưa đọc thành công")
                        .data(data)
                        .build()
        );
    }

    @Operation(summary = "Đánh dấu thông báo đã đọc")
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        notificationService.markAsRead(id, userId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Đã đánh dấu đã đọc")
                        .data("Success")
                        .build()
        );
    }

    @Operation(summary = "Đánh dấu tất cả đã đọc")
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        notificationService.markAllAsRead(userId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Đã đánh dấu tất cả là đã đọc")
                        .data("Success")
                        .build()
        );
    }

    @Operation(summary = "Chấp nhận lời mời")
    @PostMapping("/accept-invitation")
    public ResponseEntity<ApiResponse<String>> acceptInvitation(
            @RequestBody AcceptInvitationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        notificationService.acceptInvitation(request, userId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Đã chấp nhận lời mời tham gia giỏ hàng chung")
                        .data("Success")
                        .build()
        );
    }

    @Operation(summary = "Từ chối lời mời")
    @PostMapping("/reject-invitation")
    public ResponseEntity<ApiResponse<String>> rejectInvitation(
            @RequestBody RejectInvitationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        notificationService.rejectInvitation(request, userId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Đã từ chối lời mời")
                        .data("Success")
                        .build()
        );
    }

    @Operation(summary = "Xóa thông báo")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getClaimAsString("sub"));
        notificationService.deleteNotification(id, userId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Xóa thông báo thành công")
                        .data("Success")
                        .build()
        );
    }
}

