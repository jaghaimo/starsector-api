package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;

public interface MarketImmigrationModifier {
	void modifyIncoming(MarketAPI market, PopulationComposition incoming);
}
