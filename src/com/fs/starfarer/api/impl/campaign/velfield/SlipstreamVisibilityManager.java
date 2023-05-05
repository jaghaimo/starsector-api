package com.fs.starfarer.api.impl.campaign.velfield;

import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamManager.CustomStreamRevealer;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamTerrainPlugin2.SlipstreamSegment;
import com.fs.starfarer.api.util.CollisionGridUtil;
import com.fs.starfarer.api.util.Misc;

public class SlipstreamVisibilityManager implements CoreUITabListener {
	
//	public static String KEY = "$svm_ref";
//	
//	protected transient CollisionGridUtil grid;
//	protected IntervalUtil interval = new IntervalUtil(1f, 2f);
//	protected Random random = new Random();
//	
//	public static SlipstreamVisibilityManager get() {
//		return (SlipstreamVisibilityManager) Global.getSector().getMemoryWithoutUpdate().get(KEY);
//	}
//	
//	public SlipstreamVisibilityManager() {
//		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
//	}
//	
//	protected Object readResolve() {
//		return this;
//	}
	
	public void reportAboutToOpenCoreTab(CoreUITabId tab, Object param) {
		if (tab == CoreUITabId.MAP) {
			
			HyperspaceTopographyEventIntel intel = HyperspaceTopographyEventIntel.get();
			if (intel != null) {
				intel.updateMarketDetectionRanges();
			}
			//System.out.println("OPENING MAP");
			//for (int i = 0; i < 100; i++) {
				updateSlipstreamVisibility(null, 0f);
			//}
		}
	}
	
	public static void updateSlipstreamVisibility(Vector2f extraPoint, float extraRangeLY) {
		float sw = Global.getSettings().getFloat("sectorWidth");
		float sh = Global.getSettings().getFloat("sectorHeight");
		float minCellSize = 12000f;
		float cellSize = Math.max(minCellSize, sw * 0.05f);
		
		CollisionGridUtil grid = new CollisionGridUtil(-sw/2f, sw/2f, -sh/2f, sh/2f, cellSize);
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (market.getContainingLocation() == null) continue;
			if (!market.isPlayerOwned()) continue;
			
			float range = market.getStats().getDynamic().getMod(Stats.SLIPSTREAM_REVEAL_RANGE_LY_MOD).computeEffective(0f);
			range *= Misc.getUnitsPerLightYear();
			if (range <= 0) continue;
			
//			Industry spaceport = market.getIndustry(Industries.SPACEPORT);
//			if (spaceport == null || !spaceport.isFunctional()) continue;
			
			Vector2f loc = market.getLocationInHyperspace();
			CustomStreamRevealer revealer = new CustomStreamRevealer(loc, range);
			grid.addObject(revealer, loc, range * 2f, range * 2f);
		}
		
		if (extraPoint != null && extraRangeLY > 0) {
			float range = extraRangeLY * Misc.getUnitsPerLightYear();
			CustomStreamRevealer revealer = new CustomStreamRevealer(extraPoint, range);
			grid.addObject(revealer, extraPoint, range * 2f, range * 2f);
		}
		
		//System.out.println("BEGIN");
		float maxDist = 0f;
		List<CampaignTerrainAPI> terrainList = Global.getSector().getHyperspace().getTerrainCopy();
		for (CampaignTerrainAPI terrain : terrainList) {
			if (terrain.getPlugin() instanceof SlipstreamTerrainPlugin2) {
				//System.out.println("Processing: " + terrain.getId());
				SlipstreamTerrainPlugin2 stream = (SlipstreamTerrainPlugin2) terrain.getPlugin();
				if (stream.isDynamic()) continue;
				
				for (SlipstreamSegment curr : stream.getSegments()) {
					if (curr.discovered) continue;
					Iterator<Object> iter = grid.getCheckIterator(curr.loc, curr.width / 2f, curr.width / 2f);
					//Iterator<Object> iter = grid.getCheckIterator(curr.loc, 100f, 100f);
					while (iter.hasNext()) {
						Object obj = iter.next();
						if (obj instanceof CustomStreamRevealer) {
							CustomStreamRevealer rev = (CustomStreamRevealer) obj;
							Vector2f loc = rev.loc;
							float radius = rev.radius;
							
							float dist = Misc.getDistance(loc, curr.loc);
							if (dist > maxDist) {
								maxDist = dist;
//								if (dist >= 32500) {
//									System.out.println("Rev loc: " + rev.loc);
//									//grid.getCheckIterator(curr.loc, 100f, 100f);
//								}
							}
							if (dist < radius) {
								curr.discovered = true;
								break;
							}
						}
					}
				}
				//break;
			}
		}
	}	
	
	
}
