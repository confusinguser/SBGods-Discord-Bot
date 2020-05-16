package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.DiscordServer;
import com.confusinguser.sbgods.entities.Player;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SlayerExp;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Util {

    private final SBGods main;

    public Util(SBGods main) {
        this.main = main;
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

    private ArrayList<String> processMessageForDiscord(String message, int limit, ArrayList<String> currentOutput) {
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

    public void verifyPlayer(Member member, String mcName, Guild discord, MessageChannel chan) {
        DiscordServer discordServer = DiscordServer.getDiscordServerFromDiscordGuild(discord);
        if (discordServer == null) {
            return;
        }

        try {
            member.modifyNickname(mcName).complete();
        } catch (HierarchyException ignored) {} // Don't have perms to change nick

        List<String> roleNames = discord.getRoles().stream().map(Role::getName).collect(Collectors.toList()); // Make a list of all role names in the member

        boolean sendMsg = false;

        if (roleNames.contains("Verified")) {
            for (Role role : discord.getRolesByName("Verified",true)) {
                try {
                    discord.addRoleToMember(member, role).complete();
                    sendMsg = true;
                } catch (HierarchyException ignored) {}
            }
        }
        // Add guild roles if they are in one

        Player thePlayer = main.getApiUtil().getPlayerFromUsername(mcName);
        if (thePlayer.getSkyblockProfiles().isEmpty()) {
            return;
        }
        String guildName = main.getApiUtil().getGuildFromUUID(thePlayer.getUUID());
        String guildDis = "";
        if (guildName == null) {
            guildName = "";
        }


        if(guildName.equalsIgnoreCase("Skyblock Gods")){
            for (Role role : discord.getRolesByName("SBG Guild Member",true)) {
                try {
                    discord.addRoleToMember(member, role).complete();

                } catch (HierarchyException ignored) {}

                guildDis = "SkyBlock Gods";
            }
        }else{
            for (Role role : discord.getRolesByName("SBG Guild Member",true)) {
                try {
                    discord.removeRoleFromMember(member, role).complete();

                } catch (HierarchyException ignored) {}
            }
        }

        if(guildName.equalsIgnoreCase("Skyblock Forceful")){
            for (Role role : discord.getRolesByName("SBF Guild Member",true)) {
                try {
                    discord.addRoleToMember(member, role).complete();

                } catch (HierarchyException ignored) {}

                guildDis = "SkyBlock Forcefull";
            }
        }else{
            for (Role role : discord.getRolesByName("SBF Guild Member",true)) {
                try {
                    discord.removeRoleFromMember(member, role).complete();

                } catch (HierarchyException ignored) {}
            }
        }

        main.logger.info("Linked " + member.getUser().getAsTag() + " with the minecraft account " + mcName + "! (Guild: " + guildName + ")");

        if(sendMsg){
            if(guildDis.isEmpty()){
                chan.sendMessage("Linked " + member.getUser().getAsTag() + " with the minecraft account " + mcName + "!").queue();
            }else{
                chan.sendMessage("Linked " + member.getUser().getAsTag() + " with the minecraft account " + mcName + "! (Guild: " + guildDis + ")").queue();
            }
        }
    }
}