package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkyblockProfile;
import com.confusinguser.sbgods.entities.banking.BankTransaction;
import com.confusinguser.sbgods.entities.banking.TransactionType;
import com.confusinguser.sbgods.entities.leaderboard.BankBalance;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (currentDiscordServer.getBotChannelId() != null && !e.getChannel().getId().contentEquals(currentDiscordServer.getBotChannelId())) {
            e.getChannel().sendMessage(main.getMessageByKey("command_cannot_be_used_on_server")).queue();
            return;
        }

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
            Map<Player, BankBalance> usernameTotalCoinsMap = currentDiscordServer.getHypixelGuild().getTotalCoinsMap();

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

            List<Entry<Player, BankBalance>> leaderboardList = usernameTotalCoinsMap.entrySet().stream()
                    .sorted(Comparator.comparingDouble(entry -> -entry.getValue().getValue()))
                    .collect(Collectors.toList())
                    .subList(0, topX - 1);

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
            List<String> responseList = main.getUtil().processMessageForDiscord(responseString, 2000);
            for (int j = 0; j < responseList.size(); j++) {
                String message = responseList.get(j);
                if (j == 0 && !spreadsheet) {
                    e.getChannel().sendMessage(message).queue();
                } else {
                    if (spreadsheet) {
                        e.getChannel().sendMessage("```arm\n" + message + "```").queue();
                    } else {
                        e.getChannel().sendMessage(new EmbedBuilder().setTitle("Total Coins Leaderboard:").setDescription(message).build()).queue();
                    }
                }
            }
        } else {
            player(e.getChannel(), args[1]);
        }
    }

    public void player(MessageChannel channel, String playerName) {
        Player thePlayer = main.getApiUtil().getPlayerFromUsername(playerName);
        boolean bankingApi = false;
        for (String profile : thePlayer.getSkyblockProfiles()) {
            SkyblockProfile skyblockProfile = main.getApiUtil().getSkyblockProfileByProfileUUID(profile);
            if (skyblockProfile.getBankHistory().isEmpty()) {
                continue;
            }
            bankingApi = true;

            Map<String, BankBalance> personalBankMap = new HashMap<>();
            Map<String, BankBalance> personalBankMapWithoutForegin = new HashMap<>();
            EmbedBuilder profileEmbed = new EmbedBuilder();

            for (Player member : skyblockProfile.getMembers()) {
                personalBankMap.put(member.getDisplayName(), new BankBalance(0d));
                personalBankMapWithoutForegin.put(member.getDisplayName(), new BankBalance(0d));
            }

            double amountInterest = 0;
            for (BankTransaction transaction : skyblockProfile.getBankHistory()) {
                if (!personalBankMap.containsKey(transaction.getInitiatorName())) { // If it's a foreign transaction (ex-coop-member or bank interest)
                    for (Player member : skyblockProfile.getMembers()) {
                        if (transaction.getType() == TransactionType.DEPOSIT) {
                            personalBankMap.put(member.getDisplayName(), new BankBalance(personalBankMap.get(member.getDisplayName()).getBalance() + (transaction.getAmount() / skyblockProfile.getMembers().size())));
                        } else if (transaction.getType() == TransactionType.WITHDRAW) {
                            personalBankMap.put(member.getDisplayName(), new BankBalance(personalBankMap.get(member.getDisplayName()).getBalance() - (transaction.getAmount() / skyblockProfile.getMembers().size())));
                        }
                    }

                } else if (transaction.getType() == TransactionType.DEPOSIT) {
                    personalBankMap.put(transaction.getInitiatorName(), new BankBalance(personalBankMap.get(transaction.getInitiatorName()).getBalance() + transaction.getAmount()));
                    personalBankMapWithoutForegin.put(transaction.getInitiatorName(), new BankBalance(personalBankMapWithoutForegin.get(transaction.getInitiatorName()).getBalance() + transaction.getAmount()));
                } else if (transaction.getType() == TransactionType.WITHDRAW) {
                    personalBankMap.put(transaction.getInitiatorName(), new BankBalance(personalBankMap.get(transaction.getInitiatorName()).getBalance() - transaction.getAmount()));
                    personalBankMapWithoutForegin.put(transaction.getInitiatorName(), new BankBalance(personalBankMapWithoutForegin.get(transaction.getInitiatorName()).getBalance() - transaction.getAmount()));
                }
            }

            StringBuilder description = new StringBuilder();
            StringBuilder title = new StringBuilder();
            personalBankMap.entrySet().stream().sorted((entry, otherEntry) -> Double.compare(otherEntry.getValue().getBalance(), entry.getValue().getBalance()))
                    .forEach(entry -> {
                        description.append(entry.getKey()).append(" has ").append(entry.getValue().getBalance() < 0 ? "taken out " : "contributed ")
                                .append(main.getLangUtil().addNotation(Math.abs(entry.getValue().getBalance()))).append(" coins (")
                                .append(main.getLangUtil().addNotation(personalBankMapWithoutForegin.get(entry.getKey()).getBalance())).append(" coins without interest)\n");
                        title.append(entry.getKey()).append(", ");
                    });

            profileEmbed.setTitle(title.toString().substring(0, title.length() - 2));
            profileEmbed.addField("Total coins", main.getLangUtil().addNotation(skyblockProfile.getBalance()) + " coins", false);
            profileEmbed.addField("Members", description.toString(), false);
            channel.sendMessage(profileEmbed.build()).queue();
        }
        if (!bankingApi) {
            channel.sendMessage("Banking API is off for all profiles").queue();
        }
    }
}