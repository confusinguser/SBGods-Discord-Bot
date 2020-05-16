package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.EventListener;

public class VerifyAllCommand extends Command implements EventListener {

    public VerifyAllCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "verifyall";
        this.aliases = new String[]{"vall"};
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e)) {
            return;
        }

        DiscordServer currentDiscordServer = DiscordServer.getDiscordServerFromEvent(e);
        if (currentDiscordServer == null) {
            return;
        }

//        if (currentDiscordServer.getChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getChannelId())) {
//            e.getChannel().sendMessage("Verify commands cannot be ran in this channel!").queue();
//            return;
//        }

        if(e.getMember() != null && !e.getMember().hasPermission(Permission.MANAGE_ROLES)){
            e.getChannel().sendMessage("You do not have permissions to perform this command").queue();
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        if(e.getMessage().getContentRaw().equalsIgnoreCase("-vall reset")){
            for(Member member : e.getGuild().getMembers()) {
                for (Role role : e.getGuild().getRolesByName("SBG Guild Member",true)) {
                    try {
                        e.getGuild().removeRoleFromMember(member, role).queue();

                    } catch (HierarchyException ignored) {}
                }
                for (Role role : e.getGuild().getRolesByName("SBF Guild Member",true)) {
                    try {
                        e.getGuild().removeRoleFromMember(member, role).queue();

                    } catch (HierarchyException ignored) {}
                }
                for (Role role : e.getGuild().getRolesByName("Verified",true)) {
                    try {
                        e.getGuild().removeRoleFromMember(member, role).complete();

                    } catch (HierarchyException ignored) {}
                }

            }


            e.getChannel().sendMessage("Removed everyones verified and guild roles!").queue();

            return;
        }

        if(e.getMessage().getContentRaw().equalsIgnoreCase("-vall reset v")){
            for(Member member : e.getGuild().getMembers()) {
                for (Role role : e.getGuild().getRolesByName("Verified",true)) {
                    try {
                        main.logger.info("Removed " + member.getUser().getAsTag() + "'s verified role");
                        e.getGuild().removeRoleFromMember(member, role).queue();

                    } catch (HierarchyException ignored) {}
                }

            }


            e.getChannel().sendMessage("Removed everyones verified roles!").queue();

            return;
        }

        e.getChannel().sendMessage("Attempting to auto-verify all players!").queue();

        for(Member member : e.getGuild().getMembers()) {
            main.logger.info("Attempting to auto-verify " + member.getUser().getAsTag());

            String mcName = main.getApiUtil().getMcNameFromDisc(member.getUser().getAsTag().replace("#", "*"));

            if (!mcName.equals("")) {
                // Verify them automaticly!
                main.getUtil().verifyPlayer(member, mcName, e.getGuild(), e.getChannel());
            }
        }


        e.getChannel().sendMessage("Auto-verify all players complete!").queue();
    }
}
