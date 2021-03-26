package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation.ConstellationType;
import com.fs.starfarer.api.impl.campaign.procgen.ConstellationGen.SpringConnection;
import com.fs.starfarer.api.impl.campaign.procgen.ConstellationGen.SpringSystem;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain.TileParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin.CoronaParams;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;


public class StarSystemGenerator {
	
	public static class CustomConstellationParams implements Cloneable {
		public String name = null;
		public String secondaryName = null;
		public StarAge age = null;
		public int minStars = 0;
		public int maxStars = 0;
		public int numStars = 0;
		public boolean forceNebula = false;
		public List<StarSystemType> systemTypes = new ArrayList<StarSystemType>();
		public List<String> starTypes = new ArrayList<String>();
		public Vector2f location = null;
		
		public CustomConstellationParams(StarAge age) {
			this.age = age;
		}

		@Override
		public CustomConstellationParams clone() {
			try {
				return (CustomConstellationParams) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
		
		
	}
	
	
	public static enum StarSystemType {
		SINGLE,
		BINARY_CLOSE,
		BINARY_FAR,
		TRINARY_2CLOSE,
		TRINARY_1CLOSE_1FAR,
		TRINARY_2FAR,
		NEBULA,
	}
	
	public static final float MIN_STAR_DIST = 2000f;
	public static final float MAX_STAR_DIST = 2000f;
	
	public static final float TILT_MIN = -45f;
	public static final float TILT_MAX = 45f;
	public static final float PITCH_MIN = -15f;
	public static final float PITCH_MAX = 45f;
	
	public static final float MAX_ORBIT_RADIUS = 20000;
	public static final float FAR_MAX_ORBIT_RADIUS = 5000;
	
	public static final float LAGRANGE_OFFSET = 60f;
	
	public static final float BASE_INCR = 800f;
	public static final float BASE_INCR_MOON = 200f;
	
	public static final float STARTING_RADIUS_STAR_BASE = 750f;
	public static final float STARTING_RADIUS_STAR_RANGE = 500f;
	
	public static final float STARTING_RADIUS_MOON_BASE = 300f;
	public static final float STARTING_RADIUS_MOON_RANGE = 100f;
	
	//public static final float MOON_RADIUS_MULT = 0.75f;
	public static final float MOON_RADIUS_MAX_FRACTION_OF_PARENT = 0.33f;
	public static final float MOON_RADIUS_MIN_FRACTION_OF_NORMAL = 0.2f;
	public static final float MOON_RADIUS_MAX_FRACTION_OF_NORMAL = 0.75f;
	public static final float MIN_MOON_RADIUS = 60f;
	//public static final float MAX_MOON_RADIUS = 100;
	
	
	public static final String TAG_FIRST_ORBIT_ONLY = "first_orbit_only";
	public static final String TAG_GIANT_MOON = "around_giant_at_any_offset";
	public static final String TAG_LAGRANGE_ONLY = "lagrange_only";
	public static final String TAG_NOT_IN_NEBULA = "not_in_nebula";
	public static final String TAG_REQUIRES_NEBULA = "requires_nebula";
	
	public static final String TAG_NOT_NEBULA_UNLESS_MOON = "not_NEBULA_unless_moon";
	
	public static final String CAT_HAB5 = "cat_hab5";
	public static final String CAT_HAB4 = "cat_hab4";
	public static final String CAT_HAB3 = "cat_hab3";
	public static final String CAT_HAB2 = "cat_hab2";
	public static final String CAT_HAB1 = "cat_hab1";
	
	public static final String CAT_NOTHING = "cat_nothing";
	public static final String CAT_GIANT = "cat_giant";
	
	public static final String COL_LAGRANGE = "lagrange";
	public static final String COL_IN_ASTEROIDS = "in_asteroids";
	public static final String COL_IS_MOON = "is_moon";
	public static final String COL_BINARY = "binary";
	public static final String COL_TRINARY = "trinary";
	
	public static final String NEBULA_DEFAULT = "nebula";
	public static final String NEBULA_AMBER = "nebula_amber";
	public static final String NEBULA_BLUE = "nebula_blue";
	public static final String NEBULA_NONE = "no_nebula";
	
	
	public static Map<StarAge, String> nebulaTypes = new LinkedHashMap<StarAge, String>();
	public static Map<String, WeightedRandomPicker<String>> backgroundsByNebulaType = new LinkedHashMap<String, WeightedRandomPicker<String>>(); 
	
	
	public static List<TerrainGenPlugin> terrainPlugins = new ArrayList<TerrainGenPlugin>();
	public static void addTerrainGenPlugin(TerrainGenPlugin plugin) {
		terrainPlugins.add(0, plugin);
	}
	public static void removeTerrainGenPlugin(TerrainGenPlugin plugin) {
		terrainPlugins.remove(plugin);
	}
	public static Random random = new Random();
	
	static {
		terrainPlugins.add(new RingGenPlugin());
		terrainPlugins.add(new AsteroidBeltGenPlugin());
		terrainPlugins.add(new MagFieldGenPlugin());
		
		terrainPlugins.add(new NebulaSmallGenPlugin());
		terrainPlugins.add(new AsteroidFieldGenPlugin());
		
		terrainPlugins.add(new AccretionDiskGenPlugin());
		
		nebulaTypes.put(StarAge.YOUNG, NEBULA_BLUE);
		nebulaTypes.put(StarAge.AVERAGE, NEBULA_DEFAULT);
		nebulaTypes.put(StarAge.OLD, NEBULA_AMBER);
		
		nebulaTypes.put(StarAge.ANY, NEBULA_DEFAULT);
		
		updateBackgroundPickers();
	}
	
	public static void updateBackgroundPickers()
	{
		WeightedRandomPicker<String> picker;
		picker = new WeightedRandomPicker<String>(random);
		picker.add("graphics/backgrounds/background2.jpg", 10);
		picker.add("graphics/backgrounds/background4.jpg", 10);
		backgroundsByNebulaType.put(NEBULA_NONE, picker);
		
		picker = new WeightedRandomPicker<String>(random);
		picker.add("graphics/backgrounds/background5.jpg", 10);
		backgroundsByNebulaType.put(NEBULA_BLUE, picker);
		
		picker = new WeightedRandomPicker<String>(random);
		picker.add("graphics/backgrounds/background6.jpg", 10);
		backgroundsByNebulaType.put(NEBULA_AMBER, picker);
		
		picker = new WeightedRandomPicker<String>(random);
		picker.add("graphics/backgrounds/background1.jpg", 10);
		picker.add("graphics/backgrounds/background2.jpg", 10);
		backgroundsByNebulaType.put(NEBULA_DEFAULT, picker);
	}
	
	public static boolean DEBUG = Global.getSettings().isDevMode();
	
	public static TerrainGenPlugin pickTerrainGenPlugin(TerrainGenDataSpec terrainData, GenContext context) {
		for (TerrainGenPlugin plugin : terrainPlugins) {
			if (plugin.wantsToHandle(terrainData, context)) return plugin;
		}
		return null;
	}

	
	public static class GeneratedPlanet {
		public SectorEntityToken parent;
		public PlanetAPI planet;
		public float orbitDays;
		public float orbitRadius;
		public float orbitAngle;
		public boolean isMoon;
		public GeneratedPlanet(SectorEntityToken parent, PlanetAPI planet, boolean isMoon, float orbitDays, float orbitRadius, float orbitAngle) {
			this.parent = parent;
			this.planet = planet;
			this.isMoon = isMoon;
			this.orbitDays = orbitDays;
			this.orbitRadius = orbitRadius;
			this.orbitAngle = orbitAngle;
		}
	}

	public static enum LagrangePointType {
//		L1,
//		L2,
//		L3,
		L4,
		L5,
	}
	
	
	public static class LagrangePoint {
		public GeneratedPlanet parent;
		public LagrangePointType type;
		public LagrangePoint(GeneratedPlanet parent, LagrangePointType type) {
			this.parent = parent;
			this.type = type;
		}
	}
	
	
	public static class GenResult {
		public float orbitalWidth;
		public boolean onlyIncrementByWidth = false;
		public List<SectorEntityToken> entities = new ArrayList<SectorEntityToken>();
		public GenContext context;
		
		public GenResult() {
			
		}
	}
	
	public static class GenContext {
		public StarSystemGenerator gen;
		public List<GeneratedPlanet> generatedPlanets = new ArrayList<GeneratedPlanet>();
		
		public Set<String> excludeCategories = new LinkedHashSet<String>();
		
		//public NamePick parentNamePick = null;
		public StarSystemAPI system;
		public SectorEntityToken center;
		public StarGenDataSpec starData;
		public PlanetAPI parent;
		public int orbitIndex = -1;
		
		public int startingOrbitIndex = 0;
		public String age;
		
		public float currentRadius;
		public String parentCategory;
		public int parentOrbitIndex;
		public float parentRadiusOverride = -1;
		
		public GeneratedPlanet lagrangeParent = null;
		public LagrangePointType lagrangePointType = null;
		
		public List<String> multipliers = new ArrayList<String>();
		public float maxOrbitRadius;
		
		public Map<Object, Object> customData = new LinkedHashMap<Object, Object>();
		
		public GenContext(StarSystemGenerator gen, StarSystemAPI system, SectorEntityToken center,
				StarGenDataSpec starData, PlanetAPI parent,
				//NamePick parentNamePick, 
				int orbitIndex,
				String age, float currentRadius, float maxOrbitRadius, String parentCategory, int parentOrbitIndex) {
			super();
			//this.parentNamePick = parentNamePick;
			this.maxOrbitRadius = maxOrbitRadius;
			this.gen = gen;
			this.system = system;
			this.center = center;
			this.starData = starData;
			this.parent = parent;
			this.startingOrbitIndex = orbitIndex;
			this.orbitIndex = 0;
			this.age = age;
			this.currentRadius = currentRadius;
			this.parentCategory = parentCategory;
			this.parentOrbitIndex = parentOrbitIndex;
		}
		
	}
	
	//private static long index = 0;
	
	protected StarAge constellationAge;
	
	protected StarSystemType systemType = StarSystemType.SINGLE;
	protected StarAge starAge;
	protected SectorAPI sector;
	protected StarSystemAPI system;
	protected LocationAPI hyper;
	
	protected PlanetAPI star;
	protected PlanetAPI secondary;
	protected PlanetAPI tertiary;
	
	protected SectorEntityToken systemCenter;
	protected float centerRadius = 0f;
	protected AgeGenDataSpec constellationAgeData;
	protected AgeGenDataSpec starAgeData;
	protected StarGenDataSpec starData;
	protected String nebulaType;
	protected String backgroundName;
//	protected NamePick constellationName;
//	protected NamePick primaryName;
//	protected NamePick secondaryName;
//	protected NamePick tertiaryName;
	
	protected Map<SectorEntityToken, PlanetAPI> lagrangeParentMap = new LinkedHashMap<SectorEntityToken, PlanetAPI>();
	protected Map<SectorEntityToken, List<SectorEntityToken>> allNameableEntitiesAdded = new LinkedHashMap<SectorEntityToken, List<SectorEntityToken>>();
	protected CustomConstellationParams params;

	
	public StarSystemGenerator(CustomConstellationParams params) {
		this.params = params;
		this.constellationAge = params.age;
		
		if (this.constellationAge == StarAge.ANY) {
			WeightedRandomPicker<StarAge> picker = new WeightedRandomPicker<StarAge>(random);
			picker.add(StarAge.AVERAGE);
			picker.add(StarAge.OLD);
			picker.add(StarAge.YOUNG);
			this.constellationAge = picker.pick();
		}
		
		constellationAgeData = (AgeGenDataSpec) Global.getSettings().getSpec(AgeGenDataSpec.class, constellationAge.name(), true);
	}

	
	
	
	
	public void pickNebulaAndBackground() {
		boolean hasNebula = constellationAgeData.getProbNebula() > random.nextFloat();
		if (params != null && params.forceNebula) hasNebula = true;
		
//		hasNebula = true;
//		hasNebula = false;
		
		nebulaType = NEBULA_NONE;
		if (hasNebula) {
			nebulaType = nebulaTypes.get(constellationAge);
		}
		
		WeightedRandomPicker<String> bgPicker = backgroundsByNebulaType.get(nebulaType);
		backgroundName = bgPicker.pick();
	}
	
	
	
	
	public Constellation generate() {

//		if (true) {
//			params = new CustomConstellationParams(StarAge.OLD);
//			params.numStars = 1;
//			params.starTypes.add(StarTypes.YELLOW);
//			params.systemTypes.add(StarSystemType.SINGLE);
//			
//			this.constellationAge = params.age;
//			constellationAgeData = (AgeGenDataSpec) Global.getSettings().getSpec(AgeGenDataSpec.class, constellationAge.name(), true);
//		}
		
		//random = new Random(12312312312L);
		
		//DEBUG = false;
		
		lagrangeParentMap = new LinkedHashMap<SectorEntityToken, PlanetAPI>();
		allNameableEntitiesAdded = new LinkedHashMap<SectorEntityToken, List<SectorEntityToken>>();
		
		SectorAPI sector = Global.getSector();
		Vector2f loc = new Vector2f();
		if (params != null && params.location != null) {
			loc = new Vector2f(params.location);
		} else {
			loc = new Vector2f(sector.getPlayerFleet().getLocation());
			loc.x = (int) loc.x;
			loc.y = (int) loc.y;
		}
		
		
		pickNebulaAndBackground();
		
		List<StarSystemAPI> systems = new ArrayList<StarSystemAPI>();
		int stars = (int) Math.round(getNormalRandom(1, 7));
		if (params != null && params.numStars > 0) {
			stars = params.numStars;
		} else if (params != null && params.minStars > 0 && params.maxStars > 0) {
			stars = (int) Math.round(getNormalRandom(params.minStars, params.maxStars));
		}
		
//		constellationName = ProcgenUsedNames.pickName(NameGenData.TAG_CONSTELLATION, null);
//		ProcgenUsedNames.notifyUsed(constellationName.nameWithRomanSuffixIfAny);
//		Global.getSettings().greekLetterReset();
		
		for (int i = 0; i < stars; i++) {
			generateSystem(new Vector2f(0, 0));
			if (system != null) {
				systems.add(system);
			}
		}
		
		
		SpringSystem springs = ConstellationGen.doConstellationLayout(systems, random, loc);
		Global.getSector().getHyperspace().updateAllOrbits();
		
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(plugin);
		
		float minRadius = plugin.getTileSize() * 2f;
		for (StarSystemAPI curr : systems) {
			float radius = curr.getMaxRadiusInHyperspace();
			editor.clearArc(curr.getLocation().x, curr.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
			editor.clearArc(curr.getLocation().x, curr.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
		}
		
		for (SpringConnection conn : springs.connections) {
			if (!conn.pull) continue;
			float r1 = ((StarSystemAPI)conn.from.custom).getMaxRadiusInHyperspace();
			float r2 = ((StarSystemAPI)conn.to.custom).getMaxRadiusInHyperspace();
			float dist = Misc.getDistance(conn.from.loc, conn.to.loc);
			
			float radius = Math.max(0, dist * 0.67f - r1 - r2);
			
//			float x = (conn.from.loc.x + conn.to.loc.x) * 0.5f;
//			float y = (conn.from.loc.y + conn.to.loc.y) * 0.5f;
//			editor.clearArc(x, y, 0, radius + minRadius * 0.5f, 0, 360f);
//			editor.clearArc(x, y, 0, radius + minRadius, 0, 360f, 0.25f);
			
			Vector2f diff = Vector2f.sub(conn.to.loc, conn.from.loc, new Vector2f());
			float x = conn.from.loc.x + diff.x * 0.33f;
			float y = conn.from.loc.y + diff.y * 0.33f;
			editor.clearArc(x, y, 0, radius + minRadius * 1f, 0, 360f);
			editor.clearArc(x, y, 0, radius + minRadius * 2f, 0, 360f, 0.25f);
			
			x = conn.from.loc.x + diff.x * 0.67f;
			y = conn.from.loc.y + diff.y * 0.67f;
			editor.clearArc(x, y, 0, radius + minRadius * 1f, 0, 360f);
			editor.clearArc(x, y, 0, radius + minRadius * 2f, 0, 360f, 0.25f);
		}
		
		ConstellationType type = ConstellationType.NORMAL;
		if (!NEBULA_NONE.equals(nebulaType)) {
			type = ConstellationType.NEBULA;
		}
		
		Constellation c = new Constellation(type, constellationAge);
		//c.setType(ConstellationType.NORMAL); // for now; too many end up being called "Nebula" otherwise
		c.getSystems().addAll(systems);
		c.setLagrangeParentMap(lagrangeParentMap);
		c.setAllEntitiesAdded(allNameableEntitiesAdded);

		
//		SalvageEntityGeneratorOld seg = new SalvageEntityGeneratorOld(c);
//		seg.addSalvageableEntities();
		

		NameAssigner namer = new NameAssigner(c);
		namer.setSpecialNamesProbability(0.37f);
		namer.assignNames(params.name, params.secondaryName);
		
		for (SectorEntityToken entity : allNameableEntitiesAdded.keySet()) {
			if (entity instanceof PlanetAPI && entity.getMarket() != null) {
				entity.getMarket().setName(entity.getName());
			}
		}
		
		//if (systems.size() > 1) {
			for (StarSystemAPI system : systems) {
				system.setConstellation(c);
			}
		//}
		
		return c;
	}
	
	
	public void generateSystem(Vector2f loc) {
		//index++;
		
		systemType = pickSystemType(constellationAge);
		
//		if (systemType == StarSystemType.NEBULA) {
//			System.out.println("wfwefwe1231232");
//		}
		
		String uuid = Misc.genUID();
		
//		String id = "system_" + index;
//		String name = "System " + index;
		
		String id = "system_" + uuid;
		String name = "System " + uuid;
		
//		String base = constellationName.nameWithRomanSuffixIfAny;
//		if (constellationName.secondaryWithRomanSuffixIfAny != null) {
//			base = constellationName.secondaryWithRomanSuffixIfAny;
//		}
//		String name = Global.getSettings().getNextGreekLetter(constellationName) + " " + base;
//		String id = name.toLowerCase();
		
		
		//if (systemType == StarSystemType.NEBULA) name += " Nebula";
		
		if (!initSystem(name, loc)) {
			cleanup();
			return;
		}
		
		star = null;
		secondary = null;
		tertiary = null;
		systemCenter = null;
		
		
		
		if (!addStars(id)) {
			cleanup();
			return;
		}
		
//		if (systemType == StarSystemType.NEBULA) {
//			if (star.getSpec().isBlackHole()) {
//				System.out.println("wefwefew");
//			}
//		}
		
		updateAgeAfterPickingStar();
		
		float binaryPad = 1500f;
		
		float maxOrbitRadius = MAX_ORBIT_RADIUS;
		if (systemType == StarSystemType.BINARY_FAR || 
				systemType == StarSystemType.TRINARY_1CLOSE_1FAR ||
				systemType == StarSystemType.TRINARY_2FAR) {
			maxOrbitRadius -= FAR_MAX_ORBIT_RADIUS + binaryPad;
		}
		GenResult result = addPlanetsAndTerrain(MAX_ORBIT_RADIUS);
		//addJumpPoints(result);
		float primaryOrbitalRadius = star.getRadius();
		if (result != null) {
			primaryOrbitalRadius = result.orbitalWidth * 0.5f;
		}
		
		// add far stars, if needed
		float orbitAngle = random.nextFloat() * 360f;
		float baseOrbitRadius = primaryOrbitalRadius + binaryPad;
		float orbitDays = baseOrbitRadius / (3f + random.nextFloat() * 2f);
		if (systemType == StarSystemType.BINARY_FAR && secondary != null) {
			addFarStar(secondary, orbitAngle, baseOrbitRadius, orbitDays);
		} else if (systemType == StarSystemType.TRINARY_1CLOSE_1FAR && tertiary != null) {
			addFarStar(tertiary, orbitAngle, baseOrbitRadius, orbitDays);
		} else if (systemType == StarSystemType.TRINARY_2FAR) {
			addFarStar(secondary, orbitAngle, baseOrbitRadius, orbitDays);
			addFarStar(tertiary, orbitAngle + 60f + 180f * random.nextFloat(), baseOrbitRadius, orbitDays);
		}
		

		if (systemType == StarSystemType.NEBULA) {
			star.setSkipForJumpPointAutoGen(true);
		}
		
		addJumpPoints(result, false);
		
		if (systemType == StarSystemType.NEBULA) {
			system.removeEntity(star);
			StarCoronaTerrainPlugin coronaPlugin = Misc.getCoronaFor(star);
			if (coronaPlugin != null) {
				system.removeEntity(coronaPlugin.getEntity());
			}
			system.setStar(null);
			system.initNonStarCenter();
			for (SectorEntityToken entity : system.getAllEntities()) {
				if (entity.getOrbitFocus() == star ||
					entity.getOrbitFocus() == system.getCenter()) {
					entity.setOrbit(null);
				}
			}
			system.getCenter().addTag(Tags.AMBIENT_LS);
		}
		
		system.autogenerateHyperspaceJumpPoints(true, false);
		
		if (systemType == StarSystemType.NEBULA) {
			//system.addEntity(star);
			system.setStar(star);
			//system.removeEntity(system.getCenter());
			//system.setCenter(null);
		}
		
		addStableLocations();
		
		addSystemwideNebula();
	}
	
	
	protected void addFarStar(PlanetAPI farStar, float orbitAngle, float baseOrbitRadius, float orbitPeriod) {
		float min = 0;
		float max = 2;
		int numOrbits = (int) Math.round(getNormalRandom(min, max));
		GenResult resultFar = null;
		if (numOrbits > 0) {
			float currentRadius = farStar.getRadius() + STARTING_RADIUS_STAR_BASE + STARTING_RADIUS_STAR_RANGE * random.nextFloat();

			StarGenDataSpec farData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, farStar.getSpec().getPlanetType(), false);
			StarAge farAge = farData.getAge();
			if (farAge == StarAge.ANY) {
				farAge = constellationAge;
			}
			
			//GenContext context = new GenContext(this, system, star, starData, 
			GenContext context = new GenContext(this, system, farStar, farData, 
								null, 0, farAge.name(), currentRadius, FAR_MAX_ORBIT_RADIUS, null, -1);
			
			
			resultFar = addOrbitingEntities(context, numOrbits, false, true, false, true);
			resultFar.context = context;
		}
		
		if (resultFar != null) {
			baseOrbitRadius += resultFar.orbitalWidth * 0.5f;
		}
		
		SectorEntityToken center = star;
		if (systemType == StarSystemType.TRINARY_1CLOSE_1FAR) {
			center = systemCenter;
		}
		farStar.setCircularOrbit(center, orbitAngle, baseOrbitRadius, orbitPeriod);
		
		if (resultFar != null) {
			addJumpPoints(resultFar, true);
		}
	}
	
	
	
	protected StarSystemType pickSystemType(StarAge constellationAge) {
//		if (true) {
//			return StarSystemType.BINARY_CLOSE;
//		}
		
		if (params != null && !params.systemTypes.isEmpty()) {
			return params.systemTypes.remove(0);
		}
		
		
		WeightedRandomPicker<StarSystemType> picker = new WeightedRandomPicker<StarSystemType>(random);
		for (StarSystemType type : EnumSet.allOf(StarSystemType.class)) {
			Object test = Global.getSettings().getSpec(LocationGenDataSpec.class, type.name(), true);
			if (test == null) continue;
			LocationGenDataSpec data = (LocationGenDataSpec) test;
			
			boolean nebulaStatusOk = NEBULA_NONE.equals(nebulaType) || !data.hasTag(TAG_NOT_IN_NEBULA);
			nebulaStatusOk &= !NEBULA_NONE.equals(nebulaType) || !data.hasTag(TAG_REQUIRES_NEBULA);

			if (!nebulaStatusOk) continue;
			
			float freq = 0f;
			switch (constellationAge) {
			case AVERAGE:
				freq = data.getFreqAVERAGE();
				break;
			case OLD:
				freq = data.getFreqOLD();
				break;
			case YOUNG:
				freq = data.getFreqYOUNG();
				break;
			}
			picker.add(type, freq);
		}
		
		return picker.pick();
		
		//return StarSystemType.TRINARY_1CLOSE_1FAR;
		//return StarSystemType.TRINARY_2FAR;
		//return StarSystemType.TRINARY_2CLOSE;
		//return StarSystemType.BINARY_CLOSE;
		//return StarSystemType.BINARY_FAR;
		//return StarSystemType.NORMAL;
	}
	
	
	protected boolean addStars(String id) {
		if (systemType == StarSystemType.BINARY_CLOSE || 
				systemType == StarSystemType.TRINARY_1CLOSE_1FAR ||
				systemType == StarSystemType.TRINARY_2CLOSE) {
			system.initNonStarCenter();
			systemCenter = system.getCenter();
		}
		
//		if (systemType == StarSystemType.NEBULA) {
//			if (system.getId().equals("system 7f6")) {
//				System.out.println("wefwefew");
//			}
//		}
		
		PlanetSpecAPI starSpec = pickStar(constellationAge);
		if (starSpec == null) return false;
		
		starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, starSpec.getPlanetType(), false);
		float radius = getRadius(starData.getMinRadius(), starData.getMaxRadius());
		
		float corona = radius * (starData.getCoronaMult() + starData.getCoronaVar() * (random.nextFloat() - 0.5f));
		if (corona < starData.getCoronaMin()) corona = starData.getCoronaMin();
		
		//corona += 2000f;

		star = system.initStar(id, // unique id for this star 
										    starSpec.getPlanetType(),  // id in planets.json
										    radius, 		  // radius (in pixels at default zoom)
										    corona,  // corona radius, from star edge 
										    starData.getSolarWind(),
										    (float) (starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare()) * random.nextFloat()),
										    starData.getCrLossMult()
											);
		
		if (systemType == StarSystemType.NEBULA) {
			star.addTag(Tags.AMBIENT_LS);
		}
		
		if (systemCenter == null) {
			systemCenter = star;
			centerRadius = star.getRadius();
		}
		
		
		// create and switch aronud stars so that the primary is always the largest
		if (systemType == StarSystemType.BINARY_CLOSE) {
			secondary = addRandomStar(id + "_b", system.getBaseName() + " B");
			if (secondary == null) return false;
			switchPrimaryAndSecondaryIfNeeded(true);
		} else if (systemType == StarSystemType.BINARY_FAR) {
			secondary = addRandomStar(id + "_b", system.getBaseName() + " B");
			if (secondary == null) return false;
			switchPrimaryAndSecondaryIfNeeded(true);
			
			centerRadius = star.getRadius();
			
			secondary.setLightColorOverrideIfStar(pickLightColorForStar(secondary));
		} else if (systemType == StarSystemType.TRINARY_2CLOSE ||
				   systemType == StarSystemType.TRINARY_1CLOSE_1FAR ||
				   systemType == StarSystemType.TRINARY_2FAR) {
			secondary = addRandomStar(id + "_b", system.getBaseName() + " B");
			if (secondary == null) return false;
			switchPrimaryAndSecondaryIfNeeded(true);
			
			tertiary = addRandomStar(id + "_c", system.getBaseName() + " C");
			if (tertiary == null) return false;
			switchPrimaryAndTertiaryIfNeeded(true);
			
			if (systemType == StarSystemType.TRINARY_1CLOSE_1FAR) {
				tertiary.setLightColorOverrideIfStar(pickLightColorForStar(tertiary));
			} else if (systemType == StarSystemType.TRINARY_2FAR) {
				secondary.setLightColorOverrideIfStar(pickLightColorForStar(secondary));
				tertiary.setLightColorOverrideIfStar(pickLightColorForStar(tertiary));
			}
		}
		
		// make close stars orbit common center
		if (systemType == StarSystemType.BINARY_CLOSE || systemType == StarSystemType.TRINARY_1CLOSE_1FAR) {
			float dist = STARTING_RADIUS_STAR_BASE + STARTING_RADIUS_STAR_RANGE * random.nextFloat();
			
			float r1 = star.getRadius();
			float r2 = secondary.getRadius();
			if (star.getSpec().getPlanetType().equals("black_hole")) r1 *= 5f;
			if (secondary.getSpec().getPlanetType().equals("black_hole")) r2 *= 5f;
			
			float totalRadius = r1 + r2;
			dist += totalRadius;
			
			float orbitPrimary = dist * r2 / totalRadius;
			float orbitSecondary = dist * r1 / totalRadius;
			
			centerRadius = Math.max(orbitPrimary + star.getRadius(), orbitSecondary + secondary.getRadius()); 
			
			float anglePrimary = random.nextFloat() * 360f;
			float orbitDays = dist / (30f + random.nextFloat() * 50f); 
			
			star.setCircularOrbit(system.getCenter(), anglePrimary, orbitPrimary, orbitDays);
			secondary.setCircularOrbit(system.getCenter(), anglePrimary + 180f, orbitSecondary, orbitDays);
			
//			if (systemType == StarSystemType.TRINARY_1CLOSE_1FAR && tertiary != null) {
//				tertiary.setCircularOrbit(system.getCenter(), 
//						tertiary.getCircularOrbitAngle(),
//						tertiary.getCircularOrbitRadius(),
//						tertiary.getCircularOrbitPeriod());
//			}
		} else if (systemType == StarSystemType.TRINARY_2CLOSE) {
			float dist = STARTING_RADIUS_STAR_BASE + STARTING_RADIUS_STAR_RANGE * random.nextFloat();
			dist += star.getRadius();
			
			float anglePrimary = random.nextFloat() * 360f;
			float orbitDays = dist / (20f + random.nextFloat() * 80f);
			
			// smaller dist for primary/secondary so that their gravity wells get generated first
			// and are closer to the center
			star.setCircularOrbit(system.getCenter(), anglePrimary, dist - 10, orbitDays);
			secondary.setCircularOrbit(system.getCenter(), anglePrimary + 120f, dist - 5, orbitDays);
			tertiary.setCircularOrbit(system.getCenter(), anglePrimary + 240f, dist, orbitDays);
			
			centerRadius = dist + star.getRadius();
		} else {
			star.getLocation().set(0, 0);
		}
		

		if (star != null) {
			starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), false);
		}
//		if (systemCenter instanceof PlanetAPI) {
//			centerRadius = star.getRadius();
//		}
		

		setDefaultLightColorBasedOnStars();
		
		
		if (star != null) {
			ArrayList<SectorEntityToken> list = new ArrayList<SectorEntityToken>();
			list.add(star);
			allNameableEntitiesAdded.put(star, list);
			system.setStar(star);
		}
		if (secondary != null) {
			ArrayList<SectorEntityToken> list = new ArrayList<SectorEntityToken>();
			list.add(secondary);
			allNameableEntitiesAdded.put(secondary, list);
			system.setSecondary(secondary);
		}
		if (tertiary != null){
			ArrayList<SectorEntityToken> list = new ArrayList<SectorEntityToken>();
			list.add(tertiary);
			allNameableEntitiesAdded.put(tertiary, list);
			system.setTertiary(tertiary);
		}
		
		setBlackHoleIfBlackHole(star);
		setBlackHoleIfBlackHole(secondary);
		setBlackHoleIfBlackHole(tertiary);
		
		setPulsarIfNeutron(star);
		setPulsarIfNeutron(secondary);
		setPulsarIfNeutron(tertiary);
		
		return true;
	}
	
	protected void setBlackHoleIfBlackHole(PlanetAPI star) {
		if (star == null) return;
		
		if (star.getSpec().getPlanetType().equals("black_hole")) {
			StarCoronaTerrainPlugin coronaPlugin = Misc.getCoronaFor(star);
			if (coronaPlugin != null) {
				system.removeEntity(coronaPlugin.getEntity());
			}
			
			StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), false);
			float corona = star.getRadius() * (starData.getCoronaMult() + starData.getCoronaVar() * (random.nextFloat() - 0.5f));
			if (corona < starData.getCoronaMin()) corona = starData.getCoronaMin();
			
			SectorEntityToken eventHorizon = system.addTerrain(Terrain.EVENT_HORIZON, 
					new CoronaParams(star.getRadius() + corona, (star.getRadius() + corona) / 2f,
									star, starData.getSolarWind(), 
									(float) (starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare()) * random.nextFloat()),
									starData.getCrLossMult()));
			eventHorizon.setCircularOrbit(star, 0, 0, 100);
		}
	}
	
	protected void setPulsarIfNeutron(PlanetAPI star) {
		if (star == null) return;
		
		if (star.getSpec().getPlanetType().equals("star_neutron")) {
			StarCoronaTerrainPlugin coronaPlugin = Misc.getCoronaFor(star);
			if (coronaPlugin != null) {
				system.removeEntity(coronaPlugin.getEntity());
			}
			
			system.addCorona(star, 
							300, // radius
							3, // wind
							0, // flares
							3); // cr loss
			
			
			StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), false);
			float corona = star.getRadius() * (starData.getCoronaMult() + starData.getCoronaVar() * (random.nextFloat() - 0.5f));
			if (corona < starData.getCoronaMin()) corona = starData.getCoronaMin();
			
			SectorEntityToken eventHorizon = system.addTerrain(Terrain.PULSAR_BEAM, 
					new CoronaParams(star.getRadius() + corona, (star.getRadius() + corona) / 2f,
							star, starData.getSolarWind(), 
							(float) (starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare()) * random.nextFloat()),
							starData.getCrLossMult()));
			eventHorizon.setCircularOrbit(star, 0, 0, 100);
		}
	}
	
	protected void switchPrimaryAndSecondaryIfNeeded(boolean sizeDownSmaller) {
		if (star == null || secondary == null) return;
		
		if (star.getRadius() < secondary.getRadius()) {
			star = secondary;
			secondary = system.getStar();
			system.setStar(star);
			
			String temp = star.getName();
			star.setName(secondary.getName());
			secondary.setName(temp);
			
			temp = star.getId();
			star.setId(secondary.getId());
			secondary.setId(temp);
			
			if (sizeDownSmaller) {
				secondary.setRadius(Math.min(secondary.getRadius(), star.getRadius() * 0.67f));
			}
			
			if (secondary == systemCenter) {
				systemCenter = star;
				centerRadius = systemCenter.getRadius();
			}
		}
	}
	
	protected void switchPrimaryAndTertiaryIfNeeded(boolean sizeDownSmaller) {
		if (star == null || tertiary == null) return;
		
		if (star.getRadius() < tertiary.getRadius()) {
			star = tertiary;
			tertiary = system.getStar();
			system.setStar(star);
			
			String temp = star.getName();
			star.setName(tertiary.getName());
			tertiary.setName(temp);
			
			temp = star.getId();
			star.setId(tertiary.getId());
			tertiary.setId(temp);
			
			if (sizeDownSmaller) {
				tertiary.setRadius(Math.min(tertiary.getRadius(), star.getRadius() * 0.67f));
			}
			
			if (tertiary == systemCenter) {
				systemCenter = star;
				centerRadius = systemCenter.getRadius();
			}
		}
	}
	
	
	
	protected void addStableLocations() {
		int min = 1;
		int max = 3;
		
//		int planets = system.getPlanets().size(); // includes stars
//		max = Math.min(max, planets/2);
//		if (min > max) min = max;
		
		//int num = (int) Math.round(getNormalRandom(min, max));
		int num = random.nextInt(max + 1 - min) + min;
		
		if (num == min && random.nextFloat() < 0.25f) {
			num = 0;
		}
		
		addStableLocations(system, num);
	}
	public static void addStableLocations(StarSystemAPI system, int num) {
		for (int i = 0; i < num; i++) {
			LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
			weights.put(LocationType.STAR_ORBIT, 10f);
			weights.put(LocationType.OUTER_SYSTEM, 10f);
			weights.put(LocationType.L_POINT, 10f);
			weights.put(LocationType.IN_SMALL_NEBULA, 2f);
			WeightedRandomPicker<EntityLocation> locs = BaseThemeGenerator.getLocations(random, system, null, 100f, weights);
			EntityLocation loc = locs.pick();
			
			AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(system, loc, Entities.STABLE_LOCATION, Factions.NEUTRAL);
			//if (DEBUG && added != null) System.out.println("    Added stable location");
			
			if (added != null) {
				BaseThemeGenerator.convertOrbitPointingDown(added.entity);
			}
		}
	}
	
	
	protected void addJumpPoints(GenResult result, boolean farStarMode) {
		
		//float outerRadius = star.getRadius() + STARTING_RADIUS_STAR_BASE + STARTING_RADIUS_STAR_RANGE * random.nextFloat();
		float outerRadius = centerRadius + STARTING_RADIUS_STAR_BASE + STARTING_RADIUS_STAR_RANGE * random.nextFloat();
		if (result != null) {
			outerRadius = result.orbitalWidth / 2f + 500f;
		}
		
		if (farStarMode) {
			//if (result.context.orbitIndex < 0) {
			if (result.context.orbitIndex < 0 && random.nextFloat() < 0.5f) {
				return;
			}
			SectorEntityToken farStar = result.context.center;
			String name = "Omega Jump-point";
			if (result.context.center == tertiary) {
				name = "Omicron Jump-point";
			}
			JumpPointAPI point = Global.getFactory().createJumpPoint(null, name);
			point.setStandardWormholeToHyperspaceVisual();
			float orbitDays = outerRadius / (15f + random.nextFloat() * 5f);
			//point.setCircularOrbit(star, random.nextFloat() * 360f, outerRadius, orbitDays);
			point.setCircularOrbit(farStar, random.nextFloat() * 360f, outerRadius, orbitDays);
			system.addEntity(point);
			
			return;
		}
		
		
		//JumpPointAPI point = Global.getFactory().createJumpPoint(null, system.getBaseName() + " Fringe");
		JumpPointAPI point = Global.getFactory().createJumpPoint(null, "Fringe Jump-point");
		point.setStandardWormholeToHyperspaceVisual();
		float orbitDays = outerRadius / (15f + random.nextFloat() * 5f);
		//point.setCircularOrbit(star, random.nextFloat() * 360f, outerRadius, orbitDays);
		point.setCircularOrbit(systemCenter, random.nextFloat() * 360f, outerRadius, orbitDays);
		system.addEntity(point);

		
		// to make sure that "is this location clear" calculations below always work
		system.updateAllOrbits();
		
		
		if (result != null) {
			float halfway = outerRadius * 0.5f;
			
			WeightedRandomPicker<LagrangePoint> inner = new WeightedRandomPicker<LagrangePoint>(random);
			WeightedRandomPicker<LagrangePoint> outer = new WeightedRandomPicker<LagrangePoint>(random);
			
			int total = 0;
			for (GeneratedPlanet planet : result.context.generatedPlanets) {
				if (planet.isMoon) continue;
				if (planet.planet.getOrbit() == null || planet.planet.getOrbit().getFocus() == null) continue;
				total++;
				
				for (LagrangePointType type : EnumSet.of(LagrangePointType.L4, LagrangePointType.L5)) {
					float orbitRadius = planet.orbitRadius;
					float angleOffset = -LAGRANGE_OFFSET * 0.5f;
					if (type == LagrangePointType.L5) angleOffset = LAGRANGE_OFFSET * 0.5f;
					float angle = planet.orbitAngle + angleOffset;
					Vector2f location = Misc.getUnitVectorAtDegreeAngle(angle + angleOffset);
					location.scale(orbitRadius);
					Vector2f.add(location, planet.planet.getOrbit().getFocus().getLocation(), location);
					
					boolean clear = true;
					for (PlanetAPI curr : system.getPlanets()) {
						float dist = Misc.getDistance(curr.getLocation(), location);
						//System.out.println(type.name() + ": " + location + ", " + "Dist: " + dist + " (to " + curr.getName() + " at " + curr.getLocation() + ")");
						if (dist < 500) {
							clear = false;
							break;
						}
					}
					if (clear) {
						if (planet.orbitRadius < halfway || planet.orbitRadius < 5000f) {
							inner.add(new LagrangePoint(planet, type), 10f);
						} else {
							outer.add(new LagrangePoint(planet, type), 10f);
						}
					}
				}
			}
			
			
			if (outerRadius > 2000f + 5000f * random.nextFloat()) {
				boolean addedOne = false;
				if (!inner.isEmpty()) {
					LagrangePoint p = inner.pick();
					//addJumpPoint(p, p.parent.planet.getName() + " Jump-point");
					String name = "Inner System Jump-point";
					if (systemType == StarSystemType.NEBULA) name = "Inner Jump-point";
					addJumpPoint(p, name);
					addedOne = true;
				}
				
				
				if (!outer.isEmpty() && (random.nextFloat() < outer.getItems().size() * 0.2f || !addedOne)) {
					LagrangePoint p = outer.pick();
					String name = "Outer System Jump-point";
					if (systemType == StarSystemType.NEBULA) name = "Outer Jump-point";
					addJumpPoint(p, name);
					//addJumpPoint(p, "Outer System Jump-point");
					addedOne = true;
				}
				
//				while (!inner.isEmpty()) {
//					LagrangePoint p = inner.pickAndRemove();
//					//addJumpPoint(p, p.parent.planet.getName() + " Jump-point");
//					addJumpPoint(p, system.getBaseName() + " Inner System Jump-point");
//				}
//				while (!outer.isEmpty()) {
//					LagrangePoint p = outer.pickAndRemove();
//					//addJumpPoint(p, p.parent.planet.getName() + " Jump-point");
//					addJumpPoint(p, system.getBaseName() + " Outer System Jump-point");
//				}
			}
		}
		
		//system.autogenerateHyperspaceJumpPoints(true, false);
	}
	
	protected void addJumpPoint(LagrangePoint p, String name) {
		float orbitRadius = p.parent.orbitRadius;
		float orbitDays = p.parent.orbitDays;
		float angleOffset = -LAGRANGE_OFFSET * 0.5f;
		if (p.type == LagrangePointType.L5) angleOffset = LAGRANGE_OFFSET * 0.5f;
		float angle = p.parent.orbitAngle + angleOffset;

		SectorEntityToken focus = p.parent.planet.getOrbitFocus();
		if (focus == null) focus = systemCenter;
		
		JumpPointAPI point = Global.getFactory().createJumpPoint(null, name);
		point.setStandardWormholeToHyperspaceVisual();
		if (!p.parent.planet.isGasGiant()) {
			point.setRelatedPlanet(p.parent.planet);
		}
		//point.setCircularOrbit(star, angle + angleOffset, orbitRadius, orbitDays);
		//point.setCircularOrbit(systemCenter, angle + angleOffset, orbitRadius, orbitDays);
		point.setCircularOrbit(focus, angle + angleOffset, orbitRadius, orbitDays);
		system.addEntity(point);
	}
	
	public static float addOrbitingEntities(StarSystemAPI system, SectorEntityToken parentStar, StarAge age,
			int min, int max, float startingRadius,
			int nameOffset, boolean withSpecialNames) {
		return addOrbitingEntities(system, parentStar, age, min, max, startingRadius, nameOffset, withSpecialNames, true);
	}
	
	public static float addOrbitingEntities(StarSystemAPI system, SectorEntityToken parentStar, StarAge age,
										   int min, int max, float startingRadius,
										   int nameOffset, boolean withSpecialNames,
										   boolean allowHabitable) {
		CustomConstellationParams p = new CustomConstellationParams(age);
		p.forceNebula = true; // not sure why this is here; should avoid small nebula at lagrange points though (but is that desired?)
		
		StarSystemGenerator gen = new StarSystemGenerator(p);
		gen.system = system;
		gen.starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, system.getStar().getSpec().getPlanetType(), false);
		gen.starAge = age;
		gen.constellationAge = age;
		gen.starAgeData = (AgeGenDataSpec) Global.getSettings().getSpec(AgeGenDataSpec.class, age.name(), true);
		gen.star = system.getStar();
		
		gen.pickNebulaAndBackground();
		if (system.getType() != null) gen.systemType = system.getType();
		
		
		gen.systemCenter = system.getCenter();
		
		StarGenDataSpec starData = gen.starData;
		PlanetGenDataSpec planetData = null;
		PlanetAPI parentPlanet = null;
		if (parentStar instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) parentStar;
			if (planet.isStar()) {
				starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, planet.getSpec().getPlanetType(), false);
			} else {
				planetData = (PlanetGenDataSpec) Global.getSettings().getSpec(PlanetGenDataSpec.class, planet.getSpec().getPlanetType(), false);
				parentPlanet = planet;
			}
		}
		
		int parentOrbitIndex = -1;
		int startingOrbitIndex = 0;
		
		boolean addingAroundStar = parentPlanet == null;
		float r = 0;
		if (parentStar != null) {
			r = parentStar.getRadius();
		}

		float approximateExtraRadiusPerOrbit = 400f;
		if (addingAroundStar) {
			parentOrbitIndex = -1;
			startingOrbitIndex = (int) ((startingRadius  - r - STARTING_RADIUS_STAR_BASE - STARTING_RADIUS_STAR_RANGE * 0.5f) /
							     (BASE_INCR * 1.25f + approximateExtraRadiusPerOrbit));
			
			if (startingOrbitIndex < 0) startingOrbitIndex = 0;
		} else {
			float dist = 0f;
			if (parentPlanet.getOrbitFocus() != null) {
				dist = Misc.getDistance(parentPlanet.getLocation(), parentPlanet.getOrbitFocus().getLocation());
			}
			parentOrbitIndex = (int) ((dist - r - STARTING_RADIUS_STAR_BASE - STARTING_RADIUS_STAR_RANGE * 0.5f) /
					 		   (BASE_INCR * 1.25f + approximateExtraRadiusPerOrbit));
			startingOrbitIndex = (int) ((startingRadius - STARTING_RADIUS_MOON_BASE - STARTING_RADIUS_MOON_RANGE * 0.5f) /
					 		   (BASE_INCR_MOON * 1.25f));
			
			if (parentOrbitIndex < 0) parentOrbitIndex = 0;
			if (startingOrbitIndex < 0) startingOrbitIndex = 0;
		}
		
		int num = (int) Math.round(getNormalRandom(min, max));
		
		GenContext context = new GenContext(gen, system, gen.systemCenter, starData, 
							parentPlanet, startingOrbitIndex, age.name(), startingRadius, MAX_ORBIT_RADIUS,
							planetData != null ? planetData.getCategory() : null, parentOrbitIndex);
		
		if (!allowHabitable) {
			context.excludeCategories.add(CAT_HAB5);
			context.excludeCategories.add(CAT_HAB4);
			context.excludeCategories.add(CAT_HAB3);
			context.excludeCategories.add(CAT_HAB2);
		}
		
		GenResult result = gen.addOrbitingEntities(context, num, false, addingAroundStar, false, false);
		
		
		Constellation c = new Constellation(ConstellationType.NORMAL, age);
		c.getSystems().add(system);
		c.setLagrangeParentMap(gen.lagrangeParentMap);
		c.setAllEntitiesAdded(gen.allNameableEntitiesAdded);
		c.setLeavePickedNameUnused(true);
		NameAssigner namer = new NameAssigner(c);
		if (withSpecialNames) {
			namer.setSpecialNamesProbability(1f);
		} else {
			namer.setSpecialNamesProbability(0f);
		}
		namer.setRenameSystem(false);
		namer.setStructuralNameOffset(nameOffset);
		namer.assignNames(null, null);
		
		for (SectorEntityToken entity : gen.allNameableEntitiesAdded.keySet()) {
			if (entity instanceof PlanetAPI && entity.getMarket() != null) {
				entity.getMarket().setName(entity.getName());
			}
		}
		
		return result.orbitalWidth * 0.5f;
		
	}
	
	public static void addSystemwideNebula(StarSystemAPI system, StarAge age) {
		CustomConstellationParams p = new CustomConstellationParams(age);
		p.forceNebula = true;
		
		StarSystemGenerator gen = new StarSystemGenerator(p);
		gen.system = system;
		gen.starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, system.getStar().getSpec().getPlanetType(), false);
		gen.starAge = age;
		gen.constellationAge = age;
		gen.starAgeData = (AgeGenDataSpec) Global.getSettings().getSpec(AgeGenDataSpec.class, age.name(), true);
		gen.pickNebulaAndBackground();
		if (system.getType() != null) gen.systemType = system.getType();
		
		gen.addSystemwideNebula();
		
		system.setAge(age);
		system.setHasSystemwideNebula(true);
	}
	
	
	protected void addSystemwideNebula() {
		if (nebulaType.equals(NEBULA_NONE)) return;
		
		
		int w = 128;
		int h = 128;
		
		StringBuilder string = new StringBuilder();
		for (int y = h - 1; y >= 0; y--) {
			for (int x = 0; x < w; x++) {
				string.append("x");
			}
		}
		SectorEntityToken nebula = system.addTerrain(Terrain.NEBULA, new TileParams(string.toString(),
							w, h,
							"terrain", nebulaType, 4, 4, null));
		nebula.getLocation().set(0, 0);
		
		NebulaTerrainPlugin nebulaPlugin = (NebulaTerrainPlugin)((CampaignTerrainAPI)nebula).getPlugin();
		NebulaEditor editor = new NebulaEditor(nebulaPlugin);
		
		editor.regenNoise();
		
		// good medium thickness: 0.6
		//editor.noisePrune(0.8f);
		
		// yes, star age here, despite using constellation age to determine if a nebula to all exists
		// basically: young star in old constellation will have lots of nebula, but of the constellation-age color
		editor.noisePrune(starAgeData.getNebulaDensity());
		//editor.noisePrune(0.75f);
		//editor.noisePrune(0.1f);
		
//		for (float f = 0.1f; f <= 0.9f; f += 0.05f) {
//			editor.noisePrune(f);
//		}
		
		editor.regenNoise();
		
		if (systemType != StarSystemType.NEBULA) {
			for (PlanetAPI planet : system.getPlanets()) {
				
				if (planet.getOrbit() != null && planet.getOrbit().getFocus() != null &&
						planet.getOrbit().getFocus().getOrbit() != null) {
					// this planet is orbiting something that's orbiting something
					// its motion will be relative to its parent moving
					// don't clear anything out for this planet
					continue;
				}
				
				float clearThreshold = 0f; // clear everything by default
				float clearInnerRadius = 0f;
				float clearOuterRadius = 0f;
				Vector2f clearLoc = null;
	
				
				if (!planet.isStar() && !planet.isGasGiant()) {
					clearThreshold = 1f - Math.min(0f, planet.getRadius() / 300f);
					if (clearThreshold > 0.5f) clearThreshold = 0.5f;
				}
				
				Vector2f loc = planet.getLocation();
				if (planet.getOrbit() != null && planet.getOrbit().getFocus() != null) {
					Vector2f focusLoc = planet.getOrbit().getFocus().getLocation();
					float dist = Misc.getDistance(planet.getOrbit().getFocus().getLocation(), loc);
					float width = planet.getRadius() * 4f + 100f;
					if (planet.isStar()) {
						StarCoronaTerrainPlugin corona = Misc.getCoronaFor(planet);
						if (corona != null) {
							width = corona.getParams().bandWidthInEngine * 4f;
						}
						PulsarBeamTerrainPlugin pulsar = Misc.getPulsarFor(planet);
						if (pulsar != null) {
							width = Math.max(width, pulsar.getParams().bandWidthInEngine * 0.5f);
						}
					}
					clearLoc = focusLoc;
					clearInnerRadius = dist - width / 2f;
					clearOuterRadius = dist + width / 2f;
				} else if (planet.getOrbit() == null) {
					float width = planet.getRadius() * 4f + 100f;
					if (planet.isStar()) {
						StarCoronaTerrainPlugin corona = Misc.getCoronaFor(planet);
						if (corona != null) {
							width = corona.getParams().bandWidthInEngine * 4f;
						}
						PulsarBeamTerrainPlugin pulsar = Misc.getPulsarFor(planet);
						if (pulsar != null) {
							width = Math.max(width, pulsar.getParams().bandWidthInEngine * 0.5f);
						}
					}
					clearLoc = loc;
					clearInnerRadius = 0f;
					clearOuterRadius = width;
				}
				
				if (clearLoc != null) {
					float min = nebulaPlugin.getTileSize() * 2f;
					if (clearOuterRadius - clearInnerRadius < min) {
						clearOuterRadius = clearInnerRadius + min;
					}
					editor.clearArc(clearLoc.x, clearLoc.y, clearInnerRadius, clearOuterRadius, 0, 360f, clearThreshold);
				}
			}
		}

		// add a spiral going from the outside towards the star
		float angleOffset = random.nextFloat() * 360f;
		editor.clearArc(0f, 0f, 30000, 31000 + 1000f * random.nextFloat(), 
				angleOffset + 0f, angleOffset + 360f * (2f + random.nextFloat() * 2f), 0.01f, 0f);
		
		// do some random arcs
		int numArcs = (int) (8f + 6f * random.nextFloat());
		//int numArcs = 11;
		
		for (int i = 0; i < numArcs; i++) {
			//float dist = 4000f + 10000f * random.nextFloat();
			float dist = 15000f + 15000f * random.nextFloat();
			float angle = random.nextFloat() * 360f;
			
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
			dir.scale(dist - (2000f + 8000f * random.nextFloat()));
			
			//float tileSize = nebulaPlugin.getTileSize();
			//float width = tileSize * (2f + 4f * random.nextFloat());
			float width = 800f * (1f + 2f * random.nextFloat());
			
			float clearThreshold = 0f + 0.5f * random.nextFloat();
			//clearThreshold = 0f;
			
			editor.clearArc(dir.x, dir.y, dist - width/2f, dist + width/2f, 0, 360f, clearThreshold);
		}
	}
	
	
	protected GenResult addPlanetsAndTerrain(float maxOrbitRadius) {
		boolean hasOrbits = random.nextFloat() < starData.getProbOrbits();
		if (!hasOrbits) return null;
		
		float min = starData.getMinOrbits() + starAgeData.getMinExtraOrbits();
		float max = starData.getMaxOrbits() + starAgeData.getMaxExtraOrbits();
		int numOrbits = (int) Math.round(getNormalRandom(min, max));
		
		if (numOrbits <= 0) return null;

		
		
		float currentRadius = centerRadius + STARTING_RADIUS_STAR_BASE + STARTING_RADIUS_STAR_RANGE * random.nextFloat();

		//GenContext context = new GenContext(this, system, star, starData, 
		GenContext context = new GenContext(this, system, systemCenter, starData, 
							null, 0, starAge.name(), currentRadius, maxOrbitRadius, null, -1);
		
		if (systemType == StarSystemType.BINARY_CLOSE || systemType == StarSystemType.TRINARY_1CLOSE_1FAR) {
			context.multipliers.add(COL_BINARY);
		}
		if (systemType == StarSystemType.TRINARY_2CLOSE) {
			context.multipliers.add(COL_TRINARY);
		}
		
		
		GenResult result = addOrbitingEntities(context, numOrbits, false, true, false, true);
		result.context = context;
		return result;
	}
		
	protected GenResult addOrbitingEntities(GenContext context, int numOrbits, boolean addingMoons, boolean addMoons, boolean parentIsMoon, boolean nothingOk) {
		
		if (DEBUG && context.starData != null) {
			if (addingMoons && context.parent != null) {
				System.out.println("  Adding " + numOrbits + " moon orbits around " + context.parent.getSpec().getPlanetType());
			} else {
				System.out.println("Adding " + numOrbits + " orbits around " + context.starData.getId());
			}
		}
		
		float currentRadius = context.currentRadius;
		float lastIncrementExtra = 0f;
		
		String extraMult = null;
		if (parentIsMoon) extraMult = COL_IS_MOON;

		int extra = 0;
		for (int i = 0; i < numOrbits; i++) {
			
			context.orbitIndex = i + context.startingOrbitIndex + extra;
			//CategoryGenDataSpec categoryData = pickCategory(orbitIndex, i, starAge.name(), starType, context.parentCategory, null, nothingOk);
			CategoryGenDataSpec categoryData = pickCategory(context, extraMult, nothingOk);
			nothingOk = true; // only applies to first pick, after this, we'll have *something*

//			if (!addingMoons) {
//				categoryData = (CategoryGenDataSpec) Global.getSettings().getSpec(CategoryGenDataSpec.class, "cat_giant", true);
//				//categoryData = (CategoryGenDataSpec) Global.getSettings().getSpec(CategoryGenDataSpec.class, "cat_terrain_rings", true);
//			}
//			if (orbitIndex == 0 && addMoons) {
//				categoryData = (CategoryGenDataSpec) Global.getSettings().getSpec(CategoryGenDataSpec.class, "cat_giant", true);
//				//categoryData = (CategoryGenDataSpec) Global.getSettings().getSpec(CategoryGenDataSpec.class, "cat_hab4", true);
//			}
//			if (orbitIndex == 1 && addMoons) {
//			if (!addingMoons) {
//				categoryData = (CategoryGenDataSpec) Global.getSettings().getSpec(CategoryGenDataSpec.class, "cat_giant", true);
//			}
//			} else {
//				categoryData = (CategoryGenDataSpec) Global.getSettings().getSpec(CategoryGenDataSpec.class, "cat_terrain_rings", true);
//			}
			//categoryData = (CategoryGenDataSpec) Global.getSettings().getSpec(CategoryGenDataSpec.class, "cat_terrain_rings", true);
			
			GenResult result = null;
			float incrMult = 1f;

			if (categoryData != null && !CAT_NOTHING.equals(categoryData.getCategory())) {
				//WeightedRandomPicker<EntityGenDataSpec> picker = getPickerForCategory(categoryData, orbitIndex, i, starAge.name(), starType, context.parentCategory, null);
				WeightedRandomPicker<EntityGenDataSpec> picker = getPickerForCategory(categoryData, context, extraMult);
				if (DEBUG) {
					picker.print("  Picking from category " + categoryData.getCategory() + 
							", orbit index " + (context.parent != null ? context.parentOrbitIndex : context.orbitIndex));
				}
				EntityGenDataSpec entityData = picker.pick();
				if (DEBUG) {
					if (entityData == null) {
						System.out.println("  Nothing to pick");
						System.out.println();
					} else {
						System.out.println("  Picked: " + entityData.getId());
						System.out.println();
					}
				}
				
				context.currentRadius = currentRadius;
				//context.orbitIndex = i;
				
				if (entityData instanceof PlanetGenDataSpec) {
					PlanetGenDataSpec planetData = (PlanetGenDataSpec) entityData;
					result = addPlanet(context, planetData, addingMoons, addMoons);

				} else if (entityData instanceof TerrainGenDataSpec) {
					TerrainGenDataSpec terrainData = (TerrainGenDataSpec) entityData;
					result = addTerrain(context, terrainData);
				}
				
				if (result != null) {
					//List<SectorEntityToken> combined = new ArrayList<SectorEntityToken>(result.entities);
					for (SectorEntityToken curr : result.entities) {
						if (context.lagrangeParent != null && !result.entities.isEmpty()) {
							lagrangeParentMap.put(curr, context.lagrangeParent.planet);
						}
						allNameableEntitiesAdded.put(curr, result.entities);
					}
				} else {
					incrMult = 0.5f;
				}
			} else {
				incrMult = 0.5f;
			}
			
			float baseIncr = BASE_INCR;
			if (addingMoons) {
				baseIncr = BASE_INCR_MOON;
			}
			baseIncr *= incrMult;
			
			float increment = baseIncr + baseIncr * 0.5f * random.nextFloat();
			if (result != null) {
				if (result.orbitalWidth > 1000) extra++;
				//increment = Math.max(increment, result.orbitalWidth + baseIncr * 0.5f);
				//increment += result.orbitalWidth;
				increment = Math.max(increment + Math.min(result.orbitalWidth, 300f),
									 result.orbitalWidth + increment * 0.5f);
				if (result.onlyIncrementByWidth) {
					increment = result.orbitalWidth;
				}
				lastIncrementExtra = Math.max(increment * 0.1f, increment - result.orbitalWidth);
			} else {
				lastIncrementExtra = increment;
			}
			currentRadius += increment;
			
			if (currentRadius >= context.maxOrbitRadius) {
				break;
			}
		}
		
		GenResult result = new GenResult();
		result.onlyIncrementByWidth = false;
		result.orbitalWidth = (currentRadius - lastIncrementExtra) * 2f;
		return result;
	}
	
	protected GenResult addTerrain(GenContext context, TerrainGenDataSpec terrainData) {
		TerrainGenPlugin plugin = pickTerrainGenPlugin(terrainData, context);
		if (plugin == null ) return null;
		GenResult result = plugin.generate(terrainData, context);
		
		
		return result;
	}
	
	
	public GenResult addPlanet(GenContext context, PlanetGenDataSpec planetData, boolean isMoon, boolean addMoons) {
		//float orbitRadius = (orbitIndex + 1f) * 1500 + star.getRadius(); 
		float radius = getRadius(planetData.getMinRadius(), planetData.getMaxRadius());
		if (isMoon) {// || context.lagrangeParent != null) {
			//radius *= MOON_RADIUS_MULT;
			float mult = MOON_RADIUS_MIN_FRACTION_OF_NORMAL + 
			random.nextFloat() * (MOON_RADIUS_MAX_FRACTION_OF_NORMAL - MOON_RADIUS_MIN_FRACTION_OF_NORMAL);
			radius *= mult;
			if (radius < MIN_MOON_RADIUS) {
				radius = MIN_MOON_RADIUS;
			}
			float parentRadius = 100000f;
			if (context.parent != null || context.lagrangeParent != null) {
				PlanetAPI parent = context.parent;
				if (context.lagrangeParent != null) {
					parent = context.lagrangeParent.planet;
				}
				parentRadius = parent.getRadius();
			}
			if (context.parentRadiusOverride > 0) {
				parentRadius = context.parentRadiusOverride;
			}
			
			if (parentRadius > MIN_MOON_RADIUS / MOON_RADIUS_MAX_FRACTION_OF_PARENT) {
				float max = parentRadius * MOON_RADIUS_MAX_FRACTION_OF_PARENT;
				if (radius > max) {
					radius = max;
				}
			}
		}
		
		float orbitRadius = context.currentRadius + radius;
		float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
		
		//String planetId = system.getId() + "_planet_" + context.center.getId() + "_" + context.orbitIndex;
		String planetId = context.center.getId() + ":planet_" + context.orbitIndex;
		String planetName = "Planet " + context.orbitIndex;
		if (context.parent != null) {
			planetId = system.getId() + "_moon_" + context.center.getId() + "_" + context.parent.getId() + "_" + context.orbitIndex;
			planetName = context.parent.getName() + " moon " + context.orbitIndex;
		}
		String planetType = planetData.getId();
		SectorEntityToken parent = context.center;
		if (context.parent != null) parent = context.parent;
		
		float angle = random.nextFloat() * 360f;
		
		// if adding a moon to a largange point planet, then parentCategory == null
		// will fail and it'll orbit the parent rather than stay at the point
		if (context.parentCategory == null) {
			if (context.lagrangeParent != null && context.lagrangePointType != null) {
				orbitRadius = context.lagrangeParent.orbitRadius;
				orbitDays = context.lagrangeParent.orbitDays;
				float angleOffset = -LAGRANGE_OFFSET;
				if (context.lagrangePointType == LagrangePointType.L5) angleOffset = LAGRANGE_OFFSET;
				angle = context.lagrangeParent.orbitAngle + angleOffset;
				planetName += " " + context.lagrangePointType.name();
				planetId += "_" + context.lagrangePointType.name();
			}
		}
		
		PlanetAPI planet = system.addPlanet(planetId, parent, planetName, planetType, angle, radius, orbitRadius, orbitDays);
		if (planet.isGasGiant()) {
			if (systemType == StarSystemType.NEBULA) {
				planet.setAutogenJumpPointNameInHyper(system.getBaseName() + ", " + planetName + " Gravity Well");
			}
		}

		float radiusWithMoons = planet.getRadius();
		if (addMoons) {
			boolean hasOrbits = random.nextFloat() < planetData.getProbOrbits();
			float min = planetData.getMinOrbits();
			float max = planetData.getMaxOrbits();
//			double r = random.nextFloat();
//			r *= r;
//			int numOrbits = (int) (min + Math.round((max - min) * r));
			int numOrbits = (int) Math.round(getNormalRandom(min, max));
			
			if (hasOrbits && numOrbits > 0) {
//				if (!planet.isGasGiant()) {
//					System.out.println("sdfwefew " + star.getId());
//				}
				float startingRadius = planet.getRadius() + STARTING_RADIUS_MOON_BASE + STARTING_RADIUS_MOON_RANGE * random.nextFloat();
				GenContext moonContext = new GenContext(this, context.system, context.center, context.starData, planet, 0, starAge.name(),
														startingRadius, context.maxOrbitRadius, planetData.getCategory(), context.orbitIndex);
				moonContext.multipliers.addAll(context.multipliers);
				// add moons etc
				GenResult moonResult = addOrbitingEntities(moonContext, numOrbits, true, false, false, false);

				context.generatedPlanets.addAll(moonContext.generatedPlanets);
				
				// move the parent planet out so that there's room for everythnig that was added
				radius = moonResult.orbitalWidth * 0.5f;
				orbitRadius = context.currentRadius + radius;
				orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
				
				radiusWithMoons = radius;
				
				planet.setOrbit(Global.getFactory().createCircularOrbit(context.center, angle, orbitRadius, orbitDays));
			}
		} else if (isMoon) {
			float startingRadius = planet.getRadius() + STARTING_RADIUS_MOON_BASE + STARTING_RADIUS_MOON_RANGE * random.nextFloat();
			GenContext moonContext = new GenContext(this, context.system, context.center, context.starData, planet, 0, starAge.name(),
													startingRadius, context.maxOrbitRadius, planetData.getCategory(), context.orbitIndex);
			moonContext.multipliers.addAll(context.multipliers);
			GenResult moonResult = addOrbitingEntities(moonContext, 1, true, false, true, true);
			context.generatedPlanets.addAll(moonContext.generatedPlanets);
			
		}
		
		
		Color color = getColor(planetData.getMinColor(), planetData.getMaxColor());
		//System.out.println("Setting color: " + color);
		planet.getSpec().setPlanetColor(color);
		if (planet.getSpec().getAtmosphereThickness() > 0) {
			Color atmosphereColor = Misc.interpolateColor(planet.getSpec().getAtmosphereColor(), color, 0.25f);
			atmosphereColor = Misc.setAlpha(atmosphereColor, planet.getSpec().getAtmosphereColor().getAlpha());
			planet.getSpec().setAtmosphereColor(atmosphereColor);
			
			if (planet.getSpec().getCloudTexture() != null) {
				Color cloudColor = Misc.interpolateColor(planet.getSpec().getCloudColor(), color, 0.25f);
				cloudColor = Misc.setAlpha(cloudColor, planet.getSpec().getCloudColor().getAlpha());
				planet.getSpec().setAtmosphereColor(atmosphereColor);
			}
		}
		
		float tilt = planet.getSpec().getTilt();
		float pitch = planet.getSpec().getPitch();
		
//		"tilt" # left-right (>0 tilts to the left)
//		"pitch" # towards-away from the viewer (>0 pitches towards)
		float sign = (float) Math.signum(random.nextFloat() - 0.5f);
		double r = random.nextFloat();
		//r *= r;
		if (sign > 0) {
			tilt += r * TILT_MAX;
		} else {
			tilt += r * TILT_MIN;
		}
		
		sign = (float) Math.signum(random.nextFloat() - 0.5f);
		r = random.nextFloat();
		//r *= r;
		if (sign > 0) {
			pitch += r * PITCH_MAX;
		} else {
			tilt += r * PITCH_MIN;
		}
		planet.getSpec().setTilt(tilt);
		planet.getSpec().setPitch(pitch);
		
		
		if (context.orbitIndex == 0 && context.parent == null && context.orbitIndex < context.starData.getHabZoneStart() &&
				orbitRadius < 1500f + context.starData.getHabZoneStart() * 200f) {
				//&& radiusWithMoons <= planet.getRadius() + 500f) {
			if (planet.getSpec().getAtmosphereThickness() > 0) {
				WeightedRandomPicker<String> glowPicker = new WeightedRandomPicker<String>(random);
				glowPicker.add("banded", 10f);
				glowPicker.add("aurorae", 10f);
				
				String glow = glowPicker.pick();
				
				planet.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", glow));
				//system.getLightColor();
				if (context.center instanceof PlanetAPI) {
					planet.getSpec().setGlowColor(((PlanetAPI)context.center).getSpec().getCoronaColor());
				}
				planet.getSpec().setUseReverseLightForGlow(true);
				planet.getSpec().setAtmosphereThickness(0.5f);
				planet.getSpec().setCloudRotation(planet.getSpec().getCloudRotation() * (-1f - 2f * random.nextFloat()));
				
				if (planet.isGasGiant()) {// && radiusWithMoons <= planet.getRadius() + 500f) {
					system.addCorona(planet, Terrain.CORONA_AKA_MAINYU,
							300f + 200f * random.nextFloat(), // radius outside planet
							5f, // burn level of "wind"
							0f, // flare probability
							1f // CR loss mult while in it
							);
				}
			}
		}
		
		planet.applySpecChanges();
		
		PlanetConditionGenerator.generateConditionsForPlanet(context, planet);
		
		
		GeneratedPlanet generatedPlanetData = new GeneratedPlanet(parent, planet, isMoon, orbitDays, orbitRadius, angle);
		context.generatedPlanets.add(generatedPlanetData);
		
		// need to add this here because planet might have been moved after adding moons
		//if (context.parentCategory == null && context.lagrangeParent == null && planet.isGasGiant()) {
		if (!isMoon && context.lagrangeParent == null) {// && planet.isGasGiant()) {
			addStuffAtLagrangePoints(context, generatedPlanetData);
		}
		
		GenResult result = new GenResult();
		result.orbitalWidth = radius * 2f;
		result.onlyIncrementByWidth = false;
		result.entities.add(planet);
		return result;
	}
	
	protected void addStuffAtLagrangePoints(GenContext context, GeneratedPlanet planet) {
		float radius = planet.planet.getRadius();
		float probability = radius / 500f;
		if (radius < 150f) probability = 0f;
		if (planet.planet.isGasGiant()) probability = 1f;
		if (random.nextFloat() > probability) {
			return;
		}
		
		// still a high chance to end up w/ nothing if cat_nothing is picked
		
//		int orbitIndex = context.orbitIndex;
//		String starType = context.star.getTypeId();
//		if (context.parentOrbitIndex >= 0) {
//			orbitIndex = context.parentOrbitIndex;
//		}
		
		Set<LagrangePointType> points = EnumSet.of(LagrangePointType.L4, LagrangePointType.L5);
		for (LagrangePointType point : points) {
			//CategoryGenDataSpec categoryData = pickCategory(orbitIndex, 0, starAge.name(), starType, context.parentCategory, COL_LAGRANGE, true);
			CategoryGenDataSpec categoryData = pickCategory(context, COL_LAGRANGE, true);
			if (categoryData != null && !CAT_NOTHING.equals(categoryData.getCategory())) {
				//WeightedRandomPicker<EntityGenDataSpec> picker = getPickerForCategory(categoryData, orbitIndex, 0, starAge.name(), starType, context.parentCategory, COL_LAGRANGE);
				WeightedRandomPicker<EntityGenDataSpec> picker = getPickerForCategory(categoryData, context, COL_LAGRANGE);
				if (DEBUG) {
					picker.print("  Picking from category " + categoryData.getCategory() + 
							", orbit index " + (context.parent != null ? context.parentOrbitIndex : context.orbitIndex + ", for lagrange point"));
				}
				EntityGenDataSpec entityData = picker.pick();
				if (DEBUG) {
					if (entityData == null) {
						System.out.println("  Nothing to pick");
						System.out.println();
					} else {
						System.out.println("  Picked: " + entityData.getId());
						System.out.println();
					}
				}
				
				context.lagrangeParent = planet;
				context.lagrangePointType = point;
				
				GenResult result = null;
				if (entityData instanceof PlanetGenDataSpec) {
					PlanetGenDataSpec planetData = (PlanetGenDataSpec) entityData;
					result = addPlanet(context, planetData, true, true);
				} else if (entityData instanceof TerrainGenDataSpec) {
					TerrainGenDataSpec terrainData = (TerrainGenDataSpec) entityData;
					result = addTerrain(context, terrainData);
				}
				
				if (result != null) {
					for (SectorEntityToken curr : result.entities) {
						if (context.lagrangeParent != null && !result.entities.isEmpty()) {
							lagrangeParentMap.put(curr, context.lagrangeParent.planet);
						}
						allNameableEntitiesAdded.put(curr, result.entities);
					}
				}
			}
		}
		context.lagrangeParent = null;
		context.lagrangePointType = null;
	}

	
	
	//protected CategoryGenDataSpec pickCategory(int orbitIndex, int fromParentOrbitIndex, String age, String starType, String parentCategory, String extraMult, boolean nothingOk) {
	public CategoryGenDataSpec pickCategory(GenContext context, String extraMult, boolean nothingOk) {
//		int orbitIndex = context.orbitIndex;
//		if (context.parentOrbitIndex >= 0) {
//			orbitIndex = context.parentOrbitIndex;
//		}
//		int fromParentOrbitIndex = context.orbitIndex;
		String age = context.age;
		//String starType = context.star.getTypeId();
		String starType = star.getTypeId();
		if (context.center instanceof PlanetAPI) {
			PlanetAPI star = (PlanetAPI) context.center;
			if (star.isStar()) starType = star.getTypeId();
		}
		
		String parentCategory = context.parentCategory;
		
		WeightedRandomPicker<CategoryGenDataSpec> picker = new WeightedRandomPicker<CategoryGenDataSpec>(random);
		Collection<Object> categoryDataSpecs = Global.getSettings().getAllSpecs(CategoryGenDataSpec.class);
		for (Object obj : categoryDataSpecs) {
			CategoryGenDataSpec categoryData = (CategoryGenDataSpec) obj;
			
			if (context.excludeCategories.contains(categoryData.getCategory())) continue;
			
			boolean catNothing = categoryData.getCategory().equals(CAT_NOTHING);
			if (!nothingOk && catNothing) continue;
//			if (categoryData.getCategory().equals("cat_terrain_rings")) {
//				System.out.println("sdfkwefewfe");
//			}
			float weight = categoryData.getFrequency();
			if (age != null) weight *= categoryData.getMultiplier(age);
			if (starType != null) weight *= categoryData.getMultiplier(starType);
			if (parentCategory != null) weight *= categoryData.getMultiplier(parentCategory);
			for (String col : context.multipliers) {
				weight *= categoryData.getMultiplier(col);
			}
			if (extraMult != null) weight *= categoryData.getMultiplier(extraMult);
			
			//if (weight > 0 && (catNothing || !isCategoryEmpty(categoryData, orbitIndex, fromParentOrbitIndex, age, starType, parentCategory, extraMult))) {
			if (weight > 0 && (catNothing || !isCategoryEmpty(categoryData, context, extraMult, nothingOk))) {
				picker.add(categoryData, weight); 
			}
		}
		
		if (DEBUG) {
			boolean withParent = context.parent != null;
			int orbitIndex = context.orbitIndex;
			String parentType = "";
			if (withParent) {
				parentType = context.parent.getSpec().getPlanetType();
				orbitIndex = context.parentOrbitIndex;
			}
			
//			float offset = orbitIndex;
//			float minIndex = context.starData.getHabZoneStart() + planetData.getHabOffsetMin() + offset;
//			float maxIndex = context.starData.getHabZoneStart() + planetData.getHabOffsetMax() + offset;
			//boolean inRightRange = orbitIndex >= minIndex && orbitIndex <= maxIndex;
			int habDiff = orbitIndex - (int) context.starData.getHabZoneStart();
			if (withParent) {
				picker.print("  Picking category for moon of " + parentType + 
							 ", orbit from star: " + orbitIndex + " (" + habDiff + ")" +  ", extra: " + extraMult);
			} else {
				picker.print("  Picking category for entity orbiting star " + starType + 
							", orbit from star: " + orbitIndex + " (" + habDiff + ")" +  ", extra: " + extraMult);
			}
		}
		
		CategoryGenDataSpec pick = picker.pick();
		if (DEBUG) {
			System.out.println("  Picked: " + pick.getCategory());
			System.out.println();
		}
		
		return pick;
	}

	public boolean isCategoryEmpty(CategoryGenDataSpec categoryData, GenContext context, String extraMult, boolean nothingOk) {
		return getPickerForCategory(categoryData, context, extraMult, nothingOk).isEmpty();
	}
	
	protected float getHabOffset(EntityGenDataSpec data) {
		if (starAge == StarAge.YOUNG) {
			return data.getHabOffsetYOUNG();
		}
		if (starAge == StarAge.AVERAGE) {
			return data.getHabOffsetAVERAGE();
		}
		if (starAge == StarAge.OLD) {
			return data.getHabOffsetOLD();
		}
		return 0f;
	}
	
//	protected WeightedRandomPicker<EntityGenDataSpec> getPickerForCategory(CategoryGenDataSpec categoryData, 
//			int orbitIndex, int fromParentOrbitIndex, String age, String starType, String parentCategory, String extraMult) {
	protected WeightedRandomPicker<EntityGenDataSpec> getPickerForCategory(CategoryGenDataSpec categoryData, 
			GenContext context, String extraMult) {
		return getPickerForCategory(categoryData, context, extraMult, true);
	}
	protected WeightedRandomPicker<EntityGenDataSpec> getPickerForCategory(CategoryGenDataSpec categoryData, 
																		   GenContext context, String extraMult, boolean nothingOk) {
		int orbitIndex = context.orbitIndex;
		if (context.parentOrbitIndex >= 0) {
			orbitIndex = context.parentOrbitIndex;
		}
		int fromParentOrbitIndex = context.orbitIndex;
		String age = context.age;
		//String starType = context.star.getTypeId();
		String starType = star.getTypeId();
		if (context.center instanceof PlanetAPI) {
			PlanetAPI star = (PlanetAPI) context.center;
			if (star.isStar()) starType = star.getTypeId();
		}
		
		String parentCategory = context.parentCategory;
		
	
		WeightedRandomPicker<EntityGenDataSpec> picker = new WeightedRandomPicker<EntityGenDataSpec>(random);
		
		Collection<Object> planetDataSpecs = Global.getSettings().getAllSpecs(PlanetGenDataSpec.class);
		for (Object obj : planetDataSpecs) {
			PlanetGenDataSpec planetData = (PlanetGenDataSpec) obj;
			if (!planetData.getCategory().equals(categoryData.getCategory())) continue;
			
			float offset = getHabOffset(planetData);
			float minIndex = context.starData.getHabZoneStart() + planetData.getHabOffsetMin() + offset;
			float maxIndex = context.starData.getHabZoneStart() + planetData.getHabOffsetMax() + offset;
			boolean inRightRange = orbitIndex >= minIndex && orbitIndex <= maxIndex;
			boolean giantMoonException = CAT_GIANT.equals(parentCategory) && 
						(planetData.hasTag(TAG_GIANT_MOON) && context.parent != null && context.parent.isGasGiant());
			if (!inRightRange && !giantMoonException) continue;
			
//			if (planetData.getId().equals("rocky_unstable")) {
//				System.out.println("dsfwefwefw");
//			}
			
			boolean orbitIndexOk = fromParentOrbitIndex == 0 || !planetData.hasTag(TAG_FIRST_ORBIT_ONLY);
			if (!orbitIndexOk) continue;
			
			boolean lagrangeStatusOk = COL_LAGRANGE.equals(extraMult) || !planetData.hasTag(TAG_LAGRANGE_ONLY);
			if (!lagrangeStatusOk) continue;
			
			boolean nebulaStatusOk = NEBULA_NONE.equals(nebulaType) || !planetData.hasTag(TAG_NOT_IN_NEBULA);
			nebulaStatusOk &= !NEBULA_NONE.equals(nebulaType) || !planetData.hasTag(TAG_REQUIRES_NEBULA);
			nebulaStatusOk &= systemType != StarSystemType.NEBULA || !planetData.hasTag(TAG_NOT_NEBULA_UNLESS_MOON) || context.parent != null;
			if (!nebulaStatusOk) continue;
			
			float weight = planetData.getFrequency();
			if (age != null) weight *= planetData.getMultiplier(age);
			if (starType != null) weight *= planetData.getMultiplier(starType);
			if (parentCategory != null) weight *= planetData.getMultiplier(parentCategory);
			for (String col : context.multipliers) {
				weight *= planetData.getMultiplier(col);
			}
			if (extraMult != null) weight *= planetData.getMultiplier(extraMult);
			if (weight > 0) picker.add(planetData, weight);
		}
		
		Collection<Object> terrainDataSpecs = Global.getSettings().getAllSpecs(TerrainGenDataSpec.class);
		for (Object obj : terrainDataSpecs) {
			TerrainGenDataSpec terrainData = (TerrainGenDataSpec) obj;
			if (!terrainData.getCategory().equals(categoryData.getCategory())) continue;
			
			if (!nothingOk && terrainData.getId().equals("rings_nothing")) continue;
			
			float offset = getHabOffset(terrainData);
			float minIndex = context.starData.getHabZoneStart() + terrainData.getHabOffsetMin() + offset;
			float maxIndex = context.starData.getHabZoneStart() + terrainData.getHabOffsetMax() + offset;
			boolean inRightRange = orbitIndex >= minIndex && orbitIndex <= maxIndex;
			boolean giantMoonException = CAT_GIANT.equals(parentCategory) && 
								(terrainData.hasTag(TAG_GIANT_MOON) && context.parent != null && context.parent.isGasGiant());
			if (!inRightRange && !giantMoonException) continue;
			
			boolean orbitIndexOk = fromParentOrbitIndex == 0 || !terrainData.hasTag(TAG_FIRST_ORBIT_ONLY);
			if (!orbitIndexOk) continue;
			
			boolean lagrangeStatusOk = COL_LAGRANGE.equals(extraMult) || !terrainData.hasTag(TAG_LAGRANGE_ONLY);
			if (!lagrangeStatusOk) continue;
			
			boolean nebulaStatusOk = NEBULA_NONE.equals(nebulaType) || !terrainData.hasTag(TAG_NOT_IN_NEBULA);
			nebulaStatusOk &= !NEBULA_NONE.equals(nebulaType) || !terrainData.hasTag(TAG_REQUIRES_NEBULA);
			nebulaStatusOk &= systemType != StarSystemType.NEBULA || !terrainData.hasTag(TAG_NOT_NEBULA_UNLESS_MOON) || context.parent != null;
			if (!nebulaStatusOk) continue;
			
			float weight = terrainData.getFrequency();
			if (age != null) weight *= terrainData.getMultiplier(age);
			if (starType != null) weight *= terrainData.getMultiplier(starType);
			if (parentCategory != null) weight *= terrainData.getMultiplier(parentCategory);
			for (String col : context.multipliers) {
				weight *= terrainData.getMultiplier(col);
			}
			if (extraMult != null) weight *= terrainData.getMultiplier(extraMult);
			if (weight > 0) picker.add(terrainData, weight);
		}
		
		return picker;
	}
	

	
	protected void setDefaultLightColorBasedOnStars() {
		Color one = Color.white, two = null, three = null;
		
		switch (systemType) {
		case BINARY_FAR:
		case TRINARY_2FAR:
		case SINGLE:
		case NEBULA:
			one = pickLightColorForStar(star);
			break;
		case BINARY_CLOSE:
		case TRINARY_1CLOSE_1FAR:
			one = pickLightColorForStar(star);
			two = pickLightColorForStar(secondary);
			break;
		case TRINARY_2CLOSE:
			one = pickLightColorForStar(star);
			two = pickLightColorForStar(secondary);
			three = pickLightColorForStar(tertiary);
			break;
		}
		
		Color result = one;
		if (two != null && three == null) {
			result = Misc.interpolateColor(one, two, 0.5f);
		} else if (two != null && three != null) {
			result = Misc.interpolateColor(one, two, 0.5f);
			result = Misc.interpolateColor(result, three, 0.5f);
		}
		system.setLightColor(result); // light color in entire system, affects all entities
	}
	
	
	protected Color pickLightColorForStar(PlanetAPI star) {
		StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), false);
		Color min = starData.getLightColorMin();
		Color max = starData.getLightColorMax();
		Color lightColor = Misc.interpolateColor(min, max, random.nextFloat());
		return lightColor;
	}
	
	
//	protected void setLightColorBasedOnStar(PlanetAPI star) {
//		StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), false);
//		Color min = starData.getLightColorMin();
//		Color max = starData.getLightColorMax();
//		Color lightColor = Misc.interpolateColor(min, max, random.nextFloat());
//		system.setLightColor(lightColor); // light color in entire system, affects all entities
//	}
	
	
	protected PlanetAPI addRandomStar(String id, String name) {
		PlanetSpecAPI starSpec = pickStar(constellationAge);
		if (starSpec == null) return null;
		
		StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, starSpec.getPlanetType(), false);
		float radius = getRadius(starData.getMinRadius(), starData.getMaxRadius());
		
		float corona = radius * (starData.getCoronaMult() + starData.getCoronaVar() * (random.nextFloat() - 0.5f));
		if (corona < starData.getCoronaMin()) corona = starData.getCoronaMin();
		
		PlanetAPI star = system.addPlanet(id, // unique id for this star
										  null,
										  name,
										    starSpec.getPlanetType(),  // id in planets.json
										    0f,  // angle
										    radius, 		  // radius (in pixels at default zoom)
										    10000f, // orbit radius
										    1000f // orbit days
											);
		
		system.addCorona(star, corona,  // corona radius, from star edge 
			    			   starData.getSolarWind(),
			    			   (float) (starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare()) * random.nextFloat()),
			    			   starData.getCrLossMult());
		
		return star;
	}
	

	
	protected boolean initSystem(String name, Vector2f loc) {
		sector = Global.getSector();
		system = sector.createStarSystem(name);
		system.setProcgen(true);
		system.setType(systemType);
		system.getLocation().set(loc);
		hyper = Global.getSector().getHyperspace();
		
		//system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");
		system.setBackgroundTextureFilename(backgroundName);
		return true;
	}

	protected void updateAgeAfterPickingStar() {
		starAge = starData.getAge();
		if (starAge == StarAge.ANY) {
			starAge = constellationAge;
		}
		starAgeData = (AgeGenDataSpec) Global.getSettings().getSpec(AgeGenDataSpec.class, starAge.name(), true);
	}
	
	protected void cleanup() {
		if (system != null) {
			Global.getSector().removeStarSystem(system);
			system = null;
		}
	}
	
	public String getNebulaType() {
		return nebulaType;
	}
	
	public StarAge getConstellationAge() {
		return constellationAge;
	}
	
	public StarAge getStarAge() {
		return starAge;
	}
	
	
	public PlanetSpecAPI pickStar(StarAge age) {
		if (params != null && !params.starTypes.isEmpty()) {
			String id = params.starTypes.remove(0);
			//if (id.equals("black_hole") && systemType == StarSystemType.NEBULA) id = "nebula_center_old"; 
			for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
				if (spec.getPlanetType().equals(id)) {
					Object test = Global.getSettings().getSpec(StarGenDataSpec.class, id, true);
					if (test == null) continue;
					StarGenDataSpec data = (StarGenDataSpec) test;
					boolean hasTag = data.hasTag(StarSystemType.NEBULA.name());
					boolean nebType = systemType == StarSystemType.NEBULA;
					boolean nebulaStatusOk = hasTag == nebType;
					if (nebulaStatusOk) {
						return spec;
					}
				}
			}
			// doesn't work because the actual class the spec is registered under is PlanetSpec,
			// not the API-exposed PlanetSpecAPI
			//return (PlanetSpecAPI) Global.getSettings().getSpec(PlanetSpecAPI.class, id, true);
		}
		
		WeightedRandomPicker<PlanetSpecAPI> picker = new WeightedRandomPicker<PlanetSpecAPI>(random);
		for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
			if (!spec.isStar()) continue;
			
			String id = spec.getPlanetType();
			Object test = Global.getSettings().getSpec(StarGenDataSpec.class, id, true);
			if (test == null) continue;
			StarGenDataSpec data = (StarGenDataSpec) test;
			
			
//			boolean nebulaStatusOk = NEBULA_NONE.equals(nebulaType) || !data.hasTag(TAG_NOT_IN_NEBULA);
//			nebulaStatusOk &= !NEBULA_NONE.equals(nebulaType) || !data.hasTag(TAG_REQUIRES_NEBULA);
//			//nebulaStatusOk &= !NEBULA_NONE.equals(nebulaType) || !data.hasTag(StarSystemType.NEBULA.name());
//			//nebulaStatusOk &= systemType != StarSystemType.NEBULA || data.hasTag(StarSystemType.NEBULA.name());
//			nebulaStatusOk &= !data.hasTag(StarSystemType.NEBULA.name()) || systemType == StarSystemType.NEBULA;
			
			boolean hasTag = data.hasTag(StarSystemType.NEBULA.name());
			boolean nebType = systemType == StarSystemType.NEBULA;
			boolean nebulaStatusOk = hasTag == nebType; 
			
			if (!nebulaStatusOk) continue;
			
			//if (!id.equals("empty_nebula_center")) continue;

			float freq = 0f;
			switch (age) {
			case AVERAGE:
				freq = data.getFreqAVERAGE();
				break;
			case OLD:
				freq = data.getFreqOLD();
				break;
			case YOUNG:
				freq = data.getFreqYOUNG();
				break;
			}
			//System.out.println("Adding: " + spec.getPlanetType() + ", weight: " + freq);
			picker.add(spec, freq);
		}
//		if (systemType == StarSystemType.NEBULA) {
//			System.out.println("wfwefwe");
//		}
		return picker.pick();
	}

	
	public static Color getColor(Color min, Color max) {
		Color color = new Color((int) (min.getRed() + (max.getRed() - min.getRed()) * random.nextDouble()),
				(int) (min.getGreen() + (max.getGreen() - min.getGreen()) * random.nextDouble()),
				(int) (min.getBlue() + (max.getBlue() - min.getBlue()) * random.nextDouble()),
				255);
		
		return color;
	}
	
	
	
	public static float getNormalRandom(float min, float max) {
		return getNormalRandom(random, min, max);
	}
	
	public static float getNormalRandom(Random random, float min, float max) {
		double r = random.nextGaussian();
		r *= 0.2f;
		r += 0.5f;
		if (r < 0) r = 0;
		if (r > 1) r = 1;
		
		// 70% chance 0.3 < r < .7
		// 95% chance 0.1 < r < .7
		// 99% chance 0 < r < 1
		return min + (float) r * (max - min);
	}
	
	public static float getRadius(float min, float max) {
		float radius = (float) (min + (max - min) * random.nextFloat());
		return radius;
	}
	
	public static float getRandom(float min, float max) {
		float radius = (float) (min + (max - min) * random.nextFloat());
		return radius;
	}
	public Map<SectorEntityToken, PlanetAPI> getLagrangeParentMap() {
		return lagrangeParentMap;
	}
	
	public Map<SectorEntityToken, List<SectorEntityToken>> getAllEntitiesAdded() {
		return allNameableEntitiesAdded;
	}

	public static Gender pickGender() {
		if (random.nextBoolean()) {
			return Gender.MALE;
		}
		return Gender.FEMALE;
	}
}









