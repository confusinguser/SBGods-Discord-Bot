package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.entities.User;

public class Applicant {

    private final SBGods main;

    private final SkyblockPlayer skyblockPlayer;
    private final User discordUser;
    private final HypixelGuild guild;
    private int rating;

    public Applicant(SkyblockPlayer skyblockPlayer, User discordUser, HypixelGuild guild) {
        this.main = SBGods.getInstance();
        this.skyblockPlayer = skyblockPlayer;
        this.discordUser = discordUser;
        this.guild = guild;
        updateRating();
        this.rating = getRating();
    }

    public SkyblockPlayer getSkyblockPlayer() {
        return skyblockPlayer;
    }

    public User getDiscordUser() {
        return discordUser;
    }

    public int getRating() {
        return rating;
    }

    public boolean meetsRequirements() {
        return main.getApiUtil().getPlayerSlayerExp(skyblockPlayer.getUUID()).getTotalExp() >= guild.getSlayerExpRec() &&
                main.getSBUtil().toSkillExp(main.getApiUtil().getBestPlayerSkillLevels(skyblockPlayer.getUUID()).getAvgSkillLevel()) >= guild.getSkillExpRec();
    }

    private void updateRating() {
        this.rating = ((main.getApiUtil().getPlayerSlayerExp(skyblockPlayer.getUUID()).getTotalExp() / guild.getSlayerExpRec()) * (main.getSBUtil().toSkillExp(main.getApiUtil().getBestPlayerSkillLevels(skyblockPlayer.getUUID()).getAvgSkillLevel() / guild.getSkillExpRec()))) / 2;
    }
}
