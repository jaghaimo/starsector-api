package com.fs.starfarer.api.characters;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.campaign.TextPanelAPI;



public interface OfficerDataAPI {
	PersonAPI getPerson();
	void setPerson(PersonAPI person);
	void addXP(long xp);
	void addXP(long xp, TextPanelAPI textPanel);
	boolean canLevelUp();
	boolean canLevelUp(boolean allowAnyLevel);
	void levelUp(String skillId);
	List<String> getSkillPicks();
	boolean isMadePicks();
	
	/**
	 * Automatically called on level-up, but will re-roll skill picks if called again. 
	 */
	void makeSkillPicks();
	void addXP(long xp, TextPanelAPI textPanel, boolean clampXP);
	void makeSkillPicks(Random random);
	void levelUp(String skillId, Random random);

}
