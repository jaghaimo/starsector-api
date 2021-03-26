package com.fs.starfarer.api.impl.campaign.missions.hub;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class MissionFleetInterceptPlayerIfNearby implements EveryFrameScript {

	protected CampaignFleetAPI fleet;
	protected BaseHubMission mission;
	protected Set<Object> stages = new HashSet<Object>();
	protected boolean done = false;
	protected float delay;
	protected float delayRemaining;
	protected  float maxRange;
	protected boolean repeatable;
	protected boolean mustBeStrongEnoughToFight;
	
	protected IntervalUtil interval = new IntervalUtil(0.05f, 0.1f);
	
	public MissionFleetInterceptPlayerIfNearby(CampaignFleetAPI fleet, BaseHubMission mission, 
			boolean mustBeStrongEnoughToFight, float maxRange, boolean repeatable, float repeatDelay, List<Object> stages) {
		this.delay = repeatDelay;
		this.mission = mission;
		this.fleet = fleet;
		this.mustBeStrongEnoughToFight = mustBeStrongEnoughToFight;
		this.maxRange = maxRange;
		this.repeatable = repeatable;
		this.stages.addAll(stages);
	}
	
	public void advance(float amount) {
		if (done) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		delayRemaining -= days;
		if (delayRemaining < 0) delayRemaining = 0;
		if (delayRemaining > 0) return;
		
		interval.advance(days);
		if (!interval.intervalElapsed()) return;
		
		if (fleet.getMemoryWithoutUpdate().getBoolean("$turnOffAutoInterceptScript")) {
			done = true;
			return;
		}
		
		Object stage = mission.getCurrentStage();
		if (!stages.contains(stage)) {
			done = true;
			return;
		}
		
		if (fleet.getAI() instanceof ModularFleetAIAPI) {
			ModularFleetAIAPI ai = (ModularFleetAIAPI) fleet.getAI();
			CampaignFleetAPI player = Global.getSector().getPlayerFleet();
			
			if (mustBeStrongEnoughToFight && ai.getTacticalModule() != null) {
				EncounterOption option = ai.getTacticalModule().pickEncounterOption(null, player);
				boolean recentlyBeaten = fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER);
				if (recentlyBeaten) option = EncounterOption.DISENGAGE;
				if (option == EncounterOption.DISENGAGE || option == EncounterOption.HOLD_VS_STRONGER) {
					return;
				}
			}
			
			if (ai.getAssignmentModule() != null) {
				FleetAssignmentDataAPI curr = ai.getAssignmentModule().getCurrentAssignment();
				if (curr != null && curr.getTarget() == player &&
						(curr.getAssignment() == FleetAssignment.INTERCEPT ||
						curr.getAssignment() == FleetAssignment.FOLLOW)) {
					return;
				} else {
					
					if (player.getContainingLocation() == fleet.getContainingLocation() &&
							Misc.getDistance(player, fleet) <= maxRange + player.getRadius() + fleet.getRadius() && 
							fleet.getVisibilityLevelOfPlayerFleet() != VisibilityLevel.NONE) {
						ai.getAssignmentModule().addAssignmentAtStart(FleetAssignment.INTERCEPT, player, 3f, null);
						delayRemaining = delay;
						if (!repeatable) {
							done = true;
						}
					}
				}
			}
		}
	}
	
	public boolean isDone() {
		return done;
	}

	public boolean runWhilePaused() {
		return false;
	}
	

}










