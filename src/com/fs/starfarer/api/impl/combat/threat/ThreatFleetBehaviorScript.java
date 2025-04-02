package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class ThreatFleetBehaviorScript implements EveryFrameScript {

	public static float MIN_SECONDS_TO_PURSUE_AFTER_SEEN_BY_PLAYER = 30f;
	public static float MAX_SECONDS_TO_PURSUE_AFTER_SEEN_BY_PLAYER = 120f;
	protected StarSystemAPI system;
	protected CampaignFleetAPI fleet;
	protected DisposableFleetManager manager;
	
	protected float seenByPlayerTimeout = 0f;
	
	
	public ThreatFleetBehaviorScript(CampaignFleetAPI fleet, StarSystemAPI system) {
		this.fleet = fleet;
		this.system = system;
		
		pickNext();
	}
	
	protected void pickNext() {
		float days = 1f + 1f * (float) Math.random();
		SectorEntityToken target = system.createToken(0, 0);
		float distFromTarget = Misc.getDistance(fleet, target);
		if (distFromTarget < 7000f) target = null;
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player != null) {
			float distFromPlayer = Misc.getDistance(fleet, player);
			if (distFromPlayer < 4000f) target = null;
		}
		
		fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, days, "cruising");
	}

	public void advance(float amount) {
		if (fleet.getCurrentAssignment() == null) {
			pickNext();
		}
		
		seenByPlayerTimeout -= amount;

		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return;
		
		boolean playerHasSensorMods = Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean(
									DisposableThreatFleetManager.SENSOR_MODS_KEY);
		//playerHasSensorMods = true;
		if (playerHasSensorMods) {
			fleet.getStats().getDynamic().getStat(Stats.DETECTED_BY_PLAYER_RANGE_MULT).modifyMult(
					DisposableThreatFleetManager.THREAT_DETECTED_RANGE_MULT_ID, 
					DisposableThreatFleetManager.ONSLAUGHT_MKI_SENSOR_MODIFICATIONS_RANGE_MULT);
		} else {
			fleet.getStats().getDynamic().getStat(Stats.DETECTED_BY_PLAYER_RANGE_MULT).unmodifyMult(
					DisposableThreatFleetManager.THREAT_DETECTED_RANGE_MULT_ID);
		}
		
		
		boolean visibleToPlayer = fleet.isVisibleToPlayerFleet() && player.isVisibleToSensorsOf(fleet);
		if (!Global.getSettings().isCampaignSensorsOn() && fleet.isInCurrentLocation()) {
			float dist = Misc.getDistance(fleet, player);
			dist -= fleet.getRadius() + player.getRadius();
			if (playerHasSensorMods) {
				dist /= DisposableThreatFleetManager.ONSLAUGHT_MKI_SENSOR_MODIFICATIONS_RANGE_MULT;
			}
			boolean asb = player.getAbility(Abilities.SENSOR_BURST) != null &&
					player.getAbility(Abilities.SENSOR_BURST).isActive();
			visibleToPlayer = dist < 150f || asb && dist < 500f; 
		}
		
		
		if (visibleToPlayer) {
			setSeenByPlayer();
		}
		if (seenByPlayerTimeout > 0f) {
			visibleToPlayer = true;
		}
		//visibleToPlayer = false;
		
		if (!visibleToPlayer) {
			if (fleet.getAI() instanceof ModularFleetAIAPI) {
				ModularFleetAIAPI ai = (ModularFleetAIAPI) fleet.getAI();
				for (int i = 0; i < 3; i++) {
					ai.getNavModule().avoidEntity(player, 3000f, 5000f, 0.2f);
				}
				
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, false);
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, true);
			}
		} else {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, false);
		}
	}
	
	public void setSeenByPlayer() {
		seenByPlayerTimeout = MIN_SECONDS_TO_PURSUE_AFTER_SEEN_BY_PLAYER +
				(MAX_SECONDS_TO_PURSUE_AFTER_SEEN_BY_PLAYER - MIN_SECONDS_TO_PURSUE_AFTER_SEEN_BY_PLAYER) * (float) Math.random();
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	

}










