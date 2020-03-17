package com.confusinguser.sbgods.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.SkillLevels;

public class Util {

	SBGods main;

	public Util(SBGods main) {
		this.main = main;
	}


	public Entry<String, Integer> getHighestKeyValuePair(HashMap<String, Integer> map, int position) {
		List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(map.entrySet());
		list.sort(new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		return list.get(position);
	}

	public Entry<String, SkillLevels> getHighestKeyValuePair(HashMap<String, SkillLevels> map, int position, boolean isSkillLevel) {
		if (!isSkillLevel) {
			return null;
		}

		List<Entry<String, SkillLevels>> list = new ArrayList<Entry<String, SkillLevels>>(map.entrySet());
		list.sort(new Comparator<Entry<String, SkillLevels>>() {
			public int compare(Entry<String, SkillLevels> o1, Entry<String, SkillLevels> o2) {
				return ((Double) o2.getValue().getAvgSkillLevel()).compareTo(o1.getValue().getAvgSkillLevel());
			}
		});

		return list.get(position);
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
}