package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;



/**
 * NOT transient, tracks days active, and adds/removes itself as MarketImmigrationModifier 
 * if the deriving class implements that interface. Use sparingly since it'll add to savefile bloat 
 * if used across the board.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2017 Fractal Softworks, LLC
 */
public class BaseMarketConditionPlugin2 extends BaseMarketConditionPlugin {

	protected float daysActive = 0f;
	@Override
	public void advance(float amount) {
		super.advance(amount);
		float days = Global.getSector().getClock().convertToDays(amount);
		daysActive += days;
	}
	
	public float getDaysActive() {
		return daysActive;
	}
	
	public void setDaysActive(float daysActive) {
		this.daysActive = daysActive;
	}

	public void apply(String id) {
		if (this instanceof MarketImmigrationModifier) {
			market.addTransientImmigrationModifier((MarketImmigrationModifier) this);
		}
	}
	
	@Override
	public boolean isTransient() {
		return false;
	}

	public void unapply(String id) {
		if (this instanceof MarketImmigrationModifier) {
			market.removeTransientImmigrationModifier((MarketImmigrationModifier) this);
		}
	}

	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
	}
	
}





