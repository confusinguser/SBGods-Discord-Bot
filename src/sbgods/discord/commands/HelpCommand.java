package sbgods.discord.commands;

import java.awt.Color;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sbgods.SBGods;
import sbgods.discord.DiscordBot;

public class HelpCommand extends ListenerAdapter{
	SBGods main;
	DiscordBot discord;
	String name;

	public HelpCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = discord.commandPrefix + "help";
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !e.getMessage().getContentRaw().startsWith(this.name)) {
			return;
		}

		System.out.println(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		String[] args = e.getMessage().getContentRaw().split(" ");

		if (args.length == 1) {
			EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("SBGods Discord Bot Help Page");
			Random colorRandom = new Random();
			embedBuilder = embedBuilder.setDescription(embedBuilder.getDescriptionBuilder()
					.append(discord.commandPrefix + "slayer leaderboard [length / all]: \t**Shows slayer XP leaderboard**\n")
					.append(discord.commandPrefix + "slayer player <IGN>: \t**Shows a specific player's slayer XP**\n")
					.append(discord.commandPrefix + "skill leaderboard [length / all]:  \t**Shows average skill level leaderboard**\n")
					.append(discord.commandPrefix + "skill player <IGN>: \t**Shows a specific player's average skill level**").toString())
					.setColor(new Color(colorRandom.nextFloat(), colorRandom.nextFloat(), colorRandom.nextFloat()));

			e.getChannel().sendMessage(embedBuilder.build()).queue();
			return;
		}
	}
}