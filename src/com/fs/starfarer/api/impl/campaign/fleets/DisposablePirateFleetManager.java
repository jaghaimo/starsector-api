package com.fs.starfarer.api.impl.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.MercType;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateActivity;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.PirateBaseTier;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;

/**
 * Adds the following types of fleets:
 * 1) Weak pirates to any nearby populated system, sometimes.
 * 2) Pirates to systems with markets with Pirate Activity; number/strength depends on base level
 * 3) Systems with a bounty; number/strength depends on size of markets.
 *  
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public class DisposablePirateFleetManager extends DisposableFleetManager {

	protected Object readResolve() {
		super.readResolve();
		return this;
	}
	
	@Override
	protected String getSpawnId() {
		return "pirates";
	}
	
	@Override
	protected int getDesiredNumFleetsForSpawnLocation() {
		PirateBaseTier tier = getPirateActivityTier();
		MarketAPI largestBounty = getLargestMarketIfSystemHasBounty();
		
		float tierMult = getMultForTier(tier);
		float bountyMult = largestBounty == null ? 0 : largestBounty.getSize();
		
		float desiredNumFleets = 1f;
		
		desiredNumFleets += tierMult > 0 ? 3f + tierMult : 0;
		desiredNumFleets += bountyMult;
		
		return (int) Math.round(desiredNumFleets);
	}

	protected float getMultForTier(PirateBaseTier tier) {
		if (tier == null) return 0f;
		return tier.ordinal() + 1f;
	}

	protected PirateBaseTier getPirateActivityTier() {
		if (currSpawnLoc == null) return null;
		for (MarketAPI market : Global.getSector().getEconomy().getMarkets(currSpawnLoc)) {
			if (market.isHidden()) continue;
			if (market.getFactionId().equals(Factions.PIRATES)) continue;
			MarketConditionAPI mc = market.getCondition(Conditions.PIRATE_ACTIVITY);
			if (mc != null && mc.getPlugin() instanceof PirateActivity) {
				PirateActivity pa = (PirateActivity) mc.getPlugin();
				return pa.getIntel() == null ? null : pa.getIntel().getTier();
			}
		}
		return null;
	}
	protected boolean hasPirateActivity() {
		return getPirateActivityTier() != null;
	}
	
	protected MarketAPI getLargestMarketIfSystemHasBounty() {
		if (currSpawnLoc == null) return null;
		MarketAPI largest = null;
		boolean bounty = false;
		int maxSize = 0;
		for (MarketAPI market : Global.getSector().getEconomy().getMarkets(currSpawnLoc)) {
			if (market.isHidden()) continue;
			if (market.getFactionId().equals(Factions.PIRATES)) continue;
			
			if (SystemBountyManager.getInstance().isActive(market)) bounty = true;
			
			if (market.getSize() > maxSize) {
				maxSize = market.getSize();
				largest = market;
			}
		}
		if (!bounty) largest = null;
		return largest;
	}
	
	protected CampaignFleetAPI spawnFleetImpl() {
		StarSystemAPI system = currSpawnLoc;
		if (system == null) return null;
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return null;
//		float distToPlayerLY = Misc.getDistanceLY(player.getLocationInHyperspace(), system.getLocation());
//		if (distToPlayerLY > 1f) return null;
		
		PirateBaseTier tier = getPirateActivityTier();
		MarketAPI largestBounty = getLargestMarketIfSystemHasBounty();
		float tierMult = getMultForTier(tier);
		float bountyMult = 0f;
		if (largestBounty != null) {
			bountyMult = largestBounty.getSize();
		}
		//float recentSpawns = getRecentSpawnsForSystem(currSpawnLoc);
		
		// up to 5 for tier and 8 for market, so max is 13; reasonable avg/high value maybe 10
		float bonus = tierMult + bountyMult; 
		
		
		float timeFactor = (PirateBaseManager.getInstance().getDaysSinceStart() - 180f) / (365f * 2f);
		if (timeFactor < 0) timeFactor = 0;
		if (timeFactor > 1) timeFactor = 1;
		
		float earlyTimeFactor = (PirateBaseManager.getInstance().getDaysSinceStart() - 60f) / 120f;
		if (earlyTimeFactor < 0) earlyTimeFactor = 0;
		if (earlyTimeFactor > 1) earlyTimeFactor = 1;
		
		//timeFactor = 1f;
		
		float r = (float) Math.random();
		//r = (float) Math.sqrt(r);
		
		//float fp = 15f + 30f * (float) Math.random() + bonus * 15f * r * timeFactor;
		
		float fp = (10f + bonus) * earlyTimeFactor +
				   (5f + bonus) * (0.5f + 0.5f * (float) Math.random()) + 
				   50f * (0.5f + 0.5f * r) * timeFactor;
		
		// larger fleets if more fleets
		float desired = getDesiredNumFleetsForSpawnLocation();
		if (desired > 2) {
			fp += ((desired - 2) * (0.5f + (float) Math.random() * 0.5f)) * 2f * timeFactor;
		}
		
		if (fp < 10) fp = 10;
		
		MercType type;
		if (fp < 25) {
			type = MercType.SCOUT;
		} else if (fp < 75) {
			type = MercType.PRIVATEER;
		} else if (fp < 125) {
			type = MercType.PATROL;
		} else {
			type = MercType.ARMADA;
		}
		
		String fleetType = type.fleetType;
		
		float combat = fp;
		float tanker = 0f;
		
		if (type == MercType.PATROL || type == MercType.ARMADA) {
			tanker = combat * 0.1f;
		}
		
		combat = Math.round(combat);
		tanker = Math.round(tanker);
		
		FleetParamsV3 params = new FleetParamsV3(
				null,
				system.getLocation(), // location
				Factions.PIRATES,
				null, // quality override
				fleetType,
				combat, // combatPts
				0f, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				);
		params.ignoreMarketFleetSizeMult = true;
		if (timeFactor <= 0) {
			params.maxShipSize = 1;
		}
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
		
		setLocationAndOrders(fleet, 0.25f, 0.25f);
		
		return fleet;
	}
	
}








