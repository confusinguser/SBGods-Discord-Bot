package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

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
        if (e.getAuthor().isBot() || !isTheCommand(e) || !discord.shouldRun(e)) {
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        String[] args = e.getMessage().getContentRaw().split(" ");

        if (args.length == 1) {
            e.getChannel().sendMessage("Invalid argument! Valid argument: `version`!").queue();
            return;
        }

        if (args[1].equalsIgnoreCase("version")) {
            User creatorUserObj = discord.getJDA().getUserById(main.getCreatorId());

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Version " + SBGods.VERSION)
                    .setDescription(SBGods.VERSION_DESCRIPTION)
                    .setFooter("Made by " + Objects.requireNonNull(creatorUserObj).getName() + "#" + creatorUserObj.getDiscriminator());
            e.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }
        e.getChannel().sendMessage("Invalid argument! Valid argument: `version`!").queue();
    }
}
