package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

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

    public String prettifyLong(long input) {
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

    public String addNotation(double numb) {
        double retNum = 0.0;
        main.logger.info("1");
        String ret = "";
        main.logger.info("2");
        String prefix = "";
        main.logger.info("3");
        double checkVal = 1000;
        main.logger.info("4");
        if (numb < 0) {
            main.logger.info("5");
            prefix = "-";
            main.logger.info("6");
            numb *= -1;
            main.logger.info("7");
        }
        main.logger.info("8");
        ArrayList<String> notationNames = new ArrayList<>();
        main.logger.info("9");

        notationNames.add("K");
        main.logger.info("10");
        notationNames.add("M");
        main.logger.info("11");
        notationNames.add("B");
        main.logger.info("12");
        notationNames.add("T");
        main.logger.info("13");

        for (String notationName : notationNames) {
            main.logger.info("14");
            for (int i = 3; i >= 1; i--) {
                main.logger.info("15");
                if (checkVal >= numb) {
                    main.logger.info("16");
                    retNum = numb / (checkVal / 100);
                    main.logger.info("17");
                    retNum = Math.floor(retNum);
                    main.logger.info("18");
                    retNum = (retNum / Math.pow(10, i)) * 10;
                    main.logger.info("19");
                    ret = main.getUtil().round(retNum, i - 1) + notationName;
                    main.logger.info("20");
                }
                main.logger.info("21");
                checkVal *= 10;
                main.logger.info("22");
            }
            main.logger.info("23");
        }

        main.logger.info("24");
        return prefix + ret;
    }
}
