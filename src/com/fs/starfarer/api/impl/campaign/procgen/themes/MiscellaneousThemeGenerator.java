package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.PlanetaryShield;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NameGenData;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames.NamePick;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.SpecialCreationContext;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;


public class MiscellaneousThemeGenerator extends BaseThemeGenerator {
	
	public static String PK_SYSTEM_KEY = "$core_pkSystem";
	public static String PK_PLANET_KEY = "$core_pkPlanet";
	public static String PK_CACHE_KEY = "$core_pkCache";
	public static String PK_NEXUS_KEY = "$core_pkNexus";
	
	public static String PLANETARY_SHIELD_PLANET_KEY = "$core_planetaryShieldPlanet";
	public static String PLANETARY_SHIELD_PLANET = "$psi_planet";
	
	
	public static float PROB_TO_ADD_SOMETHING = 0.5f;
	
	public static int MIN_GATES = Global.getSettings().getInt("minNonCoreGatesInSector");
	public static int MAX_GATES = Global.getSettings().getInt("maxNonCoreGatesInSector");
	public static int MIN_GATES_TO_ADD = Global.getSettings().getInt("minGatesToAddOnSecondPass");
	
	
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
		//getSortedAvailableConstellations(context, true, new Vector2f(), null).size()
		List<StarSystemData> all = new ArrayList<StarSystemData>();
		
		/* this adds misc stuff to systems that are:
			1) Not tagged with some other theme
			2) But does add misc stuff to derelict-tagged systems
			So, basicallly it covers:
			 	derelict theme + whatever few constellations didn't get anything from any theme
		*/
		for (Constellation c : context.constellations) {
			String theme = context.majorThemes.get(c);
			
			List<StarSystemData> systems = new ArrayList<StarSystemData>();
			for (StarSystemAPI system : c.getSystems()) {
				StarSystemData data = computeSystemData(system);
				systems.add(data);
			}
			
			for (StarSystemData data  : systems) {
				//if (data.system.getName().toLowerCase().contains("alpha mok morred")) {
//				if (data.system.getName().toLowerCase().contains("vasuki")) {
//					System.out.println("efwefwef");
//				}
				boolean derelict = data.system.hasTag(Tags.THEME_DERELICT);
				if (!derelict && theme != null && !data.system.getTags().isEmpty()) continue;
//				if (!derelict && theme != null && !theme.equals(Themes.DERELICTS) &&
//						!theme.equals(Themes.NO_THEME)) continue;
				
				if (random.nextFloat() > PROB_TO_ADD_SOMETHING || (derelict && theme != null)) {
					data.system.addTag(Tags.THEME_MISC_SKIP);
					continue;
				}

				populateNonMain(data);
				all.add(data);
				data.system.addTag(Tags.THEME_MISC);
				data.system.addTag(Tags.THEME_INTERESTING_MINOR);
			}
		}
		
		
		SpecialCreationContext specialContext = new SpecialCreationContext();
		specialContext.themeId = getThemeId();
		SalvageSpecialAssigner.assignSpecials(all, specialContext);
		
		if (DEBUG) System.out.println("Finished generating misc derelicts\n\n\n\n\n");
		
		
		addDerelicts(context, "legion_xiv_Elite", 2, 3, 1, 2, Tags.THEME_REMNANT);
		addDerelicts(context, "phantom_Elite", 1, 2, 0, 1, Tags.THEME_REMNANT, Tags.THEME_RUINS, Tags.THEME_DERELICT, Tags.THEME_UNSAFE);
		addDerelicts(context, "revenant_Elite", 1, 2, 0, 1, Tags.THEME_REMNANT, Tags.THEME_RUINS, Tags.THEME_DERELICT, Tags.THEME_UNSAFE);
		
		
		addRedPlanet(context);
		addPKSystem(context);
		
		addSolarShadesAndMirrors(context);
		
		addCoronalTaps(context);
		
		addExtraGates(context);
	}
	
	protected void addRedPlanet(ThemeGenContext context) {
		if (DEBUG) System.out.println("Looking for planetary shield planet");
		
		PlanetAPI bestHab = null;
		PlanetAPI bestNonHab = null;
//		OrbitGap gapHab = null;
//		OrbitGap gapNonHab = null;
		float habDist = 0;
		float nonHabDist = 0;

		// looking for a habitable planet furthest from the Sector's center, with a bit of 
		// a random factor
		int systemsChecked = 0;
		for (Constellation c : context.constellations) {
			for (StarSystemAPI system : c.getSystems()) {
				if (system.hasTag(Tags.THEME_SPECIAL)) continue;
				
				if (!system.hasTag(Tags.THEME_MISC_SKIP) && 
						!system.hasTag(Tags.THEME_MISC)) {
					continue;
				}
				//[theme_derelict, theme_derelict_probes, theme_misc_skip, theme_derelict_survey_ship]
				if (system.hasTag(Tags.THEME_DERELICT)) {
					continue;
				}
				
				systemsChecked++;
				
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
			planet.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
			//planet.addTag(Tags.SALVAGEABLE);
			
//			SectorEntityToken beacon = Misc.addWarningBeacon(planet, gap, Tags.BEACON_HIGH);
//			beacon.getMemoryWithoutUpdate().set(PLANETARY_SHIELD_BEACON, true);
//			beacon.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
			
		} else {
			if (DEBUG) System.out.println("Failed to find a planet in remnant systems");
		}
		if (DEBUG) System.out.println("Finished adding Planetary Shield planet\n\n\n\n\n");
	}
	
	protected void addDerelicts(ThemeGenContext context, String variant,
					int minNonSalvageable, int maxNonSalvageable, 
					int minSalvageable, int maxSalvageable, 
					String ... allowedThemes) {
		if (Global.getSettings().getVariant(variant) != null) {
			if (DEBUG) System.out.println("Adding " + variant + " to star systems");
			
			Set<String> tags = new HashSet<String>(Arrays.asList(allowedThemes));
			
			int numSalvageable = minSalvageable + random.nextInt(maxSalvageable - minSalvageable + 1);
			int numNonSalvageable = minNonSalvageable + random.nextInt(maxNonSalvageable - minNonSalvageable + 1);
			
			List<Constellation> list = new ArrayList<Constellation>(context.constellations);
			Collections.shuffle(list, random);
			
			List<StarSystemData> systems = new ArrayList<StarSystemData>();
			for (Constellation c : list) {
				for (StarSystemAPI system : c.getSystems()) {
					StarSystemData data = computeSystemData(system);
					systems.add(data);
				}
			}
				
			Collections.shuffle(systems, random);
			for (StarSystemData data  : systems) {
				boolean matches = false;
				for (String tag : data.system.getTags()) {
					if (tags.contains(tag)) {
						matches = true;
						break;
					}
				}
				if (!matches) continue;

				EntityLocation loc = pickAnyLocation(random, data.system, 70f, null);
				AddedEntity ae = addDerelictShip(data, loc, variant);
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
						SalvageSpecialAssigner.assignSpecials(ae.entity, true);
					}
					if (DEBUG) System.out.println("      Added " + variant + " to " + data.system + "\n");
				}
				if (numSalvageable + numNonSalvageable <= 0) break;
			}
			//if (numSalvageable + numNonSalvageable <= 0) break;
			
			if (DEBUG) System.out.println("Finished adding " + variant + " to star systems\n\n\n\n\n");
		}
	}
	
	
	protected void addSolarShadesAndMirrors(ThemeGenContext context) {
		
		int num = 2 + random.nextInt(3);
		
		if (DEBUG) System.out.println("Adding up to " + num + " solar shades and mirrors");
		List<Constellation> list = new ArrayList<Constellation>(context.constellations);
		WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);
		for (Constellation c : list) {
			for (StarSystemAPI system : c.getSystems()) {
				if (system.hasTag(Tags.THEME_CORE)) continue;
				if (system.isNebula()) continue;
				
				for (PlanetAPI planet : system.getPlanets()) {
					if (planet.isStar()) continue;
					
					SectorEntityToken focus = planet.getOrbitFocus();
					if (!(focus instanceof PlanetAPI)) continue;
					if (!((PlanetAPI) focus).isNormalStar()) continue;
					
					
					PlanetGenDataSpec spec = (PlanetGenDataSpec) 
											Global.getSettings().getSpec(PlanetGenDataSpec.class, planet.getSpec().getPlanetType(), true);
					if (spec == null) continue;
					
					String cat = spec.getCategory();
					if (cat == null) continue;
					
					float weight = 0f;
					if (Planets.CAT_HAB1.equals(cat)) {
						weight = 1f;
					} else if (Planets.CAT_HAB2.equals(cat)) {
						weight = 1f;
					} else if (Planets.CAT_HAB3.equals(cat)) {
						weight = 1f;
					}
					
					if (weight <= 0) continue;
					
					weight = 0;
					
					if (planet.hasCondition(Conditions.HOT)) {
						weight += 5f;
					}
					if (planet.hasCondition(Conditions.POOR_LIGHT)) {
						weight += 5f;
					}
					if (planet.hasCondition(Conditions.WATER_SURFACE)) {
						weight += 5f;
					}
					if (Misc.hasFarmland(planet.getMarket())) {
						weight += 10f;
					}
					
					if (weight <= 0) continue;
					
					// +250 beyond normal radius
					boolean enoughRoom = true;
					for (PlanetAPI other : system.getPlanets()) {
						if (other.getOrbitFocus() == planet) {
							if (other.getCircularOrbitRadius() < planet.getRadius() + other.getRadius() + 320) {
								enoughRoom = false;
								break;
							}
						}
					}
					if (!enoughRoom) continue;
					
					
					picker.add(planet, weight);
				}
			}
		}
		
		if (DEBUG) System.out.println("Found " + picker.getItems().size() + " candidates");
		for (int i = 0; i < num && !picker.isEmpty(); i++) {
			PlanetAPI planet = picker.pickAndRemove();
			if (DEBUG) System.out.println("Adding solar shades and mirrors to [" + planet.getName() + "] in [" + 
					planet.getStarSystem() + " located at " + planet.getLocationInHyperspace());
			
			planet.getMarket().addCondition(Conditions.SOLAR_ARRAY);
			
			StarSystemAPI system = planet.getStarSystem();
			PlanetAPI star = (PlanetAPI) planet.getOrbitFocus();
			
			boolean shade = planet.hasCondition(Conditions.HOT) ||
								planet.getTypeId().equals(Planets.DESERT) ||
								planet.getTypeId().equals(Planets.DESERT1) ||
								planet.getTypeId().equals(Planets.ARID) ||
								star.getTypeId().equals(StarTypes.BLUE_GIANT) ||
								star.getTypeId().equals(StarTypes.BLUE_SUPERGIANT);
			boolean mirror = planet.hasCondition(Conditions.POOR_LIGHT) ||
								planet.getTypeId().equals(Planets.PLANET_TERRAN_ECCENTRIC) ||
//								star.getTypeId().equals(StarTypes.RED_SUPERGIANT) ||
//								star.getTypeId().equals(StarTypes.RED_GIANT) ||
								star.getTypeId().equals(StarTypes.RED_DWARF) ||
								star.getTypeId().equals(StarTypes.BROWN_DWARF);
			
			boolean forceFew = false; 
			if (!shade && !mirror) {
				mirror = true;
				shade = true;
				forceFew = true;
			}
			
			String faction = Factions.NEUTRAL;
			float period = planet.getCircularOrbitPeriod();
			float angle = planet.getCircularOrbitAngle();
			float radius = 270f + planet.getRadius();
			//String name = planet.getName();
			
			float xp = 300f;
			float profile = 2000f;
			
			if (mirror) {
				boolean manyMirrors = random.nextBoolean();
				
				SectorEntityToken mirror2 = system.addCustomEntity(null, "Stellar Mirror Beta", Entities.STELLAR_MIRROR, faction);	
				SectorEntityToken mirror3 = system.addCustomEntity(null, "Stellar Mirror Gamma", Entities.STELLAR_MIRROR, faction);
				SectorEntityToken mirror4 = system.addCustomEntity(null, "Stellar Mirror Delta", Entities.STELLAR_MIRROR, faction);
				mirror2.setCircularOrbitPointingDown(planet, angle - 30, radius, period);	
				mirror3.setCircularOrbitPointingDown(planet, angle + 0, radius, period);	
				mirror4.setCircularOrbitPointingDown(planet, angle + 30, radius, period);
				makeDiscoverable(mirror2, xp, profile);
				makeDiscoverable(mirror3, xp, profile);
				makeDiscoverable(mirror4, xp, profile);
				
				if (manyMirrors && !forceFew) {
					SectorEntityToken mirror1 = system.addCustomEntity(null, "Stellar Mirror Alpha", Entities.STELLAR_MIRROR, faction);
					SectorEntityToken mirror5 = system.addCustomEntity(null, "Stellar Mirror Epsilon", Entities.STELLAR_MIRROR, faction);
					mirror1.setCircularOrbitPointingDown(planet, angle - 60, radius, period);
					mirror5.setCircularOrbitPointingDown(planet, angle + 60, radius, period);
					makeDiscoverable(mirror1, xp, profile);
					makeDiscoverable(mirror5, xp, profile);
				}
			}
			
			if (shade) {
				boolean manyShades = random.nextBoolean();
				SectorEntityToken shade2 = system.addCustomEntity(null, "Stellar Shade Psi", Entities.STELLAR_SHADE, faction);
				shade2.setCircularOrbitPointingDown(planet, angle + 180 + 0, radius + 25, period);
				makeDiscoverable(shade2, xp, profile);
				
				if (manyShades && !forceFew) {
					SectorEntityToken shade1 = system.addCustomEntity(null, "Stellar Shade Omega", Entities.STELLAR_SHADE, faction);
					SectorEntityToken shade3 = system.addCustomEntity(null, "Stellar Shade Chi", Entities.STELLAR_SHADE, faction);
					shade1.setCircularOrbitPointingDown(planet, angle + 180 - 26, radius - 10, period);
					shade3.setCircularOrbitPointingDown(planet, angle + 180 + 26, radius - 10, period);
					makeDiscoverable(shade1, xp, profile);
					makeDiscoverable(shade3, xp, profile);
				}
			}
			
		}
		
		if (DEBUG) System.out.println("Done adding solar shades and mirrors");
	}
	
	public static void makeDiscoverable(SectorEntityToken entity, float xp, float sensorProfile) {
		entity.setDiscoverable(true);
		entity.setDiscoveryXP(xp);
		entity.setSensorProfile(sensorProfile);
	}
	
	
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
	
	
	protected void addExtraGates(ThemeGenContext context) {
//		List<SectorEntityToken> gates = new ArrayList<SectorEntityToken>();
//		List<StarSystemAPI> systems = new ArrayList<StarSystemAPI>();
//		List<Constellation> list = new ArrayList<Constellation>(context.constellations);
//		for (Constellation c : list) {
//			for (StarSystemAPI system : c.getSystems()) {
//				gates.addAll(system.getEntitiesWithTag(Tags.GATE));
//				systems.add(system);
//			}
//		}

		List<StarSystemAPI> systems = new ArrayList<StarSystemAPI>(Global.getSector().getStarSystems());
		List<SectorEntityToken> gates = new ArrayList<SectorEntityToken>();
		
		for (StarSystemAPI system : new ArrayList<StarSystemAPI>(systems)) {
			//if (system.hasTag(Tags.THEME_CORE)) continue; // this isn't set yet
			boolean galatia = system.getBaseName().toLowerCase().equals("galatia");
			if (system.getTags().isEmpty() || galatia) {
				systems.remove(system);
				continue;
			}
			gates.addAll(system.getEntitiesWithTag(Tags.GATE));
		}
		
		
		int addGates = MIN_GATES + random.nextInt(MAX_GATES - MIN_GATES + 1) - gates.size();
		if (addGates < MIN_GATES_TO_ADD) addGates = MIN_GATES_TO_ADD;
		if (addGates <= 0) {
			if (DEBUG) System.out.println("  Already have " + gates.size() + " gates, not adding any");
			return;
		}
		
		List<StarSystemData> all = new ArrayList<BaseThemeGenerator.StarSystemData>();
		
		if (DEBUG) System.out.println("");
		if (DEBUG) System.out.println("");
		if (DEBUG) System.out.println("");
		if (DEBUG) System.out.println("  Adding " + addGates + " extra gates, for a total of " + (addGates + gates.size()));
		
		for (int i = 0; i < addGates; i++) {
			float maxDist = 0;
			StarSystemAPI farthest = null;
			for (StarSystemAPI system : systems) {
				if (system.getPlanets().size() < 3) continue; // skip empty systems
				
				float minDist = Float.MAX_VALUE;
				for (SectorEntityToken gate : gates) {
					float dist = Misc.getDistanceLY(gate, system.getCenter());
					if (dist < minDist) minDist = dist;
				}
				if (minDist > maxDist) {
					maxDist = minDist;
					farthest = system;
				}
			}
			if (farthest != null) {
				StarSystemData data = new StarSystemData();
				data.system = farthest;
				WeightedRandomPicker<String> factions = SalvageSpecialAssigner.getNearbyFactions(random, farthest.getCenter(),
																				15f, 5f, 5f);
				AddedEntity gate = addInactiveGate(data, 1f, 0.5f, 0.5f, factions);
				if (gate != null && gate.entity != null) gates.add(gate.entity);
			}
		}
		if (DEBUG) System.out.println("  Done adding extra gates");
		if (DEBUG) System.out.println("");
		if (DEBUG) System.out.println("");
		if (DEBUG) System.out.println("");
		
		SpecialCreationContext specialContext = new SpecialCreationContext();
		specialContext.themeId = getThemeId();
		SalvageSpecialAssigner.assignSpecials(all, specialContext);
		
		
		
	}
	
	
	protected void addCoronalTaps(ThemeGenContext context) {
		if (DEBUG) System.out.println("Adding coronal taps...");
		
		List<Constellation> list = new ArrayList<Constellation>(context.constellations);
		
		WeightedRandomPicker<StarSystemAPI> tapSystems = new WeightedRandomPicker<StarSystemAPI>(StarSystemGenerator.random);
		WeightedRandomPicker<StarSystemAPI> backup = new WeightedRandomPicker<StarSystemAPI>(StarSystemGenerator.random);
		for (Constellation c : list) {
			for (StarSystemAPI system : c.getSystems()) {
				if (system.hasTag(Tags.THEME_SPECIAL)) continue;
				
				float w = 0f;
				if (system.hasTag(Tags.THEME_REMNANT)) {
					w = 10f;
				} else if (system.hasTag(Tags.THEME_DERELICT)) {
					w = 10f;
				} else if (system.hasTag(Tags.THEME_RUINS)) {
					w = 10f;
				} else if (system.hasTag(Tags.THEME_MISC)) {
					w = 5f;
				}
	
				if (w <= 0) continue;
	
				if (system.getType() == StarSystemType.TRINARY_2CLOSE) {
					w *= 5f;
				}
	
				boolean hasBlueStar = false;
				boolean hasNormalStar = false;
				for (PlanetAPI planet : system.getPlanets()) {
					if (!planet.isNormalStar()) continue;
					if (planet.getTypeId().equals(StarTypes.BLUE_GIANT) ||
							planet.getTypeId().equals(StarTypes.BLUE_SUPERGIANT)) { 
						hasBlueStar = true;
					}
					hasNormalStar = true;
				}
				
				if (!hasNormalStar) continue;
				
				WeightedRandomPicker<StarSystemAPI> use = tapSystems;
				if (!hasBlueStar) {
					use = backup;
				}
				use.add(system, w);
			}
		}
		
		
		if (tapSystems.isEmpty()) {
			tapSystems.addAll(backup);
		}
		
		int numTaps = 2 + random.nextInt(2);
		numTaps = 2;
		int added = 0;
		while (added < numTaps && !tapSystems.isEmpty()) {
			StarSystemAPI pick = tapSystems.pickAndRemove();
			AddedEntity tap = addCoronalTap(pick);
			if (tap != null) {
				added++;
			}
		}
		
		if (DEBUG) System.out.println("Done adding coronal taps\n\n\n");
	}
	
	public static class MakeCoronalTapFaceNearestStar implements EveryFrameScript {
		protected SectorEntityToken tap;
		public MakeCoronalTapFaceNearestStar(SectorEntityToken tap) {
			this.tap = tap;
		}
		public void advance(float amount) {
			if (!tap.isInCurrentLocation()) return;
			
			float minDist = Float.MAX_VALUE;
			PlanetAPI closest = null;
			for (PlanetAPI star : tap.getContainingLocation().getPlanets()) {
				if (!star.isStar()) continue;
				float dist = Misc.getDistance(tap.getLocation(), star.getLocation());
				if (dist < minDist) {
					minDist = dist;
					closest = star;
				}
			}
			if (closest != null) {
				tap.setFacing(Misc.getAngleInDegrees(tap.getLocation(), closest.getLocation()) + 180f);
			}			
		}
		public boolean isDone() {
			return false;
		}
		public boolean runWhilePaused() {
			return false;
		}
	}
	
	protected AddedEntity addCoronalTap(StarSystemAPI system) {
		
		if (DEBUG) System.out.println("Adding coronal tap to [" + system.getNameWithLowercaseType() + ", " + system.getLocation());
		
		String factionId = Factions.NEUTRAL;
		
		AddedEntity entity = null;
		if (system.getType() == StarSystemType.TRINARY_2CLOSE) {
			EntityLocation loc = new EntityLocation();
			loc.location = new Vector2f();
			entity = addEntity(random, system, loc, Entities.CORONAL_TAP, factionId);
			if (entity != null) {
				system.addScript(new MakeCoronalTapFaceNearestStar(entity.entity));
			}
		} else {
			WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>();
			WeightedRandomPicker<PlanetAPI> fallback = new WeightedRandomPicker<PlanetAPI>();
			for (PlanetAPI planet : system.getPlanets()) {
				if (!planet.isNormalStar()) continue;
				if (planet.getTypeId().equals(StarTypes.BLUE_GIANT) ||
						planet.getTypeId().equals(StarTypes.BLUE_SUPERGIANT)) {
					picker.add(planet);
				} else {
					fallback.add(planet);
				}
			}
			if (picker.isEmpty()) {
				picker.addAll(fallback);
			}
			
			PlanetAPI star = picker.pick();
			if (star != null) {
				CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec(Entities.CORONAL_TAP);
				EntityLocation loc = new EntityLocation();
				float orbitRadius = star.getRadius() + spec.getDefaultRadius() + 100f;
				float orbitDays = orbitRadius / 20f;
				loc.orbit = Global.getFactory().createCircularOrbitPointingDown(star, random.nextFloat() * 360f, orbitRadius, orbitDays);
				entity = addEntity(random, system, loc, Entities.CORONAL_TAP, factionId);
			}
		}
		
		
		if (entity != null) {
			system.addScript(new CoronalTapParticleScript(entity.entity));
//			system.addCorona(entity.entity, Terrain.CORONA_JET,
//					500f, // radius outside planet
//					15f, // burn level of "wind"
//					0f, // flare probability
//					1f // CR loss mult while in it
//					);
			
//			system.addTag(Tags.THEME_DERELICT);
			system.addTag(Tags.HAS_CORONAL_TAP);
		}
		
		if (DEBUG) {
			if (entity != null) {
				System.out.println(String.format("  Added coronal tap to %s", system.getNameWithLowercaseType()));
			} else {
				System.out.println(String.format("  Failed to add coronal tap to %s", system.getNameWithLowercaseType()));
			}
		}
		return entity;
	}
	
	
	protected void addPKSystem(ThemeGenContext context) {
		if (DEBUG) System.out.println("Looking for system to hide PK in");
		
		List<StarSystemAPI> preferred = new ArrayList<StarSystemAPI>();
		List<StarSystemAPI> other = new ArrayList<StarSystemAPI>();
		
		for (Constellation c : context.constellations) {
			for (StarSystemAPI system : c.getSystems()) {
				if (system.hasTag(Tags.THEME_SPECIAL)) continue;
				
				if (system.isNebula()) continue;
				if (system.hasPulsar()) continue;
				if (system.hasBlackHole()) continue;
				
				boolean misc = system.hasTag(Tags.THEME_MISC_SKIP) || system.hasTag(Tags.THEME_MISC);
				if (system.hasTag(Tags.THEME_DERELICT)) misc = false;
				
				boolean nonLargeDerelict = system.hasTag(Tags.THEME_DERELICT) && 
										!system.hasTag(Tags.THEME_DERELICT_MOTHERSHIP) &&
										!system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER) &&
										!system.hasTag(Tags.THEME_DERELICT_SURVEY_SHIP);
				
				boolean secondaryRuins = system.hasTag(Tags.THEME_RUINS_SECONDARY);
				boolean remnantNoFleets = system.hasTag(Tags.THEME_REMNANT_NO_FLEETS);
				boolean unsafe = system.hasTag(Tags.THEME_UNSAFE);
				
				if (unsafe || !(misc || nonLargeDerelict || secondaryRuins || remnantNoFleets)) {
					continue;
				}
				
				int count = 0;
				for (PlanetAPI curr : system.getPlanets()) {
					if (curr.isStar()) continue;
					if (curr.isMoon()) continue;
					if (curr.isGasGiant()) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.getCircularOrbitRadius() < 6000) continue;
					count++;
				}
				
				if (count > 0) {
					preferred.add(system);
				} else {
					other.add(system);
				}
			}
		}
		
		Comparator<StarSystemAPI> comp = new Comparator<StarSystemAPI>() {
			public int compare(StarSystemAPI o1, StarSystemAPI o2) {
				return (int) Math.signum(o2.getLocation().length() - o1.getLocation().length());
			}
		};

		List<StarSystemAPI> sorted = new ArrayList<StarSystemAPI>();
		if (!preferred.isEmpty()) {
			sorted.addAll(preferred);
		} else {
			sorted.addAll(other);
		}
		if (sorted.isEmpty()) {
			if (DEBUG) System.out.println("FAILED TO FIND SUITABLE SYSTEM FOR PK");
			return;
		}
		Collections.sort(sorted, comp);
		
		
		// pick from some of the matching systems furthest from core
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);
		for (int i = 0; i < 20 && i < sorted.size(); i++) {
			//sorted.get(i).addTag(Tags.PK_SYSTEM);
			picker.add(sorted.get(i), 1f);
		}
		
		StarSystemAPI system = picker.pick();
		
		if (DEBUG) System.out.println("Adding PK to [" + system.getName() + "] at [" + system.getLocation() + "]");
		setUpPKSystem(system);
		
		
		if (DEBUG) System.out.println("Finished adding PK system\n\n\n\n\n");
	}

	protected void setUpPKSystem(StarSystemAPI system) {
		system.addTag(Tags.THEME_SPECIAL);
		system.addTag(Tags.PK_SYSTEM);
		
		Global.getSector().getPersistentData().put(PK_SYSTEM_KEY, system);
		Global.getSector().getMemoryWithoutUpdate().set(PK_SYSTEM_KEY, system.getId());
		
		// - pick a planet at 6k range or higher to make into a tundra world
		// - turn any planets with a lower hazard rating into barren and reassign their conditions 
		
		PlanetAPI tundra = null;
		for (PlanetAPI curr : system.getPlanets()) {
			if (curr.isStar()) continue;
			//if (curr.isMoon()) continue;
			if (curr.isGasGiant()) continue;
			if (curr.getMarket() == null) continue;
			if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
			if (curr.getCircularOrbitRadius() < 6000) continue;
			
			tundra = curr;
			break;
		}
		
		//pick = null;
		
		// if there's no planet in a suitable range, create one
		// could end up with a tundra world really far out if the system is full of stuff
		// but has no planets, but it's unlikely
		if (tundra == null) {
			List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 6000, 20000, 800);
			float orbitRadius = 7000;
			if (!gaps.isEmpty()) {
				orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
			}
			float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
			float radius = 100f + random.nextFloat() * 50f;
			float angle = random.nextFloat() * 360f;
			String type = Planets.BARREN;
			NamePick namePick = ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, null, null);
			String name = namePick.nameWithRomanSuffixIfAny;
			tundra = system.addPlanet(Misc.genUID(), system.getStar(), name, type, angle, radius, orbitRadius, orbitDays);
			
			if (tundra == null) {
				if (DEBUG) System.out.println("FAILED TO CREATE PLANET IN PK SYSTEM");
				return;
			}
		}
		
		tundra.setName("Sentinel");
		tundra.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
		tundra.getMemoryWithoutUpdate().set(PK_PLANET_KEY, true);
		Global.getSector().getPersistentData().put(PK_PLANET_KEY, tundra);
		
		if (DEBUG) System.out.println("Setting planet [" + tundra.getName() + "] to tundra");
		tundra.changeType(Planets.TUNDRA, random);
		tundra.getMarket().getConditions().clear();
		PlanetConditionGenerator.generateConditionsForPlanet(null, tundra, system.getAge());
		tundra.getMarket().removeCondition(Conditions.DECIVILIZED);
		tundra.getMarket().removeCondition(Conditions.DECIVILIZED_SUBPOP);
		tundra.getMarket().removeCondition(Conditions.RUINS_EXTENSIVE);
		tundra.getMarket().removeCondition(Conditions.RUINS_SCATTERED);
		tundra.getMarket().removeCondition(Conditions.RUINS_VAST);
		tundra.getMarket().removeCondition(Conditions.RUINS_WIDESPREAD);
		tundra.getMarket().removeCondition(Conditions.INIMICAL_BIOSPHERE);
		
		tundra.getMarket().removeCondition(Conditions.FARMLAND_POOR);
		tundra.getMarket().removeCondition(Conditions.FARMLAND_ADEQUATE);
		tundra.getMarket().removeCondition(Conditions.FARMLAND_RICH);
		tundra.getMarket().removeCondition(Conditions.FARMLAND_BOUNTIFUL);
		tundra.getMarket().addCondition(Conditions.FARMLAND_POOR);

		
		// make sure the tundra world is the best habitable world in-system so there's no questions
		// as to why it was chosen by the survivors
		float pickHazard = tundra.getMarket().getHazardValue();
		
		for (PlanetAPI curr : system.getPlanets()) {
			if (curr.isStar()) continue;
			if (curr.isGasGiant()) continue;
			if (curr.getMarket() == null) continue;
			if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
			if (curr == tundra) continue;
			
			float h = curr.getMarket().getHazardValue();
			if (curr.hasCondition(Conditions.HABITABLE) && h <= pickHazard) {
				curr.changeType(Planets.BARREN_VENUSLIKE, random);
				curr.getMarket().getConditions().clear();
				PlanetConditionGenerator.generateConditionsForPlanet(null, curr, system.getAge());
			}
		}
		
		for (SectorEntityToken curr : system.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
			system.removeEntity(curr);
		}
		for (SectorEntityToken curr : system.getEntitiesWithTag(Tags.OBJECTIVE)) {
			system.removeEntity(curr);
		}
		
		
		List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 20000, 800);
		float orbitRadius = 7000;
		if (!gaps.isEmpty()) {
			orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
		}
		float radius = 500f + 200f * random.nextFloat();
		float area = radius * radius * 3.14f;
		int count = (int) (area / 80000f);
		count *= 2;
		if (count < 10) count = 10;
		if (count > 100) count = 100;
		float angle = random.nextFloat() * 360f;
		float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
		
		SectorEntityToken field = system.addTerrain(Terrain.ASTEROID_FIELD,
				new AsteroidFieldParams(
					radius, // min radius
					radius + 100f, // max radius
					count, // min asteroid count
					count, // max asteroid count
					4f, // min asteroid radius 
					16f, // max asteroid radius
					null)); // null for default name
		
		field.setCircularOrbit(system.getCenter(), angle, orbitRadius, orbitDays);
		
		SectorEntityToken cache = BaseThemeGenerator.addSalvageEntity(system, Entities.HIDDEN_CACHE, Factions.NEUTRAL);
		cache.getMemoryWithoutUpdate().set(PK_CACHE_KEY, true);
		//cache.getLocation().set(10000, 10000);
		cache.setCircularOrbit(field, 0, 0, 100f);
		Misc.setDefenderOverride(cache, new DefenderDataOverride(Factions.HEGEMONY, 1f, 20, 20, 1));
		
		// Misc.addDefeatTrigger(fleet, trigger);
		
		// add a ship graveyard around the cache - Luddic Path ships, presumably from another
		// Path operative that got farther along but never reported back
		StarSystemData data = new StarSystemData();
		WeightedRandomPicker<String> derelictShipFactions = new WeightedRandomPicker<String>(random);
		derelictShipFactions.add(Factions.LUDDIC_PATH);
		WeightedRandomPicker<String> hulls = new WeightedRandomPicker<String>(random);
		hulls.add("prometheus2", 1f);
		hulls.add("colossus2", 1f);
		hulls.add("colossus2", 1f);
		hulls.add("colossus2", 1f);
		hulls.add("eradicator", 1f);
		hulls.add("enforcer", 1f);
		hulls.add("sunder", 1f);
		hulls.add("venture_pather", 1f);
		hulls.add("manticore_luddic_path", 1f);
		hulls.add("cerberus_luddic_path", 1f);
		hulls.add("hound_luddic_path", 1f);
		hulls.add("buffalo2", 1f);
		addShipGraveyard(data, field, derelictShipFactions, hulls);
		for (AddedEntity ae : data.generated) {
			SalvageSpecialAssigner.assignSpecials(ae.entity, true);
		}
		
		// add some remnant derelicts around a fringe jump-point
		// where the fight was
		float max = 0f;
		JumpPointAPI fringePoint = null;
		List<JumpPointAPI> points = system.getEntities(JumpPointAPI.class);
		for (JumpPointAPI curr : points) {
			float dist = curr.getCircularOrbitRadius();
			if (dist > max) {
				max = dist;
				fringePoint = curr;
			}
		}
		
		if (fringePoint != null) {
			data = new StarSystemData();
			WeightedRandomPicker<String> remnantShipFactions = new WeightedRandomPicker<String>(random);
			remnantShipFactions.add(Factions.REMNANTS);
			hulls = new WeightedRandomPicker<String>(random);
			hulls.add("radiant", 0.25f);
			hulls.add("nova", 0.5f);
			hulls.add("brilliant", 1f);
			hulls.add("apex", 1f);
			hulls.add("scintilla", 1f);
			hulls.add("scintilla", 1f);
			hulls.add("fulgent", 1f);
			hulls.add("fulgent", 1f);
			hulls.add("glimmer", 1f);
			hulls.add("glimmer", 1f);
			hulls.add("lumen", 1f);
			hulls.add("lumen", 1f);
			addShipGraveyard(data, fringePoint, remnantShipFactions, hulls);
			addDebrisField(data, fringePoint, 400f);
			
			for (AddedEntity ae : data.generated) {
				SalvageSpecialAssigner.assignSpecials(ae.entity, true);
				if (ae.entity.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
					DerelictShipEntityPlugin plugin = (DerelictShipEntityPlugin) ae.entity.getCustomPlugin();
					plugin.getData().ship.condition = ShipCondition.WRECKED;
				}
			}
		}
		
		// Improvised dockyard where presumably the ship conversion took place
		SectorEntityToken dockyard = system.addCustomEntity("pk_dockyard",
				"Sentinel Gantries", Entities.ORBITAL_DOCKYARD, "neutral");

		dockyard.setCircularOrbitPointingDown(tundra, 45, 300, 30);		
		dockyard.setCustomDescriptionId("pk_orbital_dockyard");
		dockyard.getMemoryWithoutUpdate().set("$pkDockyard", true);
		
		//neutralStation.setInteractionImage("illustrations", "abandoned_station2");
		Misc.setAbandonedStationMarket("pk_dockyard", dockyard);
		
		
		// add some unused stuff to the dockyard
		CargoAPI cargo = dockyard.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
		cargo.initMothballedShips(Factions.HEGEMONY);
		
		CampaignFleetAPI temp = Global.getFactory().createEmptyFleet(Factions.HEGEMONY, null, true);
		temp.getFleetData().addFleetMember("enforcer_XIV_Elite");
		temp.getFleetData().addFleetMember("enforcer_XIV_Elite");
		temp.getFleetData().addFleetMember("eagle_xiv_Elite");
		temp.getFleetData().addFleetMember("dominator_XIV_Elite");
		DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
		p.quality = -1;
		temp.setInflater(new DefaultFleetInflater(p));
		temp.inflateIfNeeded();
		temp.setInflater(null);

		int index = 0;
		for (FleetMemberAPI member : temp.getFleetData().getMembersListCopy()) {
			for (String slotId : member.getVariant().getFittedWeaponSlots()) {
				String weaponId = member.getVariant().getWeaponId(slotId);
				if (random.nextFloat() < 0.5f) {
					member.getVariant().clearSlot(slotId);
				}
				if (random.nextFloat() < 0.25f) {
					cargo.addWeapons(weaponId, 1);
				}
			}
			if (index == 0 || index == 2) {
				cargo.getMothballedShips().addFleetMember(member);
			}
			index++;
		}
		cargo.addCommodity(Commodities.METALS, 50f + random.nextInt(51));
		
		List<CampaignFleetAPI> stations = getRemnantStations(true, false);
		float minDist = Float.MAX_VALUE;
		CampaignFleetAPI nexus = null;
		for (CampaignFleetAPI curr : stations) {
			float dist = Misc.getDistanceLY(tundra, curr);
			if (dist < minDist) {
				minDist = dist;
				nexus = curr;
			}
		}
		if (nexus != null) {
			if (DEBUG) System.out.println("Found Remnant nexus in [" + nexus.getContainingLocation().getName() + "]");
			nexus.getMemoryWithoutUpdate().set(PK_NEXUS_KEY, true);
			Global.getSector().getPersistentData().put(PK_NEXUS_KEY, nexus);
			Global.getSector().getMemoryWithoutUpdate().set(PK_NEXUS_KEY, nexus.getContainingLocation().getId());
			
			Misc.addDefeatTrigger(nexus, "PKNexusDefeated");
		}
	}
	
	
	public static List<CampaignFleetAPI> getRemnantStations(boolean includeDamaged, boolean onlyDamaged) {
		List<CampaignFleetAPI> stations = new ArrayList<CampaignFleetAPI>();
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (!system.hasTag(Tags.THEME_REMNANT_MAIN)) continue;
			if (system.hasTag(Tags.THEME_REMNANT_DESTROYED)) continue;
			
			for (CampaignFleetAPI fleet : system.getFleets()) {
				if (!fleet.isStationMode()) continue;
				if (!Factions.REMNANTS.equals(fleet.getFaction().getId())) continue;
				
				boolean damaged = fleet.getMemoryWithoutUpdate().getBoolean("$damagedStation");
				if (damaged && !includeDamaged) continue;
				if (!damaged && onlyDamaged) continue;
				
				stations.add(fleet);
			}
		}
		return stations;
	}
}
















