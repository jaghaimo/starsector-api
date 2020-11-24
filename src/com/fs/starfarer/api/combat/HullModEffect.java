package com.fs.starfarer.api.combat;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

/**
 * Note: the effect class is instantiated once per application session.
 * Storing campaign data in members of an implementing class is a bad idea,
 * use SectorAPI.getPersistentData() instead.
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public interface HullModEffect {
	void init(HullModSpecAPI spec);
	void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id);
	
	/**
	 * Effects applied here should NOT affect ship stats as this does not get called from the campaign.
	 * Apply stat changes in applyEffectsBeforeShipCreation() instead, as that does affect the campaign.
	 * @param ship
	 * @param id
	 */
	void applyEffectsAfterShipCreation(ShipAPI ship, String id);
	String getDescriptionParam(int index, HullSize hullSize);
	String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship);

	void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id);
	
	boolean isApplicableToShip(ShipAPI ship);
	
	String getUnapplicableReason(ShipAPI ship);
	
	/**
	 * ship may be null from autofit.
	 * @param ship
	 * @param marketOrNull
	 * @param mode
	 * @return
	 */
	boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode);
	String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode);
	
	
	/**
	 * Not called while paused.
	 * But, called when the fleet data needs to be re-synced,
	 * with amount=0 (such as if, say, a fleet member is moved around.
	 * in the fleet screen.)
	 * @param member
	 * @param amount
	 */
	void advanceInCampaign(FleetMemberAPI member, float amount);
	
	/**
	 * Not called while paused.
	 * @param ship
	 * @param amount
	 */
	void advanceInCombat(ShipAPI ship, float amount);
	
	/**
	 * Hullmods that return true here should only ever be built-in, as cost changes aren't handled when
	 * these mods can be added or removed to/from the variant.
	 * @return
	 */
	boolean affectsOPCosts();
	
	
	/**
	 * ship may be null, will be for modspecs. hullsize will always be CAPITAL_SHIP for modspecs.
	 * @param hullSize
	 * @param ship
	 * @param isForModSpec
	 * @return
	 */
	boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec);
	
	/**
	 * ship may be null, will be for modspecs. hullsize will always be CAPITAL_SHIP for modspecs.
	 * @param tooltip
	 * @param hullSize
	 * @param ship
	 * @param width
	 * @param isForModSpec
	 */
	void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec);
	
	
}
