package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.SkillLevels;
import com.confusinguser.sbgods.entities.SlayerExp;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Util {

    public Util(SBGods main) {
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

    public void verifyPlayer(User discord, String mcName, MessageReceivedEvent e){

        e.getMember().modifyNickname(mcName);
        e.getGuild().addRoleToMember(e.getMember(),e.getGuild().getRoleById("706081336590598165"));

        e.getChannel().sendMessage("Linked " + discord.getAsMention() + " with the minecraft account " + mcName + "!").queue();
        //Change nick, ranks ect
    }
}