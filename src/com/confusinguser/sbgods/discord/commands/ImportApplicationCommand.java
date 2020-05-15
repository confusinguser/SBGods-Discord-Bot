/*package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.Applicant;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.SkyblockPlayer;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class ImportApplicationCommand extends Command {

	public ImportApplicationCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = "importapplication";
		this.usage = this.getName() + " <IGN> <SBG / SBDG>";
		this.aliases = new String[] {"ia"};
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e) || !Objects.requireNonNull(e.getMember()).hasPermission(Permission.MANAGE_ROLES)) {
			return;
		}

		main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		String[] args = e.getMessage().getContentRaw().split(" ");

		if (args.length < 3) {
			e.getChannel().sendMessage("Invalid usage! Usage: " + this.usage).queue();
			return;
		}
		
		String messageId = e.getChannel().sendMessage("...").complete().getId();
		
		HypixelGuild guild = HypixelGuild.getEnum(args[2]);
		if (guild == null) {
			e.getChannel().editMessageById(messageId, "You can only import applications for SBG and SBDG! Usage: `" + this.usage + "`").queue();
			return;
		}
		
		SkyblockPlayer thePlayer = main.getApiUtil().getSkyblockPlayerFromUsername(args[1]);
		Applicant applicant = new Applicant(thePlayer, e.getAuthor(), guild);
		
		if (!applicant.meetsRequirements()) {
			e.getChannel().sendMessage("This player cannot be invited because they do not meet requirements").queue();
			return;
		}
		
		main.getApplicationUtil().addApplicant(applicant);
		e.getChannel().editMessageById(messageId, "**Success!** The application for **" + thePlayer.getDisplayName() + "** has been registered").queue();
	}
}
*/