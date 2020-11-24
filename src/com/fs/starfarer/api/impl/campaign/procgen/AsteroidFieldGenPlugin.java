package com.fs.starfarer.api.impl.campaign.procgen;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenResult;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.LagrangePointType;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class AsteroidFieldGenPlugin implements TerrainGenPlugin {
	
	public static final float WIDTH_PLANET = 200f;
	public static final float WIDTH_STAR = 400f;
	
	public GenResult generate(TerrainGenDataSpec terrainData, GenContext context) {
		if (context.lagrangeParent == null || context.lagrangePointType == null) return null;
		
		//System.out.println("GENERATING L-POINT NEBULA AT " + context.star.getId());
		
		SectorEntityToken parent = context.center;
		if (context.parent != null) parent = context.parent;
		if (context.lagrangeParent != null) parent = context.center;
		
		if (parent == null) return null;
		
		WeightedRandomPicker<Integer> sizePicker = new WeightedRandomPicker<Integer>(StarSystemGenerator.random);
		for (int i = 5; i <= 11; i++) {
			sizePicker.add(i, 20 - i);
		}
		
		int size = sizePicker.pick();
		float radius = size * 100f;
		
		float maxRadius = (context.currentRadius - parent.getRadius()) * 0.5f;
		if (radius > maxRadius) radius = maxRadius;
		if (radius < 400) radius = 400;
		
		float area = radius * radius * 3.14f;
		int count = (int) (area / 40000f);
		if (count < 10) count = 10;
		if (count > 100) count = 100;
		
		
		StarSystemAPI system = context.system;
		float orbitRadius = context.currentRadius + radius;
		
		float orbitDays = orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f);
		float angle = StarSystemGenerator.random.nextFloat() * 360f;
		if (context.lagrangeParent != null) {
			orbitRadius = context.lagrangeParent.orbitRadius;
			orbitDays = context.lagrangeParent.orbitDays;
			float angleOffset = -StarSystemGenerator.LAGRANGE_OFFSET;
			if (context.lagrangePointType == LagrangePointType.L5) angleOffset = StarSystemGenerator.LAGRANGE_OFFSET;
			angle = context.lagrangeParent.orbitAngle + angleOffset;
		}
		
		SectorEntityToken field = system.addTerrain(Terrain.ASTEROID_FIELD,
				new AsteroidFieldParams(
					radius, // min radius
					radius + 100f, // max radius
					count, // min asteroid count
					count, // max asteroid count
					4f, // min asteroid radius 
					16f, // max asteroid radius
					null)); // null for default name
		
		field.setCircularOrbit(parent, angle, orbitRadius, orbitDays);
		
		CategoryGenDataSpec cat = context.gen.pickCategory(context, StarSystemGenerator.COL_IN_ASTEROIDS, true);
		if (cat != null) {
			float prevRadius = context.currentRadius;
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
				context.currentRadius = orbitRadius;
				context.parentRadiusOverride = 100f / StarSystemGenerator.MOON_RADIUS_MAX_FRACTION_OF_PARENT;
				GenResult result = context.gen.addPlanet(context, planetData, true, false);
				if (result != null && !result.entities.isEmpty()) {
					context.gen.getAllEntitiesAdded().put(result.entities.get(0), result.entities);
					if (context.lagrangeParent != null && !result.entities.isEmpty()) {
						context.gen.getLagrangeParentMap().put(result.entities.get(0), context.lagrangeParent.planet);
					}
				}
				
				// not necessary to set orbit, as the lagrangeParent is set in the context so
				// addPlanet() does it correctly
//				if (!context.generatedPlanets.isEmpty()) {
//					GeneratedPlanet last = context.generatedPlanets.get(context.generatedPlanets.size() - 1);
//					//last.planet.setRadius(last.planet.getRadius() * 0.75f);
//					last.planet.setCircularOrbit(parent, angle, orbitRadius, orbitDays);
//				}
			}
			context.parentRadiusOverride = -1;
			context.currentRadius = prevRadius;
		}
		
		
		GenResult result = new GenResult();
		result.onlyIncrementByWidth = false;
		result.orbitalWidth = radius * 2f;
		result.entities.add(field);
		return result;
	}

	public boolean wantsToHandle(TerrainGenDataSpec terrainData, GenContext context) {
		return terrainData != null && terrainData.getId().equals("asteroid_field");
	}

}
