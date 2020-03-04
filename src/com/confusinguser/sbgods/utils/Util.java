package com.confusinguser.sbgods.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.objects.SkillLevels;

public class Util {

	SBGods main;

	public Util(SBGods main) {
		this.main = main;
	}


	public Map.Entry<String, Integer> getHighestKeyValuePair(HashMap<String, Integer> map) {

		Map.Entry<String, Integer> maxEntry = null;

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
				maxEntry = entry;
			}
		}
		return maxEntry;
	}

	public Map.Entry<String, SkillLevels> getHighestKeyValuePair(HashMap<String, SkillLevels> map, boolean isDouble) {
		if (!isDouble) {
			return null;
		}
		Entry<String, SkillLevels> maxEntry = null;

		for (Entry<String, SkillLevels> entry : map.entrySet()) {
			if (maxEntry == null || entry.getValue().getAvgSkillLevel() > maxEntry.getValue().getAvgSkillLevel()) {
				maxEntry = entry;
			}
		}
		return maxEntry;
	}

	public ArrayList<String> processMessageForDiscord(String message, int limit) {
		return processMessageForDiscord(message, limit, new ArrayList<String>());
	}

	private ArrayList<String> processMessageForDiscord(String message, int limit, ArrayList<String> currentOutput) {
		ArrayList<String> output = currentOutput;
		if (message.length() > limit) {
			int lastIndex = 0;
			for (int index = message.indexOf("\n"); index >= 0; index = message.indexOf("\n", index + 1)) {
				if (index > limit) {
					output.add(message.substring(0, lastIndex));
					message = message.substring(lastIndex);
					return processMessageForDiscord(message, limit, output);
				}
				lastIndex = index;
			}
		} else {
			output.add(message);
		}
		return output;
	}

	public double getAverage(List<Double> list) {
		double output = 0;
		for (int i = 0; i < list.size(); i++) {
			output += list.get(i);
		}
		output /= list.size();
		return output;
	}

	public double round(double num, int toPlaces) {
		return Double.valueOf(String.format("%." + toPlaces + "f", num));
	}


	public String toLowerCaseButFirstLetter(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
	}

}