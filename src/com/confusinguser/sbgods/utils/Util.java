package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.HypixelGuild;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.leaderboard.SkillLevels;
import com.confusinguser.sbgods.entities.leaderboard.SlayerExp;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {

    private static final SBGods main = SBGods.getInstance();
    private static final List<MessageChannel> typingChannels = new ArrayList<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Pattern stripColorCodesRegex = Pattern.compile("§[a-f0-9rlkmn]");
    private static final int[] colorCodes = new int[]{
            0x000000, // 0
            0x0000aa, // 1
            0x00aa00, // 2
            0x00aaaa, // 3
            0xaa0000, // 4
            0xaa00aa, // 5
            0xffaa00, // 6
            0xaaaaaa, // 7
            0x555555, // 8
            0x5555ff, // 9
            0x55ff55, // A
            0x55ffff, // B
            0xff5555, // C
            0xff55ff, // D
            0xffff55, // E
            0xffffff  // F
    };

    public static Entry<String, Integer> getHighestKeyValuePair(Map<String, Integer> map, int position) {
        List<Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        return list.get(position);
    }

    public static Entry<String, SkillLevels> getHighestKeyValuePair(Map<String, SkillLevels> map, int position, boolean isSkillLevel) {
        if (!isSkillLevel) {
            return null;
        }

        List<Entry<String, SkillLevels>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> Double.compare(o2.getValue().getAvgSkillLevel(), o1.getValue().getAvgSkillLevel()));

        return list.get(position);
    }

    public static Entry<String, SlayerExp> getHighestKeyValuePairForSlayerExp(Map<String, SlayerExp> map, int position) {
        List<Entry<String, SlayerExp>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> Integer.compare(o2.getValue().getTotalExp(), o1.getValue().getTotalExp()));

        return list.get(position);
    }

    public static double getAverageFromDoubleList(List<Double> list) {
        double output = 0;
        for (Double aDouble : list) {
            output += aDouble;
        }
        output /= list.size();
        return output;
    }

    public static double round(double num, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        num = num * factor;
        long tmp = Math.round(num);
        return (double) tmp / factor;
    }

    public static double getAverageFromSkillLevelArray(SkillLevels[] skillLevels) {
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


    public static double getAverageFromSlayerExpArray(SlayerExp[] array) {
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
    public static int verifyPlayer(Member member, String mcName, Guild discord, MessageChannel channel) {
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
                if (!memberRoleNames.contains(role.getName())) {
                    gotVerified = true;
                    discord.addRoleToMember(member, role).queue();
                }
            } catch (HierarchyException ignored) {
            }
        }

        // Add guild roles if they are in one
        Player thePlayer = ApiUtil.getPlayerFromUsername(mcName);

        String guildId = thePlayer.getGuildId();
        if (guildId == null) {
            guildId = "";
        }
        HypixelGuild guild = HypixelGuild.getGuildById(guildId);

        if (guild != null) {
            for (Player guildMember : ApiUtil.getGuildMembers(guild)) {
                if (guildMember.getUUID().equals(thePlayer.getUUID())) {
                    thePlayer = Player.mergePlayerAndGuildMember(thePlayer, guildMember);
                    break;
                }
            }
        }

        for (Role role : discord.getRoles().stream().filter(role -> guild != null && guild.isAltNameIgnoreCase(role.getName())).collect(Collectors.toList())) {
            try {
                discord.addRoleToMember(member, role).queue();
            } catch (HierarchyException ignored) {
            }
        }

        // Avert crisis where everyone's roles guild member roles are taken away if keys invalid
        if (main.getKeys().length != 0) {
            // Remove all roles that are for other guilds
            for (Role role : member.getRoles().stream().filter(role -> HypixelGuild.getGuildByName(role.getName()) != guild && HypixelGuild.getGuildByName(role.getName()) != null).collect(Collectors.toList())) {
                try {
                    discord.removeRoleFromMember(member, role).queue();
                } catch (HierarchyException ignored) {
                }
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
                for (Role role : discord.getRoles().stream()
                        .filter(role -> role.getName().equalsIgnoreCase("elite") ||
                                role.getName().equalsIgnoreCase("skyblock king \uD83D\uDC51") ||
                                role.getName().equalsIgnoreCase("skyblock god \uD83D\uDE4F"))
                        .collect(Collectors.toList())) {
                    try {
                        discord.removeRoleFromMember(member, role).queue();
                    } catch (HierarchyException ignored) {
                    }
                }

                switch (thePlayer.getGuildRank()) {
                    case "Elite":
                        for (Role role : discord.getRolesByName("Elite", true)) {
                            try {
                                discord.addRoleToMember(member, role).queue();
                            } catch (HierarchyException ignored) {
                            }
                        }
                        break;
                    case "Skyblock King":
                        for (Role role : discord.getRolesByName("Skyblock King \uD83D\uDC51" /* \uD83D\uDC51 = 👑 */, true)) {
                            try {
                                discord.addRoleToMember(member, role).queue();
                            } catch (HierarchyException ignored) {
                            }
                        }
                        break;
                    case "Skyblock God":
                        for (Role role : discord.getRolesByName("Skyblock God \uD83D\uDE4F", true)) {
                            try {
                                discord.addRoleToMember(member, role).queue();
                            } catch (HierarchyException ignored) {
                            }
                        }
                        break;
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

    public static void scheduleCommandAfter(Runnable command, int delay, TimeUnit unit) {
        scheduler.schedule(command, delay, unit);
    }

    public static void setTyping(boolean typing, MessageChannel channel) {
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

    public static String stripColorCodes(String input) {
        return stripColorCodesRegex.matcher(input).replaceAll("");
    }

    public static File getFileToUse(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file = getFileToUse(fileName.replace(".jar", "") + "_new.jar");
        }
        return file;
    }

    public static String getTextWithoutFormattingCodes(String text) {
        return text == null ? null : RegexUtil.getMatcher("(?i)§[0-9A-FK-OR]", text).replaceAll("");
    }

    public static String getAuthorFromGuildChatMessage(String message) {
        message = getTextWithoutFormattingCodes(message);
        if (message.startsWith("Guild > ")) message = message.substring(8);
        return message.split(":")[0];
    }

    public static String getMessageFromGuildChatMessage(String message) {
        try {
            return getTextWithoutFormattingCodes(message.split(":")[1]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return getTextWithoutFormattingCodes(message);
        }
    }

    public static <T> List<T> turnListIntoSubClassList(List<?> list, Class<T> subclass) { // Looked at like 8 different stackoverflow threads to write this
        if (list.isEmpty()) return new ArrayList<>();
        return list.stream().filter(e -> subclass.isAssignableFrom(list.get(0).getClass())).map(subclass::cast).collect(Collectors.toList());
    }

    public static int singleHexDigitToInt(char hex) {
        if (hex >= '0' && hex <= '9')
            return hex - '0';
        if (hex >= 'A' && hex <= 'F')
            return hex - 'A' + 10;
        if (hex >= 'a' && hex <= 'f')
            return hex - 'a' + 10;
        return -1;
    }

    public static Character closestMCColorCode(Color color) {
        if (color == null) return null;
        int leastTotalDiff = Integer.MAX_VALUE;
        Character closestColorCode = null;
        for (int i = 0; i < colorCodes.length; i++) {
            int rawColorCode = colorCodes[i];
            Color colorCode = new Color(rawColorCode);
            int rDiff = Math.abs(color.getRed() - colorCode.getRed());
            int gDiff = Math.abs(color.getGreen() - colorCode.getGreen());
            int bDiff = Math.abs(color.getBlue() - colorCode.getBlue());
            int totalDiff = rDiff + gDiff + bDiff;
            if (leastTotalDiff > totalDiff) {
                leastTotalDiff = totalDiff;
                closestColorCode = String.format("%x", i).charAt(0);
            }
        }
        return closestColorCode;
    }

    public static Color mcColorCodeToColor(char colorCode) {
        int colorCodeIndex = singleHexDigitToInt(colorCode);
        if (colorCodeIndex < 0 || colorCodeIndex >= 16) return null;
        return new Color(colorCodes[colorCodeIndex]);
    }

    public static String bypassAntiSpam(String message) {
        final char[] chars = new char[]{
                '⭍',
                'ࠀ'
        };
        Random random = new Random();
        StringBuilder messageBuilder = new StringBuilder(message);
        for (int i = 0; i < (256 - messageBuilder.length()); i++) {
            messageBuilder.append(chars[random.nextInt(2)]);
        }
        return messageBuilder.toString();
    }

//    public static int calculatePlayerScore() { TODO
//        return Math.round((Math.pow())) / 1000;
//    }
}