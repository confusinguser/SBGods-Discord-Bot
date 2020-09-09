package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuildDiscListCommand extends Command {

    public GuildDiscListCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "gdisclist";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (senderMember.hasPermission(Permission.MANAGE_ROLES)) {
            e.getChannel().sendMessage("You don't have permission to perform this command").queue();
            return;
        }

        String messageId = e.getChannel().sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0.0, 30) + ")").complete().getId();
        int i = 0;
        List<Player> guildMembers = main.getApiUtil().getGuildMembers(currentDiscordServer.getHypixelGuild());

        StringBuilder message = new StringBuilder();

        for (Player guildMember : guildMembers) {
            if (i++ % 5 == 0) {
                e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar((double) i / (double) guildMembers.size(), 30) + ")").queue();
            }

            Player player = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());

            if (player.getDiscordTag().equals("")) {
                message.append(player.getDisplayName()).append(": Not linked to discord.\n");
            } else {
                User discUser = main.getDiscord().getJDA().getUserByTag(player.getDiscordTag());
                if (discUser == null) {
                    message.append(player.getDisplayName()).append(": ");
                    message.append(" Not in discord server\n");
                } else {
                    message.append(player.getDisplayName()).append(": ").append(discUser.getAsMention());
                    if (!discUser.getMutualGuilds().contains(main.getDiscord().getJDA().getGuildById(currentDiscordServer.getServerId()))) {
                        message.append(" (Not in discord server)\n");
                    } else {
                        message.append("\n");
                    }
                }
            }
        }

        e.getChannel().deleteMessageById(messageId).queue();
        List<String> sendMessage = main.getUtil().processMessageForDiscord(message.toString(), 2000);

        for (String messagePart : sendMessage) {
            e.getChannel().sendMessage(new EmbedBuilder().setDescription(messagePart).build()).queue();
        }
    }

}
