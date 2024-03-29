package com.confusinguser.sbgods.entities.leaderboard;

import java.util.Map;

public class SlayerExp extends LeaderboardValue {

    private final int zombie;
    private final int spider;
    private final int wolf;
    private final int totalExp;


    public SlayerExp(int zombie, int spider, int wolf) {
        this.zombie = zombie;
        this.spider = spider;
        this.wolf = wolf;
        this.totalExp = zombie + spider + wolf;
    }

    public SlayerExp() {
        this.zombie = 0;
        this.spider = 0;
        this.wolf = 0;
        this.totalExp = 0;
    }

    public SlayerExp(Map<String, Integer> slayerMap) {
        this.zombie = slayerMap.getOrDefault("zombie", 0);
        this.spider = slayerMap.getOrDefault("spider", 0);
        this.wolf = slayerMap.getOrDefault("wolf", 0);
        this.totalExp = zombie + spider + wolf;
    }

    public static SlayerExp addExps(SlayerExp slayerExp, SlayerExp otherSlayerExp) {
        return new SlayerExp(
                slayerExp.getZombie() + otherSlayerExp.getZombie(),
                slayerExp.getSpider() + otherSlayerExp.getSpider(),
                slayerExp.getWolf() + otherSlayerExp.getWolf()
        );
    }

    public int getZombie() {
        return zombie;
    }

    public int getSpider() {
        return spider;
    }

    public int getWolf() {
        return wolf;
    }

    public int getTotalExp() {
        return totalExp;
    }

    @Override
    public double getValue() {
        return getTotalExp();
    }

    @Override
    public double getSecondaryValue() {
        return 0;
    }

    @Override
    public boolean isApproximate() {
        return false;
    }
}
