package com.fs.starfarer.api.impl.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;


public class ClosedImmigration extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

	public void apply(String id) {
		market.addTransientImmigrationModifier(this);
	}

	public void unapply(String id) {
		market.removeTransientImmigrationModifier(this);
	}

	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.PIRATES, market.getSize());
		
		incoming.getWeight().modifyFlat(getModId(), -market.getStabilityValue(), "Closed immigration");
	}
	
}



