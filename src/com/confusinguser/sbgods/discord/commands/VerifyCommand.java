package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SkyblockPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class VerifyCommand extends Command implements EventListener {

    public VerifyCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "verify";
        this.aliases = new String[]{"v"};
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

        if (currentDiscordServer.getChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getChannelId())) {
            e.getChannel().sendMessage("Verify commands cannot be ran in this channel!").queue();
            return;
        }

        main.logger.info(e.getAuthor().getName() + " ran command: " + e.getMessage().getContentRaw());

        String[] args = e.getMessage().getContentRaw().split(" ");

        if(args.length == 2){

            //check if that is actual player ign
            if(main.getApiUtil().getDiscNameFromMc(args[1]).equalsIgnoreCase(e.getAuthor().getAsTag())){
                //Verify player with ign: args[2]

                main.getUtil().verifyPlayer(e.getAuthor(),main.getApiUtil().getDiscNameFromMc(args[1]), e);
                return;
            }

            //send error message saying to link discord account with mc


            e.getChannel().sendMessage(e.getAuthor().getAsMention() + " You need to link your minecraft account with discord (through hypixel social media settings) then run this command again.").queue();
            return;
        }

        String mcName = main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag().replace("#","*"));
        if(mcName == ""){


            e.getChannel().sendMessage("There was a error auto-detecting your minecraft ign... please do -verify {ign}").queue();
            //send error saying that auto-detect failed and they need to enter their username
            return;
        }
        //verify player with mc name mcName

        main.getUtil().verifyPlayer(e.getAuthor(),mcName, e);

    }
}
