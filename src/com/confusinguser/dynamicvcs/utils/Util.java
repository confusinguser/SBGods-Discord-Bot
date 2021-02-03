package com.confusinguser.dynamicvcs.utils;

public class Util {
    public static double round(double num, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        num = num * factor;
        long tmp = Math.round(num);
        return (double) tmp / factor;
    }

    public static String escapeMarkdown(String text) {
        String unescaped = text.replaceAll("\\\\([*_`~\\\\])", "$1"); // unescape any "backslashed" character
        return unescaped.replaceAll("([*_`~\\\\])", "\\\\$1"); // escape *, _, `, ~, \
    }

    public static int parseIntOrDefault(String input, int def) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            return def;
        }
    }
}
