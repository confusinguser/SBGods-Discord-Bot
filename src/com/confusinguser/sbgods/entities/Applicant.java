package com.confusinguser.sbgods.entities;

import com.confusinguser.sbgods.SBGods;

import net.dv8tion.jda.api.entities.User;

public class Applicant {

	private SBGods main;

	private SkyblockPlayer skyblockPlayer;
	private User discordUser;
	private HypixelGuild guild;
	private int rating;

	public Applicant(SkyblockPlayer skyblockPlayer, User discordUser, HypixelGuild guild) {
		this.main = SBGods.instance;
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
		if (main.getApiUtil().getPlayerSlayerExp(skyblockPlayer.getUUID()).getTotalExp() >= guild.getSlayerExpRec() && 
				main.getSBUtil().toSkillExp(main.getApiUtil().getBestPlayerSkillLevels(skyblockPlayer.getUUID()).getAvgSkillLevel()) >= guild.getSkillExpRec()) {
			return true;
		}
		return false;
	}
	
	private void updateRating() {
		this.rating = ((main.getApiUtil().getPlayerSlayerExp(skyblockPlayer.getUUID()).getTotalExp() / guild.getSlayerExpRec()) * (main.getSBUtil().toSkillExp(main.getApiUtil().getBestPlayerSkillLevels(skyblockPlayer.getUUID()).getAvgSkillLevel() / guild.getSkillExpRec()))) / 2;		
	}
}
