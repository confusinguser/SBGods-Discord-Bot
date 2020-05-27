package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkyblockProfile;
import com.confusinguser.sbgods.entities.banking.BankTransaction;
import com.confusinguser.sbgods.entities.banking.TransactionType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BankCommand extends Command {

    public BankCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "bank";
        this.aliases = new String[]{"b"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (currentDiscordServer.getBotChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getBotChannelId())) {
            e.getChannel().sendMessage("Bank commands cannot be ran in this channel!").queue();
            return;
        }

        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid argument! Valid arguments: `leaderboard`, `player`!").queue();
            return;
        }

        boolean spreadsheet = false;
        if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
            spreadsheet = true;
        }

        if (args[1].equalsIgnoreCase("leaderboard") || args[1].equalsIgnoreCase("lb")) {
            ArrayList<Player> guildMemberUuids = main.getApiUtil().getGuildMembers(currentDiscordServer.getHypixelGuild());
            Map<String, Double> usernameTotalCoinsMap = currentDiscordServer.getHypixelGuild().getTotalCoinsMap();

            if (usernameTotalCoinsMap.size() == 0) {
                if (currentDiscordServer.getHypixelGuild().getLeaderboardProgress() == 0) {
                    e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (Please note that other leaderboards have a higher priority)").queue();
                } else {
                    e.getChannel().sendMessage("Bot is still indexing names, please try again in a few minutes! (" + currentDiscordServer.getHypixelGuild().getLeaderboardProgress() + " / " + currentDiscordServer.getHypixelGuild().getPlayerSize() + ")").queue();
                }
                return;
            }

            int topX = 10;
            if (args.length >= 3) {
                if (args[2].equalsIgnoreCase("all")) {
                    topX = guildMemberUuids.size();
                } else {
                    try {
                        topX = Math.min(guildMemberUuids.size(), Integer.parseInt(args[2]));
                    } catch (NumberFormatException exception) {
                        e.getChannel().sendMessage("**" + args[2] + "** is not a valid number!").queue();
                        return;
                    }
                }
            }

            List<Entry<String, Double>> leaderboardList = usernameTotalCoinsMap.entrySet().stream()
                    .sorted(Comparator.comparingDouble(Entry::getValue))
                    .collect(Collectors.toList())
                    .subList(0, topX - 1);

            StringBuilder response = new StringBuilder("**Total Coins Leaderboard:**\n\n");
            if (args.length >= 4 && args[3].equalsIgnoreCase("spreadsheet")) {
                for (Entry<String, Double> currentEntry : leaderboardList) {
                    response.append("**#").append(leaderboardList.indexOf(currentEntry)).append("** *").append(currentEntry.getKey()).append(":* ").append(Math.round(currentEntry.getValue())).append("\n\n");
                }
            } else {
                int totalCoins = 0;

                for (Entry<String, Double> currentEntry : leaderboardList) {
                    response.append("**#").append(leaderboardList.indexOf(currentEntry)).append("** *").append(currentEntry.getKey()).append(":* ").append(Math.round(currentEntry.getValue())).append("\n\n");
                    totalCoins += currentEntry.getValue();
                }

                // Print average coins
                if (topX == guildMemberUuids.size())
                    response.append("**Average guild coins: ");
                else
                    response.append("**Average coins top #").append(topX).append(": ");
                response.append(main.getUtil().round((double) totalCoins / topX, 2)).append("**").append('\n');

                // Print total coins
                if (topX == guildMemberUuids.size())
                    response.append("**Total coins of people in the guild: ");
                else
                    response.append("**Total coins of top #").append(topX).append(": ");
                response.append(totalCoins).append("**");
            }

            String responseString = response.toString();
            // Split the message every 2000 characters in a nice looking way because of discord limitations
            List<String> responseList = main.getUtil().processMessageForDiscord(responseString, 2000);
            for (int j = 0; j < responseList.size(); j++) {
                String message = responseList.get(j);
                if (j == 0 && !spreadsheet) {
                    e.getChannel().sendMessage(message).queue();
                } else {
                    if (spreadsheet) {
                        e.getChannel().sendMessage("```arm\n" + message + "```").queue();
                    } else {
                        e.getChannel().sendMessage("\u200E" + message).queue();
                    }
                }
            }
            return;
        }

        if (args[1].equalsIgnoreCase("player")) {
            Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[1]);
            boolean bankingApi = false;
            for (String profile : thePlayer.getSkyblockProfiles()) {
                SkyblockProfile skyblockProfile = main.getApiUtil().getSkyblockProfileByProfileUUID(profile);
                if (skyblockProfile.getBankHistory().isEmpty()) {
                    continue;
                }
                bankingApi = true;

                Map<String, Double> personalBankMap = new HashMap<>();
                Map<String, Double> personalBankMapWithoutForegin = new HashMap<>();
                EmbedBuilder profileEmbed = new EmbedBuilder();

                for (Player member : skyblockProfile.getMembers()) {
                    personalBankMap.put(member.getDisplayName(), 0d);
                    personalBankMapWithoutForegin.put(member.getDisplayName(), 0d);
                }

                double amountInterest = 0;
                for (BankTransaction transaction : skyblockProfile.getBankHistory()) {
                    if (!personalBankMap.containsKey(transaction.getInitiatorName())) { // If it's a foreign transaction (ex-coop-member or bank interest)
                        for (Player member : skyblockProfile.getMembers()) {
                            if (transaction.getType() == TransactionType.DEPOSIT) {
                                personalBankMap.put(member.getDisplayName(), personalBankMap.get(member.getDisplayName()) + (transaction.getAmount() / skyblockProfile.getMembers().size()));
                            } else if (transaction.getType() == TransactionType.WITHDRAW) {
                                personalBankMap.put(member.getDisplayName(), personalBankMap.get(member.getDisplayName()) - (transaction.getAmount() / skyblockProfile.getMembers().size()));
                            }
                        }

                    } else if (transaction.getType() == TransactionType.DEPOSIT) {
                        personalBankMap.put(transaction.getInitiatorName(), personalBankMap.get(transaction.getInitiatorName()) + transaction.getAmount());
                        personalBankMapWithoutForegin.put(transaction.getInitiatorName(), personalBankMapWithoutForegin.get(transaction.getInitiatorName()) + transaction.getAmount());
                    } else if (transaction.getType() == TransactionType.WITHDRAW) {
                        personalBankMap.put(transaction.getInitiatorName(), personalBankMap.get(transaction.getInitiatorName()) - transaction.getAmount());
                        personalBankMapWithoutForegin.put(transaction.getInitiatorName(), personalBankMapWithoutForegin.get(transaction.getInitiatorName()) - transaction.getAmount());
                    }
                }

                StringBuilder description = new StringBuilder();
                StringBuilder title = new StringBuilder();
                personalBankMap.entrySet().stream().sorted((entry, otherEntry) -> Double.compare(otherEntry.getValue(), entry.getValue()))
                        .forEach(entry -> {
                            description.append(entry.getKey()).append(" has ").append(entry.getValue() < 0 ? "taken out " : "contributed ")
                                    .append(main.getLangUtil().addNotation(Math.abs(entry.getValue()))).append(" coins (")
                                    .append(main.getLangUtil().addNotation(personalBankMapWithoutForegin.get(entry.getKey()))).append(" coins without interest)\n");
                            title.append(entry.getKey()).append(", ");
                        });

                profileEmbed.setTitle(title.toString().substring(0, title.length() - 2));
                profileEmbed.addField("Total money", main.getLangUtil().addNotation(skyblockProfile.getBalance()) + " coins", false);
                profileEmbed.addField("Members", description.toString(), false);
                e.getChannel().sendMessage(profileEmbed.build()).queue();
            }
            if (!bankingApi) {
                e.getChannel().sendMessage("Banking API is off for all profiles").queue();
            }
        }
    }
}