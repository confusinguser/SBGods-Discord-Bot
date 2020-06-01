package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map.Entry;

public class EventCommand extends Command {

    public EventCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "event";
        this.usage = this.getName() + "";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (!DiscordServer.getDiscordServerFromEvent(e).getHypixelGuild().equals(HypixelGuild.SBG)) {
            e.getChannel().sendMessage("This command cannot be used on this server.").queue();
            return;
        }
        if (!e.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            e.getChannel().sendMessage("You do not have permission to use this command.").queue();
            return;
        }
        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
            return;
        }

        if(args[1].equals("start")){

            String messageId = e.getChannel().sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0,50) + ")").complete().getId();

            JSONArray eventData = new JSONArray();

            int guildMemberAmount = main.getApiUtil().getGuildMembers(HypixelGuild.SBG).size();

            int i = 0;
            for(Player guildMember : main.getApiUtil().getGuildMembers(HypixelGuild.SBG)){

                i++;
                if(i%3==0){
                    e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar((Double.valueOf(i))/ (Double.valueOf(guildMemberAmount)),50) + ")").queue();
                }

                Player player = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());
                JSONObject playerData = new JSONObject();
                playerData.put("uuid",guildMember.getUUID());
                playerData.put("displayName",player.getDisplayName());

                SlayerExp slayer = main.getApiUtil().getPlayerSlayerExp(player.getUUID());

                playerData.put("slayerTotal",slayer.getTotalExp());
                playerData.put("slayerZombie",slayer.getZombie());
                playerData.put("slayerSpider",slayer.getSpider());
                playerData.put("slayerWolf",slayer.getWolf());

                SkillExp skillExp = main.getApiUtil().getBestProfileSkillExp(player.getUUID());

                playerData.put("skillTotal",skillExp.getTotalSkillExp());
                playerData.put("skillAlchemy",skillExp.getAlchemy());
                playerData.put("skillCarpentry",skillExp.getCarpentry());
                playerData.put("skillCombat",skillExp.getCombat());
                playerData.put("skillEnchanting",skillExp.getEnchanting());
                playerData.put("skillFarming",skillExp.getFarming());
                playerData.put("skillFishing",skillExp.getFishing());
                playerData.put("skillForaging",skillExp.getForaging());
                playerData.put("skillMining",skillExp.getMining());
                playerData.put("skillRunecrafting",skillExp.getRunecrafting());
                playerData.put("skillTaming",skillExp.getTaming());

                eventData.put(playerData);
            }

            main.getApiUtil().setEventData(eventData);

            e.getChannel().editMessageById(messageId, "Recorded player data!").queue();

            return;
        }


        e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
    }
}