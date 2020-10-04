package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkyblockProfile;
import com.confusinguser.sbgods.entities.leaderboard.BankBalance;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public class BankCommand extends Command {

    public BankCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "bank";
        this.aliases = new String[]{"b"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (main.getLeaderboardUtil().cannotRunLeaderboardCommandInChannel(e.getChannel(), currentDiscordServer))
            return;

        if (args.length == 1) {
            player(e.getChannel(), main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag()));
            return;
        }

        boolean spreadsheet = false;
        if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
            spreadsheet = true;
        }

        if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("lb")) {
            List<Player> guildMemberUuids = main.getApiUtil().getGuildMembers(currentDiscordServer.getHypixelGuild());
            Map<Player, BankBalance> playerStatMap = (Map<Player, BankBalance>) main.getLeaderboardUtil().convertPlayerStatMap(
                    currentDiscordServer.getHypixelGuild().getPlayerStatMap(),
                    entry -> entry.getValue().getBankBalance());

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

            List<Map.Entry<Player, BankBalance>> leaderboardList = (List<Map.Entry<Player, BankBalance>>) main.getLeaderboardUtil().sortLeaderboard(playerStatMap, topX);

            StringBuilder response = new StringBuilder();
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                for (Entry<Player, BankBalance> currentEntry : leaderboardList) {
                    response.append("**#").append(leaderboardList.indexOf(currentEntry) + 1).append("** *").append(currentEntry.getKey().getDisplayName()).append(":* ").append(Math.round(currentEntry.getValue().getValue())).append("\n\n");
                }
            } else {
                int totalCoins = 0;

                for (Entry<Player, BankBalance> currentEntry : leaderboardList) {
                    response.append("**#").append(leaderboardList.indexOf(currentEntry) + 1).append("** *").append(currentEntry.getKey().getDisplayName()).append(":* ").append(main.getLangUtil().addNotation(currentEntry.getValue().getValue())).append("\n\n");
                    totalCoins += currentEntry.getValue().getValue();
                }

                // Print average coins
                if (topX == guildMemberUuids.size())
                    response.append("**Average coins per player: ");
                else
                    response.append("**Average coins per player top #").append(topX).append(": ");
                response.append(main.getLangUtil().addNotation((double) totalCoins / topX)).append("**").append('\n');

                // Print total coins
                if (topX == guildMemberUuids.size())
                    response.append("**Total coins of people in the guild: ");
                else
                    response.append("**Total coins of top #").append(topX).append(": ");
                response.append(main.getLangUtil().addNotation(totalCoins)).append("**");
            }

            String responseString = response.toString();
            // Split the message every 2000 characters in a nice looking way because of discord limitations
            List<String> responseList = main.getLangUtil().processMessageForDiscord(responseString, 2000);
            main.getLeaderboardUtil().sendLeaderboard(responseList, "Average Skill XP Leaderboard", e.getChannel(), spreadsheet);
        } else {
            player(e.getChannel(), args[1]);
        }
    }

    public void player(MessageChannel channel, String playerName) {
        Player thePlayer = main.getApiUtil().getPlayerFromUsername(playerName);
        double totalCoins = 0;
        StringBuilder description = new StringBuilder();
        List<String> profilesWithBankingApiOff = new ArrayList<>();
        for (SkyblockProfile profile : main.getApiUtil().getSkyblockProfilesByPlayerUUID(thePlayer.getUUID())) {
            if (profile.getBalance().getCoins() == -1) {
                profilesWithBankingApiOff.add(profile.getCuteName());
                continue;
            }
            totalCoins += profile.getBalance().getCoins();
            description.append("**").append(profile.getCuteName()).append("**  ").append(profile.getBalance().getCoins()).append('\n');
        }
        for (String profileName : profilesWithBankingApiOff) {
            description.append("**").append(profileName).append("**  Banking API Off\n");
        }
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(main.getLangUtil().makePossessiveForm(thePlayer.getDisplayName()) + " coins");
        embed.setColor(0xfcd303);
        embed.addField("Total coins", main.getLangUtil().addNotation(Math.round(totalCoins)) + " coins", false);
        embed.addField("Profiles", description.substring(0, description.toString().length() - 1) /* Remove trailing \n */, false);
        channel.sendMessage(embed.build()).queue();
    }
}