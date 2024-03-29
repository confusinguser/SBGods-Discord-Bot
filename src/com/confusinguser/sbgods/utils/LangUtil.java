package com.confusinguser.sbgods.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangUtil {
    private static final Pattern addCommaPattern = Pattern.compile("\\d{1,3}(?=(\\d{3})+(?=\\.))");
    private static final DecimalFormat df = new DecimalFormat("#");
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("language", Locale.ENGLISH);

    static {
        df.setMaximumFractionDigits(16);
    }

    public static String makePossessiveForm(String text) {
        if (text.endsWith("s")) {
            return text + "'";
        }
        return text + "'s";
    }

    public static String toLowerCaseButFirstLetter(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static String prettifyInt(int input) {
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

    public static String prettifyLong(long input) {
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

    public static String prettifyDouble(double input) {
        if (input % 1 == 0) {
            return String.valueOf((int) Math.floor(input));
        }
        return String.valueOf(input);
    }

    public static String getAuthorGuildRank(String author) {
        Matcher matcher = RegexUtil.getMatcher("(?:\\[.*\\]|) \\w{3,32} (\\[.*\\])", author);
        if (matcher.find()) {
            String group = matcher.group(1);
            return group == null ? "" : group;
        }
        return "";
    }

    /**
     * Adds commas to a number.
     *
     * @param num The number (eg. 150385.6725)
     * @return Returns the number but with commas (eg. 150,385.6725)
     */
    public static String addCommas(double num) {
        return addCommaPattern.matcher(String.valueOf(num)).replaceAll("$1,");
    }

    public static String addNotationOrCommas(double num) {
        String notationNum = addNotation(num);
        int toHellAndBack = parseIntegerWithSuffixes(notationNum);
        if (num == toHellAndBack) return notationNum; // notationNum does not affect the value of the number
        else return addCommas(num);
    }

    /**
     * Makes a unicode progress bar with ■ and □
     *
     * @param amountDone  A double between 0 and 1 representing the progress of the bar
     * @param lengthOfBar The total length of the progress bar
     * @return The unicode progress bar using the arguments
     */
    public static String getProgressBar(double amountDone, int lengthOfBar) {
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

    public static String loopString(String input, int loops) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < loops; i++) {
            output.append(input);
        }

        return output.toString();
    }

    public static String generateStackTraceView(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

    public static String getMessageByKey(String key) {
        return resourceBundle.getString(key);
    }

    public static int parseIntegerWithSuffixes(String input) {
        int amount;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException err) {
            if (input.toLowerCase().endsWith("k")) {
                return (int) (Double.parseDouble(input.toLowerCase().replace("k", "")) * 1000);
            } else if (input.toLowerCase().endsWith("m")) {
                return (int) (Double.parseDouble(input.toLowerCase().replace("m", "")) * 1000000);
            }
            return Integer.MIN_VALUE;
        }
    }

    public static String beautifyInt(int integer) {
        String intStr = addNotation(integer);
        if (parseIntegerWithSuffixes(intStr) == integer) {
            return intStr;
        }
        return addCommas(integer);
    }

    public static List<String> processMessageForDiscord(String message, int limit) {
        return processMessageForDiscord(message, limit, new ArrayList<>());
    }

    private static List<String> processMessageForDiscord(String message, int limit, List<String> currentOutput) {
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

    public static String getAuthorNameAndRank(String author) {
        Matcher matcher = RegexUtil.getMatcher("((?:\\[.*\\]|) \\w{3,32})", author);
        if (matcher.find()) {
            String group = matcher.group(1);
            return group == null ? "" : group;
        }
        return "";
    }

    public static String addNotation(double num) {
        String prefix = "";
        if (num < 0) {
            num *= -1;
            prefix = "-";
        }
        double returnVal;
        String[] notList = new String[]{"K", "M", "B"};
        String returnValStr = df.format(num);
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
                    returnValStr = "" + Util.round(returnVal, o - 1);
                    if (returnValStr.endsWith(".0")) {
                        returnValStr = returnValStr.replace(".0", "");
                    }
                    returnValStr += notValue;
                }
                checkNum *= 10;
            }
        }
        return prefix + returnValStr;
    }
}
