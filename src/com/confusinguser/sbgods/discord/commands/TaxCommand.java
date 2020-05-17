package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class TaxCommand extends Command implements EventListener {

	public TaxCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = "tax";
		this.aliases = new String[]{};
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e)) {
			return;
		}

		main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		String[] args = e.getMessage().getContentRaw().split(" ");

		DiscordServer currentDiscordServer = DiscordServer.getDiscordServerFromEvent(e);

		if (args.length <= 1) {
			e.getChannel().sendMessage("Invalid argument! Valid arguments: `paid`, `paidall`, `owe`, `oweall`, `owelist`!").queue();
			return;
		}
		if (args[1].equalsIgnoreCase("paid")) {
			if (args.length <= 2) {
				e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " paid <IGN>`!").queue();
				return;
			}

			JSONObject taxData = main.getApiUtil().getTaxData();

			for (String guild : taxData.keySet()) {
				JSONArray guildPlayers = taxData.getJSONArray(guild);
				for (int i = 0; i < guildPlayers.length(); i++) {
					try {
						guildPlayers.getJSONObject(i).getJSONObject("name");
					} catch (JSONException ignored) {}
				}
			}

			return;
		}

		if (args[1].equalsIgnoreCase("paidall")) {
			if (args.length <= 2) {
				e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " paid <IGN>`!").queue();
				return;
			}


			return;
		}

		if (args[1].equalsIgnoreCase("owe")) {
			if (args.length <= 2) {
				e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " paid <IGN>`!").queue();
				return;
			}


			return;
		}

		if (args[1].equalsIgnoreCase("oweall")) {
			if (args.length <= 2) {
				e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " paid <IGN>`!").queue();
				return;
			}


			return;
		}
	}
}