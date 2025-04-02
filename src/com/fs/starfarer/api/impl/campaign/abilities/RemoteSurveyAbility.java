package com.fs.starfarer.api.impl.campaign.abilities;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.intel.misc.RemoteSurveyDataForPlanetIntel;
import com.fs.starfarer.api.plugins.SurveyPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class RemoteSurveyAbility extends BaseDurationAbility {

	public static final String ALREADY_DID_IN_SYSTEM = "$core_didRemoteSurveyInSystem";
	
	public static final float SURVEY_RANGE = 10000f;
	public static final float DETECTABILITY_RANGE_BONUS = 5000f;
	public static final float ACCELERATION_MULT = 4f;
	
	
	protected boolean performed = false;
	
	@Override
	protected void activateImpl() {
//		CampaignFleetAPI fleet = getFleet();
//		if (fleet == null) return;
		
//		PlanetAPI target = getTargetPlanet();
//		if (target != null) {
//			GenericProbeParams params = new GenericProbeParams();
//			params.travelInDir(fleet.getFacing(), 2f);
//			params.travelTo(target);
//			params.assumeOrbit(target, 200f, 10f);
//			params.emitPing(Pings.REMOTE_SURVEY);
//			params.wait(3f);
//			params.performAction(new Script() {
//				@Override
//				public void run() {
//					if (target.getMarket() != null && 
//							target.getMarket().getSurveyLevel() != SurveyLevel.FULL) {
//						Misc.setFullySurveyed(target.getMarket(), null, false);
//						String text = "Remote survey telemery received.";
//						new SurveyDataForPlanetIntel(target, text, null);
//					}
//				}
//			});
//			CustomCampaignEntityAPI entity = fleet.getContainingLocation().addCustomEntity(null,
//					"Remote Survey Probe", Entities.GENERIC_PROBE_ACTIVE, Factions.PLAYER, params);
//			entity.setLocation(fleet.getLocation().x, fleet.getLocation().y);
//			entity.setFacing(fleet.getFacing());
//			
//			GenericProbeEntityPlugin plugin = (GenericProbeEntityPlugin) entity.getCustomPlugin();
//			plugin.getMovement().setLocation(entity.getLocation());
//			plugin.getMovement().setFacing(entity.getFacing());
//			Vector2f vel = Misc.getUnitVectorAtDegreeAngle(entity.getFacing());
////			vel.scale(100f);
////			entity.getVelocity().set(vel);
////			plugin.getMovement().setVelocity(vel);
//			
//			Misc.fadeIn(entity, 1f);
//		}
		
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
			PlanetAPI planet = findBestPlanet();
			if (planet != null && planet.getMarket() != null) {
				MarketAPI market = planet.getMarket();
				market.setSurveyLevel(SurveyLevel.PRELIMINARY);
				//Misc.setPreliminarySurveyed(market, null, true);
				
				new RemoteSurveyDataForPlanetIntel(planet);
				
				if (planet.getStarSystem() != null) {
					planet.getStarSystem().getMemoryWithoutUpdate().set(ALREADY_DID_IN_SYSTEM, true);
				}
				
			}
			performed = true;
		}
	}
	
	
	public boolean isUsable() {
		if (!super.isUsable()) return false;
		if (getFleet() == null) return false;
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet.isInHyperspace() || fleet.isInHyperspaceTransition()) return false;
		
		if (findBestPlanet() == null) return false;
		
		return true;
	}
	
	public PlanetAPI findBestPlanet() {
		
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null || fleet.isInHyperspace() || fleet.getStarSystem() == null) return null;
		
		StarSystemAPI system = fleet.getStarSystem();
		if (system.getMemoryWithoutUpdate().contains(ALREADY_DID_IN_SYSTEM)) return null;
		
		SurveyPlugin plugin = (SurveyPlugin) Global.getSettings().getNewPluginInstance("surveyPlugin");
		
		int bestScore = 0;
		PlanetAPI best = null;
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isStar()) continue;
			if (planet.getMarket() == null) continue;
			
			SurveyLevel level = planet.getMarket().getSurveyLevel();
			if (level == SurveyLevel.FULL) continue;
			
			int score = plugin.getSurveyDataScore(planet);
			if (score > bestScore) {
				bestScore = score;
				best = planet;
			}
		}
		
		return best;
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
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			LabelAPI title = tooltip.addTitle(spec.getName());
		} else {
			tooltip.addSpacer(-10f);
		}

		float pad = 10f;
		tooltip.addPara("Coordinate the fleet's active sensor network to scan all the planets in the system and "
				+ "identify the most promising candidate for a full survey operation.", 
				pad, highlight,
				"" + (int)SURVEY_RANGE);
		
		tooltip.addPara("Increases the range at which the fleet can be detected by %s* units and brings the fleet to a near-stop as drives are powered down to reduce interference.",
				pad, highlight,
				"" + (int)DETECTABILITY_RANGE_BONUS
		);
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			//List<PlanetAPI> planets = getAllPlanetsInRange();
			PlanetAPI planet = findBestPlanet();
			if (planet == null) {
				if (fleet.isInHyperspace()) {
					tooltip.addPara("Can not be used in hyperspace.", bad, pad);
				} else if (fleet.getStarSystem() != null &&
						fleet.getStarSystem().getMemoryWithoutUpdate().contains(ALREADY_DID_IN_SYSTEM)) {
					tooltip.addPara("Remote survey already performed in this star system.", bad, pad);
				} else {
					tooltip.addPara("No suitable planets in the star system.", bad, pad);
				}
			}
		}
		
		tooltip.addPara("*2000 units = 1 map grid cell", gray, pad);
		
		
		addIncompatibleToTooltip(tooltip, expanded);
		
	}

	public boolean hasTooltip() {
		return true;
	}
	
}





