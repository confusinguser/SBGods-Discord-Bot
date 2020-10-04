package com.confusinguser.sbgods.entities.leaderboard;

import java.util.Map;

public class DungeonExps extends LeaderboardValue {

    // Classes
    private final double healer;
    private final double mage;
    private final double berserk;
    private final double archer;
    private final double tank;

    // Dungeons
    private final double catacombs;

    public DungeonExps(double healer, double mage, double berserk, double archer, double tank, double catacombs) {
        this.healer = healer;
        this.mage = mage;
        this.berserk = berserk;
        this.archer = archer;
        this.tank = tank;
        this.catacombs = catacombs;
    }

    public DungeonExps(Map<String, Double> dungeonExps) {
        this.healer = dungeonExps.getOrDefault("healer", 0d);
        this.mage = dungeonExps.getOrDefault("mage", 0d);
        this.berserk = dungeonExps.getOrDefault("berserk", 0d);
        this.archer = dungeonExps.getOrDefault("archer", 0d);
        this.tank = dungeonExps.getOrDefault("tank", 0d);
        this.catacombs = dungeonExps.getOrDefault("catacombs", 0d);
    }

    public DungeonExps() {
        this.healer = 0;
        this.mage = 0;
        this.berserk = 0;
        this.archer = 0;
        this.tank = 0;
        this.catacombs = 0;
    }

    public double getHealerExp() {
        return healer;
    }

    public double getMageExp() {
        return mage;
    }

    public double getBerserkExp() {
        return berserk;
    }

    public double getArcherExp() {
        return archer;
    }

    public double getTankExp() {
        return tank;
    }

    public double getAverageClassExp() {
        return (tank + archer + berserk + mage + healer) / 5;
    }

    public double getTotalClassExp() {
        return tank + archer + berserk + mage + healer;
    }

    public double getCatacombsExp() {
        return catacombs;
    }

    public double getAverageDungeonExp() {
        return (catacombs) / 1;
    }

    @Override
    public double getValue() {
        return getAverageDungeonExp();
    }

    @Override
    public double getSecondaryValue() {
        return getAverageClassExp();
    }

    @Override
    public boolean isApproximate() {
        return false;
    }
}
