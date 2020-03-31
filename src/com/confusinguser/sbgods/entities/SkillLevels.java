package com.confusinguser.sbgods.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.confusinguser.sbgods.SBGods;

public class SkillLevels {

	private double farming;
	private double mining;
	private double combat;
	private double foraging;
	private double fishing;
	private double enchanting;
	private double alchemy;
	private ArrayList<Double> skillList;
	private boolean approximate = false;

	SBGods main = SBGods.instance;

	public SkillLevels() {
		this.farming = 0.0;
		this.mining = 0.0;
		this.combat = 0.0;
		this.foraging = 0.0;
		this.fishing = 0.0;
		this.enchanting = 0.0;
		this.alchemy = 0.0;
		this.skillList = new ArrayList<Double>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy));
	}

	public SkillLevels(int farming, int mining, int combat, int foraging, int fishing, int enchanting, int alchemy) {
		this.farming = main.getSBUtil().toSkillLevel(farming);
		this.mining = main.getSBUtil().toSkillLevel(mining);
		this.combat = main.getSBUtil().toSkillLevel(combat);
		this.foraging = main.getSBUtil().toSkillLevel(foraging);
		this.fishing = main.getSBUtil().toSkillLevel(fishing);
		this.enchanting = main.getSBUtil().toSkillLevel(enchanting);
		this.alchemy = main.getSBUtil().toSkillLevel(alchemy);
		this.skillList = new ArrayList<Double>(Arrays.asList(this.farming, this.mining, this.combat, this.foraging, this.fishing, this.enchanting, this.alchemy));
	}

	public SkillLevels(HashMap<String, Integer> skillArray) {
		this.farming = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("farming", 0));
		this.mining = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("mining", 0));
		this.combat = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("combat", 0));
		this.foraging = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("foraging", 0));
		this.fishing = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("fishing", 0));
		this.enchanting = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("enchanting", 0));
		this.alchemy = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("alchemy", 0));
		this.skillList = new ArrayList<Double>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy));
	}

	public SkillLevels(boolean approximate) {
		this.farming = 0.0;
		this.mining = 0.0;
		this.combat = 0.0;
		this.foraging = 0.0;
		this.fishing = 0.0;
		this.enchanting = 0.0;
		this.alchemy = 0.0;
		this.skillList = new ArrayList<Double>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy));
		this.approximate = approximate;
	}

	public SkillLevels(int farming, int mining, int combat, int foraging, int fishing, int enchanting, int alchemy, boolean approximate) {
		this.farming = main.getSBUtil().toSkillLevel(farming);
		this.mining = main.getSBUtil().toSkillLevel(mining);
		this.combat = main.getSBUtil().toSkillLevel(combat);
		this.foraging = main.getSBUtil().toSkillLevel(foraging);
		this.fishing = main.getSBUtil().toSkillLevel(fishing);
		this.enchanting = main.getSBUtil().toSkillLevel(enchanting);
		this.alchemy = main.getSBUtil().toSkillLevel(alchemy);
		this.skillList = new ArrayList<Double>(Arrays.asList(this.farming, this.mining, this.combat, this.foraging, this.fishing, this.enchanting, this.alchemy));
		this.approximate = approximate;
	}

	public SkillLevels(HashMap<String, Integer> skillArray, boolean approximate) {
		this.farming = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("farming", 0));
		this.mining = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("mining", 0));
		this.combat = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("combat", 0));
		this.foraging = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("foraging", 0));
		this.fishing = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("fishing", 0));
		this.enchanting = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("enchanting", 0));
		this.alchemy = main.getSBUtil().toSkillLevel(skillArray.getOrDefault("alchemy", 0));
		this.skillList = new ArrayList<Double>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy));
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

	public ArrayList<Double> getSkillList() {
		return skillList;
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
