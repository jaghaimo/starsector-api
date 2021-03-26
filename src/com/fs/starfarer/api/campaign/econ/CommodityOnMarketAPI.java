package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.api.combat.StatBonus;

/**
 * 
 * 
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public interface CommodityOnMarketAPI {

	String getId();
	
	MutableStat getGreed();
	
	float getGreedValue();
	
	CommoditySpecAPI getCommodity();

	float getStockpile();
	void setStockpile(float stockpile);
	
	void addToStockpile(float quantity);
	void removeFromStockpile(float quantity);

	MarketAPI getMarket();

	String getDemandClass();
	MarketDemandAPI getDemand();

	float getUtilityOnMarket();

	boolean isPersonnel();
	boolean isFuel();

	//StatBonus getPlayerPriceMod();

	boolean isNonEcon();

	int getMaxSupply();
	void setMaxSupply(int maxSupply);
	int getMaxDemand();
	void setMaxDemand(int maxDemand);

	void updateMaxSupplyAndDemand();

	int getAvailable();

	
	MutableStatWithTempMods getAvailableStat();

	/**
	 * Actual quantity in "inventory" units. Gets translated into econ-unit-scale "market units"
	 * and applied to the available quantity.
	 * @return
	 */
	MutableStatWithTempMods getTradeMod();
	MutableStatWithTempMods getTradeModPlus();
	MutableStatWithTempMods getTradeModMinus();

	void reapplyEventMod();
	float getModValueForQuantity(float quantity);

	boolean isIllegal();

	boolean isIllegalAssumePrimary();

	float getQuantityForModValue(float modValue);

	
//	LinkedHashMap<String, MutableStatWithTempMods> getPiracy();
//	void addPiracy(String sourceMarket, int penalty, float days);
//	void clearPiracy();
//	void clearPiracy(String sourceMarket);
//	int getPiracy(String sourceMarket);

	int getExportIncome();

	boolean isSupplyLegal();
	void setSupplyLegal(boolean isSupplyLegal);
	boolean isDemandLegal();
	void setDemandLegal(boolean isDemandLegal);

	
	//void addTradeMod(String source, float quantity, float days, String desc);
	/**
	 * Can result in both positive and negative econ-unit changes.
	 * @param source
	 * @param quantity
	 * @param days
	 */
	void addTradeMod(String source, float quantity, float days);
	/**
	 * Only positive econ-unit changes (but value may be negative; just gets clamped to 0).
	 * @param source
	 * @param quantity
	 * @param days
	 */
	void addTradeModPlus(String source, float quantity, float days);
	
	/**
	 * Only negative econ-unit changes (but value may be positive; just gets clamped to 0).
	 * @param source
	 * @param quantity
	 * @param days
	 */
	void addTradeModMinus(String source, float quantity, float days);

	/**
	 * tradeMod + modPlus if positive + modMinus if negative.
	 * @return
	 */
	float getCombinedTradeModQuantity();

	boolean isMeta();

	int getDemandValue();

	CommodityMarketDataAPI getCommodityMarketData();

	
	/**
	 * Quantity that can be sold at a higher price.
	 * @return
	 */
	int getDeficitQuantity();

	/**
	 * Quantity that can be bought at a lower price.
	 * @return
	 */
	int getExcessQuantity();

	/**
	 * Returns net bought/sold, minus what was used to change economy units available on market.
	 * For example, if 400 units of supplies are sold by to a colony, this method will return 0.
	 * If 399 units are sold, it will return 399.
	 * 
	 * >0 means player sold stuff recently, <0 means bought.
	 * 
	 * @return
	 */
	int getPlayerTradeNetQuantity();

	//StatBonus getPlayerPriceMod();
	
	StatBonus getPlayerSupplyPriceMod();
	StatBonus getPlayerDemandPriceMod();

}




