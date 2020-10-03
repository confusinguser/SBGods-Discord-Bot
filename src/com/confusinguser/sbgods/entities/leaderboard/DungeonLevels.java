package com.confusinguser.sbgods.entities.leaderboard;

import java.util.Map;

public class DungeonLevels extends LeaderboardValue {

    // Classes
    private final double healer;
    private final double mage;
    private final double berserk;
    private final double archer;
    private final double tank;

    // Dungeons
    private final double catacombs;

    public DungeonLevels(double healer, double mage, double berserk, double archer, double tank, double catacombs) {
        this.healer = healer;
        this.mage = mage;
        this.berserk = berserk;
        this.archer = archer;
        this.tank = tank;
        this.catacombs = catacombs;
    }

    public DungeonLevels(Map<String, Double> dungeonLevels) {
        this.healer = dungeonLevels.getOrDefault("healer", 0d);
        this.mage = dungeonLevels.getOrDefault("mage", 0d);
        this.berserk = dungeonLevels.getOrDefault("berserk", 0d);
        this.archer = dungeonLevels.getOrDefault("archer", 0d);
        this.tank = dungeonLevels.getOrDefault("tank", 0d);
        this.catacombs = dungeonLevels.getOrDefault("catacombs", 0d);
    }

    public DungeonLevels() {
        this.healer = 0;
        this.mage = 0;
        this.berserk = 0;
        this.archer = 0;
        this.tank = 0;
        this.catacombs = 0;
    }

    public double getHealerLevel() {
        return healer;
    }

    public double getMageLevel() {
        return mage;
    }

    public double getBerserkLevel() {
        return berserk;
    }

    public double getArcherLevel() {
        return archer;
    }

    public double getTankLevel() {
        return tank;
    }

    public double getAverageClassLevel() {
        return (tank + archer + berserk + mage + healer) / 3;
    }


    public double getCatacombsLevel() {
        return catacombs;
    }

    public double getAverageDungeonLevel() {
        return (catacombs) / 1;
    }

    @Override
    public double getValue() {
        return getAverageClassLevel();
    }

    @Override
    public double getSecondaryValue() {
        return getAverageDungeonLevel();
    }

    @Override
    public boolean isApproximate() {
        return false;
    }
}
