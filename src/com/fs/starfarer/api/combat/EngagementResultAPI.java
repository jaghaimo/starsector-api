package com.fs.starfarer.api.combat;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CombatDamageData;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;

public interface EngagementResultAPI {

	EngagementResultForFleetAPI getWinnerResult();
	EngagementResultForFleetAPI getLoserResult();
	
	boolean didPlayerWin();
	boolean isPlayerOutBeforeEnd();
	void setPlayerOutBeforeEnd(boolean playerOutBeforeEnd);
	BattleAPI getBattle();
	void setBattle(BattleAPI battle);
	CombatDamageData getLastCombatDamageData();
	void setLastCombatDamageData(CombatDamageData lastCombatData);
	
//	/**
//	 * Applies ship and crew losses from the engagement to the fleets involved.
//	 */
//	void applyToFleets();
}
