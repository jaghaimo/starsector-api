package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.PlanetaryShield;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.SpecialCreationContext;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;


public class MiscellaneousThemeGenerator extends BaseThemeGenerator {

	public static float PROB_TO_ADD_SOMETHING = 0.5f;
	
	
	public String getThemeId() {
		return Themes.MISC;
	}

	@Override
	public float getWeight() {
		return 0f;
	}

	@Override
	public int getOrder() {
		return 1000000;
	}

	@Override
	public void generateForSector(ThemeGenContext context, float allowedUnusedFraction) {
		
		if (DEBUG) System.out.println("\n\n\n");
		if (DEBUG) System.out.println("Generating misc derelicts etc in all systems");
		
		List<StarSystemData> all = new ArrayList<StarSystemData>();
		for (Constellation c : context.constellations) {
			String theme = context.majorThemes.get(c);
			
			List<StarSystemData> systems = new ArrayList<StarSystemData>();
			for (StarSystemAPI system : c.getSystems()) {
				StarSystemData data = computeSystemData(system);
				systems.add(data);
			}
			
			for (StarSystemData data  : systems) {
				boolean derelict = data.system.hasTag(Tags.THEME_DERELICT);
				if (!derelict && theme != null) continue;
				
				if (random.nextFloat() > PROB_TO_ADD_SOMETHING) {
					data.system.addTag(Tags.THEME_MISC_SKIP);
					continue;
				}

				populateNonMain(data);
				all.add(data);
				data.system.addTag(Tags.THEME_MISC);
			}
		}
		
		
		SpecialCreationContext specialContext = new SpecialCreationContext();
		specialContext.themeId = getThemeId();
		SalvageSpecialAssigner.assignSpecials(all, specialContext);
		
		if (DEBUG) System.out.println("Finished generating misc derelicts\n\n\n\n\n");
		
		
		String legionVariant = "legion_xiv_Elite";
		if (Global.getSettings().getVariant(legionVariant) != null) {
			if (DEBUG) System.out.println("Adding XIV Legion to remnant systems");
			
			int numSalvageable = 2 + random.nextInt(3);
			int numNonSalvageable = 3 + random.nextInt(3);
			
			List<Constellation> list = new ArrayList<Constellation>(context.constellations);
			Collections.shuffle(list, random);
			for (Constellation c : list) {
				List<StarSystemData> systems = new ArrayList<StarSystemData>();
				for (StarSystemAPI system : c.getSystems()) {
					StarSystemData data = computeSystemData(system);
					systems.add(data);
				}
				
				Collections.shuffle(systems, random);
				for (StarSystemData data  : systems) {
					if (!data.system.hasTag(Tags.THEME_REMNANT)) continue;
	
					EntityLocation loc = pickAnyLocation(random, data.system, 70f, null);
					AddedEntity ae = addDerelictShip(data, loc, legionVariant);
					if (ae != null) {
						if (numSalvageable > 0) {
							numSalvageable--;
							ShipRecoverySpecialCreator creator = new ShipRecoverySpecialCreator(random, 0, 0, false, null, null);
							Object specialData = creator.createSpecial(ae.entity, new SpecialCreationContext());
							if (specialData != null) {
								Misc.setSalvageSpecial(ae.entity, specialData);
							}
						} else {
							numNonSalvageable--;
							SalvageSpecialAssigner.assignSpecials(ae.entity);
						}
					}
					if (numSalvageable + numNonSalvageable <= 0) break;
				}
				if (numSalvageable + numNonSalvageable <= 0) break;
			}
			
			if (DEBUG) System.out.println("Finished adding XIV Legion to remnant systems\n\n\n\n\n");
		}
		
		if (DEBUG) System.out.println("Adding Planetary Shield planet");
		
		
//		List<Constellation> list = new ArrayList<Constellation>(context.constellations);
//		WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);
//		for (Constellation c : list) {
//			List<StarSystemData> systems = new ArrayList<StarSystemData>();
//			for (StarSystemAPI system : c.getSystems()) {
//				StarSystemData data = computeSystemData(system);
//				systems.add(data);
//			}
//			
//			for (StarSystemData data  : systems) {
//				if (!data.system.hasTag(Tags.THEME_REMNANT_MAIN)) continue;
//
//				for (PlanetAPI planet : data.system.getPlanets()) {
//					if (planet.isStar()) continue;
//					float weight = 1f;
//					if (planet.getMarket().hasCondition(Conditions.HABITABLE)) {
//						weight = 1000f;
//					}
//					picker.add(planet, weight);
//				}
//			}
//		}
//		PlanetAPI planet = picker.pick();
		
		if (DEBUG) System.out.println("Generating planetary shield planet");
		
		PlanetAPI bestHab = null;
		PlanetAPI bestNonHab = null;
//		OrbitGap gapHab = null;
//		OrbitGap gapNonHab = null;
		float habDist = 0;
		float nonHabDist = 0;

		// looking for a habitable planet furthest from the Sector's center, with a bit of 
		// a random factor
		for (Constellation c : context.constellations) {
			for (StarSystemAPI system : c.getSystems()) {
				if (!system.hasTag(Tags.THEME_MISC_SKIP) && 
						!system.hasTag(Tags.THEME_MISC)) {
					continue;
				}
				//[theme_derelict, theme_derelict_probes, theme_misc_skip, theme_derelict_survey_ship]
				if (system.hasTag(Tags.THEME_DERELICT)) {
					continue;
				}
				
				for (PlanetAPI curr : system.getPlanets()) {
					if (curr.isStar()) continue;
					if (curr.isMoon()) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					
					float dist = system.getLocation().length() + random.nextFloat() * 6000;
					if (curr.getMarket().hasCondition(Conditions.HABITABLE)) {
						if (dist > habDist) {
//							List<OrbitGap> gaps = findGaps(curr, 50f, 500f, 100f);
//							if (!gaps.isEmpty()) {
								habDist = dist;
								bestHab = curr;
//								gapHab = gaps.get(0);
//							}
						}
					} else {
						if (dist > nonHabDist) {
//							List<OrbitGap> gaps = findGaps(curr, 50f, 500f, 100f);
//							if (!gaps.isEmpty()) {
								nonHabDist = dist;
								bestNonHab = curr;
//								gapNonHab = gaps.get(0);
//							}
						}
					}
				}
			}
		}
		
		PlanetAPI planet = bestHab;
		//OrbitGap gap = gapHab;
		if (planet == null) {
			planet = bestNonHab;
			//gap = gapNonHab;
		}

		if (planet != null) {
			if (DEBUG) System.out.println("Adding Planetary Shield to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
			PlanetaryShield.applyVisuals(planet);
			Global.getSector().getMemoryWithoutUpdate().set(PLANETARY_SHIELD_PLANET_KEY, planet);
			planet.getMemoryWithoutUpdate().set(PLANETARY_SHIELD_PLANET, true);
			
			long seed = StarSystemGenerator.random.nextLong();
			planet.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
			planet.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPEC_ID_OVERRIDE, "red_planet");
			//planet.addTag(Tags.SALVAGEABLE);
			
//			SectorEntityToken beacon = Misc.addWarningBeacon(planet, gap, Tags.BEACON_HIGH);
//			beacon.getMemoryWithoutUpdate().set(PLANETARY_SHIELD_BEACON, true);
//			beacon.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
			
		} else {
			if (DEBUG) System.out.println("Failed to find a planet in remnant systems");
		}
		if (DEBUG) System.out.println("Finished adding Planetary Shield planet\n\n\n\n\n");
		
	}
	
	public static String PLANETARY_SHIELD_PLANET_KEY = "$core_planetaryShieldPlanet";
	public static String PLANETARY_SHIELD_PLANET = "$psi_planet";
	

	
	public void populateNonMain(StarSystemData data) {
		if (DEBUG) System.out.println(" Generating misc derelicts in system " + data.system.getName());
		boolean special = data.isBlackHole() || data.isNebula() || data.isPulsar();
		if (special) {
			addResearchStations(data, 0.25f, 1, 1, createStringPicker(Entities.STATION_RESEARCH, 10f));
		}
		
		if (random.nextFloat() < 0.5f) return;
		
		WeightedRandomPicker<String> factions = SalvageSpecialAssigner.getNearbyFactions(random, data.system.getCenter(),
														15f, 10f, 10f);
		
		addShipGraveyard(data, 0.05f, 1, 1, factions);
		
		addDebrisFields(data, 0.25f, 1, 2);

		addDerelictShips(data, 0.5f, 0, 3, factions);
		
		addCaches(data, 0.25f, 0, 2, createStringPicker( 
				Entities.WEAPONS_CACHE, 4f,
				Entities.WEAPONS_CACHE_SMALL, 10f,
				Entities.WEAPONS_CACHE_HIGH, 4f,
				Entities.WEAPONS_CACHE_SMALL_HIGH, 10f,
				Entities.WEAPONS_CACHE_LOW, 4f,
				Entities.WEAPONS_CACHE_SMALL_LOW, 10f,
				Entities.SUPPLY_CACHE, 4f,
				Entities.SUPPLY_CACHE_SMALL, 10f,
				Entities.EQUIPMENT_CACHE, 4f,
				Entities.EQUIPMENT_CACHE_SMALL, 10f
				));
		
	}
	
	
	
}
















