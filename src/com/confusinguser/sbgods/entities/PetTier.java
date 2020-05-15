package com.confusinguser.sbgods.entities;

public enum PetTier {
    COMMON(0),
    UNCOMMON(6),
    RARE(11),
    EPIC(16),
    LEGENDARY(20);

    private final int rairityOffSet;

    PetTier(int rairityOffSet) {
        this.rairityOffSet = rairityOffSet;
    }

    public int getRairityOffset() {
        return rairityOffSet;
    }
}
