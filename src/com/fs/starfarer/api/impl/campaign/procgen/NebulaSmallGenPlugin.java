package com.fs.starfarer.api.impl.campaign.procgen;

import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenResult;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.LagrangePointType;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class NebulaSmallGenPlugin implements TerrainGenPlugin {
	
	public static final float WIDTH_PLANET = 200f;
	public static final float WIDTH_STAR = 400f;
	
	public GenResult generate(TerrainGenDataSpec terrainData, GenContext context) {
		if (context.lagrangeParent == null || context.lagrangePointType == null) return null;
		
		//System.out.println("GENERATING L-POINT NEBULA AT " + context.star.getId());
		
		SectorEntityToken parent = context.center;
		if (context.parent != null) parent = context.parent;
		if (context.lagrangeParent != null) parent = context.center;
		
		
		WeightedRandomPicker<Integer> sizePicker = new WeightedRandomPicker<Integer>(StarSystemGenerator.random);
		for (int i = 5; i <= 15; i++) {
			sizePicker.add(i, 20 - i);
		}
		
		int size = sizePicker.pick();
		//size = 100;
		float radius = NebulaTerrainPlugin.TILE_SIZE * (float) size / 2f;
		
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
		
		if (radius > orbitRadius * 0.5f) {
			radius = orbitRadius * 0.5f;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size * size; i++) {
			sb.append("x");
		}
		String initStr = sb.toString();
		
		String nebulaType = StarSystemGenerator.nebulaTypes.get(context.gen.getStarAge());
		
		SectorEntityToken nebula = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
				initStr,
				size, size, // size of the nebula grid, should match above string
				"terrain", nebulaType, 4, 4, null));
		nebula.setCircularOrbit(parent, angle, orbitRadius, orbitDays);
		
		NebulaTerrainPlugin nebulaPlugin = (NebulaTerrainPlugin)((CampaignTerrainAPI)nebula).getPlugin();
		NebulaEditor editor = new NebulaEditor(nebulaPlugin);
		
		editor.noisePrune(0.75f);
		editor.clearArc(nebula.getLocation().x, nebula.getLocation().y, radius * 0.8f, radius * 3f, 0, 360f);
		
		GenResult result = new GenResult();
		result.onlyIncrementByWidth = false;
		result.orbitalWidth = radius * 2f;
		result.entities.add(nebula);
		return result;
	}

	public boolean wantsToHandle(TerrainGenDataSpec terrainData, GenContext context) {
		return terrainData != null && terrainData.getId().equals("nebula_small");
	}

}
