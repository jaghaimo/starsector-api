package com.fs.starfarer.api.impl.campaign;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FleetFilter;

public class HasslePlayerScript implements EveryFrameScript {

	public static final String HASSLE_COMPLETE_KEY = "$hassleComplete";
	public static final String HASSLE_TIMEOUT_KEY = "$hassleTimeout";
	
	private IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	private float currDuration = 0f;
	private float currElapsed = 0f;
	private CampaignFleetAPI curr = null;
	private Vector2f startHyperLoc;
	private float leashRange = 0f;
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
		if (curr != null) {
			maintainOngoingHassle(days);
			return;
		}
		
		interval.advance(days);
		if (!interval.intervalElapsed()) return;
		
		final float MAX_RANGE_FROM_PLAYER = 2000;
		
		final MemoryAPI global = Global.getSector().getMemoryWithoutUpdate();
		//if (global.contains(HASSLE_TIMEOUT_KEY)) return;
		
		final CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null || player.isInHyperspace()) return;

		
		List<CampaignFleetAPI> fleets = Misc.findNearbyFleets(player, MAX_RANGE_FROM_PLAYER, new FleetFilter() {
			public boolean accept(CampaignFleetAPI curr) {
				if (curr.getFaction().isPlayerFaction()) return false;
				if (curr.isHostileTo(player)) return false;
				if (curr.isStationMode()) return false;
				if (!curr.getMemoryWithoutUpdate().getBoolean(MemFlags.WILL_HASSLE_PLAYER)) return false;
				if (curr.getMemoryWithoutUpdate().getBoolean(MemFlags.FLEET_SPECIAL_ACTION)) return false;
				
				String type = curr.getMemoryWithoutUpdate().getString(MemFlags.HASSLE_TYPE);
				if (type == null || type.isEmpty()) return false;
				String timeoutKey = HASSLE_TIMEOUT_KEY + "_" + type;
				if (global.contains(timeoutKey)) return false;
				
				if (curr.getAI() instanceof ModularFleetAIAPI) {
					ModularFleetAIAPI ai = (ModularFleetAIAPI) curr.getAI();
					if (ai.isFleeing()) return false;
					if (curr.getInteractionTarget() instanceof CampaignFleetAPI) return false;
				}
				
				VisibilityLevel vis = player.getVisibilityLevelTo(curr);
				if (vis == VisibilityLevel.NONE) return false;
				return true;
			}
		});
		
		if (fleets.isEmpty()) return;
		
		float minDist = Float.MAX_VALUE;
		CampaignFleetAPI closestHassler = null;
		for (CampaignFleetAPI curr : fleets) {
			float dist = Misc.getDistance(player.getLocation(), curr.getLocation());
			if (dist < minDist) {
				minDist = dist;
				closestHassler = curr;
			}
		}
		
		if (closestHassler == null) return;
		
		curr = closestHassler;

		boolean hassle = (float) Math.random() < 0.25f;
		
		if (hassle) {
			currDuration = 10f + (float) Math.random() * 5f;
			currElapsed = 0f;
			MemoryAPI mem = curr.getMemoryWithoutUpdate();
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "hassle", true, 1f);
			mem.set(MemFlags.FLEET_SPECIAL_ACTION, true, 1f);
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "hassle", true, currDuration);
			
			startHyperLoc = curr.getLocationInHyperspace();
			leashRange = Global.getSettings().getFloat("commRelayRangeAroundSystem");
			if (curr.isInHyperspace()) {
				leashRange *= 2f;
			}
			
			float timeoutDuration = 20f + (float) Math.random() * 10f;
			
			String type = curr.getMemoryWithoutUpdate().getString(MemFlags.HASSLE_TYPE);
			if (type != null && !type.isEmpty()) {
				String timeoutKey = HASSLE_TIMEOUT_KEY + "_" + type;
				global.set(timeoutKey, true, timeoutDuration);
			}
			//global.set(HASSLE_TIMEOUT_KEY, true, timeoutDuration);
		} else {
			curr = null;
		}
	}

	public void maintainOngoingHassle(float days) {
		if (!curr.isAlive()) {
			cleanUpCurr();
			return;
		}
		if (curr.isHostileTo(Global.getSector().getPlayerFleet())) {
			cleanUpCurr();
			return;
		}
		
		currElapsed += days;
		if (currElapsed > currDuration) {
			cleanUpCurr();
			return;
		}
		

		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (startHyperLoc != null) {
			float dist = Misc.getDistanceLY(player.getLocationInHyperspace(), startHyperLoc);
			if (dist > leashRange) {
				cleanUpCurr();
				return;
			}
		}
//		if (player.isInHyperspace() || player.isInHyperspaceTransition()) {
//			cleanUpCurr();
//			return;
//		}
		
		MemoryAPI mem = curr.getMemoryWithoutUpdate();
		if (mem.getBoolean(HASSLE_COMPLETE_KEY)) { // this happens when fleet interacts w/ player
			cleanUpCurr();
			return;
		}
		Misc.setFlagWithReason(mem, MemFlags.FLEET_BUSY, "hassle", true, 1f);
		mem.set(MemFlags.FLEET_SPECIAL_ACTION, true, 1f);
		// if visible, keep extending "pursue player" duration by a day
		// so, player has to lose the hassling fleet for a day to get away
		VisibilityLevel vis = player.getVisibilityLevelTo(curr);
		if (vis != VisibilityLevel.NONE) {
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "hassle", true, 1f);
		}
		
		
	}
	
	
	protected void cleanUpCurr() {
		if (curr != null) {
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			FleetAssignmentDataAPI a = curr.getCurrentAssignment();
			if (a != null && a.getAssignment() == FleetAssignment.INTERCEPT &&
					a.getTarget() == pf) {
				curr.removeFirstAssignmentIfItIs(a.getAssignment());
			}
			curr.setInteractionTarget(null);
			if (curr.getAI() instanceof ModularFleetAIAPI) {
				ModularFleetAIAPI ai = (ModularFleetAIAPI) curr.getAI();
				if (ai.getTacticalModule().getTarget() == pf) {
					ai.getTacticalModule().setTarget(null);
				}
			}
			
			MemoryAPI mem = curr.getMemoryWithoutUpdate();
			Misc.setFlagWithReason(mem, MemFlags.FLEET_BUSY, "hassle", false, 0f);
			mem.unset(MemFlags.FLEET_SPECIAL_ACTION);
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "hassle", false, 0f);
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "hassle", false, 0f);
			mem.unset(HASSLE_COMPLETE_KEY);
			curr = null;
			currDuration = currElapsed = 0f;
			startHyperLoc = null;
			leashRange = 0f;
		}
	}
	

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
}













