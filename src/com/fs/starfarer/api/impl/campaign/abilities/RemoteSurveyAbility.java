package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class RemoteSurveyAbility extends BaseDurationAbility {

	public static final float SURVEY_RANGE = 10000f;
	public static final float DETECTABILITY_RANGE_BONUS = 5000f;
	public static final float ACCELERATION_MULT = 4f;
	
	
	protected boolean performed = false;
	
	@Override
	protected void activateImpl() {
		if (entity.isInCurrentLocation()) {
			VisibilityLevel level = entity.getVisibilityLevelToPlayerFleet();
			if (level != VisibilityLevel.NONE) {
				Global.getSector().addPing(entity, Pings.REMOTE_SURVEY);
			}
			
			performed = false;
		}
		
	}

	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		//float b = fleet.getStats().getDynamic().getValue(Stats.SENSOR_BURST_BURN_PENALTY_MULT);
		//float b = 1f;
		//fleet.getStats().getFleetwideMaxBurnMod().modifyMult(getModId(), 1f + (0f - 1f * level) * b, "Remote survey");
		fleet.getStats().getFleetwideMaxBurnMod().modifyMult(getModId(), 0f, "Remote survey");
		fleet.getStats().getDetectedRangeMod().modifyFlat(getModId(), DETECTABILITY_RANGE_BONUS * level, "Remote survey");
		fleet.getStats().getAccelerationMult().modifyMult(getModId(), 1f + (ACCELERATION_MULT - 1f) * level);
		
		if (!performed && level >= 1f) {
			// do the actual survey stuff
			
			for (PlanetAPI planet : getSurveyableInRange()) {
				MarketAPI market = planet.getMarket();
				SurveyLevel surveyLevel = market.getSurveyLevel();
				if (market == null || (surveyLevel != SurveyLevel.SEEN && surveyLevel != SurveyLevel.NONE)) {
					continue;
				}
				
				Misc.setPreliminarySurveyed(market, null, true);
			}
			
			performed = true;
		}
	}
	
	
	public boolean isUsable() {
		if (!super.isUsable()) return false;
		if (getFleet() == null) return false;
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet.isInHyperspace() || fleet.isInHyperspaceTransition()) return false;
		
		if (getSurveyableInRange().isEmpty()) return false;
		
		return true;
	}
	
	protected List<PlanetAPI> getAllPlanetsInRange() {
		List<PlanetAPI> result = new ArrayList<PlanetAPI>();
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return result;
		if (fleet.isInHyperspace()) return result;
		
		
		for (PlanetAPI planet : fleet.getContainingLocation().getPlanets()) {
			if (planet.isStar()) continue;
			if (planet.getMarket() == null) continue;
			
			//SurveyLevel level = planet.getMarket().getSurveyLevel();
			
			float dist = Misc.getDistance(fleet.getLocation(), planet.getLocation());
			if (dist <= SURVEY_RANGE) {
				result.add(planet);
			}
		}
		return result;
	}
	
	protected List<PlanetAPI> getSurveyableInRange() {
		List<PlanetAPI> result = getAllPlanetsInRange();
		
		Iterator<PlanetAPI> iter = result.iterator();
		while (iter.hasNext()) {
			PlanetAPI curr = iter.next();
			SurveyLevel level = curr.getMarket().getSurveyLevel();
			if (level != SurveyLevel.SEEN && level != SurveyLevel.NONE) {
				iter.remove();
			}
		}
		
		return result;
	}
	

	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}
	
	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		fleet.getStats().getDetectedRangeMod().unmodify(getModId());
		fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
		fleet.getStats().getAccelerationMult().unmodify(getModId());
	}

	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI title = tooltip.addTitle(spec.getName());

		float pad = 10f;
		//tooltip.addPara("Coordinate the fleet's active sensor network to perform a preliminary survey of nearby planets.", pad);
		
		tooltip.addPara("Coordinate the fleet's active sensor network to perform a preliminary survey of all unsurveyed planets within %s* units.", 
				pad, highlight,
				"" + (int)SURVEY_RANGE);
		
		tooltip.addPara("Increases the range at which the fleet can be detected by %s* units and brings the fleet to a near-stop as drives are powered down to reduce interference.",
				pad, highlight,
				"" + (int)DETECTABILITY_RANGE_BONUS
		);
		
		
		//List<PlanetAPI> planets = getAllPlanetsInRange();
		List<PlanetAPI> planets = getSurveyableInRange();
		if (planets.isEmpty()) {
			if (getAllPlanetsInRange().isEmpty()) {
				tooltip.addPara("No planets in range.", bad, pad);
			} else {
				tooltip.addPara("You have either full or preliminary survey data for all planets in range.", bad, pad);
			}
		} else {
			tooltip.addPara("The following unsurveyed planets are in range:", pad);
	
//			tooltip.beginGridFlipped(1300f, 1, 50f, 10f);
//			int j = 0;
//			for (PlanetAPI planet : planets) {
//				float dist = Misc.getDistance(fleet.getLocation(), planet.getLocation());
//				String distStr = Misc.getWithDGS(dist);
//				
//				String status = planet.getName() + ", " + planet.getTypeNameWithWorld().toLowerCase();
//				SurveyLevel level = planet.getMarket().getSurveyLevel();
//				
//				if (level == SurveyLevel.PRELIMINARY) status += " (preliminary)";
//				else if (level == SurveyLevel.FULL) status += " (full survey)";
//				else status += " (unsurveyed)";
//				
//				tooltip.addToGrid(0, j++, status, distStr);
//			}
//			tooltip.addGrid(pad);
			
			float currPad = 3f;
			String indent = "    ";
			for (PlanetAPI planet : planets) {
				//String level = Misc.getSurveyLevelString(planet.getMarket().getSurveyLevel(), true);
				LabelAPI label = tooltip.addPara(indent + planet.getName() + ", %s",
						currPad, planet.getSpec().getIconColor(),
						planet.getTypeNameWithWorld().toLowerCase());
//				label.setHighlightColor(highlight);
//				label.highlightLast(level);
				currPad = 0f;
			}
			
//			if (getSurveyableInRange().isEmpty()) {
//				tooltip.addPara("No surveyable planets in range.", bad, pad);
//			}
		}
		
		tooltip.addPara("*2000 units = 1 map grid cell", gray, pad);
		
		
		addIncompatibleToTooltip(tooltip, expanded);
		
	}

	public boolean hasTooltip() {
		return true;
	}
	
}





