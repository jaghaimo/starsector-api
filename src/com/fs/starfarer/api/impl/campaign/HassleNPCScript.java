package com.fs.starfarer.api.impl.campaign;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.ApplyCRDamage;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

/**
 * Unlike HasslePlayerScript, this is a fleet-fleet interaction and needs to be initiated
 * by some other script.
 *
 * Copyright 2023 Fractal Softworks, LLC
 */
public class HassleNPCScript implements EveryFrameScript {

	public static String HASSLE_ASSIGNMENT_ID = "hassle_assignment_id";
	
	public static class HassleParams {
		public String fleetAction = "conducting an inspection";
		public String targetAction = "standing by for inspection";
		public float minDurDays = 2f;
		public float maxDurDays = 3f;
		public float crDamageMult = 1f;
		
		// used by NPCHassler, *not* by HassleNPCScript, but convenient to put here
		public String timeoutKey = "$NPCHassleTimeout";
		public float minGlobalTimeout = 1, maxGlobalTimeout = 2;
		public float minTargetTimeout = 25, maxTargetTimeout = 35;
	}
	
	protected CampaignFleetAPI fleet;
	protected CampaignFleetAPI target;
	protected Vector2f loc1, loc2;
	protected HassleParams params = new HassleParams();
	protected float durDays = 3f;

	private IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	boolean done = false;
	
	public HassleNPCScript(CampaignFleetAPI fleet, CampaignFleetAPI target) {
		this(fleet, target, "conducting an inspection", "standing by for inspection");
	}

	public HassleNPCScript(CampaignFleetAPI fleet, CampaignFleetAPI target, HassleParams params) {
		this.fleet = fleet;
		this.target = target;
		
		this.params = params;
		
		durDays = params.minDurDays + (params.maxDurDays - params.minDurDays) * (float) Math.random();
		interval.forceIntervalElapsed();
	}
	
	public HassleNPCScript(CampaignFleetAPI fleet, CampaignFleetAPI target, String fleetAction, String targetAction) {
		this.fleet = fleet;
		this.target = target;
		
		this.params.fleetAction = fleetAction;
		this.params.targetAction = targetAction;
		
		durDays = params.minDurDays + (params.maxDurDays - params.minDurDays) * (float) Math.random();
		
		interval.forceIntervalElapsed();
	}


	public float getDurDays() {
		return durDays;
	}

	public void setDurDays(float durDays) {
		this.durDays = durDays;
	}

	public void abort() {
		done = true;
		
		cleanUpFleet(fleet);
		cleanUpFleet(target);
	}
	
	protected void cleanUpFleet(CampaignFleetAPI fleet) {
		if (fleet.getAI() != null) {
			Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.FLEET_BUSY, "npc_hassle", false, 0f);
			fleet.getMemoryWithoutUpdate().unset(MemFlags.FLEET_SPECIAL_ACTION);
			fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED);
			
			FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
			if (curr != null && HASSLE_ASSIGNMENT_ID.equals(curr.getCustom())) {
				fleet.removeFirstAssignment();
			}
		}
	}

	public void advance(float amount) {
		if (done) return;
		
		
		if (target.isPlayerFleet()) {
			abort();
			return;
		}
		if (fleet.getBattle() != null || target.getBattle() != null) {
			abort();
			return;
		}
		if (fleet.getAI() != null && (fleet.getAI().isFleeing() || fleet.getAI().isMaintainingContact())) {
			abort();
			return;
		}
		if (target.getAI() != null && (target.getAI().isFleeing() || target.getAI().isMaintainingContact())) {
			abort();
			return;
		}
		if (fleet.getContainingLocation() != target.getContainingLocation()) {
			abort();
			return;
		}
		if (fleet.isHostileTo(target) || fleet.getFaction() == target.getFaction()) {
			abort();
			return;
		}
		
		
		float days = Global.getSector().getClock().convertToDays(amount);
		durDays -= days;
		if (durDays <= 0) {
			abort();
			if (params.crDamageMult > 0) {
				float damageFP = fleet.getFleetPoints() * 0.2f;
				ApplyCRDamage.applyCRDamage(target, damageFP, params.crDamageMult, "Vindictive inspection", null, new Random());
			}
			return;
		}
		
		if (loc1 == null) {
			loc1 = new Vector2f(target.getLocation());
			Vector2f.add(loc1, fleet.getLocation(), loc1);
			loc1.scale(0.5f);
			loc2 = Misc.getPointAtRadius(loc1, fleet.getRadius() + target.getRadius());
		}
		
		fleet.setMoveDestinationOverride(loc2.x, loc2.y);
		target.setMoveDestinationOverride(loc1.x, loc1.y);
		
		interval.advance(days);
		if (!interval.intervalElapsed()) return;
		
		
		
		if (fleet.getAI() != null) {
			Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.FLEET_BUSY, "npc_hassle", true, 0.4f);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true, 0.4f);
			fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_SPECIAL_ACTION, true, 0.4f);
			FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
			if (curr == null || !HASSLE_ASSIGNMENT_ID.equals(curr.getCustom())) {
				fleet.clearAssignments();
				fleet.addAssignmentAtStart(FleetAssignment.HOLD, null, durDays + 1f, params.fleetAction, null);
				fleet.getCurrentAssignment().setCustom(HASSLE_ASSIGNMENT_ID);
			}
		}
		if (target.getAI() != null) {
			Misc.setFlagWithReason(target.getMemoryWithoutUpdate(), MemFlags.FLEET_BUSY, "npc_hassle", true, 0.4f);
			target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true, 0.4f);
			target.getMemoryWithoutUpdate().set(MemFlags.FLEET_SPECIAL_ACTION, true, 0.4f);
			
			FleetAssignmentDataAPI curr = target.getAI().getCurrentAssignment();
			curr = null;
			if (curr == null || !HASSLE_ASSIGNMENT_ID.equals(curr.getCustom())) {
				target.clearAssignments();
				target.addAssignmentAtStart(FleetAssignment.HOLD, null, durDays + 1f, params.targetAction, null);
				target.getCurrentAssignment().setCustom(HASSLE_ASSIGNMENT_ID);
			}
		}
	}

	public boolean isDone() {
		return done;
	}

	public boolean runWhilePaused() {
		return false;
	}

	public float getCrDamageMult() {
		return params.crDamageMult;
	}

	public void setCrDamageMult(float crDamageMult) {
		this.params.crDamageMult = crDamageMult;
	}

	public HassleParams getParams() {
		return params;
	}
	
	
}













