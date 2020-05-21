package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VerifyAllCommand extends Command implements EventListener {

    public VerifyAllCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "verifyall";
        this.aliases = new String[]{"vall"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (e.getMember() != null && !e.getMember().hasPermission(Permission.MANAGE_ROLES)) {
            e.getChannel().sendMessage("You do not have permission to perform this command").queue();
            return;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("reset")) {
            if (args.length >= 3 && (args[2].equalsIgnoreCase("v") || args[2].equalsIgnoreCase("verified"))) {
                for (Member member : e.getGuild().getMembers()) {
                    for (Role role : e.getGuild().getRolesByName("verified", true)) {
                        try {
                            e.getGuild().removeRoleFromMember(member, role).queue();
                        } catch (HierarchyException ignored) {
                        }
                    }
                }
                e.getChannel().sendMessage("Removed everyone's verified roles!").queue();
                return;
            }

            for (Member member : e.getGuild().getMembers()) {
                for (Role role : member.getRoles().stream().filter(role -> HypixelGuild.getGuildByName(role.getName()) != null).collect(Collectors.toList())) {
                    try {
                        e.getGuild().removeRoleFromMember(member, role).complete();
                    } catch (HierarchyException ignored) {
                    }
                }
                for (Role role : e.getGuild().getRolesByName("verified", true)) {
                    try {
                        e.getGuild().removeRoleFromMember(member, role).complete();
                    } catch (HierarchyException ignored) {
                    }
                }

            }
            e.getChannel().sendMessage("Removed everyone's verified and guild roles!").queue();
            return;
        }
        verifyAll(e.getChannel(), e.getGuild()); // TODO - do this with all other commands
    }

    private void verifyAll(MessageChannel channel, Guild discord) {
        String messageId = channel.sendMessage("Attempting to auto-verify all players! (" + main.getLangUtil().getProgressBar(0.0, 30) + ")").complete().getId();

        int playersVerified = 0;
        int i = 0; //for loading animation
        for (Member member : discord.getMembers()) {
            i++;
            channel.editMessageById(messageId, "Attempting to auto-verify all players! (" + main.getLangUtil().getProgressBar(i / (double) discord.getMembers().size(), 30) + ")").queue();
            String mcName = main.getApiUtil().getMcNameFromDisc(member.getUser().getAsTag());
            if (!mcName.equals("")) {
                playersVerified++;
                main.getUtil().verifyPlayer(member, mcName, discord, channel);
            }
        }

        channel.editMessageById(messageId, playersVerified == 0 ? "Did not verify any players" : "Verified a total of " + playersVerified + (playersVerified == 1 ? " player!" : " players!")).queue();

        main.getUtil().scheduleCommandAfter(() ->
                channel.getHistoryAfter(messageId, 100).complete().getRetrievedHistory().stream()
                        .filter(message -> message.getContentRaw().startsWith("[VerifyAll]") // Get all messages that start with [VerifyAll]
                                && discord.getJDA().getSelfUser().getId().equals(message.getAuthor().getId()))
                        .forEach(message -> message.delete().queue()), 10, TimeUnit.SECONDS);
    }

    public void verifyAll(TextChannel textChannel) {
        if (textChannel == null) {
            return;
        }
        verifyAll(textChannel, textChannel.getGuild());
    }
}
