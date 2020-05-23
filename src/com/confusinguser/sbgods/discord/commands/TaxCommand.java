package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.TaxPayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaxCommand extends Command {

    public TaxCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "tax";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, DiscordServer currentDiscordServer, String[] args) {
        if (args.length <= 1) {
            String mcName = main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag());
            if (mcName.equalsIgnoreCase("")) {
                e.getChannel().sendMessage("You must verify your minecraft account to use this command! Run `-v <IGN>`").queue();
                return;
            }

            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            Player thePlayer = main.getApiUtil().getPlayerFromUsername(mcName);

            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();
            TaxPayer taxPayer = main.getApiUtil().getTaxPayer(thePlayer);

            e.getChannel().deleteMessageById(messageId).queue();

            e.getChannel().sendMessage("Your tax info:").queue();
            e.getChannel().sendMessage(taxPayer.getDiscordEmbed().build()).queue();
            e.getChannel().sendMessage("Other arguments you can use are: `owelist`, `info` and `setrole`, `paid (admin)`, `paidall (admin)`, `owe (admin)`, `oweall (admin)`!").queue();
            return;
        }

        if (args[1].equalsIgnoreCase("paid")) {
            if (e.getMember() != null && !e.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                e.getChannel().sendMessage("You do not have permission to use this command!").queue();
                return;
            }

            if (args.length <= 3) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " paid <IGN> <AMOUNT>`!").queue();
                return;
            }

            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[2]);
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(0.5, 20) + ")").queue();
            TaxPayer taxPayer = main.getApiUtil().getTaxPayer(thePlayer);

            taxPayer.addOwes(-Integer.parseInt(args[3]));
            taxPayer.sendDataToServer();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();

            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage("Success! The player's current tax info:").queue();
            e.getChannel().sendMessage(taxPayer.getDiscordEmbed().build()).queue();
            return;
        }

        if (args[1].equalsIgnoreCase("paidall")) {
            if (e.getMember() != null && !e.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                e.getChannel().sendMessage("You do not have permission to use this command!").queue();
                return;
            }

            if (args.length <= 2) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " paidall <amount> [role]`!").queue();
                return;
            }

            int amount = -Integer.parseInt(args[2]);
            String role = "";
            if (args.length == 4) {
                role = args[3];
            }

            ArrayList<Player> guildMembers = main.getApiUtil().getGuildMembers(HypixelGuild.SBG);
            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 30) + ")").complete().getId();

            int i = 0;

            JSONObject taxData = main.getApiUtil().getTaxData();

            for (Player guildMember : guildMembers) {
                i++;

                e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(i / (double) guildMembers.size(), 30) + ")").queue();

                if (!taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").has(guildMember.getUUID())) {
                    Player player = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());


                    TaxPayer taxPayer = main.getApiUtil().getTaxPayer(player);


                    if (role.equalsIgnoreCase("") || taxPayer.getRole().equalsIgnoreCase(role)) {
                        taxPayer.addOwes(amount);

                        try {
                            taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").remove(guildMember.getUUID());
                        } catch (JSONException ignore) {
                        }

                        taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").put(guildMember.getUUID(), taxPayer.getJSON());

                        main.getApiUtil().setTaxData(taxData);
                    }
                } else {
                    String memberRole = taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(guildMember.getUUID()).getString("role");
                    if (role.equalsIgnoreCase("") || memberRole.equalsIgnoreCase(role)) {
                        taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(guildMember.getUUID()).put("owes", taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(guildMember.getUUID()).getInt("owes") + amount);
                    }
                }

            }

            main.getApiUtil().setTaxData(taxData);

            e.getChannel().deleteMessageById(messageId).queue();

            e.getChannel().sendMessage("Successfully set everyone as paid!").queue();

            return;
        }

        if (args[1].equalsIgnoreCase("owe")) {
            if (args.length <= 2) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " owe <IGN>`!").queue();
                return;
            }

            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[2]);
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(0.5, 20) + ")").queue();
            if (thePlayer.getUUID() == null) {
                e.getChannel().deleteMessageById(messageId).queue();
                e.getChannel().sendMessage("**" + args[2] + "** is not a Hypixel player!").queue();
                return;
            }

            TaxPayer taxPayer = main.getApiUtil().getTaxPayer(thePlayer);

            taxPayer.addOwes(Integer.parseInt(args[3]));

            taxPayer.sendDataToServer();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();

            e.getChannel().deleteMessageById(messageId).queue();

            e.getChannel().sendMessage("Success, this is the players current tax info:").queue();

            e.getChannel().sendMessage(taxPayer.getDiscordEmbed().build()).queue();

            return;
        }

        if (args[1].equalsIgnoreCase("oweall")) {
            if (e.getMember() != null && !e.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                e.getChannel().sendMessage("You do not have permission to use this command!").queue();
                return;
            }

            if (args.length < 3) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " oweall <AMOUNT> [group]`!").queue();
                return;
            }
            String role = "";
            int amount = Integer.parseInt(args[2]);
            if (args.length == 4) {
                role = args[3].toLowerCase();
            }

            ArrayList<Player> guildMembers = main.getApiUtil().getGuildMembers(HypixelGuild.SBG);
            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 30) + ")").complete().getId();

            int i = 0;

            JSONObject taxData = main.getApiUtil().getTaxData();

            for (Player guildMember : guildMembers) {
                i++;

                e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(i / (double) guildMembers.size(), 30) + ")").queue();

                if (!taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").has(guildMember.getUUID())) {
                    Player player = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());

                    TaxPayer taxPayer = main.getApiUtil().getTaxPayer(player);

                    if (role.equalsIgnoreCase("") || taxPayer.getRole().equalsIgnoreCase(role)) {
                        taxPayer.addOwes(amount);

                        try {
                            taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").remove(guildMember.getUUID());
                        } catch (JSONException ignore) {
                        }

                        taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").put(guildMember.getUUID(), taxPayer.getJSON());

                        main.getApiUtil().setTaxData(taxData);
                    }
                } else {
                    String memberRole = taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(guildMember.getUUID()).getString("role");
                    if (role.equalsIgnoreCase("") || memberRole.equalsIgnoreCase(role)) {
                        taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(guildMember.getUUID()).put("owes", taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(guildMember.getUUID()).getInt("owes") + amount);
                    }
                }

            }

            main.getApiUtil().setTaxData(taxData);

            e.getChannel().deleteMessageById(messageId).queue();

            e.getChannel().sendMessage("Successfully taxed everyone!").queue();

            return;
        }
        if (args[1].equalsIgnoreCase("info")) {
            if (args.length <= 2) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " info <IGN>`!").queue();
                return;
            }

            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[2]);
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();

            TaxPayer taxPayer = main.getApiUtil().getTaxPayer(thePlayer);

            e.getChannel().deleteMessageById(messageId).queue();

            e.getChannel().sendMessage(taxPayer.getDiscordEmbed().build()).queue();
            return;
        }

        if (args[1].equalsIgnoreCase("owelist") || args[1].equalsIgnoreCase("list")) {
            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            JSONObject taxData = main.getApiUtil().getTaxData();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(0.9, 20) + ")").queue();

            ArrayList<String> playerUuids = new ArrayList<>(taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").keySet());

            ArrayList<TaxPayer> taxPayers = new ArrayList<>();
            for (String playerUuid : playerUuids) {
                JSONObject taxPayerJson = taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(playerUuid);

                if (taxPayerJson != null) {
                    if (taxPayerJson.getInt("owes") > 0 || args.length >= 3) {
                        taxPayers.add(new TaxPayer(playerUuid, taxPayerJson.getString("name"), HypixelGuild.SBG.getGuildId(), taxPayerJson, main));
                    }
                }
            }

            taxPayers.sort((taxPayer, taxPayerOther) -> Integer.compare(taxPayerOther.getOwes(), taxPayer.getOwes()));

            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();
            e.getChannel().deleteMessageById(messageId).queue();

            StringBuilder message = new StringBuilder();
            for (TaxPayer taxPayer : taxPayers) {
                message.append(main.getDiscord().escapeMarkdown(taxPayer.getName())).append(" owes **").append(taxPayer.getOwes()).append("**\n");
            }

            e.getChannel().deleteMessageById(messageId).queue();
            if (message.toString().equals("")) {
                e.getChannel().sendMessage("No one in the guild owes any tax!").queue();
                return;
            }

            List<String> responseList = main.getUtil().processMessageForDiscord(message.toString(), 2000);
            for (String messageI : responseList) {
                e.getChannel().sendMessage(new EmbedBuilder().appendDescription(messageI).build()).queue();
            }

            return;
        }

        if (args[1].equalsIgnoreCase("setrole")) {
            if (args.length < 4) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " setrole <IGN> <ROLE>`!").queue();
                return;
            }

            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[2]);
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(0.5, 20) + ")").queue();
            TaxPayer taxPayer = main.getApiUtil().getTaxPayer(thePlayer);
            taxPayer.setRole(args[3].toLowerCase());
            taxPayer.sendDataToServer();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();

            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage(taxPayer.getDiscordEmbed().build()).queue();
            return;
        }
        e.getChannel().sendMessage("Invalid argument! Valid arguments: `paid`, `paidall`, `owe`, `oweall`, `owelist`, `info`, `setrole`!").queue();
    }
}