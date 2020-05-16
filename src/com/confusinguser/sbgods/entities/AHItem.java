package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

public class AHItem {

    private String itemName;
    private String itemTier;
    private Long startingBid;
    private Long highestBid;
    private String category;
    private Long end;
    private Integer bids; //number of bids on the item

    public AHItem(String itemName, String itemTier, Long startingBid, Long highestBid, String category, Long end, Integer bids) {
        this.itemName = itemName;
        this.itemTier = itemTier;
        this.startingBid = startingBid;
        this.highestBid = highestBid;
        this.end = end;
        this.category = category;
        this.bids = bids;
    }
    public String getItemName() {
        return itemName;
    }
    public String getItemTier() {
        return itemTier;
    }
    public Long getStartingBid() {
        return startingBid;
    }
    public Long getHighestBid() {
        return highestBid;
    }
    public String getCategory() {
        return category;
    }
    public Long getEnd() {
        return end;
    }
    public Integer getBids() {
        return bids;
    }
}