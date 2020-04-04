package com.confusinguser.sbgods.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.confusinguser.sbgods.SBGods;

public class SkillLevels {

	private double farming;
	private double mining;
	private double combat;
	private double foraging;
	private double fishing;
	private double enchanting;
	private double alchemy;
	private double carpentry;
	private double runecrafting;
	private boolean approximate = false;
	private ArrayList<Double> skillList;

	SBGods main = SBGods.getInstance();

	public SkillLevels() {
		this.farming = 0.0;
		this.mining = 0.0;
		this.combat = 0.0;
		this.foraging = 0.0;
		this.fishing = 0.0;
		this.enchanting = 0.0;
		this.alchemy = 0.0;
		this.carpentry = 0.0;
		this.runecrafting = 0.0;
		this.skillList = new ArrayList<>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy));
	}

	public SkillLevels(boolean approximate) {
		this.farming = 0.0;
		this.mining = 0.0;
		this.combat = 0.0;
		this.foraging = 0.0;
		this.fishing = 0.0;
		this.enchanting = 0.0;
		this.alchemy = 0.0;
		this.carpentry = 0.0;
		this.runecrafting = 0.0;
		this.skillList = new ArrayList<>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy));
		this.approximate = approximate;
	}

	public SkillLevels(Map<String, Integer> skillMap, boolean approximate) {
		this.farming = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("farming", 0));
		this.mining = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("mining", 0));
		this.combat = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("combat", 0));
		this.foraging = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("foraging", 0));
		this.fishing = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("fishing", 0));
		this.enchanting = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("enchanting", 0));
		this.alchemy = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("alchemy", 0));
		this.carpentry = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("carpentry", 0));
		this.runecrafting = main.getSBUtil().toSkillLevelRunecrafting(skillMap.getOrDefault("runecrafting", 0));
		this.skillList = new ArrayList<>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy));
		this.approximate = approximate;
	}


	public int getFarming() {
		return (int) Math.floor(farming);
	}

	public int getMining() {
		return (int) Math.floor(mining);
	}

	public int getCombat() {
		return (int) Math.floor(combat);
	}

	public int getForaging() {
		return (int) Math.floor(foraging);
	}

	public int getFishing() {
		return (int) Math.floor(fishing);
	}

	public int getEnchanting() {
		return (int) Math.floor(enchanting);
	}

	public int getAlchemy() {
		return (int) Math.floor(alchemy);
	}
	
	public int getCarpentry() {
		return (int) Math.floor(carpentry);
	}

	public int getRunecrafting() {
		return (int) Math.floor(runecrafting);
	}

	public boolean isApproximate() {
		return approximate;
	}

	public double getAvgSkillLevel() {
		double output = 0;
		for (int i = 0; i < skillList.size(); i++) {
			output += skillList.get(i);
		}
		output /= skillList.size();
		return output;

	}
}
