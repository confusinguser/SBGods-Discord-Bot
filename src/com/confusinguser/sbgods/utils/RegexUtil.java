package com.confusinguser.sbgods.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegexUtil {
    private static final Map<String, Pattern> regexMap = new HashMap<>();

    public static Pattern getPattern(String regex) {
        Pattern pattern = regexMap.get(regex);
        if (pattern == null) {
            pattern = Pattern.compile(regex);
            regexMap.put(regex, pattern);
        }
        return pattern;
    }

    public static Matcher getMatcher(String regex, String matcherString) {
        return getPattern(regex).matcher(matcherString);
    }

    public static boolean stringMatches(String regex, String stringToMatch) {
        return getMatcher(regex, stringToMatch).matches();
    }
}
