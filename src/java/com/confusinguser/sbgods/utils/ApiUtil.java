package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.*;
import com.confusinguser.sbgods.entities.banking.BankTransaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ApiUtil {

    public final String BASE_URL = "https://api.hypixel.net/";
    private final String USER_AGENT = "Mozilla/5.0";
    private final SBGods main;
    private final int REQUEST_RATE; // unit: requests
    private long LAST_CHECK = System.currentTimeMillis();
    private int allowance; // unit: requests
    private int fails = 0;

    public ApiUtil(SBGods main) {
        this.main = main;
        REQUEST_RATE = 115 * main.getKeys().length; //caps at 120 actually
        allowance = REQUEST_RATE;
    }

    public String getResponse(String url_string, int cacheTime) {
        // See if request already in cache
        Response cacheResponse = main.getCacheUtil().getCachedResponse(main.getCacheUtil().stripUnnecesaryInfo(url_string), cacheTime);
        String cacheResponseStr = cacheResponse.getJson();
        long current = System.currentTimeMillis();
        if (cacheResponseStr != null) {
            return cacheResponseStr;
        }

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
            if (fails % 10 == 0) {
                main.logger.warning("Failed to connect to the Hypixel API " + fails + " times: " + ex);
            }
        }

        if (responseCode == 429) {
            try {
                Thread.sleep(17000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getResponse(url_string, cacheTime);

        } else if (responseCode == 403) {
            main.logger.severe("The API key \"" + main.getCurrentApiKey() + "\" is invalid!");
            System.exit(-1);
        } else if (ioException != null) {
            fails++;
            if (fails % 10 == 0) {
                main.logger.warning("Failed to connect to the Hypixel API " + fails + " times: " + ioException);
            }
            return getResponse(url_string, cacheTime);
        }
        fails = 0;
        main.getCacheUtil().addToCache(main.getCacheUtil().stripUnnecesaryInfo(url_string), response.toString());
        return response.toString();
    }

    public String getNonHypixelResponse(String url_string) {
        StringBuffer response = null;
        HttpURLConnection con = null;
        IOException ioException = null;
        try {
            URL url = new URL(url_string);

            con = (HttpURLConnection) url.openConnection();
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
            ioException = e;
        }

        int responseCode = -1;
        try {
            if (con != null) responseCode = con.getResponseCode();
        } catch (IOException ex) {
            fails++;
            if (fails % 10 == 0) {
                main.logger.warning("Failed to connect to the API " + fails + " times: " + ex);
            }
        }

        if (responseCode == 429) {
            try {
                Thread.sleep(17000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getNonHypixelResponse(url_string);

        } else if (responseCode == 403) {
            main.logger.severe("The API key \"" + main.getCurrentApiKey() + "\" is invalid!");
            System.exit(-1);
        } else if (ioException != null) {
            fails++;
            if (fails % 10 == 0) {
                main.logger.warning("Failed to connect to the API " + fails + " times: " + ioException);
            }
            return getNonHypixelResponse(url_string);
        }
        fails = 0;
        return response.toString();
    }

    public ArrayList<Player> getGuildMembers(HypixelGuild guild) {
        String response = getResponse(BASE_URL + "guild" + "?key=" + main.getNextApiKey() + "&id=" + guild.getGuildId(), 300000);
        if (response == null) return getGuildMembers(guild);

        ArrayList<Player> output = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response);
        jsonObject = jsonObject.getJSONObject("guild");
        JSONArray members = jsonObject.getJSONArray("members");

        guild.setPlayerSize(members.length());

        for (int i = 0; i < members.length(); i++) {
            JSONObject currentMember = members.getJSONObject(i);
            String uuid = currentMember.getString("uuid");
            String guildRank = currentMember.getString("rank");
            int guildJoined = currentMember.getInt("joined");
            output.add(new Player(uuid, guildRank, guildJoined));
        }

        return output;
    }

    public ArrayList<Player> getGuildMembersDeep(HypixelGuild guild) { // Might be used in the future
        String response = getResponse(BASE_URL + "guild" + "?key=" + main.getNextApiKey() + "&id=" + guild.getGuildId(), 300000);
        if (response == null) return getGuildMembersDeep(guild);

        ArrayList<Player> output = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response);
        jsonObject = jsonObject.getJSONObject("guild");
        JSONArray members = jsonObject.getJSONArray("members");

        for (int i = 0; i < members.length(); i++) {
            JSONObject currentMember = members.getJSONObject(i);
            String uuid = currentMember.getString("uuid");
            output.add(getPlayerFromUUID(uuid));
        }

        return output;
    }

    public Player getPlayerFromUUID(String UUID) {
        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&uuid=" + UUID, 300000);
        if (response == null) return new Player();

        JSONObject jsonObject = new JSONObject(response);
        String username;
        String uuid;
        String discord;
        int lastLogin;
        int lastLogout;
        JSONObject profiles;
        try {
            uuid = jsonObject.getJSONObject("player").getString("uuid");
            username = jsonObject.getJSONObject("player").getString("displayname");
            profiles = jsonObject.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles");
            lastLogin = jsonObject.getJSONObject("player").getInt("lastLogin");
            lastLogout = jsonObject.getJSONObject("player").getInt("lastLogout");
        } catch (JSONException e) {
            return new Player();
        }
        try {
            // Does not necessarily exist while the other things above have to.
            discord = jsonObject.getJSONObject("player").getJSONObject("socialMedia").getJSONObject("links").getString("DISCORD");
            setDiscNameFromMc(username, discord);
        } catch (JSONException e) {
            discord = "";
        }

        return new Player(uuid, username, discord, lastLogin, lastLogout, new ArrayList<>(profiles.keySet()));
    }

    public Player getPlayerFromUsername(String name) {
        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&name=" + name, 300000);
        if (response == null) return new Player();

        JSONObject jsonObject = new JSONObject(response);
        String username;
        String uuid;
        String discord;
        int lastLogin;
        int lastLogout;
        JSONObject profiles;
        try {
            uuid = jsonObject.getJSONObject("player").getString("uuid");
            username = jsonObject.getJSONObject("player").getString("displayname");
            profiles = jsonObject.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles");
            lastLogin = jsonObject.getJSONObject("player").getInt("lastLogin");
            lastLogout = jsonObject.getJSONObject("player").getInt("lastLogout");
        } catch (JSONException e) {
            return new Player();
        }
        try {
            // Does not necessarily exist while the other things above have to.
            discord = jsonObject.getJSONObject("player").getJSONObject("socialMedia").getJSONObject("links").getString("DISCORD");
            setDiscNameFromMc(username, discord);
        } catch (JSONException e) {
            discord = "";
        }

        return new Player(uuid, username, discord, lastLogin, lastLogout, new ArrayList<>(profiles.keySet()));
    }


    public SlayerExp getPlayerSlayerExp(String playerUUID) {
        Map<String, Integer> output = new HashMap<>();
        for (String slayer_type : Constants.slayer_types) {
            output.put(slayer_type, 0);
        }

        Player thePlayer = getPlayerFromUUID(playerUUID);

        for (String profileUUID : thePlayer.getSkyblockProfiles()) {

            String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID, thePlayer.isOnline() ? 60000 /* 1 min */ : 3600000 /* 1h */);
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
                } catch (JSONException ignored) {
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

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID, 300000);
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

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID, 300000);
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

        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&uuid=" + playerUUID, 300000);
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
                skillMap.put(main.getSBUtil().alternateToNormalSkillTypes(skill_type), main.getSBUtil().toSkillExp(jsonObject.getInt("skyblock_" + skill_type)));
            } catch (JSONException e) {
                skillMap.put(skill_type, 0);
            }
        }
        return new SkillLevels(skillMap, true);
    }

    public SkillExp getProfileSkillExp(String profileUUID, String playerUUID) {

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID, 300000);
        if (response == null) return new SkillExp();

        JSONObject jsonObject = new JSONObject(response);

        try {
            jsonObject = jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(playerUUID);
        } catch (JSONException e) {
            return new SkillExp();
        }

        HashMap<String, Integer> skillArray = new HashMap<>();
        for (String skill_type : Constants.skill_types) {
            try {
                skillArray.put(skill_type, (int) Math.floor(jsonObject.getDouble("experience_skill_" + skill_type)));
            } catch (JSONException e) {
                skillArray.put(skill_type, 0);
            }
        }
        return new SkillExp(skillArray, false);
    }

    public SkillExp getProfileSkillExpAlternate(String playerUUID) {

        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&uuid=" + playerUUID, 300000);
        if (response == null) return new SkillExp();

        JSONObject jsonObject = new JSONObject(response);

        try {
            jsonObject = jsonObject.getJSONObject("player").getJSONObject("achievements");
        } catch (JSONException e) {
            return new SkillExp();
        }

        HashMap<String, Integer> skillMap = new HashMap<>();
        for (String skill_type : Constants.alternate_skill_types) {
            try {
                skillMap.put(main.getSBUtil().alternateToNormalSkillTypes(skill_type), main.getSBUtil().toSkillExp(jsonObject.getInt("skyblock_" + skill_type)));
                skillMap.put(main.getSBUtil().alternateToNormalSkillTypes(skill_type), main.getSBUtil().toSkillExp(jsonObject.getInt("skyblock_" + skill_type)));
            } catch (JSONException e) {
                skillMap.put(skill_type, 0);
            }
        }
        return new SkillExp(skillMap, true);
    }

    public String getGuildFromUUID(String UUID) {

        String response = getResponse(BASE_URL + "guild" + "?key=" + main.getNextApiKey() + "&player=" + UUID, 300000);
        if (response == null) return null;

        JSONObject jsonObject = new JSONObject(response);

        try {
            return jsonObject.getJSONObject("guild").getString("name");
        } catch (JSONException e) {
            return null;
        }
    }

    public String getGuildIDFromUUID(String UUID) {

        String response = getResponse(BASE_URL + "guild" + "?key=" + main.getNextApiKey() + "&player=" + UUID, 300000);
        if (response == null) return null;

        JSONObject jsonObject = new JSONObject(response);

        try {
            return jsonObject.getJSONObject("guild").getString("_id");
        } catch (JSONException e) {
            return null;
        }
    }

    public ArrayList<Pet> getProfilePets(String profileUUID, String playerUUID) {

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID, 300000);
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

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID, 300000);
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

        String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID, 300000);
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

    public SkillLevels getBestProfileSkillLevels(String uuid) {
        Player thePlayer = getPlayerFromUUID(uuid);

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

    public SkillExp getBestProfileSkillExp(String uuid) {
        Player thePlayer = getPlayerFromUUID(uuid);

        if (thePlayer.getSkyblockProfiles().isEmpty()) {
            return null;
        }

        SkillExp highestSkillLevels = new SkillExp();
        for (String profile : thePlayer.getSkyblockProfiles()) {
            SkillExp skillLevels = getProfileSkillExp(profile, thePlayer.getUUID());

            if (highestSkillLevels.getTotalSkillExp() < skillLevels.getTotalSkillExp()) {
                highestSkillLevels = skillLevels;
            }
        }

        if (highestSkillLevels.getTotalSkillExp() == 0) {
            SkillExp skillLevels = getProfileSkillExpAlternate(thePlayer.getUUID());

            if (highestSkillLevels.getTotalSkillExp() < skillLevels.getTotalSkillExp()) {
                highestSkillLevels = skillLevels;
            }
        }
        return highestSkillLevels;
    }

    public PlayerAH getPlayerAHFromUsername(Player player) {
        List<AhItem> items = new ArrayList<>();
        for (String profile : player.getSkyblockProfiles()) {
            String response = getResponse(BASE_URL + "skyblock/auction" + "?key=" + main.getNextApiKey() + "&profile=" + profile, 60000);
            if (response == null)
                return new PlayerAH("There was an error fetching that user's auctions, please try again later.");

            JSONArray auctionJSONArray;
            try {
                auctionJSONArray = new JSONObject(response).getJSONArray("auctions");
            } catch (JSONException error) {
                return new PlayerAH("There was an error fetching the player's auctions, please try again later.");
            }

            List<JSONObject> unclaimedAuctionsJson = main.getUtil().getJSONObjectListByJSONArray(auctionJSONArray).stream().filter((auction) -> !auction.getBoolean("claimed")).collect(Collectors.toList());
            for (JSONObject unclaimedAuction : unclaimedAuctionsJson) {
                String itemName = unclaimedAuction.getString("item_name");
                String itemTier = unclaimedAuction.getString("tier");
                Long startingBid = unclaimedAuction.getLong("starting_bid");
                Long highestBid = unclaimedAuction.getLong("highest_bid_amount");
                String category = unclaimedAuction.getString("category");
                Long end = unclaimedAuction.getLong("end");
                Integer bids = unclaimedAuction.getJSONArray("bids").length(); // Number of bids on the item

                items.add(new AhItem(itemName, itemTier, startingBid, highestBid, category, end, bids));
            }
        }
        return new PlayerAH(items.toArray(new AhItem[0]));
    }

    public String getMcNameFromDisc(String discordName) {
        discordName = discordName.replace("#", "*");
        try {
            String response = getNonHypixelResponse("https://soopymc.my.to/api/sbgDiscord/getMcNameFromDisc.json?key=HoVoiuWfpdAjJhfTj0YN&disc=" + discordName.replace(" ", "%20"));
            if (response == null) return "";

            return new JSONObject(response).getJSONObject("data").getString("mc");
        } catch (Exception err) {
            return "";
        }

    }

    public void setDiscNameFromMc(String mcName, String discordName) { // For auto verification in the future it will already have data
        getNonHypixelResponse("http://soopymc.my.to/api/sbgDiscord/setDiscordMcName.json?key=HoVoiuWfpdAjJhfTj0YN&disc=" + discordName.replace("#", "*").replace(" ", "%20") + "&mc=" + mcName);
    }

    public Path downloadFile(String urlStr, File file) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoInput(true);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Authorization", "token f159901613c898cb80cdc39b3d8f89d2eb9f51bb");
        con.setRequestProperty("Accept", "application/octet-stream");

        BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int count;
        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();
        return Paths.get(file.getPath());
    }

    public Map.Entry<String, String> getLatestReleaseUrl() {
        StringBuffer response = null;
        HttpURLConnection con;
        try {
            URL url = new URL("https://api.github.com/repos/confusinguser/SBGods-Discord-Bot/releases");

            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Authorization", "token f159901613c898cb80cdc39b3d8f89d2eb9f51bb");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            response = new StringBuffer();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        } catch (IOException e) {
            main.logger.warning("Could not download latest release: " + e.getMessage() + e);
        }

        if (response == null) {
            return new AbstractMap.SimpleImmutableEntry<>("", "");
        }
        try {
            JSONArray output = new JSONArray(response.toString());
            return new AbstractMap.SimpleImmutableEntry<>(output.getJSONObject(0).getJSONArray("assets").getJSONObject(0).getString("name"),
                    output.getJSONObject(0).getJSONArray("assets").getJSONObject(0).getString("url"));
        } catch (JSONException e) {
            return new AbstractMap.SimpleImmutableEntry<>("", "");
        }
    }

    public JSONObject getTaxData() {
        return new JSONObject(getNonHypixelResponse("https://soopymc.my.to/api/sbgDiscord/getTaxData.json?key=HoVoiuWfpdAjJhfTj0YN")).getJSONObject("tax");
    }

    public void setTaxData(JSONObject data) {

        String dataString = data.toString(4);

        try {
            URL url = new URL("https://soopymc.my.to/api/sbgDiscord/updateTaxData.json?key=HoVoiuWfpdAjJhfTj0YN");

            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);

            byte[] out = dataString.getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }

        } catch (IOException e) {
            main.logger.warning("Could not set tax data: " + e.getMessage() + e);
        }
    }

    public JSONArray getGuildRanksChange() {
        return new JSONObject(getNonHypixelResponse("https://soopymc.my.to/api/sbgDiscord/getGuildRanksChange.json?key=HoVoiuWfpdAjJhfTj0YN")).getJSONArray("data");
    }

    public void setGuildRanksChange(JSONArray data) {
        String dataString = data.toString(4);

        try {
            URL url = new URL("https://soopymc.my.to/api/sbgDiscord/setGuildRanksChange.json?key=HoVoiuWfpdAjJhfTj0YN");

            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);

            byte[] out = dataString.getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }

        } catch (IOException e) {
            main.logger.warning(main.getLangUtil().beautifyStackTrace(e.getStackTrace(), e));
        }
    }

    public JSONArray getEventData() {
        return new JSONObject(getNonHypixelResponse("https://soopymc.my.to/api/sbgDiscord/getEventData.json?key=HoVoiuWfpdAjJhfTj0YN")).getJSONArray("data");
    }

    public void setEventData(JSONArray data) {
        String dataString = data.toString(4);

        try {
            URL url = new URL("https://soopymc.my.to/api/sbgDiscord/setEventData.json?key=HoVoiuWfpdAjJhfTj0YN");

            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);

            byte[] out = dataString.getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }

        } catch (IOException e) {
            main.logger.warning(main.getLangUtil().beautifyStackTrace(e.getStackTrace(), e));
        }
    }

    public TaxPayer getTaxPayer(Player player) {
        JSONObject taxData = getTaxData();
        JSONObject playerJson;
        try {
            playerJson = taxData.getJSONObject("guilds").getJSONObject(player.getGuildId()).getJSONObject("members").getJSONObject(player.getUUID());
        } catch (Exception e) {
            playerJson = null;
        }
        return new TaxPayer(player.getUUID(), player.getDisplayName(), player.getGuildId(), playerJson, main);
    }

    public double getTotalCoinsInProfile(String profileUUID) {
        String response = main.getApiUtil().getResponse(main.getApiUtil().BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID, 600000);
        if (response == null) return 0;
        JSONObject jsonObject = new JSONObject(response);

        if (!jsonObject.getBoolean("success")) {
            main.logger.warning("API REQ FAILED: " + jsonObject.getString("cause"));
            return 0;
        }

        double totalMoney = 0;
        try {
            totalMoney += jsonObject.getJSONObject("profile").getJSONObject("banking").getLong("balance");
        } catch (JSONException ignore) {
        }
        try {
            for (String profMemberUuid : jsonObject.getJSONObject("profile").getJSONObject("members").keySet()) {
                try {
                    totalMoney += jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(profMemberUuid).getLong("coin_purse");
                } catch (JSONException ignore) {
                }
            }
        } catch (JSONException ignore) {

        }
        return totalMoney;
    }

    public double getTotalCoinsInPlayer(String playerUUID) {
        double totalCoins = 0;
        for (String profile : getPlayerFromUUID(playerUUID).getSkyblockProfiles()) {
            totalCoins += getTotalCoinsInProfile(profile);
        }
        return totalCoins;
    }

    public SkyblockProfile getSkyblockProfileByProfileUUID(String profileUUID) {
        String response = main.getApiUtil().getResponse(main.getApiUtil().BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID, 600000);
        if (response == null) return new SkyblockProfile();
        JSONObject jsonObject = new JSONObject(response);

        List<Player> members = jsonObject.getJSONObject("profile").getJSONObject("members").keySet().stream().map(this::getPlayerFromUUID).collect(Collectors.toList());
        List<BankTransaction> transactions = new ArrayList<>();
        double balance;
        try {
            jsonObject.getJSONObject("profile").getJSONObject("banking").getJSONArray("transactions").forEach(transaction -> {
                if (transaction instanceof JSONObject) transactions.add(new BankTransaction((JSONObject) transaction));
            });
            balance = jsonObject.getJSONObject("profile").getJSONObject("banking").getDouble("balance");
        } catch (JSONException e) { // Banking API off
            return new SkyblockProfile(members, new ArrayList<>(), 0);
        }
        return new SkyblockProfile(members, transactions, balance);
    }

    public byte[] getEiffelTowerImage() {
        URL url = null;
        try {
            url = new URL("https://upload.wikimedia.org/wikipedia/commons/thumb/9/97/Construction_tour_eiffel.JPG/334px-Construction_tour_eiffel.JPG");
        } catch (MalformedURLException e) {
            return null;
        }
        try {
            InputStream is = url.openConnection().getInputStream();
            byte[] bytes = is.readAllBytes();
            is.close();
            return bytes;
        } catch (IOException exception) {
            return null;
        }
    }
}
