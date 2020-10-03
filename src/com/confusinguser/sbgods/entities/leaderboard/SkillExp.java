package com.confusinguser.sbgods.entities.leaderboard;

import com.confusinguser.sbgods.SBGods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SkillExp extends LeaderboardValue {

    private final double farming;
    private final double mining;
    private final double combat;
    private final double foraging;
    private final double fishing;
    private final double enchanting;
    private final double alchemy;
    private final double taming;
    private final double carpentry;
    private final double runecrafting;
    private final List<Double> skillList;
    private boolean approximate = false;

    public SkillExp() {
        this.farming = 0.0;
        this.mining = 0.0;
        this.combat = 0.0;
        this.foraging = 0.0;
        this.fishing = 0.0;
        this.enchanting = 0.0;
        this.alchemy = 0.0;
        this.taming = 0.0;
        this.carpentry = 0.0;
        this.runecrafting = 0.0;
        this.skillList = new ArrayList<>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy, taming));
    }

    public SkillExp(boolean approximate) {
        this.farming = 0.0;
        this.mining = 0.0;
        this.combat = 0.0;
        this.foraging = 0.0;
        this.fishing = 0.0;
        this.enchanting = 0.0;
        this.alchemy = 0.0;
        this.taming = 0.0;
        this.carpentry = 0.0;
        this.runecrafting = 0.0;
        this.skillList = new ArrayList<>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy, taming));
        this.approximate = approximate;
    }

    public SkillExp(Map<String, Integer> skillMap, boolean approximate) {
        SBGods main = SBGods.getInstance();
        this.farming = skillMap.getOrDefault("farming", 0);
        this.mining = skillMap.getOrDefault("mining", 0);
        this.combat = skillMap.getOrDefault("combat", 0);
        this.foraging = skillMap.getOrDefault("foraging", 0);
        this.fishing = skillMap.getOrDefault("fishing", 0);
        this.enchanting = skillMap.getOrDefault("enchanting", 0);
        this.alchemy = skillMap.getOrDefault("alchemy", 0);
        this.taming = skillMap.getOrDefault("taming", 0);
        this.carpentry = skillMap.getOrDefault("carpentry", 0);
        this.runecrafting = skillMap.getOrDefault("runecrafting", 0);
        this.skillList = new ArrayList<>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy, taming));
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

    public int getTaming() {
        return (int) Math.floor(taming);
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

    public double getTotalSkillExp() {
        double output = 0;
        for (double skillLevel : skillList) {
            output += skillLevel;
        }
        return output;
    }

    @Override
    public double getValue() {
        return getTotalSkillExp();
    }

    @Override
    public double getSecondaryValue() {
        return 0;
    }
}
