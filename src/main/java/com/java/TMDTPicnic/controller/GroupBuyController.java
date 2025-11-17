package com.java.TMDTPicnic.controller;

import com.java.TMDTPicnic.dto.request.GroupBuyCampaignCreateRequest;
import com.java.TMDTPicnic.dto.request.GroupBuyCommitRequest;
import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.service.GroupBuyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-buy")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService service;

    // === Helper method: kiểm tra role ADMIN ===
    private boolean isAdmin(Jwt jwt) {
        return jwt != null && "ADMIN".equalsIgnoreCase(jwt.getClaimAsString("role"));
    }

    // === Helper method: lấy userId từ token ===
    private Long getCurrentUserId(Jwt jwt) {
        Object idClaim = jwt.getClaim("userId");
        return idClaim != null ? Long.valueOf(idClaim.toString()) : null;
    }

    // === 1️⃣ ADMIN tạo chiến dịch mua chung ===
    @PostMapping("/campaigns")
    @Operation(summary = "Tạo chiến dịch mua chung (Admin)")
    public ResponseEntity<ApiResponse<GroupBuyCampaignCreateResponse>> createCampaign(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody GroupBuyCampaignCreateRequest req
    ) {
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<GroupBuyCampaignCreateResponse>builder()
                            .message("Chỉ Admin mới được phép tạo chiến dịch mua chung")
                            .build());
        }

        GroupBuyCampaignCreateResponse response = service.createCampaign(req);
        return ResponseEntity.ok(
                ApiResponse.<GroupBuyCampaignCreateResponse>builder()
                        .message("Tạo chiến dịch mua chung thành công")
                        .data(response)
                        .build()
        );
    }

    // === 2️⃣ Ai cũng có thể xem danh sách chiến dịch đang hoạt động ===
    @GetMapping("/campaigns/active")
    @Operation(summary = "Xem danh sách chiến dịch mua chung đang hoạt động (public)")
    public ResponseEntity<ApiResponse<List<GroupBuyCampaignSummaryResponse>>> getActiveCampaigns() {
        List<GroupBuyCampaignSummaryResponse> campaigns = service.getActiveCampaigns();
        return ResponseEntity.ok(
                ApiResponse.<List<GroupBuyCampaignSummaryResponse>>builder()
                        .message("Lấy danh sách chiến dịch mua chung thành công")
                        .data(campaigns)
                        .build()
        );
    }

    // === 3️⃣ Người dùng đã đăng nhập tham gia chiến dịch ===
    @PostMapping("/commit")
    @Operation(summary = "Người dùng tham gia chiến dịch mua chung")
    public ResponseEntity<ApiResponse<GroupBuyCommitResponse>> commit(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody GroupBuyCommitRequest req
    ) {
        Long currentUserId = getCurrentUserId(jwt);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<GroupBuyCommitResponse>builder()
                            .message("Bạn cần đăng nhập để tham gia mua chung")
                            .build());
        }

        GroupBuyCommitResponse response = service.commit(currentUserId, req);
        return ResponseEntity.ok(
                ApiResponse.<GroupBuyCommitResponse>builder()
                        .message("Tham gia chiến dịch thành công")
                        .data(response)
                        .build()
        );
    }
}
