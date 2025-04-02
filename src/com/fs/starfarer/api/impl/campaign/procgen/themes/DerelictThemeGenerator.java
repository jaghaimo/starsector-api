package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTPoints;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.DomainSurveyDerelictSpecial.DomainSurveyDerelictSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.DomainSurveyDerelictSpecial.SpecialType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SurveyDataSpecial.SurveyDataSpecialData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SurveyDataSpecial.SurveyDataSpecialType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.TopographicDataSpecial.TopographicDataSpecialData;
import com.fs.starfarer.api.plugins.SurveyPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;


public class DerelictThemeGenerator extends BaseThemeGenerator {

	public static final float BASE_LINK_FRACTION = 0.25f;
	public static final float SALVAGE_SPECIAL_FRACTION = 0.5f;
	public static final float TOPOGRAPHIC_DATA_FRACTION = 0.2f;
	
	public static final int BRANCHES_PER_MOTHERSHIP_MIN = 3;
	public static final int BRANCHES_PER_MOTHERSHIP_MAX = 4;
	
	public static final int BRANCHES_PER_SHIP_MIN = 2;
	public static final int BRANCHES_PER_SHIP_MAX = 3;
	
	
	public static class SystemGenData {
		public int numMotherships;
		public int numSurveyShips;
		public int numProbes;
	}
	
	
	public String getThemeId() {
		return Themes.DERELICTS;
	}

	@Override
	public void generateForSector(ThemeGenContext context, float allowedUnusedFraction) {
		
		float total = (float) (context.constellations.size() - context.majorThemes.size()) * allowedUnusedFraction;
		if (total <= 0) return;
		
		float avg1 = (BRANCHES_PER_MOTHERSHIP_MIN + BRANCHES_PER_MOTHERSHIP_MAX) / 2f;
		float avg2 = (BRANCHES_PER_SHIP_MIN + BRANCHES_PER_SHIP_MAX) / 2f;
		float perChain = 1 + avg1 + (avg1 * avg2);
		
		float num = total / perChain;
		if (num < 1) num = 1;
		if (num > 3) num = 3;
		
		if (num > 1 && num < 2) {
			num = 2;
		} else {
			num = Math.round(num);
		}
		
		List<AddedEntity> mothershipsSoFar = new ArrayList<AddedEntity>();
		
		for (int i = 0; i < num; i++) {
			addMothershipChain(context, mothershipsSoFar);
		}
		
		
		
		WeightedRandomPicker<StarSystemAPI> cryoSystems = new WeightedRandomPicker<StarSystemAPI>(StarSystemGenerator.random);
		WeightedRandomPicker<StarSystemAPI> backup = new WeightedRandomPicker<StarSystemAPI>(StarSystemGenerator.random);
		OUTER: for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float w = 0f;
			if (system.hasTag(Tags.THEME_DERELICT_PROBES)) {
				w = 10f;
			} else if (system.hasTag(Tags.THEME_DERELICT_SURVEY_SHIP)) {
				w = 10f;
			} else if (system.hasTag(Tags.THEME_DERELICT_MOTHERSHIP)) {
				w = 10f;
			} else if (system.hasTag(Tags.THEME_DERELICT)) {
				w = 10f;
			} else {
				continue;
			}
			
			int numPlanets = 0;
			boolean hasHab = false;
			for (PlanetAPI planet : system.getPlanets()) {
				if (planet.isStar()) continue;
				if (planet.getSpec().isPulsar()) continue OUTER;
				hasHab |= planet.getMarket() != null && planet.getMarket().hasCondition(Conditions.HABITABLE);
				numPlanets++;
			}

			WeightedRandomPicker<StarSystemAPI> use = cryoSystems;
			if (!hasHab || numPlanets < 3) {
				use = backup;
			}
			
			if (hasHab) w += 5;
			w += numPlanets;
			
			if (use == backup) {
				w *= 0.0001f;
			}
			use.add(system, w);
		}
		
		int numCryo = 2;
		if (cryoSystems.isEmpty() || cryoSystems.getItems().size() < numCryo + 1) {
			cryoSystems.addAll(backup);
		}
		
		int added = 0;
		WeightedRandomPicker<String> cryosleeperNames = new WeightedRandomPicker<String>(random);
		cryosleeperNames.add("Calypso");
		cryosleeperNames.add("Tantalus");
		while (added < numCryo && !cryoSystems.isEmpty()) {
			StarSystemAPI pick = cryoSystems.pickAndRemove();
			String name = cryosleeperNames.pickAndRemove();
			AddedEntity cryo = addCryosleeper(pick, name);
			if (cryo != null) {
				added++;
			}
		}
	}
	
	protected void addMothershipChain(ThemeGenContext context, List<AddedEntity> mothershipsSoFar) {
		
		List<AddedEntity> all = new ArrayList<AddedEntity>(); 
		
		
		Vector2f center = new Vector2f();
		for (AddedEntity e : mothershipsSoFar) {
			Vector2f.add(center, e.entity.getLocationInHyperspace(), center);
		}
		center.scale(1f / (float)(mothershipsSoFar.size() + 1f));
		
		List<Constellation> constellations = getSortedAvailableConstellations(context, false, center, null);
		WeightedRandomPicker<Constellation> picker = new WeightedRandomPicker<Constellation>(StarSystemGenerator.random);
		for (int i = 0; i < constellations.size() / 3; i++) {
			picker.add(constellations.get(i));
		}
		
		Constellation main = picker.pick();
		if (main == null) return;
		
		if (DEBUG) {
			System.out.println("Picked for mothership chain start: [" + main.getNameWithType() + "] (" + (int)main.getLocation().x + ", " + (int)main.getLocation().y + ")");
		}
		
		constellations.remove(main);
		context.majorThemes.put(main, Themes.DERELICTS);

		
		StarSystemAPI mainSystem = main.getSystemWithMostPlanets();
		if (mainSystem == null) return;
		
		//mainSystem.addTag(Tags.THEME_DERELICT);
		for (StarSystemAPI system : main.getSystems()) {
			system.addTag(Tags.THEME_DERELICT);
		}
		
//		if (mainSystem.getName().toLowerCase().contains("valac")) {
//			System.out.println("HERE13123123123");
//		}
		
		AddedEntity mothership = addMothership(mainSystem);
		if (mothership == null) return;
		
		all.add(mothership);
		
		if (DEBUG) {
			System.out.println("  Added mothership to [" + mainSystem.getNameWithLowercaseType() + "]");
		}
		
		//if (true) return;
		
		// probes in mothership system
		//int probesNearMothership = (int) Math.round(StarSystemGenerator.getRandom(2, 4));
		int probesNearMothership = getNumProbesForSystem(mothership.entity.getContainingLocation());
		List<AddedEntity> added = addToSystem(mainSystem, Entities.DERELICT_SURVEY_PROBE, probesNearMothership);
		all.addAll(added);
		
		
		linkFractionToParent(mothership, added, 
				 			 BASE_LINK_FRACTION,
				 			 SpecialType.LOCATION_MOTHERSHIP);
		
		// survey ships in mothership constellation
		int surveyShipsNearMothership = (int) Math.round(StarSystemGenerator.getRandom(0, 3));
		if (surveyShipsNearMothership > main.getSystems().size()) surveyShipsNearMothership = main.getSystems().size();
		if (DEBUG) {
			System.out.println(String.format("Adding %d survey ships near mothership", surveyShipsNearMothership));
		}
		List<AddedEntity> addedShips = addToConstellation(main, Entities.DERELICT_SURVEY_SHIP, surveyShipsNearMothership, false);
		all.addAll(addedShips);
		
		// probes in each system with survey ship
		for (AddedEntity e : addedShips) {
			int probesNearSurveyShip = (int) Math.round(StarSystemGenerator.getRandom(1, 3));
			//int probesNearSurveyShip = getNumProbesForSystem(e.entity.getContainingLocation());
			added = addProbes((StarSystemAPI) e.entity.getContainingLocation(), probesNearSurveyShip);
			all.addAll(added);
			
			linkFractionToParent(e, added, 
					 BASE_LINK_FRACTION,
					 SpecialType.LOCATION_SURVEY_SHIP);
		}
		
		linkFractionToParent(mothership, addedShips, 
							 BASE_LINK_FRACTION,
							 SpecialType.LOCATION_MOTHERSHIP);
		
		//if (true) return;
		
		constellations = getSortedAvailableConstellations(context, false, mothership.entity.getLocationInHyperspace(), null);
		picker = new WeightedRandomPicker<Constellation>(StarSystemGenerator.random);
		for (int i = constellations.size() - 7; i < constellations.size(); i++) {
			if (i < 0) continue;
			picker.add(constellations.get(i));
		}

		int numSurveyShipsInNearConstellations = (int) Math.round(StarSystemGenerator.getRandom(BRANCHES_PER_MOTHERSHIP_MIN, BRANCHES_PER_MOTHERSHIP_MAX));
		if (DEBUG) {
			System.out.println(String.format("Adding up to %d survey ships", numSurveyShipsInNearConstellations));
		}
		List<Constellation> constellationsForSurveyShips = new ArrayList<Constellation>();
		for (int i = 0; i < numSurveyShipsInNearConstellations && !picker.isEmpty(); i++) {
			constellationsForSurveyShips.add(picker.pickAndRemove());
		}
		List<AddedEntity> outerShips = new ArrayList<AddedEntity>();
		
		
		for (Constellation c : constellationsForSurveyShips) {
			context.majorThemes.put(c, Themes.DERELICTS);
			
			if (DEBUG) {
				System.out.println("  Picked for survey ship: [" + c.getNameWithType() + "]");
			}
			
			addedShips = addToConstellation(c, Entities.DERELICT_SURVEY_SHIP, 1, true);
			if (addedShips.isEmpty()) continue;
			
			all.addAll(addedShips);
			
			AddedEntity ship = addedShips.get(0);
			outerShips.addAll(addedShips);
			
			//int probesNearSurveyShip = (int) Math.round(StarSystemGenerator.getRandom(1, 3));
			int probesNearSurveyShip = getNumProbesForSystem(ship.entity.getContainingLocation());
			added = addProbes((StarSystemAPI) ship.entity.getContainingLocation(), probesNearSurveyShip);
			all.addAll(added);
			
			linkFractionToParent(ship, added, 
		 			 			 BASE_LINK_FRACTION,
		 			 			 SpecialType.LOCATION_SURVEY_SHIP);
			
			int probesInSameConstellation = (int) Math.round(StarSystemGenerator.getRandom(2, 5));
			int max = c.getSystems().size() + 2;
			if (probesInSameConstellation > max) probesInSameConstellation = max;
			
			added = addToConstellation(c, Entities.DERELICT_SURVEY_PROBE, probesInSameConstellation, false);
			all.addAll(added);
			
			linkFractionToParent(ship, added, 
								 BASE_LINK_FRACTION,
								 SpecialType.LOCATION_SURVEY_SHIP);
			
			
			
			List<Constellation> c2 = getSortedAvailableConstellations(context, false, c.getLocation(), constellationsForSurveyShips);
			WeightedRandomPicker<Constellation> p2 = new WeightedRandomPicker<Constellation>(StarSystemGenerator.random);
			//for (int i = 0; i < constellations.size() / 3 && i < 7; i++) {
			for (int i = c2.size() - 3; i < c2.size(); i++) {
				if (i < 0) continue;
				p2.add(constellations.get(i));
			}
			
			int probeSystemsNearShip = (int) Math.round(StarSystemGenerator.getRandom(BRANCHES_PER_SHIP_MIN, BRANCHES_PER_SHIP_MAX));
			int k = 0;
			if (DEBUG) {
				System.out.println(String.format("Adding probes to %d constellations near survey ship", probeSystemsNearShip));
			}
			List<AddedEntity> probes3 = new ArrayList<AddedEntity>();
			while (k < probeSystemsNearShip && !p2.isEmpty()) {
				Constellation pick = p2.pickAndRemove();
				k++;
				context.majorThemes.put(pick, Themes.NO_THEME);
				int probesInConstellation = (int) Math.round(StarSystemGenerator.getRandom(1, 3));
				probes3.addAll(addToConstellation(pick, Entities.DERELICT_SURVEY_PROBE, probesInConstellation, false));
			}
			
			all.addAll(probes3);
			linkFractionToParent(ship, probes3, 
		 			 BASE_LINK_FRACTION,
		 			 SpecialType.LOCATION_MOTHERSHIP);
		}
		
		linkFractionToParent(mothership, outerShips, 
				 			 BASE_LINK_FRACTION,
				 			 SpecialType.LOCATION_MOTHERSHIP);
		

		
		assignRandomSpecials(all);
	}
	
	public static Set<String> interestingConditions = new HashSet<String>();
	public static Set<String> interestingConditionsWithoutHabitable = new HashSet<String>();
	public static Set<String> interestingConditionsWithRuins = new HashSet<String>();
	static {
		//interestingConditions.add(Conditions.VOLATILES_ABUNDANT);
		interestingConditions.add(Conditions.VOLATILES_PLENTIFUL);
		//interestingConditions.add(Conditions.ORE_RICH);
		interestingConditions.add(Conditions.RARE_ORE_RICH);
		interestingConditions.add(Conditions.RARE_ORE_ULTRARICH);
		interestingConditions.add(Conditions.ORE_ULTRARICH);
		interestingConditions.add(Conditions.FARMLAND_BOUNTIFUL);
		interestingConditions.add(Conditions.FARMLAND_ADEQUATE);
		//interestingConditions.add(Conditions.ORGANICS_ABUNDANT);
		interestingConditions.add(Conditions.ORGANICS_PLENTIFUL);
		interestingConditions.add(Conditions.HABITABLE);
		
		interestingConditionsWithoutHabitable.addAll(interestingConditions);
		interestingConditionsWithoutHabitable.remove(Conditions.HABITABLE);
		
		interestingConditionsWithRuins.addAll(interestingConditions);
		interestingConditionsWithRuins.add(Conditions.RUINS_VAST);
		interestingConditionsWithRuins.add(Conditions.RUINS_EXTENSIVE);
	}

	
	
	protected void assignRandomSpecials(List<AddedEntity> entities) {
		Set<PlanetAPI> usedPlanets = new HashSet<PlanetAPI>();
		Set<StarSystemAPI> usedSystems = new HashSet<StarSystemAPI>();
		
		for (AddedEntity e : entities) {
			if (hasSpecial(e.entity)) continue;
			
			SurveyDataSpecialType type = null;
			
			if (StarSystemGenerator.random.nextFloat() < TOPOGRAPHIC_DATA_FRACTION) {
				int min = 0;
				int max = 0;
				if (Entities.DERELICT_SURVEY_PROBE.equals(e.entityType)) {
					min = HTPoints.LOW_MIN;
					max = HTPoints.LOW_MAX;
				} else if (Entities.DERELICT_SURVEY_SHIP.equals(e.entityType)) {
					min = HTPoints.MEDIUM_MIN;
					max = HTPoints.MEDIUM_MAX;
				} else if (Entities.DERELICT_MOTHERSHIP.equals(e.entityType)) {
					min = HTPoints.HIGH_MIN;
					max = HTPoints.HIGH_MAX;
				}
				int points = min + StarSystemGenerator.random.nextInt(max - min + 1);
				if (points > 0) {
					TopographicDataSpecialData data = new TopographicDataSpecialData(points);
					Misc.setSalvageSpecial(e.entity, data);
					continue;
				}
			}
			if (StarSystemGenerator.random.nextFloat() < SALVAGE_SPECIAL_FRACTION) {
				float pNothing = 0.1f;
				if (Entities.DERELICT_SURVEY_PROBE.equals(e.entityType)) {
					pNothing = 0.5f;
				} else if (Entities.DERELICT_SURVEY_SHIP.equals(e.entityType)) {
					pNothing = 0.25f;
				} else if (Entities.DERELICT_MOTHERSHIP.equals(e.entityType)) {
					pNothing = 0f;
				}
				
				float r = StarSystemGenerator.random.nextFloat();
				if (r >= pNothing) {
					type = SurveyDataSpecialType.PLANET_SURVEY_DATA;
				}
			}
			
			//type = SpecialType.PLANET_SURVEY_DATA;
				
			if (type == SurveyDataSpecialType.PLANET_SURVEY_DATA) {
				PlanetAPI planet = findInterestingPlanet(e.entity.getConstellation().getSystems(), usedPlanets);
				if (planet != null) {
					SurveyDataSpecialData data = new SurveyDataSpecialData(SurveyDataSpecialType.PLANET_SURVEY_DATA);
					data.entityId = planet.getId();
					data.includeRuins = false;
					Misc.setSalvageSpecial(e.entity, data);
					usedPlanets.add(planet);
					
//					DomainSurveyDerelictSpecialData special = new DomainSurveyDerelictSpecialData(type);
//					special.entityId = planet.getId();
//					usedPlanets.add(planet);
//					e.entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPECIAL_DATA, special);
				}
			}
		}
	}
	
	
	public static StarSystemAPI findNearbySystem(SectorEntityToken from, Set<StarSystemAPI> exclude) {
		return findNearbySystem(from, exclude, null, 10000f);
	}
	
	public static StarSystemAPI findNearbySystem(SectorEntityToken from, Set<StarSystemAPI> exclude, Random random, float maxRange) {
		if (random == null) random = StarSystemGenerator.random;
		
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);
		
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (exclude != null && exclude.contains(system)) continue;
			
			float dist = Misc.getDistance(from.getLocationInHyperspace(), system.getLocation());
			if (dist > maxRange) continue;
			if (systemIsEmpty(system)) continue;
			
			picker.add(system);
		}
		
		return picker.pick();
	}
	
	
	public static String getInterestingCondition(PlanetAPI planet, boolean includeRuins) {
		if (planet == null) return null;
		
		Set<String> conditions = interestingConditions;
		if (includeRuins) conditions = interestingConditionsWithRuins;
		
		for (MarketConditionAPI mc : planet.getMarket().getConditions()) {
			if (conditions.contains(mc.getId())) {
				return mc.getId();
			}
		}
		return null;
	}
	
	public static PlanetAPI findInterestingPlanet(List<StarSystemAPI> systems, Set<PlanetAPI> exclude) {
		return findInterestingPlanet(systems, exclude, true, false, null);
	}
	public static PlanetAPI findInterestingPlanet(List<StarSystemAPI> systems, Set<PlanetAPI> exclude, boolean includeKnown, boolean includeRuins, Random random) {
		if (random == null) random = StarSystemGenerator.random;
		
		WeightedRandomPicker<PlanetAPI> planets = new WeightedRandomPicker<PlanetAPI>(random);

		Set<String> conditions = interestingConditions;
		if (includeRuins) conditions = interestingConditionsWithRuins;
		
		SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getNewPluginInstance("surveyPlugin");
		
		for (StarSystemAPI system : systems) {
			if (system.hasTag(Tags.THEME_HIDDEN)) continue;
			
			for (PlanetAPI planet : system.getPlanets()) {
				if (planet.isStar()) continue;
				if (exclude != null && exclude.contains(planet)) continue;
				if (planet.getMarket() == null || !planet.getMarket().isPlanetConditionMarketOnly()) continue;
				if (!includeKnown && planet.getMarket() != null && planet.getMarket().getSurveyLevel() == SurveyLevel.FULL) {
					continue;
				}
				//if (planet.getMarket().getSurveyLevel() == SurveyLevel.FULL) continue;
				
				String type = plugin.getSurveyDataType(planet);
				boolean classIV = Commodities.SURVEY_DATA_4.equals(type);
				boolean classV = Commodities.SURVEY_DATA_5.equals(type);
				
				if (!(classIV || classV || planet.getMarket().getHazardValue() <= 1f)) continue;
				
				float w = 1f;
				for (MarketConditionAPI mc : planet.getMarket().getConditions()) {
					if (conditions.contains(mc.getId())) {
						w += 1f;
					}
				}
				if (classIV) w *= 0.5f;
				if (classV) w *= 4f; 
				planets.add(planet, w);
			}
		}
		return planets.pick();
	}
	
	
	protected int getNumProbesForSystem(LocationAPI system) {
		int base = 1;
		int planets = system.getPlanets().size();

		if (planets <= 3) {
		} else if (planets <= 5) {
			base += 1;
		} else if (planets <= 8) {
			base += 2;
		} else {
			base += 3;
		}
		
		base += StarSystemGenerator.random.nextInt(2);
		
		return base;
	}
	protected void linkFractionToParent(AddedEntity parent, List<AddedEntity> children, float p, SpecialType type) {
		
		WeightedRandomPicker<AddedEntity> picker = new WeightedRandomPicker<AddedEntity>(StarSystemGenerator.random);
		for (AddedEntity c : children) {
			if (!hasSpecial(c.entity)) {
				picker.add(c);
			}
		}
		
		int extraLinks = (int) Math.max(1, Math.round(children.size() * p * (1f + StarSystemGenerator.random.nextFloat() * 0.5f)));
		for (int i = 0; i < extraLinks && !picker.isEmpty(); i++) {
			AddedEntity e = picker.pickAndRemove();
			linkToParent(e.entity, parent.entity, type);
		}
	}
	
//	protected AddedEntity getClosest(AddedEntity from, List<AddedEntity> choices) {
//		float min = Float.MAX_VALUE;
//		AddedEntity result = null;
//		for (AddedEntity e : choices) {
//			
//		}
//		
//		return result;
//	}
	
	protected void linkToParent(SectorEntityToken from, SectorEntityToken parent, SpecialType type) {
		if (hasSpecial(from)) return;
		
		DomainSurveyDerelictSpecialData special = new DomainSurveyDerelictSpecialData(type);
		special.entityId = parent.getId();
		from.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPECIAL_DATA, special);
	}
	
	protected void linkToMothership(SectorEntityToken from, SectorEntityToken mothership) {
		if (hasSpecial(from)) return;
		
		DomainSurveyDerelictSpecialData special = new DomainSurveyDerelictSpecialData(SpecialType.LOCATION_MOTHERSHIP);
		special.entityId = mothership.getId();
		from.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPECIAL_DATA, special);
	}
	
	public static boolean hasSpecial(SectorEntityToken entity) {
		return entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPECIAL_DATA);
	}
	
	protected List<AddedEntity> addToConstellation(Constellation c, String type, int num, boolean biggestFirst) {
		List<AddedEntity> result = new ArrayList<AddedEntity>();
		
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(StarSystemGenerator.random);
		picker.addAll(c.getSystems());
		
		boolean first = true;
		for (int i = 0; i < num; i++) {
			StarSystemAPI system = picker.pick();
			if (biggestFirst && first) system = c.getSystemWithMostPlanets();
			first = false;
			
			if (system == null) continue;
			
			result.addAll(addToSystem(system, type, 1));
		}
		
		return result;
	}
	
	protected List<AddedEntity> addToSystem(StarSystemAPI system, String type, int num) {
		List<AddedEntity> result = new ArrayList<AddedEntity>();
		if (system == null) return result;
		
		for (int i = 0; i < num; i++) {
			AddedEntity e = null;
			if (Entities.DERELICT_MOTHERSHIP.equals(type)) {
				e = addMothership(system);
			} else if (Entities.DERELICT_SURVEY_SHIP.equals(type)) {
				e = addSurveyShip(system);
			} else if (Entities.DERELICT_SURVEY_PROBE.equals(type)) {
				result.addAll(addProbes(system, 1));
			}
			if (e != null) {
				result.add(e);
			}
		}
		return result;
	}
	
	
	
	protected AddedEntity addMothership(StarSystemAPI system) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.PLANET_ORBIT, 10f);
		weights.put(LocationType.JUMP_ORBIT, 1f);
		weights.put(LocationType.NEAR_STAR, 1f);
		weights.put(LocationType.OUTER_SYSTEM, 5f);
		weights.put(LocationType.IN_ASTEROID_BELT, 10f);
		weights.put(LocationType.IN_RING, 10f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 10f);
		weights.put(LocationType.STAR_ORBIT, 1f);
		weights.put(LocationType.IN_SMALL_NEBULA, 1f);
		weights.put(LocationType.L_POINT, 1f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, system, 100f, weights);
		
//		if (system.getName().toLowerCase().contains("valac")) {
//			for (int i = 0; i < 10; i++) {
//				//Random random = new Random(32895278947689263L);
//				StarSystemGenerator.random = new Random(32895278947689263L);
//				random = StarSystemGenerator.random;
//				locs = getLocations(random, system, 100f, weights);
//				EntityLocation loc = locs.pickAndRemove();
//				System.out.println("Location: " + loc.toString());
//			}
//		}
		
		AddedEntity entity = addEntity(random, system, locs, Entities.DERELICT_MOTHERSHIP, Factions.DERELICT);
		if (entity != null) {
			system.addTag(Tags.THEME_INTERESTING);
			system.addTag(Tags.THEME_DERELICT);
			system.addTag(Tags.THEME_DERELICT_MOTHERSHIP);
		}
		
		if (DEBUG) {
			if (entity != null) {
				System.out.println(String.format("  Added mothership to %s", system.getNameWithLowercaseType()));
			} else {
				System.out.println(String.format("  Failed to add mothership to %s", system.getNameWithLowercaseType()));
			}
		}
		return entity;
	}
	
	
	protected AddedEntity addCryosleeper(StarSystemAPI system, String name) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.PLANET_ORBIT, 10f);
		weights.put(LocationType.JUMP_ORBIT, 1f);
		weights.put(LocationType.NEAR_STAR, 1f);
		weights.put(LocationType.OUTER_SYSTEM, 5f);
		weights.put(LocationType.IN_ASTEROID_BELT, 5f);
		weights.put(LocationType.IN_RING, 5f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 5f);
		weights.put(LocationType.STAR_ORBIT, 5f);
		weights.put(LocationType.IN_SMALL_NEBULA, 5f);
		weights.put(LocationType.L_POINT, 10f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, system, 100f, weights);
		
		
		AddedEntity entity = addEntity(random, system, locs, Entities.DERELICT_CRYOSLEEPER, Factions.DERELICT);
		if (entity != null) {
			system.addTag(Tags.THEME_INTERESTING);
			system.addTag(Tags.THEME_DERELICT);
			system.addTag(Tags.THEME_DERELICT_CRYOSLEEPER);
			
			if (name != null) {
				entity.entity.setName(entity.entity.getName() + " \"" + name + "\"");
				//entity.entity.setName("Cryosleeper" + "\"" + name + "\"");
			}
		}
		
		if (DEBUG) {
			if (entity != null) {
				System.out.println(String.format("  Added cryosleeper to %s", system.getNameWithLowercaseType()));
			} else {
				System.out.println(String.format("  Failed to add cryosleeper to %s", system.getNameWithLowercaseType()));
			}
		}
		return entity;
	}
	
	protected AddedEntity addSurveyShip(StarSystemAPI system) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.PLANET_ORBIT, 10f);
		weights.put(LocationType.JUMP_ORBIT, 1f);
		weights.put(LocationType.NEAR_STAR, 1f);
		weights.put(LocationType.OUTER_SYSTEM, 5f);
		weights.put(LocationType.IN_ASTEROID_BELT, 10f);
		weights.put(LocationType.IN_RING, 10f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 10f);
		weights.put(LocationType.STAR_ORBIT, 1f);
		weights.put(LocationType.IN_SMALL_NEBULA, 1f);
		weights.put(LocationType.L_POINT, 1f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, system, 100f, weights);
		
		AddedEntity entity = addEntity(random, system, locs, Entities.DERELICT_SURVEY_SHIP, Factions.DERELICT);
		
		if (entity != null) {
			system.addTag(Tags.THEME_INTERESTING);
			system.addTag(Tags.THEME_DERELICT);
			system.addTag(Tags.THEME_DERELICT_SURVEY_SHIP);
		}
		
		if (DEBUG) {
			if (entity != null) {
				System.out.println(String.format("  Added survey ship to %s", system.getNameWithLowercaseType()));
			} else {
				System.out.println(String.format("  Failed to add survey ship to %s", system.getNameWithLowercaseType()));
			}
		}
		return entity;
	}
	
	protected List<AddedEntity> addProbes(StarSystemAPI system, int num) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.PLANET_ORBIT, 20f);
		weights.put(LocationType.JUMP_ORBIT, 10f);
		weights.put(LocationType.NEAR_STAR, 10f);
		weights.put(LocationType.OUTER_SYSTEM, 5f);
		weights.put(LocationType.IN_ASTEROID_BELT, 5f);
		weights.put(LocationType.IN_RING, 5f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 5f);
		weights.put(LocationType.STAR_ORBIT, 1f);
		weights.put(LocationType.IN_SMALL_NEBULA, 1f);
		weights.put(LocationType.L_POINT, 1f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, system, 100f, weights);
		
		List<AddedEntity> result = new ArrayList<AddedEntity>();
		for (int i = 0; i < num; i++) {
			AddedEntity probe = addEntity(random, system, locs, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT);
			if (probe != null) {
				result.add(probe);
				
				system.addTag(Tags.THEME_INTERESTING_MINOR);
				system.addTag(Tags.THEME_DERELICT);
				system.addTag(Tags.THEME_DERELICT_PROBES);
			}
			
			if (DEBUG) {
				if (probe != null) {
					System.out.println(String.format("  Added probe to %s", system.getNameWithLowercaseType()));
				} else {
					System.out.println(String.format("  Failed to add probe to %s", system.getNameWithLowercaseType()));
				}
			}
		}
		return result;
	}

	
	/**
	 * Sorted by *descending* distance from sortFrom.
	 * @param context
	 * @param sortFrom
	 * @return
	 */
	protected List<Constellation> getSortedAvailableConstellations(ThemeGenContext context, boolean emptyOk, final Vector2f sortFrom, List<Constellation> exclude) {
		List<Constellation> constellations = new ArrayList<Constellation>();
		for (Constellation c : context.constellations) {
			if (context.majorThemes.containsKey(c)) continue;
			if (!emptyOk && constellationIsEmpty(c)) continue;
			
			constellations.add(c);
		}
		
		if (exclude != null) {
			constellations.removeAll(exclude);
		}
		
		Collections.sort(constellations, new Comparator<Constellation>() {
			public int compare(Constellation o1, Constellation o2) {
				float d1 = Misc.getDistance(o1.getLocation(), sortFrom);
				float d2 = Misc.getDistance(o2.getLocation(), sortFrom);
				return (int) Math.signum(d2 - d1);
			}
		});
		return constellations;
	}
	
	
	public static boolean constellationIsEmpty(Constellation c) {
		for (StarSystemAPI s : c.getSystems()) {
			if (!systemIsEmpty(s)) return false;
		}
		return true;
	}
	public static boolean systemIsEmpty(StarSystemAPI system) {
		for (PlanetAPI p : system.getPlanets()) {
			if (!p.isStar()) return false;
		}
		//system.getTerrainCopy().isEmpty()
		return true;
	}
	
	
	
	
	
	
	
	
	
//	public List<AddedEntity> generateForSystem(StarSystemAPI system, SystemGenData data) {
//		if (data == null) return new ArrayList<AddedEntity>();
//		
//		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
//		weights.put(LocationType.PLANET_ORBIT, 1f);
//		weights.put(LocationType.JUMP_ORBIT, 1f);
//		weights.put(LocationType.NEAR_STAR, 1f);
//		weights.put(LocationType.OUTER_SYSTEM, 1f);
//		weights.put(LocationType.IN_ASTEROID_BELT, 1f);
//		weights.put(LocationType.IN_RING, 1f);
//		weights.put(LocationType.IN_ASTEROID_FIELD, 1f);
//		weights.put(LocationType.STAR_ORBIT, 1f);
//		weights.put(LocationType.IN_SMALL_NEBULA, 1f);
//		weights.put(LocationType.L_POINT, 1f);
//		WeightedRandomPicker<EntityLocation> locs = getLocations(random, system, 100f, weights);
//		
//		List<AddedEntity> result = new ArrayList<AddedEntity>();
//		for (int i = 0; i < data.numProbes; i++) {
//			AddedEntity e = addEntity(system, locs, Entities.DERELICT_SURVEY_PROBE, Factions.DERELICT);
//			if (e != null) result.add(e);
//		}
//		
//		for (int i = 0; i < data.numSurveyShips; i++) {
//			AddedEntity e = addEntity(system, locs, Entities.DERELICT_SURVEY_SHIP, Factions.DERELICT);
//			if (e != null) result.add(e);
//		}
//		
//		for (int i = 0; i < data.numMotherships; i++) {
//			AddedEntity e = addEntity(system, locs, Entities.DERELICT_MOTHERSHIP, Factions.DERELICT);
//			if (e != null) result.add(e);
//		}
//		
//		
//		return result;
//
//	}

	
	
	@Override
	public int getOrder() {
		return 1000;
	}


	
	
	
}
















