package com.fs.starfarer.api.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleCreationPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class BattleCreationPluginImpl implements BattleCreationPlugin {

	public interface NebulaTextureProvider {
		String getNebulaTex();
		String getNebulaMapTex();
	}
	
	protected float width, height;
	
	protected float xPad = 2000;
	protected float yPad = 2000;
	
	protected List<String> objs = null;

	protected float prevXDir = 0;
	protected float prevYDir = 0;
	protected boolean escape;
	
	protected BattleCreationContext context;
	protected MissionDefinitionAPI loader;
	
	public void initBattle(final BattleCreationContext context, MissionDefinitionAPI loader) {

		this.context = context;
		this.loader = loader;
		CampaignFleetAPI playerFleet = context.getPlayerFleet();
		CampaignFleetAPI otherFleet = context.getOtherFleet();
		FleetGoal playerGoal = context.getPlayerGoal();
		FleetGoal enemyGoal = context.getOtherGoal();

		// doesn't work for consecutive engagements; haven't investigated why
		//Random random = Misc.getRandom(Misc.getNameBasedSeed(otherFleet), 23);
		
		Random random = Misc.getRandom(Misc.getSalvageSeed(otherFleet) *
				(long)otherFleet.getFleetData().getNumMembers(), 23);
		//System.out.println("RNG: " + random.nextLong());
		//random = new Random(1213123L);
		//Random random = Misc.random;
		
		escape = playerGoal == FleetGoal.ESCAPE || enemyGoal == FleetGoal.ESCAPE;
		
		int maxFP = (int) Global.getSettings().getFloat("maxNoObjectiveBattleSize");
		int fpOne = 0;
		int fpTwo = 0;
		for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
			if (member.canBeDeployedForCombat() || playerGoal == FleetGoal.ESCAPE) {
				fpOne += member.getDeploymentPointsCost();
			}
		}
		for (FleetMemberAPI member : otherFleet.getFleetData().getMembersListCopy()) {
			if (member.canBeDeployedForCombat() || playerGoal == FleetGoal.ESCAPE) {
				fpTwo += member.getDeploymentPointsCost();
			}
		}
		
		int smaller = Math.min(fpOne, fpTwo);
		
		boolean withObjectives = smaller > maxFP;
		if (!context.objectivesAllowed) {
			withObjectives = false;
		}
		
		int numObjectives = 0;
		if (withObjectives) {
//			if (fpOne + fpTwo > maxFP + 70) {
//				numObjectives = 3 
//				numObjectives = 3 + (int)(Math.random() * 2.0);
//			} else {
//				numObjectives = 2 + (int)(Math.random() * 2.0);
//			}
			if (fpOne + fpTwo > maxFP + 70) {
				numObjectives = 4;
				//numObjectives = 3 + (int)(Math.random() * 2.0);
			} else {
				numObjectives = 3 + random.nextInt(2);
				//numObjectives = 2 + (int)(Math.random() * 2.0);
			}
		}
		
		// shouldn't be possible, but..
		if (numObjectives > 4) {
			numObjectives = 4;
		}
		
		int baseCommandPoints = (int) Global.getSettings().getFloat("startingCommandPoints");

		// 
		loader.initFleet(FleetSide.PLAYER, "ISS", playerGoal, false,
				context.getPlayerCommandPoints() - baseCommandPoints,
				(int) playerFleet.getCommanderStats().getCommandPoints().getModifiedValue() - baseCommandPoints);
		loader.initFleet(FleetSide.ENEMY, "", enemyGoal, true, 
				(int) otherFleet.getCommanderStats().getCommandPoints().getModifiedValue() - baseCommandPoints);

		List<FleetMemberAPI> playerShips = playerFleet.getFleetData().getCombatReadyMembersListCopy();
		if (playerGoal == FleetGoal.ESCAPE) {
			playerShips = playerFleet.getFleetData().getMembersListCopy();
		}
		for (FleetMemberAPI member : playerShips) {
			loader.addFleetMember(FleetSide.PLAYER, member);
		}
		
		
		List<FleetMemberAPI> enemyShips = otherFleet.getFleetData().getCombatReadyMembersListCopy();
		if (enemyGoal == FleetGoal.ESCAPE) {
			enemyShips = otherFleet.getFleetData().getMembersListCopy();
		}
		for (FleetMemberAPI member : enemyShips) {
			loader.addFleetMember(FleetSide.ENEMY, member);
		}
		
		width = 18000f;
		height = 18000f;
		
		if (escape) {
			width = 18000f;
			//height = 24000f;
			height = 18000f;
		} else if (withObjectives) {
			width = 24000f;
			if (numObjectives == 2) {
				height = 14000f;
			} else {
				height = 18000f;
			}
		}
		
		createMap(random);
		
		context.setInitialDeploymentBurnDuration(1.5f);
		context.setNormalDeploymentBurnDuration(6f);
		context.setEscapeDeploymentBurnDuration(1.5f);
		
		xPad = 2000f;
		yPad = 3000f;
		
		if (escape) {
//			addEscapeObjectives(loader, 4);
//			context.setInitialEscapeRange(7000f);
//			context.setFlankDeploymentDistance(9000f);
			addEscapeObjectives(loader, 2, random);
//			context.setInitialEscapeRange(4000f);
//			context.setFlankDeploymentDistance(8000f);

			context.setInitialEscapeRange(Global.getSettings().getFloat("escapeStartDistance"));
			context.setFlankDeploymentDistance(Global.getSettings().getFloat("escapeFlankDistance"));
			
			loader.addPlugin(new EscapeRevealPlugin(context));
		} else {
			if (withObjectives) {
				addObjectives(loader, numObjectives, random);
				context.setStandoffRange(height - 4500f);
			} else {
				context.setStandoffRange(6000f);
			}
		}
	}
	
	public void afterDefinitionLoad(final CombatEngineAPI engine) {
		if (coronaIntensity > 0 && (corona != null || pulsar != null)) {
			String name = "Corona";
			if (pulsar != null) name = pulsar.getTerrainName();
			else if (corona != null) name = corona.getTerrainName();
			
			final String name2 = name;
			
//			CombatFleetManagerAPI manager = engine.getFleetManager(FleetSide.PLAYER);
//			for (FleetMemberAPI member : manager.getReservesCopy()) {
//			}
			final Object key1 = new Object();
			final Object key2 = new Object();
			final String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_cr_penalty");
			engine.addPlugin(new BaseEveryFrameCombatPlugin() {
				@Override
				public void advance(float amount, List<InputEventAPI> events) {
					engine.maintainStatusForPlayerShip(key1, icon, name2, "reduced peak time", true);
					engine.maintainStatusForPlayerShip(key2, icon, name2, "faster CR degradation", true);
				}
			});
		}
	}
	
	
	protected float coronaIntensity = 0f;
	protected StarCoronaTerrainPlugin corona = null;
	protected PulsarBeamTerrainPlugin pulsar = null;
	protected void createMap(Random random) {
		loader.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		CampaignFleetAPI playerFleet = context.getPlayerFleet();
		String nebulaTex = null;
		String nebulaMapTex = null;
		boolean inNebula = false;

		boolean protectedFromCorona = false;
		for (CustomCampaignEntityAPI curr : playerFleet.getContainingLocation().getCustomEntitiesWithTag(Tags.PROTECTS_FROM_CORONA_IN_BATTLE)) {
			if (Misc.getDistance(curr.getLocation(), playerFleet.getLocation()) <= curr.getRadius() + Global.getSector().getPlayerFleet().getRadius() + 10f) {
				protectedFromCorona = true;
				break;
			}
		}
		
		float numRings = 0;
		
		Color coronaColor = null;
		// this assumes that all nebula in a system are of the same color
		for (CampaignTerrainAPI terrain : playerFleet.getContainingLocation().getTerrainCopy()) {
			//if (terrain.getType().equals(Terrain.NEBULA)) {
			if (terrain.getPlugin() instanceof NebulaTextureProvider) {
				if (terrain.getPlugin().containsEntity(playerFleet)) {
					inNebula = true;
					if (terrain.getPlugin() instanceof NebulaTextureProvider) {
						NebulaTextureProvider provider = (NebulaTextureProvider) terrain.getPlugin();
						nebulaTex = provider.getNebulaTex();
						nebulaMapTex = provider.getNebulaMapTex();
					}
				} else {
					if (nebulaTex == null) {
						if (terrain.getPlugin() instanceof NebulaTextureProvider) {
							NebulaTextureProvider provider = (NebulaTextureProvider) terrain.getPlugin();
							nebulaTex = provider.getNebulaTex();
							nebulaMapTex = provider.getNebulaMapTex();
						}	
					}
				}
			} else if (terrain.getPlugin() instanceof StarCoronaTerrainPlugin && pulsar == null && !protectedFromCorona) {
				StarCoronaTerrainPlugin plugin = (StarCoronaTerrainPlugin) terrain.getPlugin();
				if (plugin.containsEntity(playerFleet)) {
					float angle = Misc.getAngleInDegrees(terrain.getLocation(), playerFleet.getLocation());
					Color color = plugin.getAuroraColorForAngle(angle);
					float intensity = plugin.getIntensityAtPoint(playerFleet.getLocation());
					intensity = 0.4f + 0.6f * intensity;
					int alpha = (int)(80f * intensity);
					color = Misc.setAlpha(color, alpha);
					if (coronaColor == null || coronaColor.getAlpha() < alpha) {
						coronaColor = color;
						coronaIntensity = intensity;
						corona = plugin;
					}
				}
			} else if (terrain.getPlugin() instanceof PulsarBeamTerrainPlugin && !protectedFromCorona) {
				PulsarBeamTerrainPlugin plugin = (PulsarBeamTerrainPlugin) terrain.getPlugin();
				if (plugin.containsEntity(playerFleet)) {
					float angle = Misc.getAngleInDegreesStrict(terrain.getLocation(), playerFleet.getLocation());
					Color color = plugin.getPulsarColorForAngle(angle);
					float intensity = plugin.getIntensityAtPoint(playerFleet.getLocation());
					intensity = 0.4f + 0.6f * intensity;
					int alpha = (int)(80f * intensity);
					color = Misc.setAlpha(color, alpha);
					if (coronaColor == null || coronaColor.getAlpha() < alpha) {
						coronaColor = color;
						coronaIntensity = intensity;
						pulsar = plugin;
						corona = null;
					}
				}
			} else if (terrain.getType().equals(Terrain.RING)) {
				if (terrain.getPlugin().containsEntity(playerFleet)) {
					numRings++;
				}
			}
		}
		if (nebulaTex != null) {
			loader.setNebulaTex(nebulaTex);
			loader.setNebulaMapTex(nebulaMapTex);
		}
		
		if (coronaColor != null) {
			loader.setBackgroundGlowColor(coronaColor);
		}
		
		int numNebula = 15;
		if (inNebula) {
			numNebula = 100;
		}
		if (!inNebula && playerFleet.isInHyperspace()) {
			numNebula = 0;
		}
		
		for (int i = 0; i < numNebula; i++) {
			float x = random.nextFloat() * width - width/2;
			float y = random.nextFloat() * height - height/2;
			float radius = 100f + random.nextFloat() * 400f;
			if (inNebula) {
				radius += 100f + 500f * random.nextFloat();
			}
			loader.addNebula(x, y, radius);
		}
		
		float numAsteroidsWithinRange = countNearbyAsteroids(playerFleet);
		
		int numAsteroids = Math.min(400, (int)((numAsteroidsWithinRange + 1f) * 20f));
		
		loader.addAsteroidField(0, 0, random.nextFloat() * 360f, width,
								20f, 70f, numAsteroids);
		
		if (numRings > 0) {
			int numRingAsteroids = (int) (numRings * 300 + (numRings * 600f) * random.nextFloat());
			//int numRingAsteroids = (int) (numRings * 1600 + (numRings * 600f) * (float) Math.random());
			if (numRingAsteroids > 1500) {
				numRingAsteroids = 1500;
			}
			loader.addRingAsteroids(0, 0, random.nextFloat() * 360f, width,
					100f, 200f, numRingAsteroids);
		}
		
		//setRandomBackground(loader);
		loader.setBackgroundSpriteName(playerFleet.getContainingLocation().getBackgroundTextureFilename());
//		loader.setBackgroundSpriteName("graphics/backgrounds/hyperspace_bg_cool.jpg");
//		loader.setBackgroundSpriteName("graphics/ships/onslaught/onslaught_base.png");

		if (playerFleet.getContainingLocation() == Global.getSector().getHyperspace()) {
			loader.setHyperspaceMode(true);
		} else {
			loader.setHyperspaceMode(false);
		}
		
		//addMultiplePlanets();
		addClosestPlanet();
	}
	
	protected void addClosestPlanet() {
		float bgWidth = 2048f;
		float bgHeight = 2048f;
		
		PlanetAPI planet = getClosestPlanet(context.getPlayerFleet());
		if (planet == null) return;
		
		float dist = Vector2f.sub(context.getPlayerFleet().getLocation(), planet.getLocation(), new Vector2f()).length() - planet.getRadius();
		if (dist < 0) dist = 0;
		float baseRadius = planet.getRadius();
		float scaleFactor = 1.5f;
		float maxRadius = 500f;
		float minRadius = 100f;
				
//		if (planet.isStar()) {
//			scaleFactor = 0.01f;
//			maxRadius = 20f;
//		}
		
		float maxDist = SINGLE_PLANET_MAX_DIST - planet.getRadius();
		if (maxDist < 1) maxDist = 1;
		
		
		boolean playerHasStation = false;
		boolean enemyHasStation = false;
		
		for (FleetMemberAPI curr : context.getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (curr.isStation()) {
				playerHasStation = true;
				break;
			}
		}
		
		for (FleetMemberAPI curr : context.getOtherFleet().getFleetData().getMembersListCopy()) {
			if (curr.isStation()) {
				enemyHasStation = true;
				break;
			}
		}
		
		float planetYOffset = 0;
		
		if (playerHasStation) {
			planetYOffset = -bgHeight / 2f * 0.5f;
		}
		if (enemyHasStation) {
			planetYOffset = bgHeight / 2f * 0.5f;
		}
		
		
		float f = (maxDist - dist) / maxDist * 0.65f + 0.35f;
		float radius = baseRadius * f * scaleFactor;
		if (radius > maxRadius) radius = maxRadius;
		if (radius < minRadius) radius = minRadius;
		loader.setPlanetBgSize(bgWidth * f, bgHeight * f);
		loader.addPlanet(0f, planetYOffset, radius, planet, 0f, true);
	}
	
	protected void addMultiplePlanets() {
		float bgWidth = 2048f;
		float bgHeight = 2048f;
		loader.setPlanetBgSize(bgWidth, bgHeight);
		
		
		List<NearbyPlanetData> planets = getNearbyPlanets(context.getPlayerFleet());
		if (!planets.isEmpty()) {
			float maxDist = PLANET_MAX_DIST;
			for (NearbyPlanetData data : planets) {
				float dist = Vector2f.sub(context.getPlayerFleet().getLocation(), data.planet.getLocation(), new Vector2f()).length();
				float baseRadius = data.planet.getRadius();
				float scaleFactor = 1.5f;
				float maxRadius = 500f;
				
				if (data.planet.isStar()) {
					// skip stars in combat, bright and annoying
					continue;
//					scaleFactor = 0.1f;
//					maxRadius = 50f;
				}
				
				float f = (maxDist - dist) / maxDist * 0.65f + 0.35f;
				float radius = baseRadius * f * scaleFactor;
				if (radius > maxRadius) radius = maxRadius;
				
				loader.addPlanet(data.offset.x * bgWidth / PLANET_AREA_WIDTH * scaleFactor,
								 data.offset.y * bgHeight / PLANET_AREA_HEIGHT * scaleFactor,
								 radius, data.planet.getTypeId(), 0f, true);
			}
			
		}
	}
	
	
	protected void setRandomBackground(MissionDefinitionAPI loader, Random random) {
		// these have to be loaded using the graphics section in settings.json
		String [] bgs = new String [] {
				"graphics/backgrounds/background1.jpg",
				"graphics/backgrounds/background2.jpg",
				"graphics/backgrounds/background3.jpg",
				"graphics/backgrounds/background4.jpg"
		};
		String pick = bgs[Math.min(bgs.length - 1, (int)(random.nextDouble() * bgs.length))];
		loader.setBackgroundSpriteName(pick);
	}

	protected static String COMM = "comm_relay";
	protected static String SENSOR = "sensor_array";
	protected static String NAV = "nav_buoy";
	
	protected void addObjectives(MissionDefinitionAPI loader, int num, Random random) {
		//if (true) return;
		
		objs = new ArrayList<String>(Arrays.asList(new String [] {
				SENSOR,
				SENSOR,
				NAV,
				NAV,
				//COMM,
				//COMM,
		}));

		if (num == 2) { // minimum is 3 now, so this shouldn't happen
			objs = new ArrayList<String>(Arrays.asList(new String [] {
					SENSOR,
					SENSOR,
					NAV,
					NAV,
					COMM,
			}));
			addObjectiveAt(0.25f, 0.5f, 0f, 0f, random);
			addObjectiveAt(0.75f, 0.5f, 0f, 0f, random);
		} else if (num == 3) {
			float r = random.nextFloat();
			if (r < 0.33f) {
				addObjectiveAt(0.25f, 0.7f, 1f, 1f, random);
				addObjectiveAt(0.25f, 0.3f, 1f, 1f, random);
				addObjectiveAt(0.75f, 0.5f, 1f, 1f, COMM, random);
			} else if (r < 0.67f) {
				addObjectiveAt(0.75f, 0.7f, 1f, 1f, random);
				addObjectiveAt(0.75f, 0.3f, 1f, 1f, random);
				addObjectiveAt(0.25f, 0.5f, 1f, 1f, COMM, random);
			} else {
				if (random.nextFloat() < 0.5f) {
					addObjectiveAt(0.22f, 0.7f, 1f, 1f, random);
					addObjectiveAt(0.5f, 0.5f, 1f, 1f, COMM, random);
					addObjectiveAt(0.78f, 0.3f, 1f, 1f, random);
				} else {
					addObjectiveAt(0.22f, 0.3f, 1f, 1f, random);
					addObjectiveAt(0.5f, 0.5f, 1f, 1f, COMM, random);
					addObjectiveAt(0.78f, 0.7f, 1f, 1f, random);
				}
			}
		} else if (num == 4) {
			float r = random.nextFloat();
			if (r < 0.33f) {
				String [] maybeRelays = pickCommRelays(2, 2, false, true, true, false, random);
				addObjectiveAt(0.25f, 0.25f, 2f, 1f, maybeRelays[0], random);
				addObjectiveAt(0.25f, 0.75f, 2f, 1f, maybeRelays[1], random);
				addObjectiveAt(0.75f, 0.25f, 2f, 1f, maybeRelays[2], random);
				addObjectiveAt(0.75f, 0.75f, 2f, 1f, maybeRelays[3], random);
			} else if (r < 0.67f) {
				String [] maybeRelays = pickCommRelays(1, 2, true, false, true, false, random);
				addObjectiveAt(0.25f, 0.5f, 1f, 1f, maybeRelays[0], random);
				addObjectiveAt(0.5f, 0.75f, 1f, 1f, maybeRelays[1], random);
				addObjectiveAt(0.75f, 0.5f, 1f, 1f, maybeRelays[2], random);
				addObjectiveAt(0.5f, 0.25f, 1f, 1f, maybeRelays[3], random);
			} else {
				if (random.nextFloat() < 0.5f) {
					String [] maybeRelays = pickCommRelays(1, 2, true, false, true, false, random);
					addObjectiveAt(0.25f, 0.25f, 1f, 0f, maybeRelays[0], random);
					addObjectiveAt(0.4f, 0.6f, 1f, 0f, maybeRelays[1], random);
					addObjectiveAt(0.6f, 0.4f, 1f, 0f, maybeRelays[2], random);
					addObjectiveAt(0.75f, 0.75f, 1f, 0f, maybeRelays[3], random);
				} else {
					String [] maybeRelays = pickCommRelays(1, 2, false, true, false, true, random);
					addObjectiveAt(0.25f, 0.75f, 1f, 0f, maybeRelays[0], random);
					addObjectiveAt(0.4f, 0.4f, 1f, 0f, maybeRelays[1], random);
					addObjectiveAt(0.6f, 0.6f, 1f, 0f, maybeRelays[2], random);
					addObjectiveAt(0.75f, 0.25f, 1f, 0f, maybeRelays[3], random);
				}
			}
		}
	}
	
	protected String [] pickCommRelays(int min, int max, boolean comm1, boolean comm2, boolean comm3, boolean comm4, Random random) {
		String [] result = new String [4];
		
		WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<Integer>(random);
		if (comm1) picker.add(0);
		if (comm2) picker.add(1);
		if (comm3) picker.add(2);
		if (comm4) picker.add(3);
		
		int num = min + random.nextInt(max - min + 1);
		
		for (int i = 0; i < num && !picker.isEmpty(); i++) {
			result[picker.pickAndRemove()] = COMM;
		}
		return result;
	}
	
	
	protected void addEscapeObjectives(MissionDefinitionAPI loader, int num, Random random) {
		objs = new ArrayList<String>(Arrays.asList(new String [] {
				SENSOR,
				SENSOR,
				NAV,
				NAV,
				COMM,
		}));
		
		if (num == 2) {
			float r = random.nextFloat();
			if (r < 0.33f) {
				addObjectiveAt(0.25f, 0.25f, 1f, 1f, random);
				addObjectiveAt(0.75f, 0.75f, 1f, 1f, random);
			} else if (r < 0.67f) {
				addObjectiveAt(0.75f, 0.25f, 1f, 1f, random);
				addObjectiveAt(0.25f, 0.75f, 1f, 1f, random);
			} else {
				addObjectiveAt(0.5f, 0.25f, 4f, 2f, random);
				addObjectiveAt(0.5f, 0.75f, 4f, 2f, random);
			}
		} else if (num == 3) {
			float r = random.nextFloat();
			if (r < 0.33f) {
				addObjectiveAt(0.25f, 0.75f, 1f, 1f, random);
				addObjectiveAt(0.25f, 0.25f, 1f, 1f, random);
				addObjectiveAt(0.75f, 0.5f, 1f, 6f, random);
			} else if (r < 0.67f) {
				addObjectiveAt(0.25f, 0.5f, 1f, 6f, random);
				addObjectiveAt(0.75f, 0.75f, 1f, 1f, random);
				addObjectiveAt(0.75f, 0.25f, 1f, 1f, random);
			} else {
				addObjectiveAt(0.5f, 0.25f, 4f, 1f, random);
				addObjectiveAt(0.5f, 0.5f, 4f, 1f, random);
				addObjectiveAt(0.5f, 0.75f, 4f, 1f, random);
			}
		} else if (num == 4) {
			float r = random.nextFloat();
			if (r < 0.33f) {
				addObjectiveAt(0.25f, 0.25f, 1f, 1f, random);
				addObjectiveAt(0.25f, 0.75f, 1f, 1f, random);
				addObjectiveAt(0.75f, 0.25f, 1f, 1f, random);
				addObjectiveAt(0.75f, 0.75f, 1f, 1f, random);
			} else if (r < 0.67f) {
				addObjectiveAt(0.35f, 0.25f, 2f, 0f, random);
				addObjectiveAt(0.65f, 0.35f, 2f, 0f, random);
				addObjectiveAt(0.5f, 0.6f, 4f, 1f, random);
				addObjectiveAt(0.5f, 0.8f, 4f, 1f, random);
			} else {
				addObjectiveAt(0.65f, 0.25f, 2f, 0f, random);
				addObjectiveAt(0.35f, 0.35f, 2f, 0f, random);
				addObjectiveAt(0.5f, 0.6f, 4f, 1f, random);
				addObjectiveAt(0.5f, 0.8f, 4f, 1f, random);
			}
		}
	}	

	protected void addObjectiveAt(float xMult, float yMult, float xOff, float yOff, Random random) {
		addObjectiveAt(xMult, yMult, xOff, yOff, null, random);
	}
	protected void addObjectiveAt(float xMult, float yMult, float xOff, float yOff, String type, Random random) {
		//String type = pickAny();
		if (type == null) {
			type = pickAny(random);
			if (objs != null && objs.size() > 0) {
				int index = (int) (random.nextDouble() * objs.size());
				type = objs.remove(index); 
			}
		}
		
		float minX = -width/2 + xPad;
		float minY = -height/2 + yPad;
		
		float x = (width - xPad * 2f) * xMult + minX;
		float y = (height - yPad * 2f) * yMult + minY;
		
		x = ((int) x / 1000) * 1000f;
		y = ((int) y / 1000) * 1000f;
		
		float offsetX = Math.round((random.nextFloat() - 0.5f) * xOff * 1f) * 1000f;
		float offsetY = Math.round((random.nextFloat() - 0.5f) * yOff * 1f) * 1000f;
		
//		offsetX = 0;
//		offsetY = 0;
		
		float xDir = (float) Math.signum(offsetX);
		float yDir = (float) Math.signum(offsetY);
		
		if (xDir == prevXDir && xOff > 0) {
			xDir = -xDir;
			offsetX = Math.abs(offsetX) * -prevXDir;
		}
		
		if (yDir == prevYDir && yOff > 0) {
			yDir = -yDir;
			offsetY = Math.abs(offsetY) * -prevYDir;
		}
		
		prevXDir = xDir;
		prevYDir = yDir;
		
		x += offsetX;
		y += offsetY;
		
		loader.addObjective(x, y, type);
		
		if (random.nextFloat() > 0.6f) {
			float nebulaSize = random.nextFloat() * 1500f + 500f;
			loader.addNebula(x, y, nebulaSize);
		}
	}
	
	protected String pickAny(Random random) {
		float r = random.nextFloat();
		if (r < 0.33f) return "nav_buoy";
		else if (r < 0.67f) return "sensor_array";
		else return "comm_relay"; 
	}

	protected float countNearbyAsteroids(CampaignFleetAPI playerFleet) {
		float numAsteroidsWithinRange = 0;
		LocationAPI loc = playerFleet.getContainingLocation();
		if (loc instanceof StarSystemAPI) {
			StarSystemAPI system = (StarSystemAPI) loc;
			List<SectorEntityToken> asteroids = system.getAsteroids();
			for (SectorEntityToken asteroid : asteroids) {
				float range = Vector2f.sub(playerFleet.getLocation(), asteroid.getLocation(), new Vector2f()).length();
				if (range < 300) numAsteroidsWithinRange ++;
			}
		}
		return numAsteroidsWithinRange;
	}
	
	protected static class NearbyPlanetData {
		protected Vector2f offset;
		protected PlanetAPI planet;
		public NearbyPlanetData(Vector2f offset, PlanetAPI planet) {
			this.offset = offset;
			this.planet = planet;
		}
	}
	
	protected static float PLANET_AREA_WIDTH = 2000;
	protected static float PLANET_AREA_HEIGHT = 2000;
	protected static float PLANET_MAX_DIST = (float) Math.sqrt(PLANET_AREA_WIDTH/2f * PLANET_AREA_WIDTH/2f + PLANET_AREA_HEIGHT/2f * PLANET_AREA_WIDTH/2f);
	
	protected static float SINGLE_PLANET_MAX_DIST = 1000f;
	
	protected List<NearbyPlanetData> getNearbyPlanets(CampaignFleetAPI playerFleet) {
		LocationAPI loc = playerFleet.getContainingLocation();
		List<NearbyPlanetData> result = new ArrayList<NearbyPlanetData>();
		if (loc instanceof StarSystemAPI) {
			StarSystemAPI system = (StarSystemAPI) loc;
			List<PlanetAPI> planets = system.getPlanets();
			for (PlanetAPI planet : planets) {
				float diffX = planet.getLocation().x - playerFleet.getLocation().x;
				float diffY = planet.getLocation().y - playerFleet.getLocation().y;
				
				if (Math.abs(diffX) < PLANET_AREA_WIDTH/2f && Math.abs(diffY) < PLANET_AREA_HEIGHT/2f) {
					result.add(new NearbyPlanetData(new Vector2f(diffX, diffY), planet));
				}
			}
		}
		return result;
	}
	
	protected PlanetAPI getClosestPlanet(CampaignFleetAPI playerFleet) {
		LocationAPI loc = playerFleet.getContainingLocation();
		PlanetAPI closest = null;
		float minDist = Float.MAX_VALUE;
		if (loc instanceof StarSystemAPI) {
			StarSystemAPI system = (StarSystemAPI) loc;
			List<PlanetAPI> planets = system.getPlanets();
			for (PlanetAPI planet : planets) {
				if (planet.isStar()) continue;
				if (Planets.PLANET_LAVA.equals(planet.getTypeId())) continue;
				if (Planets.PLANET_LAVA_MINOR.equals(planet.getTypeId())) continue;
				if (planet.getSpec().isDoNotShowInCombat()) continue;
				
				float dist = Vector2f.sub(context.getPlayerFleet().getLocation(), planet.getLocation(), new Vector2f()).length();
				if (dist < minDist && dist < SINGLE_PLANET_MAX_DIST) {
					closest = planet;
					minDist = dist;
				}
			}
		}
		return closest;
	}
}




