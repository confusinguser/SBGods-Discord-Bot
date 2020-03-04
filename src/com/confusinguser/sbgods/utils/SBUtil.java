package com.confusinguser.sbgods.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.objects.Pet;
import com.confusinguser.sbgods.objects.PetTier;

public class SBUtil {

	SBGods main;

	public SBUtil(SBGods main) {
		this.main = main;
	}

	public int toPetLevel(int xpAmount, PetTier tier) {

		final int rarityOffset = Constants.pet_tier_offset.get(tier);
		final List<Integer> levels = new ArrayList<Integer>(Arrays.asList(Constants.pet_levels)).subList(rarityOffset, rarityOffset + 99);

		/*
		int xpMaxLevel = 0;
		for (Integer level : levels) {
			xpMaxLevel += level;
		}
		 */

		int xpTotal = 0;
		int level = 1;

		//int xpForNext = 0;

		for(int i = 0; i < 100; i++){
			xpTotal += levels.get(i);

			if(xpTotal > xpAmount){
				xpTotal -= levels.get(i);
				break;
			} else {
				level++;
			}
		}

		//int xpCurrent = (int) Math.floor(xpAmount - xpTotal);
		//int progress;

		if(level < 100) {
			//xpForNext = (int) Math.ceil(levels.get(level - 1));
			//progress = Math.max(0, Math.min(xpCurrent / xpForNext, 1));
		} else {
			level = 100;
			//xpCurrent = xpAmount - levels.get(99);
			//xpForNext = 0;
			//progress = 1;
		}

		return level;
	}

public double toSkillLevel(int xpAmount) {
	ArrayList<Integer> xpLevelArray = new ArrayList<Integer>(Arrays.asList(0, 50, 175, 375, 675, 1175, 1925, 2925, 4425, 6425, 9925, 14925, 22425, 32425, 47425, 67425, 97425, 147425, 222425, 322425, 522425, 822425, 1222425, 1722425, 2322425, 3022425, 3822425, 4722425, 5722425, 6822425, 8022425, 9322425, 10722425, 12222425, 13822425, 15522425, 17322425, 19222425, 21222425, 23322425, 25522425, 27822425, 30222425, 32722425, 35322425, 38072425, 40972425, 44072425, 47472425, 51172425, 55172425));
	int level;
	double output = xpAmount;
	for (Integer xpLevel: xpLevelArray) {
		if (xpAmount == 0) {
			return 0;
		}
		if (xpAmount > xpLevel) {
			continue;
		} else {
			level = xpLevelArray.indexOf(xpLevel) - 1;
			// Get the decimal number
			output -= xpLevelArray.get(xpLevelArray.indexOf(xpLevel) - 1);
			output /= xpLevelArray.get(xpLevelArray.indexOf(xpLevel));
			output += level;
			return output;
		}
	}
	return 50;
}

public ArrayList<Pet> keepHighestLevelOfPet(ArrayList<Pet> profilePets, ArrayList<Pet> totalPets) {
	ArrayList<Pet> output = new ArrayList<Pet>(profilePets);
	for (Pet pet : profilePets) {
		if (totalPets.contains(pet.getType())) {
			if (pet.getLevel() < findPetOfType(pet.getType(), totalPets).getLevel()) {
				output.remove(pet);
			}
		}
	}
	return output;
}

public Pet findPetOfType(String type, ArrayList<Pet> allPets) {
	for (Pet pet : allPets) {
		if (pet.getType().contentEquals(type)) {
			return pet;
		}
	}
	return new Pet(null, null, false, 0);
}
}
