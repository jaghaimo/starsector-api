package com.fs.starfarer.api.impl.campaign.world;

import java.awt.Color;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.JumpPointInteractionDialogPluginImpl;
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
import com.fs.starfarer.api.impl.campaign.shared.WormholeManager;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.CatalogEntryType;

public class GateHaulerLocation {

	public static Color ABYSS_AMBIENT_LIGHT_COLOR = new Color(100,100,100,255);
	
	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Deep Space");
		//system.setType(StarSystemType.NEBULA);
		system.setName("Deep Space"); // to get rid of "Star System" at the end of the name
		system.setType(StarSystemType.DEEP_SPACE);
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background5.jpg");

		Random random = StarSystemGenerator.random;
		
		float w = Global.getSettings().getFloat("sectorWidth");
		float h = Global.getSettings().getFloat("sectorHeight");
		float angle = 180f + random.nextFloat() * 90f;
		float systemDist = 4000f + random.nextFloat() * 2000f;
		Vector2f systemLoc = Misc.getUnitVectorAtDegreeAngle(angle);
		systemLoc.scale(systemDist);
		systemLoc.x -= 1000f;
		
		system.getLocation().set(-w/2f + systemLoc.x, -h/2f + systemLoc.y);
//		system.getLocation().set(-w/2f + 4000f - 0f * random.nextFloat(),
//				-h/2f + 4000f - 0f * random.nextFloat());
		
		
		SectorEntityToken center = system.initNonStarCenter();
		
		system.setLightColor(ABYSS_AMBIENT_LIGHT_COLOR); // light color in entire system, affects all entities
		center.addTag(Tags.AMBIENT_LS);
		
		String name = Misc.genEntityCatalogId(3125, 6, 11, CatalogEntryType.GIANT);
		
		PlanetAPI giant = system.addPlanet("nameless_ice_giant", null, name, Planets.ICE_GIANT, 0, 450, 0, 0);

		//rock.setCustomDescriptionId("???");
		giant.getMemoryWithoutUpdate().set("$gateHaulerIceGiant", true);
		
		giant.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
		giant.getMarket().addCondition(Conditions.COLD);
		giant.getMarket().addCondition(Conditions.DARK);
		giant.getMarket().addCondition(Conditions.VOLATILES_TRACE);
		giant.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		
		giant.setOrbit(null);
		giant.setLocation(0, 0);

		//StarSystemGenerator.addStableLocations(system, 1);
		EntityLocation loc = new EntityLocation();
		float orbitRadius = 7000f;
		float orbitDays = orbitRadius / (20f + 5f * StarSystemGenerator.random.nextFloat());
		loc.orbit = Global.getFactory().createCircularOrbit(giant,
				StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitDays);
		AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(system, loc, Entities.STABLE_LOCATION, Factions.NEUTRAL);		
		
		
		for (SectorEntityToken curr : system.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
			SpecialItemData item = WormholeManager.createWormholeAnchor("charlie", "Charlie");
			JumpPointAPI wormhole = WormholeManager.get().addWormhole(item, curr, null);
			wormhole.getMemoryWithoutUpdate().unset(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY);
			break;
		}
		
		
		
		orbitRadius = giant.getRadius() + 250f;
		orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
		float spin = 3f;
		loc = new EntityLocation();
		loc.orbit = Global.getFactory().createCircularOrbitWithSpin(giant, random.nextFloat() * 360f, 
																		orbitRadius, orbitDays, spin);
		added = BaseThemeGenerator.addEntity(null, system, loc, Entities.DERELICT_GATEHAULER, Factions.NEUTRAL);
		added.entity.getMemoryWithoutUpdate().set("$gateHauler", true);
		
//		CampaignFleetAPI visual = Global.getFactory().createEmptyFleet(Factions.NEUTRAL, added.entity.getName(), true);
//		visual.getFleetData().addFleetMember("derelict_gatehauler_Hull");
//		visual.setHidden(true);
//		visual.setVelocity(300f, 0f);
//		visual.setFacing(0f);
//		visual.setMoveDestination(10000f, 0f);
//		((CustomCampaignEntityAPI)added.entity).setFleetForVisual(visual);
		
		
		system.autogenerateHyperspaceJumpPoints(true, false);

		
		AbyssalRogueStellarObjectEPEC.setAbyssalDetectedRanges(system);
	}
	
}













