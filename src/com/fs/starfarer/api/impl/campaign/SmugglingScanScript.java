package com.fs.starfarer.api.impl.campaign;

import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin.MarketFilter;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FleetFilter;

public class SmugglingScanScript implements EveryFrameScript {

	public static final String SCAN_COMPLETE_KEY = "$smugglingScanComplete";
	public static final String MARKET_TIMEOUT_KEY = "$smugglingScanTimeout";
	
	private IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	private float currDuration = 0f;
	private float currElapsed = 0f;
	private CampaignFleetAPI curr = null;
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
		if (curr != null) {
			maintainOngoingScan(days);
			return;
		}
		
		interval.advance(days);
		if (!interval.intervalElapsed()) return;
		
		final float MAX_RANGE_FROM_MARKET = 5000;
		final float MAX_RANGE_FROM_PLAYER = 2000;
		
		final CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null || player.isInHyperspace()) return;
		final MarketAPI market = Misc.findNearestLocalMarket(player, MAX_RANGE_FROM_MARKET, new MarketFilter() {
			public boolean acceptMarket(MarketAPI market) {
				if (market.hasCondition(Conditions.FREE_PORT)) return false;
				
				MemoryAPI mem = market.getMemoryWithoutUpdate();
				//mem.unset(MARKET_TIMEOUT_KEY);
				if (mem.contains(MARKET_TIMEOUT_KEY)) return false;
				return true;
			}
		});
		if (market == null) return;
		if (!market.getFaction().getCustomBoolean(Factions.CUSTOM_ALLOWS_TRANSPONDER_OFF_TRADE) && !player.isTransponderOn()) {
			return;
		}
		
		if (market.getFaction().isHostileTo(player.getFaction())) return;
		
		List<CampaignFleetAPI> patrols = Misc.findNearbyFleets(player, MAX_RANGE_FROM_PLAYER, new FleetFilter() {
			public boolean accept(CampaignFleetAPI curr) {
				if (curr.getFaction() != market.getFaction()) return false;
				if (curr.isHostileTo(player)) return false;
				if (Misc.getSourceMarket(curr) != market) return false;
				if (!curr.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET)) return false;
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
		
		if (patrols.isEmpty()) return;
		
		float minDist = Float.MAX_VALUE;
		CampaignFleetAPI closestPatrol = null;
		for (CampaignFleetAPI curr : patrols) {
			float dist = Misc.getDistance(player.getLocation(), curr.getLocation());
			if (dist < minDist) {
				minDist = dist;
				closestPatrol = curr;
			}
		}
		
		if (closestPatrol == null) return;
		
		curr = closestPatrol;

		float threshold = 0.05f;
		MemoryAPI marketMemory = market.getMemory();
		float suspicionLevel = marketMemory.getFloat(MemFlags.MEMORY_MARKET_SMUGGLING_SUSPICION_LEVEL); 
		boolean doScan = (float) Math.random() < suspicionLevel * 5f && suspicionLevel >= threshold;
		//doScan = true;
		
		if (doScan) {
			currDuration = 10f + (float) Math.random() * 5f;
			currElapsed = 0f;
			MemoryAPI mem = curr.getMemoryWithoutUpdate();
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "smugglingScan", true, 1f);
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "smugglingScan", true, currDuration);
		}
		
		
		if (suspicionLevel >= threshold) {
			float timeoutDuration = 20f + (float) Math.random() * 10f;
			marketMemory.set(MARKET_TIMEOUT_KEY, true, timeoutDuration);
		}
	}

	public void maintainOngoingScan(float days) {
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
		

		// if visible, keep extending "pursue player" duration by a day
		// so, player has to lose the patrol for a day to be able to sneak into market
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		VisibilityLevel vis = player.getVisibilityLevelTo(curr);
		
		if (vis != VisibilityLevel.NONE) {
			MemoryAPI mem = curr.getMemoryWithoutUpdate();
			if (mem.getBoolean(SCAN_COMPLETE_KEY)) { // this happens when fleet interacts w/ player
				cleanUpCurr();
				return;
			}
			//curr.getMemoryWithoutUpdate().contains("$pursuePlayer_smugglingScan");
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "smugglingScan", true, 1f);
		}
	}
	
	
	protected void cleanUpCurr() {
		if (curr != null) {
			MemoryAPI mem = curr.getMemoryWithoutUpdate();
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_PURSUE_PLAYER, "smugglingScan", false, 0f);
			Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, "smugglingScan", false, 0f);
			mem.unset(SCAN_COMPLETE_KEY);
			curr = null;
			currDuration = currElapsed = 0f;
		}
	}
	

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
}













