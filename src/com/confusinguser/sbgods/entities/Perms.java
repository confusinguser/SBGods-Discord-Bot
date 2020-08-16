package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

public enum Perms {
    AH(DiscordPerms.DEFAULT),
    BANK(DiscordPerms.DEFAULT),
    DEATHS(DiscordPerms.DEFAULT),
    EVENT_START(DiscordPerms.STAFFPLUS),
    EVENT_END(DiscordPerms.STAFFPLUS),
    EVENT_PROGRESS(DiscordPerms.STAFF),
    GUILDDESCLIST(DiscordPerms.STAFFPLUS),
    HELP(DiscordPerms.DEFAULT),
    KILLS(DiscordPerms.DEFAULT),
    PETS(DiscordPerms.DEFAULT),
    PLAYER(DiscordPerms.DEFAULT),
    SBGODS_VERSION(DiscordPerms.DEFAULT),
    SBGODS_UPDATE(DiscordPerms.BOTDEV),
    SBGODS_STOP(DiscordPerms.BOTDEV),
    SBGODS_DEV_ADDGAPPLYREACT(DiscordPerms.BOTDEV),
    SETTINGS(DiscordPerms.STAFFPLUS),
    SKILL(DiscordPerms.DEFAULT),
    SKILLEXP(DiscordPerms.DEFAULT),
    SLAYER(DiscordPerms.DEFAULT),
    TAX_INFO(DiscordPerms.DEFAULT),
    TAX_PAID(DiscordPerms.STAFF),
    TAX_OWES(DiscordPerms.STAFF),
    TAX_OWELIST(DiscordPerms.STAFF),
    TAX_PAIDLIST(DiscordPerms.STAFF),
    TAX_CLEAROWES(DiscordPerms.STAFFPLUS),
    TAX_SETROLE(DiscordPerms.STAFF),
    VALL(DiscordPerms.STAFFPLUS),
    VERIFY(DiscordPerms.DEFAULT),
    VERIFYLIST(DiscordPerms.STAFFPLUS),
    WHATGUILD(DiscordPerms.DEFAULT);

    private static final SBGods main = SBGods.getInstance();

    private final DiscordPerms minPerms;

    Perms(DiscordPerms minPerms) {
        this.minPerms = minPerms;
    }
}
