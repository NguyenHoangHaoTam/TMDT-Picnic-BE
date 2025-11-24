package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.GroupBuyCampaign;
import com.java.TMDTPicnic.enums.GroupBuyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GroupBuyCampaignRepository extends JpaRepository<GroupBuyCampaign, Long> {
    List<GroupBuyCampaign> findByStatus(GroupBuyStatus status);

    // ===== GROUP BUY STATS =====
    @Query("SELECT COUNT(gbc) FROM GroupBuyCampaign gbc WHERE gbc.status = 'ACTIVE'")
    Long countActiveCampaigns();

    @Query(value = """
        SELECT COUNT(*)
        FROM group_buy_campaigns
        WHERE status = 'ACTIVE'
          AND end_at BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY)
    """, nativeQuery = true)
    Long countEndingSoonCampaigns();

    @Query("SELECT COUNT(gbc) FROM GroupBuyCampaign gbc WHERE gbc.status = 'SUCCESS'")
    Long countSuccessfulCampaigns();

    @Query("SELECT COUNT(gbc) FROM GroupBuyCampaign gbc WHERE gbc.status = 'FAILED'")
    Long countFailedCampaigns();
}
