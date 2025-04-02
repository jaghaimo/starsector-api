package com.fs.starfarer.api.impl.campaign.terrain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.enc.EncounterPoint;
import com.fs.starfarer.api.impl.campaign.enc.EncounterPointProvider;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.util.Misc;

public class HyperspaceAbyssPluginImpl extends BaseHyperspaceAbyssPlugin implements EncounterPointProvider {

	public static String EP_TYPE_ABYSSAL = "ep_type_abyssal";
	
	public static float ENCOUNTER_NEAR_ABYSSAL_SYSTEM_DIST = 2000f;
	
	
	public static float NASCENT_WELL_DETECTED_RANGE = 1000f;
	public static float JUMP_POINT_DETECTED_RANGE = 1300f;
	public static float GAS_GIANT_DETECTED_RANGE = 1600f;
	public static float STAR_DETECTED_RANGE = 2000f;
	
	
	public static float DEPTH_THRESHOLD_FOR_ENCOUNTER = 0.25f;
	public static float DEPTH_THRESHOLD_FOR_DWELLER_LIGHT = 3f;
	public static float DEPTH_THRESHOLD_FOR_ABYSSAL_LIGHT = 1f;
	public static float DEPTH_THRESHOLD_FOR_ABYSSAL_STELLAR_OBJECT = 1f;
	public static float DEPTH_THRESHOLD_FOR_ABYSSAL_STAR_SYSTEM = 0.5f;
	public static float DEPTH_THRESHOLD_FOR_NO_DUST_PARTICLES_IN_COMBAT = 0.5f;
	public static float DEPTH_THRESHOLD_FOR_FLEETS_FLEEING = 0.5f;
	
	
	public static class AbyssalEPData {
		/**
		 * The depth is uncapped. 
		 */
		public float depth;
		public Random random;
		public StarSystemAPI nearest = null;
		public float distToNearest = Float.MAX_VALUE;
		
		public AbyssalEPData() {
			
		}
	}
	
	
	public static float PLAYER_DIST_TRAVELLED_TO_REGEN_EPS = 1000f; // units not light-years
	
	protected Vector2f playerLocWhenGeneratingEPs = null;
	protected List<EncounterPoint> encounterPoints = null;
	protected Random random = new Random();
	
	public HyperspaceAbyssPluginImpl() {
		Global.getSector().getListenerManager().addListener(this);
	}
	
	protected Object readResolve() {
		if (random == null) {
			random = new Random();
		}
		return this;
	}

	public float getAbyssalDepth(Vector2f loc, boolean uncapped) {
		float w = Global.getSettings().getFloat("sectorWidth");
		float h = Global.getSettings().getFloat("sectorHeight");
		
		// Orion-Perseus Abyss, lower left of the map, plus whatever area outside the map
		float baseW = 100000f;
		float baseH = 50000f;
		
		float normalizedX = (loc.x + w/2f) / baseW;
		float normalizedY = (loc.y + h/2f) / baseH;
		
		float test = (float) (Math.sqrt(Math.max(0f, normalizedX)) + Math.sqrt(Math.max(0f, normalizedY)));
//		float test = 1f;
//		if (normalizedX >= 0 && normalizedY >= 0) {
//			test = (float) (Math.sqrt(normalizedX) + Math.sqrt(normalizedY));
//		} else if (normalizedX < 0 && (loc.y + h/2f) < baseH) {
//			test = 0f;
//		} else if (normalizedY < 0 && (loc.x + w/2f) < baseW) {
//			test = 0f;
//		}
		
//		boolean player = Misc.getDistance(Global.getSector().getPlayerFleet().getLocationInHyperspace(), loc) < 5f;
		
		if (test < 1f) {
			float threshold = 0.95f;
//			if (player) {
//				System.out.println("Depth: " + (1f - (test - threshold) / (1f - threshold)));
//			}
			if (uncapped) {
				return (1f - (test - threshold) / (1f - threshold));
			}
			if (test < threshold) return 1f;
			return 1f - (test - threshold) / (1f - threshold);
		}
		
		// outside the map area
		float left = -w/2f - loc.x; 
		float below = -h/2f - loc.y; 
		float right = loc.x - w/2f;
		float above = loc.y - h/2f;
		
		float max = Math.max(left, Math.max(right, Math.max(above, below)));
		if (max > 0) {
//			if (player) {
//				System.out.println("Depth: " + max/2000f);
//			}
			if (uncapped) return max / 2000f;
			
			return Math.min(1f, max / 2000f);
		}
		
		// inside the map, outside the Abyss area
		return 0f;
	}
	
	/**
	 * @param amount  
	 */
	public void advance(float amount) {
		if (!Global.getSector().getListenerManager().hasListener(this)) {
			Global.getSector().getListenerManager().addListener(this);
		}

	}
	
	public List<StarSystemAPI> getAbyssalSystems() {
		List<StarSystemAPI> abyssal = new ArrayList<StarSystemAPI>();
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			float depth = Misc.getAbyssalDepth(system.getLocation());
			if (depth > DEPTH_THRESHOLD_FOR_ABYSSAL_STAR_SYSTEM) {
				abyssal.add(system);
			}
		}
		return abyssal;
	}
	
	

	public List<EncounterPoint> generateEncounterPoints(LocationAPI where) {
		if (!where.isHyperspace()) return null;
		
		boolean regenerate = encounterPoints == null || playerLocWhenGeneratingEPs == null; 
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		if (!regenerate) {
			Vector2f loc = pf.getLocation();
			float dist = Misc.getDistance(loc, playerLocWhenGeneratingEPs);
			regenerate = dist > PLAYER_DIST_TRAVELLED_TO_REGEN_EPS;
		}
		
		if (!regenerate) return encounterPoints;
		
		List<StarSystemAPI> abyssal = getAbyssalSystems();
		
		playerLocWhenGeneratingEPs = new Vector2f(pf.getLocation());
		encounterPoints = new ArrayList<EncounterPoint>();
		
		float startAngle = random.nextFloat() * 360f;
		for (float angle = startAngle; angle < startAngle + 360f; angle += 30f) {
			Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
			float dist = 3000f;
			loc.scale(dist);
			Vector2f.add(loc, playerLocWhenGeneratingEPs, loc);
			loc = Misc.getPointWithinRadius(loc, 1000f, random);
			
			float depth = getAbyssalDepth(loc, true);
			if (depth < DEPTH_THRESHOLD_FOR_ENCOUNTER) continue;

			// Can match ids of other points, or have different ids for points that are 
			// very close to each other if they're across 1000-boundary 
			// this is fine. Just a way to *somewhat* limit repeated spawns in the same location
			
			String id = "abyssal_" + (int)(loc.x/1000f) + "_" + (int)(loc.y/1000f);
			EncounterPoint point = new EncounterPoint(id, where, loc, EP_TYPE_ABYSSAL);
			
			AbyssalEPData data = new AbyssalEPData();
			data.depth = depth;
			data.random = Misc.getRandom(random.nextLong(), 7);
			
			float minDist = Float.MAX_VALUE;
			StarSystemAPI nearest = null;
			for (StarSystemAPI system : abyssal) {
				float distToSystem = Misc.getDistance(system.getLocation(), loc);
				float testDist = ENCOUNTER_NEAR_ABYSSAL_SYSTEM_DIST;
				if (system.getType() == StarSystemType.DEEP_SPACE) {
					testDist *= 0.5f;
				}
				if (distToSystem < testDist && distToSystem < minDist) {
					minDist = distToSystem;
					nearest = system;
				}
			}
			if (nearest != null) {
				data.nearest = nearest;
				data.distToNearest = minDist;
			}
			
			point.custom = data;
			encounterPoints.add(point);
		}
		
		return encounterPoints;
	}
	
}




















