package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.NameAssigner;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.SpecialCreationContext;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;


public class RuinsThemeGenerator extends BaseThemeGenerator {

	public static final int MIN_CONSTELLATIONS_WITH_RUINS = 15;
	public static final int MAX_CONSTELLATIONS_WITH_RUINS = 25;
	
	public static float CONSTELLATION_SKIP_PROB = 0f;
	
	
	public String getThemeId() {
		return Themes.RUINS;
	}

	
	@Override
	public void generateForSector(ThemeGenContext context, float allowedUnusedFraction) {
		
		float total = (float) (context.constellations.size() - context.majorThemes.size()) * allowedUnusedFraction;
		if (total <= 0) return;
		
		int num = (int) StarSystemGenerator.getNormalRandom(MIN_CONSTELLATIONS_WITH_RUINS, MAX_CONSTELLATIONS_WITH_RUINS);
		if (num > total) num = (int) total;
		
		
		List<Constellation> constellations = getSortedAvailableConstellations(context, false, new Vector2f(), null);
		Collections.reverse(constellations);
		
		float skipProb = CONSTELLATION_SKIP_PROB;
		if (total < num / (1f - skipProb)) {
			skipProb = 1f - (num / total);
		}
		skipProb = 0f;

		List<StarSystemData> ruinSystems = new ArrayList<StarSystemData>();
		
		if (DEBUG) System.out.println("\n\n\n");
		if (DEBUG) System.out.println("Generating systems with ruins");
		
		int count = 0;
		
		int numUsed = 0;
		for (int i = 0; i < num && i < constellations.size(); i++) {
			Constellation c = constellations.get(i);
			if (random.nextFloat() < skipProb) {
				if (DEBUG) System.out.println("Skipping constellation " + c.getName());
				continue;
			}
			
			
//			if (c.getName().toLowerCase().contains("shero")) {
//				System.out.println("wefwefwef");
//			}
			
			List<StarSystemData> systems = new ArrayList<StarSystemData>();
			for (StarSystemAPI system : c.getSystems()) {
				StarSystemData data = computeSystemData(system);
				systems.add(data);
			}
			
			List<StarSystemData> mainCandidates = getSortedSystemsSuitedToBePopulated(systems);
			
			int numMain = 1 + random.nextInt(3);
			if (numMain > mainCandidates.size()) numMain = mainCandidates.size();
			if (numMain <= 0) {
				if (DEBUG) System.out.println("Skipping constellation " + c.getName() + ", no suitable main candidates");
				continue;
			}
			
			context.majorThemes.put(c, Themes.RUINS);
			numUsed++;

			if (DEBUG) System.out.println("Generating " + numMain + " main systems in " + c.getName());
			for (int j = 0; j < numMain; j++) {
				StarSystemData data = mainCandidates.get(j);
				populateMain(data);
				
				data.system.addTag(Tags.THEME_INTERESTING);
				data.system.addTag(Tags.THEME_RUINS);
				data.system.addTag(Tags.THEME_RUINS_MAIN);
				ruinSystems.add(data);

				RuinsFleetRouteManager fleets = new RuinsFleetRouteManager(data.system);
				data.system.addScript(fleets);
				
				if (!NameAssigner.isNameSpecial(data.system)) {
					NameAssigner.assignSpecialNames(data.system);
				}
			}
			
			for (StarSystemData data : systems) {
				int index = mainCandidates.indexOf(data);
				if (index >= 0 && index < numMain) continue;
				
				populateNonMain(data);
				
				data.system.addTag(Tags.THEME_INTERESTING);
				data.system.addTag(Tags.THEME_RUINS);
				data.system.addTag(Tags.THEME_RUINS_SECONDARY);
				ruinSystems.add(data);
			}
			
//			if (count == 1) {
//				System.out.println("RANDOM INDEX " + count + ": " + random.nextLong());
//			}
			count++;
		}
		
		SpecialCreationContext specialContext = new SpecialCreationContext();
		specialContext.themeId = getThemeId();
		SalvageSpecialAssigner.assignSpecials(ruinSystems, specialContext);
		
		if (DEBUG) System.out.println("Finished generating systems with ruins\n\n\n\n\n");
		
	}
	

	
	public void populateNonMain(StarSystemData data) {
		if (DEBUG) System.out.println(" Generating secondary ruins in " + data.system.getName());
		boolean special = data.isBlackHole() || data.isNebula() || data.isPulsar();
		if (special) {
			addResearchStations(data, 0.75f, 1, 1, createStringPicker(Entities.STATION_RESEARCH, 10f));
		}
		
		if (random.nextFloat() < 0.5f) return;
		
		if (!data.resourceRich.isEmpty()) {
			addMiningStations(data, 0.5f, 1, 1, createStringPicker(Entities.STATION_MINING, 10f));
		}
		
		if (!special && !data.habitable.isEmpty()) {
			// ruins on planet, or orbital station
			addHabCenters(data, 0.25f, 1, 1, createStringPicker(Entities.ORBITAL_HABITAT, 10f));
		}
		
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
	
	
	public void populateMain(StarSystemData data) {
		
		if (DEBUG) System.out.println(" Generating ruins in " + data.system.getName());
		
		StarSystemAPI system = data.system;
		
		int maxHabCenters = 1 + random.nextInt(3);
		
		HabitationLevel level = HabitationLevel.LOW;
		if (maxHabCenters == 2) level = HabitationLevel.MEDIUM;
		if (maxHabCenters >= 3) level = HabitationLevel.HIGH;

		addHabCenters(data, 1, maxHabCenters, maxHabCenters, createStringPicker(Entities.ORBITAL_HABITAT, 10f));
		
		// add various stations, orbiting entities, etc
		float probGate = 1f;
		float probRelay = 1f;
		float probMining = 0.5f;
		float probResearch = 0.25f;
		
		switch (level) {
		case HIGH:
			probGate = 0.5f;
			probRelay = 1f;
			break;
		case MEDIUM:
			probGate = 0.3f;
			probRelay = 0.75f;
			break;
		case LOW:
			probGate = 0.2f;
			probRelay = 0.5f;
			break;
		}
		
//		MN-6186477243757813340		
//		float test = Misc.getDistance(data.system.getLocation(), new Vector2f(48500, -22000));
//		if (test < 1000) {
//			System.out.println("HERE: " + random.nextLong());
//		}
//		if (data.system.getName().toLowerCase().contains("cadmus")) {
//			System.out.println("wefwefwefew");
//		}
		//addCommRelay(data, probRelay);
		
		addObjectives(data, probRelay);
		
		
		WeightedRandomPicker<String> factions = SalvageSpecialAssigner.getNearbyFactions(random, system.getCenter(),
												15f, 5f, 5f);
		addInactiveGate(data, probGate, 0.75f, 0.75f, factions);
		
		addShipGraveyard(data, 0.25f, 1, 1, factions);
		
		addMiningStations(data, probMining, 1, 1, createStringPicker(Entities.STATION_MINING, 10f));
		
		addResearchStations(data, probResearch, 1, 1, createStringPicker(Entities.STATION_RESEARCH, 10f));
		
		
		//addDebrisFields(data, 0.75f, 1, 5, Factions.REMNANTS, 0.2f, 1, 3);
		addDebrisFields(data, 0.75f, 1, 5);

		addDerelictShips(data, 0.75f, 0, 7, factions);
		
		
		WeightedRandomPicker<String> caches = createStringPicker( 
				Entities.SUPPLY_CACHE, 10f,
				Entities.SUPPLY_CACHE_SMALL, 10f,
				Entities.EQUIPMENT_CACHE, 10f,
				Entities.EQUIPMENT_CACHE_SMALL, 10f
				);
		
		
		float r = random.nextFloat();
		if (r < 0.33f) {
			caches.add(Entities.WEAPONS_CACHE, 10f);
			caches.add(Entities.WEAPONS_CACHE_SMALL, 10f);
		} else if (r < 0.67f) {
			caches.add(Entities.WEAPONS_CACHE_LOW, 10f);
			caches.add(Entities.WEAPONS_CACHE_SMALL_LOW, 10f);
		} else {
			caches.add(Entities.WEAPONS_CACHE_HIGH, 10f);
			caches.add(Entities.WEAPONS_CACHE_SMALL_HIGH, 10f);
		}
		
		addCaches(data, 0.75f, 0, 3, caches);
		
	}
	
	
	
	public List<StarSystemData> getSortedSystemsSuitedToBePopulated(List<StarSystemData> systems) {
		List<StarSystemData> result = new ArrayList<StarSystemData>();
		
		for (StarSystemData data : systems) {
			if (data.isBlackHole() || data.isNebula() || data.isPulsar()) continue;
			
			if (data.planets.size() >= 4 || data.habitable.size() >= 1) {
				result.add(data);
			}
		}
		
		Collections.sort(systems, new Comparator<StarSystemData>() {
			public int compare(StarSystemData o1, StarSystemData o2) {
				float s1 = getMainCenterScore(o1);
				float s2 = getMainCenterScore(o2);
				return (int) Math.signum(s2 - s1);
			}
		});
		
		return result;
	}
	
	public float getMainCenterScore(StarSystemData data) {
		float total = 0f;
		total += data.planets.size() * 1f;
		total += data.habitable.size() * 2f;
		total += data.resourceRich.size() * 0.25f;
		return total;
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
		return true;
	}
	
	
	
	
	@Override
	public int getOrder() {
		return 2000;
	}


	
	
}
















