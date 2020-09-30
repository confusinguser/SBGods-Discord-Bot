package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.HypixelRank;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.SkillLevels;
import com.confusinguser.sbgods.entities.leaderboard.SlayerExp;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {

    private final SBGods main;
    private final List<MessageChannel> typingChannels = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Pattern stripColorCodesRegex = Pattern.compile("§[a-f0-9rlkmn]");
    private final Map<InetAddress, String> latestMessageByIP = new HashMap<>();
    private String latestGuildmessageAuthor = "";
    private String latestGuildmessage = "";

    public Util(SBGods main) {
        this.main = main;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            synchronized (typingChannels) {
                for (MessageChannel channel : typingChannels) channel.sendTyping().queue();
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    public Entry<String, Integer> getHighestKeyValuePair(Map<String, Integer> map, int position) {
        List<Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        return list.get(position);
    }

    public Entry<String, SkillLevels> getHighestKeyValuePair(Map<String, SkillLevels> map, int position, boolean isSkillLevel) {
        if (!isSkillLevel) {
            return null;
        }

        List<Entry<String, SkillLevels>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> Double.compare(o2.getValue().getAvgSkillLevel(), o1.getValue().getAvgSkillLevel()));

        return list.get(position);
    }

    public Entry<String, SlayerExp> getHighestKeyValuePairForSlayerExp(Map<String, SlayerExp> map, int position) {
        List<Entry<String, SlayerExp>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> Integer.compare(o2.getValue().getTotalExp(), o1.getValue().getTotalExp()));

        return list.get(position);
    }

    public List<String> processMessageForDiscord(String message, int limit) {
        return processMessageForDiscord(message, limit, new ArrayList<>());
    }

    private List<String> processMessageForDiscord(String message, int limit, List<String> currentOutput) {
        if (message.length() > limit) {
            int lastIndex = 0;
            for (int index = message.indexOf('\n'); index >= 0; index = message.indexOf('\n', index + 1)) {
                if (index >= limit) {
                    currentOutput.add(message.substring(0, lastIndex));
                    message = message.substring(lastIndex);
                    return processMessageForDiscord(message, limit, currentOutput);
                }
                lastIndex = index;
            }
        } else {
            currentOutput.add(message);
        }
        return currentOutput;
    }

    public double getAverageFromDoubleList(List<Double> list) {
        double output = 0;
        for (Double aDouble : list) {
            output += aDouble;
        }
        output /= list.size();
        return output;
    }

    public double round(double num, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        num = num * factor;
        long tmp = Math.round(num);
        return (double) tmp / factor;
    }

    public double getAverageFromSkillLevelArray(SkillLevels[] skillLevels) {
        double output = 0;
        int length = 0;
        for (SkillLevels skillLevel : skillLevels) {
            if (!skillLevel.isApproximate()) {
                output += skillLevel.getAvgSkillLevel();
                length++;
            }
        }
        output /= length;
        return output;
    }


    public double getAverageFromSlayerExpArray(SlayerExp[] array) {
        double output = 0;
        for (SlayerExp slayerExp : array) {
            output += slayerExp.getTotalExp();
        }
        output /= array.length;
        return output;
    }

    /**
     * @param member  The discord {@link Member} object
     * @param mcName  The minecraft IGN
     * @param discord The discord {@link Guild} object
     * @param channel The discord {@link MessageChannel} object
     * @return 0 if player was already verified, 1 if player was not verified, or 2 if bot still hasn't loaded leaderboards
     */
    public int verifyPlayer(Member member, String mcName, Guild discord, MessageChannel channel) {
        try {
            if (!member.getEffectiveName().toLowerCase().contains(mcName.toLowerCase())) {
                if ((member.getEffectiveName() + " (" + mcName + ")").length() > 32)
                    member.modifyNickname(mcName).complete();
                else member.modifyNickname(member.getEffectiveName() + " (" + mcName + ")").complete();
            }
        } catch (HierarchyException ignored) {
        } // Don't have perms to change nick

        List<String> roleNames = discord.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        List<String> memberRoleNames = member.getRoles().stream().map(Role::getName).collect(Collectors.toList()); // Make a list of all role names the member has

        boolean gotVerified = false;
        for (Role role : discord.getRolesByName("verified", true)) {
            try {
                if (!member.getRoles().contains(role)) {
                    gotVerified = true;
                    discord.addRoleToMember(member, role).complete();
                }
            } catch (HierarchyException ignored) {
            }
        }

        // Add guild roles if they are in one
        Player thePlayer = main.getApiUtil().getPlayerFromUsername(mcName);

        String guildId = thePlayer.getGuildId();
        if (guildId == null) {
            guildId = "";
        }
        HypixelGuild guild = HypixelGuild.getGuildById(guildId);

        if (guild == HypixelGuild.SBG) {
            for (Player guildMember : main.getApiUtil().getGuildMembers(guild)) {
                if (guildMember.getUUID().equals(thePlayer.getUUID())) {
                    thePlayer = Player.mergePlayerAndGuildMember(thePlayer, guildMember);
                }
            }
        }

        for (Role role : discord.getRoles().stream().filter(role -> guild != null && guild.isAltNameIgnoreCase(role.getName())).collect(Collectors.toList())) {
            try {
                discord.addRoleToMember(member, role).queue();
            } catch (HierarchyException ignored) {
            }
        }
        // Remove all roles that are for other guilds
        for (Role role : member.getRoles().stream().filter(role -> HypixelGuild.getGuildByName(role.getName()) != guild && HypixelGuild.getGuildByName(role.getName()) != null).collect(Collectors.toList())) {
            try {
                discord.removeRoleFromMember(member, role).queue();
            } catch (HierarchyException ignored) {
            }
        }

        HypixelGuild hypixelGuild = HypixelGuild.getGuildById(guildId);
        DiscordServer discordServer = DiscordServer.getDiscordServerFromDiscordGuild(discord);
        if (hypixelGuild != null && discordServer != null && hypixelGuild == discordServer.getHypixelGuild()) {
            int highestLeaderboardPos = Math.min(thePlayer.getSkillPos(false), thePlayer.getSlayerPos(false));
            if (highestLeaderboardPos == -2) {
                return 2;
            }

            // Give Elite, SBK and SBG rank
            if (hypixelGuild == discordServer.getHypixelGuild()) {
                if (highestLeaderboardPos < 5) {
                    for (Role role : discord.getRolesByName("Skyblock God \uD83D\uDE4F" /* \uD83D\uDE4F = 🙏 */, true)) {
                        try {
                            discord.addRoleToMember(member, role).queue();
                        } catch (HierarchyException ignored) {
                        }
                    }
                } else {
                    for (Role role : discord.getRoles().stream()
                            .filter(role -> role.getName().toLowerCase().equals("skyblock god \uD83D\uDE4F"))
                            .collect(Collectors.toList())) {
                        try {
                            discord.removeRoleFromMember(member, role).queue();
                        } catch (HierarchyException ignored) {
                        }
                    }
                }
                if (highestLeaderboardPos < 15) {
                    for (Role role : discord.getRolesByName("Skyblock King \uD83D\uDC51" /* \uD83D\uDC51 = 👑 */, true)) {
                        try {
                            discord.addRoleToMember(member, role).queue();
                        } catch (HierarchyException ignored) {
                        }
                    }
                } else {
                    for (Role role : discord.getRoles().stream()
                            .filter(role -> role.getName().toLowerCase().equals("skyblock king \uD83D\uDC51") ||
                                    role.getName().toLowerCase().equals("skyblock god \uD83D\uDE4F"))
                            .collect(Collectors.toList())) {
                        try {
                            discord.removeRoleFromMember(member, role).queue();
                        } catch (HierarchyException ignored) {
                        }
                    }
                }
                if (highestLeaderboardPos < 45 && hypixelGuild != HypixelGuild.SBF) {
                    for (Role role : discord.getRolesByName("Elite", true)) {
                        try {
                            discord.addRoleToMember(member, role).queue();
                        } catch (HierarchyException ignored) {
                        }
                    }
                } else {
                    for (Role role : discord.getRoles().stream()
                            .filter(role -> role.getName().toLowerCase().equals("elite") ||
                                    role.getName().toLowerCase().equals("skyblock king \uD83D\uDC51") ||
                                    role.getName().toLowerCase().equals("skyblock god \uD83D\uDE4F"))
                            .collect(Collectors.toList())) {
                        try {
                            discord.removeRoleFromMember(member, role).queue();
                        } catch (HierarchyException ignored) {
                        }
                    }
                }

                String rankGiven = "Member";
                if (guild == HypixelGuild.SBG) { //highestLeaderboardPos is one higher than it needs to be so it is only less than not less than or equal to
                    if (highestLeaderboardPos < 45) {
                        rankGiven = "Elite";
                    }
                    if (highestLeaderboardPos < 15) {
                        rankGiven = "King";
                    }
                    if (highestLeaderboardPos < 5) {
                        rankGiven = "God";
                    }

                    JSONArray guildRanksChange = main.getApiUtil().getGuildRanksChange();

                    for (int i = 0; i < guildRanksChange.length(); i++) {
                        if (guildRanksChange.getJSONObject(i).getString("uuid").equals(thePlayer.getUUID())) {
                            guildRanksChange.remove(i);
                        }
                    }

                    if (thePlayer.getGuildRank() != null && !thePlayer.getGuildRank().contains(rankGiven) && (thePlayer.getGuildRank().contains("Member") || thePlayer.getGuildRank().contains("Elite") || thePlayer.getGuildRank().contains("King") || thePlayer.getGuildRank().contains("God"))) {
                        JSONObject newPlayerJson = new JSONObject();

                        newPlayerJson.put("uuid", thePlayer.getUUID());
                        newPlayerJson.put("name", thePlayer.getDisplayName());
                        newPlayerJson.put("currRank", thePlayer.getGuildRank());
                        newPlayerJson.put("needRank", rankGiven);

                        guildRanksChange.put(newPlayerJson);
                    }

                    main.getApiUtil().setGuildRanksChange(guildRanksChange);
                }
            }
        }

        if (gotVerified) {
            if (guild == null) {
                channel.sendMessage(main.getDiscord().escapeMarkdown("Linked " + member.getUser().getAsTag() + " with the minecraft account " + mcName + "!")).queue();
            } else {
                channel.sendMessage(main.getDiscord().escapeMarkdown("Linked " + member.getUser().getAsTag() + " with the minecraft account " + mcName + "! (Guild: " + guild.getDisplayName() + ")")).queue();
            }
        }
        return gotVerified ? 1 : 0;
    }

    public List<JSONObject> getJSONObjectListByJSONArray(JSONArray jsonArray) {
        List<JSONObject> output = new ArrayList<>();
        jsonArray.forEach((object) -> {
            if (object instanceof JSONObject) output.add((JSONObject) object);
        });
        return output;
    }

    public void scheduleCommandAfter(Runnable command, int delay, TimeUnit unit) {
        scheduler.schedule(command, delay, unit);
    }

    public void setTyping(boolean typing, MessageChannel channel) {
        if (typing) {
            channel.sendTyping().queue();
            synchronized (typingChannels) {
                typingChannels.add(channel);
            }
        } else {
            synchronized (typingChannels) {
                List<MessageChannel> channelsToRemove = new ArrayList<>();
                typingChannels.stream().filter(messageChannel -> messageChannel.getIdLong() == channel.getIdLong()).forEach(channelsToRemove::add);
                for (MessageChannel channelToRemove : channelsToRemove) typingChannels.remove(channelToRemove);
            }
            channel.deleteMessageById(channel.sendMessage("\u200E").complete().getId()).queue(); // To remove the typing status instantly
        }
    }

    public String stripColorCodes(String input) {
        return stripColorCodesRegex.matcher(input).replaceAll("");
    }

    public File getFileToUse(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file = getFileToUse(fileName.replace(".jar", "") + "_new.jar");
        }
        return file;
    }

    public void handleGuildMessage(DiscordBot discordBot, DiscordServer discordServer, String author, String message, InetAddress requestSenderIpAddr) {
        if (discordBot == null || author.isEmpty() || message.isEmpty()) return;
        MessageChannel channel;
        if ((channel = discordBot.getJDA().getTextChannelById(discordServer.getGuildChatChannelId())) != null) {
            if (latestGuildmessage.equals("Guild > " + author + ": " + message) && latestGuildmessageAuthor.equals(author) &&
                    !latestMessageByIP.getOrDefault(requestSenderIpAddr, "").equals(author + ":" + message)) {
                return; // TODO fix problem: if multiple ppl send messages at almost exact time then message might come delayed and it isnt latestMessage anymore
            }
            if (latestGuildmessageAuthor.equals(author)) {
                latestGuildmessage += "\n" + "Guild > " + author + ": " + message;
                try {
                    channel.deleteMessageById(channel.getLatestMessageId()).queue();
                } catch (IllegalStateException ignored) {
                } // If no last message id found
            } else {
                latestGuildmessage = "Guild > " + author + ": " + message;
            }
            channel.sendMessage(new EmbedBuilder().setColor(HypixelRank.getHypixelRankFromName(author.substring(0, author.contains("]") ? author.indexOf("]") + 1 : 0)).getColor()).setDescription(latestGuildmessage).build()).queue();
//            channel.sendMessage("```css\n" + latestGuildmessage + "\n```").queue();
            latestGuildmessageAuthor = author;
            latestMessageByIP.put(requestSenderIpAddr, author + ":" + message);
        }
    }

    public static String getTextWithoutFormattingCodes(String text) {
        return text == null ? null : RegexUtil.getMatcher("(?i)§[0-9A-FK-OR]", text).replaceAll("");
    }

    public String getAuthorFromGuildChatMessage(String message) {
        return getTextWithoutFormattingCodes(message).split(":")[0].substring(8);
    }

    public String getMessageFromGuildChatMessage(String message) {
        return getTextWithoutFormattingCodes(message.split(":")[1]);
    }
}