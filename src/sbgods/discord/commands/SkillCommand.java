package sbgods.discord.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import sbgods.SBGods;
import sbgods.discord.DiscordBot;

public class SkillCommand extends Command implements EventListener {

	private HashMap<String, Double> usernameAverageSkillLevel = new HashMap<String, Double>();

	public SkillCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = discord.commandPrefix + "skill";
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !e.getMessage().getContentRaw().startsWith(this.name)) {
			return;
		}

		System.out.println(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		String[] args = e.getMessage().getContentStripped().split(" ");

		if (args.length == 1) {
			e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
			return;
		}

		if (args[1].equalsIgnoreCase("leaderboard")) {
			if (!discord.isLeaderboardChannel(e)) {
				if (discord.getJDA().getTextChannelById(discord.leaderboard_channel_id) == null) {
					e.getChannel().sendMessage("Leaderboard commands can only be ran in a channel I don't have access to").queue();
				} else {
					e.getChannel().sendMessage("Leaderboard commands can only be ran in " + discord.getJDA().getTextChannelById(discord.leaderboard_channel_id).getAsMention()).queue();
				}
				return;
			}

			String messageId = e.getChannel().sendMessage("...").complete().getId();
			if (discord.getLastLeaderboardMessage() != null && e.getMessage().getContentRaw() == discord.getLastLeaderboardMessage().getContentRaw() && e.getMessage().getTimeCreated().getDayOfMonth() == discord.getLastLeaderboardMessage().getTimeCreated().getDayOfMonth() && Math.subtractExact(e.getMessage().getTimeCreated().getHour(), discord.getLastLeaderboardMessage().getTimeCreated().getHour()) < 2 && Math.subtractExact(e.getMessage().getTimeCreated().getMinute(), discord.getLastLeaderboardMessage().getTimeCreated().getMinute()) < 5) {
				e.getChannel().editMessageById(messageId, "Can't you even scroll up two messages you f\\*\\*\\*ing casual?\nYou know, it takes a lot of effort to give you want you call a \"leaderboard\". So show some F\\*\\*\\*ing respect " + e.getAuthor().getName() + ".");
				return;
			}
			discord.setLastLeaderboardMessage(e.getMessage());
			ArrayList<String> guildMemberUuids = main.getApiUtil().getGuildMembers();

			int topX;
			if (args.length > 2) {
				if (args[2].equalsIgnoreCase("all")) {
					topX = guildMemberUuids.size();
				} else {
					try {
						topX = Math.min(guildMemberUuids.size(), Integer.parseInt(args[2]));
					} catch (NumberFormatException exception) {
						e.getChannel().editMessageById(messageId, "**" + args[2] + "** is not a valid number!").queue();
						return;
					}
				}
			} else {
				topX = 10;
			}

			e.getChannel().editMessageById(messageId, "Getting everyone's skyblock skill levels (0 / " + guildMemberUuids.size() + ")").queue();
//
			for (int i = 0; i < guildMemberUuids.size(); i++) {
				String UUID = guildMemberUuids.get(i);
				ArrayList<String> profiles = main.getApiUtil().getSkyblockProfilesAndUsernameFromUUID(UUID);
				e.getChannel().editMessageById(messageId, "Getting everyone's skyblock skill levels (" + i + " / " + guildMemberUuids.size() + ")  (" + profiles.get(0) + " [" + Math.decrementExact(profiles.size()) + "]" + ")").queue();

				double highestAverageSkillLevel = 0;
				// Get avg. skill level the profile that has the highest
				for (int j = 1; j < profiles.size(); j++) {
					String profile = profiles.get(j);
					ArrayList<Double> skillLevels = new ArrayList<Double>();
					for (Integer skill : main.getApiUtil().getProfileSkills(profile, UUID)) {
						skillLevels.add(main.getUtil().toSkillLevel(skill));
					}
					highestAverageSkillLevel = Math.max(highestAverageSkillLevel, main.getUtil().getAverage(skillLevels));
				}

				usernameAverageSkillLevel.put(profiles.get(0), highestAverageSkillLevel);
			}
//
			StringBuilder response = new StringBuilder("**Average skill level Leaderboard:**\n");
			if (args.length > 2) {
				response.append("\n");
			} else {
				response.append("*Tip: " + this.name + " leaderboard [length / all]*\n\n");
			}
			for (int i = 0; i < topX; i++) {
				Entry<String, Double> currentEntry = main.getUtil().getHighestKeyValuePair(usernameAverageSkillLevel, true);
				usernameAverageSkillLevel.remove(currentEntry.getKey());
				if (currentEntry.getValue().doubleValue() == 0) {
					response.append("**#" + Math.addExact(i, 1) + "** *" + currentEntry.getKey() + ":* " + "**Skills API is turned off**" + "\n");
				} else {
					response.append("**#" + Math.addExact(i, 1) + "** *" + currentEntry.getKey() + ":* " + main.getUtil().round(currentEntry.getValue().doubleValue(), 2) + "\n");
				}
				if (i != topX - 1 || currentEntry.getValue().doubleValue() != 0) {
					response.append("\n");
				}
			}

			String responseString = response.toString();
			// Split the message every 2000 characters in a nice looking way because of discord limitations
			ArrayList<String> responseList = main.getUtil().processMessageForDiscord(responseString, 2000);

			System.out.println("Done, sending leaderboard to Discord\nMessage length: " + response.toString().length() + "\nNumber of messages: " + responseList.size());

			e.getChannel().deleteMessageById(messageId).queue();
			for (int i = 0; i < responseList.size(); i++) {
				String message = responseList.get(i);
				if (i == 0) {
					e.getChannel().sendMessage(message).complete();
				} else {
					e.getChannel().sendMessage("\u200E" + message).complete();
				}
			}
			return;
		}

		if (args[1].equalsIgnoreCase("player")) {
			String messageId = e.getChannel().sendMessage("...").complete().getId();

			if (args.length >= 3) {
				ArrayList<String> profiles = main.getApiUtil().getSkyblockProfilesAndUUIDAndDisplaynameFromUsername(args[2]);

				if (profiles.isEmpty()) {
					e.getChannel().editMessageById(messageId, "Player **" + args[2] + "** does not exist").queue();
					return;
				}

				double highestAverageSkillLevel = 0;
				for (int j = 2; j < profiles.size(); j++) {
					String profile = profiles.get(j);
					ArrayList<Double> skillLevels = new ArrayList<Double>();
					for (Integer skill : main.getApiUtil().getProfileSkills(profile, profiles.get(1))) {
						skillLevels.add(main.getUtil().toSkillLevel(skill));
					}
					highestAverageSkillLevel = Math.max(highestAverageSkillLevel, main.getUtil().getAverage(skillLevels));
				}

				if (highestAverageSkillLevel == 0) {
					e.getChannel().editMessageById(messageId, "Player **" + profiles.get(0) + "** does not have skill API on").queue();
					return;
				}

				e.getChannel().editMessageById(messageId, "Player **" + profiles.get(0) + "** has an average skill level of **" + highestAverageSkillLevel + "**").queue();
				return;
			} else {
				e.getChannel().editMessageById(messageId, "Invalid usage! Usage: *" + this.name + " player <IGN>*").queue();
				return;
			}
		}

		e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
	}

	public void setAvgSkillLevelHashMap(HashMap<String, Double> input) {
		usernameAverageSkillLevel = input;
	}
}