package com.fs.starfarer.api.impl.campaign.missions.academy;

import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepRewards;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch;

public abstract class GABaseMission extends HubMissionWithSearch {

	/**
	 * Whether most GA missions have a time limit. Some will regardless of this setting.
	 */
	public static boolean WITH_TIME_LIMIT = false;
	
	protected String department = null;
	
	public void pickAnyDepartment() {
		pickDepartment(GADepartments.SOCIAL,
					   GADepartments.INDUSTRIAL, 
					   GADepartments.MILITARY,
					   GADepartments.SCIENCE,
					   GADepartments.WEIRD);
	}
	public void pickDepartment(String ... tags) {
		department = GADepartments.pick(genRandom, tags);
	}
	
	public void pickDepartmentAllTags(String ... tags) {
		department = GADepartments.pickWithAllTags(genRandom, tags);
	}
	
	public void setDefaultGARepRewards() {
		// start off with with 10 rep with Sebeystyen
		// transverse jump mission unlocks at 50 rep
		// this'll determine how many missions on average it takes
		setRepRewardPerson(RepRewards.VERY_HIGH);
		setRepRewardFaction(RepRewards.MEDIUM);
	}

}
