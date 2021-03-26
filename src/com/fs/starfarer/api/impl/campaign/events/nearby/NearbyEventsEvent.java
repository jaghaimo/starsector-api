package com.fs.starfarer.api.impl.campaign.events.nearby;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventTarget;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.PirateFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.misc.DistressCallIntel;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RuinsFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.TransmitterTrapSpecial.TransmitterTrapSpecialData;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * 
 * @author Alex Mosolov
 *
 * Copyright 2014 Fractal Softworks, LLC
 */
public class NearbyEventsEvent extends BaseEventPlugin implements RouteFleetSpawner {
	
	public static enum DistressEventType {
		NORMAL,
		PIRATE_AMBUSH,
		PIRATE_AMBUSH_TRAP,
		DERELICT_SHIP,
	}
	
	public static final float DISTRESS_REPEAT_TIMEOUT = 90f;
	public static final float DISTRESS_ALREADY_WAS_NEARBY_TIMEOUT = 30f;
	public static final float DISTRESS_MIN_SINCE_PLAYER_IN_SYSTEM = 90f;
	public static final float DISTRESS_MIN_CHECK_INTERVAL = 5f;
	public static final float DISTRESS_MAX_CHECK_INTERVAL = 15f;
	public static final float DISTRESS_PROB_PER_SYSTEM = 0.2f;
	public static final float DISTRESS_MAX_PROB = 0.6f;
	
	public static final float DERELICT_SKIP_PROB = 0.5f;
	
	protected IntervalUtil derelictShipInterval = new IntervalUtil(1f, 10f);
	protected IntervalUtil distressCallInterval = new IntervalUtil(DISTRESS_MIN_CHECK_INTERVAL, DISTRESS_MAX_CHECK_INTERVAL);
	protected TimeoutTracker<String> skipForDistressCalls = new TimeoutTracker<String>();
	
	public static boolean TEST_MODE = false;
	
	public void init(String type, CampaignEventTarget eventTarget) {
		super.init(type, eventTarget);
		readResolve();
	}
	
	Object readResolve() {
		if (skipForDistressCalls == null) {
			skipForDistressCalls = new TimeoutTracker<String>();
		}
		if (distressCallInterval == null) {
			distressCallInterval = new IntervalUtil(DISTRESS_MIN_CHECK_INTERVAL, DISTRESS_MAX_CHECK_INTERVAL);
		}
		return this;
	}
	
	public void startEvent() {
		super.startEvent();
	}
	
	public void advance(float amount) {
		//if (true) return;
		
		if (!isEventStarted()) return;
		if (isDone()) return;
		
		if (Global.getSector().isInFastAdvance()) return;
		if (Global.getSector().getPlayerFleet() == null) return;
		
		
		float days = Global.getSector().getClock().convertToDays(amount);
		
		derelictShipInterval.advance(days);
		if (derelictShipInterval.intervalElapsed()) {
			maybeSpawnDerelictShip();
		}
		
		skipForDistressCalls.advance(days);
		
		distressCallInterval.advance(days);
		//TEST_MODE = true;
		if (distressCallInterval.intervalElapsed() || TEST_MODE) {
			maybeSpawnDistressCall();
		}
	}
	
	protected void maybeSpawnDerelictShip() {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (!playerFleet.isInHyperspace()) return;
		
		if ((float) Math.random() < DERELICT_SKIP_PROB) return;
		
		WeightedRandomPicker<String> factions = SalvageSpecialAssigner.getNearbyFactions(null, playerFleet,
																					 	 15f, 5f, 5f);
		
		DerelictShipData params = DerelictShipEntityPlugin.createRandom(factions.pick(), null, null, 0f);
		if (params != null) {
			ShipVariantAPI variant = Global.getSettings().getVariant(params.ship.variantId);
			params.durationDays = DerelictShipEntityPlugin.getBaseDuration(variant.getHullSize());
			
			CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
								Global.getSector().getHyperspace(),
								Entities.WRECK, Factions.NEUTRAL, params);
			entity.addTag(Tags.EXPIRES);
			entity.setDiscoverable(false);
			SalvageSpecialAssigner.assignSpecials(entity, false);
			

			float distFromPlayer = 3000f + (float) Math.random() * 2000f;
			Vector2f loc = Misc.getPointAtRadius(playerFleet.getLocationInHyperspace(), distFromPlayer, new Random());
			
			entity.getLocation().x = loc.x;
			entity.getLocation().y = loc.y;
			
			
			float angle = Misc.getAngleInDegrees(loc, playerFleet.getLocation());
			float arc = 90f;
			angle = angle - arc /2f + arc * (float) Math.random();
			float speed = 10f + 10f * (float) Math.random();
			Vector2f vel = Misc.getUnitVectorAtDegreeAngle(angle);
			vel.scale(speed);
			entity.getVelocity().set(vel);
			
		}
	}
	
	
	public static Set<String> distressCallAllowedThemes = new HashSet<String>();
	static {
		distressCallAllowedThemes.add(Tags.THEME_MISC);
		distressCallAllowedThemes.add(Tags.THEME_MISC_SKIP);
		distressCallAllowedThemes.add(Tags.THEME_RUINS);
		distressCallAllowedThemes.add(Tags.THEME_REMNANT_SUPPRESSED);
		distressCallAllowedThemes.add(Tags.THEME_REMNANT_DESTROYED);
		distressCallAllowedThemes.add(Tags.THEME_REMNANT_NO_FLEETS);
		distressCallAllowedThemes.add(Tags.THEME_DERELICT);
	}
	
	public static class NESpawnData {
		public DistressEventType type;
		public LocationAPI location;
		public SectorEntityToken jumpPoint;
	}
	
	protected void maybeSpawnDistressCall() {
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		if (!playerFleet.isInHyperspace()) return;
		if (playerFleet.isInHyperspaceTransition()) return;
		
		WeightedRandomPicker<StarSystemAPI> systems = new WeightedRandomPicker<StarSystemAPI>();
		OUTER: for (StarSystemAPI system : Misc.getNearbyStarSystems(playerFleet, Global.getSettings().getFloat("distressCallEventRangeLY"))) {
			
			if (skipForDistressCalls.contains(system.getId())) continue;
			
			if (system.hasPulsar()) continue;
			
			float sincePlayerVisit = system.getDaysSinceLastPlayerVisit();
			if (sincePlayerVisit < DISTRESS_MIN_SINCE_PLAYER_IN_SYSTEM) {
				continue;
			}
			
			boolean validTheme = false;
			for (String tag : system.getTags()) {
				if (distressCallAllowedThemes.contains(tag)) {
					validTheme = true;
					break;
				}
			}
			if (!validTheme) continue;
			
			for (CampaignFleetAPI fleet : system.getFleets()) {
				if (!fleet.getFaction().isHostileTo(Factions.INDEPENDENT)) continue OUTER;
			}
			
			if (!Misc.getMarketsInLocation(system).isEmpty()) continue;
			
			skipForDistressCalls.add(system.getId(), DISTRESS_ALREADY_WAS_NEARBY_TIMEOUT);
			systems.add(system);
		}
		
		float p = systems.getItems().size() * DISTRESS_PROB_PER_SYSTEM;
		if (p > DISTRESS_MAX_PROB) p = DISTRESS_MAX_PROB;
		if ((float) Math.random() >= p && !TEST_MODE) return;
		
		
		StarSystemAPI system = systems.pick();
		if (system == null) return;
		
		skipForDistressCalls.set(system.getId(), DISTRESS_REPEAT_TIMEOUT);
		
	
		WeightedRandomPicker<DistressEventType> picker = new WeightedRandomPicker<DistressEventType>();
		picker.add(DistressEventType.NORMAL, 10f);
		picker.add(DistressEventType.PIRATE_AMBUSH, 10f);
		picker.add(DistressEventType.PIRATE_AMBUSH_TRAP, 10f);
		picker.add(DistressEventType.DERELICT_SHIP, 10f);

		DistressEventType type = picker.pick();
		if (TEST_MODE) type = DistressEventType.PIRATE_AMBUSH;
		
		if (type == DistressEventType.NORMAL) {
			generateDistressCallNormal(system);
		} else if (type == DistressEventType.PIRATE_AMBUSH) {
			generateDistressCallAmbush(system);
		} else if (type == DistressEventType.PIRATE_AMBUSH_TRAP || TEST_MODE) {
			generateDistressCallAmbushTrap(system);
		} else if (type == DistressEventType.DERELICT_SHIP) {
			generateDistressDerelictShip(system);
		}
		
//		CommMessageAPI message = FleetLog.beginEntry("Distress Call", system.getCenter());
//		message.setSmallIcon(Global.getSettings().getSpriteName("intel_categories", "events"));
//		message.getSection1().addPara("You receive a distress call from the nearby " + system.getNameWithLowercaseType());
//		message.getSection1().addPara("There's no additional information, but that's not surprising - a typical fleet doesn't carry the equipment to broadcast a full-fledged data stream into hyperspace.");
//		FleetLog.addToLog(message, null);
		
		DistressCallIntel intel = new DistressCallIntel(system);
		Global.getSector().getIntelManager().addIntel(intel);
	}
	
	protected void generateDistressDerelictShip(StarSystemAPI system) {
		SectorEntityToken jumpPoint = Misc.getDistressJumpPoint(system);
		if (jumpPoint == null) return;

		
		WeightedRandomPicker<String> factions = SalvageSpecialAssigner.getNearbyFactions(null, system.getLocation(),
			 	 																		 15f, 5f, 5f);
		DerelictShipData params = DerelictShipEntityPlugin.createRandom(factions.pick(), null, null, DerelictShipEntityPlugin.getDefaultSModProb());
		if (params == null) return;
		
		params.durationDays = 60f;
		CustomCampaignEntityAPI derelict = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
										 system, Entities.WRECK, Factions.NEUTRAL, params);
		derelict.addTag(Tags.EXPIRES);
		
		float radius = 400f + 400f * (float) Math.random();
		float maxRadius = Math.max(300, jumpPoint.getCircularOrbitRadius() * 0.33f);
		if (radius > maxRadius) radius = maxRadius;
		
		float orbitDays = radius / (5f + Misc.random.nextFloat() * 20f);
		float angle = (float) Math.random() * 360f;
		derelict.setCircularOrbit(jumpPoint, angle, radius, orbitDays);
		
		SalvageSpecialAssigner.assignSpecialForDistressDerelict(derelict);
	}
	
	protected void generateDistressCallAmbushTrap(StarSystemAPI system) {
		SectorEntityToken jumpPoint = Misc.getDistressJumpPoint(system);
		if (jumpPoint == null) return;

		
		WeightedRandomPicker<String> factions = SalvageSpecialAssigner.getNearbyFactions(null, system.getLocation(),
			 	 																		 15f, 5f, 5f);
		DerelictShipData params = DerelictShipEntityPlugin.createRandom(factions.pick(), null, null, DerelictShipEntityPlugin.getDefaultSModProb());
		if (params == null) return;
		
		params.durationDays = 60f;
		CustomCampaignEntityAPI derelict = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
										 system, Entities.WRECK, Factions.NEUTRAL, params);
		derelict.addTag(Tags.EXPIRES);
		
		float radius = 400f + 400f * (float) Math.random();
		float maxRadius = Math.max(300, jumpPoint.getCircularOrbitRadius() * 0.33f);
		if (radius > maxRadius) radius = maxRadius;
		
		float orbitDays = radius / (5f + Misc.random.nextFloat() * 20f);
		float angle = (float) Math.random() * 360f;
		derelict.setCircularOrbit(jumpPoint, angle, radius, orbitDays);
		
		
		TransmitterTrapSpecialData data = new TransmitterTrapSpecialData();
		data.prob = 1f;
		data.maxRange = 20000f;
		data.nearbyFleetFaction = Factions.PIRATES;
		data.useAllFleetsInRange = true;
		Misc.setSalvageSpecial(derelict, data);
		
		int numPirates = new Random().nextInt(3) + 1;
		for (int i = 0; i < numPirates; i++) {
			
			NESpawnData dcd = new NESpawnData();
			dcd.type = DistressEventType.PIRATE_AMBUSH_TRAP;
			dcd.location = system;
			dcd.jumpPoint = jumpPoint;
			
			OptionalFleetData extra = new OptionalFleetData();
			extra.factionId = Factions.PIRATES;
			
			RouteData route = RouteManager.getInstance().addRoute("dcd_" + getId(), null, 
											Misc.genRandomSeed(), extra, this, dcd);
			float waitDays = 30f + (float) Math.random() * 10f;
			route.addSegment(new RouteSegment(waitDays, jumpPoint));
			
//			
//			int points = 5 + new Random().nextInt(20);
//			
//			CampaignFleetAPI fleet = PirateFleetManager.createPirateFleet(points, null, system.getLocation());
//			if (fleet != null) {
//				system.addEntity(fleet);
//				Vector2f loc = Misc.getPointAtRadius(jumpPoint.getLocation(), 500f + (float) Math.random() * 200f);
//				fleet.setLocation(loc.x, loc.y);
//				fleet.addScript(new DistressCallPirateAmbushTrapAssignmentAI(fleet, system, jumpPoint));
//			}
		}
		
	}
	
	
	protected void generateDistressCallAmbush(StarSystemAPI system) {
		SectorEntityToken jumpPoint = Misc.getDistressJumpPoint(system);
		if (jumpPoint == null) return;
		
		int numPirates = new Random().nextInt(3) + 1;
		

		for (int i = 0; i < numPirates; i++) {
			NESpawnData dcd = new NESpawnData();
			dcd.type = DistressEventType.PIRATE_AMBUSH;
			dcd.location = system;
			dcd.jumpPoint = jumpPoint;
			
			OptionalFleetData extra = new OptionalFleetData();
			extra.factionId = Factions.PIRATES;
			
			RouteData route = RouteManager.getInstance().addRoute("dcd_" + getId(), null, 
											Misc.genRandomSeed(), extra, this, dcd);
			float waitDays = 30f + (float) Math.random() * 10f;
			route.addSegment(new RouteSegment(waitDays, jumpPoint));
			
//			int points = 5 + new Random().nextInt(20);
//			
//			CampaignFleetAPI fleet = PirateFleetManager.createPirateFleet(points, null, system.getLocation());
//			if (fleet != null) {
//				system.addEntity(fleet);
//				Vector2f loc = Misc.getPointAtRadius(jumpPoint.getLocation(), 500f + (float) Math.random() * 200f);
//				fleet.setLocation(loc.x, loc.y);
//				fleet.addScript(new DistressCallPirateAmbushAssignmentAI(fleet, system, jumpPoint));
//			}
		}
		
	}
	
	
	protected void generateDistressCallNormal(StarSystemAPI system) {
		SectorEntityToken jumpPoint = Misc.getDistressJumpPoint(system);
		if (jumpPoint == null) return;
		
		NESpawnData dcd = new NESpawnData();
		dcd.type = DistressEventType.NORMAL;
		dcd.location = system;
		dcd.jumpPoint = jumpPoint;
		
		OptionalFleetData extra = new OptionalFleetData();
		extra.factionId = Factions.INDEPENDENT;
		
		RouteData route = RouteManager.getInstance().addRoute("dcd_" + getId(), null, 
										Misc.genRandomSeed(), extra, this, dcd);
		float waitDays = 30f + (float) Math.random() * 10f;
		route.addSegment(new RouteSegment(waitDays, jumpPoint));

		
//		WeightedRandomPicker<String> typePicker = new WeightedRandomPicker<String>();
//		typePicker.add(FleetTypes.SCAVENGER_SMALL, 10f);
//		typePicker.add(FleetTypes.SCAVENGER_MEDIUM, 10f);
//		typePicker.add(FleetTypes.SCAVENGER_LARGE, 10f);
//		String type = typePicker.pick();
//		type = FleetTypes.SCAVENGER_SMALL;
//		boolean pirate = (float) Math.random() < 0.5f;
//		if (TEST_MODE) pirate = true;
//		CampaignFleetAPI fleet = RuinsFleetRouteManager.createScavenger(
//									type, system.getLocation(),
//									null, pirate, null);
//		if (fleet == null) return;
//		if (Misc.getSourceMarket(fleet) == null) return;
//		
//		system.addEntity(fleet);
//		
//		fleet.removeAbility(Abilities.EMERGENCY_BURN);
//		
//		//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
//		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
//		//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
//		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.ENTITY_MISSION_IMPORTANT,
//			       			   "distress", true, 1000f);
//		fleet.getMemoryWithoutUpdate().set("$ne_eventRef", this);
//		fleet.getMemoryWithoutUpdate().set("$distress", true);
//		
//		if (pirate) {
//			fleet.getMemoryWithoutUpdate().set("$distressTurnHostile", true);
//		}
//
//		//SectorEntityToken jumpPoint = jpLoc.orbit.getFocus();
//		Vector2f loc = Misc.getPointAtRadius(jumpPoint.getLocation(), 400f + (float) Math.random() * 200f);
//		fleet.setLocation(loc.x, loc.y);
//		
//		fleet.addScript(new DistressCallNormalAssignmentAI(fleet, system, jumpPoint));
		
	}
	
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
		route.expire();
	}

	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

	public boolean shouldRepeat(RouteData route) {
		return false;
	}

	public CampaignFleetAPI spawnFleet(RouteData route) {
		
		NESpawnData data = (NESpawnData) route.getCustom();
		
		if (data.type == DistressEventType.PIRATE_AMBUSH_TRAP) {
			float tf = PirateBaseManager.getInstance().getStandardTimeFactor();
			int points = (int) (10 + new Random().nextInt(20) * tf);
			
			CampaignFleetAPI fleet = PirateFleetManager.createPirateFleet(points, null, data.location.getLocation());
			if (fleet != null) {
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
				data.location.addEntity(fleet);
				Vector2f loc = Misc.getPointAtRadius(data.jumpPoint.getLocation(), 500f + (float) Math.random() * 200f);
				fleet.setLocation(loc.x, loc.y);
				fleet.addScript(new DistressCallPirateAmbushTrapAssignmentAI(fleet, (StarSystemAPI) data.location, data.jumpPoint));
				Misc.makeHostile(fleet);
			}
			return fleet;
		} else if (data.type == DistressEventType.PIRATE_AMBUSH) {
			float tf = PirateBaseManager.getInstance().getStandardTimeFactor();
			int points = (int) (10 + new Random().nextInt(20) * tf);
			
			CampaignFleetAPI fleet = PirateFleetManager.createPirateFleet(points, null, data.location.getLocation());
			if (fleet != null) {
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
				data.location.addEntity(fleet);
				Vector2f loc = Misc.getPointAtRadius(data.jumpPoint.getLocation(), 500f + (float) Math.random() * 200f);
				fleet.setLocation(loc.x, loc.y);
				fleet.addScript(new DistressCallPirateAmbushAssignmentAI(fleet, (StarSystemAPI) data.location, data.jumpPoint));
				Misc.makeHostile(fleet);
			}
			return fleet;
		} else if (data.type == DistressEventType.NORMAL) {
			
			WeightedRandomPicker<String> typePicker = new WeightedRandomPicker<String>();
			typePicker.add(FleetTypes.SCAVENGER_SMALL, 10f);
			typePicker.add(FleetTypes.SCAVENGER_MEDIUM, 10f);
			typePicker.add(FleetTypes.SCAVENGER_LARGE, 10f);
			String type = typePicker.pick();
			type = FleetTypes.SCAVENGER_SMALL;
			boolean pirate = (float) Math.random() < 0.5f;
			if (TEST_MODE) pirate = true;
			CampaignFleetAPI fleet = RuinsFleetRouteManager.createScavenger(
										type, data.location.getLocation(),
										null, pirate, null);
			if (fleet == null) return null;
			if (Misc.getSourceMarket(fleet) == null) return null;
			
			data.location.addEntity(fleet);
			
			fleet.removeAbility(Abilities.EMERGENCY_BURN);
			
			//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
			//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
			Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.ENTITY_MISSION_IMPORTANT,
				       			   "distress", true, 1000f);
			fleet.getMemoryWithoutUpdate().set("$ne_eventRef", this);
			fleet.getMemoryWithoutUpdate().set("$distress", true);
			
			if (pirate) {
				fleet.getMemoryWithoutUpdate().set("$distressTurnHostile", true);
			}

			//SectorEntityToken jumpPoint = jpLoc.orbit.getFocus();
			Vector2f loc = Misc.getPointAtRadius(data.jumpPoint.getLocation(), 400f + (float) Math.random() * 200f);
			fleet.setLocation(loc.x, loc.y);
			
			fleet.addScript(new DistressCallNormalAssignmentAI(fleet, (StarSystemAPI) data.location, data.jumpPoint));
			
			return fleet;
		}
		
		return null;
	}
	
	
	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String action = params.get(0).getString(memoryMap);
		
		CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
		CargoAPI cargo = playerFleet.getCargo();
		
		FactionAPI playerFaction = playerFleet.getFaction();
		Color color = playerFaction.getColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color highlight = Misc.getHighlightColor();
		
		TextPanelAPI text = dialog.getTextPanel();
		
		MemoryAPI memory = BaseCommandPlugin.getEntityMemory(memoryMap);
		
		boolean tookCrew = memory.getBoolean("$playerTookDistressCrewRecently"); 
		if (action.equals("initDistress")) {
			
			CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
			MarketAPI source = Misc.getSourceMarket(fleet);
			
			float returnDistLY = 0;
			if (source != null) {
				returnDistLY = Misc.getDistanceLY(fleet.getLocationInHyperspace(), source.getLocationInHyperspace());
			} else {
				returnDistLY = Misc.getDistanceLY(fleet.getLocationInHyperspace(), new Vector2f());
			}
			
			int fuel = (int) (returnDistLY * Math.max(1, fleet.getLogistics().getFuelCostPerLightYear()));
			fuel *= 0.5f;
			if (fuel < 10) fuel = 10;
			fuel = (int) (Math.ceil(fuel / 10f) * 10);
			//fuel = 10;
			
			int credits = fuel * (int) Global.getSettings().getFloat("distressCallFuelCost");
			
			int crew = (int) (fleet.getFleetData().getMinCrew() * 0.33f);
			int takeOnCrew = Math.min(crew, cargo.getFreeCrewSpace());
			
			memory.set("$distressFuel", fuel, 0f);
			//memory.set("$distressCredits", credits, 0f);
			memory.set("$distressCredits", Misc.getWithDGS(credits), 0f);
			memory.set("$distressCrewTakeOn", takeOnCrew, 0f);
			memory.set("$distressCrew", crew, 0f);
			
			if (memory.getBoolean("$distressTurnHostile")) {
				memory.set("$distressFuelHostileThreshold", fuel, 0f);
			}
			
		} else if (action.equals("takeDistressCrew")) {
			int crew = (int) memory.getFloat("$distressCrewTakeOn");
			int needed = (int) memory.getFloat("$distressCrew");
			
			boolean enough = crew >= needed;
			
			cargo.addCrew(crew);
			AddRemoveCommodity.addCommodityGainText(Commodities.CREW, crew, text);
			
			float repChange = (int) (crew / 20);
			if (repChange < 1) repChange = 1;
			if (repChange > 5) repChange = 5;
			adjustRep(repChange, null, dialog.getInteractionTarget().getActivePerson().getFaction(),
					  dialog.getInteractionTarget().getActivePerson(), text);
			//memory.set("$playerTookDistressCrewRecently", true); -- this is set in rules.csv with an expiration
			
			if (enough) {
				DistressCallNormalAssignmentAI.undistress(dialog.getInteractionTarget());
				
				CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
				DistressCallNormalAssignmentAI.scuttleShips(fleet, crew);
			}
			
		} else if (action.equals("sellDistressFuel")) {
			int fuel = (int) memory.getFloat("$distressFuel");
			int credits = (int) memory.getFloat("$distressCredits");
			
			cargo.removeFuel(fuel);
			cargo.getCredits().add(credits);
			
			AddRemoveCommodity.addCommodityLossText(Commodities.FUEL, fuel, text);
			AddRemoveCommodity.addCreditsGainText(credits, text);
			
			DistressCallNormalAssignmentAI.undistress(dialog.getInteractionTarget());
			
			if (tookCrew) {
				int crew = (int) memory.getFloat("$distressCrewTakeOn");
				float repChange = (int) (crew / 20);
				if (repChange < 1) repChange = 1;
				if (repChange > 5) repChange = 5;
				adjustRep(-repChange, RepLevel.INHOSPITABLE, dialog.getInteractionTarget().getActivePerson().getFaction(),
  						  dialog.getInteractionTarget().getActivePerson(), text);
			}
			
		} else if (action.equals("scaredDistressFuel")) {
			int fuel = (int) memory.getFloat("$distressFuel");
			//int credits = (int) memory.getFloat("$distressCredits");
			
			cargo.removeFuel(fuel);
			//cargo.getCredits().add(credits);
			
			AddRemoveCommodity.addCommodityLossText(Commodities.FUEL, fuel, text);
			//AddRemoveCommodity.addCreditsGainText(credits, text);
			
			DistressCallNormalAssignmentAI.undistress(dialog.getInteractionTarget());
			
		} else if (action.equals("giveDistressFuel")) {
			int fuel = (int) memory.getFloat("$distressFuel");
			int credits = (int) memory.getFloat("$distressCredits");
			
			cargo.removeFuel(fuel);
			//cargo.getCredits().add(credits);
			
			AddRemoveCommodity.addCommodityLossText(Commodities.FUEL, fuel, text);
			//AddRemoveCommodity.addCreditsGainText(credits, text);

			if (!tookCrew) {
				float repChange = (int) (credits / 1000);
				if (repChange > 10) repChange = 10;
				adjustRep(repChange, null, dialog.getInteractionTarget().getActivePerson().getFaction(),
						  dialog.getInteractionTarget().getActivePerson(), text);
			}
			
			DistressCallNormalAssignmentAI.undistress(dialog.getInteractionTarget());
			
		}
		
		
//		else if (action.equals("showDistressResources")) {
//			ResourceCostPanelAPI cost = text.addCostPanel("Required crew & machinery", SalvageEntity.COST_HEIGHT,
//					color, playerFaction.getDarkUIColor());
//			cost.setNumberOnlyMode(true);
//			cost.setWithBorder(false);
//			cost.setAlignment(Alignment.LMID);
//			cost.addCost(Commodities.FUEL, 10, color);
//			cost.setSecondTitle("  Available crew & machinery");
//			cost.addCost(Commodities.FUEL, 10, color);
//			cost.update();
//		}
		
		return true;
	}
	
	
	protected void adjustRep(float repChangePercent, RepLevel limit, FactionAPI faction, PersonAPI person, TextPanelAPI text) {
		if (repChangePercent != 0) {
			CustomRepImpact impact = new CustomRepImpact();
			impact.delta = repChangePercent * 0.01f;
			impact.limit = limit;
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, impact,
										  null, text, true), 
										  faction.getId());
			
			if (person != null) {
				impact.delta *= 2f;
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, impact,
											  null, text, true), person);
			}
		}
	}
	

	public Map<String, String> getTokenReplacements() {
		Map<String, String> map = super.getTokenReplacements();
		return map;
	}

	@Override
	public String[] getHighlights(String stageId) {
		return null;
	}
	
	@Override
	public Color[] getHighlightColors(String stageId) {
		return super.getHighlightColors(stageId);
	}
	
	
	@Override
	public CampaignEventTarget getEventTarget() {
		return super.getEventTarget();
	}

	public boolean isDone() {
		return false;
	}
	
	@Override
	public CampaignEventCategory getEventCategory() {
		return CampaignEventCategory.DO_NOT_SHOW_IN_MESSAGE_FILTER;
	}
	
	public boolean showAllMessagesIfOngoing() {
		return false;
	}
	
	public static void main(String[] args) throws ParseException {
		Locale.setDefault(Locale.GERMAN);
		
//		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
//		symbols.setDecimalSeparator('.');
//		symbols.setGroupingSeparator(','); 
//		DecimalFormat format = new DecimalFormat("###,###,###,###,###", symbols);
		DecimalFormat format = new DecimalFormat("###,###,###,###,###");
		System.out.println(format.parse("25,000").floatValue());
	}

	
}










