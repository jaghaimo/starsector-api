package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenResult;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;

public class MagFieldGenPlugin implements TerrainGenPlugin {
	
	public static Color [] baseColors = {
		new Color(50, 25, 100, 70),
		//new Color(50, 30, 100, 30),
		//new Color(75, 105, 165, 75)
	};
	
	public static Color [][] auroraColors = {
		{new Color(140, 100, 235), 
			new Color(180, 110, 210),
			new Color(150, 140, 190),
			new Color(140, 190, 210),
			new Color(90, 200, 170),
			new Color(65, 230, 160), 
			new Color(20, 220, 70) },
		{new Color(50, 20, 110, 130),
			new Color(150, 30, 120, 150), 
			new Color(200, 50, 130, 190),
			new Color(250, 70, 150, 240),
			new Color(200, 80, 130, 255),
			new Color(75, 0, 160), 
			new Color(127, 0, 255) },
		{new Color(90, 180, 140),
			new Color(130, 145, 190),
			new Color(165, 110, 225), 
			new Color(95, 55, 240), 
			new Color(45, 0, 250),
			new Color(20, 0, 240),
			new Color(10, 0, 150) },
		{new Color(90, 180, 40),
			new Color(130, 145, 90),
			new Color(165, 110, 145), 
			new Color(95, 55, 160), 
			new Color(45, 0, 130),
			new Color(20, 0, 130),
			new Color(10, 0, 150) },
		{new Color(50, 20, 110, 130),
			new Color(150, 30, 120, 150), 
			new Color(200, 50, 130, 190),
			new Color(250, 70, 150, 240),
			new Color(200, 80, 130, 255),
			new Color(75, 0, 160), 
			new Color(127, 0, 255) },
		{new Color(55, 60, 140),
				new Color(65, 85, 155),
				new Color(175, 105, 165), 
				new Color(90, 130, 180), 
				new Color(105, 150, 190),
				new Color(120, 175, 205),
				new Color(135, 200, 220)},
	};
	
	public static final float WIDTH_PLANET = 200f;
	public static final float WIDTH_STAR = 400f;
	
	public GenResult generate(TerrainGenDataSpec terrainData, GenContext context) {
		//if (!(context.star instanceof PlanetAPI)) return null;
		
		StarSystemAPI system = context.system;
		SectorEntityToken parent = context.center;
		if (context.parent != null) parent = context.parent;
		
		boolean isStar = false;
		boolean hasAtmosphere = false;
		if (parent instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI) parent;
			isStar = planet.isStar();
			hasAtmosphere = planet.getSpec().getAtmosphereThickness() > 0;
		} else if (parent == context.system.getCenter()) {
			isStar = true;
		}
		
		if (context.parent != null) parent = context.parent;
		
		//System.out.println("GENERATING MAG FIELD AROUND " + parent.getId());
		
		int baseIndex = (int) (baseColors.length * StarSystemGenerator.random.nextDouble());
		int auroraIndex = (int) (auroraColors.length * StarSystemGenerator.random.nextDouble());
		
		
		float bandWidth = parent.getRadius() + WIDTH_PLANET;
		float midRadius = (parent.getRadius() + WIDTH_PLANET) / 2f;
//		float visStartRadius = parent.getRadius() + 50f;
//		float visEndRadius = parent.getRadius() + 50f + WIDTH_PLANET + 50f;
		float visStartRadius = parent.getRadius();
		float visEndRadius = parent.getRadius() + WIDTH_PLANET + 50f;
		float auroraProbability = 0f;
		
		float orbitalWidth = WIDTH_PLANET;
		
		if (isStar || context.orbitIndex > 0) {
			bandWidth = WIDTH_STAR;
			midRadius = context.currentRadius + bandWidth / 2f;
			visStartRadius = context.currentRadius;
			visEndRadius = context.currentRadius + bandWidth;
			
			orbitalWidth = WIDTH_STAR;
			
			if (isStar) {
				auroraProbability = 1f;
			} else {
				auroraProbability = 0.25f + 0.75f * StarSystemGenerator.random.nextFloat();
			}
		} else if (hasAtmosphere) {
			auroraProbability = 0.25f + 0.75f * StarSystemGenerator.random.nextFloat();
		}
		
		SectorEntityToken magField = system.addTerrain(Terrain.MAGNETIC_FIELD,
				new MagneticFieldParams(bandWidth, // terrain effect band width 
						midRadius, // terrain effect middle radius
						parent, // entity that it's around
						visStartRadius, // visual band start
						visEndRadius, // visual band end
						baseColors[baseIndex], // base color
						auroraProbability, // probability to spawn aurora sequence, checked once/day when no aurora in progress
						auroraColors[auroraIndex]
				));
		magField.setCircularOrbit(parent, 0, 0, 100);
		
		GenResult result = new GenResult();
		result.onlyIncrementByWidth = !isStar;
		result.orbitalWidth = orbitalWidth;
		result.entities.add(magField);
		return result;
	}

	public boolean wantsToHandle(TerrainGenDataSpec terrainData, GenContext context) {
		return terrainData != null && terrainData.getId().equals("magnetic_field");
	}

}
