package com.java.TMDTPicnic.service;

import com.java.TMDTPicnic.dto.response.*;
import com.java.TMDTPicnic.dto.request.*;
import com.java.TMDTPicnic.entity.*;
import com.java.TMDTPicnic.enums.GroupBuyStatus;
import com.java.TMDTPicnic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupBuyService {

    private final GroupBuyCampaignRepository campaignRepo;
    private final GroupBuyCommitRepository commitRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    // ==========================================================
    // 1️⃣ TẠO CHIẾN DỊCH MUA CHUNG (Admin)
    // ==========================================================
    @Transactional
    public GroupBuyCampaignCreateResponse createCampaign(GroupBuyCampaignCreateRequest req) {
        // Kiểm tra logic thời gian
        if (req.getStartAt().isAfter(req.getEndAt())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        GroupBuyCampaign campaign = GroupBuyCampaign.builder()
                .product(product)
                .minQtyToUnlock(req.getMinQtyToUnlock())
                .discountedPrice(req.getDiscountedPrice())
                .startAt(req.getStartAt())
                .endAt(req.getEndAt())
                .status(GroupBuyStatus.ACTIVE)
                .build();

        campaignRepo.save(campaign);

        return new GroupBuyCampaignCreateResponse(
                campaign.getId(),
                product.getName(),
                campaign.getStatus().name(),
                "Group buy campaign created successfully"
        );
    }

    // ==========================================================
    // 2️⃣ DANH SÁCH CHIẾN DỊCH ĐANG HOẠT ĐỘNG
    // ==========================================================
    public List<GroupBuyCampaignSummaryResponse> getActiveCampaigns() {
        return campaignRepo.findByStatus(GroupBuyStatus.ACTIVE)
                .stream()
                .map(c -> {
                    int totalCommitted = commitRepo.sumQtyByCampaign(c);
                    return new GroupBuyCampaignSummaryResponse(
                            c.getId(),
                            c.getProduct().getName(),
                            c.getMinQtyToUnlock(),
                            totalCommitted,
                            c.getStatus().toString(),
                            c.getProduct().getThumbnail(),
                            c.getDiscountedPrice()
                    );
                })
                .collect(Collectors.toList());
    }

    // ==========================================================
    // 3️⃣ NGƯỜI DÙNG THAM GIA MUA CHUNG (Commit)
    // ==========================================================
    @Transactional
    public GroupBuyCommitResponse commit(Long userId, GroupBuyCommitRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        GroupBuyCampaign campaign = campaignRepo.findById(req.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found."));

        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra trạng thái hợp lệ
        if (campaign.getStatus() != GroupBuyStatus.ACTIVE) {
            throw new IllegalStateException("Campaign is not active.");
        }
        if (campaign.getStartAt().isAfter(now)) {
            throw new IllegalStateException("Campaign has not started yet.");
        }
        if (campaign.getEndAt().isBefore(now)) {
            throw new IllegalStateException("Campaign has already ended.");
        }

        // Tạo commit mới
        GroupBuyCommit commit = GroupBuyCommit.builder()
                .user(user)
                .campaign(campaign)
                .qtyCommitted(req.getQtyCommitted())
                .amountPaid(req.getAmountPaid())
                .committedAt(now)
                .build();

        commitRepo.save(commit);

        // Cập nhật trạng thái nếu đạt minQty
        int totalCommitted = commitRepo.sumQtyByCampaign(campaign);
        GroupBuyStatus oldStatus = campaign.getStatus();

        if (totalCommitted >= campaign.getMinQtyToUnlock()) {
            campaign.setStatus(GroupBuyStatus.SUCCESS);
            campaignRepo.save(campaign);

            // Log hoặc gửi thông báo thay đổi trạng thái
            GroupBuyStatusUpdateResponse updateDTO = new GroupBuyStatusUpdateResponse(
                    campaign.getId(),
                    totalCommitted,
                    campaign.getMinQtyToUnlock(),
                    oldStatus.name(),
                    campaign.getStatus().name()
            );
            log.info("✅ Campaign status updated: {}", updateDTO);
        }

        return new GroupBuyCommitResponse(
                commit.getId(),
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                req.getQtyCommitted(),
                req.getAmountPaid(),
                commit.getCommittedAt()
        );
    }

    // ==========================================================
    // 4️⃣ CẬP NHẬT TRẠNG THÁI CHIẾN DỊCH (Cron Job)
    // ==========================================================
    @Transactional
    public void updateCampaignStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<GroupBuyCampaign> campaigns = campaignRepo.findAll();

        for (GroupBuyCampaign c : campaigns) {
            int totalCommitted = commitRepo.sumQtyByCampaign(c);
            GroupBuyStatus oldStatus = c.getStatus();
            GroupBuyStatus newStatus = oldStatus;

            if (c.getEndAt().isBefore(now) && totalCommitted < c.getMinQtyToUnlock()) {
                newStatus = GroupBuyStatus.EXPIRED;
            } else if (totalCommitted >= c.getMinQtyToUnlock()) {
                newStatus = GroupBuyStatus.SUCCESS;
            }

            if (newStatus != oldStatus) {
                c.setStatus(newStatus);
                campaignRepo.save(c);

                GroupBuyStatusUpdateResponse updateDTO = new GroupBuyStatusUpdateResponse(
                        c.getId(),
                        totalCommitted,
                        c.getMinQtyToUnlock(),
                        oldStatus.name(),
                        newStatus.name()
                );
                log.info("🔄 Campaign status changed: {}", updateDTO);
            }
        }
    }
}
