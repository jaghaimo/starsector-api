package com.fs.starfarer.api.campaign;

import java.util.List;

public interface FactionDoctrineAPI {
	int getWarships();
	void setWarships(int warships);
	int getCarriers();
	void setCarriers(int carriers);
	int getPhaseShips();
	void setPhaseShips(int phaseShips);
	int getOfficerQuality();
	void setOfficerQuality(int officerQuality);
	int getShipQuality();
	void setShipQuality(int shipQuality);
	int getNumShips();
	void setNumShips(int numShips);
	int getShipSize();
	void setShipSize(int shipSize);
	int getAggression();
	void setAggression(int aggression);
	int getFleets();
	void setFleets(int fleets);
	float getCombatFreighterProbability();
	void setCombatFreighterProbability(float combatFreighterProbability);
	float getCommanderSkillsShuffleProbability();
	void setCommanderSkillsShuffleProbability(float commanderSkillsShuffleProbability);
	List<String> getCommanderSkills();
	
	/**
	 * (shipQuality - 1) multiplied by doctrineFleetQualityPerPoint from settings.json.
	 * @return
	 */
	float getShipQualityContribution();
	
	FactionDoctrineAPI clone();
	
	
	float getCombatFreighterCombatUseFraction();
	void setCombatFreighterCombatUseFraction(float combatFreighterCombatUseFraction);
	float getCombatFreighterCombatUseFractionWhenPriority();
	void setCombatFreighterCombatUseFractionWhenPriority(float combatFreighterCombatUseFractionWhenPriority);
	float getAutofitRandomizeProbability();
	void setAutofitRandomizeProbability(float autofitRandomizeProbability);
	int getTotalStrengthPoints();
	boolean isStrictComposition();
	void setStrictComposition(boolean strictComposition);
}
