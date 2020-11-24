package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CircularOrbitWithSpinAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.ObjectiveGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.LagrangePointType;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public abstract class BaseThemeGenerator implements ThemeGenerator {
	
	public static enum HabitationLevel {
		LOW,
		MEDIUM,
		HIGH,
	}
	
	public static class StarSystemData {
		public StarSystemAPI system;
		public List<PlanetAPI> stars = new ArrayList<PlanetAPI>();
		public List<PlanetAPI> planets = new ArrayList<PlanetAPI>();
		public List<PlanetAPI> habitable = new ArrayList<PlanetAPI>();
		public List<PlanetAPI> gasGiants = new ArrayList<PlanetAPI>();
		public List<PlanetAPI> resourceRich = new ArrayList<PlanetAPI>();
		
		public Set<SectorEntityToken> alreadyUsed = new LinkedHashSet<SectorEntityToken>();
		
		public Set<AddedEntity> generated = new LinkedHashSet<AddedEntity>();
		
		public boolean isBlackHole() {
			return system.getStar() != null && system.getStar().getSpec().isBlackHole();
		}
		
		public boolean isPulsar() {
			//return system.getStar() != null && system.getStar().getSpec().getPlanetType().equals(StarTypes.NEUTRON_STAR);
			return system.hasPulsar();
		}
		
		public boolean isNebula() {
			return system.isNebula();
		}

		@Override
		public String toString() {
			return String.format(system.getName() + " %d %d %d %d %d", stars.size(), planets.size(), habitable.size(), 
								 gasGiants.size(),
								 resourceRich.size());
		}
		
		
	}
	
	
	public static boolean DEBUG = Global.getSettings().isDevMode();
	
	
	public static class AddedEntity {
		public SectorEntityToken entity;
		public EntityLocation location;
		public String entityType;
		public AddedEntity(SectorEntityToken entity, EntityLocation location, String entityType) {
			this.entity = entity;
			this.location = location;
			this.entityType = entityType;
		}
	}
	
	
	public static enum LocationType {
		PLANET_ORBIT,
		GAS_GIANT_ORBIT,
		JUMP_ORBIT,
		NEAR_STAR,
		IN_ASTEROID_BELT,
		IN_ASTEROID_FIELD,
		IN_RING,
		
		L_POINT,
		IN_SMALL_NEBULA,
		
		OUTER_SYSTEM,
		STAR_ORBIT,
	}

	public static class EntityLocation {
		public LocationType type;
		public Vector2f location = null;
		public OrbitAPI orbit = null;
		
		
		@Override
		public String toString() {
			return String.format("Type: %s, orbitPeriod: %s", type.name(), orbit == null ? "null" : "" + orbit.getOrbitalPeriod());
		}
		
		
	}
	
	abstract public int getOrder();
	abstract public String getThemeId();

	public float getWeight() {
		return 100f;
	}
	
	protected Random random;
	public BaseThemeGenerator() {
		random = StarSystemGenerator.random;
	}
	

	abstract public void generateForSector(ThemeGenContext context, float allowedSectorFraction);
	
	
	public void addShipGraveyard(StarSystemData data, float chanceToAddAny, int min, int max, WeightedRandomPicker<String> factions) {
		if (random.nextFloat() >= chanceToAddAny) return;
		int num = min + random.nextInt(max - min + 1);
		
		for (int i = 0; i < num; i++) {
			LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
//			weights.put(LocationType.IN_ASTEROID_BELT, 5f);
//			weights.put(LocationType.IN_ASTEROID_FIELD, 5f);
//			weights.put(LocationType.IN_RING, 5f);
//			weights.put(LocationType.IN_SMALL_NEBULA, 5f);
//			weights.put(LocationType.L_POINT, 5f);
//			weights.put(LocationType.NEAR_STAR, 5f);
//			weights.put(LocationType.OUTER_SYSTEM, 5f);
			weights.put(LocationType.STAR_ORBIT, 5f);
			WeightedRandomPicker<EntityLocation> locs = getLocations(random, data.system, null, 1000f, weights);
			EntityLocation loc = locs.pick();
			
			if (loc != null) {
				SectorEntityToken token = data.system.createToken(0, 0);
				data.system.addEntity(token);
				setEntityLocation(token, loc, null);
				addShipGraveyard(data, token, factions);
			}
		}
		
	}
	
	public void addShipGraveyard(StarSystemData data, SectorEntityToken focus, WeightedRandomPicker<String> factions) {
		int numShips = random.nextInt(9) + 3;
		//numShips = 12;
		if (DEBUG) System.out.println("    Adding ship graveyard (" + numShips + " ships)");
		
		WeightedRandomPicker<Float> bands = new WeightedRandomPicker<Float>(random);
		for (int i = 0; i < numShips + 5; i++) {
			bands.add(new Float(140 + i * 20), (i + 1) * (i + 1));
		}
		
//		WeightedRandomPicker<String> factions = new WeightedRandomPicker<String>(random);
//		factions.add(Factions.TRITACHYON, 10f);
//		factions.add(Factions.HEGEMONY, 7f);
//		factions.add(Factions.INDEPENDENT, 3f);
		
		for (int i = 0; i < numShips; i++) {
			float radius = bands.pickAndRemove();
			
			DerelictShipData params = DerelictShipEntityPlugin.createRandom(factions.pick(), null, random);
			if (params != null) {
				CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) addSalvageEntity(
									focus.getContainingLocation(),
									Entities.WRECK, Factions.NEUTRAL, params);
				entity.setDiscoverable(true);
				float orbitDays = radius / (5f + StarSystemGenerator.random.nextFloat() * 10f);
				entity.setCircularOrbit(focus, random.nextFloat() * 360f, radius, orbitDays);
				if (DEBUG) System.out.println("      Added ship: " + 
						((DerelictShipEntityPlugin)entity.getCustomPlugin()).getData().ship.variantId);
				
				AddedEntity added = new AddedEntity(entity, null, Entities.WRECK);
				data.generated.add(added);
			}
		}
	}
	
	public void addDerelictShips(StarSystemData data, float chanceToAddAny, int min, int max, WeightedRandomPicker<String> factions) {
		if (random.nextFloat() >= chanceToAddAny) return;
		
		//data.system.updateAllOrbits();
		
		int num = min + random.nextInt(max - min + 1);
		for (int i = 0; i < num; i++) {
			EntityLocation loc = pickAnyLocation(random, data.system, 70f, null);
			addDerelictShip(data, loc, factions);
		}
		
	}
	
	public void addMiningStations(StarSystemData data, float chanceToAddAny, int min, int max, WeightedRandomPicker<String> stationTypes) {
		if (random.nextFloat() >= chanceToAddAny) return;
		
		int num = min + random.nextInt(max - min + 1);
		if (DEBUG) System.out.println("    Adding " + num + " mining stations");
		for (int i = 0; i < num; i++) {
			List<PlanetAPI> miningCandidates = new ArrayList<PlanetAPI>();
			miningCandidates.addAll(data.gasGiants);
			miningCandidates.addAll(data.resourceRich);
			
			LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
			weights.put(LocationType.IN_ASTEROID_BELT, 10f);
			weights.put(LocationType.IN_ASTEROID_FIELD, 10f);
			weights.put(LocationType.IN_RING, 10f);
			weights.put(LocationType.IN_SMALL_NEBULA, 10f);
			WeightedRandomPicker<EntityLocation> locs = getLocations(random, data.system, null, 100f, weights);
			EntityLocation loc = locs.pick();
			
			String type = stationTypes.pick();
			if (loc != null || !miningCandidates.isEmpty()) {
				if ((random.nextFloat() > 0.5f && loc != null) || miningCandidates.isEmpty()) {
					addStation(loc, data, type, Factions.NEUTRAL);
				} else {
					PlanetAPI planet = miningCandidates.get(random.nextInt(miningCandidates.size()));
					EntityLocation planetOrbitLoc = createLocationAtRandomGap(random, planet, 100f);
					addStation(planetOrbitLoc, data, type, Factions.NEUTRAL);
					data.alreadyUsed.add(planet);
				}
			}
		}
	}
	
	
	public static float NOT_HABITABLE_PLANET_PROB = 0.1f;
	public static float ORBITAL_HABITAT_PROB = 0.5f;
	
	public void addHabCenters(StarSystemData data, float chanceToAddAny, int min, int max, WeightedRandomPicker<String> stationTypes) {
		if (random.nextFloat() >= chanceToAddAny) return;
		
		WeightedRandomPicker<PlanetAPI> habPlanets = new WeightedRandomPicker<PlanetAPI>(random);
		for (PlanetAPI planet : data.habitable) {
			float h = planet.getMarket().getHazardValue();
			h -= 0.5f;
			if (h < 0.1f) h = 0.1f;
			float w = 1f / h;
			habPlanets.add(planet, w);
		}
		
		WeightedRandomPicker<PlanetAPI> otherPlanets = new WeightedRandomPicker<PlanetAPI>(random);
		for (PlanetAPI planet : data.planets) {
			if (data.habitable.contains(planet)) continue;
			otherPlanets.add(planet);
		}
		
		int num = min + random.nextInt(max - min + 1);
		if (DEBUG) System.out.println("    Adding up to " + num + " hab centers on planets/in orbit");
		for (int i = 0; i < num; i++) {
			int option = 0;
			if (!habPlanets.isEmpty() && (random.nextFloat() > NOT_HABITABLE_PLANET_PROB || i == 0)) {
				option = 0; // habitable planet
			} else {
				if (otherPlanets.isEmpty() || random.nextFloat() < ORBITAL_HABITAT_PROB) {
					option = 2; // orbital habitat
				} else {
					option = 1; // other planet
				}
			}
			
			if (option == 0) {
				PlanetAPI planet = habPlanets.pickAndRemove();
				addRuins(planet);
				data.alreadyUsed.add(planet);
			} else if (option == 1) {
				PlanetAPI planet = otherPlanets.pickAndRemove();
				addRuins(planet);
				data.alreadyUsed.add(planet);
			} else if (option == 2) {
				String type = stationTypes.pick();
				EntityLocation loc = pickCommonLocation(random, data.system, 100f, true, null);
				addStation(loc, data, type, Factions.NEUTRAL);
			}
		}
	}
	
	
	public void addResearchStations(StarSystemData data, float chanceToAddAny, int min, int max, WeightedRandomPicker<String> stationTypes) {
		if (random.nextFloat() >= chanceToAddAny) return;
		
		int num = min + random.nextInt(max - min + 1);
		if (DEBUG) System.out.println("    Adding " + num + " research stations");
		for (int i = 0; i < num; i++) {
			String type = stationTypes.pick();
			
			List<PlanetAPI> researchCandidates = new ArrayList<PlanetAPI>();
			researchCandidates.addAll(data.gasGiants);
			
			LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
			weights.put(LocationType.IN_SMALL_NEBULA, 5f);
			weights.put(LocationType.GAS_GIANT_ORBIT, 10f);
			weights.put(LocationType.NEAR_STAR, 5f);
			WeightedRandomPicker<EntityLocation> locs = getLocations(random, data.system, data.alreadyUsed, 100f, weights);
			EntityLocation loc = locs.pick();
			
			if (loc != null) {
				AddedEntity added = addStation(loc, data, type, Factions.NEUTRAL);
				if (loc.orbit != null && loc.orbit.getFocus() instanceof PlanetAPI) {
					PlanetAPI planet = (PlanetAPI) loc.orbit.getFocus();
					if (!planet.isStar()) {
						data.alreadyUsed.add(planet);
					}
				}
			}
		}
	}
	
	
	public void addRuins(PlanetAPI planet) {
		if (planet == null) return;
		
		MarketAPI market = planet.getMarket();
		clearRuins(market);
		
		String ruins = pickRuinsType(planet);
		if (DEBUG) System.out.println("      Added " + ruins + " to " + market.getName());
		market.addCondition(ruins);
		if (shouldHaveDecivilized(planet, ruins)) {
			if (DEBUG) System.out.println("        Added decivilized to " + market.getName());
			market.addCondition(Conditions.DECIVILIZED);
		}
	}
	
	public boolean shouldHaveDecivilized(PlanetAPI planet, String ruins) {
		float chance = 0.25f;
		
		if (planet.getMarket().hasCondition(Conditions.HABITABLE)) {
			chance += 0.25f;
		}
		
		if (ruins != null && ruins.equals(Conditions.RUINS_EXTENSIVE)) {
			chance += 0.1f;
		}
		if (ruins != null && ruins.equals(Conditions.RUINS_VAST)) {
			chance += 0.2f;
		}
		
		return StarSystemGenerator.random.nextFloat() < chance;
	}
	

	public List<AddedEntity> addObjectives(StarSystemData data, float prob) {
		List<AddedEntity> result = new ArrayList<AddedEntity>();

		Set<String> used = new HashSet<String>();
		
		float mult = 2f;
		for (SectorEntityToken loc : data.system.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
			mult *= 0.5f;
			if (StarSystemGenerator.random.nextFloat() >= prob * mult) continue;
			
			WeightedRandomPicker<ObjectiveGenDataSpec> picker = new WeightedRandomPicker<ObjectiveGenDataSpec>(random);
			for (Object o : Global.getSettings().getAllSpecs(ObjectiveGenDataSpec.class)) {
				ObjectiveGenDataSpec spec = (ObjectiveGenDataSpec) o;
				if (used.contains(spec.getCategory())) continue;
				picker.add(spec, spec.getFrequency());
			}
			
			ObjectiveGenDataSpec pick = picker.pick();
			if (pick == null) break;
			
			used.add(pick.getCategory());
			
			SectorEntityToken built = data.system.addCustomEntity(null,
					 									 		  null,
					 									 		  pick.getId(), // type of object, defined in custom_entities.json
					 									 		  Factions.NEUTRAL); // faction
			built.getMemoryWithoutUpdate().set(MemFlags.OBJECTIVE_NON_FUNCTIONAL, true);
			if (loc.getOrbit() != null) {
				built.setOrbit(loc.getOrbit().makeCopy());
			}
			built.setLocation(loc.getLocation().x, loc.getLocation().y);
			data.system.removeEntity(loc);
			
			AddedEntity e = new AddedEntity(built, null, pick.getId());
			result.add(e);
		}
		
		
		return result;
	}
	
	public static ObjectiveGenDataSpec getObjectiveSpec(String id) {
		ObjectiveGenDataSpec spec = (ObjectiveGenDataSpec) Global.getSettings().getSpec(ObjectiveGenDataSpec.class, id, false);
		return spec;
	}
	
	public AddedEntity addCommRelay(StarSystemData data, float prob) {
		if (StarSystemGenerator.random.nextFloat() >= prob) return null;
		
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.STAR_ORBIT, 10f);
		weights.put(LocationType.OUTER_SYSTEM, 10f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, data.system, null, 100f, weights);
		EntityLocation loc = locs.pick();
		
		AddedEntity added = addNonSalvageEntity(data.system, loc, Entities.COMM_RELAY, Factions.NEUTRAL);
		if (DEBUG && added != null) System.out.println("    Added comm relay");
		
		if (added != null) {
			convertOrbitNoSpin(added.entity);
			added.entity.getMemoryWithoutUpdate().set(MemFlags.OBJECTIVE_NON_FUNCTIONAL, true);
		}
		
		return added;
	}
	
	public AddedEntity addInactiveGate(StarSystemData data, float prob, float probDebris, float probShips, WeightedRandomPicker<String> factions) {
		if (StarSystemGenerator.random.nextFloat() >= prob) return null;
		
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.STAR_ORBIT, 10f);
		weights.put(LocationType.OUTER_SYSTEM, 10f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, data.system, null, 100f, weights);
		EntityLocation loc = locs.pick();
		
		AddedEntity added = addNonSalvageEntity(data.system, loc, Entities.INACTIVE_GATE, Factions.NEUTRAL);
		if (DEBUG && added != null) System.out.println("    Added inactive gate");
		
		if (added != null) {
			convertOrbitNoSpin(added.entity);
			
			if (random.nextFloat() < probDebris) {
				if (DEBUG && added != null) System.out.println("      Added debris field around gate");
				addDebrisField(data, added.entity, 500f + random.nextFloat() * 100f);
				if (random.nextFloat() < probShips) {
					if (DEBUG && added != null) System.out.println("      Added ship graveyard around gate");
					addShipGraveyard(data, added.entity, factions);
				}
			}
		}
		
		return added;
	}
	
	
	public String pickRuinsType(PlanetAPI planet) {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(StarSystemGenerator.random);
		
		float hazard = planet.getMarket().getHazardValue();

		float add1 = 0, add2 = 0;
		
		if (hazard <= 1f) {
			add1 = 10f;
			add2 = 5f;
		} else if (hazard <= 1.25f) {
			add1 = 5f;
			add2 = 1f;
		}
		
		picker.add(Conditions.RUINS_SCATTERED, 10f);
		picker.add(Conditions.RUINS_WIDESPREAD, 10f);
		picker.add(Conditions.RUINS_EXTENSIVE, 3f + add1);
		picker.add(Conditions.RUINS_VAST, 1f + add2);
		
		return picker.pick();
	}
	
	
	public AddedEntity addStation(EntityLocation loc, StarSystemData data, String customEntityId, String factionId) {
		if (loc == null) return null;
		
		AddedEntity station = addEntity(data.system, loc, customEntityId, factionId);
		if (station != null) {
			data.generated.add(station);
		}
		SectorEntityToken focus = station.entity.getOrbitFocus();
		if (DEBUG) System.out.println("      Added " + customEntityId);
		if (focus instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) focus;
			data.alreadyUsed.add(planet);
			
			boolean nearStar = planet.isStar() && station.entity.getOrbit() != null && station.entity.getCircularOrbitRadius() < 5000; 
			
			if (planet.isStar() && !nearStar) {
//				station.entity.setFacing(random.nextFloat() * 360f);
//				convertOrbitNoSpin(station.entity);
			} else {
				convertOrbitPointingDown(station.entity);
			}
		}
		
//		station.entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_DEFENDER_FACTION, Factions.REMNANTS);
//		station.entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_DEFENDER_PROB, 1f);
		
		return station;
	}
	
	
	public void addCaches(StarSystemData data, float chanceToAddAny, int min, int max, WeightedRandomPicker<String> cacheTypes) {
		if (random.nextFloat() >= chanceToAddAny) return;
		
		int num = min + random.nextInt(max - min + 1);
		if (DEBUG) System.out.println("    Adding " + num + " resource caches");
		for (int i = 0; i < num; i++) {
			EntityLocation loc = pickHiddenLocation(random, data.system, 70f, null);
			String type = cacheTypes.pick();
			AddedEntity added = addEntity(data.system, loc, type, Factions.NEUTRAL);
			if (added != null) {
				data.generated.add(added);
			}
			
			if (DEBUG && added != null) System.out.println("      Added resource cache: " + type);
		}
	}
	
	public void addDebrisFields(StarSystemData data, float chanceToAddAny, int min, int max) {
		addDebrisFields(data, chanceToAddAny, min, max, null, 0f, 0, 0);
	}
	public void addDebrisFields(StarSystemData data, float chanceToAddAny, int min, int max, String defFaction, float defProb, int minStr, int maxStr) {
		if (random.nextFloat() >= chanceToAddAny) return;

		int numDebrisFields = min + random.nextInt(max - min + 1);
		if (DEBUG) System.out.println("    Adding up to " + numDebrisFields + " debris fields");
		for (int i = 0; i < numDebrisFields; i++) {
			
			
			float radius = 150f + random.nextFloat() * 300f;
			EntityLocation loc = pickAnyLocation(random, data.system, radius + 100f, null);
			if (loc == null) continue;
			
			DebrisFieldParams params = new DebrisFieldParams(
					radius, // field radius - should not go above 1000 for performance reasons
					1f, // density, visual - affects number of debris pieces
					10000000f, // duration in days 
					0f); // days the field will keep generating glowing pieces
			
			if (defFaction != null) {
				params.defFaction = defFaction;
				params.defenderProb = defProb;
				params.minStr = minStr;
				params.maxStr = maxStr;
			}
			
			params.source = DebrisFieldSource.GEN;
			SectorEntityToken debris = Misc.addDebrisField(data.system, params, random);
			setEntityLocation(debris, loc, Entities.DEBRIS_FIELD_SHARED);
			
			AddedEntity added = new AddedEntity(debris, loc, Entities.DEBRIS_FIELD_SHARED);
			data.generated.add(added);
			
			if (DEBUG) System.out.println("      Added debris field");
		}
	}
	
	public AddedEntity addDebrisField(StarSystemData data, SectorEntityToken focus, float radius) {
		DebrisFieldParams params = new DebrisFieldParams(
				radius, // field radius - should not go above 1000 for performance reasons
				1f, // density, visual - affects number of debris pieces
				10000000f, // duration in days 
				0f); // days the field will keep generating glowing pieces
		
		params.source = DebrisFieldSource.GEN;
		SectorEntityToken debris = Misc.addDebrisField(focus.getContainingLocation(), params, random);
		debris.setCircularOrbit(focus, 0, 0, 100f);
		if (DEBUG) System.out.println("      Added debris field");
		
		EntityLocation loc = new EntityLocation();
		loc.type = LocationType.OUTER_SYSTEM; // sigh
		AddedEntity added = new AddedEntity(debris, loc, Entities.DEBRIS_FIELD_SHARED);
		data.generated.add(added);
		
		return added;
	}
	
	
	public WeightedRandomPicker<String> createStringPicker(Object ... params) {
		return createStringPicker(random, params);
	}
	
	public static WeightedRandomPicker<String> createStringPicker(Random random, Object ... params) {
		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
		for (int i = 0; i < params.length; i += 2) {
			String item = (String) params[i];
			float weight = 0f;
			if (params[i+1] instanceof Float) {
				weight = (Float) params[i+1];
			} else if (params[i+1] instanceof Integer) {
				weight = (Integer) params[i+1];
			}
			picker.add(item, weight);
		}
		return picker;
	}
	
	
	public void addDerelictShip(StarSystemData data, EntityLocation loc, WeightedRandomPicker<String> factions) {
		if (loc == null) return;
		
//		WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<String>(random);
//		for (String faction : factions) {
//			factionPicker.add(faction);
//		}
		String faction = factions.pick();
		DerelictShipData params = DerelictShipEntityPlugin.createRandom(faction, null, random);
		if (params != null) {
			CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) addSalvageEntity(data.system,
											Entities.WRECK, Factions.NEUTRAL, params);
			entity.setDiscoverable(true);
			setEntityLocation(entity, loc, Entities.WRECK);
			if (DEBUG) System.out.println("      Added ship: " + 
					((DerelictShipEntityPlugin)entity.getCustomPlugin()).getData().ship.variantId);
			
			AddedEntity added = new AddedEntity(entity, null, Entities.WRECK);
			data.generated.add(added);
		}
		
	}
	
	public AddedEntity addDerelictShip(StarSystemData data, EntityLocation loc, String variantId) {
		if (loc == null) return null;
	
		DerelictShipData params = DerelictShipEntityPlugin.createVariant(variantId, random);
		if (params != null) {
			CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) addSalvageEntity(data.system,
					Entities.WRECK, Factions.NEUTRAL, params);
			entity.setDiscoverable(true);
			setEntityLocation(entity, loc, Entities.WRECK);
			if (DEBUG) System.out.println("      Added ship: " + 
					((DerelictShipEntityPlugin)entity.getCustomPlugin()).getData().ship.variantId);
			
			AddedEntity added = new AddedEntity(entity, null, Entities.WRECK);
			data.generated.add(added);
			return added;
		}
		return null;
		
	}
	
	
	public static EntityLocation pickCommonLocation(Random random, StarSystemAPI system, float gap, boolean allowStarOrbit, Set<SectorEntityToken> exclude) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.PLANET_ORBIT, 10f);
		if (allowStarOrbit) {
			weights.put(LocationType.STAR_ORBIT, 10f);
		}
		weights.put(LocationType.GAS_GIANT_ORBIT, 5f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, system, exclude, gap, weights);
		if (locs.isEmpty()) {
			return pickAnyLocation(random, system, gap, exclude);
		}
		return locs.pick();
	}
	
	public static EntityLocation pickUncommonLocation(Random random, StarSystemAPI system, float gap, Set<SectorEntityToken> exclude) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.IN_ASTEROID_BELT, 5f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 5f);
		weights.put(LocationType.IN_RING, 5f);
		weights.put(LocationType.IN_SMALL_NEBULA, 5f);
		weights.put(LocationType.L_POINT, 5f);
		weights.put(LocationType.GAS_GIANT_ORBIT, 5f);
		weights.put(LocationType.JUMP_ORBIT, 5f);
		weights.put(LocationType.NEAR_STAR, 5f);
		weights.put(LocationType.OUTER_SYSTEM, 5f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, system, exclude, gap, weights);
		if (locs.isEmpty()) {
			return pickAnyLocation(random, system, gap, exclude);
		}
		return locs.pick();
	}
	
	public static EntityLocation pickAnyLocation(Random random, StarSystemAPI system, float gap, Set<SectorEntityToken> exclude) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.PLANET_ORBIT, 10f);
		weights.put(LocationType.STAR_ORBIT, 10f);
		weights.put(LocationType.IN_ASTEROID_BELT, 5f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 5f);
		weights.put(LocationType.IN_RING, 5f);
		weights.put(LocationType.IN_SMALL_NEBULA, 5f);
		weights.put(LocationType.L_POINT, 5f);
		weights.put(LocationType.GAS_GIANT_ORBIT, 5f);
		weights.put(LocationType.JUMP_ORBIT, 5f);
		weights.put(LocationType.NEAR_STAR, 5f);
		weights.put(LocationType.OUTER_SYSTEM, 5f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, system, exclude, gap, weights);
		return locs.pick();
	}
	
	public static EntityLocation pickHiddenLocation(Random random, StarSystemAPI system, float gap, Set<SectorEntityToken> exclude) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.IN_ASTEROID_BELT, 5f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 5f);
		weights.put(LocationType.IN_RING, 5f);
		weights.put(LocationType.IN_SMALL_NEBULA, 5f);
		weights.put(LocationType.L_POINT, 5f);
		weights.put(LocationType.GAS_GIANT_ORBIT, 5f);
		weights.put(LocationType.NEAR_STAR, 5f);
		weights.put(LocationType.OUTER_SYSTEM, 5f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, system, exclude, gap, weights);
		if (locs.isEmpty()) {
			return pickAnyLocation(random, system, gap, exclude);
		}
		return locs.pick();
	}
	
	public static EntityLocation pickHiddenLocationNotNearStar(Random random, StarSystemAPI system, float gap, Set<SectorEntityToken> exclude) {
		LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
		weights.put(LocationType.IN_ASTEROID_BELT, 5f);
		weights.put(LocationType.IN_ASTEROID_FIELD, 5f);
		weights.put(LocationType.IN_RING, 5f);
		weights.put(LocationType.IN_SMALL_NEBULA, 5f);
		weights.put(LocationType.L_POINT, 5f);
		weights.put(LocationType.GAS_GIANT_ORBIT, 5f);
		weights.put(LocationType.OUTER_SYSTEM, 5f);
		WeightedRandomPicker<EntityLocation> locs = getLocations(random, system, exclude, gap, weights);
		if (locs.isEmpty()) {
			return pickAnyLocation(random, system, gap, exclude);
		}
		return locs.pick();
	}
	
	
	
	
	
	public static WeightedRandomPicker<EntityLocation> getLocations(Random random, StarSystemAPI system,
			float minGap, LinkedHashMap<LocationType, Float> weights) {
		return getLocations(random, system, null, minGap, weights);
	}
	public static WeightedRandomPicker<EntityLocation> getLocations(Random random, StarSystemAPI system, Set<SectorEntityToken> exclude,
																	float minGap, LinkedHashMap<LocationType, Float> weights) {
		WeightedRandomPicker<EntityLocation> result = new WeightedRandomPicker<EntityLocation>(StarSystemGenerator.random);
		
		system.updateAllOrbits();
		
//		if (system.getType() == StarSystemType.TRINARY_1CLOSE_1FAR) {
//			System.out.println("fwfewfwe");
//		}
		
		float inner = getInnerRadius(system);
		float outer = getOuterRadius(system);
		//outer += 1000f;
		if (outer < 3000) outer = 3000;
		if (outer > 25000) outer = 25000;
		
		StarSystemType systemType = system.getType();
		
		for (LocationType type : weights.keySet()) {
			float weight = weights.get(type);
			List<EntityLocation> locs = new ArrayList<EntityLocation>();
			switch (type) {
			case PLANET_ORBIT:
				for (PlanetAPI planet : system.getPlanets()) {
					//if (planet.isMoon()) continue;
					if (planet.isGasGiant()) continue;
					if (planet.isStar()) continue;
					if (exclude != null && exclude.contains(planet)) continue;
					
					float ow = getOrbitalRadius(planet);
					List<OrbitGap> gaps = findGaps(planet, 100f, 100f + ow + minGap, minGap);
					EntityLocation loc = createLocationAtRandomGap(random, planet, gaps, type);
					if (loc != null) locs.add(loc);
				}
				break;
			case L_POINT:
				for (PlanetAPI planet : system.getPlanets()) {
					if (planet.isStar()) continue;
					if (planet.isMoon()) continue;
					if (planet.getRadius() < 100) continue;
					if (planet.getOrbit() == null || planet.getOrbit().getFocus() == null) continue;
					if (planet.getCircularOrbitRadius() <= 0) continue;
					for (LagrangePointType lpt : EnumSet.of(LagrangePointType.L4, LagrangePointType.L5)) {
						float orbitRadius = planet.getCircularOrbitRadius();
						float angleOffset = -StarSystemGenerator.LAGRANGE_OFFSET * 0.5f;
						if (lpt == LagrangePointType.L5) angleOffset = StarSystemGenerator.LAGRANGE_OFFSET * 0.5f;
						float angle = planet.getCircularOrbitAngle() + angleOffset;
						Vector2f location = Misc.getUnitVectorAtDegreeAngle(angle);
						location.scale(orbitRadius);
						Vector2f.add(location, planet.getOrbit().getFocus().getLocation(), location);
						
						boolean clear = isAreaEmpty(system, location);
						if (clear) {
							EntityLocation loc = new EntityLocation();
							loc.type = type;
							float orbitDays = planet.getCircularOrbitPeriod();
//							loc.orbit = Global.getFactory().createCircularOrbit(planet.getOrbitFocus(), 
//																		angle, orbitRadius, orbitDays);
							loc.orbit = Global.getFactory().createCircularOrbitWithSpin(planet.getOrbitFocus(), 
												angle, orbitRadius, orbitDays, StarSystemGenerator.random.nextFloat() * 10f + 1f);
							locs.add(loc);
						}
					}
				}
				break;
			case GAS_GIANT_ORBIT:
				for (PlanetAPI planet : system.getPlanets()) {
					if (planet.isStar()) continue;
					if (!planet.isGasGiant()) continue;
					if (exclude != null && exclude.contains(planet)) continue;
					
					float ow = getOrbitalRadius(planet);
					List<OrbitGap> gaps = findGaps(planet, 100f, 100f + ow + minGap, minGap);
					EntityLocation loc = createLocationAtRandomGap(random, planet, gaps, type);
					if (loc != null) locs.add(loc);
				}
				break;
			case JUMP_ORBIT:
				List<SectorEntityToken> jumpPoints = system.getEntitiesWithTag(Tags.JUMP_POINT);
				for (SectorEntityToken point : jumpPoints) {
					if (exclude != null && exclude.contains(point)) continue;
					List<OrbitGap> gaps = findGaps(point, 200f, 200f + point.getRadius() + minGap, minGap);
					EntityLocation loc = createLocationAtRandomGap(random, point, gaps, type);
					if (loc != null) locs.add(loc);
				}
				break;
			case NEAR_STAR:
				if (systemType != StarSystemType.NEBULA) {
					float r = system.getStar().getRadius();
					float extra = 500f;
					r += extra;
					List<OrbitGap> gaps = findGaps(system.getStar(), 200f, 200f + r + minGap, minGap);
					EntityLocation loc = createLocationAtRandomGap(random, system.getStar(), gaps, type);
					if (loc != null) locs.add(loc);
					
					if (system.getSecondary() != null) {
						r = system.getSecondary().getRadius();
						gaps = findGaps(system.getSecondary(), 200f, 200f + r + minGap, minGap);
						loc = createLocationAtRandomGap(random, system.getSecondary(), gaps, type);
						if (loc != null) locs.add(loc);
					}
					if (system.getTertiary() != null) {
						r = system.getTertiary().getRadius();
						gaps = findGaps(system.getTertiary(), 200f, 200f + r + minGap, minGap);
						loc = createLocationAtRandomGap(random, system.getTertiary(), gaps, type);
						if (loc != null) locs.add(loc);
					}
				}
				break;
			case IN_RING:
				for (CampaignTerrainAPI terrain : system.getTerrainCopy()) {
					if (exclude != null && exclude.contains(terrain)) continue;
					if (terrain.hasTag(Tags.ACCRETION_DISK)) continue;
					CampaignTerrainPlugin plugin = terrain.getPlugin();
					if (plugin instanceof RingSystemTerrainPlugin) {
						RingSystemTerrainPlugin ring = (RingSystemTerrainPlugin) plugin;
						float start = ring.params.middleRadius - ring.params.bandWidthInEngine / 2f;
						List<OrbitGap> gaps = findGaps(terrain, 
											  start - 100f, start + ring.params.bandWidthInEngine + 100f, minGap);
						EntityLocation loc = createLocationAtRandomGap(random, terrain, gaps, type);
						if (loc != null) locs.add(loc);
					}
				}
				break;
			case IN_SMALL_NEBULA:
				for (CampaignTerrainAPI terrain : system.getTerrainCopy()) {
					if (exclude != null && exclude.contains(terrain)) continue;
					CampaignTerrainPlugin plugin = terrain.getPlugin();
					if (plugin instanceof NebulaTerrainPlugin) {
						NebulaTerrainPlugin nebula = (NebulaTerrainPlugin) plugin;
						float tilesHigh = nebula.getTiles()[0].length;
						float tilesWide = nebula.getTiles().length;
						float ts = nebula.getTileSize();
						float w = ts * tilesWide;
						float h = ts * tilesHigh;
						if (w <= 10000) {
							float r = (float) Math.sqrt(w * w + h * h);
							if (terrain.getOrbit() == null) {
								Vector2f point = Misc.getPointWithinRadius(terrain.getLocation(), r * 0.5f, StarSystemGenerator.random);
								EntityLocation loc = new EntityLocation();
								loc.type = type;
								loc.location = point;
								loc.orbit = null;
								locs.add(loc);
							} else {
								float min = Math.min(100f, r * 0.25f);
								float max = r;
								EntityLocation loc = new EntityLocation();
								loc.type = type;
								float orbitRadius = min + (max - min) * (0.75f * StarSystemGenerator.random.nextFloat());
								float orbitDays = orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f);
								loc.orbit = Global.getFactory().createCircularOrbitWithSpin(terrain, 
										StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitDays, StarSystemGenerator.random.nextFloat() * 10f + 1f);
								locs.add(loc);
							}
						}
					}
				}
				break;
			case IN_ASTEROID_BELT:
				for (CampaignTerrainAPI terrain : system.getTerrainCopy()) {
					if (exclude != null && exclude.contains(terrain)) continue;
					CampaignTerrainPlugin plugin = terrain.getPlugin();
					if (plugin instanceof AsteroidBeltTerrainPlugin && !(plugin instanceof AsteroidFieldTerrainPlugin)) {
						AsteroidBeltTerrainPlugin ring = (AsteroidBeltTerrainPlugin) plugin;
						if (ring.params != null) {
							float start = ring.params.middleRadius - ring.params.bandWidthInEngine / 2f;
							List<OrbitGap> gaps = findGaps(terrain, 
												  start - 100f, start + ring.params.bandWidthInEngine + 100f, minGap);
							EntityLocation loc = createLocationAtRandomGap(random, terrain, gaps, type);
							if (loc != null) locs.add(loc);
						} else {
							//System.out.println("egaegfwgwgew");
						}
					}
				}
				break;
			case IN_ASTEROID_FIELD:
				for (CampaignTerrainAPI terrain : system.getTerrainCopy()) {
					if (exclude != null && exclude.contains(terrain)) continue;
					CampaignTerrainPlugin plugin = terrain.getPlugin();
					if (plugin instanceof AsteroidFieldTerrainPlugin) {
						AsteroidFieldTerrainPlugin ring = (AsteroidFieldTerrainPlugin) plugin;
						if (isAreaEmpty(system, terrain.getLocation())) {
							float min = Math.min(100f, ring.params.bandWidthInEngine * 0.25f);
							float max = ring.params.bandWidthInEngine;
							EntityLocation loc = new EntityLocation();
							loc.type = type;
							float orbitRadius = min + (max - min) * (0.75f * StarSystemGenerator.random.nextFloat());
							float orbitDays = orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f);
//							loc.orbit = Global.getFactory().createCircularOrbit(terrain, 
//											StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitDays);
							loc.orbit = Global.getFactory().createCircularOrbitWithSpin(terrain, 
									StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitDays, StarSystemGenerator.random.nextFloat() * 10f + 1f);
							locs.add(loc);
							
						}
					}
				}
				break;
			case OUTER_SYSTEM:
				EntityLocation loc = new EntityLocation();
				loc.type = type;
				float orbitRadius = outer + 300f + 1000f * StarSystemGenerator.random.nextFloat();
				float orbitDays = orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f);
//				loc.orbit = Global.getFactory().createCircularOrbit(system.getCenter(), 
//										StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitDays);
				loc.orbit = Global.getFactory().createCircularOrbitWithSpin(system.getCenter(), 
						StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitDays, StarSystemGenerator.random.nextFloat() * 10f + 1f);
				locs.add(loc);
				break;
			case STAR_ORBIT:
//				if (system.getType() == StarSystemType.TRINARY_1CLOSE_1FAR) {
//					System.out.println("fwfewfwe");
//				}
					
				SectorEntityToken main = system.getCenter();
				List<SectorEntityToken> secondary = new ArrayList<SectorEntityToken>();
				switch (system.getType()) {
				case BINARY_FAR:
					secondary.add(system.getSecondary());
					break;
				case TRINARY_1CLOSE_1FAR:
					secondary.add(system.getTertiary());
					break;
				case TRINARY_2FAR:
					secondary.add(system.getSecondary());
					secondary.add(system.getTertiary());
					break;
				}
				
				if (main != null) {
					List<OrbitGap> gaps = findGaps(main, inner, outer + minGap, minGap);
					for (OrbitGap gap : gaps) {
						loc = createLocationAtGap(main, gap, type);
						if (loc != null) locs.add(loc);
					}
				}
				
				for (SectorEntityToken star : secondary) {
					float ow = getOrbitalRadius((PlanetAPI) star);
					if (ow < 3000) ow = 3000;
					float r = star.getRadius();
					List<OrbitGap> gaps = findGaps(star, r, ow + r + minGap, minGap);
					for (OrbitGap gap : gaps) {
						loc = createLocationAtGap(star, gap, type);
						if (loc != null) locs.add(loc);
					}
				}
				
				break;
			}
			
			// if in nebula, convert circular orbits to fixed locations
			if (system.getType() == StarSystemType.NEBULA) {
				for (EntityLocation loc : locs) {
					if (loc.orbit != null && loc.orbit.getFocus() == system.getCenter()) {
						loc.location = loc.orbit.computeCurrentLocation();
						loc.orbit = null;
					}
				}
			}
			
			if (!locs.isEmpty()) {
				float weightPer = weight / (float) locs.size();
				for (EntityLocation loc : locs) {
					result.add(loc, weightPer);
				}
			}
			
		}
		
		return result;
	}
	
	public static EntityLocation createLocationAtRandomGap(Random random, SectorEntityToken center, float minGap) {
		float ow = getOrbitalRadius(center);
		List<OrbitGap> gaps = findGaps(center, 100f, 100f + ow + minGap, minGap);
		EntityLocation loc = createLocationAtRandomGap(random, center, gaps, LocationType.PLANET_ORBIT);
		return loc;
	}
	
	
	private static EntityLocation createLocationAtRandomGap(Random random, SectorEntityToken center, List<OrbitGap> gaps, LocationType type) {
		if (gaps.isEmpty()) return null;
		WeightedRandomPicker<OrbitGap> picker = new WeightedRandomPicker<OrbitGap>(random);
		picker.addAll(gaps);
		OrbitGap gap = picker.pick();
		return createLocationAtGap(center, gap, type);
	}
	
	private static EntityLocation createLocationAtGap(SectorEntityToken center, OrbitGap gap, LocationType type) {
		if (gap != null) {
			EntityLocation loc = new EntityLocation();
			loc.type = type;
			float orbitRadius = gap.start + (gap.end - gap.start) * (0.25f + 0.5f * StarSystemGenerator.random.nextFloat());
			float orbitDays = orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f);
//			loc.orbit = Global.getFactory().createCircularOrbit(center, 
//					StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitDays);
			loc.orbit = Global.getFactory().createCircularOrbitWithSpin(center, 
					StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitDays, StarSystemGenerator.random.nextFloat() * 10f + 1f);
			return loc;
		}
		return null;
	}
	
	
	public static class OrbitGap {
		public float start;
		public float end;
	}
	
	public static class OrbitItem {
		public SectorEntityToken item;
		public float orbitRadius;
		public float orbitalWidth;
	}
	
	public static List<OrbitGap> findGaps(SectorEntityToken center, float minPad, float maxDist, float minGap) {
		List<OrbitGap> gaps = new ArrayList<OrbitGap>();
		
		LocationAPI loc = center.getContainingLocation();
		if (loc == null) return gaps;
		
		List<OrbitItem> items = new ArrayList<OrbitItem>();
		for (PlanetAPI planet : loc.getPlanets()) {
			if (planet.getOrbitFocus() != center) continue;
			
			OrbitItem item = new OrbitItem();
			item.item = planet;
			item.orbitRadius = planet.getCircularOrbitRadius();
			if (item.orbitRadius > maxDist) continue;
			
			item.orbitalWidth = getOrbitalRadius(planet) * 2f;
			items.add(item);
		}
		
		for (CampaignTerrainAPI terrain : loc.getTerrainCopy()) {
			if (terrain.getOrbitFocus() != center) continue;
			
			CampaignTerrainPlugin plugin = terrain.getPlugin();
			if (plugin instanceof StarCoronaTerrainPlugin) continue;
			if (plugin instanceof MagneticFieldTerrainPlugin) continue;
			if (plugin instanceof PulsarBeamTerrainPlugin) continue;
			
			if (plugin instanceof BaseRingTerrain) {
				BaseRingTerrain ring = (BaseRingTerrain) plugin;
				
				OrbitItem item = new OrbitItem();
				item.item = terrain;
				item.orbitRadius = ring.params.middleRadius;
				if (item.orbitRadius > maxDist) continue;
				
				item.orbitalWidth = ring.params.bandWidthInEngine;
				items.add(item);
			}
		}
		
		List<CustomCampaignEntityAPI> entities = loc.getEntities(CustomCampaignEntityAPI.class);
		for (SectorEntityToken custom : entities) {
			if (custom.getOrbitFocus() != center) continue;
			
			OrbitItem item = new OrbitItem();
			item.item = custom;
			item.orbitRadius = custom.getCircularOrbitRadius();
			if (item.orbitRadius > maxDist) continue;
			
			item.orbitalWidth = custom.getRadius() * 2f;
			items.add(item);
		}
		
		//List<SectorEntityToken> jumpPoints = loc.getEntitiesWithTag(Tags.JUMP_POINT);
		List<SectorEntityToken> jumpPoints = loc.getJumpPoints();
		for (SectorEntityToken point : jumpPoints) {
			if (point.getOrbitFocus() != center) continue;
			
			OrbitItem item = new OrbitItem();
			item.item = point;
			item.orbitRadius = point.getCircularOrbitRadius();
			if (item.orbitRadius > maxDist) continue;
			
			item.orbitalWidth = point.getRadius() * 2f;
			items.add(item);
		}
		
		Collections.sort(items, new Comparator<OrbitItem>() {
			public int compare(OrbitItem o1, OrbitItem o2) {
				return (int)Math.signum(o1.orbitRadius - o2.orbitRadius);
			}
		});
		
		float prev = center.getRadius() + minPad;
		for (OrbitItem item : items) {
			float next = item.orbitRadius - item.orbitalWidth / 2f;
			if (next - prev >= minGap) {
				OrbitGap gap = new OrbitGap();
				gap.start = prev;
				gap.end = next;
				gaps.add(gap);
			}
			prev = Math.max(prev, item.orbitRadius + item.orbitalWidth / 2f);
		}
		
		if (maxDist - prev >= minGap) {
			OrbitGap gap = new OrbitGap();
			gap.start = prev;
			gap.end = maxDist;
			gaps.add(gap);
		}
		
		return gaps;
	}
	
	
	public static float getInnerRadius(StarSystemAPI system) {
		switch (system.getType()) {
		case NEBULA:
			return 0;
		case SINGLE:
		case BINARY_FAR:
		case TRINARY_2FAR:
			return system.getStar().getRadius();
		case BINARY_CLOSE:
		case TRINARY_1CLOSE_1FAR:
			return Math.max(system.getStar().getCircularOrbitRadius() + system.getStar().getRadius(),
							system.getSecondary().getCircularOrbitRadius() + system.getSecondary().getRadius());
		case TRINARY_2CLOSE:
			float max = Math.max(system.getStar().getCircularOrbitRadius() + system.getStar().getRadius(),
							     system.getSecondary().getCircularOrbitRadius() + system.getSecondary().getRadius());
			max = Math.max(max,
						   system.getTertiary().getCircularOrbitRadius() + system.getTertiary().getRadius());
			return max;
		}
		return 0;
	}
	
	public static float getOuterRadius(StarSystemAPI system) {
		float max = 0f;
		
		for (PlanetAPI planet : system.getPlanets()) {
			//float r = planet.getCircularOrbitRadius() + getOrbitalRadius(planet);
			float r = planet.getLocation().length() + getOrbitalRadius(planet);
			if (r > max) max = r;
		}
		
		for (CampaignTerrainAPI terrain : system.getTerrainCopy()) {
			CampaignTerrainPlugin plugin = terrain.getPlugin();
			
			if (plugin instanceof BaseRingTerrain && !(plugin instanceof PulsarBeamTerrainPlugin)) {
				BaseRingTerrain ring = (BaseRingTerrain) plugin;
				float r = ring.params.middleRadius + ring.params.bandWidthInEngine * 0.5f;
				r += Misc.getDistance(system.getCenter().getLocation(), terrain.getLocation());
				if (r > max) max = r;
			} else if (plugin instanceof BaseTiledTerrain) {
				if (plugin instanceof NebulaTerrainPlugin) continue;
				
				BaseTiledTerrain tiles = (BaseTiledTerrain) plugin;
				float r = tiles.getRenderRange();
				r += Misc.getDistance(system.getCenter().getLocation(), terrain.getLocation());
				if (r > max) max = r;
			}
		}
		
		List<CustomCampaignEntityAPI> entities = system.getEntities(CustomCampaignEntityAPI.class);
		for (SectorEntityToken custom : entities) {
			//float r = custom.getCircularOrbitRadius() + custom.getRadius();
			float r = Misc.getDistance(system.getCenter().getLocation(), custom.getLocation());
			r += custom.getRadius();
			if (r > max) max = r;
		}
		
		List<SectorEntityToken> jumpPoints = system.getEntitiesWithTag(Tags.JUMP_POINT);
		for (SectorEntityToken point : jumpPoints) {
			float r = Misc.getDistance(system.getCenter().getLocation(), point.getLocation());
			r += point.getRadius();
			if (r > max) max = r;
		}
		
		return max;
	}
	
	public static boolean isAreaEmpty(LocationAPI loc, Vector2f coords) {
		//loc.updateAllOrbits();
		
		float range = 400f;
		for (PlanetAPI planet : loc.getPlanets()) {
			float dist = Misc.getDistance(planet.getLocation(), coords);
			if (dist < range + planet.getRadius()) return false;
		}
		
		List<CustomCampaignEntityAPI> entities = loc.getEntities(CustomCampaignEntityAPI.class);
		for (SectorEntityToken custom : entities) {
			float dist = Misc.getDistance(custom.getLocation(), coords);
			if (dist < range + custom.getRadius()) {
				return false;
			}
		}
		
		for (CampaignTerrainAPI terrain : loc.getTerrainCopy()) {
			CampaignTerrainPlugin plugin = terrain.getPlugin();
//			if (plugin instanceof PulsarBeamTerrainPlugin) continue;
//			if (plugin instanceof HyperspaceTerrainPlugin) continue;
//			if (plugin instanceof NebulaTerrainPlugin) continue;
//			if (plugin instanceof BaseRingTerrain) {
			if (plugin instanceof DebrisFieldTerrainPlugin) {
				DebrisFieldTerrainPlugin ring = (DebrisFieldTerrainPlugin) plugin;
				float r = ring.params.middleRadius + ring.params.bandWidthInEngine * 0.5f;
				float dist = Misc.getDistance(terrain.getLocation(), coords);
				if (dist < range + r) return false;
			}
		}
		
		return true;
	}
	
	public static float getOrbitalRadius(SectorEntityToken center) {
		LocationAPI loc = center.getContainingLocation();
		if (loc == null) return center.getRadius();
		
		float max = center.getRadius();
		for (PlanetAPI planet : loc.getPlanets()) {
			if (planet.getOrbitFocus() != center) continue;
			float r = planet.getCircularOrbitRadius() + getOrbitalRadius(planet);
			if (r > max) max = r;
		}
		
		for (CampaignTerrainAPI terrain : loc.getTerrainCopy()) {
			if (terrain.getOrbitFocus() != center) continue;
			CampaignTerrainPlugin plugin = terrain.getPlugin();
			
			if (plugin instanceof PulsarBeamTerrainPlugin) continue;
			
			if (plugin instanceof BaseRingTerrain) {
				BaseRingTerrain ring = (BaseRingTerrain) plugin;
				float r = ring.params.middleRadius + ring.params.bandWidthInEngine * 0.5f;
				if (r > max) max = r;
			}
		}
		
		List<CustomCampaignEntityAPI> entities = loc.getEntities(CustomCampaignEntityAPI.class);
		for (SectorEntityToken custom : entities) {
			if (custom.getOrbitFocus() != center) continue;
			float r = custom.getCircularOrbitRadius() + custom.getRadius();
			if (r > max) max = r;
		}
		
		return max;
	}
	
	public static AddedEntity addEntity(StarSystemAPI system, WeightedRandomPicker<EntityLocation> locs, String type, String faction) {
		EntityLocation loc = locs.pickAndRemove();
		return addEntity(system, loc, type, faction);
	}
	
	public static AddedEntity addNonSalvageEntity(StarSystemAPI system, EntityLocation loc, String type, String faction) {
		if (loc != null) {
			SectorEntityToken entity = system.addCustomEntity(null, null, type, faction);
			if (loc.orbit != null) {
				entity.setOrbit(loc.orbit);
				loc.orbit.setEntity(entity);
			} else {
				entity.setOrbit(null);
				entity.getLocation().set(loc.location);
			}
			AddedEntity data = new AddedEntity(entity, loc, type);
			return data;
		}
		return null;
	}
	
	public static AddedEntity addEntity(StarSystemAPI system, EntityLocation loc, String type, String faction) {
		if (loc != null) {
			SectorEntityToken entity = addSalvageEntity(system, type, faction);
			if (loc.orbit != null) {
				entity.setOrbit(loc.orbit);
				loc.orbit.setEntity(entity);
			} else {
				entity.setOrbit(null);
				entity.getLocation().set(loc.location);
			}
			AddedEntity data = new AddedEntity(entity, loc, type);
			return data;
		}
		return null;
	}
	
	public static AddedEntity setEntityLocation(SectorEntityToken entity, EntityLocation loc, String type) {
		if (loc != null) {
			if (loc.orbit != null) {
				entity.setOrbit(loc.orbit);
				loc.orbit.setEntity(entity);
			} else {
				entity.setOrbit(null);
				entity.getLocation().set(loc.location);
			}
			AddedEntity data = new AddedEntity(entity, loc, type);
			return data;
		}
		return null;
	}
	
	public static SectorEntityToken addSalvageEntity(LocationAPI location, String id, String faction) {
		return addSalvageEntity(location, id, faction, null);
	}
	public static SectorEntityToken addSalvageEntity(LocationAPI location, String id, String faction, Object pluginParams) {
		SalvageEntityGenDataSpec spec = SalvageEntityGeneratorOld.getSalvageSpec(id);
		
		CustomCampaignEntityAPI entity = location.addCustomEntity(null, spec.getNameOverride(), id, faction, pluginParams);
		
		if (spec.getRadiusOverride() > 0) {
			entity.setRadius(spec.getRadiusOverride());
		}
		
		switch (spec.getType()) {
		case ALWAYS_VISIBLE:
			entity.setSensorProfile(null);
			entity.setDiscoverable(null);
			break;
		case DISCOVERABLE:
			entity.setSensorProfile(1f);
			entity.setDiscoverable(true);
			break;
		case NOT_DISCOVERABLE:
			entity.setSensorProfile(1f);
			entity.setDiscoverable(false);
			break;
		}
		
		long seed = StarSystemGenerator.random.nextLong();
		entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
		
		entity.getDetectedRangeMod().modifyFlat("gen", spec.getDetectionRange());
		
		return entity;
	}
	
	
	public static CargoAPI genCargoFromDrop(SectorEntityToken entity) {
		MemoryAPI memory = entity.getMemoryWithoutUpdate();
		long seed = memory.getLong(MemFlags.SALVAGE_SEED);
		Random random = Misc.getRandom(seed, 1);
		
		List<DropData> dropValue = new ArrayList<DropData>(entity.getDropValue());
		List<DropData> dropRandom = new ArrayList<DropData>(entity.getDropRandom());
		SalvageEntityGenDataSpec spec = (SalvageEntityGenDataSpec) Global.getSettings().getSpec(
									SalvageEntityGenDataSpec.class, entity.getCustomEntityType(), true);
		
		if (spec != null) {
			dropValue.addAll(spec.getDropValue());
			dropRandom.addAll(spec.getDropRandom());
		}
		
		CargoAPI salvage = SalvageEntity.generateSalvage(random, 1f, 1f, 1f, 1f, dropValue, dropRandom);
		return salvage;
	}
	
	
	public static StarSystemData computeSystemData(StarSystemAPI system) {
		StarSystemData data = new StarSystemData();
		data.system = system;
		
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) {
				data.stars.add(planet);
			} else {
				data.planets.add(planet);
			}
			
			if (planet.isGasGiant()) {
				data.gasGiants.add(planet);
			}
			
			if (planet.getMarket() != null && planet.getMarket().isPlanetConditionMarketOnly()) {
				MarketAPI market = planet.getMarket();
				if (market.hasCondition(Conditions.HABITABLE)) {
					data.habitable.add(planet);
				}
				
				for (String conditionId : DerelictThemeGenerator.interestingConditionsWithoutHabitable) {
					if (market.hasCondition(conditionId)) {
						data.resourceRich.add(planet);
						break;
					}
				}
				
			}
		}
		
		return data;
	}
	
	public static void clearRuins(MarketAPI market) {
		if (market == null) return;
		
		market.removeCondition(Conditions.RUINS_EXTENSIVE);
		market.removeCondition(Conditions.RUINS_SCATTERED);
		market.removeCondition(Conditions.RUINS_VAST);
		market.removeCondition(Conditions.RUINS_WIDESPREAD);
		market.removeCondition(Conditions.DECIVILIZED);
	}
	
	public static void convertOrbitPointingDown(SectorEntityToken entity) {
		SectorEntityToken focus = entity.getOrbitFocus();
		if (focus != null) {
			float angle = entity.getCircularOrbitAngle();
			float period = entity.getCircularOrbitPeriod();
			float radius = entity.getCircularOrbitRadius();
			entity.setCircularOrbitPointingDown(focus, angle, radius, period);
		}
	}
	
	public static void convertOrbitNoSpin(SectorEntityToken entity) {
		convertOrbitNoSpin(entity, 90f);
	}
	public static void convertOrbitNoSpin(SectorEntityToken entity, float facing) {
		SectorEntityToken focus = entity.getOrbitFocus();
		if (focus != null) {
			float angle = entity.getCircularOrbitAngle();
			float period = entity.getCircularOrbitPeriod();
			float radius = entity.getCircularOrbitRadius();
			entity.setCircularOrbit(focus, angle, radius, period);
			entity.setFacing(facing);
		}
	}
	
	public static void convertOrbitWithSpin(SectorEntityToken entity, float spin) {
		SectorEntityToken focus = entity.getOrbitFocus();
		if (focus != null) {
			float angle = entity.getCircularOrbitAngle();
			float period = entity.getCircularOrbitPeriod();
			float radius = entity.getCircularOrbitRadius();
			entity.setCircularOrbitWithSpin(focus, angle, radius, period, spin, spin);
			((CircularOrbitWithSpinAPI) entity.getOrbit()).setSpinVel(spin);
		}
	}
	
	
	public Random getRandom() {
		return random;
	}
	public void setRandom(Random random) {
		this.random = random;
	}
	
	
}









