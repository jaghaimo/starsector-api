package com.fs.starfarer.api.impl.campaign.ids;

public class Tags {
	
	/**
	 * On fleet members and on variants.
	 */
	public static final String TAG_NO_AUTOFIT = "no_autofit";
	public static final String TAG_AUTOMATED_NO_PENALTY = "no_auto_penalty";
	public static final String TAG_RETAIN_SMODS_ON_RECOVERY = "retain_smods_on_recovery";
	//public static final String NO_RESTORATION = "no_restoration";
	
	public static final String TAG_NO_AUTOFIT_UNLESS_PLAYER = "no_autofit_unless_player";
	
	/**
	 * In ship_systems.csv, NOT on ships
	 */
	public static final String SYSTEM_USES_DAMPER_FIELD_AI = "uses_damper_ai";
	
	public static final String SPECIAL_ALLOWS_SYSTEM_USE = "special_allows_system_use";
	public static final String SYSTEM_ALLOWS_SPECIAL_USE = "system_allows_special_use";
	
	
	/**
	 * Derelict ship does not allow story point recovery. 
	 */
	public static final String UNRECOVERABLE = "unrecoverable";
	
	public static final String VARIANT_ALLOW_EXCESS_OP_ETC = "variant_allow_excess_op_etc";
	public static final String VARIANT_ALWAYS_RETAIN_SMODS_ON_SALVAGE = "variant_always_retain_smods_on_salvage";
	public static final String VARIANT_CONSISTENT_WEAPON_DROPS = "consistent_weapon_drops";
	public static final String VARIANT_DO_NOT_DROP_AI_CORE_FROM_CAPTAIN = "no_ai_core_drop";
	public static final String VARIANT_ALWAYS_RECOVERABLE = "always_recoverable";
	public static final String VARIANT_UNBOARDABLE = "unboardable";
	
	
	public static final String HULL_UNRESTORABLE = "unrestorable";
	public static final String VARIANT_UNRESTORABLE = "unrestorable";
	
	
	/**
	 * Faster spawning for a not-a-real-ship just used to fire beams (and generate threat for the AI to respond to them).
	 * Automatically added by createFXDrone(). 
	 */
	public static final String VARIANT_FX_DRONE = "fx_drone";

	
	
	/**
	 * For phase cloak, whether it can be disrupted by nearby shots/objects.
	 */
	public static final String DISRUPTABLE = "disruptable";
	
	public static final String NO_MARKET_INFO = "no_market_info";
	
	
	
	public static final String MARKET_NO_INDUSTRIES_ALLOWED = "market_no_industries_allowed";
	
	public static final String MARKET_NO_OFFICER_SPAWN = "market_no_officer_spawn";
	
	
	public static final String AUTOMATED_RECOVERABLE = "auto_rec";
	
	public static final String RESTRICTED = "restricted";
	
	public static final String MILITARY_MARKET_ONLY = "req_military";
	
	/**
	 * Fighter wings with this tag can be installed on automated ships. 
	 */
	public static final String AUTOMATED_FIGHTER = "auto_fighter";
	public static final String ONLY_ALLOW_AUTOMATED_FIGHTERS = "only_allow_automated_fighters";
	
	
	public static final String NO_ENTITY_TOOLTIP = "no_entity_tooltip";  
	
	public static final String NO_TOPOGRAPHY_SCANS = "no_topography_scans";
	
	public static final String PK_SYSTEM = "pk_system";
	public static final String SYSTEM_CUT_OFF_FROM_HYPER = "system_cut_off_from_hyper";
	public static final String SYSTEM_ABYSSAL = "system_abyssal";
	
	public static final String THEME_HIDDEN = "theme_hidden";
	
	public static final String THEME_CORE_UNPOPULATED = "theme_core_unpopulated";
	public static final String THEME_CORE_POPULATED = "theme_core_populated";
	public static final String THEME_CORE = "theme_core";
	
	/**
	 * Used to mark systems that would be "interesting" for the player to visit, due to ruins, derelicts,
	 * anything special, and so on. These systems will be more likely to be picked for containing the 
	 * targets of randomly generated missions. Should ONLY be used where it's acceptable that this may happen. 
	 */
	public static final String THEME_INTERESTING = "theme_interesting";
	
	/**
	 * Used to mark systems that would be "interesting" for the player to visit, due to ruins, derelicts,
	 * anything special, and so on. These systems will be more likely to be picked for containing the 
	 * targets of randomly generated missions. Should ONLY be used where it's acceptable that this may happen. 
	 */
	public static final String THEME_INTERESTING_MINOR = "theme_interesting_minor";
	
	public static final String THEME_MISC = "theme_misc";
	public static final String THEME_MISC_SKIP = "theme_misc_skip";
	
	public static final String THEME_RUINS = "theme_ruins";
	public static final String THEME_RUINS_MAIN = "theme_ruins_main";
	public static final String THEME_RUINS_SECONDARY = "theme_ruins_secondary";
	
	/**
	 * Marker meaning that this system should not be picked again for something special during procgen, like
	 * the "Red Planet" mission.
	 */
	public static final String THEME_SPECIAL = "theme_special";
	
	public static final String THEME_DERELICT = "theme_derelict";
	public static final String THEME_DERELICT_MOTHERSHIP = "theme_derelict_mothership";
	public static final String THEME_DERELICT_CRYOSLEEPER = "theme_derelict_cryosleeper";
	public static final String THEME_DERELICT_SURVEY_SHIP = "theme_derelict_survey_ship";
	public static final String THEME_DERELICT_PROBES = "theme_derelict_probes";
	
	public static final String HAS_CORONAL_TAP = "has_coronal_tap";
	
	public static final String THEME_UNSAFE = "theme_unsafe"; // relatively powerful fleets that may interfere with missions
	public static final String THEME_REMNANT = "theme_remnant";
	public static final String THEME_REMNANT_MAIN = "theme_remnant_main";
	public static final String THEME_REMNANT_SECONDARY = "theme_remnant_secondary";
	public static final String THEME_REMNANT_NO_FLEETS = "theme_remnant_no_fleets";
	public static final String THEME_REMNANT_DESTROYED = "theme_remnant_destroyed";
	public static final String THEME_REMNANT_SUPPRESSED = "theme_remnant_suppressed";
	public static final String THEME_REMNANT_RESURGENT = "theme_remnant_resurgent";
	
	public static final String SALVAGE_ENTITY_NO_REMOVE = "no_remove";
	public static final String SALVAGE_ENTITY_NO_DEBRIS = "no_debris";
	public static final String SALVAGE_ENTITY_NON_CUSTOM = "not_a_custom_entity";
	//public static final String SALVAGE_ENTITY_NO_SALVAGE = "no_salvage";
	
	public static final String WEAPON_REMNANTS = "remnant";
	public static final String SHIP_REMNANTS = "remnant";
	
	public static final String SHIP_CAN_NOT_SCUTTLE = "ship_can_not_scuttle";
	public static final String SHIP_UNIQUE_SIGNATURE = "ship_unique_signature";
	
	public static final String SHIP_RECOVERABLE = "ship_recoverable";
	public static final String SHIP_LIMITED_TOOLTIP = "ship_limited_tooltip";
	//public static final String SHIP_DESC_ID = "ship_desc_id";
	
	public static final String DO_NOT_RESPAWN_PLAYER_IN = "do_not_respawn_player_in";
	
	
	
	public static final String MODULE_UNSELECTABLE = "module_unselectable";
	public static final String MODULE_REFIT_BRIGHT = "module_refit_bright";
	public static final String MODULE_NO_STATUS_BAR = "module_no_status_bar";
	public static final String MODULE_HULL_BAR_ONLY = "module_hull_bar_only";
	
	
	public static final String HULLMOD_PHASE_BRAWLER = "phase_brawler";
	
	public static final String HULLMOD_NO_DROP = "no_drop";
	public static final String HULLMOD_NO_DROP_SALVAGE = "no_drop_salvage";
	
	// battle-damage related hullmods
	public static final String HULLMOD_DMOD = "dmod";
	public static final String HULLMOD_DAMAGE = "damage";
	public static final String HULLMOD_DAMAGE_PHASE = "phaseDamage";
	public static final String HULLMOD_CIV = "civ";
	public static final String HULLMOD_REQ_SHIELDS = "reqShields";
	public static final String HULLMOD_NOT_CIV = "notCiv";
	public static final String HULLMOD_CIV_ONLY = "civOnly";
	public static final String HULLMOD_DAMAGE_STRUCT = "damageStruct";
	public static final String HULLMOD_DESTROYED_ALWAYS = "destroyedDamageAlways";
	//public static final String HULLMOD_CIV_ALWAYS = "civDamageAlways";
	public static final String HULLMOD_FIGHTER_BAY_DAMAGE = "fighterBayDamage";
	public static final String HULLMOD_CARRIER_ALWAYS = "carrierDamageAlways";
	public static final String HULLMOD_PEAK_TIME = "peak_time";
	public static final String HULLMOD_NOT_PHASE = "notPhase";
	public static final String HULLMOD_NOT_AUTO = "notAuto";
	
	public static final String HULLMOD_NO_BUILD_IN = "no_build_in";
	
	public static final String HULLMOD_SHIELDS = "shields";
	public static final String HULLMOD_DEFENSIVE = "defensive";
	public static final String HULLMOD_OFFENSIVE = "offensive";
	public static final String HULLMOD_ENGINES = "engines";
	public static final String HULLMOD_SPECIAL = "special";
	
	
	public static final String WING_NO_DROP = "no_drop";
	public static final String WING_NO_SELL = "no_sell";
	
	public static final String WING_RAPID_REFORM = "rapid_reform";
	public static final String WING_LEADER_NO_SWARM = "leader_no_swarm";
	public static final String WING_WINGMEN_NO_SWARM = "wingmen_no_swarm";
	public static final String WING_MATCH_LEADER_FACING = "match_leader_facing";
	public static final String WING_ATTACK_AT_AN_ANGLE = "attack_at_an_angle";
	public static final String WING_INDEPENDENT_OF_CARRIER = "independent_of_carrier";
	public static final String WING_TARGET_ESCORT_TARGET = "target_escort_target";
	
	/**
	 * Can apply to wing or to fighter's hull variant. 
	 */
	public static final String WING_STAY_IN_FRONT_OF_SHIP = "stay_in_front_of_ship";
	
	public static final String NO_DROP = "no_drop"; // general-purpose
	public static final String NO_BP_DROP = "no_bp_drop"; // no blueprints dropped during raid
	public static final String WEAPON_NO_SELL = "no_sell"; // not for sale at markets
	public static final String NO_SELL = "no_sell"; // not for sale at markets
	
	public static final String MISSION_ITEM = "mission_item";
	
	
	/**
	 * Stuff that shouldn't be able to be destroyed. Currently only works for campaign objectives.
	 * See also: MemFlags.STORY_CRITIAL, for things that are story critical with specific "reasons" and
	 * stop being critical once the reasons are all no longer valid.
	 */
	public static final String STORY_CRITICAL = "story_critical";
	
	
	public static final String STAR = "star";
	public static final String PLANET = "planet";
	public static final String GAS_GIANT = "gas_giant";
	public static final String TERRAIN = "terrain";
	public static final String SYSTEM_ANCHOR = "system_anchor";
	public static final String NON_CLICKABLE = "non_clickable";
	public static final String FADING_OUT_AND_EXPIRING = "fading_out_and_expiring";
	
	public static final String ACCRETION_DISK = "accretion_disk";
	
	public static final String AMBIENT_LS = "ambient_ls";
	
	public static final String HAS_INTERACTION_DIALOG = "has_interaction_dialog";
	
//	public static final String MARKET_PATROL = "market_patrol";
//	public static final String MARKET_MILITARY = "market_military";
	
	public static final String COMM_RELAY = "comm_relay";
	public static final String NAV_BUOY = "nav_buoy";
	public static final String SENSOR_ARRAY = "sensor_array";
	public static final String OBJECTIVE = "objective";
	public static final String STABLE_LOCATION = "stable_location";
	public static final String MISSION_LOCATION = "mission_location";
	public static final String MAKESHIFT = "makeshift";
	
	
	public static final String USE_STATION_VISUAL = "use_station_visual";
	
	public static final String STATION = "station";
	public static final String GATE = "gate";
	public static final String ORBITAL_JUNK = "orbital_junk";
	
	public static final String TRANSIENT = "transient";
	public static final String JUMP_POINT = "jump_point";
	public static final String STELLAR_MIRROR = "stellar_mirror";
	public static final String STELLAR_SHADE = "stellar_shade";
	
	public static final String CRYOSLEEPER = "cryosleeper";
	public static final String CORONAL_TAP = "coronal_tap";
	
	public static final String WARNING_BEACON = "warning_beacon";
	public static final String BEACON_LOW = "beacon_low";
	public static final String BEACON_MEDIUM = "beacon_medium";
	public static final String BEACON_HIGH = "beacon_high";
	
	
	public static final String PROTECTS_FROM_CORONA_IN_BATTLE  = "protects_from_corona_in_battle";
	
	public static final String SALVAGE_MUSIC= "salvage_music";
	
	public static final String SALVAGEABLE = "salvageable";
	public static final String DEBRIS_FIELD = "debris";
	
	public static final String WRECK = "wreck";
	
	public static final String SYSTEM_ALREADY_USED_FOR_STORY = "system_already_used_for_story";
	
	/**
	 * Will expire at some point in the near future; not everything that expires is guaranteed to have this tag.
	 */
	public static final String EXPIRES = "expires";
	
	public static final String NOT_RANDOM_MISSION_TARGET = "not_random_mission_target";
	

	
	public static final String NEUTRINO = "neutrino";
	public static final String NEUTRINO_LOW = "neutrino_low";
	public static final String NEUTRINO_HIGH = "neutrino_high";
	
	public static final String REPORT_REP = "reputation_change";
	public static final String REPORT_PRICES = "prices";
	public static final String REPORT_IMPORTANT = "important";
	public static final String REPORT_NOMAP = "nomap";
	public static final String REPORT_NO_SYSTEM = "no_system_prefix";
	public static final String FLEET_LOG = "fleet_log";
	public static final String INCOME_REPORT = "income_report";
	
	
//	public static final String INTEL_HOSTILE = "Hostile";
//	public static final String INTEL_NOT_HOSTILE = "Not Hostile";
	
	public static final String MISSION_PRIORITY = "priority";
	public static final String MISSION_NON_REPEATABLE = "non_repeatable";
	
	public static final String INTEL_IMPORTANT = "Important";
	public static final String INTEL_NEW = "New";
	public static final String INTEL_BOUNTY = "Bounties";
	public static final String INTEL_MAJOR_EVENT = "Major events";
	public static final String INTEL_CONTACTS = "Contacts";
	public static final String INTEL_FLEET_DEPARTURES = "Fleet departures";
	public static final String INTEL_SMUGGLING = "Smuggling";
	public static final String INTEL_EXPLORATION = "Exploration";
	public static final String INTEL_FLEET_LOG = "Fleet log";
	public static final String INTEL_LOCAL = "Local";
	public static final String INTEL_TRADE = "Trade";
	public static final String INTEL_MISSIONS = "Missions";
	//public static final String INTEL_OFFERS = "Offers";
	public static final String INTEL_ACCEPTED = "Accepted";
	public static final String INTEL_STORY = "Story";
	public static final String INTEL_COMMISSION = "Commission";
	public static final String INTEL_AGREEMENTS = "Agreements";
	public static final String INTEL_HOSTILITIES = "Hostilities";
	public static final String INTEL_MILITARY = "Military";
	public static final String INTEL_BEACON = "Warning beacons";
	public static final String INTEL_GATES = "Gates";
	public static final String INTEL_SHRINES = "Luddic shrines";
	public static final String INTEL_COLONIES = "Colony threats";
	public static final String INTEL_COMM_SNIFFERS = "Comm sniffers";
	
	public static final String INTEL_DECIVILIZED = "Decivilized";
	
	public static final String INTEL_PRODUCTION = "Production";
	
	public static final String CONTACT_MILITARY = "military";
	public static final String CONTACT_TRADE = "trade";
	public static final String CONTACT_UNDERWORLD = "underworld";
	public static final String CONTACT_SCIENCE = "science";
	public static final String CONTACT_PATHER = "pather";
	
	public static final String INVOLUNTARY_RETIREMENT = "involuntary_retirement";
	public static final String REPLACEMENT_ARCHON = "replacement_archon";
	
	public static final String GHOST = "ghost";
	
	public static final String UNAFFECTED_BY_SLIPSTREAM = "unaffected_by_slipstream";
	public static final String IMMUNE_TO_REMORA_PULSE = "immune_to_remora_pulse";
	public static final String ZIG_GHOST = "zig_ghost";
	
	public static final String OMEGA = "omega";
	
	public static final String DAMAGE_SPECIAL = "damage_special";
	public static final String DAMAGE_SOFT_FLUX = "damage_soft_flux";
	
	public static final String LIDAR = "lidar";
	public static final String NOVA = "nova";
	public static final String FIXED_RANGE = "fixed_range";
	
	public static final String FIRES_ONE_BURST = "fires_one_burst";
	
	public static final String KANTA_GIFT = "kanta_gift";
	
	public static final String LUDDIC_SHRINE = "luddic_shrine";
	
	public static final String SLIPSTREAM_VISIBLE_IN_ABYSS = "slipstream_visible_in_abyss";
	
	public static final String TEMPORARY_LOCATION = "temporary_location";
	public static final String STAR_HIDDEN_ON_MAP = "star_hidden_on_map";
	
	// MissileAutoloader
	public static final String RELOAD_1PT = "reload_1pt";
	public static final String RELOAD_1_AND_A_HALF_PT = "reload_1_and_a_half_pt";
	public static final String RELOAD_2PT = "reload_2pt";
	public static final String RELOAD_3PT = "reload_3pt";
	public static final String RELOAD_4PT = "reload_4pt";
	public static final String RELOAD_5PT = "reload_5pt";
	public static final String RELOAD_6PT = "reload_6pt";
	public static final String NO_RELOAD = "no_reload";
	
	
}








