package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

public enum Perms {
    AH(DiscordPerm.DEFAULT),
    BANK(DiscordPerm.DEFAULT),
    DEATHS(DiscordPerm.DEFAULT),
    EVENT_START(DiscordPerm.STAFFPLUS),
    EVENT_END(DiscordPerm.STAFFPLUS),
    EVENT_PROGRESS(DiscordPerm.STAFF),
    GUILDDESCLIST(DiscordPerm.STAFFPLUS),
    HELP(DiscordPerm.DEFAULT),
    KILLS(DiscordPerm.DEFAULT),
    PETS(DiscordPerm.DEFAULT),
    PLAYER(DiscordPerm.DEFAULT),
    SBGODS_VERSION(DiscordPerm.DEFAULT),
    SBGODS_UPDATE(DiscordPerm.BOTDEV),
    SBGODS_STOP(DiscordPerm.BOTDEV),
    SBGODS_DEV_ADDGAPPLYREACT(DiscordPerm.BOTDEV),
    SETTINGS(DiscordPerm.STAFFPLUS),
    SKILL(DiscordPerm.DEFAULT),
    SKILLEXP(DiscordPerm.DEFAULT),
    SLAYER(DiscordPerm.DEFAULT),
    TAX_INFO(DiscordPerm.DEFAULT),
    TAX_PAID(DiscordPerm.STAFF),
    TAX_OWES(DiscordPerm.STAFF),
    TAX_OWELIST(DiscordPerm.STAFF),
    TAX_PAIDLIST(DiscordPerm.STAFF),
    TAX_CLEAROWES(DiscordPerm.STAFFPLUS),
    TAX_SETROLE(DiscordPerm.STAFF),
    VALL(DiscordPerm.STAFFPLUS),
    VERIFY(DiscordPerm.DEFAULT),
    VERIFYLIST(DiscordPerm.STAFFPLUS),
    WHATGUILD(DiscordPerm.DEFAULT);

    private static final SBGods main = SBGods.getInstance();

    private final DiscordPerm minPerms;

    Perms(DiscordPerm minPerms) {
        this.minPerms = minPerms;
    }
}
