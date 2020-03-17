package com.confusinguser.sbgods.utils;

import com.confusinguser.sbgods.SBGods;

public class LangUtil {

	SBGods main;

	public LangUtil(SBGods main) {
		this.main = main;
	}

	public String makePossessiveForm(String text) {
		if (text.endsWith("s")) {
			return text + "\'";
		}
		return text + "\'s";
	}

	public String toLowerCaseButFirstLetter(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
	}
}
