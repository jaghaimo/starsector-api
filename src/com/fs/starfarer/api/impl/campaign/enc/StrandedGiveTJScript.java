package com.fs.starfarer.api.impl.campaign.enc;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class StrandedGiveTJScript implements EveryFrameScript {

	protected IntervalUtil interval = new IntervalUtil(5f, 10f); 
	protected StarSystemAPI prev; 
	protected float elapsed;

	public StrandedGiveTJScript() {
	}

	public void advance(float amount) {
		interval.advance(amount);
		if (interval.intervalElapsed()) {
			LocationAPI curr = Global.getSector().getCurrentLocation();
			if (!(curr instanceof StarSystemAPI)) return;
			
			StarSystemAPI system = (StarSystemAPI) curr;
			
			if (prev != system) {
				prev = system;
				elapsed = 0f;
			}
			
			for (SectorEntityToken s : system.getJumpPoints()) {
				JumpPointAPI jp = (JumpPointAPI) s;
				if (!jp.getDestinations().isEmpty()) {
					return;
				}
			}
			
			elapsed += interval.getIntervalDuration();
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			if (!pf.hasAbility(Abilities.TRANSVERSE_JUMP) && elapsed > 60f &&
					!Global.getSector().getCampaignUI().isShowingDialog() && 
					!Global.getSector().getCampaignUI().isShowingMenu()) {
				Misc.showRuleDialog(pf, "StrandedInDeepSpace");
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



