package com.fs.starfarer.api.impl.campaign.ghosts;

import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class BaseSensorGhostCreator implements SensorGhostCreator {
	
	public List<SensorGhost> createGhost(SensorGhostManager manager) {
		return null;
	}

	public float getFrequency(SensorGhostManager manager) {
		return 10f;
	}

	public String getId() {
		return getClass().getSimpleName();
	}

	public float getTimeoutDaysOnSuccessfulCreate(SensorGhostManager manager) {
		return 10f + manager.getRandom().nextFloat() * 10f;
	}
	
	
	public static Vector2f findClearHyperspaceArea(Vector2f from, float minRange, float maxRange, float radius, Random random) {
		return findHyperspaceArea(from, minRange, maxRange, radius, random, true, 0f);
	}
	public static Vector2f findDeepHyperspaceArea(Vector2f from, float minRange, float maxRange, float radius, Random random) {
		return findHyperspaceArea(from, minRange, maxRange, radius, random, false, 0f);
	}
	public static Vector2f findHyperspaceArea(Vector2f from, float minRange, float maxRange, float radius, Random random, boolean clear, float noSlipstreamRange) {
		if (random == null) random = Misc.random;
		
		CampaignTerrainAPI terrain = Misc.getHyperspaceTerrain();
		if (terrain == null) return null;
		
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) terrain.getPlugin();
		
		int[][] tiles = plugin.getTiles();
		float perTile = plugin.getTileSize();
		int areaSize = (int) Math.ceil(radius / perTile);
		
		
		Vector2f best = null;
		float bestScore = 0f;
		
		float total = (areaSize + areaSize + 1) * (areaSize + areaSize + 1);
		if (total < 1f) total = 1f;
		for (int k = 0; k < 20; k++) {
			Vector2f test = Misc.getPointWithinRadiusUniform(from, minRange, maxRange, random);
			int [] coords = plugin.getTile(test);
			if (coords == null) continue;
			
			if (noSlipstreamRange > 0) {
				if (Misc.isInsideSlipstream(test, noSlipstreamRange)) continue;
			}
			
			float count = 0f;
			for (int i = coords[0] - areaSize; i <= coords[0] + areaSize; i++) {
				if (i < 0) continue;
				if (i >= tiles.length) continue;
				for (int j = coords[1] - areaSize; j < coords[1] + areaSize; j++) {
					if (j < 0) continue;
					if (j >= tiles[0].length) continue;
				
					int texIndex = tiles[i][j];
					if (clear) {
						if (texIndex <= 0) {
							count++;
						}
					} else {
						if (texIndex > 0) {
							count++;
						}
					}
				}
			}
			float score = count / total;
			if (score >= 0.75f && score > bestScore) {
				best = test;
				bestScore = score;
			}
		}
		
		return best;
	}
	
	public static SlipstreamTerrainPlugin2 pickNearbySlipstream(float radius, Random random) {
		return pickNearbySlipstream(radius, Global.getSector().getHyperspace(), random);
	}
	public static SlipstreamTerrainPlugin2 pickNearbySlipstream(float radius, LocationAPI location, Random random) {
		WeightedRandomPicker<SlipstreamTerrainPlugin2> picker = new WeightedRandomPicker<SlipstreamTerrainPlugin2>(random);
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		Vector2f loc = pf.getLocation();
		
		for (CampaignTerrainAPI ter : location.getTerrainCopy()) {
			if (ter.getPlugin() instanceof SlipstreamTerrainPlugin2) {
				SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) ter.getPlugin();
				if (plugin.containsPoint(loc, radius)) {
					float w = 1f;
					if (plugin.isPreventedFromAffecting(pf)) {
						w *= 0.25f; // more likely to pick the slipstream the player is in, if there is one
					}
					picker.add(plugin, w);
				}
			}
		}
		return picker.pick();
	}
	
	// moved to Misc
//	public static boolean isInsideSlipstream(Vector2f loc, float radius) {
//		return isInsideSlipstream(loc, radius, Global.getSector().getHyperspace());
//	}
//	public static boolean isInsideSlipstream(Vector2f loc, float radius, LocationAPI location) {
//		for (CampaignTerrainAPI ter : location.getTerrainCopy()) {
//			if (ter.getPlugin() instanceof SlipstreamTerrainPlugin2) {
//				SlipstreamTerrainPlugin2 plugin = (SlipstreamTerrainPlugin2) ter.getPlugin();
//				if (plugin.containsPoint(loc, radius)) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	public boolean canSpawnWhilePlayerInOrNearSlipstream() {
		return false;
	}
}






