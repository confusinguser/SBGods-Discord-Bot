package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ApiUtil {

    private final String BASE_URL = "https://api.hypixel.net/";
    private final String USER_AGENT = "Mozilla/5.0";
    private final SBGods main;
    private int REQUEST_RATE = 0; // unit: requests
    private long LAST_CHECK = System.currentTimeMillis();
    private int allowance = REQUEST_RATE; // unit: requests
    private int fails = 0;

    public ApiUtil(SBGods main) {
        this.main = main;
        REQUEST_RATE = 30 * main.keys.length;
    }

    public String getResponse(String url_string, int cacheTime) {

        // See if request already in cache
        Response cacheResponse = main.getCacheUtil().getCachedResponse(main.getCacheUtil().stripUnnecesaryInfo(url_string));
        String cacheResponseStr = cacheResponse.getJson();
        long current = System.currentTimeMillis();
        if (cacheResponseStr != null && current - cacheResponse.getTimeStamp() < cacheTime) {
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
            return getResponse(url_string, cacheTime);

        } else if (responseCode == 403) {
            main.logger.severe("The API key \"" + main.getCurrentApiKey() + "\" is invalid!");
            System.exit(-1);
        } else if (ioException != null) {
            fails++;
            if (fails % 10 == 0 || fails == 1) {
                main.logger.warning("Failed to connect to the Hypixel API " + fails + " times, this may be a problem: " + ioException.toString() + '\n' + ioException.fillInStackTrace());
            }
            return getResponse(url_string, cacheTime);
        }
        fails = 0;
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
            return getNonHypixelResponse(url_string);

        } else if (responseCode == 403) {
            main.logger.severe("The API key \"" + main.getCurrentApiKey() + "\" is invalid!");
            System.exit(-1);
        } else if (ioException != null) {
            fails++;
            if (fails % 10 == 0 || fails == 1) {
                main.logger.warning("Failed to connect to the API " + fails + " times, this may be a problem: " + ioException.toString() + '\n' + ioException.fillInStackTrace());
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

        for (int i = 0; i < members.length(); i++) {
            JSONObject currentMember = members.getJSONObject(i);
            String uuid = currentMember.getString("uuid");
            output.add(new Player(uuid, null, null, false, null, main));
        }

        return output;
    }

    public Player getPlayerFromUUID(String UUID) {
        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&uuid=" + UUID, 300000);
        if (response == null) return new Player(main);

        JSONObject jsonObject = new JSONObject(response);
        String username;
        String uuid;
        String discord;
        boolean online;
        JSONObject profiles;
        try {
            uuid = jsonObject.getJSONObject("player").getString("uuid");
            username = jsonObject.getJSONObject("player").getString("displayname");
            profiles = jsonObject.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles");
            online = jsonObject.getJSONObject("player").getLong("lastLogin") > jsonObject.getJSONObject("player").getLong("lastLogout");
        } catch (JSONException e) {
            return new Player(main);
        }
        try {
            // Does not necessarily exist while the other things above have to.
            discord = jsonObject.getJSONObject("player").getJSONObject("socialMedia").getJSONObject("links").getString("DISCORD");
            setDiscNameFromMc(username, discord);
        } catch (JSONException e) {
            discord = "";
        }

        return new Player(uuid, username, discord, online, new ArrayList<>(profiles.keySet()), main);
    }

    public Player getPlayerFromUsername(String name) {
        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&name=" + name, 300000);
        if (response == null) return new Player(main);

        JSONObject jsonObject = new JSONObject(response);
        String username;
        String uuid;
        String discord;
        boolean online;
        JSONObject profiles;
        try {
            uuid = jsonObject.getJSONObject("player").getString("uuid");
            username = jsonObject.getJSONObject("player").getString("displayname");
            profiles = jsonObject.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles");
            online = jsonObject.getJSONObject("player").getLong("lastLogin") > jsonObject.getJSONObject("player").getLong("lastLogout");
        } catch (JSONException e) {
            return new Player(main);
        }
        try {
            // Does not necessarily exist while the other things above have to.
            discord = jsonObject.getJSONObject("player").getJSONObject("socialMedia").getJSONObject("links").getString("DISCORD");
            setDiscNameFromMc(username, discord);
        } catch (JSONException e) {
            discord = "";
        }

        return new Player(uuid, username, discord, online, new ArrayList<>(profiles.keySet()), main);
    }


    public SlayerExp getPlayerSlayerExp(String playerUUID) {
        Map<String, Integer> output = new HashMap<>();
        for (String slayer_type : Constants.slayer_types) {
            output.put(slayer_type, 0);
        }

        Player thePlayer = getPlayerFromUUID(playerUUID);

        int cacheTime = 60000;

        if (!thePlayer.getIsOnline()) {
            cacheTime = 3600000; //1h
        }

        for (String profileUUID : thePlayer.getSkyblockProfiles()) {

            String response = getResponse(BASE_URL + "skyblock/profile" + "?key=" + main.getNextApiKey() + "&profile=" + profileUUID, cacheTime);
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
            } catch (JSONException e) {
                skillMap.put(skill_type, 0);
            }
        }
        return new SkillLevels(skillMap, true);
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

    public SkillLevels getBestPlayerSkillLevels(String uuid) {
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

    public PlayerAH getPlayerAHFromUsername(String player, String messageId, MessageReceivedEvent e) {
        //main.logger.info("Loading " + player + "'s main data");
        e.getChannel().editMessageById(messageId, "Loading..").queue();
        e.getChannel().sendTyping().queue();
        String response = getResponse(BASE_URL + "player" + "?key=" + main.getNextApiKey() + "&name=" + player, 300000);
        if (response == null)
            return new PlayerAH("There was a error fetching that user's auctions, please try again later.");

        JSONObject jsonObject = new JSONObject(response);
        JSONObject playerData = jsonObject;
        String playerId = playerData.getJSONObject("player").getString("_id");

        Iterator<String> playerProfiles = playerData.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles").keys();

        int len = 0;
        AHItem[] items = new AHItem[14];

        //main.logger.info("Loaded " + player + "'s main data");

        e.getChannel().editMessageById(messageId, "Loading...").queue();
        e.getChannel().sendTyping().queue();

        int amountLoops = playerData.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles").length();
        int loops = 0;

        while (playerProfiles.hasNext()) {
            String profileId = playerProfiles.next();

            //main.logger.info("Loading " + player + "'s " + playerData.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles").getJSONObject(profileId).getString("cute_name") + " data");


            response = getResponse(BASE_URL + "skyblock/auction" + "?key=" + main.getNextApiKey() + "&profile=" + profileId + "&uuid=" + playerId, 60000);
            if (response == null)
                return new PlayerAH("There was a error fetching that user's auctions, please try again later.");

            jsonObject = new JSONObject(response);
            JSONArray auctionJSONArray;


            //main.logger.info("Loaded " + player + "'s " + playerData.getJSONObject("player").getJSONObject("stats").getJSONObject("SkyBlock").getJSONObject("profiles").getJSONObject(profileId).getString("cute_name") + " data");

            loops++;
            e.getChannel().editMessageById(messageId, "Loading...  (" + loops + "/" + amountLoops + ")").complete();
            e.getChannel().sendTyping().queue();

            try {
                auctionJSONArray = jsonObject.getJSONArray("auctions");
            } catch (JSONException error) {
                return new PlayerAH("There was a error fetching that user's auctions, please try again later.");
            }

            for (int i = 0; i < auctionJSONArray.length(); i++) {
                JSONObject obj = auctionJSONArray.getJSONObject(i);

                if (!obj.getBoolean("claimed")) {
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
        String response = getNonHypixelResponse("http://soopymc.my.to/api/sbgDiscord/setDiscordMcName.json?key=HoVoiuWfpdAjJhfTj0YN&disc=" + discordName.replace("#", "*").replace(" ", "%20") + "&mc=" + mcName);
    }

    public Path downloadFile(String urlStr, String fileLocation) throws IOException {
        File file = new File(fileLocation);
        if (!file.createNewFile()) {
            return downloadFile(urlStr, fileLocation.replace(".jar", "") + "_new.jar");
        }
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoInput(true);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Authorization", "token f159901613c898cb80cdc39b3d8f89d2eb9f51bb");

        BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = bis.readAllBytes();
        fos.write(buffer, 0, buffer.length);
        fos.flush();
        fos.close();
        bis.close();
        return Paths.get(fileLocation);
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
        IOException ioException = null;

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
}
