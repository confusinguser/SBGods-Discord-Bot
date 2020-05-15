/*package com.confusinguser.sbgods.utils;

import java.util.ArrayList;

import com.confusinguser.sbgods.SBGods;
import com.confusinguser.sbgods.entities.Applicant;

public class ApplicationUtil {

	// private SBGods main;
	private final ArrayList<Applicant> applicants = new ArrayList<>();


	public ApplicationUtil(SBGods main) {
		// this.main = main;
	}

	public void addApplicant(Applicant applicant) {
		applicants.add(applicant);
	}

	public Applicant getBestApplicant() {
		Applicant highestApplicant = null;
		for (Applicant applicant : applicants) {
			if (highestApplicant == null || applicant.getRating() > highestApplicant.getRating()) {
				highestApplicant = applicant;
			}
		}
		return highestApplicant;
	}

	public ArrayList<Applicant> getApplicationList() {
		return applicants;
	}
}
*/