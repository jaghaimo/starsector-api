package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class MildClimate extends LCAttractorHigh implements MarketImmigrationModifier {
	
	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		super.modifyIncoming(market, incoming);
		incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
	}
	
	protected float getImmigrationBonus() {
		return market.getSize();
	}
	
	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		if (!market.isPlanetConditionMarketOnly()) {
			tooltip.addPara("%s population growth (based on colony size)", 
					10f, Misc.getHighlightColor(),
					"+" + (int) getImmigrationBonus());
		}
	}
}
