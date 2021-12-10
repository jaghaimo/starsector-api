package com.fs.starfarer.api.campaign.econ;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CommDirectoryAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.rules.HasMemory;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.MutableMarketStatsAPI;
import com.fs.starfarer.api.fleet.ShipFilter;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.econ.impl.ConstructionQueue;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;

public interface MarketAPI extends HasMemory {
	
	public static enum MarketInteractionMode {
		LOCAL,
		REMOTE,
	}
	
	public static enum SurveyLevel {
		NONE,
		SEEN,
		PRELIMINARY,
		FULL
	}
	
	SectorEntityToken getPrimaryEntity();
	Set<SectorEntityToken> getConnectedEntities();
	
	String getId();
	String getName();
	int getSize();
	
	//Vector2f getLocation();
	
	void setSize(int size);
	
	List<CommodityOnMarketAPI> getAllCommodities();
	CommodityOnMarketAPI getCommodityData(String commodityId);
	List<CommodityOnMarketAPI> getCommoditiesWithTag(String tag);
	List<CommodityOnMarketAPI> getCommoditiesWithTags(String ... tags);
	
	MarketDemandAPI getDemand(String demandClass);
	List<MarketDemandAPI> getDemandWithTag(String tag);

	List<MarketConditionAPI> getConditions();
	/**
	 * Returns token which can be used to remove this specific condition.
	 * @param id
	 * @return
	 */
	String addCondition(String id);
	
	/**
	 * Returns token which can be used to remove this specific condition.
	 * @param id
	 * @param param
	 * @return
	 */
	String addCondition(String id, Object param);
	
	/**
	 * Removes all copies of this condition.
	 * @param id
	 */
	void removeCondition(String id);
	
	/**
	 * Removes specific copy of a condition.
	 * @param token return value from addCondition()
	 */
	void removeSpecificCondition(String token);
	
	boolean hasCondition(String id);
	boolean hasSpecificCondition(String token);
	void reapplyConditions();
	void reapplyCondition(String token);
	
	MarketDemandDataAPI getDemandData();
	
//	StabilityTrackerAPI getStabilityTracker();
	
	MutableStat getTariff();
	
	/**
	 * Modifier for the price the market is willing to buy things at.
	 * Only the multiplier part of this works.
	 * @return
	 */
	StatBonus getDemandPriceMod();
	
	/**
	 * Modifier for the price the market is willing to sell things at.
	 * @return
	 */
	StatBonus getSupplyPriceMod();
	
	
	/**
	 * Price for the market selling quantity of given commodity, given the current stockpile/demand/etc.
	 * @param commodityId
	 * @param quantity
	 * @return
	 */
	float getSupplyPrice(String commodityId, double quantity, boolean isPlayerPrice);
	
	
	/**
	 * Price for the market buying quantity of given commodity, given the current stockpile/demand/etc.
	 * @param commodityId
	 * @param quantity
	 * @return
	 */
	float getDemandPrice(String commodityId, double quantity, boolean isPlayerPrice);
	
	
	/**
	 * @param commodityId
	 * @param quantity
	 * @param existingTransactionUtility positive for stuff sold to market, negative for stuff bought from market.
	 * @param isPlayerPrice
	 * @return
	 */
	float getDemandPriceAssumingExistingTransaction(String commodityId, double quantity, double existingTransactionUtility, boolean isPlayerPrice);
	
	/**
	 * @param commodityId
	 * @param quantity
	 * @param existingTransactionUtility positive for stuff sold to market, negative for stuff bought from market.
	 * @param isPlayerPrice
	 * @return
	 */
	float getSupplyPriceAssumingExistingTransaction(String commodityId, double quantity, double existingTransactionUtility, boolean isPlayerPrice);
	
	/**
	 * Checks against FactionAPI.getIllegalCommodities() for the faction owning the market.
	 * @param commodityId
	 * @return
	 */
	boolean isIllegal(String commodityId);
	
	/**
	 * Checks against FactionAPI.getIllegalCommodities() for the faction owning the market.
	 * @param com
	 * @return
	 */
	boolean isIllegal(CommodityOnMarketAPI com);
	
	MutableStatWithTempMods getStability();
	
	/**
	 * Integer value from 0 to 10, inclusive.
	 * @return
	 */
	float getStabilityValue();
	
	FactionAPI getFaction();
	String getFactionId();
	
	void addSubmarket(String specId);
	boolean hasSubmarket(String specId);
	List<SubmarketAPI> getSubmarketsCopy();
	void removeSubmarket(String specId);
	SubmarketAPI getSubmarket(String specId);
	void setFactionId(String factionId);
	
	
	/**
	 * Updates the local price multiplier (based on stability).
	 */
	void updatePriceMult();
	MemoryAPI getMemory();
	
	MemoryAPI getMemoryWithoutUpdate();

	/**
	 * May add more than one ship if a fallback specifies to add multiple ships.
	 * (For example, 2 small freighters if a medium freighter isn't available.)
	 * 
	 * See FactionAPI.pickShipAndAddToFleet for return value explanation.
	 * @return
	 */
	float pickShipAndAddToFleet(String role, ShipPickParams params, CampaignFleetAPI fleet);
	
	float pickShipAndAddToFleet(String role, String factionId, ShipPickParams params, CampaignFleetAPI fleet);
	
	float getShipQualityFactor();
	
	
	StarSystemAPI getStarSystem();
	LocationAPI getContainingLocation();
	
	
	Vector2f getLocationInHyperspace();
	void setPrimaryEntity(SectorEntityToken primaryEntity);

	
//	/**
//	 * Will be null unless inited. Repeated invocations will do nothing.
//	 */
//	void initCommDirectory();

	/**
	 * @return
	 */
	CommDirectoryAPI getCommDirectory();

	void addPerson(PersonAPI person);
	void removePerson(PersonAPI person);
	List<PersonAPI> getPeopleCopy();
	MutableMarketStatsAPI getStats();
	
	List<ShipRolePick> pickShipsForRole(String role, ShipPickParams params,
			Random random, ShipFilter filter);
	List<ShipRolePick> pickShipsForRole(String role, String factionId, ShipPickParams params, Random random, ShipFilter filter);
	
	
	boolean isPlanetConditionMarketOnly();
	void setPlanetConditionMarketOnly(boolean isPlanetConditionMarketOnly);
	void setName(String name);
	MutableStat getHazard();
	
	/**
	 * 1f = 100%.
	 * @return
	 */
	float getHazardValue();
	
//	boolean isSurveyed();
//	void setSurveyed(boolean surveyed);
	PlanetAPI getPlanetEntity();
	
	SurveyLevel getSurveyLevel();
	void setSurveyLevel(SurveyLevel surveyLevel);
	
	
	void advance(float amount);
	boolean isForceNoConvertOnSave();
	void setForceNoConvertOnSave(boolean forceNoConvertOnSave);
	void updatePrices();
	
	/**
	 * Get a condition using its unique id.
	 * @param token
	 * @return
	 */
	MarketConditionAPI getSpecificCondition(String token);
	
	
	/**
	 * Get the first condition of a specific type; id is non-unique.
	 * @param id
	 * @return
	 */
	MarketConditionAPI getFirstCondition(String id);
	boolean isInEconomy();
	
	List<Industry> getIndustries();
	void addIndustry(String id);
	
	/**
	 * Pass in null for mode when calling this from API code.
	 * @param id
	 * @param mode
	 */
	void removeIndustry(String id, MarketInteractionMode mode, boolean forUpgrade);
	void reapplyIndustries();
	
	/**
	 * Same as getLocationInHyperspace().
	 * @return
	 */
	Vector2f getLocation();
	
	/**
	 * In-system, i.e. not affected by fuel shortages etc.
	 * @return
	 */
	Industry getIndustry(String id);
	
	boolean hasIndustry(String id);
	List<CommodityOnMarketAPI> getCommoditiesCopy();
	MarketConditionAPI getCondition(String id);
	
	float getIndustryUpkeep();
	float getIndustryIncome();
	boolean hasWaystation();
	Industry instantiateIndustry(String id);
	MarketAPI clone();
	void clearCommodities();
	boolean isPlayerOwned();
	void setPlayerOwned(boolean playerOwned);
	float getPrevStability();
	float getExportIncome(boolean withOverhead);
	float getNetIncome();
	MutableStat getIncomeMult();
	MutableStat getUpkeepMult();
	PopulationComposition getPopulation();
	PopulationComposition getIncoming();
	void setPopulation(PopulationComposition population);
	void setIncoming(PopulationComposition incoming);
//	StatBonus getIncomingImmigrationMod();
//	StatBonus getOutgoingImmigrationMod();
	
	LinkedHashSet<MarketImmigrationModifier> getImmigrationModifiers();
	LinkedHashSet<MarketImmigrationModifier> getTransientImmigrationModifiers();
	void addImmigrationModifier(MarketImmigrationModifier mod);
	void removeImmigrationModifier(MarketImmigrationModifier mod);
	void addTransientImmigrationModifier(MarketImmigrationModifier mod);
	void removeTransientImmigrationModifier(MarketImmigrationModifier mod);
	List<MarketImmigrationModifier> getAllImmigrationModifiers();
	
//	boolean isAllowImport();
//	void setAllowImport(boolean allowImport);
//	boolean isAllowExport();
//	void setAllowExport(boolean allowExport);
	
	float getIncentiveCredits();
	void setIncentiveCredits(float incentiveCredits);
	
	boolean isImmigrationIncentivesOn();
	void setImmigrationIncentivesOn(Boolean incentivesOn);
	
	
	boolean isFreePort();
	void setFreePort(boolean freePort);
	boolean isImmigrationClosed();
	void setImmigrationClosed(boolean closed);
	boolean wasIncomingSetBefore();
	void addCondition(MarketConditionAPI mc);
	
	PersonAPI getAdmin();
	/**
	 * The old admin, if any, is removed from the market and its comm directory. 
	 * @param admin
	 */
	void setAdmin(PersonAPI admin);
	float getDaysInExistence();
	void setDaysInExistence(float daysInExistence);
	/**
	 * o = 0%, 1 = 100%.
	 * @return
	 */
	StatBonus getAccessibilityMod();
	boolean hasSpaceport();
	void setHasSpaceport(boolean hasSpaceport);
	void setHasWaystation(boolean hasWaystation);
	
	/**
	 * Markets with the same economy group will not be visible from markets outside this group
	 * (in "nearby markets" dialog etc) and will only trade with each other. null by default, which
	 * forms its own group.
	 * @return
	 */
	String getEconGroup();
	
	/**
	 * Markets with the same economy group will not be visible from markets outside this group
	 * (in "nearby markets" dialog etc) and will only trade with each other. null by default, which
	 * forms its own group.
	 */
	void setEconGroup(String econGroup);
	
	void addIndustry(String id, List<String> params);
	boolean hasTag(String tag);
	void addTag(String tag);
	void removeTag(String tag);
	Collection<String> getTags();
	void clearTags();
	String getOnOrAt();
	
	Color getTextColorForFactionOrPlanet();
	Color getDarkColorForFactionOrPlanet();
	
	/**
	 * Hidden markets do not offer missions or otherwise participate in events/intel/etc that would
	 * indirectly reveal their existence to the player.
	 * @return
	 */
	boolean isHidden();
	
	/**
	 * Hidden markets do not offer missions or otherwise participate in events/intel/etc that would
	 * indirectly reveal their existence to the player. Hidden markets also do not participate in the economy.
	 */
	void setHidden(Boolean hidden);
	
	boolean isUseStockpilesForShortages();
	void setUseStockpilesForShortages(boolean useStockpilesForShortages);
	float getShortageCounteringCost();
	void addSubmarket(SubmarketAPI submarket);
	ConstructionQueue getConstructionQueue();
	boolean isInHyperspace();
	
	LinkedHashSet<String> getSuppressedConditions();
	boolean isConditionSuppressed(String id);
	void suppressCondition(String id);
	void unsuppressCondition(String id);
	float getImmigrationIncentivesCost();
	boolean isInvalidMissionTarget();
	void setInvalidMissionTarget(Boolean invalidMissionTarget);
	void setSuppressedConditions(LinkedHashSet<String> suppressedConditions);
	void setRetainSuppressedConditionsSetWhenEmpty(Boolean retainSuppressedConditionsSetWhenEmpty);
}





