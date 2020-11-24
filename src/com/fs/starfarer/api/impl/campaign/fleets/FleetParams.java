package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;

/**
 * Parameters for generic fleet creation.
 * Fleet point values are targets not hard limits.
 * 
 * "Pts" are not fleet points
 * 1/2/4/8 points = frigate/destroyer/cruiser/capital 
 * 
 * @author Alex Mosolov
 *
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class FleetParams {

	public Vector2f hyperspaceLocation;
	public MarketAPI market;
	public String factionId;
	public String fleetType;
	public float combatPts;
	public float freighterPts; 	
	public float tankerPts; 	
	public float transportPts; 	
	public float linerPts;
	public float civilianPts;
	public float utilityPts;
	
	public float qualityBonus;
	public float qualityOverride = -1f;
	
	public float officerNumMult = 1f;
	public int officerLevelBonus = 0;
	public int levelLimit = 1000;
	
	public PersonAPI commander;
	public String factionIdForShipPicking;
	public Random random = null;
	public boolean withOfficers = true;
	
	
	/**
	 * 0: fighter, 4: capital
	 */
	public int maxShipSize = 1000;
	
	
	public FleetParams() {
	}

	public FleetParams(Vector2f hyperspaceLocation, MarketAPI market,
			String factionId, String fleetType, float combatFP,
			float freighterPts, float tankerPts, float transportPts,
			float linerPts, float civilianPts, float utilityPts,
			float qualityBonus, float qualityOverride) {
		this.hyperspaceLocation = hyperspaceLocation;
		this.market = market;
		this.factionId = factionId;
		this.fleetType = fleetType;
		this.combatPts = combatFP;
		this.freighterPts = freighterPts;
		this.tankerPts = tankerPts;
		this.transportPts = transportPts;
		this.linerPts = linerPts;
		this.civilianPts = civilianPts;
		this.utilityPts = utilityPts;
		this.qualityBonus = qualityBonus;
		this.qualityOverride = qualityOverride;
	}
	
	
	public FleetParams(Vector2f hyperspaceLocation, MarketAPI market,
			String factionId, String factionIdForShipPicking, String fleetType, float combatPts,
			float freighterPts, float tankerPts, float transportPts,
			float linerPts, float civilianPts, float utilityPts,
			float qualityBonus, float qualityOverride, float officerNumMult,
			int officerLevelBonus) {
		super();
		this.hyperspaceLocation = hyperspaceLocation;
		this.market = market;
		this.factionId = factionId;
		this.factionIdForShipPicking = factionIdForShipPicking;
		this.fleetType = fleetType;
		this.combatPts = combatPts;
		this.freighterPts = freighterPts;
		this.tankerPts = tankerPts;
		this.transportPts = transportPts;
		this.linerPts = linerPts;
		this.civilianPts = civilianPts;
		this.utilityPts = utilityPts;
		this.qualityBonus = qualityBonus;
		this.qualityOverride = qualityOverride;
		this.officerNumMult = officerNumMult;
		this.officerLevelBonus = officerLevelBonus;
	}
	
	public FleetParams(Vector2f hyperspaceLocation, MarketAPI market,
			String factionId, String fleetType, float combatPts,
			float freighterPts, float tankerPts, float transportPts,
			float linerPts, float civilianPts, float utilityPts,
			float qualityBonus, float qualityOverride, float officerNumMult,
			int officerLevelBonus, PersonAPI commander, int levelLimit) {
		super();
		this.hyperspaceLocation = hyperspaceLocation;
		this.market = market;
		this.factionId = factionId;
		this.fleetType = fleetType;
		this.combatPts = combatPts;
		this.freighterPts = freighterPts;
		this.tankerPts = tankerPts;
		this.transportPts = transportPts;
		this.linerPts = linerPts;
		this.civilianPts = civilianPts;
		this.utilityPts = utilityPts;
		this.qualityBonus = qualityBonus;
		this.qualityOverride = qualityOverride;
		this.officerNumMult = officerNumMult;
		this.officerLevelBonus = officerLevelBonus;
		this.commander = commander;
		this.levelLimit = levelLimit;
	}

	public float getTotalPts() {
		return combatPts + freighterPts + tankerPts + transportPts + linerPts + civilianPts + utilityPts;
	}
}











