package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ScavengerPiracyScript implements EveryFrameScript {

	protected IntervalUtil piracyCheck = new IntervalUtil(0.2f, 0.4f);
	protected CampaignFleetAPI fleet;
	public ScavengerPiracyScript(CampaignFleetAPI fleet) {
		this.fleet = fleet;
	}
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		piracyCheck.advance(days);
		if (piracyCheck.intervalElapsed()) {
			doPiracyCheck();
		}
	}
	
	protected void doPiracyCheck() {
		if (fleet.getBattle() != null) return;
		
		
		boolean isCurrentlyPirate = fleet.getFaction().getId().equals(Factions.PIRATES);
		
		if (fleet.isTransponderOn() && !isCurrentlyPirate) {
			return;
		}
		
		if (isCurrentlyPirate) {
			List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);
			if (visible.isEmpty()) {
				fleet.setFaction(Factions.INDEPENDENT, true);
				Misc.clearTarget(fleet, true);
			}
			return;
		}
		
		List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);
		if (visible.size() == 1) {
			int weakerCount = 0;
			for (CampaignFleetAPI other : visible) {
				if (fleet.getAI() != null && 
						Global.getSector().getFaction(Factions.PIRATES).isHostileTo(other.getFaction())) {
					EncounterOption option = fleet.getAI().pickEncounterOption(null, other, true);
					if (option == EncounterOption.ENGAGE || option == EncounterOption.HOLD) {
						float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
						VisibilityLevel level = other.getVisibilityLevelTo(fleet);
						boolean seesComp = level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS ||
										   level == VisibilityLevel.COMPOSITION_DETAILS;
						if (dist < 800f && seesComp) {
							weakerCount++;
						}
					}
				}
			}
		
			if (weakerCount == 1) {
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
				fleet.setNoFactionInName(true);
				fleet.setFaction(Factions.PIRATES, true);
			}
		}
		
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	


	
	
}










