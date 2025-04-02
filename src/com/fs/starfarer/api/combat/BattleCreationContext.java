package com.fs.starfarer.api.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;

public class BattleCreationContext {

	private float initialStepSize = 1f;
	private float initialNumSteps = 0f;
	
	
	private float initialDeploymentBurnDuration = 1f;
	private float normalDeploymentBurnDuration = 6f;
	private float escapeDeploymentBurnDuration = 1.5f;
	private float standoffRange = 6000f;
	private float initialEscapeRange = Global.getSettings().getFloat("escapeStartDistance");
	private float flankDeploymentDistance = Global.getSettings().getFloat("escapeFlankDistance");
	
	
	private CampaignFleetAPI playerFleet;
	private FleetGoal playerGoal;
	private CampaignFleetAPI otherFleet;
	private FleetGoal otherGoal;
	
	private float pursuitRangeModifier = 0f;
	
	public float extraEnemyStandoffRange = 0f;
	
	private int playerCommandPoints = 0;
	
	public boolean aiRetreatAllowed = true;
	public boolean objectivesAllowed = true;
	public boolean forceObjectivesOnMap = false;
	public boolean enemyDeployAll = false;
//	public boolean playerDefendingStation = false;
//	public boolean enemyDefendingStation = false;
	
	public boolean fightToTheLast = false;
	
	public BattleCreationContext(CampaignFleetAPI playerFleet,
			FleetGoal playerGoal, CampaignFleetAPI otherFleet,
			FleetGoal otherGoal) {
		this.playerFleet = playerFleet;
		this.playerGoal = playerGoal;
		this.otherFleet = otherFleet;
		this.otherGoal = otherGoal;
		
		if (otherFleet != null && otherFleet.getFaction() != null) {
			boolean ftl = otherFleet.getFaction().getCustomBoolean(Factions.CUSTOM_FIGHT_TO_THE_LAST);
			if (otherFleet.getMemoryWithoutUpdate().contains(MemFlags.FLEET_FIGHT_TO_THE_LAST)) {
				ftl = otherFleet.getMemoryWithoutUpdate().getBoolean(MemFlags.FLEET_FIGHT_TO_THE_LAST);
			}
			fightToTheLast = ftl;
		}
	}
	
	public int getPlayerCommandPoints() {
		return playerCommandPoints;
	}

	public void setPlayerCommandPoints(int playerCommandPoints) {
		this.playerCommandPoints = playerCommandPoints;
	}

	public CampaignFleetAPI getPlayerFleet() {
		return playerFleet;
	}

	public FleetGoal getPlayerGoal() {
		return playerGoal;
	}

	public CampaignFleetAPI getOtherFleet() {
		return otherFleet;
	}

	public FleetGoal getOtherGoal() {
		return otherGoal;
	}

	public float getPursuitRangeModifier() {
		return pursuitRangeModifier;
	}

	public void setPursuitRangeModifier(float pursuitRangeModifier) {
		this.pursuitRangeModifier = pursuitRangeModifier;
	}

	public float getInitialDeploymentBurnDuration() {
		return initialDeploymentBurnDuration;
	}

	public void setInitialDeploymentBurnDuration(float initialDeploymentBurnDuration) {
		this.initialDeploymentBurnDuration = initialDeploymentBurnDuration;
	}

	public float getNormalDeploymentBurnDuration() {
		return normalDeploymentBurnDuration;
	}

	public void setNormalDeploymentBurnDuration(float normalDeploymentBurnDuration) {
		this.normalDeploymentBurnDuration = normalDeploymentBurnDuration;
	}

	public float getEscapeDeploymentBurnDuration() {
		return escapeDeploymentBurnDuration;
	}

	public void setEscapeDeploymentBurnDuration(float escapeDeploymentBurnDuration) {
		this.escapeDeploymentBurnDuration = escapeDeploymentBurnDuration;
	}

	public float getStandoffRange() {
		return standoffRange;
	}

	public void setStandoffRange(float standoffRange) {
		this.standoffRange = standoffRange;
	}

	public float getInitialEscapeRange() {
		return initialEscapeRange;
	}

	public void setInitialEscapeRange(float initialEscapeRange) {
		this.initialEscapeRange = initialEscapeRange;
	}

	public float getFlankDeploymentDistance() {
		return flankDeploymentDistance;
	}

	public void setFlankDeploymentDistance(float sideDeploymentDistance) {
		this.flankDeploymentDistance = sideDeploymentDistance;
	}

	public float getInitialStepSize() {
		return initialStepSize;
	}

	public void setInitialStepSize(float initialStepSize) {
		this.initialStepSize = initialStepSize;
	}

	public float getInitialNumSteps() {
		return initialNumSteps;
	}

	public void setInitialNumSteps(float initialNumSteps) {
		this.initialNumSteps = initialNumSteps;
	}

	public float getExtraEnemyStandoffRange() {
		return extraEnemyStandoffRange;
	}

}




