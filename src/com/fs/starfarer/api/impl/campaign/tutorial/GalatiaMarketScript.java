package com.fs.starfarer.api.impl.campaign.tutorial;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketDemandAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.IntervalUtil;

public class GalatiaMarketScript implements EveryFrameScript {

	protected MarketAPI market;
	protected IntervalUtil interval = new IntervalUtil(15f, 25f);
	
	public GalatiaMarketScript(MarketAPI market) {
		this.market = market;
		
		FactionAPI faction = market.getFaction(); 
		market.getTariff().modifyFlat("default_tariff", faction.getTariffFraction());
		
		updateCommodities();
	}

	Object readResolve() {
		market.updatePrices();
		return this;
	}
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		interval.advance(days);
		if (!interval.intervalElapsed()) return;

		updateCommodities();
	}
	
	protected void updateCommodities() {
		for (CommodityOnMarketAPI com : market.getAllCommodities()) {
			//com.setStockpile(com.getSupplyValue());
			com.setStockpile(200);
		}
		
		for (MarketDemandAPI demand : market.getDemandData().getDemandList()) {
			CommodityOnMarketAPI com = market.getCommodityData(demand.getBaseCommodity().getId());
			com.addToStockpile(demand.getDemandValue());
		}
		
		CommodityOnMarketAPI supplies = market.getCommodityData(Commodities.SUPPLIES);
		supplies.addToStockpile(100f + 100f * (float) Math.random());
		
		CommodityOnMarketAPI fuel = market.getCommodityData(Commodities.FUEL);
		fuel.addToStockpile(100f + 100f * (float) Math.random());
		
		//Global.getSector().getEconomy().updateStabilityAndPriceMult(market);
		market.updatePrices();
	}

	public boolean isDone() {
		return market.isInEconomy();
	}

	public boolean runWhilePaused() {
		return false;
	}

}








