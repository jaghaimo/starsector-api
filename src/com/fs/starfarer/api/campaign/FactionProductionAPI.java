package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public interface FactionProductionAPI {

	public static enum ProductionItemType {
		SHIP,
		FIGHTER,
		WEAPON
	}
	
	public interface ItemInProductionAPI {
		FighterWingSpecAPI getWingSpec();
		WeaponSpecAPI getWeaponSpec();
		ShipHullSpecAPI getShipSpec();
		float getBaseBuildDelay();
		int getBaseCost();
		ProductionItemType getType();
		void setType(ProductionItemType type);
		String getSpecId();
		void setSpecId(String specId);
		float getBuildDelay();
		void setBuildDelay(float buildDelay);
		float getTimeInterrupted();
		void setTimeInterrupted(float timeInterrupted);
		int getQuantity();
		void setQuantity(int quantity);
	}

	/**
	 * Sum of faction-wide production of Ships & Weapons commodity, times productionCapacityPerSWUnit. In credits.
	 * @return
	 */
	int getMonthlyProductionCapacity();
	
	boolean addItem(ProductionItemType type, String specId);
	boolean addItem(ProductionItemType type, String specId, int quantity);
	void removeItem(ProductionItemType type, String specId, int count);
	int getCount(ProductionItemType type, String specId);
	
	List<ItemInProductionAPI> getInterrupted();
	List<ItemInProductionAPI> getCurrent();
	int getTotalCurrentCost();
	
	FactionAPI getFaction();

	int getUnitCost(ProductionItemType type, String specId);

	void clear();

	MarketAPI getGatheringPoint();
	void setGatheringPoint(MarketAPI gatheringPoint);

	int getAccruedProduction();
	void setAccruedProduction(int accruedProduction);

	float getProductionCapacityForMarket(MarketAPI market);

}
