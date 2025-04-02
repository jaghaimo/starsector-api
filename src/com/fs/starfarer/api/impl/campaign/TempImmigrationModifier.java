package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.util.Misc;

public class TempImmigrationModifier implements EconomyTickListener, MarketImmigrationModifier {

	protected String id;
	protected long startTime = 0;
	
	protected  MarketAPI market;
	protected float pointsPerMonth;
	protected float durDays;
	protected String desc;
	
	public TempImmigrationModifier(MarketAPI market, float pointsPerMonth, float durDays, String desc) {
		this.market = market;
		this.pointsPerMonth = pointsPerMonth;
		this.durDays = durDays;
		this.desc = desc;
		startTime = Global.getSector().getClock().getTimestamp();
		Global.getSector().getListenerManager().addListener(this);
		id = "temp_im_ " + Misc.genUID();
		market.addImmigrationModifier(this);
	}


	public void reportEconomyTick(int iterIndex) {
		float daysPassed = Global.getSector().getClock().getElapsedDaysSince(startTime);
		if (daysPassed > durDays) {
			Global.getSector().getListenerManager().removeListener(this);
			market.removeImmigrationModifier(this);
			return;
		}
	}
	
	public void reportEconomyMonthEnd() {
	}

	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		float daysPassed = Global.getSector().getClock().getElapsedDaysSince(startTime);
		int daysLeft = (int) (durDays - daysPassed);
		if (daysLeft <= 0) return;
		
		String days = "days";
		if (daysLeft == 1) days = "day";
		incoming.getWeight().modifyFlat(id, pointsPerMonth, desc + " (" + daysLeft + " " + days + " left)");		
	}
}



