package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.GroupBuyCampaign;
import com.java.TMDTPicnic.enums.GroupBuyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupBuyCampaignRepository extends JpaRepository<GroupBuyCampaign, Long> {
    List<GroupBuyCampaign> findByStatus(GroupBuyStatus status);
}
