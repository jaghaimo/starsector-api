package com.fs.starfarer.api.impl.campaign.intel;

import java.util.ArrayList;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.GenericMissionManager.GenericMissionCreator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SurveyPlanetIntelCreator implements GenericMissionCreator {

	public EveryFrameScript createMissionIntel() {
		PlanetAPI planet = pickPlanet();
		if (planet == null) return null;
		
		return new SurveyPlanetMissionIntel(planet);
	}
	
	public float getMissionFrequencyWeight() {
		return 10f;
	}
	
	
	protected transient WeightedRandomPicker<PlanetAPI> planetPicker = null;

	
	protected void initPicker() {
		planetPicker = new WeightedRandomPicker<PlanetAPI>();
		OUTER: for (StarSystemAPI system : Global.getSector().getStarSystems()) {
			//if (!Misc.getMarketsInLocation(system).isEmpty()) continue;
			for (MarketAPI market : Misc.getMarketsInLocation(system)) {
				if (!market.isHidden()) continue OUTER;
			}
			
			for (PlanetAPI planet : system.getPlanets()) {
				if (!isValidMissionTarget(planet)) continue;
				if (Misc.isImportantForReason(planet.getMemoryWithoutUpdate(), "spm")) continue;

				float weight = 1f;
				for (MarketConditionAPI mc : planet.getMarket().getConditions()) {
					if (mc.getGenSpec() != null) {
						weight += mc.getGenSpec().getXpMult();
					}
				}
				
				planetPicker.add(planet);
			}
		}
	}
	
	protected void prunePicker() {
		for (PlanetAPI planet : new ArrayList<PlanetAPI>(planetPicker.getItems())) {
			if (!isValidMissionTarget(planet)) {
				planetPicker.remove(planet);
			}
		}
	}
	
	public static boolean isValidMissionTarget(PlanetAPI planet) {
		if (planet.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) return false;
		if (planet.isStar() || planet.getMarket() == null || !planet.getMarket().isPlanetConditionMarketOnly() ||
				planet.getMarket().getSurveyLevel() == SurveyLevel.FULL) {
			return false;
		}
		if (planet.getContainingLocation() != null && planet.getContainingLocation().hasTag(Tags.THEME_HIDDEN)) {
			return false;
		}
		return true;
	}
	
	protected PlanetAPI pickPlanet() {
		if (planetPicker == null) {
			initPicker();
		}
		prunePicker();
		
		PlanetAPI planet = planetPicker.pick();
		for (EveryFrameScript s : GenericMissionManager.getInstance().getActive()) {
			if (s instanceof SurveyPlanetMissionIntel) {
				SurveyPlanetMissionIntel intel = (SurveyPlanetMissionIntel) s;
				if (planet == intel.getPlanet()) {
					return null;
				}
			}
		}
		
		return planet;
	}

}



