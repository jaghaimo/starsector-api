package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOffer;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.historian.HistorianData.HistorianOfferCreator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class BaseHistorianOfferCreator implements HistorianOfferCreator {

	protected float frequency = 10f;
	
	public HistorianOffer createOffer(Random random, List<HistorianOffer> soFar) {
		return null;
	}

	public boolean ignoresLimit() {
		return false;
	}

	public float getFrequency() {
		return frequency;
	}

	public void setFrequency(float frequency) {
		this.frequency = frequency;
	}
	
	

	public SectorEntityToken pickEntity(Random random, boolean allowDerelict) {
		WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(random);
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasTag(Tags.THEME_CORE_POPULATED)) continue;
			if (!allowDerelict && system.hasTag(Tags.THEME_DERELICT)) continue;
			
			if (system.hasTag(Tags.THEME_DERELICT) || 
					system.hasTag(Tags.THEME_RUINS) ||
					system.hasTag(Tags.THEME_REMNANT)) {
				
				for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.SALVAGEABLE)) {
					float w = 1f;
					
					// skip derelict ships etc that will expire
					if (entity.hasTag(Tags.EXPIRES)) continue;
					if (entity.getCircularOrbitRadius() > 10000f) continue;
					picker.add(entity, w);
				}
			}
		}
		return picker.pick();
	}
	
	
	public PlanetAPI pickUnexploredRuins(Random random) {
		WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasTag(Tags.THEME_CORE_POPULATED)) continue;
			
			if (!system.hasTag(Tags.THEME_INTERESTING) && 
					!system.hasTag(Tags.THEME_INTERESTING_MINOR)) continue;
			
			for (PlanetAPI planet : system.getPlanets()) {
				if (planet.isStar()) continue;
				
				if (Misc.hasUnexploredRuins(planet.getMarket())) {
					float w = 1f;
					picker.add(planet, w);
				}
			}
		}
		return picker.pick();
	}
	
	public static SectorEntityToken createEntity(Random random) {
		WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);
		for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			if (system.hasTag(Tags.THEME_CORE_POPULATED)) continue;
			if (system.hasTag(Tags.THEME_REMNANT_MAIN)) continue;
			if (system.hasTag(Tags.THEME_REMNANT_RESURGENT)) continue;
			
			if (!system.hasTag(Tags.THEME_INTERESTING) && 
					!system.hasTag(Tags.THEME_INTERESTING_MINOR)) continue;
			
			float sinceVisit = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp());
			if (sinceVisit < 360) continue;
			
			picker.add(system);
		}
		
		StarSystemAPI system = picker.pick();
		if (system == null) return null;

		EntityLocation loc = BaseThemeGenerator.pickHiddenLocationNotNearStar(random, system, 100f, null);
		if (loc == null) return null;
		
		//AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(system, loc, Entities.STABLE_LOCATION, Factions.NEUTRAL);
		AddedEntity added = BaseThemeGenerator.addEntity(random, system, loc, Entities.EQUIPMENT_CACHE, Factions.NEUTRAL);
		
		if (added == null || added.entity == null) return null;
		
		//added.entity.removeTag(Tags.SALVAGEABLE);
//		added.entity.setDiscoverable(null);
//		added.entity.setDiscoveryXP(null);
//		added.entity.setSensorProfile(null);
		
		//Misc.setDefenderOverride(added.entity, new DefenderDataOverride(Factions.DERELICT, 1f, 100f, 100f));
		
		added.entity.addTag(Tags.EXPIRES); // so it doesn't get targeted by "analyze entity" missions
		
		return added.entity;
	}

	public String getOfferId(BaseHistorianOffer offer) {
		return null;
	}

	public void notifyAccepted(HistorianOffer offer) {
		
	}
}





