package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class TaxPayer {
    public static final Comparator<TaxPayer> owesComparator = Comparator.comparingInt(TaxPayer::getOwes);
    private final String uuid;
    private final String name;
    private final String guildId;
    private final SBGods main;
    private final JSONObject jsonData;

    public TaxPayer(String uuid, String name, String guildId, JSONObject jsonData, SBGods main) {
        this.uuid = uuid;
        this.name = name;
        this.guildId = guildId;
        this.main = main;

        if (jsonData == null) {
            this.jsonData = new JSONObject();
            this.jsonData.put("owes", 0);
            this.jsonData.put("role", "Default");
            this.jsonData.put("name", name);
        } else {
            this.jsonData = jsonData;
        }
    }

    public String getUuid() {
        return uuid;
    }

    public String getGuildId() {
        return guildId;
    }

    public int getOwes() {
        return jsonData.getInt("owes");
    }

    public void setOwes(int amount) {
        jsonData.remove("owes");
        jsonData.put("owes", amount);
    }

    public void addOwes(int amount) {
        setOwes(getOwes() + amount);
    }

    public String getRole() {
        return jsonData.getString("role");
    }

    public void setRole(String role) {
        jsonData.remove("role");
        jsonData.put("role", role);
    }

    public String getName() {
        return jsonData.getString("name");
    }

    public JSONObject getJSON() {
        return jsonData;
    }

    public void sendDataToServer() {
        JSONObject taxData = main.getApiUtil().getTaxData();

        try {
            taxData.getJSONObject("guilds").getJSONObject(guildId).getJSONObject("members").remove(uuid);
        } catch (JSONException ignore) {
        }

        taxData.getJSONObject("guilds").getJSONObject(guildId).getJSONObject("members").put(uuid, jsonData);

        main.getApiUtil().setTaxData(taxData);
    }

    public EmbedBuilder getDiscordEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0xb8300b).setTitle(getName() + "'s Tax Status");

        embedBuilder.appendDescription("Role: **" + getRole() + "**\n");
        embedBuilder.appendDescription("Owes: **" + getOwes() + "**");

        return embedBuilder;
    }
}
