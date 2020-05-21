package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
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
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer) {

//        if (currentDiscordServer.getBotChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getBotChannelId())) {
//            e.getChannel().sendMessage("Verify commands cannot be ran in this channel!").queue();
//            return;
//        }

        if (e.getMember() != null && !e.getMember().hasPermission(Permission.MANAGE_ROLES)) {
            e.getChannel().sendMessage("You do not have permission to perform this command").queue();
            return;
        }

        String[] args = e.getMessage().getContentRaw().split(" ");

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

        String messageId = e.getChannel().sendMessage("Attempting to auto-verify all players!").complete().getId();

        int playersVerified = 0;
        for (Member member : e.getGuild().getMembers()) {
            // if member doesn't have the verified role, then try to verify them
            if (member.getRoles().stream().filter(role -> role.getName().equalsIgnoreCase("verified")).count() <= 0) {
                String mcName = main.getApiUtil().getMcNameFromDisc(member.getUser().getAsTag());
                if (!mcName.equals("")) {
                    playersVerified++;
                    main.getUtil().verifyPlayer(member, mcName, e.getGuild(), e.getChannel());
                }
            }
        }
        e.getChannel().editMessageById(messageId, playersVerified == 0 ? "Did not verify any players" : "Verified a total of " + playersVerified + (playersVerified == 1 ? " player!" : " players!")).queue();

        main.getUtil().scheduleCommandAfter(() ->
                e.getChannel().getHistoryAfter(messageId, 100).complete().getRetrievedHistory().stream()
                .filter(message -> message.getContentRaw().startsWith("[VerifyAll]") // Get all messages that start with [VerifyAll]
                        && message.getAuthor().isBot())
                .forEach(message -> message.delete().queue()), 10, TimeUnit.SECONDS);
    }
}
