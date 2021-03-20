package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.*;
import com.confusinguser.sbgods.entities.leaderboard.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
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
        if (cacheResponse != null) {
            return cacheResponse.getJson();
        }
        long current = System.currentTimeMillis();

        // rate limiting
        int timePassed = (int) ((current - LAST_CHECK) / 1000);
        LAST_CHECK = current;
        // unit: seconds
        int PER = 60;
        allowance += timePassed * (REQUEST_RATE / PER);
        if (allowance > REQUEST_RATE) {
            allowance = REQUEST_RATE; // throttle
        }
        while (allowance < 1) {
            try {
                //noinspection BusyWait
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // rate limiting
            int timePassedLoop = (int) ((current - LAST_CHECK) / 1000);
            LAST_CHECK = current;
            allowance += timePassed * (REQUEST_RATE / PER);
            if (allowance > REQUEST_RATE) {
                allowance = REQUEST_RATE; // throttle
            }
        }
        allowance -= 1;


        StringBuffer response;
        HttpURLConnection con = null;
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
            fails = 0;
            main.getCacheUtil().addToCache(main.getCacheUtil().stripUnnecesaryInfo(url_string), response.toString());
            return response.toString();
        } catch (IOException ioException) {

            int responseCode = -1;
            try {
                if (con != null) responseCode = con.getResponseCode();
            } catch (IOException ex) {
                if (onIOException(ioException)) return null;
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
                main.removeApiKey(main.getCurrentApiKey());
                return null;
            } else {
                if (onIOException(ioException)) return null;
                return getResponse(url_string, cacheTime);
            }
        }
    }

    private boolean onIOException(IOException ioException) {
        if (fails > 20) return true;
        fails++;
        if (fails % 10 == 0 || fails == 1) {
            main.logger.warning("Failed to connect to the Hypixel API " + fails + " times: " + ioException);
            main.getDiscord().reportFail(ioException, "ApiUtil getResponse()");
        }
        return false;
    }

    public String getNonHypixelResponse(String url_string) {
        StringBuffer response;
        HttpURLConnection con = null;
        IOException ioException;
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
            fails = 0;
            return response.toString();
        } catch (IOException e) {
            ioException = e;
        }

        int responseCode = -1;
        try {
            if (con != null) responseCode = con.getResponseCode();
        } catch (IOException ex) {
            if (fails > 20) return null;
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
            main.removeApiKey(main.getCurrentApiKey());
//            System.exit(-1);
            return null;
        } else {
            if (fails > 20) return null;
            fails++;
            if (fails % 10 == 0) {
                main.logger.warning("Failed to connect to the API " + fails + " times: " + ioException);
            }
            return getNonHypixelResponse(url_string);
        }
    }

    public void sendData(String dataString, String urlString) {
        try {
            URL url = new URL(urlString);
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
            main.getDiscord().reportFail(e, "Data Sender");
        }
    }

    public List<Player> getGuildMembers(HypixelGuild guild) {
        String response = getResponse(BASE_URL + "guild" + "?key=" + main.getNextApiKey() + "&id=" + guild.getGuildId(), 300000);
        if (response == null) return getGuildMembers(guild);

        List<Player> output = new ArrayList<>();
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

    public List<Player> getGuildMembersDeep(HypixelGuild guild) { // Might be used in the future
        String response = getResponse(BASE_URL + "guild" + "?key=" + main.getNextApiKey() + "&id=" + guild.getGuildId(), 300000);
        if (response == null) return getGuildMembersDeep(guild);

        List<Player> output = new ArrayList<>();
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

    public SlayerExp getPlayerSlayerExp(Player thePlayer) {
        Map<String, Integer> output = new HashMap<>();
        for (String slayer_type : Constants.slayer_types) {
            output.put(slayer_type, 0);
        }

        JSONObject response = new JSONObject(getResponse(BASE_URL + "skyblock/profiles" + "?key=" + main.getNextApiKey() + "&uuid=" + thePlayer.getUUID(), 60000));

        for (int i = 0; i < response.getJSONArray("profiles").length(); i++) {
            JSONObject jsonObject = response.getJSONArray("profiles").getJSONObject(i);


            try {
                jsonObject = jsonObject.getJSONObject("members").getJSONObject(thePlayer.getUUID()).getJSONObject("slayer_bosses");
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
        Map<String, Integer> output = new HashMap<>();
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

        String response = getResponse(BASE_URL + "skyblock/profiles" + "?key=" + main.getNextApiKey() + "&uuid=" + playerUUID, 300000);
        if (response == null) return new SkillLevels();

        JSONObject jsonObject = new JSONObject(response);

        try {
            for (int i = 0; i < jsonObject.getJSONArray("profiles").length(); i++) {
                if (jsonObject.getJSONArray("profiles").getJSONObject(i).getString("profile_id").equalsIgnoreCase(profileUUID)) {
                    jsonObject = jsonObject.getJSONArray("profiles").getJSONObject(i).getJSONObject("members").getJSONObject(playerUUID);
                    break;
                }
            }
        } catch (JSONException e) {
            return new SkillLevels();
        }

        Map<String, Integer> skillMap = new HashMap<>();
        for (String skill_type : Constants.skill_types) {
            try {
                skillMap.put(skill_type, (int) Math.floor(jsonObject.getDouble("experience_skill_" + skill_type)));
            } catch (JSONException e) {
                skillMap.put(skill_type, 0);
            }
        }
        return SkillLevels.fromSkillExp(skillMap, false);
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

        Map<String, Integer> skillMap = new HashMap<>();
        for (String skill_type : Constants.alternate_skill_types) {
            try {
                skillMap.put(main.getSBUtil().alternateToNormalSkillType(skill_type), main.getSBUtil().toSkillExp(jsonObject.getInt("skyblock_" + skill_type)));
                skillMap.put(main.getSBUtil().alternateToNormalSkillType(skill_type), main.getSBUtil().toSkillExp(jsonObject.getInt("skyblock_" + skill_type)));
            } catch (JSONException e) {
                skillMap.put(skill_type, 0);
            }
        }
        return SkillLevels.fromSkillExp(skillMap, true);
    }

    public LeaderboardValues getBestLeaderboardValues(Player thePlayer) {
        SlayerExp slayerExp = getPlayerSlayerExp(thePlayer);
        SkillLevels skillLevels = getBestPlayerSkillLevels(thePlayer);
        DungeonExps dungeonExps = getBestDungeonExpsForPlayer(thePlayer);
        BankBalance bankBalance = getTotalCoinsInPlayer(thePlayer);
        return new LeaderboardValues(slayerExp, bankBalance, skillLevels, dungeonExps);
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

    public String getGuildNameFromId(String ID) {
        if (ID == null) return null;
        String response = getResponse(BASE_URL + "guild" + "?key=" + main.getNextApiKey() + "&id=" + ID, 300000);
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

    public List<Pet> getProfilePets(String profileUUID, String playerUUID) {

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

        List<Pet> output = new ArrayList<>();
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

        Map<String, Integer> output = new HashMap<>();

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

    public Map<String, Integer> getProfileDeaths(String profileUUID, String playerUUID) {

        Map<String, Integer> output = new HashMap<>();

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

    public SkillLevels getBestPlayerSkillLevels(Player thePlayer) {
        String response = getResponse(BASE_URL + "skyblock/profiles" + "?key=" + main.getNextApiKey() + "&uuid=" + thePlayer.getUUID(), 300000);
        SkillLevels highestSkillLevels = new SkillLevels();

        SkillLevels skillLevels;
        for (JsonElement jsonElement : JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("profiles")) {
            JsonObject profile = jsonElement.getAsJsonObject();
            JsonObject member;
            try {
                member = profile.getAsJsonObject("members").getAsJsonObject(thePlayer.getUUID());
            } catch (NullPointerException e) {
                continue;
            }
            Map<String, Integer> skillMap = new HashMap<>();
            for (String skill_type : Constants.skill_types) {
                try {
                    skillMap.put(skill_type, (int) Math.floor(member.get("experience_skill_" + skill_type).getAsDouble()));
                } catch (JSONException | NullPointerException e) {
                    skillMap.put(skill_type, 0);
                }
            }
            skillLevels = SkillLevels.fromSkillExp(skillMap, false);

            if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
                highestSkillLevels = skillLevels;
            }
        }

        if (highestSkillLevels.getAvgSkillLevel() == 0) {
            skillLevels = getProfileSkillsAlternate(thePlayer.getUUID());

            if (highestSkillLevels.getAvgSkillLevel() < skillLevels.getAvgSkillLevel()) {
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

            List<JSONObject> unclaimedAuctionsJson = Util.turnListIntoSubClassList(auctionJSONArray.toList(), JSONObject.class).stream().filter((auction) -> !auction.getBoolean("claimed")).collect(Collectors.toList());
            for (JSONObject unclaimedAuction : unclaimedAuctionsJson) {
                String itemName = unclaimedAuction.getString("item_name");
                String itemTier = unclaimedAuction.getString("tier");
                long startingBid = unclaimedAuction.getLong("starting_bid");
                long highestBid = unclaimedAuction.getLong("highest_bid_amount");
                String category = unclaimedAuction.getString("category");
                long end = unclaimedAuction.getLong("end");
                int bids = unclaimedAuction.getJSONArray("bids").length(); // Number of bids on the item

                items.add(new AhItem(itemName, itemTier, startingBid, highestBid, category, end, bids));
            }
        }
        return new PlayerAH(items.toArray(new AhItem[0]));
    }

    public JSONArray getEventData() {
        return new JSONObject(getNonHypixelResponse("https://soopymc.my.to/api/sbgDiscord/getEventData.json?key=HoVoiuWfpdAjJhfTj0YN")).getJSONArray("data");
    }

    public void setEventData(JSONArray data) {
        String dataString = data.toString(4);
        sendData(dataString, "https://soopymc.my.to/api/sbgDiscord/setEventData.json?key=HoVoiuWfpdAjJhfTj0YN");
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

    public void sendGuildMessageToSApi(String guildMessage) {
        try {
            getNonHypixelResponse("http://soopymc.my.to/api/sbgDiscord/newGuildChatMessage.json?key=HoVoiuWfpdAjJhfTj0YN&message=" + URLEncoder.encode(guildMessage, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {
        }
    }

    public List<JsonObject> getSMPMessageQueue() {
        JsonArray queue = JsonParser.parseString(getNonHypixelResponse("https://soopymc.my.to/api/smp/getMessageQ.json?key=HoVoiuWfpdAjJhfTj0YN")).getAsJsonObject().getAsJsonArray("data");
        List<JsonObject> output = new ArrayList<>();
        for (JsonElement message : queue) {
            output.add(message.getAsJsonObject());
        }
        return output;
    }

    public Path downloadFile(String urlStr, File file) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoInput(true);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
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
            con.setRequestProperty("User-Agent", USER_AGENT);
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
        sendData(dataString, "https://soopymc.my.to/api/sbgDiscord/updateTaxData.json?key=HoVoiuWfpdAjJhfTj0YN");
    }

    public JSONArray getGuildRanksChange() {
        return new JSONObject(getNonHypixelResponse("https://soopymc.my.to/api/sbgDiscord/getGuildRanksChange.json?key=HoVoiuWfpdAjJhfTj0YN")).getJSONArray("data");
    }

    public void setGuildRanksChange(JSONArray data) {
        String dataString = data.toString(4);
        sendData(dataString, "https://soopymc.my.to/api/sbgDiscord/setGuildRanksChange.json?key=HoVoiuWfpdAjJhfTj0YN");
    }

    public TaxPayer getTaxPayer(Player player) {
        JSONObject taxData = getTaxData();
        JSONObject playerJson;
        try {
            playerJson = taxData.getJSONObject("guilds").getJSONObject(player.getGuildId()).getJSONObject("members").getJSONObject(player.getUUID());
        } catch (JSONException e) {
            playerJson = null;
        }
        return new TaxPayer(player.getUUID(), player.getDisplayName(), player.getGuildId(), playerJson, main);
    }

    public BankBalance getTotalCoinsInPlayer(Player thePlayer) {
        double totalCoins = 0;
        String response = main.getApiUtil().getResponse(BASE_URL + "skyblock/profiles" + "?key=" + main.getNextApiKey() + "&uuid=" + thePlayer.getUUID(), 600000);
        JSONObject jsonObject = new JSONObject(response);
        for (Object profile : jsonObject.getJSONArray("profiles").toList()) {
            @SuppressWarnings("unchecked") JSONObject profileJson = new JSONObject((Map<Object, Object>) profile);

            try {
                totalCoins += jsonObject.getJSONObject("profile").getJSONObject("banking").getLong("balance");
            } catch (JSONException ignore) {
            }
            try {
                for (String profMemberUuid : jsonObject.getJSONObject("profile").getJSONObject("members").keySet()) {
                    try {
                        totalCoins += jsonObject.getJSONObject("profile").getJSONObject("members").getJSONObject(profMemberUuid).getLong("coin_purse");
                    } catch (JSONException ignore) {
                    }
                }
            } catch (JSONException ignore) {
            }
        }
        return new BankBalance(totalCoins);
    }

    public List<SkyblockProfile> getSkyblockProfilesByPlayerUUID(String playerUUID) {
        String response = main.getApiUtil().getResponse(BASE_URL + "skyblock/profiles" + "?key=" + main.getNextApiKey() + "&uuid=" + playerUUID, 600000);
        if (response == null) return new ArrayList<>();
        JSONObject jsonObject = new JSONObject(response);

        List<SkyblockProfile> output = new ArrayList<>();
        for (Object profile : jsonObject.getJSONArray("profiles").toList()) {
            @SuppressWarnings("unchecked") JSONObject profileJson = new JSONObject((Map<Object, Object>) profile);

            List<Player> members = profileJson.getJSONObject("members").keySet().stream().map(this::getPlayerFromUUID).collect(Collectors.toList());
            String cuteName = profileJson.getString("cute_name");
            double balance;
            try {
                balance = profileJson.getJSONObject("banking").getDouble("balance");
            } catch (JSONException e) { // Banking API off
                output.add(new SkyblockProfile(members, cuteName, new BankBalance()));
                continue;
            }
            output.add(new SkyblockProfile(members, cuteName, new BankBalance(balance)));
        }
        return output;
    }

    /*public SkyblockProfilePlayer getSkyblockProfilePlayer(String playerUUID, String profileUUID) {
        String response = main.getApiUtil().getResponse(BASE_URL + "skyblock/profiles" + "?key=" + main.getNextApiKey() + "&player=" + playerUUID, 600000);
        if (response == null) return null;
        JSONObject jsonObject = new JSONObject(response);

        for (Object profile : jsonObject.getJSONArray("profiles")) {
            if (profile instanceof JSONObject && ((JSONObject) profile).getString("profile_id").equals(profileUUID)) {
                try {
                    JSONObject member = ((JSONObject) profile).getJSONObject("members").getJSONObject(playerUUID);
                    String inv_contents = member.getJSONObject("inv_contents").getString("data");
                    String talisman_bag = member.getJSONObject("talisman_bag").getString("data");
                    String candy_inventory_contents = member.getJSONObject("candy_inventory_contents").getString("data");
                    String wardrobe_contents = member.getJSONObject("wardrobe_contents").getString("data");
                    //String pets = member.getJSONObject("pets").getString("data");
                } catch (JSONException e) {
                    return null;
                }

            }
        }
        return null;
    }*/

    @SuppressWarnings("unchecked")
    public DungeonExps getBestDungeonExpsForPlayer(Player thePlayer) {
        String response = main.getApiUtil().getResponse(BASE_URL + "skyblock/profiles" + "?key=" + main.getNextApiKey() + "&uuid=" + thePlayer.getUUID(), 600000);
        if (response == null) return null;
        JSONObject jsonObject = new JSONObject(response);

        DungeonExps bestDungeonExps = new DungeonExps();
        for (Object profile : jsonObject.getJSONArray("profiles").toList()) {
            JSONObject profilejson = new JSONObject((Map<Object, Object>) profile);
            Map<String, Double> dungeonLevelsMap = new HashMap<>();
            try {
                try {
                    profilejson = profilejson.getJSONObject("members").getJSONObject(thePlayer.getUUID()).getJSONObject("dungeons");
                } catch (JSONException ex) {
                    continue;
                }
                for (String dungeonClass : Constants.dungeon_classes) {
                    try {
                        dungeonLevelsMap.put(dungeonClass, profilejson.getJSONObject("player_classes").getJSONObject(dungeonClass).getDouble("experience"));
                    } catch (JSONException ex) {
                        dungeonLevelsMap.put(dungeonClass, 0d);
                    }
                }
                for (String dungeon : Constants.dungeons) {
                    try {
                        dungeonLevelsMap.put(dungeon, profilejson.getJSONObject("dungeon_types").getJSONObject(dungeon).getDouble("experience"));
                    } catch (JSONException ex) {
                        dungeonLevelsMap.put(dungeon, 0d);
                    }
                }
            } catch (JSONException ex) {
                main.getDiscord().reportFail(ex, "Dungeon Level Fetcher");
            }
            DungeonExps dungeonExps = new DungeonExps(dungeonLevelsMap);
            if (bestDungeonExps.getAverageDungeonExp() < dungeonExps.getAverageDungeonExp()) {
                bestDungeonExps = dungeonExps;
            }
        }
        return bestDungeonExps;
    }

    /*public int getNetWorth(String playerUUID) {
        int netWorth = 0;
        for (SkyblockProfile profile : getSkyblockProfilesByPlayerUUID(playerUUID)) {
            netWorth += profile.getBalance();
            profile.getMembers().get(1).
        }
    }*/
}
