package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

public class Pet {
	private String type;
	private PetTier tier;
	private boolean active;
	private float xp;
	private int level;

	public Pet(String type, PetTier tier, boolean active, float xp) {
		this.type = type;
		this.tier = tier;
		this.active = active;
		this.xp = xp;
		this.level = SBGods.instance.getSBUtil().toPetLevel((int) Math.floor(xp), tier);
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
