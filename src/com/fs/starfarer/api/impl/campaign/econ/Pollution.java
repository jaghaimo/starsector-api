package com.fs.starfarer.api.impl.campaign.econ;

import java.util.Map;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;


public class Pollution extends BaseHazardCondition implements MarketImmigrationModifier {
	
	public void apply(String id) {
		super.apply(id);
		market.addTransientImmigrationModifier(this);
	}

	public void unapply(String id) {
		super.unapply(id);
		market.removeTransientImmigrationModifier(this);
	}

	@Override
	public Map<String, String> getTokenReplacements() {
		return super.getTokenReplacements();
	}

	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.LUDDIC_PATH, 10f);
	}

}
