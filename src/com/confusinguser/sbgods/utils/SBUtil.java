package com.confusinguser.sbgods.utils;

public class SBUtil {
    public static double toSkillLevel(int xp) {
        return toSkillLevel(xp, Constants.skill_exp_levels);
    }

    public static double toSkillLevelRunecrafting(int xp) {
        return toSkillLevel(xp, Constants.runecrafting_exp_levels);
    }

    public static double toSkillLevelDungeoneering(double xp) {
        return toSkillLevel(xp, Constants.dungeoneering_exp_levels);
    }

    private static double toSkillLevel(double xp, int[] nonCumulativeLevelRequirements) {
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

    public static int toSkillExp(int level) {
        int exp = 0;
        for (int i = 0; i < level && i < Constants.skill_exp_levels.length; i++) {
            exp += Constants.skill_exp_levels[i];
        }
        return exp;
    }

    public static int toSkillExp(double level) {
        int exp = toSkillExp((int) Math.floor(level));
        try {
            exp += (Constants.skill_exp_levels[(int) Math.floor(level) + 1] - Constants.skill_exp_levels[(int) Math.floor(level) - 1]) *
                    (level - Math.floor(level));
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 60;
        }
        return exp;
    }

    public static int toSkillExpRunecrafting(double level) {
        int exp = toSkillExpRunecrafting((int) Math.floor(level));
        try {
            exp += (Constants.runecrafting_exp_levels[(int) Math.floor(level) + 1] - Constants.runecrafting_exp_levels[(int) Math.floor(level) - 1]) *
                    (level - Math.floor(level));
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 24;
        }
        return exp;
    }

    public static int toSkillExpRunecrafting(int level) {
        int exp = 0;
        for (int i = 0; i < level && i < Constants.skill_exp_levels.length; i++) {
            exp += Constants.skill_exp_levels[i];
        }
        return exp;
    }

    public static String alternateToNormalSkillType(String skill_type) {
        for (int i = 0; i < Constants.alternate_skill_types.length; i++) {
            if (Constants.alternate_skill_types[i].contentEquals(skill_type)) {
                return Constants.skill_types[i];
            }
        }
        return skill_type;
    }
}
