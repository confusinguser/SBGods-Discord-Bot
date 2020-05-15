/*package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.Applicant;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.SkyblockPlayer;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ApplyCommand extends Command {

	public ApplyCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = "apply";
		this.usage = this.getName() + " <IGN> <SBG / SBDG>";
		this.aliases = new String[] {};
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e)) {
			return;
		}

		main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		String[] args = e.getMessage().getContentRaw().split(" ");

		if (args.length < 3) {
			e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
			return;
		}

		HypixelGuild guild = HypixelGuild.getEnum(args[2]);
		if (guild == null) {
			e.getChannel().sendMessage("You can only apply for the guilds SBG and SBDG! Usage: `" + this.usage + "`").queue();
			return;
		}

		SkyblockPlayer thePlayer = main.getApiUtil().getSkyblockPlayerFromUsername(args[1]);
		if (thePlayer.getDiscordTag() == null) {
			e.getChannel().sendMessage("You need to set your discord on Hypixel to apply").queue();
			return;
		}
		if (!thePlayer.getDiscordTag().contentEquals(e.getAuthor().getAsTag())) {
			e.getChannel().sendMessage("Failed to apply! Your Discord on Hypixel is not the same as this").queue();
			return;
		}

		Applicant applicant = new Applicant(thePlayer, e.getAuthor(), guild);

		if (!applicant.meetsRequirements()) {
			e.getChannel().sendMessage(new StringBuilder("You do not meet the requirements, current requirements are:\n```diff\n")
					.append("- Total slayer exp of ")
					.append(main.getLangUtil().prettifyInt(guild.getSlayerExpRec()) + '\n')
					.append("- Average skill level of " + main.getLangUtil().prettifyDouble(main.getUtil().round(main.getSBUtil().toSkillLevel(guild.getSkillExpRec()), 1)) + "```")
			.toString());
			return;
		}

		main.getApplicationUtil().addApplicant(applicant);
		e.getChannel().sendMessage("Success! A staff member will be in touch with you shortly").queue();
	}
}
*/