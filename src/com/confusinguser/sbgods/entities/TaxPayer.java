package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class TaxPayer {
    public static Comparator<TaxPayer> owesComparator = new Comparator<TaxPayer>() {
        @Override
        public int compare(TaxPayer tp1, TaxPayer tp2) {
            return (Integer.compare(tp1.getOwes(), tp2.getOwes()));
        }
    };
    private final String uuid;
    private final String name;
    private final String guildId;
    private final SBGods main;
    private JSONObject jsonData;

    public TaxPayer(String uuid, String name, String guildId, JSONObject jsonData, SBGods main) {
        this.uuid = uuid;
        this.name = name;
        this.guildId = guildId;
        this.jsonData = jsonData;
        this.main = main;

        init();
    }

    private void init() {
        if (jsonData == null) {
            jsonData = new JSONObject();
            jsonData.put("owes", 0);
            jsonData.put("role", "default");
            jsonData.put("name", name);
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

    public void sendDataToDiscord(MessageChannel chan) {

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0xb8300b).setTitle(getName() + "'s Tax Status");

        embedBuilder.appendDescription("Role: **" + getRole() + "**\n");
        embedBuilder.appendDescription("Owes: **" + getOwes() + "**");

        chan.sendMessage(embedBuilder.build()).queue();

    }
}
