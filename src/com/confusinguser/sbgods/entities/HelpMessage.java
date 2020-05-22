package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Random;

public enum HelpMessage {

    HELP("help",
            "help [COMMAND]",
            new String[]{"help [COMMAND] [SUBCOMMAND]"},
            "Gives more info on the usage of a command.",
            "Will also list all subcommands."),

    AH("ah",
            "ah [IGN]",
            new String[]{},
            "Lists the player (and coop)'s Auctions."),

    DEATHS("deaths",
            "deaths player [IGN]",
            new String[]{},
            "Lists every death to show how pathetic the player is."),

    KILLS("kills",
            "kills player [IGN]",
            new String[]{},
            "Lists the player's kills."),

    PETS("pets",
            "pets [IGN]",
            new String[]{},
            "List the player's pets (on all profiles).",
            "Will also show what pets are equiped and what level they are."),

    PLAYER("player",
            "player [IGN]",
            new String[]{},
            "Lists lots of stats about a player"),

    SBGODS("sbgods",
            "sbgods [COMMAND]",
            new String[]{"sbgods version","sbgods update","sbgods stop"},
            "Some commands to change bot stuff"),

    SBGODS_VERSION("sbgods version",
            "sbgods version",
            new String[]{},
            "Lists the bot version information."),

    SBGODS_UPDATE("sbgods update",
            "sbgods update",
            new String[]{},
            "Updates the bot from github.",
            "Requires to be a bot dev to use."),

    SBGODS_STOP("sbgods stop",
            "sbgods stop",
            new String[]{},
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
            new String[]{"skill player [IGN]","skill leaderboard [LENGTH | all]"},
            "The main skill command."),

    SKILL_PLAYER("skill player",
            "skill player [IGN]",
            new String[]{},
            "Shows a player's skill levels."),

    SKILL_LEADERBOARD("skill",
            "skill leaderboard [LENGTH | all]",
            new String[]{},
            "Shows the guild's skill leaderboard."),

    SKILLEXP("skillexp",
            "skill [SUBCOMMAND]",
            new String[]{"skillexp player [IGN]","skillexp leaderboard [LENGTH | all]"},
            "The main skillexp command."),

    SKILLEXP_PLAYER("skillexp player",
            "skillexp player [IGN]",
            new String[]{},
            "Shows a player's skillexp levels."),

    SKILLEXP_LEADERBOARD("skillexp",
            "skillexp leaderboard [LENGTH | all]",
            new String[]{},
            "Shows the guild's skillexp leaderboard."),

    SLAYER("slayer",
            "slayer [SUBCOMMAND]",
            new String[]{"slayer player [IGN]","slayer leaderboard [LENGTH | all]"},
            "The main slayer command."),

    SLAYER_PLAYER("slayer player",
            "slayer player [IGN]",
            new String[]{},
            "Shows a player's slayer exp."),

    SLAYER_LEADERBOARD("slayer",
            "slayer leaderboard [LENGTH | all]",
            new String[]{},
            "Shows the guild's slayer leaderboard."),

    TAX("tax",
            "tax [SUBCOMMAND]",
            new String[]{"tax owelist","tax info", "tax setrole", "tax paid", "tax paidall", "tax owe", "tax oweall"},
            "The main tax command.",
            "This will also show your tax info"),

    TAX_OWELIST("tax owelist",
            "tax owelist",
            new String[]{},
            "Lists everyone in the guild that owes tax."),

    TAX_INFO("tax info",
            "tax info [IGN]",
            new String[]{},
            "Will show the tax info for a specific player."),

    TAX_SETROLE("tax setrole",
            "tax setrole [IGN] [ROLE]",
            new String[]{},
            "Sets a player's tax role.",
            "",
            "Requires to be a bot dev or server admin to use."),

    TAX_PAID("tax paid",
            "tax paid [PLAYER] [AMOUNT]",
            new String[]{},
            "Sets a player as having paid amount of tax. (Additive)",
            "",
            "Requires to be a bot dev or server admin to use."),

    TAX_PAIDALL("tax paidall",
            "tax paidall [AMOUNT]",
            new String[]{},
            "Sets everyone as having paid amount of tax. (Additive)",
            "",
            "Requires to be a bot dev or server admin to use."),

    TAX_OWE("tax owe",
            "tax owe [PLAYER] [AMOUNT]",
            new String[]{},
            "Sets a player as owing that amount of tax. (Additive)",
            "",
            "Requires to be a bot dev or server admin to use."),

    TAX_OWEALL("tax",
            "tax [SUBCOMMAND]",
            new String[]{},
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
            new String[]{},
            "Removes the verified role from all players.",
            "This will not remove sbg and sbf guild member roles.",
            "",
            "Requires to be a bot dev or server admin to use."),

    VERIFY("verify",
            "verify [IGN]",
            new String[]{},
            "Will verify you based of the given ign.",
            "If no ign is given it will attempt to auto-get your ign.",
            "Auto-getting someones ign will not work 90% of the time tho."),

    WHATGUILD("whatguild",
            "whatguild [IGN]",
            new String[]{},
            "Lists the guild that the player is in");

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
    public String[] getSubcommands() {
        return subCommands;
    }
    public String[] getHelpLines() {
        return helpLines;
    }

    public MessageEmbed getEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("`" + SBGods.getInstance().getLangUtil().toLowerCaseButFirstLetter(command) + "` command help.");
        Random colorRandom = new Random();
        embedBuilder.setColor(new Color(colorRandom.nextFloat(), colorRandom.nextFloat(), colorRandom.nextFloat()));
        embedBuilder.setFooter("Version " + SBGods.VERSION + "\nDescription: " + SBGods.VERSION_DESCRIPTION_MINOR);
        embedBuilder.addField("Usage", "`" + SBGods.getInstance().getDiscord().commandPrefix + usage + "`",false);

        embedBuilder.addField("Sub-commands", "`" + (subCommands.length==0? "NONE" : SBGods.getInstance().getDiscord().commandPrefix + String.join("`\n`" + SBGods.getInstance().getDiscord().commandPrefix, subCommands)) + "`", false);

        for (String helpLine : helpLines) {
            embedBuilder.appendDescription(helpLine + "\n");
        }

        return embedBuilder.build();
    }
}