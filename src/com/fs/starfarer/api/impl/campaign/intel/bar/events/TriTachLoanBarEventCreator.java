package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

public class TriTachLoanBarEventCreator extends BaseBarEventCreator {
	
	public PortsideBarEvent createBarEvent() {
		return new TriTachLoanBarEvent();
	}

	@Override
	public float getBarEventFrequencyWeight() {
		if (Global.getSector().getFaction(Factions.TRITACHYON).getRelToPlayer().isAtBest(RepLevel.HOSTILE)) {
			return 0f;
		}
		
		return super.getBarEventFrequencyWeight();
	}

	@Override
	public float getBarEventAcceptedTimeoutDuration() {
		return TriTachLoanBarEvent.REPAYMENT_DAYS + 200f * (float) Math.random();
	}
}
