package com.fs.starfarer.api.impl.campaign.procgen.themes;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;

public class SalvageEntityGeneratorOld {

//	protected Constellation con;
//	
//	public SalvageEntityGeneratorOld(Constellation con) {
//		this.con = con;
//	}


//	public void addSalvageableEntities() {
//		
//		Set<PlanetAPI> usedPlanets = new HashSet<PlanetAPI>();
//		for (StarSystemAPI system : con.getSystems()) {
//			SectorEntityToken center = system.getCenter();
//			if (center == null) continue;
//			SectorEntityToken probe = addSalvageEntity(system, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT);
//			float orbitRadius = 4000;
//			float orbitDays = orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f);
//			probe.setCircularOrbitWithSpin(center, StarSystemGenerator.random.nextFloat()* 360f, 
//										   4000, orbitDays, 10f, 20f);
//			
//			PlanetAPI planet = findInterestingPlanet(con, usedPlanets);
//			if (planet != null) {
//				DomainSurveyDerelictSpecialData special = new DomainSurveyDerelictSpecialData(SpecialType.PLANET_INTERESTING_PROPERTY);
//				special.type = SpecialType.PLANET_INTERESTING_PROPERTY;
//				special.type = SpecialType.PLANET_SURVEY_DATA;
//				special.entityId = planet.getId();
//				for (MarketConditionAPI mc : planet.getMarket().getConditions()) {
//					if (interestingConditions.contains(mc.getId())) {
//						special.secondaryId = mc.getId();
//					}
//				}
//				usedPlanets.add(planet);
//				probe.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPECIAL_DATA, special);
//			}
//			
//			
//			
//			SectorEntityToken ship = addSalvageEntity(system, Entities.DERELICT_SURVEY_SHIP, Factions.DERELICT);
//			orbitRadius = 5000;
//			orbitDays = orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f);
//			ship.setCircularOrbitWithSpin(center, StarSystemGenerator.random.nextFloat()* 360f, 
//										   4000, orbitDays, 10f, 20f);
//			ship.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPECIAL_DATA, new DomainSurveyDerelictSpecialData(SpecialType.SCRAMBLED));
//			
//			
////			DomainSurveyDerelictSpecialData special = new DomainSurveyDerelictSpecialData(SpecialType.PLANET_INTERESTING_PROPERTY);
////			special.type = SpecialType.LOCATION_SURVEY_SHIP;
////			special.entityId = ship.getId();
////			probe.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPECIAL_DATA, special);
//			
//			
//			SectorEntityToken mothership = addSalvageEntity(system, Entities.DERELICT_MOTHERSHIP, Factions.DERELICT);
//			orbitRadius = 6000;
//			orbitDays = orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f);
//			mothership.setCircularOrbitWithSpin(center, StarSystemGenerator.random.nextFloat()* 360f, 
//										   4000, orbitDays, 10f, 20f);
//			mothership.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPECIAL_DATA, new DomainSurveyDerelictSpecialData(SpecialType.SCRAMBLED));
//			
//			//SectorEntityToken debris = addSalvageEntity(system, Entities.DEBRIS_FIELD, Factions.DERELICT);
//			//SectorEntityToken debris = system.addCustomEntity(null, null, Entities.DEBRIS_FIELD, Factions.NEUTRAL);
////			DebrisFieldParams params = new DebrisFieldParams(100f, 50f, 1f, 5f, 2f);
////			SectorEntityToken debris = system.addTerrain(Terrain.DEBRIS_FIELD, params);
////			orbitRadius = 6500;
////			orbitDays = orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f);
////			debris.setCircularOrbitWithSpin(center, StarSystemGenerator.random.nextFloat()* 360f, 
////					4000, orbitDays, 10f, 20f);
//		}
//	}
	
	
//	protected SectorEntityToken addSalvageEntity(StarSystemAPI system, String id, String faction) {
//		SectorEntityToken entity = system.addCustomEntity(null, null, id, faction);
//		
//		SalvageEntityGenDataSpec spec = getSalvageSpec(id);
//		
//		switch (spec.getType()) {
//		case ALWAYS_VISIBLE:
//			entity.setSensorProfile(null);
//			entity.setDiscoverable(null);
//			break;
//		case DISCOVERABLE:
//			entity.setSensorProfile(1f);
//			entity.setDiscoverable(true);
//			break;
//		case NOT_DISCOVERABLE:
//			entity.setSensorProfile(1f);
//			entity.setDiscoverable(false);
//			break;
//		}
//		
//		long seed = StarSystemGenerator.random.nextLong();
//		entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
//		
//		//entity.getMemoryWithoutUpdate().set("$hasDefenders", true);
//		
//		entity.getDetectedRangeMod().modifyFlat("gen", spec.getDetectionRange());
//		
//		return entity;
//	}
	
	
	
//	public static SalvageEntityGenDataSpec getSalvageSpec(SectorEntityToken entity) {
//		String id = entity.getCustomEntityType();
//		if (entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPEC_ID)) {
//			id = entity.getMemoryWithoutUpdate().getString(MemFlags.SALVAGE_SPEC_ID);
//		}
//		return getSalvageSpec(id);
//	}
	
	public static SalvageEntityGenDataSpec getSalvageSpec(String id) {
		SalvageEntityGenDataSpec spec = (SalvageEntityGenDataSpec) Global.getSettings().getSpec(SalvageEntityGenDataSpec.class, id, false);
		return spec;
	}
	
	public static boolean hasSalvageSpec(String id) {
		SalvageEntityGenDataSpec spec = (SalvageEntityGenDataSpec) Global.getSettings().getSpec(SalvageEntityGenDataSpec.class, id, true);
		return spec != null;
	}
	
//	public static SalvageEntityGenDataSpec getSalvageSpec(String id, boolean nullOnNotFound) {
//		SalvageEntityGenDataSpec spec = (SalvageEntityGenDataSpec) Global.getSettings().getSpec(SalvageEntityGenDataSpec.class, id, true);
//		return spec;
//	}
	
	

//	public static Set<String> interestingConditions = new HashSet<String>();
//	
//	static {
//		interestingConditions.add(Conditions.VOLATILES_PLENTIFUL);
//		interestingConditions.add(Conditions.ORE_ULTRARICH);
//		interestingConditions.add(Conditions.RARE_ORE_RICH);
//		interestingConditions.add(Conditions.RARE_ORE_ULTRARICH);
//		interestingConditions.add(Conditions.FARMLAND_BOUNTIFUL);
//		interestingConditions.add(Conditions.FARMLAND_ADEQUATE);
//		interestingConditions.add(Conditions.ORGANICS_PLENTIFUL);
//		interestingConditions.add(Conditions.HABITABLE);
//	}
	
//	public static PlanetAPI findInterestingPlanet(Constellation c, Set<PlanetAPI> exclude) {
//		WeightedRandomPicker<PlanetAPI> planets = new WeightedRandomPicker<PlanetAPI>();
//		
//		for (StarSystemAPI system : c.getSystems()) {
//			for (PlanetAPI planet : system.getPlanets()) {
//				if (planet.isStar()) continue;
//				if (exclude.contains(planet)) continue;
//				if (planet.getMarket() == null || !planet.getMarket().isPlanetConditionMarketOnly()) continue;
//				//if (planet.getMarket().getSurveyLevel() == SurveyLevel.FULL) continue;
//				for (MarketConditionAPI mc : planet.getMarket().getConditions()) {
//					if (interestingConditions.contains(mc.getId())) {
//						if (mc.getId().equals(Conditions.HABITABLE) && planet.getMarket().getHazardValue() > 0.25f) {
//							continue;
//						}
//						planets.add(planet);
//						break;
//					}
//				}
//			}
//		}
//		return planets.pick();
//	}
}














