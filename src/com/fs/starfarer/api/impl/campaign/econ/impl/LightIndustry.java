package com.fs.starfarer.api.impl.campaign.econ.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;


public class LightIndustry extends BaseIndustry {

	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		
		demand(Commodities.ORGANICS, size);
		
		supply(Commodities.DOMESTIC_GOODS, size);
		//supply(Commodities.SUPPLIES, size - 3);
			
		//if (!market.getFaction().isIllegal(Commodities.LUXURY_GOODS)) {
		if (!market.isIllegal(Commodities.LUXURY_GOODS)) {
			supply(Commodities.LUXURY_GOODS, size - 2);
		} else {
			supply(Commodities.LUXURY_GOODS, 0);
		}
		//if (!market.getFaction().isIllegal(Commodities.DRUGS)) {
		if (!market.isIllegal(Commodities.DRUGS)) {
			supply(Commodities.DRUGS, size - 2);
		} else {
			supply(Commodities.DRUGS, 0);
		}
		
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.ORGANICS);
		
		applyDeficitToProduction(1, deficit,
					Commodities.DOMESTIC_GOODS,
					Commodities.LUXURY_GOODS,
					//Commodities.SUPPLIES,
					Commodities.DRUGS);
		
		if (!isFunctional()) {
			supply.clear();
		}
	}

	
	@Override
	public void unapply() {
		super.unapply();
	}
	
	@Override
	public String getCurrentImage() {
		float size = market.getSize();
		PlanetAPI planet = market.getPlanetEntity();
		if (planet == null || planet.isGasGiant()) {
			if (size <= 4) {
				return Global.getSettings().getSpriteName("industry", "light_industry_orbital_low");
			}
			if (size >= 7) {
				return Global.getSettings().getSpriteName("industry", "light_industry_orbital_high");
			}
			return Global.getSettings().getSpriteName("industry", "light_industry_orbital");
		}
		else
		{
			if (size <= 4) {
				return Global.getSettings().getSpriteName("industry", "light_industry_low");
			}
			if (size >= 7) {
				return Global.getSettings().getSpriteName("industry", "light_industry_high");
			}
		}
		
		return super.getCurrentImage();
	}

}
