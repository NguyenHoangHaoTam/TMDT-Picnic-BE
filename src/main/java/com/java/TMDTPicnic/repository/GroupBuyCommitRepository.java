package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.GroupBuyCommit;
import com.java.TMDTPicnic.entity.GroupBuyCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupBuyCommitRepository extends JpaRepository<GroupBuyCommit, Long> {
    List<GroupBuyCommit> findByCampaign(GroupBuyCampaign campaign);
    @Query("SELECT COALESCE(SUM(c.qtyCommitted), 0) FROM GroupBuyCommit c WHERE c.campaign = :campaign")
    int sumQtyByCampaign(@Param("campaign") GroupBuyCampaign campaign);

}
