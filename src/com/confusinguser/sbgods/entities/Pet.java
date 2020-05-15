package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

public class Pet {
    private final String type;
    private final PetTier tier;
    private final boolean active;
    private final float xp;
    private final int level;

    public Pet(String type, PetTier tier, boolean active, float xp) {
        this.type = type;
        this.tier = tier;
        this.active = active;
        this.xp = xp;
        this.level = SBGods.getInstance().getSBUtil().toPetLevel((int) Math.floor(xp), tier);
    }

    public String getType() {
        return type;
    }

    public PetTier getTier() {
        return tier;
    }

    public boolean isActive() {
        return active;
    }

    public float getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

}
