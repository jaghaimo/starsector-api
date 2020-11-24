package com.fs.starfarer.api.impl.campaign.intel.raid;


public abstract class ActionStage extends BaseRaidStage {

	public ActionStage(RaidIntel raid) {
		super(raid);
	}

	@Override
	public void notifyStarted() {
		updateRoutes();
	}

	protected void updateRoutes() {
	}
	
	@Override
	protected void updateStatus() {
		super.updateStatus();
	}

	
	public void advance(float amount) {
		super.advance(amount);
	}
	
	public abstract boolean isPlayerTargeted();
}
