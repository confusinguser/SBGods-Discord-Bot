package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Random;

public enum HelpMessage {

    //<editor-fold defaultstate="collapsed" desc="All -help [COMMAND] data">
    HELP("help",
            "help [COMMAND]",
            new String[]{"help [COMMAND] [SUBCOMMAND]"},
            "Gives more info on the usage of a command.",
            "Will also list all subcommands."),

    AH("ah",
            "ah [IGN]",
            "Lists the player's and coop's Auctions."),

    DEATHS("deaths",
            "deaths player [IGN]",
            "Lists every death to show how pathetic the player is."), // Maybe a bit too offensive?
                                                                       //Soopy: Nahh, u can never be too offensive...    xD
    KILLS("kills",                                              //edit it if you want lol
            "kills player [IGN]",
            "Lists the player's kills."),

    PETS("pets",
            "pets [IGN]",
            "List the player's pets (on all profiles).",
            "Will also show what pets are equiped and what level they are."),

    PLAYER("player",
            "player [IGN]",
            "Lists lots of stats about a player"),

    SBGODS("sbgods",
            "sbgods [COMMAND]",
            new String[]{"sbgods version", "sbgods update", "sbgods stop"},
            "Some commands to change bot stuff"),

    SBGODS_VERSION("sbgods version",
            "sbgods version",
            "Lists the bot version information."),

    SBGODS_UPDATE("sbgods update",
            "sbgods update",
            "Updates the bot from github.",
            "Requires to be a bot dev to use."),

    SBGODS_STOP("sbgods stop",
            "sbgods stop",
            "Stops the SBGods bot.",
            "This may cause the bot to:",
            "1) Just stop",
            "2) Reboot (the bot stops but gets started instantly)",
            "",
            "Requires to be a bot dev to use."),

    SETTINGS("settings",
            "settings [SETTING]",
            new String[]{"settings prefix [PREFIX]"},
            "Changes the bot's prefix",
            "",
            "Requires to be a bot dev to use."),

    SKILL("skill",
            "skill [SUBCOMMAND]",
            new String[]{"skill player [IGN]", "skill leaderboard [LENGTH | all]"},
            "The main skill command."),

    SKILL_PLAYER("skill player",
            "skill player [IGN]",
            "Shows a player's skill levels."),

    SKILL_LEADERBOARD("skill leaderboard",
            "skill leaderboard [LENGTH | all]",
            "Shows the guild's skill leaderboard."),

    SKILLEXP("skillexp",
            "skill [SUBCOMMAND]",
            new String[]{"skillexp player [IGN]", "skillexp leaderboard [LENGTH | all]"},
            "The main skillexp command."),

    SKILLEXP_PLAYER("skillexp player",
            "skillexp player [IGN]",
            "Shows a player's skillexp levels."),

    SKILLEXP_LEADERBOARD("skillexp leaderboard",
            "skillexp leaderboard [LENGTH | all]",
            "Shows the guild's skillexp leaderboard."),

    SLAYER("slayer",
            "slayer [SUBCOMMAND]",
            new String[]{"slayer player [IGN]", "slayer leaderboard [LENGTH | all]"},
            "The main slayer command."),

    SLAYER_PLAYER("slayer player",
            "slayer player [IGN]",
            "Shows a player's slayer exp."),

    SLAYER_LEADERBOARD("slayer leaderboard",
            "slayer leaderboard [LENGTH | all]",
            "Shows the guild's slayer leaderboard."),

    TAX("tax",
            "tax [SUBCOMMAND]",
            new String[]{"tax owelist", "tax paidlist", "tax info", "tax setrole", "tax paid", "tax paidall", "tax owe", "tax oweall"},
            "The main tax command.",
            "This will also show your tax info"),

    TAX_OWELIST("tax owelist",
            "tax owelist",
            "Lists everyone in the guild that owes tax."),

    TAX_PAIDLIST("tax paidlist",
            "tax paidlist",
            "Lists everyone in the guild that has paid tax."),

    TAX_INFO("tax info",
            "tax info [IGN]",
            "Will show the tax info for a specific player."),

    TAX_SETROLE("tax setrole",
            "tax setrole [IGN] [ROLE]",
            "Sets a player's tax role.",
            "",
            "Requires to be a bot dev or server admin to use."),

    TAX_PAID("tax paid",
            "tax paid [PLAYER] [AMOUNT]",
            "Sets a player as having paid amount of tax. (Additive)",
            "",
            "Requires to be a bot dev or server admin to use."),

    TAX_PAIDALL("tax paidall",
            "tax paidall [AMOUNT]",
            "Sets everyone as having paid amount of tax. (Additive)",
            "",
            "Requires to be a bot dev or server admin to use."),

    TAX_OWE("tax owe",
            "tax owe [PLAYER] [AMOUNT]",
            "Sets a player as owing that amount of tax. (Additive)",
            "",
            "Requires to be a bot dev or server admin to use."),

    TAX_OWEALL("tax",
            "tax [SUBCOMMAND]",
            "Sets everyone as owing that amount of tax. (Additive)",
            "",
            "Requires to be a bot dev or server admin to use."),

    VERIFYALL("verifyall",
            "verifyall",
            new String[]{"verifyall reset"},
            "Will attempt to auto-verify all players in the server.",
            "This will also update already verified player's roles.",
            "",
            "Requires to be a bot dev or server admin to use."),

    VERIFYALL_RESET("verifyall reset",
            "verifyall reset",
            new String[]{"verifyall reset verified"},
            "Removes the verified role from all players.",
            "Will also remove the sbg and sbf guild member roles.",
            "",
            "Requires to be a bot dev or server admin to use."),

    VERIFYALL_RESET_VERIFIED("verifyall reset verified",
            "verifyall reset verified",
            "Removes the verified role from all players.",
            "This will not remove sbg and sbf guild member roles.",
            "",
            "Requires to be a bot dev or server admin to use."),

    VERIFY("verify",
            "verify [IGN]",
            "Will verify you based of the given ign.",
            "If no ign is given it will attempt to auto-get your ign.",
            "Auto-getting someone's ign will not work 90% of the time."),

    WHATGUILD("whatguild",
            "whatguild [IGN]",
            "Lists the guild that the player is in");
    //</editor-fold>

    private final SBGods main = SBGods.getInstance();

    private final String command;
    private final String usage;
    private final String[] subCommands;
    private final String[] helpLines;

    HelpMessage(String command, String usage, String[] subCommands, String... lines) {
        this.command = command;
        this.usage = usage;
        this.subCommands = subCommands;
        this.helpLines = lines;
    }

    HelpMessage(String command, String usage, String... lines) {
        this.command = command;
        this.usage = usage;
        this.subCommands = new String[0];
        this.helpLines = lines;
    }

    public static HelpMessage getHelpFromCommand(String input) {
        for (HelpMessage helpMessage : values()) {
            if (helpMessage.command.equalsIgnoreCase(input)) {
                return helpMessage;
            }
        }
        return null;
    }

    public String getCommand() {
        return command;
    }

    public String getUsage() {
        return usage;
    }

    public String[] getSubCommands() {
        return subCommands;
    }

    public String[] getHelpLines() {
        return helpLines;
    }

    public MessageEmbed getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("`" + command + "` command help.");
        Random colorRandom = new Random();
        embedBuilder.setColor(new Color(colorRandom.nextFloat(), colorRandom.nextFloat(), colorRandom.nextFloat()));
        embedBuilder.addField("Usage", "`" + main.getDiscord().commandPrefix + usage + "`", false);
        if (subCommands.length > 0) {
            embedBuilder.addField("Sub-commands", "`" + main.getDiscord().commandPrefix + String.join("`\n`" + main.getDiscord().commandPrefix, subCommands) + "`", false);
        }
        embedBuilder.setFooter("Version " + SBGods.VERSION);

        for (String helpLine : helpLines) {
            embedBuilder.appendDescription(helpLine + "\n");
        }

        return embedBuilder.build();
    }
}