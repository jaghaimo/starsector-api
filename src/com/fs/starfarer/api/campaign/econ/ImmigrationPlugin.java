package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;


public interface ImmigrationPlugin {

	/**
	 * Only called once every 3 days or thereabouts.
	 * @param days
	 * @param uiUpdateOnly 
	 */
	void advance(float days, boolean uiUpdateOnly);


	float getWeightForMarketSize(float size);
	PopulationComposition computeIncoming();

	//float getIncentiveCostDistMult();

	int getCreditsForOnePercentPopulationIncrease();
	float getFractionForPopulationPoints(float points);
	float getPopulationPointsForFraction(float fraction);


	float getIncentivePercentPerMonth();

}
