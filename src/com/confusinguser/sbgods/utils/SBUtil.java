package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.PetTier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SBUtil {

    public SBUtil(SBGods main) {
    }

    public int toPetLevel(int xpAmount, PetTier tier) {

        int rarityOffset = tier.getRairityOffset();
        List<Integer> levels = new ArrayList<>(Arrays.asList(Constants.pet_levels)).subList(rarityOffset, rarityOffset + 99);

        int xpTotal = 0;
        int level = 1;

        for (int i = 0; i < 100; i++) {
            try {
                xpTotal += levels.get(i);
            } catch (IndexOutOfBoundsException ex) {
                return 100;
            }

            if (xpTotal > xpAmount) {
                break;
            } else {
                level++;
            }
        }

        if (level > 100) {
            level = 100;
        }

        return level;
    }

    public double toSkillLevel(int xp) {
        return toSkillLevel(xp, Constants.skill_exp_levels);
    }

    public double toSkillLevelRunecrafting(int xp) {
        return toSkillLevel(xp, Constants.runecrafting_exp_levels);
    }

    public double toSkillLevelDungeoneering(double xp) {
        return toSkillLevel(xp, Constants.dungeoneering_exp_levels);
    }

    private double toSkillLevel(double xp, int[] nonCumulativeLevelRequirements) {
        int xpTotal = 0;
        int level = 0;
        double xpForNext = Double.POSITIVE_INFINITY;
        int maxLevel = nonCumulativeLevelRequirements.length - 1;

        for (int x = 1; x <= maxLevel; x++) {
            xpTotal += nonCumulativeLevelRequirements[x];

            if (xpTotal > xp) {
                xpTotal -= nonCumulativeLevelRequirements[x];
                break;
            } else {
                level = x;
            }
        }

        int xpCurrent = (int) Math.floor(xp - xpTotal);
        if (level < maxLevel)
            xpForNext = Math.ceil(nonCumulativeLevelRequirements[level + 1]);
        double progress = Math.max(0, Math.min(xpCurrent / xpForNext, 1));

        return level + progress;
    }

    public int toSkillExp(int level) {
        return toSkillExp((double) level);
    }

    public int toSkillExp(double level) {
        int exp = 0;
        for (int i = 0; i < level && i < Constants.skill_exp_levels.length; i++) {
            exp += Constants.skill_exp_levels[i];
        }
        if (level == 50) return Constants.skill_exp_levels[49];
        exp += (Constants.skill_exp_levels[(int) Math.floor(level) + 1] - Constants.skill_exp_levels[(int) Math.floor(level) - 1]) *
                (level - Math.floor(level));

        return exp;
    }

    public int toSkillExpRunecrafting(double level) {
        int exp = 0;
        for (int i = 0; i < level && i < Constants.runecrafting_exp_levels.length; i++) {
            exp += Constants.runecrafting_exp_levels[i];
        }
        if (level == 24) return Constants.runecrafting_exp_levels[23];
        exp += (Constants.runecrafting_exp_levels[(int) Math.floor(level) + 1] - Constants.runecrafting_exp_levels[(int) Math.floor(level) - 1]) *
                (level - Math.floor(level));

        return exp;
    }

    public String alternateToNormalSkillType(String skill_type) {
        for (int i = 0; i < Constants.alternate_skill_types.length; i++) {
            if (Constants.alternate_skill_types[i].contentEquals(skill_type)) {
                return Constants.skill_types[i];
            }
        }
        return skill_type;
    }
}
