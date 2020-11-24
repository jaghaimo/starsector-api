package com.fs.starfarer.api.fleet;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;


public interface FleetLogisticsAPI {
	
	/**
	 * Total supply use per day, including what's actually consumed by ship maintenance.
	 * @return
	 */
	float getTotalSuppliesPerDay();
		
	float getFuelCostPerLightYear();
	float getBaseFuelCostPerLightYear();
	
//	/**
//	 * How much is actually needed for all repairs to proceed at the best possible rate.
//	 * @return
//	 */
	//float getMaximumRepairSupplyConsumptionPerDay();
	
	
	float getExcessCargoCapacitySupplyCost();
	float getExcessFuelCapacitySupplyCost();
	float getExcessPersonnelCapacitySupplyCost();
	float getMarineSuppliesPerDay();
	float getCrewSuppliesPerDay();
	
	/**
	 * @return getCrewSuppliesPerDay() + getMarineSuppliesPerDay()
	 */
	float getPersonnelSuppliesPerDay();
	
	/**
	 * Added up monthly supply cost for ships, divided by 30 to get daily cost.
	 * @return
	 */
	float getShipMaintenanceSupplyCost();

	CampaignFleetAPI getFleet();

	float getTotalRepairAndRecoverySupplyCost();
	
	/**
	 * Also updates the logistics rating.
	 * Not particularly fast, should not be called often (i.e. every frame for every fleet = bad idea.)
	 */
	void updateRepairUtilizationForUI();

}
