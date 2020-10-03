package com.confusinguser.sbgods.entities.leaderboard;

import com.confusinguser.sbgods.SBGods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SkillLevels extends LeaderboardValue {

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

    public SkillLevels() {
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

    public SkillLevels(boolean approximate) {
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

    public SkillLevels(Map<String, Integer> skillMap, boolean approximate) {
        SBGods main = SBGods.getInstance();
        this.farming = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("farming", 0));
        this.mining = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("mining", 0));
        this.combat = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("combat", 0));
        this.foraging = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("foraging", 0));
        this.fishing = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("fishing", 0));
        this.enchanting = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("enchanting", 0));
        this.alchemy = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("alchemy", 0));
        this.taming = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("taming", 0));
        this.carpentry = main.getSBUtil().toSkillLevel(skillMap.getOrDefault("carpentry", 0));
        this.runecrafting = main.getSBUtil().toSkillLevelRunecrafting(skillMap.getOrDefault("runecrafting", 0));
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

    public double getAvgSkillLevel() {
        double output = 0;
        for (double skillLevel : skillList) {
            output += skillLevel;
        }
        output /= skillList.size();
        return output;
    }

    @Override
    public double getValue() {
        return getAvgSkillLevel();
    }

    @Override
    public double getSecondaryValue() {
        return 0;
    }
}
