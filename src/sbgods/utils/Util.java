package sbgods.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sbgods.SBGods;

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

	public Map.Entry<String, Double> getHighestKeyValuePair(HashMap<String, Double> map, boolean isDouble) {
		if (!isDouble) {
			return null;
		}
		Entry<String, Double> maxEntry = null;

		for (Entry<String, Double> entry : map.entrySet()) {
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
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

	public double round(double num, int toPlaces) {
		return Double.valueOf(String.format("%." + toPlaces + "f", num));
	}

}