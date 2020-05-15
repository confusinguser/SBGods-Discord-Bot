/* NOT USED
package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.TaxPayer;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ConfigValues {

	SBGods main;
	private GuildChannel dataChannel = main.getDiscord().getJDA().getGuildChannelById("697710664285945946");
	private List<TaxPayer> taxPayers = new ArrayList<>();
	private JSONObject settingsConfig;
	private long lastTaxMillis = 0;

	public ConfigValues(SBGods main) {
		this.main = main;
	}

	public void loadConfig() {
		boolean foundMessage = false;
		for (Message message : dataChannel.getGuild().getHistory().getRetrievedHistory()) {
			try {
				settingsConfig = new JSONObject(new String(Base64.getDecoder().decode(message.getContentRaw())));
				foundMessage = true;
				break;
			} catch (JSONException | IllegalArgumentException e) {
				if (!message.getAuthor().isBot()) {
					message.delete().queue();
				}
			}
		}
		if (settingsConfig.has("lastTaxMillis")) {
			lastTaxMillis = settingsConfig.getLong("lastTaxMillis");
		}

		if (!foundMessage) {
			System.out.println("No config found");
		}
	}

	public void saveConfig() {
		settingsConfig = new JSONObject();
		settingsConfig.append("lastTaxMillis", lastTaxMillis);
		boolean foundMessage = false;
		try {
			for (Message message : dataChannel.getHistory().getRetrievedHistory()) {
				if (message.getAuthor().getId().contentEquals(main.getDiscord().getJDA().getSelfUser().getId())) {
					message.editMessage(new String(Base64.getEncoder().encode(settingsConfig.toString().getBytes()))).queue();
					foundMessage = true;
					break;
				}
			}
		} catch (NullPointerException e) {;}
		if (!foundMessage) {
			dataChannel.sendMessage(new String(Base64.getEncoder().encode(settingsConfig.toString().getBytes()))).queue();
		}
	}

	public List<TaxPayer> getTaxPayers() {
		return taxPayers;
	}

	public long getLastTaxMillis() {
		return lastTaxMillis;
	}

	public void setLastTaxMillis(long lastTaxMillis) {
		this.lastTaxMillis = lastTaxMillis;
	}

}
*/