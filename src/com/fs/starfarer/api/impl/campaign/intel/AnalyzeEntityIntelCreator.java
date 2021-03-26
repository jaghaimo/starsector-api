package com.fs.starfarer.api.impl.campaign.intel;

import java.util.ArrayList;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.GenericMissionManager.GenericMissionCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class AnalyzeEntityIntelCreator implements GenericMissionCreator {

	public EveryFrameScript createMissionIntel() {
		SectorEntityToken entity = pickEntity();
		if (entity == null) return null;
		return new AnalyzeEntityMissionIntel(entity);
	}
	
	public float getMissionFrequencyWeight() {
		return 15f;
	}
	
	
	protected transient WeightedRandomPicker<SectorEntityToken> entityPicker = null;
	
	protected void initPicker() {
		entityPicker = new WeightedRandomPicker<SectorEntityToken>();
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasTag(Tags.THEME_DERELICT_MOTHERSHIP)) continue;
			if (system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER)) continue;
			
			if (system.hasTag(Tags.THEME_DERELICT_PROBES) || 
					system.hasTag(Tags.THEME_RUINS) ||
					system.hasTag(Tags.THEME_REMNANT_DESTROYED)) {
				
				float w = 1f;
				if (system.hasTag(Tags.THEME_DERELICT_PROBES)) {
					w = 3f;
					if (Global.getSector().isInNewGameAdvance()) {
						w = 5f;
					}
				}
				for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.SALVAGEABLE)) {
					// skip derelict ships etc that will expire
					if (entity.hasTag(Tags.EXPIRES)) continue;
					if (Misc.isImportantForReason(entity.getMemoryWithoutUpdate(), "aem")) continue;
					if (entity.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) continue;
					if (entity.getMemoryWithoutUpdate() != null && entity.getMemoryWithoutUpdate().getBoolean("$ttWeaponsCache")) continue;
					if (entity.getCircularOrbitRadius() > 10000f) continue;
					if (entity.getContainingLocation() != null && entity.getContainingLocation().hasTag(Tags.THEME_HIDDEN)) continue;
					entityPicker.add(entity, w);
				}
				
			}
		}
	}
	
	protected void prunePicker() {
		for (SectorEntityToken item : new ArrayList<SectorEntityToken>(entityPicker.getItems())) {
			if (!item.isAlive()) {
				entityPicker.remove(item);
			}
		}
	}

	protected SectorEntityToken pickEntity() {
		if (entityPicker == null) {
			initPicker();
		}
		prunePicker();
		
		SectorEntityToken entity = entityPicker.pick();
		
		for (EveryFrameScript s : GenericMissionManager.getInstance().getActive()) {
			if (s instanceof AnalyzeEntityMissionIntel) {
				AnalyzeEntityMissionIntel intel = (AnalyzeEntityMissionIntel) s;
				if (entity == intel.getEntity()) {
					return null;
				}
			}
		}
		
		return entity;
	}
	

}



