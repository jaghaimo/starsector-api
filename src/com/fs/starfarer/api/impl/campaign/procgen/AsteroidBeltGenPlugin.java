package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenResult;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GeneratedPlanet;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class AsteroidBeltGenPlugin implements TerrainGenPlugin {
	
	public GenResult generate(TerrainGenDataSpec terrainData, GenContext context) {
		StarSystemAPI system = context.system;
		SectorEntityToken parent = context.center;
		if (context.parent != null) parent = context.parent;
		
		//float width = StarSystemGenerator.getRadius(terrainData.getMinRadius(), terrainData.getMaxRadius());
		float width = 200f;
		
		float countMult = 1f;
		float maxPlanets = 1;
		if (context.currentRadius > 2000f && StarSystemGenerator.random.nextFloat() > 0.5f) {
			width += 100f;
			countMult += 0.5f;
			maxPlanets++;
		}
		if (context.currentRadius > 4000f && StarSystemGenerator.random.nextFloat() > 0.5f) {
			width += 100f;
			countMult += 0.5f;
			maxPlanets++;
		}
		if (context.currentRadius > 6000f && StarSystemGenerator.random.nextFloat() > 0.5f) {
			width += 100f;
			countMult += 0.5f;
			maxPlanets++;
		}
		
		//if (maxPlanets > 3) maxPlanets = 3;
		if (maxPlanets > 1) maxPlanets = 1;
		
		
		float orbitRadius = context.currentRadius + width / 2f;
		float orbitDays = orbitRadius / (15f + 5f * StarSystemGenerator.random.nextFloat());

		int count = (int) (orbitDays * (0.25f + 0.5f * StarSystemGenerator.random.nextFloat()) * countMult);
		if (count > 400) count = 400;
		
		SectorEntityToken belt = system.addAsteroidBelt(parent, count, orbitRadius, width, orbitDays * .75f, orbitDays * 1.5f, Terrain.ASTEROID_BELT,  null);
		
		WeightedRandomPicker<Integer> indexPicker = new WeightedRandomPicker<Integer>(StarSystemGenerator.random);
		indexPicker.add(0);
		indexPicker.add(1);
		//indexPicker.add(2);
		//indexPicker.add(3);
		
		system.addRingBand(parent, "misc", "rings_asteroids0", 256f, indexPicker.pickAndRemove(), Color.white, 256f, orbitRadius - width * 0.25f, orbitDays * 1.05f, null, null);
		
		
		indexPicker = new WeightedRandomPicker<Integer>(StarSystemGenerator.random);
		indexPicker.add(0);
		indexPicker.add(1);
		indexPicker.add(2);
		indexPicker.add(3);
		system.addRingBand(parent, "misc", "rings_asteroids0", 256f, indexPicker.pickAndRemove(), Color.white, 256f, orbitRadius + width * 0.25f, orbitDays, null, null);

		
		float prevRadius = context.currentRadius;
		//float [] offsets = new float [] {0.5f, 0.15f, 0.85f};
		//float [] offsets = new float [] {0.25f};
		for (int i = 0; i < maxPlanets; i++) {
			CategoryGenDataSpec cat = context.gen.pickCategory(context, StarSystemGenerator.COL_IN_ASTEROIDS, true);
			if (cat != null) {
				WeightedRandomPicker<EntityGenDataSpec> picker = context.gen.getPickerForCategory(cat, context,
																		StarSystemGenerator.COL_IN_ASTEROIDS);
				if (StarSystemGenerator.DEBUG) {
					picker.print("  Picking from category " + cat.getCategory() + 
							", orbit index " + (context.parent != null ? context.parentOrbitIndex : context.orbitIndex));
				}
				EntityGenDataSpec pick = picker.pick();
				if (StarSystemGenerator.DEBUG) {
					if (pick == null) {
						System.out.println("  Nothing to pick");
						System.out.println();
					} else {
						System.out.println("  Picked: " + pick.getId());
						System.out.println();
					}
				}
				
				if (pick instanceof PlanetGenDataSpec) {
					PlanetGenDataSpec planetData = (PlanetGenDataSpec) pick;
					//context.currentRadius = prevRadius + width * 0.1f + i * (width * 0.8f) / maxPlanets;
					//context.currentRadius = prevRadius + width * offsets[i];
					context.currentRadius = prevRadius - width * 0.6f;
					//context.parentRadiusOverride = width * 0.75f / maxPlanets / StarSystemGenerator.MOON_RADIUS_MAX_FRACTION_OF_PARENT;
					context.parentRadiusOverride = 200f;
					GenResult result = context.gen.addPlanet(context, planetData, true, false);
					if (result != null && !result.entities.isEmpty()) {
						context.gen.getAllEntitiesAdded().put(result.entities.get(0), result.entities);
					}
					
					if (!context.generatedPlanets.isEmpty()) {
						GeneratedPlanet last = context.generatedPlanets.get(context.generatedPlanets.size() - 1);
						last.planet.setRadius(last.planet.getRadius() * 0.75f);
					}
				}
			}
		}
		context.parentRadiusOverride = -1;
		context.currentRadius = prevRadius;
		
		
		GenResult result = new GenResult();
		result.onlyIncrementByWidth = false;
		result.orbitalWidth = width;
		result.entities.add(belt); 
		return result;
	}

	public boolean wantsToHandle(TerrainGenDataSpec terrainData, GenContext context) {
		return terrainData != null && terrainData.getId().equals("asteroid_belt");
	}

}
