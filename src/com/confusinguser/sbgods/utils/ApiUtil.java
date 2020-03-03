package com.confusinguser.sbgods.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.objects.PetTier;

public class ApiUtil {

	SBGods main;

	private final String USER_AGENT = "Mozilla/5.0";
	private final String BASE_URL = "https://api.hypixel.net/";

	public ApiUtil(SBGods main) {
		this.main = main;
	}

	private final int REQUEST_RATE = 30; // unit: requests
	private long LAST_CHECK = System.currentTimeMillis();
	int allowance = REQUEST_RATE; // unit: requests

	private String getResponse(String url_string) {
		long current = System.currentTimeMillis();
		int time_passed = (int) ((current - LAST_CHECK) / 1000);
		LAST_CHECK = current;
		allowance += time_passed * REQUEST_RATE;
		if (allowance > REQUEST_RATE) {
			allowance = REQUEST_RATE; // throttle
		} if (allowance < 1) {
			try {
				Thread.sleep(4500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			allowance -= 1.0;
		}

		StringBuffer response = null;
		try {
			URL url = new URL(url_string);

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			response = new StringBuffer();

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

		} catch (IOException e) {
			System.out.println("Failed to connect to the Hypixel API. Retrying in 30 seconds...");
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e1) {
				e.printStackTrace();
			}
			return getResponse(url_string);
		}

		if (new JSONObject(response.toString()).has("throttle") && new JSONObject(response.toString()).getBoolean("throttle")) {
			//System.out.println("Got throttled, pausing for 17 seconds");
			try {
				Thread.sleep(17000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return getResponse(url_string);
		} else if (new JSONObject(response.toString()).has("cause") && new JSONObject(response.toString()).getString("cause") == "Invalid API key!") {
			System.out.println("API key is invalid. Please contact ConfusingUser#5712 for further assistance.");
		}
		return response.toString();
	}


	public ArrayList<String> getGuildMembers() {
		StringBuilder url_string = new StringBuilder(BASE_URL);
		url_string.append("guild").append("?key=" + main.getApikey()).append("&id=" + main.getGuildId());

		String response = getResponse(url_string.toString());

		ArrayList<String> output = new ArrayList<String>();
		JSONObject jsonObject = new JSONObject(response);
		jsonObject = jsonObject.getJSONObject("guild");
		JSONArray members = jsonObject.getJSONArray("members");

		JSONObject currentMember;
		String currentUuid;

		for (int i = 0; i < members.length(); i++) {
			currentMember = members.getJSONObject(i);
			currentUuid = currentMember.getString("uuid");
			output.add(currentUuid);
		}

		return output;
	}

	public ArrayList<String> getSkyblockProfilesAndUsernameFromUUID(String UUID) {
		StringBuilder url_string = new StringBuilder(BASE_URL);
		url_string.append("player").append("?key=" + main.getApikey()).append("&uuid=" + UUID);

		String response = getResponse(url_string.toString());

		JSONObject jsonObject = new JSONObject(response);
		String currentName;
		try {
			currentName = jsonObject.getJSONObject("player").getString("displayname");
			jsonObject = jsonObject.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles");
		} catch (JSONException e) {
			return new ArrayList<String>();
		}

		Set<String> profilesSet = jsonObject.keySet();
		ArrayList<String> output = new ArrayList<String>();
		output.add(currentName);
		output.addAll(profilesSet);

		return output;
	}

	public ArrayList<String> getSkyblockProfilesAndDisplaynameAndUUIDFromUsername(String name) {
		StringBuilder url_string = new StringBuilder(BASE_URL);
		url_string.append("player").append("?key=" + main.getApikey()).append("&name=" + name);

		String response = getResponse(url_string.toString());

		JSONObject jsonObject = new JSONObject(response);
		String currentName;
		String currentUuid;
		JSONObject profiles;
		try {
			currentName = jsonObject.getJSONObject("player").getString("displayname");
			currentUuid = jsonObject.getJSONObject("player").getString("uuid");
			profiles = jsonObject.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles");
		} catch (JSONException e) {
			return new ArrayList<String>();
		}

		Set<String> profilesSet = profiles.keySet();
		ArrayList<String> output = new ArrayList<String>();
		output.add(currentName);
		output.add(currentUuid);
		output.addAll(profilesSet);

		return output;
	}


	public int getProfileSlayerXP(String profileUUID, String playerUUID) {

		StringBuilder url_string = new StringBuilder(BASE_URL);
		url_string.append("skyblock/profile").append("?key=" + main.getApikey()).append("&profile=" + profileUUID);

		String response = getResponse(url_string.toString());

		String[] slayer_types = {"zombie", "spider", "wolf"};

		JSONObject jsonObject = new JSONObject(response);

		try {
			jsonObject = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(playerUUID).getJSONObject("slayer_bosses");
		} catch (JSONException e) {
			return 0;
		}

		int output = 0;
		for (String slayer_type : slayer_types) {
			try {
				output += jsonObject.getJSONObject(slayer_type).getInt("xp");
			} catch (JSONException e) {}
		}
		return output;
	}


	public ArrayList<Integer> getProfileSkills(String profileUUID, String playerUUID) {
		StringBuilder url_string = new StringBuilder(BASE_URL);
		url_string.append("skyblock/profile").append("?key=" + main.getApikey()).append("&profile=" + profileUUID);

		String response = getResponse(url_string.toString());

		String[] skill_types = {"experience_skill_combat", "experience_skill_mining", "experience_skill_alchemy", "experience_skill_farming", "experience_skill_enchanting", "experience_skill_fishing", "experience_skill_foraging"};

		JSONObject jsonObject = new JSONObject(response);

		try {
			jsonObject = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(playerUUID);
		} catch (JSONException e) {
			return new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0, 0, 0));
		}

		ArrayList<Integer> output = new ArrayList<Integer>();
		for (String skill_type : skill_types) {
			try {
				output.add((int) Math.floor(jsonObject.getDouble(skill_type)));
			} catch (JSONException e) {
				output.add(0);
			}
		}
		return output;
	}


	public String getGuildFromUUID(String UUID) {

		StringBuilder url_string = new StringBuilder(BASE_URL);
		url_string.append("guild").append("?key=" + main.getApikey()).append("&player=" + UUID);

		String response = getResponse(url_string.toString());
		JSONObject jsonObject = new JSONObject(response);

		try {
			return jsonObject.getJSONObject("guild").getString("name");
		} catch (JSONException e) {
			return null;
		}
	}

	public HashMap<String, Entry<Integer, Boolean>> getProfilePets(String profileUUID, String playerUUID) {
		StringBuilder url_string = new StringBuilder(BASE_URL);
		url_string.append("skyblock/profile").append("?key=" + main.getApikey()).append("&profile=" + profileUUID);

		String response = getResponse(url_string.toString());
		JSONObject jsonObject = new JSONObject(response);

		try {
			jsonObject = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(playerUUID);
		} catch (JSONException e) {
			return new HashMap<String, Entry<Integer, Boolean>>();
		}

		JSONArray pets;
		try {
			pets = jsonObject.getJSONArray("pets");
		} catch (JSONException e) {
			return new HashMap<String, Entry<Integer, Boolean>>();
		}

		HashMap<String, Entry<Integer, Boolean>> output = new HashMap<String, Entry<Integer, Boolean>>();
		for(int i = 0; i < pets.length(); i++) {
			String name;
			Integer level;
			String rarity;
			Boolean active;
			try {
				name = pets.getJSONObject(i).getString("type");
				rarity = pets.getJSONObject(i).getString("tier");
				level = main.getSBUtil().toPetLevel(pets.getJSONObject(i).getInt("exp"), PetTier.valueOf(rarity));
				active = pets.getJSONObject(i).getBoolean("active");
			} catch (JSONException e) {
				continue;
			}
			if (name == "") {
				continue;
			}
			name = name.toLowerCase().replace("_", " ");
			name = name.substring(0, 1).toUpperCase() + name.substring(1);
			rarity = rarity.substring(0, 1).toUpperCase() + rarity.toLowerCase().substring(1);

			output.put(rarity + " " + name, new AbstractMap.SimpleEntry<Integer, Boolean>(level, active));
		}
		return output;
	}
}