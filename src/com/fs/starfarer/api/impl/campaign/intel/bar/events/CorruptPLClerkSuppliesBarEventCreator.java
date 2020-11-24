package com.fs.starfarer.api.impl.campaign.intel.bar.events;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;

public class CorruptPLClerkSuppliesBarEventCreator extends BaseBarEventCreator {
	
	public PortsideBarEvent createBarEvent() {
		return new CorruptPLClerkSuppliesBarEvent();
	}

}
