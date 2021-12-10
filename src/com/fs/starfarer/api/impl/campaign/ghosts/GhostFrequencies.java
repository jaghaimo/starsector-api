package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ghosts.types.NoGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.ZigguratGhost;
import com.fs.starfarer.api.util.Misc;

public class GhostFrequencies {
	public static String ZIGGURAT = "ziggurat";

	public static float getNoGhostFrequency(SensorGhostManager manager) {
		if (manager.isSpawnTriggeredBySensorBurst()) return 0f;
		
		float total = 0f;
		for (SensorGhostCreator c : SensorGhostManager.CREATORS) {
			if (!(c instanceof NoGhostCreator)) {
				float f = c.getFrequency(manager);
				//System.out.println(c.getClass().getSimpleName() + ": " + f);
				total += f; 
			}
		}
		float mult = 0.5f + getCoreFactor() * 5f;
		return Math.max(10f, total) * mult;
		//return 10f;
	}
	public static float getChargerFrequency(SensorGhostManager manager) {
		return 10f * getNotInCoreFactor() * getSBFactor(manager, 1f, 2f);
	}
	public static float getEncounterTricksterFrequency(SensorGhostManager manager) {
		return 10f * getNotInCoreFactor() * (0.5f + 0.5f * getFringeFactor()) * getSBFactor(manager, 1f, 2f);
	}
	public static float getEchoFrequency(SensorGhostManager manager) {
		return 5f * getSBFactor(manager, 1f, 5f);
	}
	public static float getGuideFrequency(SensorGhostManager manager) {
		return 10f * getNotInCoreFactor() * getSBFactor(manager, 1f, 2f);
	}
	public static float getLeviathanCalfFrequency(SensorGhostManager manager) {
		return 5f * (getNotInCoreFactor() + getFringeFactor()) * getSBFactor(manager, 1f, 0f);
	}
	public static float getLeviathanFrequency(SensorGhostManager manager) {
		return 5f * (getNotInCoreFactor() + getFringeFactor()) * getSBFactor(manager, 1f, 0f);
	}
	public static float getMinnowFrequency(SensorGhostManager manager) {
		return 10f * getNotInCoreFactor() * (0.5f + 0.5f * getFringeFactor()) * getSBFactor(manager, 1f, 0f);
	}
	public static float getRacerFrequency(SensorGhostManager manager) {
		return 10f * getNotInCoreFactor();
	}
	public static float getRemnantFrequency(SensorGhostManager manager) {
		return 1f * getFringeFactor();
	}
	public static float getRemoraFrequency(SensorGhostManager manager) {
		return 10f * getNotInCoreFactor() * (0.25f + 0.75f * getFringeFactor()) * getSBFactor(manager, 1f, 4f);
	}
	public static float getShipFrequency(SensorGhostManager manager) {
		return 2f * getFringeFactor();
	}
	public static float getStormcallerFrequency(SensorGhostManager manager) {
		return 5f * getNotInCoreFactor() * (0.5f + 0.5f * getFringeFactor()) * getSBFactor(manager, 1f, 0f);
	}
	public static float getStormTricksterFrequency(SensorGhostManager manager) {
		return 5f * getNotInCoreFactor() * (0.25f + 0.75f * getFringeFactor()) * getSBFactor(manager, 1f, 2f);
	}
	public static float getZigguratFrequency(SensorGhostManager manager) {
		if (manager.hasGhostOfClass(ZigguratGhost.class)) {
			return 0f;
		}
		String id = ZIGGURAT;
		boolean found = false;
		for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
			if (member.getHullSpec().getBaseHullId().equals(id)) {
				found = true;
				break;
			}
		}
		if (!found) return 0f;
		return 10000f;
	}
	
	
	
	
	public static float getFringeFactor() {
		Vector2f loc = Global.getSector().getPlayerFleet().getLocationInHyperspace();
		float sw = Global.getSettings().getFloat("sectorWidth");
		float sh = Global.getSettings().getFloat("sectorHeight");
		float f = 0.8f;
		float a = sw * 0.5f * f;
		float b = sh * 0.5f * f;
		float x = loc.x;
		float y = loc.y;
		
		float test = (x * x) / (a * a) + (y * y)/ (b * b);
		//System.out.println("Test: " + test);
		float result = 0f;
		if (test >= 1f) {
			result = 1f;
		} else if (test <= 0.75f) {
			result = 0f;
		} else {
			result = (test - 0.75f) / 0.25f;
		}
		return result;
	}
	
	public static float getSBFactor(SensorGhostManager manager, float factorIfNoBurst, float factorIfBurst) {
		if (manager.isSpawnTriggeredBySensorBurst()) return factorIfBurst;
		return factorIfNoBurst;
	}
	
	public static boolean isInsideCore() {
		return getCoreFactor() >= 1f;
	}
	
	public static float getNotInFringeFactor() {
		return 1f - getFringeFactor();
	}
	
	public static float getNotInCoreFactor() {
		return 1f - getCoreFactor();
	}
	public static float getCoreFactor() {
		Vector2f loc = Global.getSector().getPlayerFleet().getLocationInHyperspace();
		
		Vector2f min = Misc.getCoreMin();
		Vector2f max = Misc.getCoreMax();
		Vector2f center = Misc.getCoreCenter();
		
		float f = 1.4f;
		float a = (max.x - min.x) * 0.5f * f;
		float b = (max.y - min.y) * 0.5f * f;
		float x = loc.x - center.x;
		float y = loc.y - center.y;
		
		float test = (x * x) / (a * a) + (y * y)/ (b * b);
		//System.out.println("Test: " + test);
		float result = 0f;
		if (test >= 1f) {
			result = 0f;
		} else if (test <= 0.75f) {
			result = 1f;
		} else {
			result = 1f - (test - 0.75f) / 0.25f;
		}
		return result;
	}
	
}










