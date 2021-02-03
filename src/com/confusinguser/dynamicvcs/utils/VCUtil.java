package com.confusinguser.dynamicvcs.utils;

import net.dv8tion.jda.api.entities.VoiceChannel;

public class VCUtil {
    public static String actualChannelName(String channelName) {
        return RegexUtil.getGroup("(.+) \\d{1,3}", channelName, 1);
    }

    public static boolean shouldAffectChannel(VoiceChannel vc) {
        return RegexUtil.stringMatches(".+ \\d{1,3}", vc.getName());
    }

    public static int getChannelNumber(String channelName) {
        return Util.parseIntOrDefault(RegexUtil.getGroup(".+ (\\d{1,3})", channelName, 1), -1);
    }
}
