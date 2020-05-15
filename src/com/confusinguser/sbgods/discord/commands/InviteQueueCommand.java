/*package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.Applicant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Objects;

public class InviteQueueCommand extends Command {

	public InviteQueueCommand(SBGods main, DiscordBot discord) {
		this.main = main;
		this.discord = discord;
		this.name = "invitequeue";
		this.usage = this.getName();
		this.aliases = new String[] {"iq"};
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e) || !Objects.requireNonNull(e.getMember()).hasPermission(Permission.MANAGE_ROLES)) {
			return;
		}

		main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

		EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Invite Queue");
		
		ArrayList<Applicant> applications = main.getApplicationUtil().getApplicationList();
		for (int i = 0; i < applications.size(); i++) {
			embedBuilder.appendDescription("**" + Math.incrementExact(i) + ". **" + applications.get(i).getSkyblockPlayer().getDisplayName());
			
			// If discord isn't linked on hypixel OR it's outdated / doesn't exist
			if (applications.get(i).getSkyblockPlayer().getDiscordTag() == null || discord.getJDA().getUserByTag(applications.get(i).getSkyblockPlayer().getDiscordTag()) == null) {
				embedBuilder.appendDescription("\n");
			} else {
				embedBuilder.appendDescription(": " + applications.get(i).getDiscordUser().getAsMention() + '\n');
			}
		}
		
		if (applications.size() == 0) embedBuilder.setDescription("All chaught up! No more people to invite!");
		e.getChannel().sendMessage(embedBuilder.build()).queue();
	}
}*/