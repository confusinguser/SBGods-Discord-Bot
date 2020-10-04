package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.LeaderboardValue;
import com.confusinguser.sbgods.entities.leaderboard.SkillLevels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SkillCommand extends Command {

    public SkillCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "skill";
        this.aliases = new String[]{"skills"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (main.getLeaderboardUtil().cannotRunLeaderboardCommandInChannel(e.getChannel(), currentDiscordServer))
            return;

        if (args.length == 1) {
            player(e.getChannel(), main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag()));
            return;
        }

        if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("lb")) {
            List<Player> guildMemberUuids = main.getApiUtil().getGuildMembers(currentDiscordServer.getHypixelGuild());
            @SuppressWarnings("unchecked")
            Map<Player, SkillLevels> playerStatMap = (Map<Player, SkillLevels>) main.getLeaderboardUtil().convertPlayerStatMap(
                    currentDiscordServer.getHypixelGuild().getPlayerStatMap(),
                    entry -> entry.getValue().getSkillLevels());

            if (playerStatMap.size() == 0) {
                if (currentDiscordServer.getHypixelGuild().getLeaderboardProgress() == 0) {
                    e.getChannel().sendMessage(main.getMessageByKey("bot_is_still_indexing_names")).queue();
                } else {
                    e.getChannel().sendMessage(String.format(main.getMessageByKey("bot_is_still_indexing_names_progress"), currentDiscordServer.getHypixelGuild().getLeaderboardProgress(), currentDiscordServer.getHypixelGuild().getPlayerSize())).queue();
                }
                return;
            }

            int topX = main.getLeaderboardUtil().calculateTopXFromArgs(args, playerStatMap.size());
            if (topX < 0) {
                e.getChannel().sendMessage("**" + args[2] + "** is not a valid number!").queue();
                return;
            }

            List<? extends Entry<Player, ? extends LeaderboardValue>> leaderboardList = main.getLeaderboardUtil().sortLeaderboard(playerStatMap, topX);

            StringBuilder response = new StringBuilder();
            boolean spreadsheet = false;
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                spreadsheet = true;
            }
            if (spreadsheet) {
                for (Entry<Player, ? extends LeaderboardValue> currentEntry : leaderboardList) {
                    if (!currentEntry.getValue().isApproximate()) {
                        response.append(currentEntry.getKey().getDisplayName()).append("    ").append(main.getUtil().round(currentEntry.getValue().getValue(), 2)).append("\n");
                    }
                }
            } else {
                int totalAvgSkillLvl = 0;
                for (Entry<Player, ? extends LeaderboardValue> currentEntry : leaderboardList) {
                    response.append("**#").append(leaderboardList.indexOf(currentEntry) + 1).append("** *").append(currentEntry.getKey().getDisplayName()).append(":* ").append(main.getUtil().round(currentEntry.getValue().getValue(), 2));
                    if (currentEntry.getValue().isApproximate()) {
                        response.append(" *(appr.)*");
                    }
                    response.append("\n");
                    totalAvgSkillLvl += currentEntry.getValue().getValue();
                }
                if (topX == guildMemberUuids.size())
                    response.append("\n**Average guild skill level: ");
                else
                    response.append("\n**Average skill level top #").append(topX).append(": ");
                response.append(main.getUtil().round((double) totalAvgSkillLvl / topX, 2)).append("**");
            }

            String responseString = response.toString();
            // Split the message every 2000 characters in a nice looking way because of discord limitations
            List<String> responseList = main.getLangUtil().processMessageForDiscord(responseString, 2000);
            main.getLeaderboardUtil().sendLeaderboard(responseList, "Average Skill Level Leaderboard", e.getChannel(), spreadsheet);
        } else {
            player(e.getChannel(), args[1]);
        }
    }

    private void player(MessageChannel channel, String playerName) {
        Player thePlayer = main.getApiUtil().getPlayerFromUsername(playerName);

        if (thePlayer.getSkyblockProfiles().isEmpty()) {
            channel.sendMessage("Player **" + playerName + "** has never played skyblock!").queue();
            return;
        }

        SkillLevels skillLevels = main.getApiUtil().getBestPlayerSkillLevels(thePlayer.getUUID());

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0x03731d).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " skill levels");
        StringBuilder descriptionBuilder = embedBuilder.getDescriptionBuilder();

        if (skillLevels.isApproximate()) {
            descriptionBuilder.append("Approximate average skill level: **").append(main.getUtil().round(skillLevels.getAvgSkillLevel(), 3)).append("**\n\n");
        } else {
            descriptionBuilder.append("Average skill level: **").append(main.getUtil().round(skillLevels.getAvgSkillLevel(), 3)).append("**\n\n");
        }

        descriptionBuilder
                .append("Farming: **").append(skillLevels.getFarming()).append("**\n")
                .append("Mining: **").append(skillLevels.getMining()).append("**\n")
                .append("Combat: **").append(skillLevels.getCombat()).append("**\n")
                .append("Foraging: **").append(skillLevels.getForaging()).append("**\n")
                .append("Fishing: **").append(skillLevels.getFishing()).append("**\n")
                .append("Enchanting: **").append(skillLevels.getEnchanting()).append("**\n");
        if (!skillLevels.isApproximate())
            descriptionBuilder.append("Taming: **").append(skillLevels.getTaming()).append("**\n");
        descriptionBuilder.append("Alchemy: **").append(skillLevels.getAlchemy()).append("**\n");

        embedBuilder.setDescription(descriptionBuilder.toString());

        StringBuilder footerBuilder = new StringBuilder();
        embedBuilder.setFooter(footerBuilder
                .append("Carpentry: ").append(skillLevels.getCarpentry())
                .append(", runecrafting: ").append(skillLevels.getRunecrafting())
                .toString());

        channel.sendMessage(embedBuilder.build()).queue();
    }
}