package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;



public interface GroundRaidTargetPickerDelegate {
	void pickedGroundRaidTargets(List<GroundRaidObjectivePlugin> data);
	void cancelledGroundRaidTargetPicking();
	int getNumMarineTokens();
	String getRaidEffectiveness();
	String getProjectedMarineLosses(List<GroundRaidObjectivePlugin> data);
	Color getMarineLossesColor(List<GroundRaidObjectivePlugin> data);
	int getProjectedCreditsValue(List<GroundRaidObjectivePlugin> data);
	int getCargoSpaceNeeded(List<GroundRaidObjectivePlugin> data);
	int getFuelSpaceNeeded(List<GroundRaidObjectivePlugin> data);
	MutableStat getMarineLossesStat(List<GroundRaidObjectivePlugin> data);
	float getAverageMarineLosses(List<GroundRaidObjectivePlugin> data);
	
	boolean isDisruptIndustryMode();
	boolean isCustomOnlyMode();
}










