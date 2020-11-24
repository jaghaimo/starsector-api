package com.fs.starfarer.api.characters;

import java.util.List;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.util.DynamicStatsAPI;

public interface MutableCharacterStatsAPI {

	public static interface SkillLevelAPI {
		float getLevel();
		void setLevel(float level);
		SkillSpecAPI getSkill();
	}
	
	int getLevel();
	long getXP();
	
	
//	int getSkillPoints();
//	int getAptitudePoints();
//	void setSkillPoints(int points);
//	void setAptitudePoints(int points);
	
//	void addAptitudePoints(int points);
//	void addSkillPoints(int points);
	void increaseSkill(String id);
	void increaseAptitude(String id);
	
	void setSkillLevel(String id, float level);
	void setAptitudeLevel(String id, float level);
	float getAptitudeLevel(String id);
	
	/**
	 * Only returns whole numbers. Float is used for convenience to avoid some extra casting. Other methods work likewise.
	 * @param id
	 * @return
	 */
	float getSkillLevel(String id);
	
	void addXP(long xp, TextPanelAPI textPanel, boolean withMessage);
	void addXP(long xp, TextPanelAPI textPanel);
	void addXP(long xp);
	
	
	MutableStat getWeaponOPCostMult();
	StatBonus getShipOrdnancePointBonus();
	
	StatBonus getSmallWeaponOPCost();
	StatBonus getMediumWeaponOPCost();
	StatBonus getLargeWeaponOPCost();
	
	MutableStat getRepairRateMult();
	
	MutableStat getCommandPoints();
	
	MutableStat getMarineEffectivnessMult();
	//MutableStat getCrewXPGainMult();
	
	//MutableStat getFleetSizeTravelPenaltyMult();
	//StatBonus getCombatDeploymentCost();
	
	StatBonus getMaxCapacitorsBonus();
	StatBonus getMaxVentsBonus();
	
	//StatBonus getTravelSpeedBonus();
	
	void levelUpIfNeeded();
	void levelUpIfNeeded(TextPanelAPI textPanel);
	
	DynamicStatsAPI getDynamic();
	
	int getPoints();
	void setPoints(int points);
	void addPoints(int points);
	MutableStat getOfficerNumber();
	CampaignFleetAPI getFleet();
	void setFleet(CampaignFleetAPI fleet);
	void refreshCharacterStatsEffects();
	boolean isSkipRefresh();
	void setSkipRefresh(boolean skipRefresh);
	List<SkillLevelAPI> getSkillsCopy();
	List<String> getGrantedAbilityIds();
	MutableStat getAdminNumber();
	MutableStat getOutpostNumber();
	
	void refreshGovernedOutpostEffects(MarketAPI market);
	void refreshAllOutpostsEffects(MarketAPI market);
	void refreshAllOutpostsEffectsForPlayerOutposts();

}
