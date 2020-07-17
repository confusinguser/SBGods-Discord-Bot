package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public enum DiscordPerms {
    DEFAULT(0, "Default"),
    STAFF(1, "Staff"),
    STAFFPLUS(2, "Staff+"),
    BOTDEV(3, "Botdev");

    private static final SBGods main = SBGods.getInstance();

    private final int power;
    private final String name;

    DiscordPerms(int power, String name) {
        this.power = power;
        this.name = name;
    }

    public static DiscordPerms getPerms(Member member) {
        if (main.isDeveloper(member.getId())) {
            return DiscordPerms.BOTDEV;
        }
        if (member.hasPermission(Permission.MANAGE_SERVER)) {
            return DiscordPerms.STAFFPLUS;
        }
        if (member.hasPermission(Permission.MANAGE_ROLES)) {
            return DiscordPerms.STAFF;
        }

        return DiscordPerms.DEFAULT;
    }

    public String getName() {
        return name;
    }

    public int getPower() {
        return power;
    }
}
