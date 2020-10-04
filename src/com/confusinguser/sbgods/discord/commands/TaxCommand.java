package com.confusinguser.sbgods.discord.commands;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TaxCommand extends Command {

    public TaxCommand(SBGods main, DiscordBot discord) {
        this.main = main;
        this.discord = discord;
        this.name = "tax";
        this.aliases = new String[]{};
    }

    @Override
    public void handleCommand(MessageReceivedEvent e, @NotNull DiscordServer currentDiscordServer, @NotNull Member senderMember, String[] args) {
        if (!currentDiscordServer.getHypixelGuild().equals(HypixelGuild.SBG)) {
            e.getChannel().sendMessage(main.getMessageByKey("command_cannot_be_used_in_channel")).queue();
            return;
        }

        if (args.length <= 1) {
            String mcName = main.getApiUtil().getMcNameFromDisc(e.getAuthor().getAsTag());
            if (mcName.equalsIgnoreCase("")) {
                e.getChannel().sendMessage(main.getLangUtil().getMessageByKey("not_verfied")).queue();
                return;
            }

            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            Player thePlayer = main.getApiUtil().getPlayerFromUsername(mcName);

            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();
            TaxPayer taxPayer = main.getApiUtil().getTaxPayer(thePlayer);

            e.getChannel().deleteMessageById(messageId).queue();

            e.getChannel().sendMessage("Your tax info:").queue();
            e.getChannel().sendMessage(taxPayer.getDiscordEmbed()).queue();
            if (DiscordPerms.getPerms(senderMember).getPower() >= DiscordPerms.STAFF.getPower())
            e.getChannel().sendMessage("Other arguments you can use are: `owelist`, `paidlist`, `info` and `setrole (admin)`, `paid (admin)`, `paidall (admin)`, `owe (admin)`, `oweall (admin)`!").queue();
            return;
        }

        if (args[1].equalsIgnoreCase("paid")) {
            if (senderMember.getRoles().stream().map(Role::getName).noneMatch(roleName -> roleName.equals("Splash Tax Team"))) {
                e.getChannel().sendMessage("You do not have permission to use this command!").queue();
                return;
            }

            if (args.length <= 3) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " paid <IGN> <AMOUNT>`!").queue();
                return;
            }

            int amount = main.getLangUtil().parseIntegerWithSuffixes(args[3]);
            if (amount == Integer.MIN_VALUE) {
                e.getChannel().sendMessage("Invalid amount! Usage: `" + discord.commandPrefix + name + " paid <IGN> <AMOUNT>`!").queue();
                return;
            }
            amount *= -1;

            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[2]);
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(0.5, 20) + ")").queue();
            TaxPayer taxPayer = main.getApiUtil().getTaxPayer(thePlayer);

            taxPayer.addOwes(amount);
            taxPayer.sendDataToServer();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();

            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage("Success! The player's current tax info:").queue();
            e.getChannel().sendMessage(taxPayer.getDiscordEmbed()).queue();
            return;
        }

        if (args[1].equalsIgnoreCase("paidall")) {
            if (senderMember.getRoles().stream().map(Role::getName).noneMatch(roleName -> roleName.equals("Splash Tax Team"))) {
                e.getChannel().sendMessage("You do not have permission to use this command!").queue();
                return;
            }

            if (args.length <= 3) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " paidall <amount> [role]`!").queue();
                return;
            }

            int amount = main.getLangUtil().parseIntegerWithSuffixes(args[3]);
            if (amount == Integer.MIN_VALUE) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " paidall <amount> [role]`!").queue();
                return;
            }
            amount *= -1;
            String role = "";
            if (args.length == 4) {
                role = args[3];
            }

            List<Player> guildMembers = main.getApiUtil().getGuildMembers(HypixelGuild.SBG);
            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 30) + ")").complete().getId();

            int i = 0;

            JSONObject taxData = main.getApiUtil().getTaxData();

            for (Player guildMember : guildMembers) {
                i++;

                e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(i / (double) guildMembers.size(), 30) + ")").queue();

                if (!taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").has(guildMember.getUUID())) {
                    Player player = main.getApiUtil().getPlayerFromUUID(guildMember.getUUID());

                    TaxPayer taxPayer = main.getApiUtil().getTaxPayer(player);
                    if (role.isEmpty() || taxPayer.getRole().equalsIgnoreCase(role)) {
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
            if (senderMember.getRoles().stream().map(Role::getName).noneMatch(roleName -> roleName.equals("Splash Tax Team"))) {
                e.getChannel().sendMessage(main.getMessageByKey("no_permission")).queue();
                return;
            }

            if (args.length <= 3) {
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

            int amount = main.getLangUtil().parseIntegerWithSuffixes(args[3]);
            if (amount == Integer.MIN_VALUE) {
                e.getChannel().sendMessage("Invalid amount! Usage: `" + discord.commandPrefix + name + " paid <IGN> <AMOUNT>`!").queue();
                return;
            }

            taxPayer.addOwes(amount);
            taxPayer.sendDataToServer();
            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage("Success, this is the player's current tax info:").queue();
            e.getChannel().sendMessage(taxPayer.getDiscordEmbed()).queue();
            return;
        }

        if (args[1].equalsIgnoreCase("oweall")) {
            if (senderMember.getRoles().stream().map(Role::getName).noneMatch(roleName -> roleName.equals("Splash Tax Team"))) {
                e.getChannel().sendMessage("You do not have permission to use this command!").queue();
                return;
            }

            if (args.length < 3) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " oweall <AMOUNT> [group]`!").queue();
                return;
            }
            String role = "";

            int amount = main.getLangUtil().parseIntegerWithSuffixes(args[3]);
            if (amount == Integer.MIN_VALUE) {
                e.getChannel().sendMessage("Invalid amount! Usage: `" + discord.commandPrefix + name + " paid <IGN> <AMOUNT>`!").queue();
                return;
            }

            if (args.length == 4) {
                role = args[3].toLowerCase();
            }

            List<Player> guildMembers = main.getApiUtil().getGuildMembers(HypixelGuild.SBG);
            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 30) + ")").complete().getId();

            int i = 0;

            JSONObject taxData = main.getApiUtil().getTaxData();

            for (Player guildMember : guildMembers) {
                i++;
                if (i % 10 == 0) {
                    e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(i / (double) guildMembers.size(), 30) + ")").queue();
                }

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
            if (args.length <= 3) {
                e.getChannel().sendMessage("Invalid usage! Usage: `" + discord.commandPrefix + name + " info <IGN>`!").queue();
                return;
            }

            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            Player thePlayer = main.getApiUtil().getPlayerFromUsername(args[2]);
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();

            TaxPayer taxPayer = main.getApiUtil().getTaxPayer(thePlayer);

            e.getChannel().deleteMessageById(messageId).queue();

            e.getChannel().sendMessage(taxPayer.getDiscordEmbed()).queue();
            return;
        }

        if (args[1].equalsIgnoreCase("owelist") || args[1].equalsIgnoreCase("list")) {
            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            JSONObject taxData = main.getApiUtil().getTaxData();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(0.9, 20) + ")").queue();

            List<String> playerUuids = new ArrayList<>(taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").keySet());

            List<TaxPayer> taxPayers = new ArrayList<>();
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
                message.append(main.getDiscord().escapeMarkdown(taxPayer.getName())).append(" owes **").append(main.getLangUtil().addNotation(taxPayer.getOwes())).append("**\n");
            }

            e.getChannel().deleteMessageById(messageId).queue();
            if (message.toString().equals("")) {
                e.getChannel().sendMessage("No one in the guild owes any tax!").queue();
                return;
            }

            List<String> responseList = main.getLangUtil().processMessageForDiscord(message.toString(), 2000);
            for (String messageI : responseList) {
                e.getChannel().sendMessage(new EmbedBuilder().appendDescription(messageI).build()).queue();
            }

            return;
        }

        if (args[1].equalsIgnoreCase("paidlist")) {
            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            JSONObject taxData = main.getApiUtil().getTaxData();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(0.9, 20) + ")").queue();

            List<String> playerUuids = new ArrayList<>(taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").keySet());

            List<TaxPayer> taxPayers = new ArrayList<>();
            for (String playerUuid : playerUuids) {
                JSONObject taxPayerJson = taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(playerUuid);

                if (taxPayerJson != null) {
                    if (taxPayerJson.getInt("owes") <= 0 && !taxPayerJson.getString("role").equalsIgnoreCase("no-tax")) {
                        taxPayers.add(new TaxPayer(playerUuid, taxPayerJson.getString("name"), HypixelGuild.SBG.getGuildId(), taxPayerJson, main));
                    }
                }
            }

            taxPayers.sort((taxPayer, taxPayerOther) -> Integer.compare(taxPayerOther.getOwes(), taxPayer.getOwes()));

            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();
            e.getChannel().deleteMessageById(messageId).queue();

            if (taxPayers.isEmpty()) {
                e.getChannel().sendMessage("No one in the guild has paid any tax!").queue();
                return;
            }

            StringBuilder message = new StringBuilder();
            for (TaxPayer taxPayer : taxPayers) {
                message.append(main.getDiscord().escapeMarkdown(taxPayer.getName())).append(" owes **").append(main.getLangUtil().addNotation(taxPayer.getOwes())).append("**\n");
            }

            e.getChannel().deleteMessageById(messageId).queue();

            List<String> responseList = main.getLangUtil().processMessageForDiscord(message.toString(), 2000);
            for (String partMessage: responseList) {
                e.getChannel().sendMessage(new EmbedBuilder().appendDescription(partMessage).build()).queue();
            }

            return;
        }

        if (args[1].equalsIgnoreCase("prune")) {
            if (senderMember.getRoles().stream().map(Role::getName).noneMatch(roleName -> roleName.equals("Splash Tax Team"))) {
                e.getChannel().sendMessage("You do not have permission to use this command!").queue();
                return;
            }
            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            JSONObject taxData = main.getApiUtil().getTaxData();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(0.5, 20) + ")").queue();

            List<String> playerUuids = new ArrayList<>(taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").keySet());

            List<Player> SBGGuildMembers = main.getApiUtil().getGuildMembers(HypixelGuild.SBG);

            int playersRemoved = 0;
            for (String playerUuid : playerUuids) {
                boolean inGuild = false;

                for (Player player : SBGGuildMembers) {
                    if (player.getUUID().equals(playerUuid)) {
                        inGuild = true;
                        break;
                    }
                }

                if (!inGuild) {
                    e.getChannel().sendMessage("Removed tax data about " + taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(playerUuid).getString("name")).complete().delete().queueAfter(30, TimeUnit.SECONDS);
                    taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").remove(playerUuid);
                    playersRemoved++;
                }
            }

            main.getApiUtil().setTaxData(taxData);
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();
            e.getChannel().editMessageById(messageId, "Success, removed " + playersRemoved + " from the list").queue();

            return;
        }

        if (args[1].equalsIgnoreCase("clearowes")) {
            if (senderMember.getRoles().stream().map(Role::getName).noneMatch(roleName -> roleName.equals("Splash Tax Team"))) {
                e.getChannel().sendMessage("You do not have permission to use this command!").queue();
                return;
            }
            String messageId = e.getChannel().sendMessage("Loading (" + main.getLangUtil().getProgressBar(0.0, 20) + ")").complete().getId();

            JSONObject taxData = main.getApiUtil().getTaxData();
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(0.5, 20) + ")").queue();

            List<String> playerUuids = new ArrayList<>(taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").keySet());

            int playersUpdated = 0;
            for (String playerUuid : playerUuids) {

                long currOwes = taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(playerUuid).getLong("owes");

                if (currOwes > 0) {
                    e.getChannel().sendMessage("Removed tax owes data about " + taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(playerUuid).getString("name")).complete().delete().queueAfter(30, TimeUnit.SECONDS);

                    taxData.getJSONObject("guilds").getJSONObject(HypixelGuild.SBG.getGuildId()).getJSONObject("members").getJSONObject(playerUuid).put("owes", 0);

                    playersUpdated++;
                }
            }

            main.getApiUtil().setTaxData(taxData);
            e.getChannel().editMessageById(messageId, "Loading (" + main.getLangUtil().getProgressBar(1.0, 20) + ")").queue();
            e.getChannel().editMessageById(messageId, "Success, removed " + playersUpdated + " players tax owes data").queue();

            return;
        }

        if (args[1].equalsIgnoreCase("setrole")) {
            if (senderMember.getRoles().stream().map(Role::getName).noneMatch(roleName -> roleName.equals("Splash Tax Team"))) {
                e.getChannel().sendMessage("You do not have permission to use this command!").queue();
                return;
            }
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
            e.getChannel().deleteMessageById(messageId).queue();
            e.getChannel().sendMessage(taxPayer.getDiscordEmbed()).queue();
            return;
        }
        e.getChannel().sendMessage("Invalid argument! Valid arguments: `paid`, `paidall`, `owe`, `oweall`, `owelist`, `paidlist`, `info`, `setrole`!").queue();
    }
}