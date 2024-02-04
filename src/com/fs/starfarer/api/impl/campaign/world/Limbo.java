package com.fs.starfarer.api.impl.campaign.world;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.shared.WormholeManager;
import com.fs.starfarer.api.impl.campaign.shared.WormholeManager.WormholeItemData;

public class Limbo {

	public void generate(SectorAPI sector) {
		
		StarSystemAPI system = sector.createStarSystem("Limbo");
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		float w = Global.getSettings().getFloat("sectorWidth");
		float h = Global.getSettings().getFloat("sectorHeight");
		system.getLocation().set(-w/2f + 2300f, -h/2f + 2100f);
		
		
		LocationAPI hyper = Global.getSector().getHyperspace();
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");
		
		
		// create the star and generate the hyperspace anchor for this system
		PlanetAPI star = system.initStar("limbo", // unique id for this star 
										    StarTypes.BROWN_DWARF,  // id in planets.json
										    400f, 		  // radius (in pixels at default zoom)
										    250); // corona radius, from star edge
		
		
		system.setLightColor(new Color(180, 205, 140)); // light color in entire system, affects all entities
		
		Random random = StarSystemGenerator.random;
		
		PlanetAPI planet = system.addPlanet("limbo_hades", star, "Hades", Planets.ROCKY_METALLIC, 215, 60, 1700, 75);
		planet.getMemoryWithoutUpdate().set("$hades", true);
		planet.setCustomDescriptionId("limbo_hades");
		planet.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		planet.getMarket().addCondition(Conditions.COLD);
		planet.getMarket().addCondition(Conditions.POOR_LIGHT);
		planet.getMarket().addCondition(Conditions.RARE_ORE_RICH);
		
		float orbitRadius = planet.getRadius() + 300f;
		float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
		EntityLocation loc = new EntityLocation();
		loc.orbit = Global.getFactory().createCircularOrbitPointingDown(planet, random.nextFloat() * 360f, 
																		orbitRadius, orbitDays); 
		AddedEntity added = BaseThemeGenerator.addEntity(null, system, loc, Entities.STATION_MINING, Factions.NEUTRAL);
		added.entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPEC_ID_OVERRIDE, "limbo_mining_station");
		added.entity.setName("Automated Mining Station");
		added.entity.getMemoryWithoutUpdate().set("$limboMiningStation", true);
		added.entity.setCustomDescriptionId("station_mining_automated");
		
		CargoAPI cargo = Global.getFactory().createCargo(true);
		cargo.addCommodity(Commodities.RARE_METALS, 200f + random.nextInt(101));
		BaseSalvageSpecial.addExtraSalvage(added.entity, cargo);

		
		StarSystemGenerator.addStableLocations(system, 1);
		
		
		for (SectorEntityToken curr : system.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
			curr.getMemoryWithoutUpdate().set(WormholeManager.LIMBO_STABLE_LOCATION, true);
			
			orbitRadius = curr.getRadius() + 200f;
			orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
			loc = new EntityLocation();
			loc.orbit = Global.getFactory().createCircularOrbitPointingDown(curr, random.nextFloat() * 360f, 
					orbitRadius, orbitDays); 
			added = BaseThemeGenerator.addEntity(null, system, loc, Entities.LARGE_CACHE, Factions.NEUTRAL);
			added.entity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPEC_ID_OVERRIDE, Entities.SPEC_LIMBO_WORMHOLE_CACHE);
			added.entity.setName("Mothballed Equipment Cache");
			added.entity.setCustomDescriptionId(Entities.SPEC_LIMBO_WORMHOLE_CACHE);
			added.entity.getMemoryWithoutUpdate().set("$limboWormholeCache", true);
			
			cargo = Global.getFactory().createCargo(true);
			SpecialItemData item = WormholeManager.createWormholeAnchor("foxtrot", "Foxtrot");
			cargo.addItems(CargoItemType.SPECIAL, item, 1);
			
			item = new SpecialItemData(Items.WORMHOLE_SCANNER, null);
			cargo.addItems(CargoItemType.SPECIAL, item, 1);
			
			cargo.addFuel(110 + random.nextInt(21));
			BaseSalvageSpecial.addExtraSalvage(added.entity, cargo);
			
			break;
		}
		
		
		
		if (Global.getSettings().isDevMode()) {
			orbitRadius = planet.getRadius() + 600f;
			orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
			loc = new EntityLocation();
			loc.orbit = Global.getFactory().createCircularOrbitPointingDown(planet, random.nextFloat() * 360f, 
																			orbitRadius, orbitDays); 
			added = BaseThemeGenerator.addEntity(null, system, loc, Entities.EQUIPMENT_CACHE, Factions.NEUTRAL);
			added.entity.setName("Test Wormhole Cache");
	
			CargoAPI testCargo = Global.getFactory().createCargo(true);
			
			WormholeItemData itemData = new WormholeItemData("standard", "bravo", "Bravo");
			SpecialItemData item = new SpecialItemData(Items.WORMHOLE_ANCHOR, itemData.toJsonStr());
			testCargo.addItems(CargoItemType.SPECIAL, item, 1);
			
			itemData = new WormholeItemData("standard", "sierra", "Sierra");
			item = new SpecialItemData(Items.WORMHOLE_ANCHOR, itemData.toJsonStr());
			testCargo.addItems(CargoItemType.SPECIAL, item, 1);
			
			item = new SpecialItemData(Items.WORMHOLE_SCANNER, null);
			testCargo.addItems(CargoItemType.SPECIAL, item, 1);
	
			BaseSalvageSpecial.addExtraSalvage(added.entity, testCargo);
		}

		system.autogenerateHyperspaceJumpPoints(true, true);
	}
}










