package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.SlayerExp;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public class SlayerCommand extends Command {

    public SlayerCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "slayer";
        this.aliases = new String[]{};
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
            Map<Player, SlayerExp> playerStatMap = (Map<Player, SlayerExp>) main.getLeaderboardUtil().convertPlayerStatMap(currentDiscordServer.getHypixelGuild().getPlayerStatMap(), entry -> entry.getValue().getSlayerExp());

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

            List<Entry<Player, SlayerExp>> leaderboardList = (List<Entry<Player, SlayerExp>>) main.getLeaderboardUtil().sortLeaderboard(playerStatMap, topX);

            StringBuilder response = new StringBuilder();
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                for (Entry<Player, SlayerExp> currentEntry : leaderboardList) {
                    response.append(currentEntry.getKey().getDisplayName()).append("    ").append(main.getLangUtil().addNotation(Math.round(currentEntry.getValue().getTotalExp()))).append("\n");
                }
            } else {
                int totalSlayer = 0;
                for (Entry<Player, SlayerExp> currentEntry : leaderboardList) {
                    response.append("**#").append(leaderboardList.indexOf(currentEntry) + 1).append("** *").append(currentEntry.getKey().getDisplayName()).append(":* ").append(main.getLangUtil().addNotation(currentEntry.getValue().getTotalExp())).append("\n");
                    totalSlayer += currentEntry.getValue().getTotalExp();
                }
                if (topX == guildMemberUuids.size())
                    response.append("\n**Average guild slayer exp: ");
                else
                    response.append("\n**Average slayer exp top #").append(topX).append(": ");
                response.append(main.getLangUtil().addNotation(Math.round((double) totalSlayer / topX))).append("**");
            }

            String responseString = response.toString();
            // Split the message every 2000 characters in a nice looking way because of discord limitations
            List<String> responseList = main.getLangUtil().processMessageForDiscord(responseString, 2000);

            boolean spreadsheet = false;
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                spreadsheet = true;
            }

            main.getLeaderboardUtil().sendLeaderboard(responseList, "Total Slayer XP Leaderboard", e.getChannel(), spreadsheet);
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

        SlayerExp playerSlayerExp = main.getApiUtil().getPlayerSlayerExp(thePlayer);

        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(0x51047d).setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " Slayer XP");
        embedBuilder.addField("Total Slayer XP: ", main.getLangUtil().addCommas(playerSlayerExp.getTotalExp()), false);
        embedBuilder.addField("Slayers",
                "**Zombie**  " + main.getLangUtil().addCommas(playerSlayerExp.getZombie()) + "\n" +
                        "**Spider**  " + main.getLangUtil().addCommas(playerSlayerExp.getSpider()) + "\n" +
                        "**Wolf**    " + main.getLangUtil().addCommas(playerSlayerExp.getWolf()) + "\n",
                false);

        channel.sendMessage(embedBuilder.build()).queue();
    }
}