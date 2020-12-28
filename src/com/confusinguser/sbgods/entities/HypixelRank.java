package com.confusinguser.sbgods.entities;

public enum HypixelRank {
    DEFAULT(0x555555, "", '7'),
    VIP(0x55FF55, "[VIP]", 'a'),
    VIP_PLUS(0x55FF55, "[VIP+]", 'a'),
    MVP(0x00AAAA, "[MVP]", 'b'),
    MVP_PLUS(0x00AAAA, "[MVP+]", 'b'),
    SUPERSTAR(0xFFAA00, "[MVP++]", '6'),
    YOUTUBE(0xFF5555, "[YOUTUBE]", 'c');

    private final int color;
    private final String tag;
    private final char colorCode;

    HypixelRank(int color, String tag, char colorCode) {
        this.color = color;
        this.tag = tag;
        this.colorCode = colorCode;
    }

    public static HypixelRank getHypixelRankFromRankName(String rankString) {
        for (HypixelRank rank : values()) {
            if (rankString.equals(rank.getTag())) return rank;
        }
        return DEFAULT;
    }

    public static HypixelRank getHypixelRankFromRankColorCode(char colorCode) {
        for (HypixelRank rank : values()) {
            if (colorCode == rank.getColorCode()) return rank;
        }
        return DEFAULT;
    }

    public int getColor() {
        return color;
    }

    public String getTag() {
        return tag;
    }

    public char getColorCode() {
        return colorCode;
    }
}
