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

    public SkillLevels(Map<String, Double> skillMap, boolean approximate) {
        SBGods main = SBGods.getInstance();
        this.farming = skillMap.getOrDefault("farming", 0d);
        this.mining = skillMap.getOrDefault("mining", 0d);
        this.combat = skillMap.getOrDefault("combat", 0d);
        this.foraging = skillMap.getOrDefault("foraging", 0d);
        this.fishing = skillMap.getOrDefault("fishing", 0d);
        this.enchanting = skillMap.getOrDefault("enchanting", 0d);
        this.alchemy = skillMap.getOrDefault("alchemy", 0d);
        this.taming = skillMap.getOrDefault("taming", 0d);
        this.carpentry = skillMap.getOrDefault("carpentry", 0d);
        this.runecrafting = skillMap.getOrDefault("runecrafting", 0d);
        this.skillList = new ArrayList<>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy, taming));
        this.approximate = approximate;
    }

    public SkillLevels(double farming, double mining, double combat, double foraging, double fishing, double enchanting, double alchemy, double taming, double carpentry, double runecrafting, boolean approximate) {
        this.farming = farming;
        this.mining = mining;
        this.combat = combat;
        this.foraging = foraging;
        this.fishing = fishing;
        this.enchanting = enchanting;
        this.alchemy = alchemy;
        this.taming = taming;
        this.carpentry = carpentry;
        this.runecrafting = runecrafting;
        this.skillList = new ArrayList<>(Arrays.asList(farming, mining, combat, foraging, fishing, enchanting, alchemy, taming));
        this.approximate = approximate;
    }

    public static SkillLevels fromSkillExp(Map<String, Integer> skillMap, boolean approximate) {
        SBGods main = SBGods.getInstance();
        return new SkillLevels(main.getSBUtil().toSkillLevel(skillMap.getOrDefault("farming", 0)),
                main.getSBUtil().toSkillLevel(skillMap.getOrDefault("mining", 0)),
                main.getSBUtil().toSkillLevel(skillMap.getOrDefault("combat", 0)),
                main.getSBUtil().toSkillLevel(skillMap.getOrDefault("foraging", 0)),
                main.getSBUtil().toSkillLevel(skillMap.getOrDefault("fishing", 0)),
                main.getSBUtil().toSkillLevel(skillMap.getOrDefault("enchanting", 0)),
                main.getSBUtil().toSkillLevel(skillMap.getOrDefault("alchemy", 0)),
                main.getSBUtil().toSkillLevel(skillMap.getOrDefault("taming", 0)),
                main.getSBUtil().toSkillLevel(skillMap.getOrDefault("carpentry", 0)),
                main.getSBUtil().toSkillLevelRunecrafting(skillMap.getOrDefault("runecrafting", 0)),
                approximate
        );
    }

    public static SkillLevels fromSkillExp(double farming, double mining, double combat, double foraging, double fishing, double enchanting, double alchemy, double taming, double carpentry, double runecrafting, boolean approximate) {
        return new SkillLevels(farming, mining, combat, foraging, fishing, enchanting, alchemy, taming, carpentry, runecrafting, approximate);
    }

    public double getFarming() {
        return farming;
    }

    public double getMining() {
        return mining;
    }

    public double getCombat() {
        return combat;
    }

    public double getForaging() {
        return foraging;
    }

    public double getFishing() {
        return fishing;
    }

    public double getEnchanting() {
        return enchanting;
    }

    public double getAlchemy() {
        return alchemy;
    }

    public double getTaming() {
        return taming;
    }

    public double getCarpentry() {
        return carpentry;
    }

    public double getRunecrafting() {
        return runecrafting;
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

    public double getTotalSkillExp() {
        double output = 0;
        for (double skillLevel : skillList) {
            output += SBGods.getInstance().getSBUtil().toSkillExp(skillLevel);
        }
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
