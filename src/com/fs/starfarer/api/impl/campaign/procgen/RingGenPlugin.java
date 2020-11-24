package com.fs.starfarer.api.impl.campaign.procgen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenContext;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.GenResult;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class RingGenPlugin implements TerrainGenPlugin {
	
	public static class TexAndIndex {
		public String tex;
		public int index;
	}
	
	public static final String RING_GEN_DATA_KEY = "rgd_key";
	public static class RingGenData {
		public int lastOrbitAddedAt = -1;
		public float endOfLastRingRadius = -1;
		public List<SectorEntityToken> lastBatch = null;
	}
	
	
	
	protected TexAndIndex getTexAndIndex(TerrainGenDataSpec data) {
		TexAndIndex result = new TexAndIndex();
		WeightedRandomPicker<Integer> indexPicker = new WeightedRandomPicker<Integer>(StarSystemGenerator.random);
		
		if (data.getId().equals("ring_ice")) {
			result.tex = "rings_ice0";
			indexPicker.add(0);
			indexPicker.add(1);
			indexPicker.add(2);
			indexPicker.add(3);
		} else if (data.getId().equals("ring_dust")) {
			result.tex = "rings_dust0";
			indexPicker.add(0);
			indexPicker.add(1);
			indexPicker.add(2);
			indexPicker.add(3);
		} else if (data.getId().equals("ring_special")) {
			result.tex = "rings_special0";
			indexPicker.add(1);
		}
		
		result.index = indexPicker.pick();
		
		return result;
	}
	
	public RingGenData getData(GenContext context) {
		RingGenData data = null;
		if (context.customData.get(RING_GEN_DATA_KEY) instanceof RingGenData) {
			data = (RingGenData) context.customData.get(RING_GEN_DATA_KEY);
		}
		if (data == null) {
			data = new RingGenData();
			context.customData.put(RING_GEN_DATA_KEY, data);
		}
		return data;
	}
	
	
	public GenResult generate(TerrainGenDataSpec terrainData, GenContext context) {
		StarSystemAPI system = context.system;
		SectorEntityToken parent = context.center;
		if (context.parent != null) parent = context.parent;
		
		float orbitRadius = context.currentRadius;
		
//		if (context.parent != null) {
//			System.out.println("RING RADIUS " + orbitRadius + " INDEX " + context.orbitIndex);
//		}
		
		float bandWidth = 256f;

		RingGenData data = getData(context);
		TexAndIndex tex;
		
		Color color = StarSystemGenerator.getColor(terrainData.getMinColor(), terrainData.getMaxColor());
		
		List<SectorEntityToken> rings = new ArrayList<SectorEntityToken>();
		float totalWidth = 0f;
		if (StarSystemGenerator.random.nextFloat() < 0.25f) {
			// two singles, set apart a bit
			float spacing = 175f;
			totalWidth = bandWidth * 2f + spacing;
			
			float orbitDays = (orbitRadius + totalWidth / 2f) / (15f + 5f * StarSystemGenerator.random.nextFloat());
			tex = getTexAndIndex(terrainData);
			rings.add(system.addRingBand(parent, "misc", tex.tex, 256f, tex.index, color, bandWidth,
							   orbitRadius + bandWidth / 2f, orbitDays, Terrain.RING, null));
			
			orbitDays = (orbitRadius + bandWidth / 2f + spacing + bandWidth / 2f) / (15f + 5f * StarSystemGenerator.random.nextFloat());
			tex = getTexAndIndex(terrainData);
			rings.add(system.addRingBand(parent, "misc", tex.tex, 256f, tex.index, color, bandWidth,
							   orbitRadius + bandWidth / 2f + spacing + bandWidth / 2f, orbitDays * 1.2f, Terrain.RING, null));
		} else {
			float numBands = 1;
			//float pExtraBand = 0.5f;
			if (orbitRadius > 2000f && StarSystemGenerator.random.nextFloat() > 0.5f) numBands++;
			if (orbitRadius > 4000f && StarSystemGenerator.random.nextFloat() > 0.5f) numBands++;
			if (orbitRadius > 6000f && StarSystemGenerator.random.nextFloat() > 0.5f) numBands++;
			
			//numBands = 3;
			
			float startingRadius = orbitRadius + bandWidth/2f;
			float spacing = bandWidth * 0.5f;
			for (float i = 0; i < numBands; i++) {
				float currentRadius = startingRadius + i * spacing;
				float orbitDays = (currentRadius) / (15f + 5f * StarSystemGenerator.random.nextFloat());
				tex = getTexAndIndex(terrainData);
				if (StarSystemGenerator.random.nextFloat() > 0.5f) {
					if (StarSystemGenerator.random.nextFloat() > 0.5f) {
						tex = getTexAndIndex(terrainData);
					}
					rings.add(system.addRingBand(parent, "misc", tex.tex, 256f, tex.index, color, bandWidth,
							currentRadius, orbitDays * 1.2f, Terrain.RING, null));
					
					if (StarSystemGenerator.random.nextFloat() > 0.5f) {
						if (StarSystemGenerator.random.nextFloat() > 0.5f) {
							tex = getTexAndIndex(terrainData);
						}
						rings.add(system.addRingBand(parent, "misc", tex.tex, 256f, tex.index, color, bandWidth,
								currentRadius, orbitDays * 1.4f, Terrain.RING, null));
					}					
				}
			}
			totalWidth = (numBands - 1f) * spacing + bandWidth;
		}
		
		
		if (context.orbitIndex > 0 && data.lastOrbitAddedAt == context.orbitIndex - 1) {
			float spacing = bandWidth * 0.5f;
			for (float currRadius = data.endOfLastRingRadius; currRadius <= orbitRadius; currRadius += spacing) {
				float orbitDays = currRadius / (15f + 5f * StarSystemGenerator.random.nextFloat());
				tex = getTexAndIndex(terrainData);
				rings.add(system.addRingBand(parent, "misc", tex.tex, 256f, tex.index, color, bandWidth,
								   currRadius, orbitDays, Terrain.RING, null));
			}
			
			if (data.lastBatch != null) {
				rings.addAll(data.lastBatch);
				// will be re-added after this method returns, pointing to the full set of rings
				// as being part of one nameable entity
				for (SectorEntityToken ring : rings) {
					context.gen.getAllEntitiesAdded().remove(ring);
				}
			}
		}
		
		data.lastOrbitAddedAt = context.orbitIndex;
		data.endOfLastRingRadius = orbitRadius + bandWidth;
		data.lastBatch = new ArrayList<SectorEntityToken>(rings);
		
		
		GenResult result = new GenResult();
		result.onlyIncrementByWidth = false;
		result.orbitalWidth = totalWidth;
		result.entities.addAll(rings); 
		return result;
	}

	public boolean wantsToHandle(TerrainGenDataSpec terrainData, GenContext context) {
		return terrainData != null && 
			(terrainData.getId().equals("ring_ice") ||
			 terrainData.getId().equals("ring_dust") ||
			 terrainData.getId().equals("ring_special"));
	}

}
