package com.fs.starfarer.api.characters;

import java.util.List;

import com.fs.starfarer.api.campaign.TextPanelAPI;



public interface OfficerDataAPI {
	PersonAPI getPerson();
	void setPerson(PersonAPI person);
	void addXP(long xp);
	void addXP(long xp, TextPanelAPI textPanel);
	boolean canLevelUp();
	void levelUp(String skillId);
	List<String> getSkillPicks();
	boolean isMadePicks();
	
	/**
	 * Automatically called on level-up, but will re-roll skill picks if called again. 
	 */
	void makeSkillPicks();

}
