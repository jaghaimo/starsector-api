package com.fs.starfarer.api.impl.campaign.econ;

public class ConditionData {

	
	
	public static final float BASE_SUPPLIES_1 = 5f;
	public static final float BASE_SUPPLIES_2 = 20f;
	public static final float BASE_SUPPLIES_3 = 50f;
	
	public static final float CREW_MARINES_NON_CONSUMING_FRACTION = 0.5f;
	
	public static final float AQUACULTURE_MACHINERY_MULT = 0.001f;
	public static final float AQUACULTURE_FOOD_MULT = 0.2f;
	//public static final float AQUACULTURE_ORGANICS_MULT = 0.01f;
	
	
	public static final float AUTOFAC_HEAVY_METALS = 1000;
	public static final float AUTOFAC_HEAVY_RARE_METALS = 100;
	public static final float AUTOFAC_HEAVY_VOLATILES = 1000;
	public static final float AUTOFAC_HEAVY_ORGANICS = 1000;
	//public static final float AUTOFAC_HEAVY_CREW = 1000;
	public static final float AUTOFAC_HEAVY_MACHINERY_DEMAND = 200;
	public static final float AUTOFAC_HEAVY_MACHINERY = 1200;
	public static final float AUTOFAC_HEAVY_SUPPLIES = 5000; // 2000
	public static final float AUTOFAC_HEAVY_HAND_WEAPONS = 2000;

	public static final float COTTAGE_INDUSTRY_ORGANICS = 2000f;
	public static final float COTTAGE_INDUSTRY_DOMESTIC_GOODS = 1000f;
	
	public static final float CRYOSANCTUM_VOLATILES_MULT = 0.1f;
	public static final float CRYOSANCTUM_ORGANICS_MULT = 0.1f;
	public static final float CRYOSANCTUM_MACHINERY_MULT = 0.01f;
	public static final float CRYOSANCTUM_CREW_MULT = 0.01f;
	public static final float CRYOSANCTUM_ORGANS = 500f;
	public static final float CRYOSANCTUM_CREW = 50f;
	
	public static final float DECIV_WEAPONS_MULT = 0.01f;
	public static final float DECIV_CREW_MULT = 5f;
	public static final float DECIV_SUPPLIES = 0.01f;
	public static final float DECIV_DRUGS_MULT = 5f;
	public static final float DECIV_GOODS_PENALTY = 0f;
	public static final float DECIV_PRODUCTION_PENALTY = 0f;
	public static final float DECIV_FOOD_PENALTY = 0.25f;
	public static final float DECIV_DEMAND_PRICE_MULT = 0.5f;
	
	public static final float DISSIDENT_WEAPONS_MULT = 0.01f;
	public static final float DISSIDENT_MARINES_MULT = 0.001f;
	public static final float DISSIDENT_CREW_MULT = 2f;
	
	public static final float FREE_PORT_DRUGS = 100f;
	
	public static final float FRONTIER_WEAPONS = 0.001f;
	public static final float FRONTIER_SUPPLIES = 0.01f;
	public static final float FRONTIER_LUXURY_PENALTY = 0.1f;
	
	public static final float FUEL_PRODUCTION_ORGANICS = 1000;
	public static final float FUEL_PRODUCTION_VOLATILES = 1000;
	public static final float FUEL_PRODUCTION_RARE_METALS = 300;
	public static final float FUEL_PRODUCTION_MACHINERY = 200;
	//public static final float FUEL_PRODUCTION_CREW = 500;
	public static final float FUEL_PRODUCTION_FUEL = 2500;
	//public static final float FUEL_PRODUCTION_FUEL = 60000;
	
	public static final float HEADQUARTERS_OFFICER_NUM_MULT_BONUS = 0.25f;
	public static final float HEADQUARTERS_OFFICER_LEVEL_MULT_BONUS = 0.25f;
	
	//public static final float HYDROPONICS_COMPLEX_FOOD = 100000;
	public static final float HYDROPONICS_COMPLEX_FOOD = 400;
//	public static final float HYDROPONICS_COMPLEX_ORGANICS = 5000;
//	public static final float HYDROPONICS_COMPLEX_CREW = 1000;
//	public static final float HYDROPONICS_COMPLEX_MACHINERY = 250;
	
	public static final float LIGHT_INDUSTRY_ORGANICS = 500f;
	public static final float LIGHT_INDUSTRY_VOLATILES = 500f;
	public static final float LIGHT_INDUSTRY_MACHINERY = 100f;
	public static final float LIGHT_INDUSTRY_DOMESTIC_GOODS = 1000f;
	public static final float LIGHT_INDUSTRY_LUXURY_GOODS = 200f;
	
	public static final float LUDDIC_MAJORITY_LUXURY_MULT = 0.1f;
	
	public static final float MILITARY_BASE_SUPPLIES = 1000;
	public static final float MILITARY_BASE_FUEL = 4000;
	public static final float MILITARY_BASE_WEAPONS = 1000;
	public static final float MILITARY_BASE_MACHINERY = 50;
	public static final float MILITARY_BASE_MARINES_DEMAND = 100;
	public static final float MILITARY_BASE_CREW_DEMAND = 500;
	public static final float MILITARY_BASE_MARINES_SUPPLY = 150; // 200
	public static final float MILITARY_BASE_CREW_SUPPLY = 500;
	public static final float MILITARY_BASE_OFFICER_LEVEL_MULT_BONUS = 0.25f;
	public static final float MILITARY_BASE_OFFICER_NUM_MULT_BONUS = 0.25f;
	
	public static final float ORBITAL_BURNS_FOOD_BONUS = 2f;
	
	public static final float ORBITAL_STATION_FUEL_BASE = 1000f;
	public static final float ORBITAL_STATION_FUEL_MAX = 15000f; // 10000f;
	public static final float ORBITAL_STATION_REGULAR_CREW_SUPPLY = 500f;
	public static final float ORBITAL_STATION_SUPPLIES = 1000f;
	public static final float ORBITAL_STATION_CREW = 500f;
	public static final float ORBITAL_STATION_FUEL_MULT = 0.002f;
	//public static final float ORBITAL_STATION_OFFICER_LEVEL_MULT_BONUS = 0.25f;
	public static final float ORBITAL_STATION_OFFICER_NUM_MULT_BONUS = 0.25f;
	
	public static final float ORE_MINING_ORE = 2000f;
	public static final float ORE_MINING_RARE_ORE = 400;
	public static final float ORE_MINING_MACHINERY = 100;
	//public static final float ORE_MINING_CREW = 500;
	
	public static final float ORE_REFINING_ORE = 3000f;
	public static final float ORE_REFINING_RARE_ORE = 600f;
	public static final float ORE_REFINING_METAL_PER_ORE = 0.5f;
	public static final float ORE_REFINING_MACHINERY = 100;
	//public static final float ORE_REFINING_CREW = 500;
	
	public static final float ORGANICS_MINING_ORGANICS = 5500f;
	public static final float ORGANICS_MINING_MACHINERY = 100;
	//public static final float ORGANICS_MINING_CREW = 1000;
	
	public static final float ORGANIZED_CRIME_VOLATILES = 500f;
	public static final float ORGANIZED_CRIME_ORGANICS = 500f;
	public static final float ORGANIZED_CRIME_DRUGS = 1000f;
	public static final float ORGANIZED_CRIME_ORGANS = 400f;
	public static final float ORGANIZED_CRIME_MARINES = 100f;
	public static final float ORGANIZED_CRIME_WEAPONS = 50f;
	
	public static final float OUTPOST_MARINES_SIZE_1 = 5;
	public static final float OUTPOST_MARINES_SIZE_2 = 10;
	public static final float OUTPOST_MARINES_SIZE_3 = 50;
	public static final float OUTPOST_MARINES_MAX = 100;
	public static final float OUTPOST_FUEL = 1000;

	public static final float REFUGEE_POPULATION_PENALTY = 0.5f;
	public static final float REFUGEE_GREEN_CREW_MIN = 10f;
	public static final float REFUGEE_GREEN_CREW_MULT = 2f;
	
	public static final float RURAL_POLITY_FOOD_BONUS = 2f;
	public static final float RURAL_POLITY_DEMAND_MULT = 0.5f;
	
	public static final float SHIPBREAKING_CREW = 5000f;
	public static final float SHIPBREAKING_MACHINERY = 500f;
	public static final float SHIPBREAKING_SUPPLIES = 10000f;
	public static final float SHIPBREAKING_METALS = 4000f;
	public static final float SHIPBREAKING_RARE_METALS = 2000f;
	
	public static final float SPACEPORT_FUEL_BASE = 1000f;
	public static final float SPACEPORT_FUEL_MAX = 15000f; //10000f;
	public static final float SPACEPORT_SUPPLIES = 1000f;
	public static final float SPACEPORT_CREW = 2000f;
	public static final float SPACEPORT_FUEL_MULT = 0.002f;
	public static final float SPACEPORT_OFFICER_NUM_MULT_BONUS = 0.25f;
	
	public static final float TRADE_CENTER_STOCKPILE_CREDITS_PER_DEMAND_PER_SIZE = 1000;

	public static final float URBANIZED_POLITY_FOOD_PENALTY = 0.5f;
	public static final float URBANIZED_POLITY_DEMAND_MULT = 2f;
	
	public static final float VICE_DRUGS = 500;
	public static final float VICE_WEAPONS = 100;

	public static final float VOLATILES_DEPOT_STOCKPILE = 5000f;
	
	public static final float VOLATILES_MINING_VOLATILES = 5500f;
	public static final float VOLATILES_MINING_MACHINERY = 100;
	//public static final float VOLATILES_MINING_CREW = 5000;
	
	public static final float VOLTURNIAN_LOBSTER_PENS_LOBSTER = 5000f;
	
	
	public static final float FARMING_ORGANICS_FRACTION = 0.33f;

	public static final float WORLD_BARREN_MARGINAL_FARMING_MULT = 0.2f;
	public static final float WORLD_DESERT_FARMING_MULT = 0.2f;
	public static final float WORLD_ICE_FARMING_MULT = 0.2f;
	public static final float WORLD_TUNDRA_FARMING_MULT = 0.5f;
	public static final float WORLD_ARID_FARMING_MULT = 0.5f;
	public static final float WORLD_WATER_FARMING_MULT = 0.5f;
	public static final float WORLD_JUNGLE_FARMING_MULT = 0.5f;
	public static final float WORLD_TERRAN_FARMING_MULT = 2f;
	public static final float WORLD_TWILIGHT_FARMING_MULT = 1f;
	
//	public static final float WORLD_BARREN_MARGINAL_FARMING_MULT = 0.05f;
//	public static final float WORLD_DESERT_FARMING_MULT = 0.15f;
//	public static final float WORLD_ICE_FARMING_MULT = 0.002f;
//	public static final float WORLD_TUNDRA_FARMING_MULT = 0.05f;
//	public static final float WORLD_ARID_FARMING_MULT = 0.3f;
//	public static final float WORLD_WATER_FARMING_MULT = 0.2f;
//	public static final float WORLD_JUNGLE_FARMING_MULT = 0.15f;
//	public static final float WORLD_TERRAN_FARMING_MULT = 0.5f;
//	public static final float WORLD_TWILIGHT_FARMING_MULT = 0.35f;
	
	
	public static final float WORLD_BARREN_MARGINAL_MACHINERY_MULT = 0.00005f;
	
	public static final float WORLD_DESERT_MACHINERY_MULT = 0.00005f;
	
	public static final float WORLD_ICE_MACHINERY_MULT = 0.00001f;
	
	public static final float WORLD_TUNDRA_MACHINERY_MULT = 0.000025f;
	
	public static final float WORLD_ARID_MACHINERY_MULT = 0.00005f;
	
	public static final float WORLD_WATER_MACHINERY_MULT = 0.00005f;
	public static final float WORLD_WATER_MAX_FOOD = 50000;
	
	public static final float WORLD_JUNGLE_MACHINERY_MULT = 0.00005f;
	
	public static final float WORLD_TERRAN_MACHINERY_MULT = 0.0001f;
	
	public static final float WORLD_TWILIGHT_MACHINERY_MULT = 0.00075f;
	
	public static final float WORLD_UNINHABITABLE_ORGANICS_MULT = 0.0005f;
	
	
	public static final float POPULATION_GREEN_CREW = 50f;
	public static final float POPULATION_FOOD = 500f;
	public static final float POPULATION_LUXURY_GOODS = 100f;
	public static final float POPULATION_DOMESTIC_GOODS = 500f;
	public static final float POPULATION_ORGANS = 100f;
	public static final float POPULATION_DRUGS = 100f;
	public static final float POPULATION_WEAPONS = 50f;
	public static final float POPULATION_FUEL = 200f;
	public static final float POPULATION_SUPPLIES = 200f;
	//public static final float POPULATION_SUPPLIES_FOR_CREW_MARINES = 0.1f;
	
	
	public static final float STABILITY_CRYOSANCTUM = 1f;
	public static final float STABILITY_OUTPOST = 2f;
	public static final float STABILITY_REGIONAL_CAPITAL = 2f;
	public static final float STABILITY_STEALTH_MINEFIELDS = 1f;
	public static final float STABILITY_HEADQUARTERS = 2f;
	public static final float STABILITY_MILITARY_BASE = 2f;
	public static final float STABILITY_ORBITAL_STATION = 1f;
	public static final float STABILITY_SPACEPORT = 1f;
	public static final float STABILITY_ORGANIZED_CRIME = -1f;
	public static final float STABILITY_REFUGEE_POPULATION = -1f;
	public static final float STABILITY_DISSIDENT = -1f;
	public static final float STABILITY_DECIVILIZED = -10f;
	
	public static final float STABILITY_TRADE_CENTER = 2f;
	
	public static final float STABILITY_LUDDIC_MAJORITY_BONUS = 2f;
	public static final float STABILITY_LUDDIC_MAJORITY_PENALTY = -2f;
	
	
}

