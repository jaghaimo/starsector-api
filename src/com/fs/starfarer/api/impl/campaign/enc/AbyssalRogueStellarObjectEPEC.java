package com.fs.starfarer.api.impl.campaign.enc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.AccretionDiskGenPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.CustomConstellationParams;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenResult;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GeneratedPlanet;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.TerrainGenDataSpec;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceAbyssPluginImpl;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceAbyssPluginImpl.AbyssalEPData;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin.CoronaParams;
import com.fs.starfarer.api.impl.campaign.world.GateHaulerLocation;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.CatalogEntryType;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class AbyssalRogueStellarObjectEPEC extends BaseEPEncounterCreator {

	public static enum RogueStellarObjectType {
		PLANETOID,
		GAS_GIANT,
		BLACK_HOLE
	}
	
	public static float PROB_BLACK_HOLE_ORBITERS = 0.1f;
	
	public static WeightedRandomPicker<RogueStellarObjectType> STELLAR_OBJECT_TYPES = new WeightedRandomPicker<RogueStellarObjectType>();
	static {
		STELLAR_OBJECT_TYPES.add(RogueStellarObjectType.PLANETOID, 100f);
		STELLAR_OBJECT_TYPES.add(RogueStellarObjectType.GAS_GIANT, 20f);
		STELLAR_OBJECT_TYPES.add(RogueStellarObjectType.BLACK_HOLE, 7f);
	}
	
	public static WeightedRandomPicker<String> PLANETOID_TYPES = new WeightedRandomPicker<String>();
	static {
		PLANETOID_TYPES.add(Planets.CRYOVOLCANIC, 5f);
		PLANETOID_TYPES.add(Planets.FROZEN, 2.5f);
		PLANETOID_TYPES.add(Planets.FROZEN1, 2.5f);
		PLANETOID_TYPES.add(Planets.FROZEN2, 2.5f);
		PLANETOID_TYPES.add(Planets.FROZEN3, 2.5f);
		PLANETOID_TYPES.add(Planets.BARREN, 2.5f);
		PLANETOID_TYPES.add(Planets.BARREN2, 2.5f);
		PLANETOID_TYPES.add(Planets.BARREN3, 2.5f);
		PLANETOID_TYPES.add(Planets.BARREN_CASTIRON, 2.5f);
		PLANETOID_TYPES.add(Planets.BARREN_BOMBARDED, 5f);
		PLANETOID_TYPES.add(Planets.ROCKY_METALLIC, 5f);
		PLANETOID_TYPES.add(Planets.ROCKY_ICE, 5f);
	}
	public static WeightedRandomPicker<String> GAS_GIANT_TYPES = new WeightedRandomPicker<String>();
	static {
		GAS_GIANT_TYPES.add(Planets.ICE_GIANT, 10f);
		GAS_GIANT_TYPES.add(Planets.GAS_GIANT, 5f);
	}
	
	public static WeightedRandomPicker<String> BLACK_HOLE_TYPES = new WeightedRandomPicker<String>();
	static {
		BLACK_HOLE_TYPES.add(StarTypes.BLACK_HOLE, 10f);
	}
	
	public float getFrequencyForPoint(EncounterManager manager, EncounterPoint point) {
		return AbyssalFrequencies.getAbyssalRogueStellarObjectFrequency(manager, point);
	}
	

	@Override
	public void createEncounter(EncounterManager manager, EncounterPoint point) {
		
		AbyssalEPData data = (AbyssalEPData) point.custom;
		SectorAPI sector = Global.getSector();
		
		StarSystemAPI system = sector.createStarSystem("Deep Space");
		system.setProcgen(true);
		//system.setType(StarSystemType.NEBULA);
		system.setName("Deep Space"); // to get rid of "Star System" at the end of the name
		system.setOptionalUniqueId(Misc.genUID());
		system.setType(StarSystemType.DEEP_SPACE);
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		system.addTag(Tags.TEMPORARY_LOCATION);
		system.addTag(Tags.SYSTEM_ABYSSAL);
		
		if (data.random.nextFloat() < 0.5f) {
			system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
		} else {
			system.setBackgroundTextureFilename("graphics/backgrounds/background5.jpg");
		}

		system.getLocation().set(point.loc.x, point.loc.y);

		SectorEntityToken center = system.initNonStarCenter();
		
		system.setLightColor(GateHaulerLocation.ABYSS_AMBIENT_LIGHT_COLOR);
		center.addTag(Tags.AMBIENT_LS);
		
		RogueStellarObjectType objectType = STELLAR_OBJECT_TYPES.pick(data.random);
		if (objectType == null) return;
		
		WeightedRandomPicker<StarAge> agePicker = new WeightedRandomPicker<StarAge>(data.random);
		agePicker.add(StarAge.OLD, 10f);
		agePicker.add(StarAge.AVERAGE, 5f);
		agePicker.add(StarAge.YOUNG, 3f);
		
		StarAge age = agePicker.pick();
		String nebulaId = Planets.NEBULA_CENTER_OLD;
		if (age == StarAge.AVERAGE) {
			nebulaId = Planets.NEBULA_CENTER_AVERAGE;
		} else if (age == StarAge.YOUNG) {
			nebulaId = Planets.NEBULA_CENTER_YOUNG;
		}
		
		
		StarGenDataSpec starData = (StarGenDataSpec) 
				Global.getSettings().getSpec(StarGenDataSpec.class, nebulaId, false);
		
		CustomConstellationParams params = new CustomConstellationParams(age);
		Random prev = StarSystemGenerator.random;
		StarSystemGenerator.random = data.random;
		
		StarSystemGenerator gen = new StarSystemGenerator(params);
		gen.init(system, age);
		
		GenContext context = new GenContext(gen, system, system.getCenter(), starData,
				null, 0, age.name(), 0, 1000, null, -1);
		// just so it doesn't try to add things at the planet's lagrange points
		context.lagrangeParent = new GeneratedPlanet(null, null, false, 0, 0, 0);
		
		context.excludeCategories.add(StarSystemGenerator.CAT_HAB5);
		context.excludeCategories.add(StarSystemGenerator.CAT_HAB4);
		context.excludeCategories.add(StarSystemGenerator.CAT_HAB3);
		context.excludeCategories.add(StarSystemGenerator.CAT_HAB2);
		
		PlanetAPI main = null;
		
		
		if (objectType == RogueStellarObjectType.BLACK_HOLE) {
			main = addBlackHole(system, context, data);
			
			if (main != null) {
				system.setStar(main);
				system.setCenter(main);
				system.removeEntity(center);
				center = main;
				
				if (data.random.nextFloat() < PROB_BLACK_HOLE_ORBITERS || true) {
					context.starData = (StarGenDataSpec) 
							Global.getSettings().getSpec(StarGenDataSpec.class, StarTypes.BLACK_HOLE, false);
					StarSystemGenerator.addOrbitingEntities(system, main, age, 1, 3, 500, 0, false, false);
				}
			}
		} else {
			String planetType;
			if (objectType == RogueStellarObjectType.PLANETOID) {
				planetType = PLANETOID_TYPES.pick(data.random);
			} else {
				planetType = GAS_GIANT_TYPES.pick(data.random);
			}
			//planetType = Planets.CRYOVOLCANIC;
			//planetType = Planets.GAS_GIANT;
			PlanetGenDataSpec planetData = (PlanetGenDataSpec) Global.getSettings().getSpec(PlanetGenDataSpec.class, planetType, false);
			
			GenResult result = gen.addPlanet(context, planetData, false, true);
			if (result == null || result.entities.isEmpty() || 
					!(result.entities.get(0) instanceof PlanetAPI)) return;
			main = (PlanetAPI) result.entities.get(0);
		}
		
		if (main == null) return;
		
		main.setOrbit(null);
		main.setLocation(0, 0);
		
		boolean multiple = context.generatedPlanets.size() > 1;
		int index = data.random.nextInt(20);
		
//		List<GeneratedPlanet> sorted = new ArrayList<GeneratedPlanet>(context.generatedPlanets);
//		Collections.sort(sorted, new Comparator<GeneratedPlanet>() {
//			public int compare(GeneratedPlanet o1, GeneratedPlanet o2) {
//				return (int) Math.signum(o2.planet.getRadius() - o1.planet.getRadius());
//			}
//		});
		List<PlanetAPI> sorted = new ArrayList<PlanetAPI>(system.getPlanets());
		Collections.sort(sorted, new Comparator<PlanetAPI>() {
			public int compare(PlanetAPI o1, PlanetAPI o2) {
				return (int) Math.signum(o2.getRadius() - o1.getRadius());
			}
		});
		
//		for (GeneratedPlanet curr : sorted) {
//			PlanetAPI planet = curr.planet;
		for (PlanetAPI planet : sorted) {
			CatalogEntryType type = CatalogEntryType.PLANET;
			if (planet.isGasGiant()) type = CatalogEntryType.GIANT;
			if (planet.getSpec().isBlackHole()) type = CatalogEntryType.BLACK_HOLE;
			
			String firstChar = null;
			if (multiple) {
				firstChar = "" + Character.valueOf((char) ('A' + (index % 26)));
				index++;
			}
			
			String name = Misc.genEntityCatalogId(firstChar, -1, -1, -1, type);			
			planet.setName(name);
			if (planet.getMarket() != null) {
				planet.getMarket().setName(name);
				
				planet.getMarket().removeCondition(Conditions.RUINS_SCATTERED);
				planet.getMarket().removeCondition(Conditions.RUINS_WIDESPREAD);
				planet.getMarket().removeCondition(Conditions.RUINS_EXTENSIVE);
				planet.getMarket().removeCondition(Conditions.RUINS_VAST);
				planet.getMarket().removeCondition(Conditions.DECIVILIZED);
				planet.getMarket().removeCondition(Conditions.DECIVILIZED_SUBPOP);
				planet.getMarket().removeCondition(Conditions.POLLUTION);
			}
			
			// the standard "barren" description mentions a primary star
			if (planet.getSpec().getDescriptionId().equals(Planets.BARREN)) {
				planet.setDescriptionIdOverride("barren_deep_space");
			}
			
		}
		
		
		StarSystemGenerator.random = prev;
		
		system.autogenerateHyperspaceJumpPoints(true, false, false);
		
		setAbyssalDetectedRanges(system);
		
		system.addScript(new AbyssalLocationDespawner(system));
		
		addSpecials(system, manager, point, data);
	}
	
	protected void addSpecials(StarSystemAPI system, EncounterManager manager, EncounterPoint point, AbyssalEPData data) {
		
	}
	
	public static void setAbyssalDetectedRanges(StarSystemAPI system) {
		if (system.getAutogeneratedJumpPointsInHyper() != null) {
			for (JumpPointAPI jp : system.getAutogeneratedJumpPointsInHyper()) {
				if (jp.isStarAnchor()) {
					jp.addTag(Tags.STAR_HIDDEN_ON_MAP);
				}
				float range = HyperspaceAbyssPluginImpl.JUMP_POINT_DETECTED_RANGE;
				if (jp.isGasGiantAnchor()) {
					range = HyperspaceAbyssPluginImpl.GAS_GIANT_DETECTED_RANGE;
				} else if (jp.isStarAnchor()) {
					range = HyperspaceAbyssPluginImpl.STAR_DETECTED_RANGE;
				}
				
				setAbyssalDetectedRange(jp, range);
			}
		}
		
		if (system.getAutogeneratedNascentWellsInHyper() != null) {
			for (NascentGravityWellAPI well : system.getAutogeneratedNascentWellsInHyper()) {
				setAbyssalDetectedRange(well, HyperspaceAbyssPluginImpl.NASCENT_WELL_DETECTED_RANGE);
			}
		}
	}
	
	public static void setAbyssalDetectedRange(SectorEntityToken entity, float range) {
		float detectedRange = range / HyperspaceTerrainPlugin.ABYSS_SENSOR_RANGE_MULT;
		
		float maxSensorRange = Global.getSettings().getSensorRangeMaxHyper();
		float desired = detectedRange * HyperspaceTerrainPlugin.ABYSS_SENSOR_RANGE_MULT;
		if (desired > maxSensorRange) {
			entity.setExtendedDetectedAtRange(desired - maxSensorRange);
		}
		
		entity.setSensorProfile(1f);
		entity.setDiscoverable(false);
		entity.getDetectedRangeMod().modifyFlat("jpDetRange", detectedRange);
		
		float mult = Math.min(0.5f, 400f / range);
		entity.setDetectionRangeDetailsOverrideMult(mult);
		
		//getMemoryWithoutUpdate().set(MemFlags.EXTRA_SENSOR_INDICATORS, 3)
	}
	
	public PlanetAPI addBlackHole(StarSystemAPI system, GenContext context, AbyssalEPData data) {
		
		StarGenDataSpec starData = (StarGenDataSpec) 
				Global.getSettings().getSpec(StarGenDataSpec.class, StarTypes.BLACK_HOLE, false);
		
		system.setLightColor(starData.getLightColorMin());
		
		float radius = starData.getMinRadius() + 
				(starData.getMaxRadius() - starData.getMinRadius()) * data.random.nextFloat(); 

		PlanetAPI planet = system.addPlanet(null, null, null, StarTypes.BLACK_HOLE, 0, radius, 0, 0);
		
		
		// convert corona to Event Horizon
		StarCoronaTerrainPlugin coronaPlugin = Misc.getCoronaFor(planet);
		if (coronaPlugin != null) {
			system.removeEntity(coronaPlugin.getEntity());
		}
		
		starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, planet.getSpec().getPlanetType(), false);
		float corona = planet.getRadius() * (starData.getCoronaMult() + starData.getCoronaVar() * (data.random.nextFloat() - 0.5f));
		if (corona < starData.getCoronaMin()) corona = starData.getCoronaMin();
		
		SectorEntityToken eventHorizon = system.addTerrain(Terrain.EVENT_HORIZON, 
				new CoronaParams(planet.getRadius() + corona, (planet.getRadius() + corona) / 2f,
						planet, starData.getSolarWind(), 
								(float) (starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare()) * data.random.nextFloat()),
								starData.getCrLossMult()));
		eventHorizon.setCircularOrbit(planet, 0, 0, 100);

		
		
		// add accretion disk
		Collection<TerrainGenDataSpec> terrainDataSpecs = Global.getSettings().getAllSpecs(TerrainGenDataSpec.class);
		TerrainGenDataSpec terrainData = null;
		for (TerrainGenDataSpec curr : terrainDataSpecs) {
			if (curr.getId().equals(Tags.ACCRETION_DISK)) {
				terrainData = curr;
				break;
			}
		}
		
		if (terrainData != null) {
			AccretionDiskGenPlugin diskGen = new AccretionDiskGenPlugin();
			context.parent = planet;
			context.currentRadius = 500f + data.random.nextFloat() * 1000f;
			diskGen.generate(terrainData, context);
		}
		
		GeneratedPlanet p = new GeneratedPlanet(null, planet, false, 0, 0, 0);
		context.generatedPlanets.add(p);
		
		return planet;
	}
	
	
//	if (planet.getSpec().getAtmosphereThickness() > 0) {
//		Color atmosphereColor = Misc.interpolateColor(planet.getSpec().getAtmosphereColor(), color, 0.25f);
//		atmosphereColor = Misc.setAlpha(atmosphereColor, planet.getSpec().getAtmosphereColor().getAlpha());
//		planet.getSpec().setAtmosphereColor(atmosphereColor);
//		
//		if (planet.getSpec().getCloudTexture() != null) {
//			Color cloudColor = Misc.interpolateColor(planet.getSpec().getCloudColor(), color, 0.25f);
//			cloudColor = Misc.setAlpha(cloudColor, planet.getSpec().getCloudColor().getAlpha());
//			planet.getSpec().setAtmosphereColor(atmosphereColor);
//		}
//	}
	
	//planet.getMemoryWithoutUpdate().set("$gateHaulerIceGiant", true);
	
//	StarGenDataSpec starData = (StarGenDataSpec) 
//			Global.getSettings().getSpec(StarGenDataSpec.class, Planets.NEBULA_CENTER_OLD, false);
//	GenContext context = new GenContext(null, system, system.getCenter(), starData,
//					null, 0, StarAge.ANY.name(), 0, 1000, null, -1);
	
	//PlanetConditionGenerator.generateConditionsForPlanet(context, planet, StarAge.ANY);

//	StarGenDataSpec starData1 = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, StarTypes.BLACK_HOLE, false);
//	float radius = starData1.getMinRadius() + 
//			(starData1.getMaxRadius() - starData1.getMinRadius()) * data.random.nextFloat();
//	
//	float corona = radius * (starData1.getCoronaMult() + starData1.getCoronaVar() * (data.random.nextFloat() - 0.5f));
//	if (corona < starData1.getCoronaMin()) corona = starData1.getCoronaMin();
//	SectorEntityToken center = system.initStar(null, StarTypes.BLACK_HOLE, 150, corona);

}













