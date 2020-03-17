package com.confusinguser.sbgods.entities;

import java.util.HashMap;

public class SlayerExp {

	private int zombie;
	private int spider;
	private int wolf;
	private int totalExp;


	public SlayerExp(int zombie, int spider, int wolf) {
		this.zombie = zombie;
		this.spider = spider;
		this.wolf = wolf;
		this.totalExp = zombie + spider + wolf;
	}

	public SlayerExp() {
		this.zombie = 0;
		this.spider = 0;
		this.wolf = 0;
		this.totalExp = 0;
	}

	public SlayerExp(HashMap<String, Integer> slayerHashMap) {
		this.zombie = slayerHashMap.getOrDefault("zombie", 0);
		this.spider = slayerHashMap.getOrDefault("spider", 0);
		this.wolf = slayerHashMap.getOrDefault("wolf", 0);
		this.totalExp = zombie + spider + wolf;
	}

	public int getZombie() {
		return zombie;
	}

	public int getSpider() {
		return spider;
	}

	public int getWolf() {
		return wolf;
	}

	public int getTotalExp() {
		return totalExp;
	}
}
