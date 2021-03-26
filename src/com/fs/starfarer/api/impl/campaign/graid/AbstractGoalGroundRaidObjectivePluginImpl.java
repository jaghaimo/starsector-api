package com.fs.starfarer.api.impl.campaign.graid;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;

public abstract class AbstractGoalGroundRaidObjectivePluginImpl extends BaseGroundRaidObjectivePluginImpl {

	protected RaidDangerLevel danger;

	public AbstractGoalGroundRaidObjectivePluginImpl(MarketAPI market, RaidDangerLevel danger) {
		super(market, null);
		this.danger = danger;
		
		int marines = danger.marineTokens;
		setMarinesRequired(marines);
	}
	
	@Override
	public String getQuantityString(int marines) {
		return "";
	}
	@Override
	public String getValueString(int marines) {
		return "";
	}
	public float getValueSortValue() {
		return super.getValueSortValue();
	}
	public int getCargoSpaceNeeded() {
		return 0;
	}
	public int getFuelSpaceNeeded() {
		return 0;
	}
	public int getProjectedCreditsValue() {
		return 0;
	}
	@Override
	public int getValue(int marines) {
		return 0;
	}
	@Override
	public float getQuantity(int marines) {
		return 0;
	}
	public RaidDangerLevel getDangerLevel() {
		return danger;
	}
	public float getQuantitySortValue() {
		float add = getName().hashCode();
		return QUANTITY_SORT_TIER_1 + add; 
	}
	

}









