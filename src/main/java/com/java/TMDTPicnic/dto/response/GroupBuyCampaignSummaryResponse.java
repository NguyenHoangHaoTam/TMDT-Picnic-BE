package com.java.TMDTPicnic.dto.response;

import java.math.BigDecimal;

public class GroupBuyCampaignSummaryResponse {
    private Long id;
    private String productName;
    private Integer minQtyToUnlock;
    private Integer totalCommittedQty;
    private String status;
    private String thumbnail;
    private BigDecimal discountedPrice;
    public GroupBuyCampaignSummaryResponse(Long id, String productName, Integer minQtyToUnlock, Integer totalCommittedQty,
                                      String status, String thumbnail, BigDecimal discountedPrice) {
        this.id = id;
        this.productName = productName;
        this.minQtyToUnlock = minQtyToUnlock;
        this.totalCommittedQty = totalCommittedQty;
        this.status = status;
        this.thumbnail = thumbnail;
        this.discountedPrice = discountedPrice;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getMinQtyToUnlock() { return minQtyToUnlock; }
    public void setMinQtyToUnlock(Integer minQtyToUnlock) { this.minQtyToUnlock = minQtyToUnlock; }

    public Integer getTotalCommittedQty() { return totalCommittedQty; }
    public void setTotalCommittedQty(Integer totalCommittedQty) { this.totalCommittedQty = totalCommittedQty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public BigDecimal getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(BigDecimal discountedPrice) { this.discountedPrice = discountedPrice; }
}
