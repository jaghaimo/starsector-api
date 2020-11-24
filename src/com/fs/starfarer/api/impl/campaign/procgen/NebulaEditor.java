package com.fs.starfarer.api.impl.campaign.procgen;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.util.Misc;

public class NebulaEditor {
	
	protected BaseTiledTerrain plugin;
	protected int [][] tiles;
	protected float ts;
	protected float width, height;
	protected float cx, cy;
	protected int w, h;
	private float [][] noise;

	
	public NebulaEditor(BaseTiledTerrain plugin) {
		this.plugin = plugin;
		tiles = plugin.getTiles();
		ts = plugin.getTileSize();
		
		width = tiles.length * ts;
		height = tiles[0].length * ts;
		
		cx = plugin.getEntity().getLocation().x;
		cy = plugin.getEntity().getLocation().y;
		
		w = tiles.length;
		h = tiles[0].length;
		
		regenNoise();
	}
	
	public void regenNoise() {
		float spikes = 1f;
		noise = Misc.initNoise(StarSystemGenerator.random, w, h, spikes);
		Misc.genFractalNoise(StarSystemGenerator.random, noise, 0, 0, w - 1, h - 1, 1, spikes);
		Misc.normalizeNoise(noise);
		
//		// bug in noise generation? last line doesn't seem to have high enough values
		// never mind, noise has to be power of two-sized
//		for (int i = 0; i < w; i++) {
//			noise[i][h - 1] = noise[i][h - 2];
//		}
	}
	
//	public void noisePrune(float fractionKeep) {
//		if (noise == null) regenNoise();
//		int count = 0;
//		for (int i = 0; i < w; i++) {
//			for (int j = 0; j < h; j++) {
//				if (noise[i][j] < 1f - fractionKeep) {
//					tiles[i][j] = -1;
//					count++;
//				}
//			}
//		}
//		//System.out.println("Pruned " + (int)((count * 100f) / (float) (w * h)) + "% with keep=" + fractionKeep);
//	}
	
	public void noisePrune(float fractionKeep) {
		if (noise == null) regenNoise();
		float [] counts = new float[100];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				float f = noise[i][j];
				int index = (int) (f * 100f);
				if (index < 0) index = 0;
				if (index > 99) index = 99;
				counts[index]++;
			}
		}
		
		float total = w * h;
		float keep = fractionKeep * total;
		float threshold = 0f;
		float totalKept = 0f;
		for (int i = 0; i < 100; i++) {
			totalKept += counts[i];
			if (totalKept >= keep) {
				threshold = (float) i / 100f;
				break;
			}
		}
		
		int count = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (noise[i][j] > threshold) {
					tiles[i][j] = -1;
					count++;
				}
			}
		}
		//System.out.println("Pruned " + (int)((count * 100f) / (float) (w * h)) + "% with keep=" + fractionKeep);
	}
	
	
	public void clearArc(float x, float y, float innerRadius, float outerRadius, float startAngle, float endAngle) {
		clearArc(x, y, innerRadius, outerRadius, startAngle, endAngle, 0f);
	}
	
	public void clearArc(float x, float y, float innerRadius, float outerRadius, float startAngle, float endAngle, float noiseThresholdToClear) {
		clearArc(x, y, innerRadius, outerRadius, startAngle, endAngle, 1f, noiseThresholdToClear);
	}
	
	public void clearArc(float x, float y, float innerRadius, float outerRadius, float startAngle, float endAngle, float endRadiusMult, float noiseThresholdToClear) {
		float circumference = (float) Math.PI * 2f * outerRadius;
		float degreesPerIteration = 360f / (circumference / (ts * 0.5f));
		
		for (float angle = startAngle; angle < endAngle; angle += degreesPerIteration) {
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
			float distMult = 1f;
			if (endAngle > startAngle) {
				float p = (angle - startAngle) / (endAngle - startAngle);
				distMult = 1f + (endRadiusMult - 1f) * p;
			}
			
			//for (float dist = innerRadius; dist <= outerRadius; dist += ts * 0.5f) {
			for (float dist = innerRadius * distMult; dist <= innerRadius * distMult + (outerRadius - innerRadius); dist += ts * 0.5f) {
				Vector2f curr = new Vector2f(dir);
				//curr.scale(dist * distMult);
				curr.scale(dist);
				curr.x += x;
				curr.y += y;
				setTileAt(curr.x, curr.y, -1, noiseThresholdToClear);
			}
		}
	}
	
	public void setTileAt(float x, float y, int value) {
		setTileAt(x, y, value, 0f);
	}
	
	public void setTileAt(float x, float y, int value, float noiseThresholdToClear) {
		int cellX = (int) ((width / 2f + x - cx) / ts);
		int cellY = (int) ((height / 2f + y - cy) / ts);
		
//		if (cellX < 0) cellX = 0;
//		if (cellY < 0) cellY = 0;
//		if (cellX > tiles.length - 1) cellX = tiles.length - 1; 
//		if (cellY > tiles[0].length - 1) cellY = tiles[0].length - 1;
		
		if (cellX < 0) return;
		if (cellY < 0) return;
		if (cellX > tiles.length - 1) return; 
		if (cellY > tiles[0].length - 1) return;
//		if (cellX < 0) cellX = 0;
//		if (cellY < 0) cellY = 0;
//		if (cellX > tiles.length - 1) cellX = tiles.length - 1; 
//		if (cellY > tiles[0].length - 1) cellY = tiles[0].length - 1;
		
		if (noiseThresholdToClear <= 0 || noise[cellX][cellY] > noiseThresholdToClear) {
			tiles[cellX][cellY] = value;
		}
		//tiles[cellX][tiles[0].length - 1] = -1;
	}
	
	
	
}

















