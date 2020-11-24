package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.impl.campaign.intel.SystemBountyIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class SystemBounty extends BaseMarketConditionPlugin {
	
	private SystemBountyIntel intel = null;
	
	public SystemBounty() {
	}

	public void apply(String id) {
	}

	public void unapply(String id) {
	}
	
	@Override
	public void setParam(Object param) {
		intel = (SystemBountyIntel) param;
	}
	
	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltip(tooltip, expanded);
	}
	
	@Override
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		intel.createSmallDescription(tooltip, getTooltipWidth(), 300f, true);
	}
	
}




