package com.fs.starfarer.api.impl.campaign.velfield;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamBuilder.StreamType;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamParams2;
import com.fs.starfarer.api.util.Misc;

/**
 * 
 * 4444444444
 * 3333333333
 * 2222222222
 * 1111111111
 * 0123456789
 * 
 * @author Alex
 *
 * Copyright 2021 Fractal Softworks, LLC
 */
public class VelocityField {

	public static float RAD_PER_DEG = 0.01745329251f;
	public static Vector2f rotateAroundOrigin(Vector2f v, float cos, float sin) {
		Vector2f r = new Vector2f();
		r.x = v.x * cos - v.y * sin;
		r.y = v.x * sin + v.y * cos;
		return r;
	}
	
	protected Vector2f[][] field;
	protected float cellSize;
	
	public VelocityField(int width, int height, float cellSize) {
		field = new Vector2f[width][height];
		this.cellSize = cellSize;
		
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++) {
				field[i][j] = new Vector2f();
			}
		}
	}
	
	public Vector2f[][] getField() {
		return field;
	}

	public Vector2f getCell(int i, int j) {
//		if (i < 0 || j < 0) return new Vector2f();
//		if (i >= field.length || j >= field[0].length) return new Vector2f();
		if (i < 0) i = 0;
		if (j < 0) j = 0;
		if (i >= field.length) i = field.length - 1;
		if (j >= field[0].length) j = field[0].length - 1;
		return field[i][j];
	}
	
	public boolean isInsideField(float x, float y, Vector2f bottomLeft, float angle) {
		updateCacheIfNeeded(angle);
		
		x -= bottomLeft.x;
		y -= bottomLeft.y;
		Vector2f temp = new Vector2f(x, y);
		temp = rotateAroundOrigin(temp, cachedCos, -cachedSin);
		x = temp.x;
		y = temp.y;
		
		if (x < 0) return false;
		if (y < 0) return false;
		
		float w = cellSize * (field.length - 1f);
		if (x > w) return false;
		float h = cellSize * (field[0].length - 1f);
		if (y > h) return false;
		return true;
	}
	
	transient float cacheKeyAngle;
	transient float cachedSin;
	transient float cachedCos;
	public void updateCacheIfNeeded(float angle) {
		if (cacheKeyAngle != angle) {
			cacheKeyAngle = angle;
			cachedCos = (float) Math.cos(angle * RAD_PER_DEG);
			cachedSin = (float) Math.sin(angle * RAD_PER_DEG);
		}
	}
	
	public Vector2f getVelocity(float x, float y, Vector2f bottomLeft, float angle) {
		updateCacheIfNeeded(angle);
		
		x -= bottomLeft.x;
		y -= bottomLeft.y;
		Vector2f temp = new Vector2f(x, y);
		temp = rotateAroundOrigin(temp, cachedCos, -cachedSin);
		x = temp.x;
		y = temp.y;
		
		int cellX1 = (int) (x / cellSize);
		int cellY1 = (int) (y / cellSize);
		if (x < 0) cellX1 = (int) (-1f * Math.abs(x) / cellSize) - 1;
		if (y < 0) cellY1 = (int) (-1f * Math.abs(y) / cellSize) - 1;
		
		int cellX2 = cellX1 + 1;
		int cellY2 = cellY1 + 1;
		
		float px = (x / cellSize) - (float) cellX1;
		float py = (y / cellSize) - (float) cellY1;
		
		// px, py describe where the point (x, y) is in the cell between the vertices of the cell that has 
		// cellX, cellY as its bottom left corner

		//System.out.println(cellX1 + "," + cellY1 + " from x, cellSize: " + x + "," + cellSize);
		
		Vector2f bl = getCell(cellX1, cellY1);
		Vector2f br = getCell(cellX2, cellY1);
		Vector2f tl = getCell(cellX1, cellY2);
		Vector2f tr = getCell(cellX2, cellY2);
		
		Vector2f result = new Vector2f();
		result.x = (1f - py) * (bl.x * (1f - px) + br.x * px) + py * (tl.x * (1f - px) + tr.x * px); 
		result.y = (1f - px) * (bl.y * (1f - py) + tl.y * py) + px * (br.y * (1f - py) + tr.y * py); 
		result = rotateAroundOrigin(result, cachedCos, cachedSin);
		return result;
	}
	
	
//	public Vector2f getVelocityOld(float x, float y, Vector2f bottomLeft, float angle) {
//		x -= bottomLeft.x;
//		y -= bottomLeft.y;
//		Vector2f temp = new Vector2f(x, y);
//		temp = rotateAroundOrigin(temp, -angle);
//		x = temp.x;
//		y = temp.y;
//				
//		
//		int cellX1 = (int) (x / cellSize);
//		int cellY1 = (int) (y / cellSize);
//		if (x < 0) cellX1 = (int) (-1f * Math.abs(x) / cellSize) - 1;
//		if (y < 0) cellY1 = (int) (-1f * Math.abs(y) / cellSize) - 1;
//		
//		int cellX2 = cellX1 + 1;
//		int cellY2 = cellY1 + 1;
//		
//		float px = (x / cellSize) - (float) cellX1;
//		float py = (y / cellSize) - (float) cellY1;
//		
//		// px, py describe where the point (x, y) is in the cell between the vertices of the cell that has 
//		// cellX, cellY as its bottom left corner
//
//		//System.out.println(cellX1 + "," + cellY1 + " from x, cellSize: " + x + "," + cellSize);
//		
//		Vector2f bl = getCell(cellX1, cellY1);
//		Vector2f br = getCell(cellX2, cellY1);
//		Vector2f tl = getCell(cellX1, cellY2);
//		Vector2f tr = getCell(cellX2, cellY2);
//		
//		Vector2f result = new Vector2f();
//		result.x = (1f - py) * (bl.x * (1f - px) + br.x * px) + py * (tl.x * (1f - px) + tr.x * px); 
//		result.y = (1f - px) * (bl.y * (1f - py) + tl.y * py) + px * (br.y * (1f - py) + tr.y * py); 
//		result = rotateAroundOrigin(result, angle);
//		return result;
//	}

	
	public float getCellSize() {
		return cellSize;
	}
	
	public void shiftDown() {
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length - 1; j++) {
				field[i][j] = field[i][j + 1];
			}
		}
		
		for (int i = 0; i < field.length; i++) {
			field[i][field[0].length - 1] = new Vector2f(); 
		}
	}

	public static void spawnTest() {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		SlipstreamParams2 params = new SlipstreamParams2();
		
		params.burnLevel = 30;
		params.minSpeed = Misc.getSpeedForBurnLevel(params.burnLevel - 5);
		params.maxSpeed = Misc.getSpeedForBurnLevel(params.burnLevel + 5);
		params.lineLengthFractionOfSpeed = 0.25f * Math.max(0.25f, Math.min(1f, 30f / (float) params.burnLevel));
		
//		params.minColor = new Color(0.5f, 0.3f, 0.75f, 0.1f);
//		params.maxColor = new Color(0.5f, 0.6f, 1f, 0.3f);
		
		//params.slowDownInWiderSections = true;
		
//		params.baseWidth = 1025f;
//		params.widthForMaxSpeed = 768f;
		
//		float width = 512;
//		params.widthForMaxSpeed = width;
		
		//params.numParticles = 500;
//		CustomCampaignEntityAPI e = 
//				Global.getSector().getCurrentLocation().addCustomEntity(null, null, "slipstream2", Factions.NEUTRAL, params);
//		e.setLocation(pf.getLocation().x + 200f, pf.getLocation().y + 200f);
		
		
		CampaignTerrainAPI slipstream = (CampaignTerrainAPI) Global.getSector().getCurrentLocation().addTerrain(Terrain.SLIPSTREAM, params);
		slipstream.setLocation(pf.getLocation().x + 200f, pf.getLocation().y + 200f);
		
		SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) slipstream.getPlugin();
		float spacing = 200f;
		
		long seed = 23895464576452L + 4384357483229348234L;
		seed = 1181783497276652981L ^ seed;
		seed *= 27;
		Random random = new Random(seed);
		//random = Misc.random;
		SlipstreamBuilder builder = new SlipstreamBuilder(slipstream.getLocation(), plugin, StreamType.WIDE, random);
		//builder.buildTest();
		Vector2f to = new Vector2f(slipstream.getLocation());
		to.x += 20000;
		to.y += 20000;
//		to.x += 160000;
//		to.y += 100000;
		//builder.buildToDestination(to);
		Vector2f control = new Vector2f(slipstream.getLocation());
		control.x += 10000f;
		//control.x += 160000f;
		builder.buildToDestination(control, to);
		//builder.buildToDestination(to);
		
		//new SlipstreamManager().checkIntersectionsAndFadeSections(plugin);
		
		//plugin.despawn(5f);
		
//		int iter = 100;
//		for (int i = 0; i < iter; i++) {
//			float yOff = (float) Math.sin(i * 0.05f);
//			float w = width + i * 10f;
//			//float currSpacing = Math.max(spacing, width / 2f);
//			float currSpacing = spacing;
//			plugin.addSegment(new Vector2f(slipstream.getLocation().x + i * currSpacing,
//			//addSegment(new Vector2f(entity.getLocation().x + i * (spacing + (50 - i) * 5),
//					slipstream.getLocation().y + yOff * 2000f), 
//					//width);
//					//width * (0.7f + (float) Math.random() * 0.7f));
//					w);
//		}
		
		
		//SlipstreamEntityPlugin plugin = (SlipstreamEntityPlugin) e.getCustomPlugin();
		//TurbulenceEntityPlugin plugin = (TurbulenceEntityPlugin) e.getCustomPlugin();
		
//		VelocityField f = new VelocityField(11, 31, 100f);
//		plugin.setField(f);
		
		
		
	}

	
}



