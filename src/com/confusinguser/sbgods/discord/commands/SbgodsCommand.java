package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.utils.ApiUtil;
import com.confusinguser.sbgods.utils.Util;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class SbgodsCommand extends Command {

    public SbgodsCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "sbgods";
        this.aliases = new String[]{"sbg"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (args.length == 1) {
            e.getChannel().sendMessage("Invalid argument! Valid arguments: `version`, `update`, `stop`!").queue();
            return;
        }

        if (args[1].equalsIgnoreCase("update")) {
            if (senderMember.getRoles().stream().noneMatch(role -> role.getName().toLowerCase().contains("bot") || main.isDeveloper(e.getAuthor().getId()))) {
                e.getChannel().sendMessage("You do not have the permission to update the bot!").queue();
                return;
            }

            Map.Entry<String, String> latestReleaseUrl = ApiUtil.getLatestReleaseUrl();
            if (latestReleaseUrl.getValue().equals("")) {
                e.getChannel().sendMessage("There are no releases available!").queue();
                return;
            }

            String messageId = e.getChannel().sendMessage("Downloading update...").complete().getId();

            File file = Util.getFileToUse(latestReleaseUrl.getKey());
            Path newFilePath;
            try {
                newFilePath = ApiUtil.downloadFile(latestReleaseUrl.getValue(), file);
            } catch (IOException ex) {
                e.getChannel().sendMessage("There was a problem when downloading the latest release").queue();
                main.getDiscord().reportFail(ex, "Release Downloader");
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
                new ProcessBuilder("cmd /c start cmd /k java -jar \"" + newFilePath + "\"").start().waitFor();
                System.exit(0);
            } catch (IOException | InterruptedException ex) {
//                e.getChannel().editMessageById(messageId, "Could not start new version, try stopping the bot").queue();
                System.exit(0);
                return;
            }
            return;
        }

        if (args[1].equalsIgnoreCase("stop")) {
            if (senderMember.getRoles().stream().noneMatch(role -> role.getName().toLowerCase().contains("bot") || main.isDeveloper(e.getAuthor().getId()))) {
                e.getChannel().sendMessage("You do not have the permission to stop the bot!").queue();
                return;
            }
            e.getChannel().sendMessage("Stopping bot...").complete(); // Bot is going to shut down so we have to complete this before
            Util.setTyping(false, e.getChannel());
            System.exit(0);
            return;
        }
        if (main.isDeveloper(senderMember.getId())) {
            if (args[1].contentEquals("dev")) { //for doing dev things... no-one should even know about it exept devs
                if (args[2].contentEquals("addGApplyReact")) {
                    e.getChannel().retrievePinnedMessages().complete().get(0).addReaction("☑").queue();
                    return;
                }
            }
        }
        e.getChannel().sendMessage("Invalid argument! Valid arguments: `update`, `stop`!").queue();
    }
}
