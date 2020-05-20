package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;
import net.dv8tion.jda.api.entities.User;

public class Applicant {

    private final SBGods main;

    private final Player player;
    private final User discordUser;
    private final HypixelGuild guild;
    private int rating;

    public Applicant(Player player, User discordUser, HypixelGuild guild) {
        this.main = SBGods.getInstance();
        this.player = player;
        this.discordUser = discordUser;
        this.guild = guild;
        updateRating();
        this.rating = getRating();
    }

    public Player getPlayer() {
        return player;
    }

    public User getDiscordUser() {
        return discordUser;
    }

    public int getRating() {
        return rating;
    }

    public boolean meetsRequirements() {
        return main.getApiUtil().getPlayerSlayerExp(player.getUUID()).getTotalExp() >= guild.getSlayerExpRec() &&
                main.getSBUtil().toSkillExp(main.getApiUtil().getBestProfileSkillLevels(player.getUUID()).getAvgSkillLevel()) >= guild.getSkillExpRec();
    }

    private void updateRating() {
        this.rating = ((main.getApiUtil().getPlayerSlayerExp(player.getUUID()).getTotalExp() / guild.getSlayerExpRec()) * (main.getSBUtil().toSkillExp(main.getApiUtil().getBestProfileSkillLevels(player.getUUID()).getAvgSkillLevel() / guild.getSkillExpRec()))) / 2;
    }
}
