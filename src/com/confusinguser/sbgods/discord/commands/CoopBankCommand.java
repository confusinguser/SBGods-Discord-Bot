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

import java.util.HashMap;
import java.util.Map;

public class CoopBankCommand extends Command {

    public CoopBankCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "coopbank";
        this.usage = this.getName() + " <IGN>";
        this.aliases = new String[]{"cb"};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (args.length <= 1) {
            e.getChannel().sendMessage("Invalid usage! Usage: " + usage).queue();
            return;
        }

        Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[1]);
        boolean bankingApi = false;
        for (String profile : thePlayer.getSkyblockProfiles()) {
            SkyblockProfile skyblockProfile = main.getApiUtil().getSkyblockProfileByProfileUUID(profile);
            if (skyblockProfile.getBankHistory().isEmpty()) {
                continue;
            }
            bankingApi = true;

            Map<String, Double> personalBankMap = new HashMap<>();
            EmbedBuilder profileEmbed = new EmbedBuilder();

            for (Player member : skyblockProfile.getMembers()) {
                personalBankMap.put(member.getDisplayName(), 0d);
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
                } else if (transaction.getType() == TransactionType.WITHDRAW) {
                    personalBankMap.put(transaction.getInitiatorName(), personalBankMap.get(transaction.getInitiatorName()) - transaction.getAmount());
                }
            }

            StringBuilder description = new StringBuilder();
            StringBuilder title = new StringBuilder();
            personalBankMap.entrySet().stream().sorted((entry, otherEntry) -> Double.compare(otherEntry.getValue(), entry.getValue()))
                    .forEach(entry -> {
                        description.append(entry.getKey()).append(" has ").append(entry.getValue() < 0 ? "taken out " : "contributed ").append(main.getLangUtil().addNotation(Math.abs(entry.getValue()))).append(" coins\n");
                        title.append(entry.getKey()).append(", ");
                    });

            profileEmbed.setTitle(title.toString().substring(0, title.length()-2));
            profileEmbed.addField("Total money", main.getLangUtil().addNotation(skyblockProfile.getBalance()) + " coins", false);
            profileEmbed.addField("Members", description.toString(), false);
            e.getChannel().sendMessage(profileEmbed.build()).queue();
        }
        if (!bankingApi) {
            e.getChannel().sendMessage("Banking API is off for all profiles").queue();
        }
    }
}
