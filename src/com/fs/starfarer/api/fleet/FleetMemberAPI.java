package com.fs.starfarer.api.fleet;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface FleetMemberAPI {
	PersonAPI getCaptain();
	
	
	MutableShipStatsAPI getStats();
	
	String getShipName();
	void setShipName(String name);
	
	/**
	 * Unique id, generated using Misc.genUID().
	 * @return
	 */
	String getId();
	
	String getSpecId();
	String getHullId();
	FleetMemberType getType();
	
	boolean isFlagship();
	
	int getNumFlightDecks();
	boolean isCarrier();
	boolean isCivilian();
	//boolean isWoefullyUndergunned();
	void setFlagship(boolean isFlagship);
	int getFleetPointCost();
	boolean isFighterWing();
	boolean isFrigate();
	boolean isDestroyer();
	boolean isCruiser();
	boolean isCapital();
	int getNumFightersInWing();
	float getFuelCapacity();
	float getCargoCapacity();
	float getMinCrew();
	float getNeededCrew();
	float getMaxCrew();
	float getFuelUse();
	
	RepairTrackerAPI getRepairTracker();
	ShipHullSpecAPI getHullSpec();
	PersonAPI getFleetCommander();
	
	
	boolean canBeDeployedForCombat();
	ShipVariantAPI getVariant();
	FleetDataAPI getFleetData();
	
	void setVariant(ShipVariantAPI variant, boolean withRefit, boolean withStatsUpdate);
	CrewCompositionAPI getCrewComposition();
	FleetMemberStatusAPI getStatus();
	
	
	/**
	 * Fraction of crew on the ship, 0 to 1, ignores levels of crew.
	 * @return
	 */
	float getCrewFraction();
	
	
	int getReplacementChassisCount();
	
	
	/**
	 * Probably not needed given the current state of the API.
	 * @param statUpdateNeeded
	 */
	void setStatUpdateNeeded(boolean statUpdateNeeded);
	
	BuffManagerAPI getBuffManager();
	
	boolean isMothballed();
	
	/**
	 * From 0 to 1, CR fraction. Multiplied by number of fighters if fighter wing.
	 * @return
	 */
	float getDeployCost();
	void setCaptain(PersonAPI commander);
	
	/**
	 * Based on fleet points, modified by CR and ordnance points actually used by the variant.
	 * 
	 * Not modified by hull status or captain quality.
	 * @return
	 */
	float getMemberStrength();
	
	
	int getOwner();
	void setOwner(int owner);
	
	
	/**
	 * In credits. Includes properly-adjusted cost of mounted weapons. Does not include any tariffs.
	 * @return
	 */
	float getBaseSellValue();
	
	/**
	 * In credits. Includes properly-adjusted cost of mounted weapons. Does not include any tariffs.
	 * @return
	 */
	float getBaseBuyValue();
	
	boolean needsRepairs();
	boolean canBeRepaired();
	
	float getDeploymentPointsCost();
	float getDeploymentCostSupplies();
	float getBaseDeployCost();
	
	
	/**
	 * True for non-player-controlled ships on the player's side in combat.
	 * Transient, not saved.
	 * @return
	 */
	boolean isAlly();
	
	/**
	 * True for non-player-controlled ships on the player's side in combat.
	 * Transient, not saved.
	 */
	void setAlly(boolean isAlly);
	void setFleetCommanderForStats(PersonAPI alternateFleetCommander, FleetDataAPI fleetForStats);
	FleetDataAPI getFleetDataForStats();
	PersonAPI getFleetCommanderForStats();
	void updateStats();


	boolean isStation();


//	ShipVariantAPI getModuleVariant(String slotId);
//	void setModuleVariant(String slotId, ShipVariantAPI variant);


	float getBaseDeploymentCostSupplies();
	//float getMaintenanceCostSupplies();


	/**
	 * Base value of hull and all mounted non-built-in weapons and fighter LPCs.
	 * @return
	 */
	float getBaseValue();


	/**
	 * Sprite to use in the campaign view. Currently used by custom stations.
	 * @param spriteOverride
	 */
	void setSpriteOverride(String spriteOverride);
	String getSpriteOverride();

	Vector2f getOverrideSpriteSize();
	void setOverrideSpriteSize(Vector2f overrideSpriteSize);


	boolean isPhaseShip();


	void setId(String id);


	float getUnmodifiedDeploymentPointsCost();


}






