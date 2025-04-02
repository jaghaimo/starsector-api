package com.fs.starfarer.api.impl.campaign.enc;

import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceAbyssPluginImpl;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceAbyssPluginImpl.AbyssalEPData;

public class AbyssalFrequencies {
	
	public static float NO_ABYSS_ENCOUNTER_MULT = 10f;
	
	public static float DWELLER_FREQ_MIN = 3f;
	public static float DWELLER_FREQ_MAX = 10f;
	
	public static float LIGHT_FREQ = 10f;
	public static float ROGUE_REGULAR_FREQ = 5f;
	public static float ROGUE_HINTS_FREQ = 5f;
	
	public static float getNoAbyssalEncounterFrequency(EncounterManager manager, EncounterPoint point) {
		if (!HyperspaceAbyssPluginImpl.EP_TYPE_ABYSSAL.equals(point.type)) return 0f;
		
		float total = 0f;
		for (EPEncounterCreator c : EncounterManager.CREATORS) {
			if (!(c instanceof AbyssalNoEPEC)) {
				// non-abyssal creators should return 0 for abyssal points
				float f = c.getFrequencyForPoint(manager, point);
				total += f; 
			}
		}
		
		total *= NO_ABYSS_ENCOUNTER_MULT;
		
		return Math.min(10000f, Math.max(100f, total));
	}
	
	public static boolean isPointSuited(EncounterPoint point, boolean allowNearStar, float depthRequired) {
		if (!HyperspaceAbyssPluginImpl.EP_TYPE_ABYSSAL.equals(point.type)) return false;
		AbyssalEPData data = (AbyssalEPData) point.custom;
		if (data.depth < depthRequired) return false;
		if (!allowNearStar && data.nearest != null) return false;
		return true;
	}
	
	public static float getAbyssalLightFrequency(EncounterManager manager, EncounterPoint point) {
		if (!isPointSuited(point, false, HyperspaceAbyssPluginImpl.DEPTH_THRESHOLD_FOR_ABYSSAL_LIGHT)) {
			return 0f;
		}
		return LIGHT_FREQ;
	}
	
	public static float getAbyssalLightDwellerFrequency(EncounterManager manager, EncounterPoint point) {
		if (!isPointSuited(point, false, HyperspaceAbyssPluginImpl.DEPTH_THRESHOLD_FOR_DWELLER_LIGHT)) {
			return 0f;
		}
		
		AbyssalEPData data = (AbyssalEPData) point.custom;
		float f = DWELLER_FREQ_MIN;
		f += (DWELLER_FREQ_MAX - DWELLER_FREQ_MIN) * 
				(data.depth - HyperspaceAbyssPluginImpl.DEPTH_THRESHOLD_FOR_DWELLER_LIGHT) * 0.33f;
		if (f > DWELLER_FREQ_MAX) f = DWELLER_FREQ_MAX; 
		return f;
	}
	
	public static float getAbyssalRogueStellarObjectFrequency(EncounterManager manager, EncounterPoint point) {
		if (!isPointSuited(point, false, HyperspaceAbyssPluginImpl.DEPTH_THRESHOLD_FOR_ABYSSAL_STELLAR_OBJECT)) {
			return 0f;
		}
		return ROGUE_REGULAR_FREQ;
	}
	
	public static float getAbyssalRogueStellarObjectDireHintsFrequency(EncounterManager manager, EncounterPoint point) {
		if (!isPointSuited(point, false, HyperspaceAbyssPluginImpl.DEPTH_THRESHOLD_FOR_ABYSSAL_STELLAR_OBJECT)) {
			return 0f;
		}
		if (DebugFlags.ABYSSAL_GHOST_SHIPS_DEBUG) {
			return 1000000000f;
		}
		return ROGUE_HINTS_FREQ;
	}
	


}





