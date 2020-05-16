package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ApiUtil {

    private final String BASE_URL = "https://api.hypixel.net/";
    private SBGods main;
    private int REQUEST_RATE; // unit: requests
    private long LAST_CHECK = System.currentTimeMillis();
    private int allowance = REQUEST_RATE; // unit: requests
    private int fails = 0;

    public ApiUtil(SBGods main) {
        this.main = main;
        REQUEST_RATE = 30 * main.keys.length;
    }

    private String getResponse(String url_string) {

        // See if request already in cache
        String cacheResponse = main.getCacheUtil().getCachedResponse(main.getCacheUtil().stripUnnecesaryInfo(url_string)).getJson();
        if (cacheResponse != null) {
            return cacheResponse;
        }

        long current = System.currentTimeMillis();
        int timePassed = (int) ((current - LAST_CHECK) / 1000);

        LAST_CHECK = current;
        // unit: seconds
        int PER = 60;
        allowance += timePassed * (REQUEST_RATE / PER);
        if (allowance > REQUEST_RATE) {
            allowance = REQUEST_RATE; // throttle
        }
        if (allowance < 1) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        } else {
            allowance -= 1;
        }

        StringBuffer response = null;
        HttpURLConnection con = null;
        IOException ioException = null;
        try {
            URL url = new URL(url_string);

            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            String USER_AGENT = "Mozilla/5.0";
            con.setRequestProperty("User-Agent", USER_AGENT);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            response = new StringBuffer();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        } catch (IOException e) {
            ioException = e;
        }

        int responseCode = -1;
        try {
            if (con != null) responseCode = con.getResponseCode();
        } catch (IOException ex) {
            fails++;
            if (fails % 10 == 0 || fails == 1) {
                main.logger.warning("Failed to connect to the Hypixel API " + fails + " times, this may be a problem: " + ex.toString() + '\n' + ex.fillInStackTrace());
            }
        }

        if (responseCode == 429) {
            try {
                Thread.sleep(17000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            return getResponse(url_string);

        } else if (responseCode == 403) {
            main.logger.severe("The API key \"" + main.getCurrentApiKey() + "\" is invalid!");
            System.exit(-1);
        } else if (ioException != null) {
            fails++;
            if (fails % 10 == 0 || fails == 1) {
                main.logger.warning("Failed to connect to the Hypixel API " + fails + " times, this may be a problem: " + ioException.toString() + '\n' + ioException.fillInStackTrace());
            }
            return getResponse(url_string);
        }
        fails = 0;
        return response.toString();
    }
    private String getNonHypixelResponse(String url_string) {

        StringBuffer response = null;
        HttpURLConnection con = null;
        IOException ioException = null;
        try {
            URL url = new URL(url_string);

            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            String USER_AGENT = "Mozilla/5.0";
            con.setRequestProperty("User-Agent", USER_AGENT);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            response = new StringBuffer();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        } catch (IOException e) {
            ioException = e;
        }

        int responseCode = -1;
        try {
            if (con != null) responseCode = con.getResponseCode();
        } catch (IOException ex) {
            fails++;
            if (fails % 10 == 0 || fails == 1) {
                main.logger.warning("Failed to connect to the API " + fails + " times, this may be a problem: " + ex.toString() + '\n' + ex.fillInStackTrace());
            }
        }

        if (responseCode == 429) {
            try {
                Thread.sleep(17000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            return getResponse(url_string);

        } else if (responseCode == 403) {
            main.logger.severe("The API key \"" + main.getCurrentApiKey() + "\" is invalid!");
            System.exit(-1);
        } else if (ioException != null) {
            fails++;
            if (fails % 10 == 0 || fails == 1) {
                main.logger.warning("Failed to connect to the API " + fails + " times, this may be a problem: " + ioException.toString() + '\n' + ioException.fillInStackTrace());
            }
            return getResponse(url_string);
        }
        fails = 0;
        return response.toString();
    }

    public ArrayList<SkyblockPlayer> getGuildMembers(HypixelGuild guild) {
        String response = getResponse(BASE_URL + "guild" + "?key=" + main.getNextApiKey() + "&id=" + guild.getGuildId());
        if (response == null) return getGuildMembers(guild);

        ArrayList<SkyblockPlayer> output = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response);
        jsonObject = jsonObject.getJSONObject("guild");
        JSONArray members = jsonObject.getJSONArray("members");

        for (int i = 0; i < members.length(); i++) {
            JSONObject currentMember = members.getJSONObject(i);
            String uuid = currentMember.getString("uuid");
            output.add(new SkyblockPlayer(uuid, null, null, null));
        }

        return output;
    }

    public SkyblockPlayer getSkyblockPlayerFromUUID(String UUID) {
        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&uuid=" + UUID);
        if (response == null) return new SkyblockPlayer();

        JSONObject jsonObject = new JSONObject(response);
        String username;
        String uuid;
        String discord;
        JSONObject profiles;
        try {
            uuid = jsonObject.getJSONObject("player").getString("uuid");
            username = jsonObject.getJSONObject("player").getString("displayname");
            profiles = jsonObject.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles");
        } catch (JSONException e) {
            return new SkyblockPlayer();
        }
        try {
            // Does not necessarily exist while the other things above have to.
            discord = jsonObject.getJSONObject("player").getJSONObject("socialMedia").getJSONObject("links").getString("DISCORD");
        } catch (JSONException e) {
            discord = null;
        }

        return new SkyblockPlayer(uuid, username, discord, new ArrayList<>(profiles.keySet()));
    }

    public SkyblockPlayer getSkyblockPlayerFromUsername(String name) {
        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&name=" + name);
        if (response == null) return new SkyblockPlayer();

        JSONObject jsonObject = new JSONObject(response);
        String username;
        String uuid;
        String discord;
        JSONObject profiles;
        try {
            uuid = jsonObject.getJSONObject("player").getString("uuid");
            username = jsonObject.getJSONObject("player").getString("displayname");
            profiles = jsonObject.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles");
        } catch (JSONException e) {
            return new SkyblockPlayer();
        }
        try {
            // Does not necessarily exist while the other things above have to.
            discord = jsonObject.getJSONObject("player").getJSONObject("socialMedia").getJSONObject("links").getString("DISCORD");
        } catch (JSONException e) {
            discord = null;
        }

        return new SkyblockPlayer(uuid, username, discord, new ArrayList<>(profiles.keySet()));
    }


    public SlayerExp getPlayerSlayerExp(String playerUUID) {
        Map<String, Integer> output = new HashMap<>();
        for (String slayer_type : Constants.slayer_types) {
            output.put(slayer_type, 0);
        }

        SkyblockPlayer thePlayer = getSkyblockPlayerFromUUID(playerUUID);

        for (String profileUUID : thePlayer.getSkyblockProfiles()) {

            String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID);
            if (response == null) return new SlayerExp();

            JSONObject jsonObject = new JSONObject(response);

            try {
                jsonObject = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(playerUUID).getJSONObject("slayer_bosses");
            } catch (JSONException e) {
                continue;
            }

            for (String slayer_type : Constants.slayer_types) {
                try {
                    output.put(slayer_type, output.get(slayer_type) + jsonObject.getJSONObject(slayer_type).getInt("xp"));
                } catch (JSONException e) {
                    continue;
                }
            }
        }
        return new SlayerExp(output);
    }

    public SlayerExp getProfileSlayerExp(String profileUUID, String playerUUID) {
        HashMap<String, Integer> output = new HashMap<>();
        for (String slayer_type : Constants.slayer_types) {
            output.put(slayer_type, 0);
        }

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID);
        if (response == null) return new SlayerExp();

        JSONObject jsonObject = new JSONObject(response);

        try {
            jsonObject = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(playerUUID).getJSONObject("slayer_bosses");
        } catch (JSONException ignored) {
        }

        for (String slayer_type : Constants.slayer_types) {
            try {
                output.put(slayer_type, output.get(slayer_type) + jsonObject.getJSONObject(slayer_type).getInt("xp"));
            } catch (JSONException ignored) {
            }
        }
        return new SlayerExp(output);
    }

    public SkillLevels getProfileSkills(String profileUUID, String playerUUID) {

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID);
        if (response == null) return new SkillLevels();

        JSONObject jsonObject = new JSONObject(response);

        try {
            jsonObject = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(playerUUID);
        } catch (JSONException e) {
            return new SkillLevels();
        }

        HashMap<String, Integer> skillArray = new HashMap<>();
        for (String skill_type : Constants.skill_types) {
            try {
                skillArray.put(skill_type, (int) Math.floor(jsonObject.getDouble("experience_skill_" + skill_type)));
            } catch (JSONException e) {
                skillArray.put(skill_type, 0);
            }
        }
        return new SkillLevels(skillArray, false);
    }

    public SkillLevels getProfileSkillsAlternate(String playerUUID) {

        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&uuid=" + playerUUID);
        if (response == null) return new SkillLevels();

        JSONObject jsonObject = new JSONObject(response);

        try {
            jsonObject = jsonObject.getJSONObject("player").getJSONObject("achievements");
        } catch (JSONException e) {
            return new SkillLevels();
        }

        HashMap<String, Integer> skillMap = new HashMap<>();
        for (String skill_type : Constants.alternate_skill_types) {
            try {
                skillMap.put(main.getSBUtil().alternateToNormalSkillTypes(skill_type), main.getSBUtil().toSkillExp(jsonObject.getInt("skyblock_" + skill_type)));
            } catch (JSONException e) {
                skillMap.put(skill_type, 0);
            }
        }
        return new SkillLevels(skillMap, true);
    }

    public String getGuildFromUUID(String UUID) {

        String response = getResponse(BASE_URL + "guild" + "?key=" + main.getNextApiKey() + "&player=" + UUID);
        if (response == null) return null;

        JSONObject jsonObject = new JSONObject(response);

        try {
            return jsonObject.getJSONObject("guild").getString("name");
        } catch (JSONException e) {
            return null;
        }
    }

    public ArrayList<Pet> getProfilePets(String profileUUID, String playerUUID) {

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID);
        if (response == null) return new ArrayList<>();

        JSONObject jsonObject = new JSONObject(response);

        try {
            jsonObject = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(playerUUID);
        } catch (JSONException e) {
            return new ArrayList<>();
        }

        JSONArray pets;
        try {
            pets = jsonObject.getJSONArray("pets");
        } catch (JSONException e) {
            return new ArrayList<>();
        }

        ArrayList<Pet> output = new ArrayList<>();
        for (int i = 0; i < pets.length(); i++) {
            String type;
            int xp;
            PetTier tier;
            boolean active;
            try {
                type = pets.getJSONObject(i).getString("type");
                tier = PetTier.valueOf(pets.getJSONObject(i).getString("tier").toUpperCase());
                xp = pets.getJSONObject(i).getInt("exp");
                active = pets.getJSONObject(i).getBoolean("active");
            } catch (JSONException e) {
                continue;
            }
            if (type.equals("")) {
                continue;
            }
            type = type.toLowerCase().replace("_", " ");
            type = type.substring(0, 1).toUpperCase() + type.substring(1);

            output.add(new Pet(type, tier, active, xp));
        }
        return output;
    }

    public Map<String, Integer> getProfileKills(String profileUUID, String playerUUID) {

        HashMap<String, Integer> output = new HashMap<>();

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID);
        if (response == null) return new HashMap<>();

        JSONObject jsonObject = new JSONObject(response);

        try {
            jsonObject = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(playerUUID).getJSONObject("stats");
        } catch (JSONException e) {
            return new HashMap<>();
        }

        for (String type : jsonObject.keySet()) {
            if (type.startsWith("kills_")) {
                output.put(main.getLangUtil().toLowerCaseButFirstLetter(type.replace("kills_", "").replace("_", " ")), jsonObject.getInt(type));
            }
        }
        return output;
    }

    public HashMap<String, Integer> getProfileDeaths(String profileUUID, String playerUUID) {

        HashMap<String, Integer> output = new HashMap<>();

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID);
        if (response == null) return new HashMap<>();

        JSONObject jsonObject = new JSONObject(response);

        try {
            jsonObject = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(playerUUID).getJSONObject("stats");
        } catch (JSONException e) {
            return new HashMap<>();
        }

        for (String type : jsonObject.keySet()) {
            if (type.startsWith("deaths_")) {
                output.put(main.getLangUtil().toLowerCaseButFirstLetter(type.replace("deaths_", "").replace("_", " ")), jsonObject.getInt(type));
            }
        }
        return output;
    }

    public SkillLevels getBestPlayerSkillLevels(String uuid) {
        SkyblockPlayer thePlayer = getSkyblockPlayerFromUUID(uuid);

        if (thePlayer.getSkyblockProfiles().isEmpty()) {
            return null;
        }

        SkillLevels highestSkillLevels = new SkillLevels();
        for (String profile : thePlayer.getSkyblockProfiles()) {
            SkillLevels skillLevels = getProfileSkills(profile, thePlayer.getUUID());

            if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
                highestSkillLevels = skillLevels;
            }
        }

        if (highestSkillLevels.getAvgSkillLevel() == 0) {
            SkillLevels skillLevels = getProfileSkillsAlternate(thePlayer.getUUID());

            if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
                highestSkillLevels = skillLevels;
            }
        }
        return highestSkillLevels;
    }

    public PlayerAH getPlayerAHFromUsername(String player, String messageId, MessageReceivedEvent e) {
        //main.logger.info("Loading " + player + "'s main data");
        e.getChannel().editMessageById(messageId, "Loading..").queue();
        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&name=" + player);
        if (response == null) return new PlayerAH("There was a error fetching that user's auctions, please try again later.");

        JSONObject jsonObject = new JSONObject(response);
        JSONObject playerData = jsonObject;
        String playerId = playerData.getJSONObject("player").getString("_id");

        Iterator<String> playerProfiles = playerData.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles").keys();

        int len = 0;
        AHItem[] items = new AHItem[14];

        //main.logger.info("Loaded " + player + "'s main data");

        e.getChannel().editMessageById(messageId, "Loading...").queue();

        Integer amountLoops = playerData.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles").length();
        Integer loops = 0;

        while(playerProfiles.hasNext()) {
            String profileId = playerProfiles.next();

            //main.logger.info("Loading " + player + "'s " + playerData.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles").getJSONObject(profileId).getString("cute_name") + " data");


            response = getResponse(BASE_URL + "skyblock/auction" + "?key=" + main.getNextApiKey() + "&profile=" + profileId + "&uuid=" + playerId);
            if (response == null) return new PlayerAH("There was a error fetching that user's auctions, please try again later.");

            jsonObject = new JSONObject(response);
            JSONArray auctionJSONArray;


            //main.logger.info("Loaded " + player + "'s " + playerData.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles").getJSONObject(profileId).getString("cute_name") + " data");

            loops++;
            e.getChannel().editMessageById(messageId, "Loading...  (" + loops + "/" + amountLoops + ")").complete();

            try {
                auctionJSONArray = jsonObject.getJSONArray("auctions");
            } catch (JSONException error) {
                return new PlayerAH("There was a error fetching that user's auctions, please try again later.");
            }

            for (int i = 0 ; i < auctionJSONArray.length(); i++) {
                JSONObject obj = auctionJSONArray.getJSONObject(i);

                if(!obj.getBoolean("claimed")) {
                    String itemName = obj.getString("item_name");
                    String itemTier = obj.getString("tier");
                    Long startingBid = obj.getLong("starting_bid");
                    Long highestBid = obj.getLong("highest_bid_amount");
                    String category = obj.getString("category");
                    Long end = obj.getLong("end");
                    Integer bids = obj.getJSONArray("bids").length(); //number of bids on the item

                    items[len] = new AHItem(itemName, itemTier, startingBid, highestBid, category, end, bids);

                    len++;
                }
            }

        }
        //main.logger.info("Done! (" + len + " items found)");
        return new PlayerAH(items).setLength(len);
    }

    public String getMcNameFromDisc(String discordName) {
        String response = getNonHypixelResponse("http://soopymc.my.to/api/sbgDiscord/getMcNameFromDisc.json?key=HoVoiuWfpdAjJhfTj0YN&disc=" + discordName);
        if (response == null) return "";

        JSONObject jsonObject = new JSONObject(response);
        JSONObject playerData = jsonObject;
        return playerData.getJSONObject("data").getString("mc");

    }

    public String getDiscNameFromMc(String mcName) {

        //main.logger.info("1");
        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&name=" + mcName);
        if (response == null) return "";

        //main.logger.info("2");

        JSONObject jsonObject = new JSONObject(response);
        String discordName;

        try {
            discordName = jsonObject.getJSONObject("player").getJSONObject("socialMedia").getJSONObject("links").getString("DISCORD");
            mcName = jsonObject.getJSONObject("player").getString("displayname");
        }
        catch (Exception e){
            return "";
        }
        //main.logger.info("3");
        response = getNonHypixelResponse("http://soopymc.my.to/api/sbgDiscord/setDiscordMcName.json?key=HoVoiuWfpdAjJhfTj0YN&disc=" + discordName.replace("#","*") + "&mc=" + mcName);

        //main.logger.info("4");
        return discordName;
    }
}

