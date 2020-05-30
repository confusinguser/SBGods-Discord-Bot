package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;

import java.util.regex.Pattern;

public class LangUtil {

    private final SBGods main;

    private final Pattern addCommaPattern = Pattern.compile("\\d{1,3}(?=(\\d{3})+(?=\\.))");

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

    public String addNotation(double num) {
        String prefix = "";
        if (num < 0) {
            num *= -1;
            prefix = "-";
        }
        double returnVal;
        String[] notList = new String[]{"K", "M", "B"};
        String returnValStr = String.valueOf(num);
        long checkNum = 1000;

        String notValue;
        int i = 0;
        for (int u = notList.length; u > 0; u--) {
            notValue = notList[i];
            i++;
            for (int o = 3; o >= 1; o--) {
                if (num >= checkNum) {
                    returnVal = num / checkNum * 100;
                    returnVal = Math.floor(returnVal);
                    returnVal = (returnVal / Math.pow(10, o)) * 10;
                    returnValStr = +main.getUtil().round(returnVal, o - 1) + notValue;
                    if (returnValStr.endsWith("0")) {
                        returnValStr = returnValStr.replace(".0", "");
                    }
                }
                checkNum *= 10;
            }
        }
        return prefix + returnValStr;
    }

    /**
     * Adds commas to a number
     *
     * @param num The number (eg. 150385.6725)
     * @return Returns the number but with commas (eg. 150,385.6725)
     */
    public String addCommas(double num) {
        return addCommaPattern.matcher(String.valueOf(num)).replaceAll("$1,");
    }

    /**
     * Makes a unicode progress bar with ■ and □
     *
     * @param amountDone  A double between 0 and 1 representing the progress of the bar
     * @param lengthOfBar The total length of the progress bar
     * @return The unicode progress bar using the arguments
     */
    public String getProgressBar(double amountDone, int lengthOfBar) {
        String returnVal = "";
        String progressChar = "■";
        String otherChar = "□";

        if (amountDone > 1d / lengthOfBar) {
            returnVal += "**";
            returnVal += loopString(progressChar, (int) (amountDone * lengthOfBar));
            returnVal += "**";
        }

        returnVal += loopString(otherChar, lengthOfBar - ((int) (amountDone * lengthOfBar)));
        return returnVal;
    }

    public String loopString(String input, int loops) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < loops; i++) {
            output.append(input);
        }

        return output.toString();
    }

    public String beautifyStackTrace(StackTraceElement[] trace, Throwable t) {
        StringBuilder output = new StringBuilder('\t' + t.toString());
        for (StackTraceElement traceElement : trace) {
            output.append("\n\tat ").append(traceElement);
        }

        return output.toString();
    }

    /**
     * @param notationString The string to get the integer from
     * @return The integer, or -1 if {@code notationString} did not have a valid notation
     */
    public int notationToInt(String notationString) {
        try {
            return Integer.parseInt(notationString);
        } catch (NumberFormatException err) {
            if (notationString.toLowerCase().endsWith("k")) {
                return Integer.parseInt(notationString.toLowerCase().replace("k", "")) * 1000;
            } else if (notationString.toLowerCase().endsWith("m")) {
                return Integer.parseInt(notationString.toLowerCase().replace("m", "")) * 1000000;
            } else {
                return -1;
            }
        }
    }
}
