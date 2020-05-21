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
        for (String profile : thePlayer.getSkyblockProfiles()) {
            SkyblockProfile skyblockProfile = main.getApiUtil().getSkyblockProfileByProfileUUID(profile);

            Map<String, Double> personalBankMap = new HashMap<>();
            EmbedBuilder profileEmbed = new EmbedBuilder();

            for (BankTransaction transaction : skyblockProfile.getBankHistory()) {
                if (transaction.getType() == TransactionType.DEPOSIT) {
                    personalBankMap.put(transaction.getInitiatorName(), personalBankMap.get(transaction.getInitiatorName()) + transaction.getAmount());
                } else if (transaction.getType() == TransactionType.WITHDRAW) {
                    personalBankMap.put(transaction.getInitiatorName(), personalBankMap.get(transaction.getInitiatorName()) - transaction.getAmount());
                }
            }
        }
    }
}
