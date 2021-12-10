package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NameAssigner;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager.RemnantFleetInteractionConfigGen;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.SpecialCreationContext;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;


public class RemnantThemeGenerator extends BaseThemeGenerator {

	public static enum RemnantSystemType {
		DESTROYED(Tags.THEME_REMNANT_DESTROYED, "$remnantDestroyed"),
		SUPPRESSED(Tags.THEME_REMNANT_SUPPRESSED, "$remnantSuppressed"),
		RESURGENT(Tags.THEME_REMNANT_RESURGENT, "$remnantResurgent"),
		;
		
		private String tag;
		private String beaconFlag;
		private RemnantSystemType(String tag, String beaconFlag) {
			this.tag = tag;
			this.beaconFlag = beaconFlag;
		}
		public String getTag() {
			return tag;
		}
		public String getBeaconFlag() {
			return beaconFlag;
		}
	}
	
	
	public static final int MIN_CONSTELLATIONS_WITH_REMNANTS = 15;
	public static final int MAX_CONSTELLATIONS_WITH_REMNANTS = 25;
	
	public static float CONSTELLATION_SKIP_PROB = 0.25f;
	
	
	public String getThemeId() {
		return Themes.REMNANTS;
	}

	@Override
	public void generateForSector(ThemeGenContext context, float allowedUnusedFraction) {
		
		float total = (float) (context.constellations.size() - context.majorThemes.size()) * allowedUnusedFraction;
		if (total <= 0) return;
		
		int num = (int) StarSystemGenerator.getNormalRandom(MIN_CONSTELLATIONS_WITH_REMNANTS, MAX_CONSTELLATIONS_WITH_REMNANTS);
		//num = 30;
		if (num > total) num = (int) total;
		
		
		int numDestroyed = (int) (num * (0.23f + 0.1f * random.nextFloat()));
		if (numDestroyed < 1) numDestroyed = 1;
		int numSuppressed = (int) (num * (0.23f + 0.1f * random.nextFloat()));
		if (numSuppressed < 1) numSuppressed = 1;
		
		float suppressedStationMult = 0.5f;
		int suppressedStations = (int) Math.ceil(numSuppressed * suppressedStationMult);
		
		WeightedRandomPicker<Boolean> addSuppressedStation = new WeightedRandomPicker<Boolean>(random);
		for (int i = 0; i < numSuppressed; i++) {
			if (i < suppressedStations) {
				addSuppressedStation.add(true, 1f);
			} else {
				addSuppressedStation.add(false, 1f);
			}
		}
		
		List<Constellation> constellations = getSortedAvailableConstellations(context, false, new Vector2f(), null);
		Collections.reverse(constellations);
		
		float skipProb = CONSTELLATION_SKIP_PROB;
		if (total < num / (1f - skipProb)) {
			skipProb = 1f - (num / total);
		}
		//skipProb = 0f;

		List<StarSystemData> remnantSystems = new ArrayList<StarSystemData>();
		
		if (DEBUG) System.out.println("\n\n\n");
		if (DEBUG) System.out.println("Generating remnant systems");
		
		int count = 0;
		
		int numUsed = 0;
		for (int i = 0; i < num && i < constellations.size(); i++) {
			Constellation c = constellations.get(i);
			if (random.nextFloat() < skipProb) {
				if (DEBUG) System.out.println("Skipping constellation " + c.getName());
				continue;
			}
			
			
			List<StarSystemData> systems = new ArrayList<StarSystemData>();
			for (StarSystemAPI system : c.getSystems()) {
				StarSystemData data = computeSystemData(system);
				systems.add(data);
			}
			
			List<StarSystemData> mainCandidates = getSortedSystemsSuitedToBePopulated(systems);
			
			int numMain = 1 + random.nextInt(2);
			if (numMain > mainCandidates.size()) numMain = mainCandidates.size();
			if (numMain <= 0) {
				if (DEBUG) System.out.println("Skipping constellation " + c.getName() + ", no suitable main candidates");
				continue;
			}
			
			RemnantSystemType type = RemnantSystemType.RESURGENT;
			if (numUsed < numDestroyed) {
				type = RemnantSystemType.DESTROYED;
			} else if (numUsed < numDestroyed + numSuppressed) {
				type = RemnantSystemType.SUPPRESSED;
			}
			
			context.majorThemes.put(c, Themes.REMNANTS);
			numUsed++;

			if (DEBUG) System.out.println("Generating " + numMain + " main systems in " + c.getName());
			for (int j = 0; j < numMain; j++) {
				StarSystemData data = mainCandidates.get(j);
				populateMain(data, type);
				
				data.system.addTag(Tags.THEME_INTERESTING);
				data.system.addTag(Tags.THEME_REMNANT);
				if (type != RemnantSystemType.DESTROYED) {
					data.system.addTag(Tags.THEME_UNSAFE);
				}
				data.system.addTag(Tags.THEME_REMNANT_MAIN);
				data.system.addTag(type.getTag());
				remnantSystems.add(data);

				if (!NameAssigner.isNameSpecial(data.system)) {
					NameAssigner.assignSpecialNames(data.system);
				}
				
				
				if (type == RemnantSystemType.DESTROYED) {
					RemnantSeededFleetManager fleets = new RemnantSeededFleetManager(data.system, 3, 8, 1, 2, 0.05f);
					data.system.addScript(fleets);
				} else if (type == RemnantSystemType.SUPPRESSED) {
					RemnantSeededFleetManager fleets = new RemnantSeededFleetManager(data.system, 7, 12, 4, 12, 0.25f);
					data.system.addScript(fleets);

					Boolean addStation = random.nextFloat() < suppressedStationMult; 
					if (j == 0 && !addSuppressedStation.isEmpty()) addSuppressedStation.pickAndRemove();
					if (addStation) {
						List<CampaignFleetAPI> stations = addBattlestations(data, 1f, 1, 1, createStringPicker("remnant_station2_Damaged", 10f));
						for (CampaignFleetAPI station : stations) {
							int maxFleets = 2 + random.nextInt(3);
							RemnantStationFleetManager activeFleets = new RemnantStationFleetManager(
									station, 1f, 0, maxFleets, 25f, 6, 12);
							data.system.addScript(activeFleets);
						}
						
					}
				} else if (type == RemnantSystemType.RESURGENT) {
					List<CampaignFleetAPI> stations = addBattlestations(data, 1f, 1, 1, createStringPicker("remnant_station2_Standard", 10f));
					for (CampaignFleetAPI station : stations) {
						int maxFleets = 8 + random.nextInt(5);
						RemnantStationFleetManager activeFleets = new RemnantStationFleetManager(
								station, 1f, 0, maxFleets, 15f, 8, 24);
						data.system.addScript(activeFleets);
					}
				}
			}
			
			for (StarSystemData data : systems) {
				int index = mainCandidates.indexOf(data);
				if (index >= 0 && index < numMain) continue;
				
				populateNonMain(data);
				
				if (type == RemnantSystemType.DESTROYED) {
					data.system.addTag(Tags.THEME_INTERESTING_MINOR);
				} else {
					data.system.addTag(Tags.THEME_INTERESTING);
				}
				data.system.addTag(Tags.THEME_REMNANT);
				//data.system.addTag(Tags.THEME_UNSAFE); // just a few 1-2 frigate fleets, and not even always
				data.system.addTag(Tags.THEME_REMNANT_SECONDARY);
				data.system.addTag(type.getTag());
				remnantSystems.add(data);
				
				if (random.nextFloat() < 0.5f) {
					RemnantSeededFleetManager fleets = new RemnantSeededFleetManager(data.system, 1, 3, 1, 2, 0.05f);
					data.system.addScript(fleets);
				} else {
					data.system.addTag(Tags.THEME_REMNANT_NO_FLEETS);
				}
			}
			
//			if (count == 18) {
//				System.out.println("REM RANDOM INDEX " + count + ": " + random.nextLong());
//			}
			count++;
		}
		
		SpecialCreationContext specialContext = new SpecialCreationContext();
		specialContext.themeId = getThemeId();
		SalvageSpecialAssigner.assignSpecials(remnantSystems, specialContext);
		
		addDefenders(remnantSystems);
		
		if (DEBUG) System.out.println("Finished generating remnant systems\n\n\n\n\n");
		
	}
	

	
	public void addDefenders(List<StarSystemData> systemData) {
		for (StarSystemData data : systemData) {
//			float prob = 0.1f;
//			float max = 3f;
//			if (data.system.hasTag(Tags.THEME_REMNANT_SECONDARY)) {
//				prob = 0.05f;
//				max = 1f;
//			}
			float mult = 1f;
			if (data.system.hasTag(Tags.THEME_REMNANT_SECONDARY)) {
				mult = 0.5f;
			}
			
			for (AddedEntity added : data.generated) {
				if (added.entityType == null) continue;
				if (Entities.WRECK.equals(added.entityType)) continue;
				
				float prob = 0f;
				float min = 1f;
				float max = 1f;
				if (Entities.STATION_MINING_REMNANT.equals(added.entityType)) {
					prob = 0.25f;
					min = 8;
					max = 15;
				} else if (Entities.ORBITAL_HABITAT_REMNANT.equals(added.entityType)) {
					prob = 0.25f;
					min = 8;
					max = 15;
				} else if (Entities.STATION_RESEARCH_REMNANT.equals(added.entityType)) {
					prob = 0.25f;
					min = 10;
					max = 20;
				}
				// to compensate for this being changed to use fleet points
				min *= 3;
				max *= 3;
				
				prob *= mult;
				min *= mult;
				max *= mult;
				if (min < 1) min = 1;
				if (max < 1) max = 1;
				
				if (random.nextFloat() < prob) {
					Misc.setDefenderOverride(added.entity, new DefenderDataOverride(Factions.REMNANTS, 1f, min, max, 4));
				}
				//Misc.setDefenderOverride(added.entity, new DefenderDataOverride(Factions.REMNANTS, prob, 1, max));
			}
		}
		
	}
	
	public void populateNonMain(StarSystemData data) {
		if (DEBUG) System.out.println(" Generating secondary remnant system in " + data.system.getName());
		boolean special = data.isBlackHole() || data.isNebula() || data.isPulsar();
		if (special) {
			addResearchStations(data, 0.75f, 1, 1, createStringPicker(Entities.STATION_RESEARCH_REMNANT, 10f));
		}
		
		if (random.nextFloat() < 0.5f) return;
		
		if (!data.resourceRich.isEmpty()) {
			addMiningStations(data, 0.5f, 1, 1, createStringPicker(Entities.STATION_MINING_REMNANT, 10f));
		}
		
		if (!special && !data.habitable.isEmpty()) {
			// ruins on planet, or orbital station
			addHabCenters(data, 0.25f, 1, 1, createStringPicker(Entities.ORBITAL_HABITAT_REMNANT, 10f));
		}
		
		
		addShipGraveyard(data, 0.05f, 1, 1,
				createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));
		
		//addDebrisFields(data, 0.25f, 1, 2, Factions.REMNANTS, 0.1f, 1, 1);
		addDebrisFields(data, 0.25f, 1, 2);

		addDerelictShips(data, 0.5f, 0, 3, 
				createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));
		
		addCaches(data, 0.25f, 0, 2, createStringPicker( 
				Entities.WEAPONS_CACHE_REMNANT, 4f,
				Entities.WEAPONS_CACHE_SMALL_REMNANT, 10f,
				Entities.SUPPLY_CACHE, 4f,
				Entities.SUPPLY_CACHE_SMALL, 10f,
				Entities.EQUIPMENT_CACHE, 4f,
				Entities.EQUIPMENT_CACHE_SMALL, 10f
				));
		
	}
	
	
	public void populateMain(StarSystemData data, RemnantSystemType type) {
		
		if (DEBUG) System.out.println(" Generating remnant center in " + data.system.getName());
		
		StarSystemAPI system = data.system;
		
		addBeacon(system, type);
		
		if (DEBUG) System.out.println("    Added warning beacon");
		
		int maxHabCenters = 1 + random.nextInt(3);
		
		HabitationLevel level = HabitationLevel.LOW;
		if (maxHabCenters == 2) level = HabitationLevel.MEDIUM;
		if (maxHabCenters >= 3) level = HabitationLevel.HIGH;

		addHabCenters(data, 1, maxHabCenters, maxHabCenters, createStringPicker(Entities.ORBITAL_HABITAT_REMNANT, 10f));
		
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
		
		//addCommRelay(data, probRelay);
		addObjectives(data, probRelay);
		addInactiveGate(data, probGate, 0.5f, 0.5f, 
				createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));
		
		addShipGraveyard(data, 0.25f, 1, 1,
				createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));
		
		addMiningStations(data, probMining, 1, 1, createStringPicker(Entities.STATION_MINING_REMNANT, 10f));
		
		addResearchStations(data, probResearch, 1, 1, createStringPicker(Entities.STATION_RESEARCH_REMNANT, 10f));
		
		
		//addDebrisFields(data, 0.75f, 1, 5, Factions.REMNANTS, 0.2f, 1, 3);
		addDebrisFields(data, 0.75f, 1, 5);

		
//		MN-6186477243757813340		
//		float test = Misc.getDistance(data.system.getLocation(), new Vector2f(-33500, 9000));
//		if (test < 600) {
//			System.out.println("HERE");
//		}
		
		addDerelictShips(data, 0.75f, 0, 7, 
				createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));
		
		addCaches(data, 0.75f, 0, 3, createStringPicker( 
				Entities.WEAPONS_CACHE_REMNANT, 10f,
				Entities.WEAPONS_CACHE_SMALL_REMNANT, 10f,
				Entities.SUPPLY_CACHE, 10f,
				Entities.SUPPLY_CACHE_SMALL, 10f,
				Entities.EQUIPMENT_CACHE, 10f,
				Entities.EQUIPMENT_CACHE_SMALL, 10f
				));
		
	}
	
	
	
	public List<StarSystemData> getSortedSystemsSuitedToBePopulated(List<StarSystemData> systems) {
		List<StarSystemData> result = new ArrayList<StarSystemData>();
		
		for (StarSystemData data : systems) {
			if (data.isBlackHole() || data.isNebula() || data.isPulsar()) continue;
			
			if (data.planets.size() >= 4 || data.habitable.size() >= 1) {
				result.add(data);
				
//				Collections.sort(data.habitable, new Comparator<PlanetAPI>() {
//					public int compare(PlanetAPI o1, PlanetAPI o2) {
//						return (int) Math.signum(o1.getMarket().getHazardValue() - o2.getMarket().getHazardValue());
//					}
//				});
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
	
	
	public static CustomCampaignEntityAPI addBeacon(StarSystemAPI system, RemnantSystemType type) {
		
		SectorEntityToken anchor = system.getHyperspaceAnchor();
		List<SectorEntityToken> points = Global.getSector().getHyperspace().getEntities(JumpPointAPI.class);
		
		float minRange = 600;
		
		float closestRange = Float.MAX_VALUE;
		JumpPointAPI closestPoint = null;
		for (SectorEntityToken entity : points) {
			JumpPointAPI point = (JumpPointAPI) entity;
			
			if (point.getDestinations().isEmpty()) continue;
			
			JumpDestination dest = point.getDestinations().get(0);
			if (dest.getDestination().getContainingLocation() != system) continue;
			
			float dist = Misc.getDistance(anchor.getLocation(), point.getLocation());
			if (dist < minRange + point.getRadius()) continue;
			
			if (dist < closestRange) {
				closestPoint = point;
				closestRange = dist;
			}
		}
		
		CustomCampaignEntityAPI beacon = Global.getSector().getHyperspace().addCustomEntity(null, null, Entities.WARNING_BEACON, Factions.NEUTRAL);
		//beacon.getMemoryWithoutUpdate().set("$remnant", true);
		beacon.getMemoryWithoutUpdate().set(type.getBeaconFlag(), true);
		
		switch (type) {
		case DESTROYED: beacon.addTag(Tags.BEACON_LOW); break;
		case SUPPRESSED: beacon.addTag(Tags.BEACON_MEDIUM); break;
		case RESURGENT: beacon.addTag(Tags.BEACON_HIGH); break;
		}
		
		if (closestPoint == null) {
			float orbitDays = minRange / (10f + StarSystemGenerator.random.nextFloat() * 5f);
			//beacon.setCircularOrbit(anchor, StarSystemGenerator.random.nextFloat() * 360f, minRange, orbitDays);
			beacon.setCircularOrbitPointingDown(anchor, StarSystemGenerator.random.nextFloat() * 360f, minRange, orbitDays);
		} else {
			float angleOffset = 20f + StarSystemGenerator.random.nextFloat() * 20f;
			float angle = Misc.getAngleInDegrees(anchor.getLocation(), closestPoint.getLocation()) + angleOffset;
			float radius = closestRange;
			
			if (closestPoint.getOrbit() != null) {
//				OrbitAPI orbit = Global.getFactory().createCircularOrbit(anchor, angle, radius, 
//																closestPoint.getOrbit().getOrbitalPeriod()); 
				OrbitAPI orbit = Global.getFactory().createCircularOrbitPointingDown(anchor, angle, radius, 
						closestPoint.getOrbit().getOrbitalPeriod()); 
				beacon.setOrbit(orbit);
			} else {
				Vector2f beaconLoc = Misc.getUnitVectorAtDegreeAngle(angle);
				beaconLoc.scale(radius);
				Vector2f.add(beaconLoc, anchor.getLocation(), beaconLoc);
				beacon.getLocation().set(beaconLoc);
			}
		}
		
		Color glowColor = new Color(255,200,0,255);
		Color pingColor = new Color(255,200,0,255);
		if (type == RemnantSystemType.SUPPRESSED) {
			glowColor = new Color(250,155,0,255);
			pingColor = new Color(250,155,0,255);
		} else if (type == RemnantSystemType.RESURGENT) {
			glowColor = new Color(250,55,0,255);
			pingColor = new Color(250,125,0,255);
		}
		Misc.setWarningBeaconColors(beacon, glowColor, pingColor);
		
		
		return beacon;
	}
	
//	Map<LocationType, Float> weights = new HashMap<LocationType, Float>();
//	weights.put(LocationType.PLANET_ORBIT, 10f);
//	weights.put(LocationType.JUMP_ORBIT, 1f);
//	weights.put(LocationType.NEAR_STAR, 1f);
//	weights.put(LocationType.OUTER_SYSTEM, 5f);
//	weights.put(LocationType.IN_ASTEROID_BELT, 10f);
//	weights.put(LocationType.IN_RING, 10f);
//	weights.put(LocationType.IN_ASTEROID_FIELD, 10f);
//	weights.put(LocationType.STAR_ORBIT, 1f);
//	weights.put(LocationType.IN_SMALL_NEBULA, 1f);
//	weights.put(LocationType.L_POINT, 1f);
//	WeightedRandomPicker<EntityLocation> locs = getLocations(system, 100f, weights);
	
	
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
	
	
	
	public List<CampaignFleetAPI> addBattlestations(StarSystemData data, float chanceToAddAny, int min, int max, 
												WeightedRandomPicker<String> stationTypes) {
		List<CampaignFleetAPI> result = new ArrayList<CampaignFleetAPI>();
		if (random.nextFloat() >= chanceToAddAny) return result;
		
		int num = min + random.nextInt(max - min + 1);
		if (DEBUG) System.out.println("    Adding " + num + " battlestations");
		for (int i = 0; i < num; i++) {
			
			EntityLocation loc = pickCommonLocation(random, data.system, 200f, true, null);
			
			String type = stationTypes.pick();
			if (loc != null) {
				
				CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.REMNANTS, FleetTypes.BATTLESTATION, null);
				
				FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, type);
				fleet.getFleetData().addFleetMember(member);
				
				//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
				fleet.addTag(Tags.NEUTRINO_HIGH);
				
				fleet.setStationMode(true);
				
				addRemnantStationInteractionConfig(fleet);
				
				data.system.addEntity(fleet);
				
				//fleet.setTransponderOn(true);
				fleet.clearAbilities();
				fleet.addAbility(Abilities.TRANSPONDER);
				fleet.getAbility(Abilities.TRANSPONDER).activate();
				fleet.getDetectedRangeMod().modifyFlat("gen", 1000f);
				
				fleet.setAI(null);
				
				setEntityLocation(fleet, loc, null);
				convertOrbitWithSpin(fleet, 5f);
				
				boolean damaged = type.toLowerCase().contains("damaged");
				String coreId = Commodities.ALPHA_CORE;
				if (damaged) {
					// alpha for both types; damaged is already weaker
					//coreId = Commodities.BETA_CORE;
					fleet.getMemoryWithoutUpdate().set("$damagedStation", true);
					fleet.setName(fleet.getName() + " (Damaged)");
				}
					
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(coreId);
				PersonAPI commander = plugin.createPerson(coreId, fleet.getFaction().getId(), random);
				
				fleet.setCommander(commander);
				fleet.getFlagship().setCaptain(commander);
				
				if (!damaged) {
					RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(fleet.getFlagship());
					RemnantOfficerGeneratorPlugin.addCommanderSkills(commander, fleet, null, 3, random);
				}
				
				member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
				
				
				//RemnantSeededFleetManager.addRemnantAICoreDrops(random, fleet, mult);
				
				result.add(fleet);
				
//				MarketAPI market = Global.getFactory().createMarket("station_market_" + fleet.getId(), fleet.getName(), 0);
//				market.setPrimaryEntity(fleet);
//				market.setFactionId(fleet.getFaction().getId());
//				market.addCondition(Conditions.ABANDONED_STATION);
//				market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
//				((StoragePlugin)market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
//				fleet.setMarket(market);
				
			}
		}
		
		return result;
	}
	
	public static void addRemnantStationInteractionConfig(CampaignFleetAPI fleet) {
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, 
				   new RemnantStationInteractionConfigGen());		
	}
	
	
	@Override
	public int getOrder() {
		return 1500;
	}


	public static class RemnantStationInteractionConfigGen implements FIDConfigGen {
		public FIDConfig createConfig() {
			FIDConfig config = new FIDConfig();
			
			config.alwaysAttackVsAttack = true;
			config.leaveAlwaysAvailable = true;
			config.showFleetAttitude = false;
			config.showTransponderStatus = false;
			config.showEngageText = false;
			
			
			config.delegate = new BaseFIDDelegate() {
				public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
					new RemnantFleetInteractionConfigGen().createConfig().delegate.
								postPlayerSalvageGeneration(dialog, context, salvage);
				}
				public void notifyLeave(InteractionDialogAPI dialog) {
				}
				public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
					bcc.aiRetreatAllowed = false;
					bcc.objectivesAllowed = false;
				}
			};
			return config;
		}
	}
	
	
}
















