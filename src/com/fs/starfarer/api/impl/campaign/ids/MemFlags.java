package com.fs.starfarer.api.impl.campaign.ids;

/**
 * Variable names for stuff stored in memory that:
 * 1) Generally is used more than once in code, so it's convenient to track from here.
 * 
 * These may or may not be used in rules.csv.
 * 
 * 
 * @author Alex Mosolov
 *
 * Copyright 2014 Fractal Softworks, LLC
 */
public class MemFlags {

	public static final String PLAYER_ATROCITIES = "$atrocities";
	
	public static final String STORY_CRITICAL = "$story_critical";
	
	/**
	 * For stuff like the Galatia Academy, where the market is fake and doesn't have the standard buy/sell/fleet/etc
	 * interaction options.
	 */
	public static final String MARKET_HAS_CUSTOM_INTERACTION_OPTIONS = "$hasCustomInteractionOptions";
	
	public static final String MARKET_EXTRA_SUSPICION = "$marketExtraSuspicion";
	public static final String PATROL_EXTRA_SUSPICION = "$patrolExtraSuspicion";
	
	public static final String CAN_ONLY_BE_ENGAGED_WHEN_VISIBLE_TO_PLAYER = "$canOnlyBeEngagedWhenVisibleToPlayer";
	
	public static final String RECENTLY_SALVAGED = "$recentlySalvaged";
	
	public static final String FLEET_DO_NOT_IGNORE_PLAYER = "$cfai_doNotIgnorePlayer";
	public static final String FLEET_IGNORES_OTHER_FLEETS = "$cfai_ignoreOtherFleets";
	public static final String FLEET_IGNORED_BY_OTHER_FLEETS = "$cfai_ignoredByOtherFleets";
	
	public static final String FLEET_PATROL_DISTANCE = "$cfai_patrolDist";
	
	public static final String RECENTLY_RAIDED = "$recentlyRaided";
	public static final String RECENTLY_BOMBARDED = "$recentlyBombarded";
	
	public static final String STATION_FLEET = "$stationFleet";
	public static final String STATION_BASE_FLEET = "$stationBaseFleet";
	public static final String STATION_MARKET = "$stationMarket";
	
	public static final String STAR_SYSTEM_IN_ANCHOR_MEMORY = "$anchor_starSystem";
	
	public static final String PREV_SALVAGE_SPECIAL_DATA = "$prevSalvageSpecialData";
	public static final String SALVAGE_SPECIAL_DATA = "$salvageSpecialData";
	public static final String SALVAGE_SEED = "$salvageSeed";
	public static final String SALVAGE_SPEC_ID_OVERRIDE = "$salvageSpecId";
	public static final String SALVAGE_DEBRIS_FIELD = "$salvageDebrisField";
//	public static final String SALVAGE_DEFENDER_FACTION = "$salvageDefFaction";
//	public static final String SALVAGE_DEFENDER_PROB = "$salvageDefProb";
	//public static final String SALVAGE_SPEC_ID = "$salvageSpecId";
	public static final String SALVAGE_DEFENDER_OVERRIDE = "$salvageDOv";
	
	public static final String ENTITY_MISSION_IMPORTANT = "$missionImportant";
	
	//public static final String BATTLE_CREATION_CONTEXT_SCRIPT = "$bcc_script";
	//public static final String FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE = "$fidConifg";
	public static final String FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN = "$fidConifgGen";
	
	public static final String FCM_FACTION = "$fcm_faction";
	public static final String FCM_EVENT = "$fcm_eventRef";
	
	
	public static final String FLEET_NO_MILITARY_RESPONSE = "$core_fleetNoMilitaryResponse";
	
	public static final String FLEET_FIGHT_TO_THE_LAST = "$core_fightToTheLast";
	
	public static final String FLEET_BUSY = "$core_fleetBusy";
	public static final String FLEET_MILITARY_RESPONSE = "$core_fleetMilitaryResponse";
	
	public static final String MEMORY_KEY_PATROL_ALLOW_TOFF = "$patrolAllowTOff";
	public static final String OBJECTIVE_NON_FUNCTIONAL = "$objectiveNonFunctional";
	public static final String MEMORY_KEY_NO_SHIP_RECOVERY = "$noShipRecovery";
	
	public static final String MARKET_MILITARY = "$military";
	public static final String MARKET_PATROL = "$patrol";
	
	public static final String MEMORY_KEY_SOURCE_MARKET = "$sourceMarket";
	public static final String MEMORY_KEY_PATROL_FLEET = "$isPatrol";
	public static final String MEMORY_KEY_WAR_FLEET = "$isWarFleet";
	public static final String MEMORY_KEY_RAIDER = "$isRaider";
	public static final String MEMORY_KEY_CUSTOMS_INSPECTOR = "$isCustomsInspector";
	public static final String MEMORY_KEY_TRADE_FLEET = "$isTradeFleet";
	public static final String MEMORY_KEY_SCAVENGER = "$isScavenger";
	public static final String MEMORY_KEY_SMUGGLER = "$isSmuggler";
	public static final String MEMORY_KEY_PIRATE = "$isPirate";
	public static final String MEMORY_KEY_FLEET_TYPE = "$fleetType";
	
	public static final String MEMORY_KEY_FORCE_TRANSPONDER_OFF = "$forceTOff";
	
	public static final String MEMORY_KEY_SKIP_TRANSPONDER_STATUS_INFO = "$skipTInfo";
	
	
	
	public static final String MEMORY_KEY_AVOID_PLAYER_SLOWLY = "$cfai_avoidPlayerSlowly";
	public static final String MEMORY_KEY_NEVER_AVOID_PLAYER_SLOWLY = "$cfai_neverAvoidPlayerSlowly";
	
	public static final String MEMORY_KEY_MAKE_HOSTILE = "$cfai_makeHostile";
	public static final String MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF = "$cfai_makeHostileWhileTOff";
	public static final String MEMORY_KEY_MAKE_NON_HOSTILE = "$cfai_makeNonHostile";
	public static final String MEMORY_KEY_MAKE_ALWAYS_PURSUE = "$cfai_makeAlwaysPursue";
	public static final String MEMORY_KEY_MAKE_PREVENT_DISENGAGE = "$cfai_makePreventDisengage";
	public static final String MEMORY_KEY_MAKE_ALLOW_DISENGAGE = "$cfai_makeAllowDisengage";
	public static final String MEMORY_KEY_MAKE_AGGRESSIVE = "$cfai_makeAggressive";
	public static final String MEMORY_KEY_MAKE_HOLD_VS_STRONGER = "$cfai_holdVsStronger";
	public static final String MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY = "$cfai_makeAggressiveLastsOneBattle";
	public static final String MEMORY_KEY_MAKE_NON_AGGRESSIVE = "$cfai_makeNonAggressive";
	public static final String MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER = "$cfai_recentlyDefeatedByPlayer";
	public static final String MEMORY_KEY_NO_JUMP = "$cfai_noJump";
	
	public static final String MEMORY_KEY_ALLOW_LONG_PURSUIT = "$cfai_longPursuit";
	
	public static final String MEMORY_KEY_IGNORE_PLAYER_COMMS = "$ignorePlayerCommRequests";
	
	public static final String MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_OFF = "$sawPlayerTransponderOff";
	public static final String MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON = "$sawPlayerTransponderOn";
	
	
	public static final String MEMORY_KEY_PURSUE_PLAYER = "$pursuePlayer";
	public static final String MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET = "$keepPursuingPlayer";
	public static final String MEMORY_MARKET_SMUGGLING_SUSPICION_LEVEL = "$smugglingSuspicion";
	public static final String MEMORY_KEY_PLAYER_HOSTILE_ACTIVITY_NEAR_MARKET = "$playerHostileTimeout";
	
	public static final String MEMORY_KEY_LOW_REP_IMPACT = "$lowRepImpact";
	public static final String MEMORY_KEY_NO_REP_IMPACT = "$noRepImpact";
	
	public static final String MEMORY_KEY_DO_NOT_SHOW_FLEET_DESC = "$shownFleetDescAlready";
	public static final String MEMORY_KEY_FORCE_AUTOFIT_ON_NO_AUTOFIT_SHIPS = "$overrideNoAutofit";
	
	public static final String MEMORY_KEY_MISSION_IMPORTANT = "$missionImportant";
	
	
	public static final String MEMORY_KEY_NUM_GR_INVESTIGATIONS = "$numGRInvestigations";
	public static final String MEMORY_KEY_REQUIRES_DISCRETION = "$requiresDiscretionToDeal";
	
	public static final String MARKET_DO_NOT_INIT_COMM_LISTINGS = "$doNotInitCommListings";

	public static String HIDDEN_BASE_MEM_FLAG = "$core_hiddenBase";
	
	
}










