package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValues;
import com.confusinguser.sbgods.entities.leaderboard.SkillLevels;
import com.confusinguser.sbgods.entities.leaderboard.SlayerExp;
import com.confusinguser.sbgods.utils.LeaderboardUpdater;
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

public class EventCommand extends Command {

    public final static Collection<String> validProgressTypes = List.of("totalSlayer", "totalSkill", "wolfSlayer", "spiderSlayer", "zombieSlayer", "alchemySkill", "carpentrySkill", "combatSkill", "enchantingSkill", "farmingSkill", "fishingSkill", "foragingSkill", "miningSkill", "runecraftingSkill", "tamingSkill");
    public String progressTypeToPost;

    public EventCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "event";
        this.aliases = new String[]{};
        this.progressTypeToPost = "";
        this.usage = getName() + " `start`, `progress`, `startPostingLeaderboard`, `stopPostingLb`, `stop`";
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
                e.getChannel().sendMessage("Usage: -event startPostingLeaderboard <Progress Type (Case Sensitive)>.\n" +
                        "Valid progress types are: `totalSlayer`, `totalSkill`, `wolfSlayer`, `spiderSlayer`, `zombieSlayer`, `alchemySkill`, `carpentrySkill`, `combatSkill`, `enchantingSkill`, `farmingSkill`, `fishingSkill`, `foragingSkill`, `miningSkill`, `runecraftingSkill`, `tamingSkill`").queue();
            } else {
                this.progressTypeToPost = args[2];
                e.getChannel().sendMessage("Started posting event leaderboard").queue();
            }
            return;
        }

        if (args[1].equals("sendLb")) {
            if (LeaderboardUpdater.instance.getLatestEventLeaderboardIds() != null &&
                    LeaderboardUpdater.instance.getLatestEventLeaderboardIds().size() != 0) {
                for (String messageId : LeaderboardUpdater.instance.getLatestEventLeaderboardIds()) {
                    TextChannel textChannel;
                    if ((textChannel = main.getDiscord().getJDA().getTextChannelById("753934993788633170")) != null)
                        textChannel.deleteMessageById(messageId).queue();
                }
            }
            LeaderboardUpdater.instance.setLatestEventLeaderboardIds(main.getDiscord().eventCommand.sendProgressLeaderboard(main.getDiscord().getJDA().getTextChannelById("753934993788633170"), "skillTotal", "Total Skill Exp Progress", true));
            return;
        }

        if (args[1].equals("stopPostingLb") || args[1].equals("stopPostingLeaderboard")) {
            this.progressTypeToPost = "";
            e.getChannel().sendMessage("Stopped posting event leaderboard").queue();
            return;
        }

        if (args[1].equals("start")) {
            String messageId = e.getChannel().sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0, 50) + ")").complete().getId();
            JSONArray eventData = new JSONArray();
            List<Player> guildmembers = main.getApiUtil().getGuildMembers(HypixelGuild.SBG);
            int i = 0;
            for (Player guildMember : guildmembers) {
                if (i++ % 3 == 0) {
                    e.getChannel().editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar((double) i / guildmembers.size(), 50) + ")").queue();
                }
                JSONObject playerData = new JSONObject();

                Player player = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());
                playerData.put("uuid", player.getUUID());
                playerData.put("displayName", player.getDisplayName());
                playerData.put("lastLogin", player.getLastLogin());
                playerData.put("lastLogout", player.getLastLogout());

                LeaderboardValues leaderboardValues = main.getApiUtil().getBestLeaderboardValues(player.getUUID());
                playerData.put("slayerTotal", leaderboardValues.getSlayerExp().getTotalExp());
                playerData.put("slayerZombie", leaderboardValues.getSlayerExp().getZombie());
                playerData.put("slayerSpider", leaderboardValues.getSlayerExp().getSpider());
                playerData.put("slayerWolf", leaderboardValues.getSlayerExp().getWolf());

                playerData.put("skillTotal", leaderboardValues.getSkillLevels().getTotalSkillExp());
                playerData.put("skillAlchemy", main.getSBUtil().toSkillExp(leaderboardValues.getSkillLevels().getAlchemy()));
                playerData.put("skillCarpentry", main.getSBUtil().toSkillExp(leaderboardValues.getSkillLevels().getCarpentry()));
                playerData.put("skillCombat", main.getSBUtil().toSkillExp(leaderboardValues.getSkillLevels().getCombat()));
                playerData.put("skillEnchanting", main.getSBUtil().toSkillExp(leaderboardValues.getSkillLevels().getEnchanting()));
                playerData.put("skillFarming", main.getSBUtil().toSkillExp(leaderboardValues.getSkillLevels().getFarming()));
                playerData.put("skillFishing", main.getSBUtil().toSkillExp(leaderboardValues.getSkillLevels().getFishing()));
                playerData.put("skillForaging", main.getSBUtil().toSkillExp(leaderboardValues.getSkillLevels().getForaging()));
                playerData.put("skillMining", main.getSBUtil().toSkillExp(leaderboardValues.getSkillLevels().getMining()));
                playerData.put("skillRunecrafting", main.getSBUtil().toSkillExp(leaderboardValues.getSkillLevels().getRunecrafting()));
                playerData.put("skillTaming", main.getSBUtil().toSkillExp(leaderboardValues.getSkillLevels().getTaming()));

                playerData.put("dungeonClassTotal", leaderboardValues.getDungeonLevels().getTotalClassExp());
                playerData.put("dungeonClassHealer", leaderboardValues.getDungeonLevels().getHealerExp());
                playerData.put("dungeonClassBerserk", leaderboardValues.getDungeonLevels().getBerserkExp());
                playerData.put("dungeonClassArcher", leaderboardValues.getDungeonLevels().getArcherExp());
                playerData.put("dungeonClassTank", leaderboardValues.getDungeonLevels().getTankExp());
                playerData.put("dungeonDungeonCatacombs", leaderboardValues.getDungeonLevels().getCatacombsExp());

                eventData.put(playerData);
            }

            main.getApiUtil().setEventData(eventData);
            e.getChannel().editMessageById(messageId, "Started event!").queue();
            return;
        }

        if (args[1].equals("stop")) {
            String messageId = e.getChannel().sendMessage("...").complete().getId();
            main.getApiUtil().setEventData(new JSONArray());
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

        StringBuilder messageBuilder = new StringBuilder(message + "\n");
//        List<JSONObject> leaderboardList = eventProgress.toList().stream()
//                .filter(object -> object instanceof JSONObject)
//                .map(object -> (JSONObject) object)
//                .sorted(Comparator.comparingDouble(jsonObject -> jsonObject.getInt(key)))
//                .collect(Collectors.toList());

        JSONArray leaderboardList = sortPlayerProgressArray(eventProgress, key);

        for (int i = 0; i < leaderboardList.length(); i++) {
            JSONObject player = leaderboardList.getJSONObject(i);
            messageBuilder.append("#").append(i + 1).append(" ").append(player.getString("displayName").replace("_", "\\_")).append(": ").append(main.getLangUtil().addNotation(player.getJSONObject("playerProgress").getInt(key))).append("\n");
        }
        message = messageBuilder.toString();

        List<String> messageSend = main.getLangUtil().processMessageForDiscord(message, 2000);

        ArrayList<String> res = new ArrayList<>();
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
            messageId = channel.sendMessage("Loading... (" + main.getLangUtil().getProgressBar(0, 50) + ")").complete().getId();
        }

        JSONArray data = main.getApiUtil().getEventData();

        for (int i = 0; i < data.length(); i++) {
            if (i % 3 == 0 && showLoadingMsg) {
                channel.editMessageById(messageId, "Loading... (" + main.getLangUtil().getProgressBar((double) i / data.length(), 50) + ")").queue();
            }

            JSONObject memberDataJSON = data.getJSONObject(i);
            LeaderboardValues memberData = LeaderboardValues.fromJSON(memberDataJSON);
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

            SlayerExp slayer = main.getApiUtil().getPlayerSlayerExp(memberDataJSON.getString("uuid"));

            playerProgress.put("slayerZombie", slayer.getZombie() - memberData.getSlayerExp().getZombie());
            playerProgress.put("slayerSpider", slayer.getSpider() - memberData.getSlayerExp().getSpider());
            playerProgress.put("slayerWolf", slayer.getWolf() - memberData.getSlayerExp().getWolf());
            playerProgress.put("slayerTotal", slayer.getTotalExp() - memberData.getSlayerExp().getTotalExp());

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
                SkillLevels skillLevels = main.getApiUtil().getBestPlayerSkillLevels(memberDataJSON.getString("uuid"));

                int totalSkillProgress = 0;
                playerProgress.put("skillAlchemy", (main.getSBUtil().toSkillExp(skillLevels.getAlchemy())) - memberData.getSkillLevels().getAlchemy());
                totalSkillProgress += playerProgress.getInt("skillAlchemy");
                playerProgress.put("skillCombat", main.getSBUtil().toSkillExp(skillLevels.getCombat()) - memberData.getSkillLevels().getCombat());
                totalSkillProgress += playerProgress.getInt("skillCombat");
                playerProgress.put("skillEnchanting", main.getSBUtil().toSkillExp(skillLevels.getEnchanting()) - memberData.getSkillLevels().getEnchanting());
                totalSkillProgress += playerProgress.getInt("skillEnchanting");
                playerProgress.put("skillFarming", main.getSBUtil().toSkillExp(skillLevels.getFarming()) - memberData.getSkillLevels().getFarming());
                totalSkillProgress += playerProgress.getInt("skillFarming");
                playerProgress.put("skillFishing", main.getSBUtil().toSkillExp(skillLevels.getFishing()) - memberData.getSkillLevels().getFishing());
                totalSkillProgress += playerProgress.getInt("skillFishing");
                playerProgress.put("skillForaging", main.getSBUtil().toSkillExp(skillLevels.getForaging()) - memberData.getSkillLevels().getForaging());
                totalSkillProgress += playerProgress.getInt("skillForaging");
                playerProgress.put("skillMining", main.getSBUtil().toSkillExp(skillLevels.getMining()) - memberData.getSkillLevels().getMining());
                totalSkillProgress += playerProgress.getInt("skillMining");
                playerProgress.put("skillTaming", main.getSBUtil().toSkillExp(skillLevels.getTaming()) - memberData.getSkillLevels().getTaming());
                totalSkillProgress += playerProgress.getInt("skillTaming");
                playerProgress.put("skillCarpentry", main.getSBUtil().toSkillExp(skillLevels.getCarpentry()) - memberData.getSkillLevels().getCarpentry());
                playerProgress.put("skillRunecrafting", main.getSBUtil().toSkillExpRunecrafting(skillLevels.getRunecrafting()) - memberData.getSkillLevels().getRunecrafting());
                playerProgress.put("skillTotal", totalSkillProgress);
            }
            memberDataJSON.put("playerProgress", playerProgress);
            data.remove(i);
            addToPos(i, memberDataJSON, data);
        }

        if (showLoadingMsg) {
            channel.deleteMessageById(messageId).queue();
        }

        return data;
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