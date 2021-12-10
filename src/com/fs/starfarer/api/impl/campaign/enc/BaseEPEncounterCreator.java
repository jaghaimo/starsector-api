package com.fs.starfarer.api.impl.campaign.enc;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathBaseIntel;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel;
import com.fs.starfarer.api.util.Misc;

public class BaseEPEncounterCreator implements EPEncounterCreator {
	public static float PATHER_AMBUSH_RANGE_FOR_FULL_PROXIMITY_FACTOR = 4000f;
	public static float PATHER_AMBUSH_MAX_RANGE = 16000f;
	
	public static float PIRATE_AMBUSH_RANGE_FOR_FULL_PROXIMITY_FACTOR = 4000f;
	public static float PIRATE_AMBUSH_MAX_RANGE = 16000f;
	
	public static float RUINS_RANGE_FOR_FULL_PROXIMITY_FACTOR = 4000f;
	public static float RUINS_MAX_RANGE = 16000f;
	
	public static float CORE_PROXIMITY_MAX_RANGE = 30000f;
	
	
	public String getId() {
		return getClass().getSimpleName();
	}

	public float getPointTimeoutMin() {
		return 30f;
	}
	public float getPointTimeoutMax() {
		return 90f;
	}

	public float getCreatorTimeoutMin() {
		return 0;
	}

	public float getCreatorTimeoutMax() {
		return 0;
	}

	public float getFrequencyForPoint(EncounterManager manager, EncounterPoint point) {
		return 10f;
	}

	public void createEncounter(EncounterManager manager, EncounterPoint point) {
	}
	
	
	
	public static float getLuddicPathBaseProximityFactor(LuddicPathBaseIntel base, Vector2f locInHyper) {
		if (base == null) return 0f;
		float dist = Misc.getDistance(base.getEntity().getLocationInHyperspace(), locInHyper);
		if (dist < PATHER_AMBUSH_RANGE_FOR_FULL_PROXIMITY_FACTOR) {
			return 1f;
		}
		float f = 1f - (dist - PATHER_AMBUSH_RANGE_FOR_FULL_PROXIMITY_FACTOR) / (PATHER_AMBUSH_MAX_RANGE - PATHER_AMBUSH_RANGE_FOR_FULL_PROXIMITY_FACTOR);
		if (f < 0f) f = 0f;
		if (f > 1f) f = 1f;
		return f;
				
	}
	public static LuddicPathBaseIntel getClosestLuddicPathBase(Vector2f locInHyper) {
		return getClosestLuddicPathBase(locInHyper, true);
	}
	public static LuddicPathBaseIntel getClosestLuddicPathBase(Vector2f locInHyper, boolean onlyInProximity) {
		LuddicPathBaseIntel closest = null;
		float minDist = Float.MAX_VALUE;
		for (IntelInfoPlugin p : Global.getSector().getIntelManager().getIntel(LuddicPathBaseIntel.class)) {
			LuddicPathBaseIntel intel = (LuddicPathBaseIntel) p;
			if (intel.getEntity() == null || !intel.getEntity().isAlive()) continue;
			float dist = Misc.getDistance(intel.getEntity().getLocationInHyperspace(), locInHyper);
			if (onlyInProximity && dist > PATHER_AMBUSH_MAX_RANGE) continue;
			if (dist < minDist) {
				minDist = dist;
				closest = intel;
			}
		}
		return closest;
	}
	
	public static float getPirateBaseProximityFactor(PirateBaseIntel base, Vector2f locInHyper) {
		if (base == null) return 0f;
		float dist = Misc.getDistance(base.getEntity().getLocationInHyperspace(), locInHyper);
		if (dist < PIRATE_AMBUSH_RANGE_FOR_FULL_PROXIMITY_FACTOR) {
			return 1f;
		}
		float f = 1f - (dist - PIRATE_AMBUSH_RANGE_FOR_FULL_PROXIMITY_FACTOR) / (PIRATE_AMBUSH_MAX_RANGE - PIRATE_AMBUSH_RANGE_FOR_FULL_PROXIMITY_FACTOR);
		if (f < 0f) f = 0f;
		if (f > 1f) f = 1f;
		return f;
				
	}
	public static PirateBaseIntel getClosestPirateBase(Vector2f locInHyper) {
		return getClosestPirateBase(locInHyper, true);
	}
	public static PirateBaseIntel getClosestPirateBase(Vector2f locInHyper, boolean onlyInProximity) {
		PirateBaseIntel closest = null;
		float minDist = Float.MAX_VALUE;
		for (IntelInfoPlugin p : Global.getSector().getIntelManager().getIntel(PirateBaseIntel.class)) {
			PirateBaseIntel intel = (PirateBaseIntel) p;
			if (intel.getEntity() == null || !intel.getEntity().isAlive()) continue;
			float dist = Misc.getDistance(intel.getEntity().getLocationInHyperspace(), locInHyper);
			if (onlyInProximity && dist > PIRATE_AMBUSH_MAX_RANGE) continue;
			if (dist < minDist) {
				minDist = dist;
				closest = intel;
			}
		}
		return closest;
	}
	
	
	public static float getCoreProximityFactor(Vector2f locInHyper) {
		Vector2f min = Misc.getCoreMin();
		Vector2f max = Misc.getCoreMax();
		Vector2f core = Misc.getCoreCenter();
		
		float across = Misc.getDistance(min, max);
		float fullProximityAt = across * 0.5f;
		fullProximityAt = Math.min(CORE_PROXIMITY_MAX_RANGE / 2f, fullProximityAt);
		
		float dist = Misc.getDistance(core, locInHyper);
		if (dist < fullProximityAt) {
			return 1f;
		}
		float f = 1f - (dist - fullProximityAt) / (CORE_PROXIMITY_MAX_RANGE - fullProximityAt);
		if (f < 0f) f = 0f;
		if (f > 1f) f = 1f;
		return f;
	}
	
	public static float getRuinsProximityFactor(StarSystemAPI system, Vector2f locInHyper) {
		if (system == null) return 0f;
		float dist = Misc.getDistance(system.getLocation(), locInHyper);
		if (dist < RUINS_RANGE_FOR_FULL_PROXIMITY_FACTOR) {
			return 1f;
		}
		float f = 1f - (dist - RUINS_RANGE_FOR_FULL_PROXIMITY_FACTOR) / (RUINS_MAX_RANGE - RUINS_RANGE_FOR_FULL_PROXIMITY_FACTOR);
		if (f < 0f) f = 0f;
		if (f > 1f) f = 1f;
		return f;
	}
	
	
	public static StarSystemAPI getClosestSystemWithRuins(Vector2f locInHyper) {
		return getClosestSystemWithRuins(locInHyper, true);
	}
	public static StarSystemAPI getClosestSystemWithRuins(Vector2f locInHyper, boolean onlyInProximity) {
		StarSystemAPI closest = null;
		float minDist = Float.MAX_VALUE;
		for (StarSystemAPI curr : Global.getSector().getStarSystems()) {
			if (curr.hasTag(Tags.THEME_RUINS_MAIN)) {
				float dist = Misc.getDistance(curr.getLocation(), locInHyper);
				if (onlyInProximity && dist > RUINS_MAX_RANGE) continue;
				if (dist < minDist) {
					minDist = dist;
					closest = curr;
				}
			}
		}
		return closest;
	}
}
