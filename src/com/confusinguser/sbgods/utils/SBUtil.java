package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.Pet;
import com.confusinguser.sbgods.entities.PetTier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SBUtil {

    private final SBGods main;

    public SBUtil(SBGods main) {
        this.main = main;
    }

    public int toPetLevel(int xpAmount, PetTier tier) {

        final int rarityOffset = tier.getRairityOffset();
        final List<Integer> levels = new ArrayList<>(Arrays.asList(Constants.pet_levels)).subList(rarityOffset, rarityOffset + 99);

        int xpTotal = 0;
        int level = 1;


        for (int i = 0; i < 100; i++) {
            xpTotal += levels.get(i);

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
        ArrayList<Integer> skillExpLevels = new ArrayList<>(Arrays.asList(Constants.skill_exp_levels));

        int xpTotal = 0;
        int level = 0;
        double xpForNext = Double.POSITIVE_INFINITY;
        int maxLevel = skillExpLevels.size() - 1;

        for (int x = 1; x < maxLevel; x++) {
            xpTotal += skillExpLevels.get(x);

            if (xpTotal > xp) {
                xpTotal -= skillExpLevels.get(x);
                break;
            } else {
                level = x;
            }
        }

        int xpCurrent = (int) Math.floor(xp - xpTotal);
        if (level < maxLevel)
            xpForNext = Math.ceil(skillExpLevels.get(level + 1));
        double progress = Math.max(0, Math.min(xpCurrent / xpForNext, 1));

        return level + progress;
    }

    public double toSkillLevelRunecrafting(int xp) {
        ArrayList<Integer> skillExpLevels = new ArrayList<>(Arrays.asList(Constants.runecrafting_exp_levels));

        int xpTotal = 0;
        int level = 0;
        double xpForNext = Double.POSITIVE_INFINITY;
        int maxLevel = skillExpLevels.size() - 1;

        for (int x = 1; x < maxLevel; x++) {
            xpTotal += skillExpLevels.get(x);

            if (xpTotal > xp) {
                xpTotal -= skillExpLevels.get(x);
                break;
            } else {
                level = x;
            }
        }

        int xpCurrent = (int) Math.floor(xp - xpTotal);
        if (level < maxLevel)
            xpForNext = Math.ceil(skillExpLevels.get(level + 1));
        double progress = Math.max(0, Math.min(xpCurrent / xpForNext, 1));

        return level + progress;
    }

    public int toSkillExp(int level) {
        int exp = 0;
        for (int i = 0; i < level; i++) {
            exp += Constants.skill_exp_levels[i];
        }
        return exp;
    }

    public int toSkillExp(double level) {
        int exp = toSkillExp((int) Math.floor(level));
        exp += exp * (level - Math.floor(level));

        return exp;
    }

    public ArrayList<Pet> keepHighestLevelOfPet(ArrayList<Pet> profilePets, ArrayList<Pet> totalPets) {
        ArrayList<Pet> output = new ArrayList<>(profilePets);
        for (Pet pet : profilePets) {
            if (totalPets.contains(pet)) {
                if (pet.getLevel() < findPetOfType(pet.getType(), totalPets).getLevel()) {
                    output.remove(pet);
                }
            }
        }
        return output;
    }

    private Pet findPetOfType(String type, ArrayList<Pet> allPets) {
        for (Pet pet : allPets) {
            if (pet.getType().contentEquals(type)) {
                return pet;
            }
        }
        return new Pet(null, null, false, 0);
    }

    public String alternateToNormalSkillTypes(String skill_type) {
        for (int i = 0; i < Constants.alternate_skill_types.length; i++) {
            if (Constants.alternate_skill_types[i].contentEquals(skill_type)) {
                return Constants.skill_types[i];
            }
        }
        return skill_type;
    }
}
