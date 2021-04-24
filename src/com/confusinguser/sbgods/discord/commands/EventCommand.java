package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValues;
import com.confusinguser.sbgods.utils.ApiUtil;
import com.confusinguser.sbgods.utils.LangUtil;
import com.confusinguser.sbgods.utils.SBUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EventCommand extends Command {

    public final static Collection<String> validProgressTypes = List.of("totalSlayer", "totalSkill", "wolfSlayer", "spiderSlayer", "zombieSlayer", "alchemySkill", "carpentrySkill", "combatSkill", "enchantingSkill", "farmingSkill", "fishingSkill", "foragingSkill", "miningSkill", "runecraftingSkill", "tamingSkill");
    private JSONArray eventData;
    public String progressTypeToPost;

    public EventCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "event";
        this.progressTypeToPost = "";
        //TODO EventManager
        this.eventData = ApiUtil.getEventData();
        this.usage = getName() + " <start, progress, startPostingLeaderboard, stopPostingLb, stop>";
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (!currentDiscordServer.getHypixelGuild().equals(HypixelGuild.SBG)) {
            e.getChannel().sendMessage(main.getMessageByKey("command_cannot_be_used_in_channel")).queue();
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

        if (args[1].equals("startPostingLb") || args[1].equals("startPostingLeaderboard")) {
            if (args.length == 2 || !validProgressTypes.contains(args[2])) {
                e.getChannel().sendMessage("Usage: -event startPostingLeaderboard <ProgressType>.\n" +
                        "Valid progress types are: `totalSlayer`, `totalSkill`, `wolfSlayer`, `spiderSlayer`, `zombieSlayer`, `alchemySkill`, `carpentrySkill`, `combatSkill`, `enchantingSkill`, `farmingSkill`, `fishingSkill`, `foragingSkill`, `miningSkill`, `runecraftingSkill`, `tamingSkill`").queue();
            } else {
                this.progressTypeToPost = args[2];
                e.getChannel().sendMessage("Started posting event leaderboard").queue();
            }
            return;
        }

        if (args[1].equals("stopPostingLb") || args[1].equals("stopPostingLeaderboard")) {
            this.progressTypeToPost = "";
            e.getChannel().sendMessage("Stopped posting event leaderboard").queue();
            return;
        }

        if (args[1].equals("start")) {
            String messageId = e.getChannel().sendMessage("Loading... (" + LangUtil.getProgressBar(0, 50) + ")").complete().getId();
            JSONArray eventData = new JSONArray();
            List<Player> guildmembers = ApiUtil.getGuildMembers(HypixelGuild.SBG);
            int i = 0;
            Map<Player, LeaderboardValues> playerStatMap = HypixelGuild.SBG.getPlayerStatMap();
            if (playerStatMap == null || playerStatMap.isEmpty()) {
                e.getChannel().deleteMessageById(messageId).queue();
                e.getChannel().sendMessage("Leaderboards haven't loaded, try again in a few minutes").queue();
                return;
            }
            for (Map.Entry<Player, LeaderboardValues> playerStat : playerStatMap.entrySet()) {
                if (i++ % 3 == 0) {
                    e.getChannel().editMessageById(messageId, "Loading... (" + LangUtil.getProgressBar((double) i / guildmembers.size(), 50) + ")").queue();
                }
                JSONObject playerData = new JSONObject();

                playerData.put("uuid", playerStat.getKey().getUUID());
                playerData.put("displayName", playerStat.getKey().getDisplayName());
                playerData.put("lastLogin", playerStat.getKey().getLastLogin());
                playerData.put("lastLogout", playerStat.getKey().getLastLogout());

                playerData.put("slayerTotal", playerStat.getValue().getSlayerExp().getTotalExp());
                playerData.put("slayerZombie", playerStat.getValue().getSlayerExp().getZombie());
                playerData.put("slayerSpider", playerStat.getValue().getSlayerExp().getSpider());
                playerData.put("slayerWolf", playerStat.getValue().getSlayerExp().getWolf());

                playerData.put("skillTotal", playerStat.getValue().getSkillLevels().getTotalSkillExp());
                playerData.put("skillAlchemy", SBUtil.toSkillExp(playerStat.getValue().getSkillLevels().getAlchemy()));
                playerData.put("skillCarpentry", SBUtil.toSkillExp(playerStat.getValue().getSkillLevels().getCarpentry()));
                playerData.put("skillCombat", SBUtil.toSkillExp(playerStat.getValue().getSkillLevels().getCombat()));
                playerData.put("skillEnchanting", SBUtil.toSkillExp(playerStat.getValue().getSkillLevels().getEnchanting()));
                playerData.put("skillFarming", SBUtil.toSkillExp(playerStat.getValue().getSkillLevels().getFarming()));
                playerData.put("skillFishing", SBUtil.toSkillExp(playerStat.getValue().getSkillLevels().getFishing()));
                playerData.put("skillForaging", SBUtil.toSkillExp(playerStat.getValue().getSkillLevels().getForaging()));
                playerData.put("skillMining", SBUtil.toSkillExp(playerStat.getValue().getSkillLevels().getMining()));
                playerData.put("skillRunecrafting", SBUtil.toSkillExp(playerStat.getValue().getSkillLevels().getRunecrafting()));
                playerData.put("skillTaming", SBUtil.toSkillExp(playerStat.getValue().getSkillLevels().getTaming()));

                playerData.put("dungeonClassTotal", playerStat.getValue().getDungeonLevels().getTotalClassExp());
                playerData.put("dungeonClassHealer", playerStat.getValue().getDungeonLevels().getHealerExp());
                playerData.put("dungeonClassBerserk", playerStat.getValue().getDungeonLevels().getBerserkExp());
                playerData.put("dungeonClassArcher", playerStat.getValue().getDungeonLevels().getArcherExp());
                playerData.put("dungeonClassTank", playerStat.getValue().getDungeonLevels().getTankExp());
                playerData.put("dungeonDungeonCatacombs", playerStat.getValue().getDungeonLevels().getCatacombsExp());

                eventData.put(playerData);
            }

            this.eventData = eventData;
            e.getChannel().editMessageById(messageId, "Started event!").queue();
            ApiUtil.setEventData(eventData);
            return;
        }

        if (args[1].equals("stop")) {
            String messageId = e.getChannel().sendMessage("...").complete().getId();
            eventData = new JSONArray();
            e.getChannel().editMessageById(messageId, "Removed all event data!").queue(); // TODO here are the final scores
            return;
        }

        if (args[1].equals("progress")) {
            if (args.length <= 2) {
                e.getChannel().sendMessage("You must specify a progress type. Valid progress types are: `totalSlayer`, `totalSkill`, `wolfSlayer`, `spiderSlayer`, `zombieSlayer`, `alchemySkill`, `carpentrySkill`, `combatSkill`, `enchantingSkill`, `farmingSkill`, `fishingSkill`, `foragingSkill`, `miningSkill`, `runecraftingSkill`, `tamingSkill`").queue();
                return;
            }

            switch (args[2].toLowerCase()) {
                case "totalskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillTotal", "Total Skill Exp Progress", true);
                    return;
                case "totalslayer":
                    sendProgressLeaderboard(e.getTextChannel(), "slayerTotal", "Total Slayer Exp Progress", true);
                    return;
                case "wolfslayer":
                    sendProgressLeaderboard(e.getTextChannel(), "slayerWolf", "Wolf Slayer Exp Progress", true);
                    return;
                case "spiderslayer":
                    sendProgressLeaderboard(e.getTextChannel(), "slayerSpider", "Spider Slayer Exp Progress", true);
                    return;
                case "zombieslayer":
                    sendProgressLeaderboard(e.getTextChannel(), "slayerZombie", "Zombie Slayer Exp Progress", true);
                    return;
                case "alchemyskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillAlchemy", "Alchemy Skill Exp Progress", true);
                    return;
                case "carpentryskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillCarpentry", "Carpentry Skill Exp Progress", true);
                    return;
                case "combatskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillCombat", "Combat Skill Exp Progress", true);
                    return;
                case "enchantingskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillEnchanting", "Enchanting Skill Exp Progress", true);
                    return;
                case "farmingskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillFarming", "Farming Skill Exp Progress", true);
                    return;
                case "fishingskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillFishing", "Fishing Skill Exp Progress", true);
                    return;
                case "foragingskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillForaging", "Foraging Skill Exp Progress", true);
                    return;
                case "miningskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillMining", "Mining Skill Exp Progress", true);
                    return;
                case "runecraftingskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillRunecrafting", "Runecrafting Skill Exp Progress", true);
                    return;
                case "tamingskill":
                    sendProgressLeaderboard(e.getTextChannel(), "skillTaming", "Taming Skill Exp Progress", true);
                    return;
                default:
                    e.getChannel().sendMessage("Invalid progress type! Valid progress types are: `totalslayer`, `totalSkill`, `wolfslayer`, `spiderslayer`, `zombieslayer`, `alchemySkill`, `carpentrySkill`, `combatSkill`, `enchantingSkill`, `farmingSkill`, `fishingSkill`, `foragingSkill`, `miningSkill`, `runecraftingSkill`, `tamingSkill`").queue();
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

    public List<String> sendProgressLeaderboard(TextChannel channel, String key, String message, boolean showLoadingMsg) {
        JSONArray eventProgress = getEventDataProgress(channel, showLoadingMsg);

        if (eventProgress.isEmpty()) {
            channel.sendMessage("Event not started! Start using `-event start`").queue();
            return new ArrayList<>();
        }

        StringBuilder messageBuilder = new StringBuilder(message + "\n");
//        List<JSONObject> leaderboardList = eventProgress.toList().stream()
//                .filter(object -> object instanceof JSONObject)
//                .map(object -> (JSONObject) object)
//                .sorted(Comparator.comparingDouble(jsonObject -> jsonObject.getInt(key)))
//                .collect(Collectors.toList());

        JSONArray leaderboardList = sortPlayerProgressArray(eventProgress, key);
        for (int i = 0; i < leaderboardList.length(); i++) {
            JSONObject player = leaderboardList.getJSONObject(i);
            messageBuilder.append("#").append(i + 1).append(" ").append(player.getString("displayName").replace("_", "\\_")).append(": ").append(LangUtil.addNotation(player.getJSONObject("playerProgress").getInt(key))).append("\n");
        }
        message = messageBuilder.toString();

        List<String> messageSend = LangUtil.processMessageForDiscord(message, 2000);

        List<String> res = new ArrayList<>();
        for (String messageSending : messageSend) {
            String id = channel.sendMessage(new EmbedBuilder().setDescription(messageSending).build()).complete().getId();
            res.add(id);
        }

        return res;
    }

    public void addToPos(int pos, JSONObject toPut, JSONArray toPutIn) {
        for (int i = toPutIn.length(); i > pos; i--) {
            toPutIn.put(i, toPutIn.get(i - 1));
        }
        toPutIn.put(pos, toPut);
    }

    private JSONArray getEventDataProgress(MessageChannel channel, boolean showLoadingMsg) {
        String messageId = "";

        if (showLoadingMsg) {
            messageId = channel.sendMessage("Loading... (" + LangUtil.getProgressBar(0, 12) + ")").complete().getId();
        }

        for (int i = 0; i < eventData.length(); i++) {
            if (i % 10 == 0 && showLoadingMsg) {
                channel.editMessageById(messageId, "Loading... (" + LangUtil.getProgressBar((double) i / eventData.length(), 12) + ")").queue();
            }

            JSONObject memberDataJSON = eventData.getJSONObject(i);
            LeaderboardValues memberData = LeaderboardValues.fromJSON(memberDataJSON);
            LeaderboardValues currData = ApiUtil.getBestLeaderboardValues(ApiUtil.getPlayerFromUUID(memberDataJSON.getString("uuid")));
//            Player player = ApiUtil.getPlayerFromUUID(memberData.getString("uuid"));
//
//            if (memberData.getInt("lastLogin") > memberData.getInt("lastLogout")) {
//                needToUpdateData = true;
//            } else {
//                if (memberData.getInt("lastLogin") < player.getLastLogin()) {
//                    needToUpdateData = true;
//                }
            //}

            JSONObject playerProgress = new JSONObject();

            playerProgress.put("slayerZombie", currData.getSlayerExp().getZombie() - memberData.getSlayerExp().getZombie());
            playerProgress.put("slayerSpider", currData.getSlayerExp().getSpider() - memberData.getSlayerExp().getSpider());
            playerProgress.put("slayerWolf", currData.getSlayerExp().getWolf() - memberData.getSlayerExp().getWolf());
            playerProgress.put("slayerTotal", currData.getSlayerExp().getTotalExp() - memberData.getSlayerExp().getTotalExp());

            if (memberData.getSkillLevels() == null) { // Skill api off
                playerProgress.put("skillAlchemy", 0);
                playerProgress.put("skillCombat", 0);
                playerProgress.put("skillEnchanting", 0);
                playerProgress.put("skillFarming", 0);
                playerProgress.put("skillFishing", 0);
                playerProgress.put("skillForaging", 0);
                playerProgress.put("skillMining", 0);
                playerProgress.put("skillTaming", 0);
                playerProgress.put("skillCarpentry", 0);
                playerProgress.put("skillRunecrafting", 0);

                playerProgress.put("skillTotal", 0);
            } else {

                playerProgress.put("skillAlchemy", currData.getSkillLevels().getAlchemy() - memberData.getSkillLevels().getAlchemy());
                playerProgress.put("skillCombat", currData.getSkillLevels().getCombat() - memberData.getSkillLevels().getCombat());
                playerProgress.put("skillEnchanting", currData.getSkillLevels().getEnchanting() - memberData.getSkillLevels().getEnchanting());
                playerProgress.put("skillFarming", currData.getSkillLevels().getFarming() - memberData.getSkillLevels().getFarming());
                playerProgress.put("skillFishing", currData.getSkillLevels().getFishing() - memberData.getSkillLevels().getFishing());
                playerProgress.put("skillForaging", currData.getSkillLevels().getForaging() - memberData.getSkillLevels().getForaging());
                playerProgress.put("skillMining", currData.getSkillLevels().getMining() - memberData.getSkillLevels().getMining());
                playerProgress.put("skillTaming", currData.getSkillLevels().getTaming() - memberData.getSkillLevels().getTaming());
                playerProgress.put("skillCarpentry", currData.getSkillLevels().getCarpentry() - memberData.getSkillLevels().getCarpentry());
                playerProgress.put("skillRunecrafting", currData.getSkillLevels().getRunecrafting() - memberData.getSkillLevels().getRunecrafting());
                playerProgress.put("skillTotal", currData.getSkillLevels().getTotalSkillExp() - memberData.getSkillLevels().getTotalSkillExp());
            }
            memberDataJSON.put("playerProgress", playerProgress);
            eventData.remove(i);
            addToPos(i, memberDataJSON, eventData);
        }

        if (showLoadingMsg) {
            channel.deleteMessageById(messageId).queue();
        }

        return eventData;
    }

    public String getLeaderboardTitleForProgressType(String progressType) {
        switch (progressType.toLowerCase()) {
            case "totalskill":
                return "Total Skill Exp Progress";
            case "totalslayer":
                return "Total Slayer Exp Progress";
            case "wolfslayer":
                return "Wolf Slayer Exp Progress";
            case "spiderslayer":
                return "Spider Slayer Exp Progress";
            case "zombieslayer":
                return "Zombie Slayer Exp Progress";
            case "alchemyskill":
                return "Alchemy Skill Exp Progress";
            case "carpentryskill":
                return "Carpentry Skill Exp Progress";
            case "combatskill":
                return "Combat Skill Exp Progress";
            case "enchantingskill":
                return "Enchanting Skill Exp Progress";
            case "farmingskill":
                return "Farming Skill Exp Progress";
            case "fishingskill":
                return "Fishing Skill Exp Progress";
            case "foragingskill":
                return "Foraging Skill Exp Progress";
            case "miningskill":
                return "Mining Skill Exp Progress";
            case "runecraftingskill":
                return "Runecrafting Skill Exp Progress";
            case "tamingskill":
                return "Taming Skill Exp Progress";
            default:
                return null;
        }
    }
}