package com.fs.starfarer.api.impl.campaign;

import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class TOffAlarm implements EveryFrameScript {
	private IntervalUtil tracker = new IntervalUtil(0.05f, 0.1f);
	private final CampaignFleetAPI fleet;
	
	public TOffAlarm(CampaignFleetAPI fleet) {
		this.fleet = fleet;
		notifyNearby();
	}

	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		tracker.advance(days);
		
		
		if (tracker.intervalElapsed()) {
			notifyNearby();
		}
	}
	
		
	public void notifyNearby() {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (playerFleet == null) return;
		
		//VisibilityLevel playerVis = playerFleet.getVisibilityLevelTo(fleet);
		//if (playerVis == VisibilityLevel.NONE) return;
		if (fleet.getContainingLocation() == null) return;
		
		// don't spread "hostile" status if this fleet was the aggressor or if attacking it has a low
		// reputation impact for some other reason
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_LOW_REP_IMPACT)) {
			return;
		}
		
		List<CampaignFleetAPI> fleets = fleet.getContainingLocation().getFleets();
		for (CampaignFleetAPI other : fleets) {
			VisibilityLevel level = other.getVisibilityLevelTo(fleet);
			if (level == VisibilityLevel.NONE) continue;
			
			if (level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS && other.getFaction() == fleet.getFaction()) {
				MemoryAPI mem = other.getMemoryWithoutUpdate();
				Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF, "tOffAlarm", true, 1f);
			}
		}
	}

	public boolean isDone() {
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		if (mem.getBoolean(MemFlags.MEMORY_KEY_LOW_REP_IMPACT)) return true;
		return !(mem.is(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF, true) &&
			   mem.is(MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF, true));
	}

	public boolean runWhilePaused() {
		return false;
	}

}
