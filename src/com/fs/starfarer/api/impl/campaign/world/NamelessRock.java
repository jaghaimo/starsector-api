package com.fs.starfarer.api.impl.campaign.world;

import java.util.LinkedHashMap;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.enc.AbyssalRogueStellarObjectEPEC;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class NamelessRock {

	public static String ONESLAUGHT_SENSOR_ARRAY = "$oneslaughtSensorArray";
	public static String NAMELESS_ROCK_LOCATION_ID = "nameless_rock_location";
	
	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Deep Space");
		//system.setType(StarSystemType.NEBULA);
		system.setName("Deep Space"); // to get rid of "Star System" at the end of the name
		system.setOptionalUniqueId(NAMELESS_ROCK_LOCATION_ID);
		
		system.setType(StarSystemType.DEEP_SPACE);
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		system.addTag(Tags.SYSTEM_ABYSSAL);
		
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background5.jpg");

		Random random = StarSystemGenerator.random;
		
		float w = Global.getSettings().getFloat("sectorWidth");
		float h = Global.getSettings().getFloat("sectorHeight");
		
		Vector2f systemLoc = new Vector2f();
		float outsideMapPad = 2500f;
		float outsideMapRand = 2500f;
		float r = random.nextFloat();
		// avoid the actual lower-left corner somewhat, since the gate hauler is there, too, but
		if (r < 0.5f) { // left
			systemLoc.x = -w/2f - outsideMapPad - outsideMapRand * random.nextFloat();
			//systemLoc.y = -h/2f - outsideMapPad + (h + outsideMapPad * 2f) * random.nextFloat();
			systemLoc.y = -h/2f + (h + outsideMapPad * 1f) * random.nextFloat();
		} else { //if (r < 0.75f) { // bottom
			systemLoc.x = -w/2f + (w + outsideMapPad * 1f) * random.nextFloat();
			systemLoc.y = -h/2f - outsideMapPad - outsideMapRand * random.nextFloat();
		}
//		else if (r < 0.5f) { // right
//			systemLoc.x = w/2f + outsideMapPad + outsideMapRand * random.nextFloat();
//			systemLoc.y = -h/2f - outsideMapPad + (h + outsideMapPad * 2f) * random.nextFloat();
//		} else { // top
//			systemLoc.x = -w/2f - outsideMapPad + (w + outsideMapPad * 2f) * random.nextFloat();
//			systemLoc.y = h/2f + outsideMapPad + outsideMapRand * random.nextFloat();
//		}
		system.getLocation().set(systemLoc.x, systemLoc.y);
		
//		Vector2f systemLoc = new Vector2f(3000f, 21000f);
//		systemLoc = Misc.getPointWithinRadius(systemLoc, 1000f, random);
//		system.getLocation().set(-w/2f + systemLoc.x, -h/2f + systemLoc.y);

		SectorEntityToken center = system.initNonStarCenter();
		
		system.setLightColor(GateHaulerLocation.ABYSS_AMBIENT_LIGHT_COLOR); // light color in entire system, affects all entities
		center.addTag(Tags.AMBIENT_LS);
		
		//String name = Misc.genEntityCatalogId(2700, 11, 7, CatalogEntryType.PLANET);
		String name = "Nameless Rock";
		
		PlanetAPI rock = system.addPlanet("nameless_rock", null, name, Planets.BARREN, 0, 150, 0, 0);
		rock.setDescriptionIdOverride("barren_deep_space");
		
		//rock.setCustomDescriptionId("???");
		rock.getMemoryWithoutUpdate().set("$namelessRock", true);
		
		rock.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		rock.getMarket().addCondition(Conditions.VERY_COLD);
		rock.getMarket().addCondition(Conditions.DARK);
		rock.getMarket().addCondition(Conditions.ORE_RICH);
		rock.getMarket().addCondition(Conditions.RARE_ORE_MODERATE);
		
		rock.setOrbit(null);
		rock.setLocation(0, 0);
		
		addOnslaughtMkI(rock);

		system.autogenerateHyperspaceJumpPoints(false, false);
		
		AbyssalRogueStellarObjectEPEC.setAbyssalDetectedRanges(system);
		
		createSensorArrayInClosestSystem(system);
	}
	
	
	protected void addOnslaughtMkI(PlanetAPI rock) {
		PerShipData ship = new PerShipData("onslaught_mk1_Ancient", ShipCondition.WRECKED, 0f);
		ship.shipName = "Serial #2F38CB017";
		DerelictShipData params = new DerelictShipData(ship, false);
		CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
									rock.getContainingLocation(),
									Entities.WRECK, Factions.NEUTRAL, params);
		//Misc.makeImportant(entity, "onslaughtMkI");
		entity.getMemoryWithoutUpdate().set("$onslaughtMkI", true);
		entity.setSensorProfile(1f);
		entity.setDiscoverable(true);
		
		Random random = StarSystemGenerator.random;
		float orbitRadius = rock.getRadius() + 200f;
		float orbitDays = orbitRadius / (10f + random.nextFloat() * 5f);
		entity.setCircularOrbit(rock, random.nextFloat() * 360f, orbitRadius, orbitDays);

		ShipRecoverySpecialData data = new ShipRecoverySpecialData(null);
		data.notNowOptionExits = true;
		data.noDescriptionText = true;
		DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) entity.getCustomPlugin();
		PerShipData copy = (PerShipData) dsep.getData().ship.clone();
		copy.variant = Global.getSettings().getVariant(copy.variantId).clone();
		copy.variantId = null;
		copy.variant.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
		copy.variant.addTag(Tags.SHIP_UNIQUE_SIGNATURE);
		copy.nameAlwaysKnown = true;
		//copy.addDmods = false;
		copy.pruneWeapons = false;
		
		// makes it unpilotable by the the player with Neural Link, don't want that 
//		AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.GAMMA_CORE);
//		if (plugin != null) {
//			copy.captain = plugin.createPerson(Commodities.GAMMA_CORE, Factions.PLAYER, null);
//			copy.captain.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
//			copy.captain.getStats().setLevel(copy.captain.getStats().getLevel() + 1);
//			Misc.setUnremovable(copy.captain, true);
//			Misc.setKeepOnShipRecovery(copy.captain, true);
//		}
		
		data.addShip(copy);
		
		Misc.setSalvageSpecial(entity, data);
	}
	
	protected void createSensorArrayInClosestSystem(StarSystemAPI rockSystem) {
		float minDist = Float.MAX_VALUE;
		StarSystemAPI closest = null;
		for (StarSystemAPI curr : Global.getSector().getStarSystems()) {
			if (curr == rockSystem) continue;
			if (Misc.getAbyssalDepth(curr.getLocation()) > 0f) continue;
			if (curr.getType() == StarSystemType.DEEP_SPACE) continue; 
			float dist = Misc.getDistance(curr.getLocation(), rockSystem.getLocation());
			if (dist < minDist) {
				closest = curr;
				minDist = dist;
			}
		}
		
		if (closest == null) return;
		

		SectorEntityToken sensor = null;
		for (SectorEntityToken curr: closest.getEntitiesWithTag(Tags.SENSOR_ARRAY)) {
			if (curr.hasTag(Tags.MAKESHIFT)) continue;
			sensor = curr;
			break;
		}
		
		if (sensor == null) {
			LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<LocationType, Float>();
			weights.put(LocationType.STAR_ORBIT, 10f);
			weights.put(LocationType.OUTER_SYSTEM, 10f);
			WeightedRandomPicker<EntityLocation> locs = BaseThemeGenerator.getLocations(null, closest, null, 100f, weights);
			EntityLocation loc = locs.pick();
			AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(closest,
					loc, Entities.SENSOR_ARRAY, Factions.NEUTRAL);
			if (added != null) {
				sensor = added.entity;
			}
		}

		// really shouldn't be possible as BaseThemeGenerator.getLocations() is supposed to
		// always find something if STAR_ORBIT/OUTER_SYSTEM are supplied in the weights
		if (sensor == null) return;
		
		sensor.getMemoryWithoutUpdate().set(ONESLAUGHT_SENSOR_ARRAY, true);
		Global.getSector().getPersistentData().put(ONESLAUGHT_SENSOR_ARRAY, sensor);

		// to get it:
		//SectorEntityToken sensor = (SectorEntityToken) Global.getSector().getPersistentData().get(ONESLAUGHT_SENSOR_ARRAY);
	}
}













