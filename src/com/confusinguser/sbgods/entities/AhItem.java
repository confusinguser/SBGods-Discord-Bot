package com.confusinguser.sbgods.entities;

public class AhItem {

    private final String itemName;
    private final String itemTier;
    private final long startingBid;
    private final long highestBid;
    private final String category;
    private final long end;
    private final int bids; //number of bids on the item

    public AhItem(String itemName, String itemTier, long startingBid, long highestBid, String category, long end, int bids) {
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

    public int getItemTierColor() {
        switch (itemTier) {
            case "COMMON":
                return 0xFFFFFF;
            case "UNCOMMON":
                return 0x00AA00;
            case "RARE":
                return 0x0000AA;
            case "EPIC":
                return 0xAA00AA;
            case "LEGENDARY":
                return 0xFFAA00;
            default:
                return 0xFF55FF;
        }
    }

    public long getStartingBid() {
        return startingBid;
    }

    public long getHighestBid() {
        return highestBid;
    }

    public String getCategory() {
        return category;
    }

    public long getEnd() {
        return end;
    }

    public int getBids() {
        return bids;
    }
}