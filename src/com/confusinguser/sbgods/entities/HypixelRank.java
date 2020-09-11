package com.confusinguser.sbgods.entities;

public enum HypixelRank {
    DEFAULT(0x555555, ""),
    VIP(0x55FF55, "[VIP]"),
    VIP_PLUS(0x55FF55, "[VIP+]"),
    MVP(0x00AAAA, "[MVP]"),
    MVP_PLUS(0x00AAAA, "[MVP+]"),
    SUPERSTAR(0xFFAA00, "[MVP++]");

    private final int color;
    private final String tag;

    HypixelRank(int color, String tag) {
        this.color = color;
        this.tag = tag;
    }

    public static HypixelRank getHypixelRankFromName(String rankString) {
        for (HypixelRank rank : values()) {
            if (rankString.equals(rank.getTag())) return rank;
        }
        return DEFAULT;
    }

    public int getColor() {
        return color;
    }

    public String getTag() {
        return tag;
    }
}
