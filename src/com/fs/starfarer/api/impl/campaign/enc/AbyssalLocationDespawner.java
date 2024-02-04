package com.fs.starfarer.api.impl.campaign.enc;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class AbyssalLocationDespawner implements EveryFrameScript {

	protected IntervalUtil interval = new IntervalUtil(1f, 2f); 
	protected StarSystemAPI system;
	//protected float elapsed;

	public AbyssalLocationDespawner(StarSystemAPI system) {
		this.system = system;
	}

	public void advance(float amount) {
		if (system == null) return;
		
		
		interval.advance(amount);
		if (interval.intervalElapsed()) {
			
			// this part is now handled by StrandedGiveTJScript
//			if (system.isCurrentLocation()) {
//				elapsed += interval.getIntervalDuration();
//				CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
//				if (!pf.hasAbility(Abilities.TRANSVERSE_JUMP) && elapsed > 60f &&
//						!Global.getSector().getCampaignUI().isShowingDialog() && 
//						!Global.getSector().getCampaignUI().isShowingMenu()) {
//					Misc.showRuleDialog(pf, "StrandedInDeepSpace");
//				}
//				return;
//			} else {
//				elapsed = 0f;
//			}
			
			if (system.getDaysSinceLastPlayerVisit() < 10f) {
				return;
			}
			
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			if (pf == null) {
				return;
			}
			float distLY = Misc.getDistanceLY(pf.getLocationInHyperspace(), system.getLocation());
			if (distLY < 5) {
				return;
			}
			
			Global.getSector().removeStarSystemNextFrame(system);
			
			
			List<SectorEntityToken> remove = new ArrayList<SectorEntityToken>();
			for (SectorEntityToken curr : Global.getSector().getHyperspace().getJumpPoints()) {
				JumpPointAPI jp = (JumpPointAPI) curr;
				if (jp.getDestinations().isEmpty()) continue;
				JumpDestination dest = jp.getDestinations().get(0);
				if (dest.getDestination() == null) continue;
				if (system == dest.getDestination().getStarSystem()) {
					remove.add(curr);
				}
			}
			
			for (NascentGravityWellAPI curr : Global.getSector().getHyperspace().getGravityWells()) {
				if (curr.getTarget() == null) continue;
				if (system == curr.getTarget().getStarSystem()) {
					remove.add(curr);
				}
			}
			
			remove.add(system.getHyperspaceAnchor());
			
			for (SectorEntityToken curr : remove) {
				Global.getSector().getHyperspace().removeEntity(curr);
			}
			
			system = null;
		}
	}
	
	public boolean isDone() {
		return system == null;
	}

	public boolean runWhilePaused() {
		return false;
	}	
}



