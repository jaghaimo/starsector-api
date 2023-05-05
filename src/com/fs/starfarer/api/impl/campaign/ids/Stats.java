package com.fs.starfarer.api.impl.campaign.ids;

public class Stats {
	
	// character stats
	public static final String CAN_DEPLOY_LEFT_RIGHT_MOD = "can_deploy_left_right";
	
	public static final String AUXILIARY_EFFECT_ADD_PERCENT = "auxiliary_effect_add_percent";
	public static final String OFFICER_MAX_LEVEL_MOD = "officer_max_level_mod";
	public static final String OFFICER_MAX_ELITE_SKILLS_MOD = "officer_max_elite_skills_mod";
	public static final String BUILD_IN_BONUS_XP_MOD = "build_in_bonus_xp_mod";
	public static final String CUSTOM_PRODUCTION_MOD = "custom_production_mod";
	
	public static final String DEPLOYMENT_POINTS_FRACTION_OF_BATTLE_SIZE_BONUS_MOD = "deployment_points_fraction_of_battle_size_bonus_mod";
	public static final String DEPLOYMENT_POINTS_MIN_FRACTION_OF_BATTLE_SIZE_BONUS_MOD = "deployment_points_min_fraction_of_battle_size_bonus_mod";
	
	/**
	 * Set for both character (from having Neural Link skill) and ship (from having Neural Interface hullmod.
	 */
	public static final String HAS_NEURAL_LINK = "custom_production_mod";
	
	public static final String NUM_MAX_CONTACTS_MOD = "num_max_contacts_mod";
	
	// outposts - these are admin stats, so, applied in "CharacterStatsSkillEffect"
	public static final String FUEL_SUPPLY_BONUS_MOD = "fuel_supply_bonus";
	public static final String SUPPLY_BONUS_MOD = "supply_bonus";
	public static final String DEMAND_REDUCTION_MOD = "demand_reduction";
	
	// markets 
	//- old, will probably replace these two?
//	public static final String OFFICER_NUM_MULT = "officer_num_mult";
//	public static final String OFFICER_LEVEL_MULT = "officer_level_mult";
	
	public static final String SLIPSTREAM_REVEAL_RANGE_LY_MOD = "slipstream_reveal_range_ly_mod";
	
	public static final String ADMIN_PROB_MOD = "admin_prob";
	public static final String OFFICER_PROB_MOD = "officer_prob";
	public static final String OFFICER_ADDITIONAL_PROB_MULT_MOD = "additional_officer_prob_mult";
	public static final String OFFICER_IS_MERC_PROB_MOD = "officer_is_merc_prob";
	
	public static final String PRODUCTION_QUALITY_MOD = "production_quality_mod";
	public static final String FLEET_QUALITY_MOD = "fleet_quality_mod";
	public static final String COMBAT_FLEET_SIZE_MULT = "combat_fleet_size_mult";
	public static final String COMBAT_FLEET_SPAWN_RATE_MULT = "combat_fleet_spawn_rate_mult";
	
	public static final String PATROL_NUM_HEAVY_MOD = "patrol_num_heavy_mod";
	public static final String PATROL_NUM_MEDIUM_MOD = "patrol_num_medium_mod";
	public static final String PATROL_NUM_LIGHT_MOD = "patrol_num_light_mod";
	
	public static final String GROUND_DEFENSES_MOD = "ground_defenses_mod";
	
	public static final String TECH_MINING_MULT = "tech_mining_mult";
	
	/**
	 * Does not include structures.
	 */
	public static final String MAX_INDUSTRIES = "max_industries";
	
	// fleets
	public static final String FUEL_USE_NOT_SHOWN_ON_MAP_MULT = "fuel_use_not_shown_on_map_mult";
	
	public static final String PLANETARY_OPERATIONS_MOD = "ground_attack_mod";
	public static final String PLANETARY_OPERATIONS_CASUALTIES_MULT = "ground_attack_casualties_mult";
	
	public static final String CAN_SEE_NASCENT_POINTS = "can_see_nascent_points"; // not actually used, can always see
	
	public static final String SURVEY_MAX_HAZARD = "survey_max_hazard";
	public static final String SURVEY_COST_MULT = "survey_cost_mult";
	public static final String PLANET_MINING_VALUE_MULT = "planet_mining_value_mult";
	//public static final String SALVAGE_MAX_RATING = "salvage_max_rating";
	
	public static final String MOVE_SLOW_SPEED_BONUS_MOD = "move_slow_speed_bonus_mod";
	
	public static final String NAVIGATION_PENALTY_MULT = "nav_penalty_mult";
	public static final String COORDINATED_MANEUVERS_MAX = "coord_maneuvers_max";
	public static final String ELECTRONIC_WARFARE_MAX = "electronic_warfare_max";
	
	public static final String FUEL_SALVAGE_VALUE_MULT_FLEET = "fuel_salvage_value_mult_fleet";
	public static final String SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE = "salvage_value_bonus_fleet";
	public static final String SALVAGE_VALUE_MULT_FLEET_NOT_RARE = "salvage_value_bonus_fleet_not_rare";
	public static final String BATTLE_SALVAGE_MULT_FLEET = "battle_salvage_value_bonus_fleet";
	
	/**
	 * This one is on the commander, not the fleet.
	 */
	public static final String COMMAND_POINT_RATE_COMMANDER = "command_point_rate";
	public static final String NON_COMBAT_CREW_LOSS_MULT = "overall_crew_loss_mult";
	
	public static final String OWN_WEAPON_RECOVERY_MOD = "own_weapon_recovery_mod";
	public static final String OWN_WING_RECOVERY_MOD = "own_wing_recovery_mod";
	
	public static final String ENEMY_WEAPON_RECOVERY_MOD = "enemy_weapon_recovery_mod";
	public static final String ENEMY_WING_RECOVERY_MOD = "enemy_wing_recovery_mod";
	
	public static final String SHIP_RECOVERY_MOD = "ship_recovery_mod";
	public static final String SHIP_DMOD_REDUCTION = "ship_dmod_reduction_mod";
	
	public static final String OFFICER_SHIP_RECOVERY_MOD = "officer_ship_recovery_mod";
	
	public static final String RECOVERED_CR_MIN = "ship_recovery_cr_min";
	public static final String RECOVERED_CR_MAX = "ship_recovery_cr_max";
	public static final String RECOVERED_HULL_MIN = "ship_recovery_hull_min";
	public static final String RECOVERED_HULL_MAX = "ship_recovery_hull_max";
	
	
	// fleet ability modifiers
	public static final String GO_DARK_DETECTED_AT_MULT = "go_dark_effectiveness";
	//public static final String GO_DARK_BURN_PENALTY_MULT = "go_dark_burn_penalty_mult";
	//public static final String SENSOR_BURST_BURN_PENALTY_MULT = "sensor_burst_penalty_mult";
	public static final String SUSTAINED_BURN_BONUS = "sustained_burn_bonus";
	public static final String EMERGENCY_BURN_CR_MULT = "emergency_burn_mult";
	public static final String DIRECT_JUMP_CR_MULT = "direct_jump_cr_mult";
	
	
	// fleet members
	//public static final String CR_LOSS_WHEN_DISABLED_MULT = "cr_loss_when_disabled_mult";
	
	public static final String FLEET_GROUND_SUPPORT = "ground_support";
	public static final String FLEET_BOMBARD_COST_REDUCTION = "fleet_bombard_cost_reduction";
	
	public static final String CORONA_EFFECT_MULT = "corona_resistance";
	public static final String BOARDING_CHANCE_MULT = "boarding_chance_mult";
	
	/**
	 * 20% of this modifier applies to post-combat salvage.
	 */
	public static final String SALVAGE_VALUE_MULT_MOD = "salvage_value_bonus_ship";
	
	public static final String PHASE_FIELD_SENSOR_PROFILE_MOD  = "phase_field_sensor_profile_mod";
	public static final String HRS_SENSOR_RANGE_MOD  = "hrs_sensor_range_mod";
	
	//public static final String BATTLE_SALVAGE_VALUE_MULT_MOD = "battle_salvage_value_bonus_ship";
	public static final String HULL_DAMAGE_CR_LOSS = "hull_damage_cr_loss";
	public static final String SURVEY_COST_REDUCTION = "survey_cost_reduction_";
	public static final String FLEET_BURN_BONUS = "fleet_burn_bonus";
	public static String getSurveyCostReductionId(String commodityId) {
		return SURVEY_COST_REDUCTION + commodityId;
	}
	
	// ships
	public static final String ACT_AS_COMBAT_SHIP = "act_as_combat_ship";
	
	/**
	 * Modifying suppliesToRecover does not affect deployment points.
	 * But this modifies the base value of supplies to recover for deployment points purposes only.
	 */
	public static final String DEPLOYMENT_POINTS_MOD = "deployment_points_mod";
	
	public static final String FIGHTER_REARM_TIME_EXTRA_FLAT_MOD = "fighter_rearm_time_extra_flat_mod";
	public static final String FIGHTER_REARM_TIME_EXTRA_PER_WING_MOD = "fighter_rearm_time_extra_per_wing_mod";
	public static final String FIGHTER_REARM_TIME_EXTRA_FRACTION_OF_BASE_REFIT_TIME_MOD = "fighter_rearm_time_extra_fraction_of_base_refit_time_mod";
	
	//public static final String HAS_FORCE_CONCENTRATION_BONUS_MOD = "has_force_concentration_bonus";
	
	
	public static final String PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD = "phase_cloak_flux_level_for_min_speed_mod";
	
	public static final String MAX_PERMANENT_HULLMODS_MOD = "max_permanent_hullmods_mod";
	public static final String MAX_LOGISTICS_HULLMODS_MOD = "max_logistics_hullmods_mod";
	public static final String PHASE_CLOAK_SPEED_MOD = "phase_cloak_speed";
	public static final String PHASE_CLOAK_ACCEL_MOD = "phase_cloak_accel";
	
	public static final String PD_IGNORES_FLARES = "pd_ignores_flares";
	public static final String PD_BEST_TARGET_LEADING = "pd_best_target_leading";
	
	public static final String SHIELD_PIERCED_MULT = "shield_pierced_mult";
	public static final String PHASE_TIME_BONUS_MULT = "phase_time_mult";
	
	public static final String FIGHTER_CREW_LOSS_MULT = "fighter_crew_loss_mult";
	
	public static final String EXPLOSION_RADIUS_MULT = "explosion_radius_mult";
	public static final String EXPLOSION_DAMAGE_MULT = "explosion_damage_mult";
	
	public static final String COORDINATED_MANEUVERS_FLAT = "coord_maneuvers_flat";
	public static final String ELECTRONIC_WARFARE_FLAT = "electronic_warfare_flat";
	public static final String ELECTRONIC_WARFARE_PENALTY_MULT = "electronic_warfare_penalty_mult";
	public static final String ELECTRONIC_WARFARE_PENALTY_MOD = "electronic_warfare_penalty_mod";
	
	public static final String ELECTRONIC_WARFARE_PENALTY_MAX_FOR_SHIP_MOD = "electronic_warfare_penalty_max_for_ship_mod";
	public static final String COMMAND_POINT_RATE_FLAT = "command_point_rate_flat";
	public static final String INDIVIDUAL_SHIP_RECOVERY_MOD = "individual_ship_recovery_mod";
	
	public static final String INSTA_REPAIR_FRACTION = "insta_repair_fraction";
	
	public static final String CR_MALFUNCION_RANGE = "cr_malfunction_range_mult";
	
	public static final String REPLACEMENT_RATE_DECREASE_MULT = "replacement_rate_decrease_mult";
	public static final String REPLACEMENT_RATE_INCREASE_MULT = "replacement_rate_increase_mult";
	
	public static final String DMOD_EFFECT_MULT = "dmod_effect_mult";
	public static final String DMOD_AVOID_PROB_MOD = "dmod_avoid_prob_mod";
	
	/**
	 * Base value is 1.
	 */
	public static final String DMOD_ACQUIRE_PROB_MOD = "dmod_acquire_prob_mod";
	public static final String DMOD_REDUCE_MAINTENANCE = "dmod_reduce_maintenance";
	
	
	// OP cost reductions - used from core, but specified as dynamic stats to avoid adding all the
	// always-there stats to MutableShipStats.
	public static final String SMALL_BALLISTIC_MOD = "small_ballistic_mod";
	public static final String MEDIUM_BALLISTIC_MOD = "medium_ballistic_mod";
	public static final String LARGE_BALLISTIC_MOD = "large_ballistic_mod";
	
	public static final String ALL_FIGHTER_COST_MOD = "all_fighter_cost_mod";
	public static final String FIGHTER_COST_MOD = "fighter_cost_mod";
	public static final String BOMBER_COST_MOD = "bomber_cost_mod";
	public static final String INTERCEPTOR_COST_MOD = "interceptor_cost_mod";
	public static final String SUPPORT_COST_MOD = "support_cost_mod";
	
	public static final String SMALL_ENERGY_MOD = "small_energy_mod";
	public static final String MEDIUM_ENERGY_MOD = "medium_energy_mod";
	public static final String LARGE_ENERGY_MOD = "large_energy_mod";
	
	public static final String SMALL_MISSILE_MOD = "small_missile_mod";
	public static final String MEDIUM_MISSILE_MOD = "medium_missile_mod";
	public static final String LARGE_MISSILE_MOD = "large_missile_mod";
	
	public static final String SMALL_PD_MOD = "small_pd_mod";
	public static final String MEDIUM_PD_MOD = "medium_pd_mod";
	public static final String LARGE_PD_MOD = "large_pd_mod";
	
	public static final String SMALL_BEAM_MOD = "small_beam_mod";
	public static final String MEDIUM_BEAM_MOD = "medium_beam_mod";
	public static final String LARGE_BEAM_MOD = "large_beam_mod";
		
	public static final String CONVERTED_HANGAR_MOD = "converted_hangar_mod";
	public static final String CONVERTED_HANGAR_NO_CREW_INCREASE = "converted_hangar_no_crew_increase";
	public static final String CONVERTED_HANGAR_NO_REARM_INCREASE = "converted_hangar_no_rearm_increase";
	public static final String CONVERTED_HANGAR_NO_DP_INCREASE = "converted_hangar_no_dp_increase";
	public static final String CONVERTED_HANGAR_NO_REFIT_PENALTY = "converted_hangar_no_refit_penalty";
	//public static final String CONVERTED_HANGAR_NO_PERFORMANCE_PENALTY = "converted_hangar_no_performance_penalty";
	//public static final String CONVERTED_HANGAR_NO_COST_INCREASE = "converted_hangar_no_cost_increase";
	
	
	/**
	 * Base chance is 0.5. 
	 */
	public static final String MODULE_DETACH_CHANCE_MULT = "module_detach_chance_mult";
	
	public static final String DO_NOT_FIRE_THROUGH = "do_not_fire_through";
	
	
//	public static final String SALVAGE_COST_REDUCTION = "survey_cost_reduction_";
//	public static String getSalvageCostReductionId(String commodityId) {
//		return SALVAGE_COST_REDUCTION + commodityId;
//	}
	
	// markets
	public static final String TRADE_IMPACT_MULT_PREFIX = "trade_impact_mult_";
	public static String getPlayerTradeRepImpactMultId(String commodityId) {
		return TRADE_IMPACT_MULT_PREFIX + commodityId;
	}
	public static String getPlayerBuyRepImpactMultId(String commodityId) {
		return TRADE_IMPACT_MULT_PREFIX + commodityId + "_buy";
	}
	public static String getPlayerSellRepImpactMultId(String commodityId) {
		return TRADE_IMPACT_MULT_PREFIX + commodityId + "_sell";
	}
	

	
}
