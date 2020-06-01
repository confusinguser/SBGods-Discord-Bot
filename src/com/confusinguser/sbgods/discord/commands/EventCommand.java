package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
                playerData.put("uuid",player.getUUID());
                playerData.put("displayName",player.getDisplayName());
                playerData.put("lastLogin",player.getLastLogin());
                playerData.put("lastLogout",player.getLastLogout());

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

        if(args[1].equals("stop")){

            String messageId = e.getChannel().sendMessage("...").complete().getId();

            main.getApiUtil().setEventData(new JSONArray());

            e.getChannel().editMessageById(messageId, "Removed all event data!").queue();

            return;
        }

        if(args[1].equals("progress")){

            String progressTypes = "`totalslayer` `totalskill` `wolfslayer` `spiderslayer` `zombieslayer` `alchemyskill` `carpentryskill` `combatskill` `enchantingskill` `farmingskill` `fishingskill` `foragingskill` `miningskill` `runecraftingskill` `tamingskill`";

            if(args.length == 2){
                e.getChannel().sendMessage("You must specify a progress type. Avalible progress types are: " + progressTypes).queue();
                return;
            }

            if(args[2].equalsIgnoreCase("totalskill")){
                sendProgressLb(e.getTextChannel(),"skillTotal", "Total Skill Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("totalslayer")){
                sendProgressLb(e.getTextChannel(),"slayerTotal", "Total Slayer Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("wolfslayer")){
                sendProgressLb(e.getTextChannel(),"slayerWolf", "Wolf Slayer Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("spiderslayer")){
                sendProgressLb(e.getTextChannel(),"slayerSpider", "Spider Slayer Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("zombieslayer")){
                sendProgressLb(e.getTextChannel(),"slayerZombie", "Zombie Slayer Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("alchemyskill")){
                sendProgressLb(e.getTextChannel(),"skillAlchemy", "Alchemy Skill Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("carpentryskill")){
                sendProgressLb(e.getTextChannel(),"skillCarpentry", "Carpentry Skill Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("combatskill")){
                sendProgressLb(e.getTextChannel(),"skillCombat", "Combat Skill Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("enchantingskill")){
                sendProgressLb(e.getTextChannel(),"skillEnchanting", "Enchanting Skill Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("farmingskill")){
                sendProgressLb(e.getTextChannel(),"skillFarming", "Farming Skill Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("fishingskill")){
                sendProgressLb(e.getTextChannel(),"skillFishing", "Fishing Skill Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("foragingskill")){
                sendProgressLb(e.getTextChannel(),"skillForaging", "Foraging Skill Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("miningskill")){
                sendProgressLb(e.getTextChannel(),"skillMining", "Mining Skill Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("runecraftingskill")){
                sendProgressLb(e.getTextChannel(),"skillRunecrafting", "Runecrafting Skill Exp Progress\n");
                return;
            }

            if(args[2].equalsIgnoreCase("tamingskill")){
                sendProgressLb(e.getTextChannel(),"skillTaming", "Taming Skill Exp Progress\n");
                return;
            }


            e.getChannel().sendMessage("Invalid progress type. Valid progress types are: " + progressTypes).queue();

            return;
        }


        e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
    }

    private void sendProgressLb(TextChannel e, String key, String message){
        JSONArray eventProgress = getEventDataProgress(e);

        int len = eventProgress.length();//  Really bad soring algorithm below, but i couldent get any other to work
        for(int i = 0;i<len;i++){
            JSONObject best = eventProgress.getJSONObject(0);
            int bestIndex=0;
            for(int o = 0;o<eventProgress.length();o++){
                if(eventProgress.getJSONObject(o).getJSONObject("playerProgress").getInt(key)>best.getJSONObject("playerProgress").getInt(key)){
                    best = eventProgress.getJSONObject(o);
                    bestIndex = o;
                }
            }

            message += "#" + i + " " + best.getString("displayName") + ": " + main.getLangUtil().addNotation(best.getJSONObject("playerProgress").getInt(key)) + "\n";
            eventProgress.remove(bestIndex);
        }

        List<String> messageSend = main.getUtil().processMessageForDiscord(message, 2000);
        for(String messageSending : messageSend){
            e.sendMessage(new EmbedBuilder().setDescription(messageSending).build()).queue();
        }
    }

    private JSONArray getEventDataProgress(TextChannel e){
        String messageId = e.sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0,50) + ")").complete().getId();

        JSONArray data = main.getApiUtil().getEventData();

        for(int i = 0;i<data.length();i++){
            if(i%3==0){
                e.editMessageById(messageId,"Loading... (" + main.getLangUtil().getProgressBar(Double.valueOf(i)/Double.valueOf(data.length()),50) + ")").queue();
            }

            JSONObject memberData = data.getJSONObject(i);

            boolean needToUpdateData = false;
            Player player = null;

            if(memberData.getInt("lastLogin")>memberData.getInt("lastLogout")){
                needToUpdateData = true;
            }else{
                player = main.getApiUtil().getPlayerFromUUID(memberData.getString("uuid"));
                if(memberData.getInt("lastLogin")<player.getLastLogin()){
                    needToUpdateData = true;
                }
            }

            if(needToUpdateData){
                if(player == null){
                    player = main.getApiUtil().getPlayerFromUUID(memberData.getString("uuid"));
                }
                JSONObject playerProgress = new JSONObject();

                SlayerExp slayer = main.getApiUtil().getPlayerSlayerExp(player.getUUID());

                playerProgress.put("slayerTotal",slayer.getTotalExp()-memberData.getInt("slayerTotal"));
                playerProgress.put("slayerZombie",slayer.getZombie()-memberData.getInt("slayerZombie"));
                playerProgress.put("slayerSpider",slayer.getSpider()-memberData.getInt("slayerSpider"));
                playerProgress.put("slayerWolf",slayer.getWolf()-memberData.getInt("slayerWolf"));

                SkillExp skillExp = main.getApiUtil().getBestProfileSkillExp(player.getUUID());

                playerProgress.put("skillTotal",skillExp.getTotalSkillExp()-memberData.getInt("skillTotal"));
                playerProgress.put("skillAlchemy",skillExp.getAlchemy()-memberData.getInt("skillAlchemy"));
                playerProgress.put("skillCarpentry",skillExp.getCarpentry()-memberData.getInt("skillCarpentry"));
                playerProgress.put("skillCombat",skillExp.getCombat()-memberData.getInt("skillCombat"));
                playerProgress.put("skillEnchanting",skillExp.getEnchanting()-memberData.getInt("skillEnchanting"));
                playerProgress.put("skillFarming",skillExp.getFarming()-memberData.getInt("skillFarming"));
                playerProgress.put("skillFishing",skillExp.getFishing()-memberData.getInt("skillFishing"));
                playerProgress.put("skillForaging",skillExp.getForaging()-memberData.getInt("skillForaging"));
                playerProgress.put("skillMining",skillExp.getMining()-memberData.getInt("skillMining"));
                playerProgress.put("skillRunecrafting",skillExp.getRunecrafting()-memberData.getInt("skillRunecrafting"));
                playerProgress.put("skillTaming",skillExp.getTaming()-memberData.getInt("skillTaming"));

                memberData.put("playerProgress",playerProgress);
            }else{
                JSONObject playerProgress = new JSONObject();

                playerProgress.put("slayerTotal",0);
                playerProgress.put("slayerZombie",0);
                playerProgress.put("slayerSpider",0);
                playerProgress.put("slayerWolf",0);

                playerProgress.put("skillTotal",0);
                playerProgress.put("skillAlchemy",0);
                playerProgress.put("skillCarpentry",0);
                playerProgress.put("skillCombat",0);
                playerProgress.put("skillEnchanting",0);
                playerProgress.put("skillFarming",0);
                playerProgress.put("skillFishing",0);
                playerProgress.put("skillForaging",0);
                playerProgress.put("skillMining",0);
                playerProgress.put("skillRunecrafting",0);
                playerProgress.put("skillTaming",0);

                memberData.put("playerProgress",playerProgress);
            }

            data.remove(i);
            data.put(i,memberData);
        }

        e.deleteMessageById(messageId).queue();

        return data;
    }
}