package com.fs.starfarer.api.impl.campaign;

import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.HassleNPCScript.HassleParams;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FleetFilter;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class NPCHassler implements EveryFrameScript {
	
	protected CampaignFleetAPI fleet;
	protected HassleParams params;
	protected StarSystemAPI restrictTo;
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	public NPCHassler(CampaignFleetAPI fleet) {
		this(fleet, new HassleParams(), null);
	}
	public NPCHassler(CampaignFleetAPI fleet, StarSystemAPI restrictTo) {
		this(fleet, new HassleParams(), restrictTo);
	}
	public NPCHassler(CampaignFleetAPI fleet, HassleParams params, StarSystemAPI restrictTo) {
		this.fleet = fleet;
		this.params = params;
		this.restrictTo = restrictTo;
	}
	
	public HassleParams getParams() {
		return params;
	}
	
	public void advance(float amount) {
//		if (!params.timeoutKey.startsWith("$")) {
//			params.timeoutKey = "$" + params.timeoutKey;
//		}
		
		float days = Misc.getDays(amount);
		interval.advance(days);
		if (!interval.intervalElapsed()) return;
		
		if (restrictTo != null && fleet.getContainingLocation() != restrictTo) return;
		if (Global.getSector().getMemoryWithoutUpdate().contains(params.timeoutKey)) return;
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.FLEET_SPECIAL_ACTION)) return;
		
		if (fleet.getBattle() != null) return;
		if (fleet.getAI() != null && (fleet.getAI().isFleeing() || fleet.getAI().isMaintainingContact())) {
			return;
		}
		if (fleet.getCurrentAssignment() != null && 
				fleet.getCurrentAssignment().getAssignment() == FleetAssignment.ORBIT_PASSIVE) {
			return;
		}
		
		
		List<CampaignFleetAPI> targets = Misc.findNearbyFleets(fleet, 1000f, new FleetFilter() {
			public boolean accept(CampaignFleetAPI other) {
				return isTargetAllowed(other);
			}
		});
		
		WeightedRandomPicker<CampaignFleetAPI> picker = new WeightedRandomPicker<CampaignFleetAPI>();
		picker.addAll(targets);
		CampaignFleetAPI target = picker.pick();
		if (target != null) {
			fleet.getContainingLocation().addScript(new HassleNPCScript(fleet, target));
			float globalTimeout = params.minGlobalTimeout + 
					(params.maxGlobalTimeout - params.minGlobalTimeout) * (float) Math.random();
			Global.getSector().getMemoryWithoutUpdate().set(params.timeoutKey, true, globalTimeout);
			float targetTimeout = params.minTargetTimeout + 
					(params.maxTargetTimeout - params.minTargetTimeout) * (float) Math.random();
			target.getMemoryWithoutUpdate().set(params.timeoutKey, true, targetTimeout);
		}
	}
	
	protected boolean isTargetAllowed(CampaignFleetAPI target) {
		if (target.isPlayerFleet() || target.getAI() == null) return false;
		if (target.isHostileTo(fleet)) return false;
		if (target.isStationMode()) return false;
		if (target.getBattle() != null) return false;
		
		if (target.getFaction() == fleet.getFaction()) return false;
		if (target.getMemoryWithoutUpdate().contains(params.timeoutKey)) return false;
		
		if (target.getAI() instanceof ModularFleetAIAPI) {
			ModularFleetAIAPI ai = (ModularFleetAIAPI) target.getAI();
			if (ai.isFleeing() || ai.isMaintainingContact()) return false;
			if (fleet.getInteractionTarget() instanceof CampaignFleetAPI) return false;
		}
		
		if (!isTargetRightTypeOfFleet(target)) return false;
		
		VisibilityLevel vis = target.getVisibilityLevelTo(fleet);
		if (vis == VisibilityLevel.NONE) return false;
		return true;
	}
	
	/**
	 * This is called in addition to the standard faction/hostility/visibility/etc checks in isTargetAllowed().
	 * @param target
	 * @return
	 */
	protected boolean isTargetRightTypeOfFleet(CampaignFleetAPI target) {
		if (Misc.isTrader(target)) return true; 
		if (Misc.isPirate(target)) return false;
		if (Misc.isPatrol(target) && !target.getFaction().isPlayerFaction()) return false;
		if (Misc.isWarFleet(target)) return false;
		if (target.getFaction().getCustomBoolean(Factions.CUSTOM_DECENTRALIZED)) return true;
		return false;
	}
	
	
	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
}
