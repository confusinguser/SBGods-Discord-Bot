package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

public class LangUtil {

    private final SBGods main;

    private final Pattern addCommaPattern = Pattern.compile("\\d{1,3}(?=(\\d{3})+(?=\\.))");
    private final DecimalFormat df = new DecimalFormat("#");

    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("language", Locale.ENGLISH);

    public LangUtil(SBGods main) {
        this.main = main;
        df.setMaximumFractionDigits(16);
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
                    returnValStr = "" + main.getUtil().round(returnVal, o - 1);
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

    /**
     * Adds commas to a number.
     *
     * @param num The number (eg. 150385.6725)
     * @return Returns the number but with commas (eg. 150,385.6725)
     */
    public String addCommas(double num) {
        return addCommaPattern.matcher(String.valueOf(num)).replaceAll("$1,");
    }

    public String addNotationOrCommas(double num) {
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

    /**
     * Got From {@link Throwable#printStackTrace()}
     */
    public String generateStackTraceView(Throwable throwable) {
        StringBuilder output = new StringBuilder(throwable.toString() + "\n");
        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>());
        dejaVu.add(throwable);

        // Print our stack trace
        StackTraceElement[] trace = throwable.getStackTrace();
        for (StackTraceElement traceElement : trace)
            output.append("\tat ").append(traceElement).append('\n');

        // Print suppressed exceptions, if any
        for (Throwable se : throwable.getSuppressed())
            generateEnclosedStackTrace(se, output, trace, "Suppressed: ", "\t", dejaVu);

        // Print cause, if any
        Throwable ourCause = throwable.getCause();
        if (ourCause != null)
            generateEnclosedStackTrace(ourCause, output, trace, "Caused by: ", "", dejaVu);
        return output.toString();
    }

    private void generateEnclosedStackTrace(Throwable throwable, StringBuilder output, StackTraceElement[] enclosingTrace, String caption, String prefix, Set<Throwable> dejaVu) {
        if (dejaVu.contains(throwable)) {
            output.append(prefix).append(caption).append("[CIRCULAR REFERENCE: ").append(throwable).append("]");
        } else {
            dejaVu.add(throwable);
            // Compute number of frames in common between this and enclosing trace
            StackTraceElement[] trace = throwable.getStackTrace();
            int m = trace.length - 1;
            int n = enclosingTrace.length - 1;
            while (m >= 0 && n >= 0 && trace[m].equals(enclosingTrace[n])) {
                m--;
                n--;
            }
            int framesInCommon = trace.length - 1 - m;

            // Print our stack trace
            output.append(prefix).append(caption).append(throwable);
            for (int i = 0; i <= m; i++)
                output.append(prefix).append("\tat ").append(trace[i]);
            if (framesInCommon != 0)
                output.append(prefix).append("\t... ").append(framesInCommon).append(" more");

            // Print suppressed exceptions, if any
            for (Throwable se : throwable.getSuppressed())
                generateEnclosedStackTrace(se, output, trace, "Suppressed: ", prefix + "\t", dejaVu);

            // Print cause, if any
            Throwable ourCause = throwable.getCause();
            if (ourCause != null)
                generateEnclosedStackTrace(ourCause, output, trace, "Caused by: ", prefix, dejaVu);
        }
    }

    public String getMessageByKey(String key) {
        return resourceBundle.getString(key);
    }

    public int parseIntegerWithSuffixes(String input) {
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

    public String beautifyInt(int integer) {
        String intStr = addNotation(integer);
        if (parseIntegerWithSuffixes(intStr) == integer) {
            return intStr;
        }
        return addCommas(integer);
    }

    public List<String> processMessageForDiscord(String message, int limit) {
        return processMessageForDiscord(message, limit, new ArrayList<>());
    }

    private List<String> processMessageForDiscord(String message, int limit, List<String> currentOutput) {
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
}
