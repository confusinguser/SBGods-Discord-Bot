package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;

public class LangUtil {

    private final SBGods main;

    public LangUtil(SBGods main) {
        this.main = main;
    }

    public String makePossessiveForm(String text) {
        if (text.endsWith("s")) {
            return text + "'";
        }
        return text + "'s";
    }

    public String toLowerCaseButFirstLetter(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public String prettifyInt(int input) {
        if (input < 1000) {
            return String.valueOf(input);
        }
        if (input % 100000 == 0) {
            if (input >= 1000000 && !(input % 1000000 == 0)) {
                return (double) input / 1000000 + "M";
            }
            return input / 1000000 + "M";
        }
        if (input % 100 == 0 && input < 1000000) {
            if (((double) input) / 1000 % 1 == 0) {
                return input / 1000 + "K";
            } else {
                return (double) input / 1000 + "K";
            }
        }
        return String.valueOf(input);
    }

    public String prettifyDouble(double input) {
        if (input % 1 == 0) {
            return String.valueOf((int) Math.floor(input));
        }
        return String.valueOf(input);
    }
}
