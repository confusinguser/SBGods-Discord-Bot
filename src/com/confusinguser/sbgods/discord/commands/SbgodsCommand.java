package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class SbgodsCommand extends Command implements EventListener {

    public SbgodsCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "sbgods";
        this.aliases = new String[]{};
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || isNotTheCommand(e) || discord.shouldNotRun(e)) {
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        String[] args = e.getMessage().getContentRaw().split(" ");

        if (args.length == 1) {
            e.getChannel().sendMessage("Invalid argument! Valid arguments: `version`, `update`, `stop`!").queue();
            return;
        }

        if (args[1].equalsIgnoreCase("version")) {
            User creatorUserObj = discord.getJDA().getUserById(main.getCreatorId());

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Version " + SBGods.VERSION)
                    .setDescription(SBGods.VERSION_DESCRIPTION)
                    .setFooter("Made by " + Objects.requireNonNull(creatorUserObj).getAsTag());
            e.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        if (args[1].equalsIgnoreCase("update")) {
            if (e.getMember() != null && e.getMember().getRoles().stream().noneMatch(role -> role.getName().toLowerCase().contains("bot") || e.getAuthor().getId().equals(main.getCreatorId()))) {
                e.getChannel().sendMessage("You do not have the permission to update the bot!").queue();
                return;
            }

            e.getChannel().sendTyping().queue();
            Map.Entry<String, String> latestReleaseUrl = main.getApiUtil().getLatestReleaseUrl();
            if (latestReleaseUrl.getValue().equals("")) {
                e.getChannel().sendMessage("There are no releases available!").queue();
                return;
            }

            Path newFilePath;
            try {
                newFilePath = main.getApiUtil().downloadFile(latestReleaseUrl.getValue(), latestReleaseUrl.getKey());
            } catch (IOException ex) {
                e.getChannel().sendMessage("There was a problem when downloading the latest release").queue();
                main.logger.severe(ex.toString() + '\n' + Arrays.toString(ex.getStackTrace()));
                return;
            }
            if (!SBGods.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith(".jar")) {
                e.getChannel().sendMessage("Cannot update because bot is running on an IDE").queue();
                return;
            }

            String messageId = e.getChannel().sendMessage("**Success!** Downloaded new version and going to restart bot!").complete().getId();
            try {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar \"" + newFilePath.toString() + "\""});
                System.exit(0);
            } catch (IOException ex) {
                e.getChannel().editMessageById(messageId, "Could not start new version").queue(); // Dumb to close bot when you know it won't restart
                return;
            }
            return;
        }

        if (args[1].equalsIgnoreCase("stop")) {
            if (e.getMember() != null && e.getMember().getRoles().stream().noneMatch(role -> role.getName().toLowerCase().contains("bot") || e.getAuthor().getId().equals(main.getCreatorId()))) {
                e.getChannel().sendMessage("You do not have the permission to stop the bot!").queue();
                return;
            }
            e.getChannel().sendMessage("Stopping bot...").queue();
            System.exit(0);
            return;
        }
        e.getChannel().sendMessage("Invalid argument! Valid arguments: `version`, `update`, `stop`!").queue();
    }
}
