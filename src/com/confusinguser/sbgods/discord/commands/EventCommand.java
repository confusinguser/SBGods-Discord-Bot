package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.SkillExp;
import com.confusinguser.sbgods.entities.leaderboard.SlayerExp;
import com.confusinguser.sbgods.utils.LeaderboardUpdater;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EventCommand extends Command {

    public boolean postLeaderboard;

    public EventCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "event";
        this.aliases = new String[]{};
        this.postLeaderboard = false;
        this.usage = getName() + " `start`, `progress`, `startPostingLb`, `stopPostingLb`, `stop`";
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (!currentDiscordServer.getHypixelGuild().equals(HypixelGuild.SBG)) {
            e.getChannel().sendMessage(main.getMessageByKey("command_cannot_be_used_on_server")).queue();
            return;
        }
        if (!senderMember.hasPermission(Permission.MANAGE_SERVER)) {
            e.getChannel().sendMessage(main.getMessageByKey("no_permission")).queue();
            return;
        }
        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: " + this.usage).queue();
            return;
        }

        if (args[1].equals("startPostingLb")) {
            this.postLeaderboard = true;
            e.getChannel().sendMessage("Started posting event lb").queue();
            return;
        }
        if (args[1].equals("sendLb")) {
            if(LeaderboardUpdater.instance.getLatestEventLbIds() != null) {
                if (LeaderboardUpdater.instance.getLatestEventLbIds().size() != 0) {
                    for (String messageId : LeaderboardUpdater.instance.getLatestEventLbIds()) {
                        TextChannel textChannel;
                        if ((textChannel = main.getDiscord().getJDA().getTextChannelById("753934993788633170")) != null)
                            textChannel.deleteMessageById(messageId).queue();
                    }
                }
            }
            LeaderboardUpdater.instance.setLatestEventLbIds(main.getDiscord().eventCommand.sendProgressLbRetIds(main.getDiscord().getJDA().getTextChannelById("753934993788633170"), "skillTotal", "Total Skill Exp Progress\n", true));
            return;
        }

        if (args[1].equals("stopPostingLb")) {
            this.postLeaderboard = false;
            e.getChannel().sendMessage("Stopped posting event lb").queue();
            return;
        }

        if (args[1].equals("start")) {

            String messageId = e.getChannel().sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0, 50) + ")").complete().getId();

            JSONArray eventData = new JSONArray();

            List<Player> guildmembers = main.getApiUtil().getGuildMembers(HypixelGuild.SBG);
            int i = 0;
            for (Player guildMember : guildmembers) {
                i++;
                if (i % 3 == 0) {
                    e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar((double) i / guildmembers.size(), 50) + ")").queue();
                }
                JSONObject playerData = new JSONObject();
                    Player player = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());
                    playerData.put("uuid", player.getUUID());
                    playerData.put("displayName", player.getDisplayName());
                    playerData.put("lastLogin", player.getLastLogin());
                    playerData.put("lastLogout", player.getLastLogout());

                    SlayerExp slayer = main.getApiUtil().getPlayerSlayerExp(player.getUUID());

                    playerData.put("slayerTotal", slayer.getTotalExp());
                    playerData.put("slayerZombie", slayer.getZombie());
                    playerData.put("slayerSpider", slayer.getSpider());
                    playerData.put("slayerWolf", slayer.getWolf());

                SkillExp skillExp = main.getApiUtil().getBestProfileSkillExp(player.getUUID());

                playerData.put("skillTotal", skillExp.getTotalSkillExp());
                playerData.put("skillAlchemy", skillExp.getAlchemy());
                playerData.put("skillCarpentry", skillExp.getCarpentry());
                playerData.put("skillCombat", skillExp.getCombat());
                playerData.put("skillEnchanting", skillExp.getEnchanting());
                playerData.put("skillFarming", skillExp.getFarming());
                playerData.put("skillFishing", skillExp.getFishing());
                playerData.put("skillForaging", skillExp.getForaging());
                playerData.put("skillMining", skillExp.getMining());
                playerData.put("skillRunecrafting", skillExp.getRunecrafting());
                playerData.put("skillTaming", skillExp.getTaming());

                eventData.put(playerData);
            }

            main.getApiUtil().setEventData(eventData);

            e.getChannel().editMessageById(messageId, "Recorded player data!").queue();

            return;
        }

        if (args[1].equals("stop")) {

            String messageId = e.getChannel().sendMessage("...").complete().getId();

            main.getApiUtil().setEventData(new JSONArray());

            e.getChannel().editMessageById(messageId, "Removed all event data!").queue();

            return;
        }

        if (args[1].equals("progress")) {
            if (args.length <= 2) {
                e.getChannel().sendMessage("You must specify a progress type. Avalible progress types are: `totalslayer` `totalskill` `wolfslayer` `spiderslayer` `zombieslayer` `alchemyskill` `carpentryskill` `combatskill` `enchantingskill` `farmingskill` `fishingskill` `foragingskill` `miningskill` `runecraftingskill` `tamingskill`").queue();
                return;
            }

            switch (args[2].toLowerCase()) {
                case "totalskill":
                    sendProgressLb(e.getTextChannel(), "skillTotal", "Total Skill Exp Progress\n", true);
                    return;

                case "totalslayer":
                    sendProgressLb(e.getTextChannel(), "slayerTotal", "Total Slayer Exp Progress\n", true);
                    return;

                case "wolfslayer":
                    sendProgressLb(e.getTextChannel(), "slayerWolf", "Wolf Slayer Exp Progress\n", true);
                    return;

                case "spiderslayer":
                    sendProgressLb(e.getTextChannel(), "slayerSpider", "Spider Slayer Exp Progress\n", true);
                    return;

                case "zombieslayer":
                    sendProgressLb(e.getTextChannel(), "slayerZombie", "Zombie Slayer Exp Progress\n", true);
                    return;

                case "alchemyskill":
                    sendProgressLb(e.getTextChannel(), "skillAlchemy", "Alchemy Skill Exp Progress\n", true);
                    return;

                case "carpentryskill":
                    sendProgressLb(e.getTextChannel(), "skillCarpentry", "Carpentry Skill Exp Progress\n", true);
                    return;

                case "combatskill":
                    sendProgressLb(e.getTextChannel(), "skillCombat", "Combat Skill Exp Progress\n", true);
                    return;

                case "enchantingskill":
                    sendProgressLb(e.getTextChannel(), "skillEnchanting", "Enchanting Skill Exp Progress\n", true);
                    return;

                case "farmingskill":
                    sendProgressLb(e.getTextChannel(), "skillFarming", "Farming Skill Exp Progress\n", true);
                    return;

                case "fishingskill":
                    sendProgressLb(e.getTextChannel(), "skillFishing", "Fishing Skill Exp Progress\n", true);
                    return;

                case "foragingskill":
                    sendProgressLb(e.getTextChannel(), "skillForaging", "Foraging Skill Exp Progress\n", true);
                    return;

                case "miningskill":
                    sendProgressLb(e.getTextChannel(), "skillMining", "Mining Skill Exp Progress\n", true);
                    return;

                case "runecraftingskill":
                    sendProgressLb(e.getTextChannel(), "skillRunecrafting", "Runecrafting Skill Exp Progress\n", true);
                    return;

                case "tamingskill":
                    sendProgressLb(e.getTextChannel(), "skillTaming", "Taming Skill Exp Progress\n", true);
                    return;

                default:
                    e.getChannel().sendMessage("Invalid progress type. Valid progress types are: `totalslayer` `totalskill` `wolfslayer` `spiderslayer` `zombieslayer` `alchemyskill` `carpentryskill` `combatskill` `enchantingskill` `farmingskill` `fishingskill` `foragingskill` `miningskill` `runecraftingskill` `tamingskill`").queue();
                    return;
            }
        }
        e.getChannel().sendMessage("Invalid usage! Usage: `" + this.usage + "`").queue();
    }

    private JSONArray sortPlayerProgressArray(JSONArray jsonArr, String sortBy) {
        JSONArray sortedJsonArray = new JSONArray();

        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < jsonArr.length(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }
        jsonValues.sort((a, b) -> {
            int valA = 0;
            int valB = 0;

            try {
                valA = a.getJSONObject("playerProgress").getInt(sortBy);
                valB = b.getJSONObject("playerProgress").getInt(sortBy);
            } catch (JSONException ignored) {
            }
            return valB - valA;
        });
        for (int i = 0; i < jsonArr.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }
        return sortedJsonArray;
    }

    public void sendProgressLb(TextChannel channel, String key, String message, boolean showLoadingMsg) {
        JSONArray eventProgress = getEventDataProgress(channel, showLoadingMsg);

        StringBuilder messageBuilder = new StringBuilder(message);
//        List<JSONObject> leaderboardList = eventProgress.toList().stream()
//                .filter(object -> object instanceof JSONObject)
//                .map(object -> (JSONObject) object)
//                .sorted(Comparator.comparingDouble(jsonObject -> jsonObject.getInt(key)))
//                .collect(Collectors.toList());

        JSONArray leaderboardList = sortPlayerProgressArray(eventProgress, key);

        for (int i = 0; i < leaderboardList.length(); i++) {
            JSONObject player = leaderboardList.getJSONObject(i);
            messageBuilder.append("#").append(i + 1).append(" ").append(player.getString("displayName").replace("_","\\_")).append(": ").append(main.getLangUtil().addNotation(player.getJSONObject("playerProgress").getInt(key))).append("\n");
        }
        message = messageBuilder.toString();

        List<String> messageSend = main.getUtil().processMessageForDiscord(message, 2000);
        for (String messageSending : messageSend) {
            channel.sendMessage(new EmbedBuilder().setDescription(messageSending).build()).queue();
        }
    }

    public List<String> sendProgressLbRetIds(TextChannel channel, String key, String message, boolean showLoadingMsg) {
        JSONArray eventProgress = getEventDataProgress(channel, showLoadingMsg);

        StringBuilder messageBuilder = new StringBuilder(message);
//        List<JSONObject> leaderboardList = eventProgress.toList().stream()
//                .filter(object -> object instanceof JSONObject)
//                .map(object -> (JSONObject) object)
//                .sorted(Comparator.comparingDouble(jsonObject -> jsonObject.getInt(key)))
//                .collect(Collectors.toList());

        JSONArray leaderboardList = sortPlayerProgressArray(eventProgress, key);

        for (int i = 0; i < leaderboardList.length(); i++) {
            JSONObject player = leaderboardList.getJSONObject(i);
            messageBuilder.append("#").append(i + 1).append(" ").append(player.getString("displayName").replace("_","\\_")).append(": ").append(main.getLangUtil().addNotation(player.getJSONObject("playerProgress").getInt(key))).append("\n");
        }
        message = messageBuilder.toString();

        List<String> messageSend = main.getUtil().processMessageForDiscord(message, 2000);

        ArrayList<String> res = new ArrayList<>();
        for (String messageSending : messageSend) {
            String id = channel.sendMessage(new EmbedBuilder().setDescription(messageSending).build()).complete().getId();
            res.add(id);
        }

        return res;
    }

    public void addToPos(int pos, JSONObject jsonObj, JSONArray jsonArr) {
        for (int i = jsonArr.length(); i > pos; i--) {
            jsonArr.put(i, jsonArr.get(i - 1));
        }
        jsonArr.put(pos, jsonObj);
    }

    private JSONArray getEventDataProgress(TextChannel e, boolean showLoadingMsg) {
        String messageId = "";

        if (showLoadingMsg) {
            messageId = e.sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0, 50) + ")").complete().getId();
        }

        JSONArray data = main.getApiUtil().getEventData();

        for (int i = 0; i < data.length(); i++) {
            if (i % 3 == 0 && showLoadingMsg) {
                e.editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar((double) i / (double) data.length(), 50) + ")").queue();
            }

            JSONObject memberData = data.getJSONObject(i);

//            Player player = main.getApiUtil().getPlayerFromUUID(memberData.getString("uuid"));
//
//            if (memberData.getInt("lastLogin") > memberData.getInt("lastLogout")) {
//                needToUpdateData = true;
//            } else {
//                if (memberData.getInt("lastLogin") < player.getLastLogin()) {
//                    needToUpdateData = true;
//                }
            //}

            JSONObject playerProgress = new JSONObject();

            if(memberData.getInt("skillTotal") == 0){

                SlayerExp slayer = main.getApiUtil().getPlayerSlayerExp(memberData.getString("uuid"));

                playerProgress.put("slayerZombie", (slayer.getZombie() - memberData.getInt("slayerZombie")));
                playerProgress.put("slayerSpider", slayer.getSpider() - memberData.getInt("slayerSpider"));
                playerProgress.put("slayerWolf", slayer.getWolf() - memberData.getInt("slayerWolf"));
                playerProgress.put("slayerTotal", playerProgress.getInt("slayerZombie") + playerProgress.getInt("slayerSpider") + playerProgress.getInt("slayerWolf"));

                playerProgress.put("skillAlchemy", 0);
                playerProgress.put("skillCarpentry", 0);
                playerProgress.put("skillCombat", 0);
                playerProgress.put("skillEnchanting", 0);
                playerProgress.put("skillFarming", 0);
                playerProgress.put("skillFishing", 0);
                playerProgress.put("skillForaging", 0);
                playerProgress.put("skillMining", 0);
                playerProgress.put("skillRunecrafting", 0);
                playerProgress.put("skillTaming", 0);

                playerProgress.put("skillTotal", 0);
            }else {
                SlayerExp slayer = main.getApiUtil().getPlayerSlayerExp(memberData.getString("uuid"));

                playerProgress.put("slayerZombie", (slayer.getZombie() - memberData.getInt("slayerZombie")));
                playerProgress.put("slayerSpider", slayer.getSpider() - memberData.getInt("slayerSpider"));
                playerProgress.put("slayerWolf", slayer.getWolf() - memberData.getInt("slayerWolf"));
                playerProgress.put("slayerTotal", playerProgress.getInt("slayerZombie") + playerProgress.getInt("slayerSpider") + playerProgress.getInt("slayerWolf"));

                SkillExp skillExp = main.getApiUtil().getBestProfileSkillExp(memberData.getString("uuid"));

                int totalSkillProgress = 0;
                playerProgress.put("skillAlchemy", (skillExp.getAlchemy() - memberData.getInt("skillAlchemy")));
                totalSkillProgress += playerProgress.getInt("skillAlchemy");
                playerProgress.put("skillCarpentry", skillExp.getCarpentry() - memberData.getInt("skillCarpentry"));
                playerProgress.put("skillCombat", skillExp.getCombat() - memberData.getInt("skillCombat"));
                totalSkillProgress += playerProgress.getInt("skillCombat");
                playerProgress.put("skillEnchanting", skillExp.getEnchanting() - memberData.getInt("skillEnchanting"));
                totalSkillProgress += playerProgress.getInt("skillEnchanting");
                playerProgress.put("skillFarming", skillExp.getFarming() - memberData.getInt("skillFarming"));
                totalSkillProgress += playerProgress.getInt("skillFarming");
                playerProgress.put("skillFishing", skillExp.getFishing() - memberData.getInt("skillFishing"));
                totalSkillProgress += playerProgress.getInt("skillFishing");
                playerProgress.put("skillForaging", skillExp.getForaging() - memberData.getInt("skillForaging"));
                totalSkillProgress += playerProgress.getInt("skillForaging");
                playerProgress.put("skillMining", skillExp.getMining() - memberData.getInt("skillMining"));
                totalSkillProgress += playerProgress.getInt("skillMining");
                playerProgress.put("skillRunecrafting", skillExp.getRunecrafting() - memberData.getInt("skillRunecrafting"));
                playerProgress.put("skillTaming", skillExp.getTaming() - memberData.getInt("skillTaming"));
                totalSkillProgress += playerProgress.getInt("skillTaming");

                playerProgress.put("skillTotal", totalSkillProgress);
            }

            memberData.put("playerProgress", playerProgress);

            data.remove(i);
            addToPos(i, memberData, data);
        }

        if (showLoadingMsg) {
            e.deleteMessageById(messageId).queue();
        }

        return data;
    }
}