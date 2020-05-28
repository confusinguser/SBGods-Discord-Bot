package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class SbgodsCommand extends Command {

    public SbgodsCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "sbgods";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (args.length == 1 || e.getMember() == null) {
            e.getChannel().sendMessage("Invalid argument! Valid arguments: `version`, `update`, `stop`!").queue();
            return;
        }

        if (args[1].equalsIgnoreCase("version")) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Version " + SBGods.VERSION)
                    .setDescription(
                            "Minor: " + SBGods.VERSION_DESCRIPTION_MINOR +
                                    "\nPatch: " + SBGods.VERSION_DESCRIPTION_PATCH)
                    .setFooter("Made by ConfusingUser#5712 & Soopyboo32#3042");
            e.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        if (args[1].equalsIgnoreCase("test") && currentDiscordServer.equals(DiscordServer.Test)) {

            for (Role role : e.getGuild().getRolesByName("Role name", true)) {
                e.getGuild().addRoleToMember(e.getGuild().getMemberByTag("Player"), role).queue();
            }
            return;
        }

        if (args[1].equalsIgnoreCase("update")) {
            if (e.getMember() != null && e.getMember().getRoles().stream().noneMatch(role -> role.getName().toLowerCase().contains("bot") || main.isDeveloper(e.getAuthor().getId()))) {
                e.getChannel().sendMessage("You do not have the permission to update the bot!").queue();
                return;
            }

            Map.Entry<String, String> latestReleaseUrl = main.getApiUtil().getLatestReleaseUrl();
            if (latestReleaseUrl.getValue().equals("")) {
                e.getChannel().sendMessage("There are no releases available!").queue();
                return;
            }

            String messageId = e.getChannel().sendMessage("Downloading update...").complete().getId();

            File file = main.getUtil().getFileToUse(latestReleaseUrl.getKey());
            Path newFilePath;
            try {
                newFilePath = main.getApiUtil().downloadFile(latestReleaseUrl.getValue(), file);
            } catch (IOException ex) {
                e.getChannel().sendMessage("There was a problem when downloading the latest release").queue();
                main.logger.severe("Exception when downloading the latest release: \n" + main.getLangUtil().beautifyStackTrace(ex.getStackTrace(), ex));
                file.delete();
                return;
            }
            if (!SBGods.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith(".jar")) {
                e.getChannel().sendMessage("Cannot update because bot is running on an IDE").queue();
                return;
            }

            e.getChannel().editMessageById(messageId, "**Success!** Downloaded new version and going to restart bot!").complete();
            try {
                new ProcessBuilder("cmd /c exit").inheritIO().start().waitFor();
                new ProcessBuilder("cmd /c start cmd /k java -jar \"" + newFilePath.toString() + "\"").start().waitFor();
                System.exit(0);
            } catch (IOException | InterruptedException ex) {
                e.getChannel().editMessageById(messageId, "Could not start new version, try stopping the bot").queue();
                return;
            }
            return;
        }

        if (args[1].equalsIgnoreCase("stop")) {
            if (e.getMember() != null && e.getMember().getRoles().stream().noneMatch(role -> role.getName().toLowerCase().contains("bot") || main.isDeveloper(e.getAuthor().getId()))) {
                e.getChannel().sendMessage("You do not have the permission to stop the bot!").queue();
                return;
            }
            e.getChannel().sendMessage("Stopping bot...").complete(); // Bot is going to shut down so we have to complete this before
            main.getUtil().setTyping(false, e.getChannel());
            System.exit(0);
            return;
        }
        if (main.isDeveloper(e.getMember().getId())) {
            if (args[1].contentEquals("dev")) { //for doing dev things... no-one should even know about it exept devs
                if (args[2].contentEquals("addGApplyReact")) {
                    e.getChannel().retrievePinnedMessages().complete().get(0).addReaction("☑").queue();
                    return;
                }
            }
        }
        e.getChannel().sendMessage("Invalid argument! Valid arguments: `version`, `update`, `stop`!").queue();
    }
}
