package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

public class TriTachMajorLoanBarEventCreator extends BaseBarEventCreator {
	
	public PortsideBarEvent createBarEvent() {
		return new TriTachMajorLoanBarEvent();
	}

	@Override
	public float getBarEventFrequencyWeight() {
		if (Global.getSector().getFaction(Factions.TRITACHYON).getRelToPlayer().isAtBest(RepLevel.HOSTILE)) {
			return 0f;
		}
		
		float repaid = Global.getSector().getMemoryWithoutUpdate().getFloat(TriTachLoanIntel.NUM_REPAID_LOANS);
		if (repaid <= 0){
			return 0f;
		}
		
		return super.getBarEventFrequencyWeight();
	}

	@Override
	public float getBarEventAcceptedTimeoutDuration() {
		return TriTachMajorLoanBarEvent.REPAYMENT_DAYS_MAJOR + 400f * (float) Math.random();
	}
}
