package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.util.Misc;

/**
 * Parameters for generic fleet creation.
 * Fleet point values are targets not hard limits.
 * 
 * "Pts" are fleet points in this implementation, unlike FleetFactoryV2. 
 * 
 * @author Alex Mosolov
 *
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class FleetParamsV3 {
	/**
	 * Use setSource() to set the source or directly call updateQualityAndProducerFromSourceMarket()
	 * to set the quality. Otherwise, the market's quality won't get used. 
	 */
	public MarketAPI source;
	
	public Vector2f locInHyper;
	public float quality;
	public String factionId;
	public String fleetType;
	
	public float combatPts;
	public float freighterPts; 	
	public float tankerPts; 	
	public float transportPts; 	
	public float linerPts;
	public float utilityPts;
	
	
	/**
	 * 0: fighter, 4: capital
	 */
	public int maxShipSize = 1000;
	
	/**
	 *  0: fighter, 4: capital, only affects combat ships; smaller ships will still be added with remaining fleet points.
	 */
	public int minShipSize = 0;
	
	public float qualityMod = 0f;
	public Float qualityOverride = null;
	public Integer averageSMods = null;
	public boolean withOfficers = true;
//	public boolean applyDoctrineFleetSize = true;
//	public boolean applyMarketSizeToFleetSize = true;
//	public boolean applyShipsDeficit = true;
	public Boolean ignoreMarketFleetSizeMult = null;
	public Boolean onlyApplyFleetSizeToCombatShips = null;
	public Boolean doNotPrune = null;
	public Boolean doNotAddShipsBeforePruning = null;
	public ShipPickMode modeOverride = null;
	
	public int officerLevelBonus = 0;
	public int officerNumberBonus = 0;
	public Integer maxOfficersToAdd = null;
	public float officerNumberMult = 1;
	public int officerLevelLimit = 0;
	public int commanderLevelLimit = 0;
//	public int maxOfficers = -1;
//	public int minOfficers = -1;
	public Random random = null;
	public PersonAPI commander;
	
	public Boolean noCommanderSkills;
	public Boolean forceAllowPhaseShipsEtc;
	public Boolean treatCombatFreighterSettingAsFraction;
	public FactionDoctrineAPI doctrineOverride = null;
	//public Boolean forceNoTimestamp;
	public Long timestamp;
	
	public Integer maxNumShips;
	public Boolean onlyRetainFlagship;
	public String flagshipVariantId;
	public ShipVariantAPI flagshipVariant;
	//public Boolean allowEmptyFleet = null;
	
	public HubMissionWithTriggers.OfficerQuality aiCores = null;
	public boolean doNotIntegrateAICores = false;
	
	// Used in FleetFactoryV3 to pass some data between methods. Do not use directly.
	public transient ShipPickMode mode;
	public transient boolean banPhaseShipsEtc;
	public transient Boolean blockFallback = null;

	public Boolean allWeapons = null;
	
	/**
	 * If non-null: these ship variants will be added to the fleet before anything else
	 */
	public List<String> addShips;
	
	public FleetParamsV3(MarketAPI source, Vector2f locInHyper, String factionId, Float qualityOverride, String fleetType,
			float combatPts, float freighterPts, float tankerPts,
			float transportPts, float linerPts,
			float utilityPts, float qualityMod) {
		if (source != null) {
			init(source, fleetType, factionId, combatPts, freighterPts, tankerPts, transportPts, linerPts, utilityPts, qualityMod);
			if (factionId != null) {
				this.factionId = factionId;
			}
			this.qualityOverride = qualityOverride;
			this.locInHyper = locInHyper;
		} else {
			init(locInHyper, factionId, qualityOverride, fleetType,
					combatPts, freighterPts, tankerPts, transportPts, linerPts, utilityPts, qualityMod);
		}
	}
	
	public FleetParamsV3() {
		
	}
			
	public FleetParamsV3(Vector2f locInHyper, String factionId, Float qualityOverride, String fleetType,
			float combatPts, float freighterPts, float tankerPts,
			float transportPts, float linerPts,
			float utilityPts, float qualityMod) {
		init(locInHyper, factionId, qualityOverride, fleetType,
				combatPts, freighterPts, tankerPts, transportPts, linerPts, utilityPts, qualityMod);
	}
	
	public FleetParamsV3(MarketAPI source, String fleetType,
			float combatPts, float freighterPts, float tankerPts,
			float transportPts, float linerPts,
			float utilityPts, float qualityMod) {
		init(source, fleetType, null, combatPts, freighterPts, tankerPts, transportPts, linerPts, utilityPts, qualityMod);
	}
	
	public void init(MarketAPI source, String fleetType, String factionId,
			float combatPts, float freighterPts, float tankerPts,
			float transportPts, float linerPts,
			float utilityPts, float qualityMod) {
		init(source.getLocationInHyperspace(), null, null, 
				fleetType, combatPts, freighterPts, tankerPts, transportPts, linerPts, utilityPts, qualityMod);
		this.factionId = source.getFactionId();
		if (factionId != null) {
			this.factionId = factionId;
		}
		this.source = source;
		timestamp = Global.getSector().getClock().getTimestamp();
		updateQualityAndProducerFromSourceMarket();
	}
	
	public void init(Vector2f locInHyper, String factionId, Float qualityOverride, String fleetType,
			float combatPts, float freighterPts, float tankerPts,
			float transportPts, float linerPts,
			float utilityPts, float qualityMod) {
		this.locInHyper = locInHyper;
		this.factionId = factionId;
		this.qualityOverride = qualityOverride;
		this.fleetType = fleetType;
		this.combatPts = combatPts;
		this.freighterPts = freighterPts;
		this.tankerPts = tankerPts;
		this.transportPts = transportPts;
		this.linerPts = linerPts;
		this.utilityPts = utilityPts;
		this.qualityMod = qualityMod;
	}
	
	public void setSource(MarketAPI source, boolean updateQuality) {
		this.source = source;
		if (updateQuality) {
			updateQualityAndProducerFromSourceMarket();
		}
	}
	
	
	public void updateQualityAndProducerFromSourceMarket() {
		if (source != null) {
			this.quality = Misc.getShipQuality(source, factionId);
		}
		
//		this.quality = 0f;
//		if (producer != null) {
//			this.quality = producer.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).computeEffective(0f);
//		}
//		if (source != null) {
//			this.quality += source.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).computeEffective(0f);
//			//this.quality += source.getFaction().getDoctrine().getShipQualityContribution();
//		} else if (factionId != null) {
//			this.quality += Global.getSector().getFaction(factionId).getDoctrine().getShipQualityContribution();
//		}
	}
	
	public float getTotalPts() {
		return combatPts + freighterPts + tankerPts + transportPts + linerPts + utilityPts;
	}
	
	
}











