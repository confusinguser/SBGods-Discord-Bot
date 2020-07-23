package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

<<<<<<< HEAD:src/java/com/confusinguser/sbgods/entities/DiscordPerm.java
public enum DiscordPerm {
=======
public enum DiscordPerms {
>>>>>>> b7bbc11fe2a548c228a94dbb78af06564540a375:src/java/com/confusinguser/sbgods/entities/DiscordPerms.java
    DEFAULT("Default"),
    STAFF("Staff"),
    STAFFPLUS("Staff+"),
    BOTDEV("Botdev");

    private static final SBGods main = SBGods.getInstance();

    private final String name;

<<<<<<< HEAD:src/java/com/confusinguser/sbgods/entities/DiscordPerm.java
    DiscordPerm(String name) {
=======
    DiscordPerms(String name) {
>>>>>>> b7bbc11fe2a548c228a94dbb78af06564540a375:src/java/com/confusinguser/sbgods/entities/DiscordPerms.java
        this.name = name;
    }

    public static DiscordPerm getPerms(Member member) {
        if (main.isDeveloper(member.getId())) {
            return DiscordPerm.BOTDEV;
        }
        if (member.hasPermission(Permission.MANAGE_SERVER)) {
            return DiscordPerm.STAFFPLUS;
        }
        if (member.hasPermission(Permission.MANAGE_ROLES)) {
            return DiscordPerm.STAFF;
        }

        return DiscordPerm.DEFAULT;
    }

    public String getName() {
        return name;
    }

    public int getPower() {
        return ordinal();
    }
}
