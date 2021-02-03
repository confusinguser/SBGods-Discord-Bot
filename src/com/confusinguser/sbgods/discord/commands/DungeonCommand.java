package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.DungeonExps;
import com.confusinguser.sbgods.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public class DungeonCommand extends Command {

    public DungeonCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "dungeon";
        this.aliases = new String[]{"dungeons"};
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
            Map<Player, DungeonExps> playerStatMap = (Map<Player, DungeonExps>) main.getLeaderboardUtil().convertPlayerStatMap(currentDiscordServer.getHypixelGuild().getPlayerStatMap(), entry -> entry.getValue().getDungeonLevels());

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

            List<Entry<Player, DungeonExps>> leaderboardList = (List<Entry<Player, DungeonExps>>) main.getLeaderboardUtil().sortLeaderboard(playerStatMap, topX);

            StringBuilder response = new StringBuilder();
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                for (Entry<Player, DungeonExps> currentEntry : leaderboardList) {
                    response.append(currentEntry.getKey().getDisplayName()).append("    ").append(Math.round(currentEntry.getValue().getAverageDungeonExp())).append("\n");
                }
            } else {
                int dungeonExp = 0;
                for (Entry<Player, DungeonExps> currentEntry : leaderboardList) {
                    response.append("**#").append(leaderboardList.indexOf(currentEntry) + 1).append("** *").append(currentEntry.getKey().getDisplayName()).append(":* ").append(main.getLangUtil().addNotation(currentEntry.getValue().getAverageDungeonExp())).append("\n");
                    dungeonExp += currentEntry.getValue().getAverageDungeonExp();
                }
                if (topX == guildMemberUuids.size())
                    response.append("\n**Average guild dungeon exp: ");
                else
                    response.append("\n**Average dungeon exp top #").append(topX).append(": ");
                response.append(main.getLangUtil().addNotation(Math.round((double) dungeonExp / topX))).append("**");
            }

            String responseString = response.toString();
            // Split the message every 2000 characters in a nice looking way because of discord limitations
            List<String> responseList = main.getLangUtil().processMessageForDiscord(responseString, 2000);

            boolean spreadsheet = false;
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                spreadsheet = true;
            }

            main.getLeaderboardUtil().sendLeaderboard(responseList, "Average Dungeon XP Leaderboard", e.getChannel(), spreadsheet);
        } else {
            player(e.getChannel(), args[1]);
        }
    }

    public void player(MessageChannel channel, String playerName) {
        Player thePlayer = main.getApiUtil().getPlayerFromUsername(playerName);
        if (thePlayer.getUUID() == null) {
            channel.sendMessage("Player **" + playerName + "** does not exist!").queue();
            return;
        }
        if (thePlayer.getSkyblockProfiles().isEmpty()) {
            channel.sendMessage("Player **" + playerName + "** has never played Skyblock!").queue();
            return;
        }

        DungeonExps dungeonExps = main.getApiUtil().getBestDungeonExpsForPlayer(thePlayer);

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0x51047d).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " Dungeon XP");
        embedBuilder.addField("Average Dungeon Level", "" + Util.round(main.getSBUtil().toSkillLevelDungeoneering(dungeonExps.getAverageDungeonExp()), 2), false);
        embedBuilder.addField("Dungeons and Classes",
                "**Catacombs** " + main.getLangUtil().addCommas(dungeonExps.getCatacombsExp()) + "\n" +
                        "**Healer**    " + main.getLangUtil().addCommas(dungeonExps.getHealerExp()) + "\n" +
                        "**Mage**      " + main.getLangUtil().addCommas(dungeonExps.getMageExp()) + "\n" +
                        "**Berserk**   " + main.getLangUtil().addCommas(dungeonExps.getBerserkExp()) + "\n" +
                        "**Archer**    " + main.getLangUtil().addCommas(dungeonExps.getArcherExp()) + "\n" +
                        "**Tank**      " + main.getLangUtil().addCommas(dungeonExps.getTankExp()) + "\n",
                false);

        channel.sendMessage(embedBuilder.build()).queue();
    }
}