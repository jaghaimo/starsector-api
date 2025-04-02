package com.fs.starfarer.api.impl.campaign.ids;

public class ShipRoles {

	public static final String MARKET_RANDOM_SHIPS = "marketRandom";
	
	
	/**
	 * Excludes ships that are annoying to fight in low numbers - Omen, Hyperion, 
	 * long-range beam/kiting loadouts, etc. Anything that's extremely likely to only end 
	 * after full CR drain.
	 * 
	 * Phase ships are in their own roles so not included here either (and there's other logic
	 * to not add them to small fleets).
	 * 
	 * Also should not include escort/support type variants. 
	 */
	public static final String COMBAT_SMALL_FOR_SMALL_FLEET = "combatSmallForSmallFleet";
	
	public static final String COMBAT_SMALL = "combatSmall";
	public static final String COMBAT_MEDIUM = "combatMedium";
	public static final String COMBAT_LARGE = "combatLarge";
	public static final String COMBAT_CAPITAL = "combatCapital";
	public static final String COMBAT_FREIGHTER_SMALL = "combatFreighterSmall";
	public static final String COMBAT_FREIGHTER_MEDIUM = "combatFreighterMedium";
	public static final String COMBAT_FREIGHTER_LARGE = "combatFreighterLarge";
	
	public static final String THREAT_FABRICATOR = "threatFabricator";
	public static final String THREAT_HIVE = "threatHive";
	public static final String THREAT_OVERSEER = "threatOverseer";
	
	public static final String DWELLER_TENDRIL = "dwellerTendril";
	public static final String DWELLER_MAELSTROM = "dwellerMaelstrom";
	public static final String DWELLER_EYE = "dwellerEye";
	public static final String DWELLER_MAW = "dwellerMaw";
	
	public static final String CIV_RANDOM = "civilianRandom";
	
	public static final String PHASE_SMALL = "phaseSmall";
	public static final String PHASE_MEDIUM = "phaseMedium";
	public static final String PHASE_LARGE = "phaseLarge";
		
	public static final String PHASE_CAPITAL = "phaseCapital";
		
	public static final String CARRIER_SMALL = "carrierSmall";
	public static final String CARRIER_MEDIUM = "carrierMedium";
	public static final String CARRIER_LARGE = "carrierLarge";
	public static final String FREIGHTER_SMALL = "freighterSmall";
	public static final String FREIGHTER_MEDIUM = "freighterMedium";
	public static final String FREIGHTER_LARGE = "freighterLarge";
	public static final String TANKER_SMALL = "tankerSmall";
	public static final String TANKER_MEDIUM = "tankerMedium";
	public static final String TANKER_LARGE = "tankerLarge";
	public static final String PERSONNEL_SMALL = "personnelSmall";
	public static final String PERSONNEL_MEDIUM = "personnelMedium";
	public static final String PERSONNEL_LARGE = "personnelLarge";
	public static final String LINER_SMALL = "linerSmall";
	public static final String LINER_MEDIUM = "linerMedium";
	public static final String LINER_LARGE = "linerLarge";
	public static final String TUG = "tug";
	public static final String CRIG = "crig";
	public static final String UTILITY = "utility";
	
	

	@Deprecated public static final String FAST_ATTACK = "fastAttack";
	@Deprecated public static final String ESCORT_SMALL = "escortSmall";
	@Deprecated public static final String ESCORT_MEDIUM = "escortMedium";
}
