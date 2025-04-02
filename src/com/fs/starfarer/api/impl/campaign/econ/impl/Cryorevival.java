package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.util.Map;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.listeners.ColonyOtherFactorsListener;
import com.fs.starfarer.api.impl.PlanetSearchData.PlanetFilter;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.misc.CryosleeperIntel;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public class Cryorevival extends BaseIndustry implements MarketImmigrationModifier {

	public static float ALPHA_CORE_BONUS = 1f;
	public static float IMPROVE_BONUS = 1f;
	
	public static float MIN_BONUS_MULT = 0.1f;
	public static float MAX_BONUS_DIST_LY = 10f;
	
	public static float MAX_BONUS_WHEN_UNMET_DEMAND = 0.5f;
	
	public void apply() {
		super.apply(true);
		
		//int size = market.getSize();
		//demand(Commodities.HEAVY_MACHINERY, size);
		demand(Commodities.ORGANICS, 10);
	}

	
	@Override
	public void unapply() {
		super.unapply();
	}

	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			Color h = Misc.getHighlightColor();
			float opad = 10f;
			
			float bonus = getImmigrationBonus();
			float max = getMaxImmigrationBonus();

			float distMult = getDistancePopulationMult(market.getLocationInHyperspace());
			float demandMult = getDemandMetPopulationMult();
			
			Pair<SectorEntityToken, Float> p = getNearestCryosleeper(market.getLocationInHyperspace(), true);
			if (p != null) {
				String dStr = "" + Misc.getRoundedValueMaxOneAfterDecimal(p.two);
				String lights = "light-years";
				if (dStr.equals("1")) lights = "light-year";
				tooltip.addPara("Distance to nearest cryosleeper: %s " + lights + ", growth bonus multiplier: %s.",
						opad, h,
						"" + Misc.getRoundedValueMaxOneAfterDecimal(p.two), 
						"" + (int)Math.round(distMult * 100f) + "%");
			}
			if (mode != IndustryTooltipMode.NORMAL) {
				tooltip.addPara("If any demand is unmet, " +
						"the maximum growth bonus is reduced by %s.",
						opad, h,
						"" + (int)Math.round(MAX_BONUS_WHEN_UNMET_DEMAND * 100f) + "%");
			} else {
				tooltip.addPara("%s growth bonus multiplier based on met demand. If any demand is unmet, " +
								"the maximum bonus is reduced by %s.",
								opad, h,
								"" + (int)Math.round(demandMult * 100f) + "%",
								"" + (int)Math.round(MAX_BONUS_WHEN_UNMET_DEMAND * 100f) + "%");
			}
							
			
			tooltip.addPara("Population growth: %s (max for colony size: %s)", opad, h, "+" + Math.round(bonus), "+" + Math.round(max));
		}
	}
	
	
	@Override
	public boolean isAvailableToBuild() {
		//if (Global.getSettings().isDevMode()) return true;
		
		return getDistancePopulationMult(market.getLocationInHyperspace()) > 0; 
		
//		StarSystemAPI system = market.getStarSystem();
//		if (system == null) return false;
//		for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.CRYOSLEEPER)) {
//			if (entity.getMemoryWithoutUpdate().contains("$usable")) {
//				return true;
//			}
//		}
//		return false;
	}


	@Override
	public boolean showWhenUnavailable() {
		return false;
	}
	

	@Override
	public String getUnavailableReason() {
		return "Requires in-system cryosleeper"; // unused since not shown when unavailable
	}

	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		if (isFunctional()) {
			incoming.add(Factions.SLEEPER, getImmigrationBonus() * 2f);
			incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), getNameForModifier());
			
			if (Commodities.ALPHA_CORE.equals(getAICoreId())) {
				incoming.getWeight().modifyFlat(getModId(1), (int)(getImmigrationBonus() * ALPHA_CORE_BONUS),
											"Alpha core (" + getNameForModifier() + ")");
			}
			if (isImproved()) {
				incoming.getWeight().modifyFlat(getModId(2), (int)(getImmigrationBonus() * IMPROVE_BONUS),
											getImprovementsDescForModifiers() + " (" + getNameForModifier() + ")");
			}
		}
	}
	
	
	@Override
	protected void applyAlphaCoreModifiers() {
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
	}
	
	@Override
	protected void applyAlphaCoreSupplyAndDemandModifiers() {
		demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
	}
	
	protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		String pre = "Alpha-level AI core currently assigned. ";
		if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			pre = "Alpha-level AI core. ";
		}
		float a = getImmigrationBonus() * ALPHA_CORE_BONUS;
		String str = "+" + (int)Math.round(a);
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"%s population growth.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
					str);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"%s population growth.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
				str);
		
	}
	
	
	@Override
	public boolean canImprove() {
		return true;
	}
	
	public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		float a = getImmigrationBonus() * IMPROVE_BONUS;
		String str = "" + (int)Math.round(a);
		
		if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
			info.addPara("Population growth increased by %s.", 0f, highlight,str);
		} else {
			info.addPara("Increases population growth by %s.", 0f, highlight,str);
		}

		info.addSpacer(opad);
		super.addImproveDesc(info, mode);
	}
	
	
	public static Pair<SectorEntityToken, Float> getNearestCryosleeper(Vector2f locInHyper, boolean usable) {
		SectorEntityToken nearest = null;
		float minDist = Float.MAX_VALUE;
		
		//for (SectorEntityToken entity : Global.getSector().getCustomEntitiesWithTag(Tags.CRYOSLEEPER)) {
		for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(CryosleeperIntel.class)) {
			CryosleeperIntel cryo = (CryosleeperIntel) intel;
			SectorEntityToken entity = cryo.getEntity();
			if (!usable || entity.getMemoryWithoutUpdate().contains("$usable")) {
				float dist = Misc.getDistanceLY(locInHyper, entity.getLocationInHyperspace());
				if (dist > MAX_BONUS_DIST_LY && Math.round(dist * 10f) <= MAX_BONUS_DIST_LY * 10f) {
					dist = MAX_BONUS_DIST_LY;
				}
				if (dist < minDist) {
					minDist = dist;
					nearest = entity;
				}
			}
		}
		
		if (nearest == null) return null;
		
		return new Pair<SectorEntityToken, Float>(nearest, minDist);
	}
	

	public static float getDistancePopulationMult(Vector2f locInHyper) {
		Pair<SectorEntityToken, Float> p = getNearestCryosleeper(locInHyper, true);
		if (p == null) return 0f;
		if (p.two > MAX_BONUS_DIST_LY) return 0f;
		
		
		float f = 1f - p.two / MAX_BONUS_DIST_LY;
		if (f < 0f) f = 0f;
		if (f > 1f) f = 1f;
		
		float mult = MIN_BONUS_MULT + (1f - MIN_BONUS_MULT) * f;
		
		return mult;
	}
	
	protected float getDemandMetPopulationMult() {
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.ORGANICS);
		float demand = getDemand(Commodities.ORGANICS).getQuantity().getModifiedValue();
		float def = deficit.two;
		if (def > demand) def = demand;
		
		float mult = 1f;
		if (def > 0 && demand > 0) {
			mult = (demand - def) / demand;
			mult *= MAX_BONUS_WHEN_UNMET_DEMAND;
		}
		return mult;
	}
	
	protected float getImmigrationBonus() {
		return getMaxImmigrationBonus() * getDemandMetPopulationMult() * getDistancePopulationMult(market.getLocationInHyperspace());
	}
	
	protected float getMaxImmigrationBonus() {
		return getSizeMult() * 10f;
	}
	
	
	public static class CryosleeperFactor implements ColonyOtherFactorsListener, PlanetFilter {
		public boolean isActiveFactorFor(SectorEntityToken entity) {
			return getNearestCryosleeper(entity.getLocationInHyperspace(), true) != null;
		}

		public void printOtherFactors(TooltipMakerAPI text, SectorEntityToken entity) {
			float distMult = getDistancePopulationMult(entity.getLocationInHyperspace());
			
			Pair<SectorEntityToken, Float> p = getNearestCryosleeper(entity.getLocationInHyperspace(), true);
			if (p != null) {
				Color h = Misc.getHighlightColor();
				float opad = 10f;
				
				String dStr = "" + Misc.getRoundedValueMaxOneAfterDecimal(p.two);
				String lights = "light-years";
				if (dStr.equals("1")) lights = "light-year";
				
				if (p.two > MAX_BONUS_DIST_LY) {
					text.addPara("The nearest cryosleeper is located in the " + 
							p.one.getContainingLocation().getNameWithLowercaseType() + ", %s " + lights + " away. The maximum " +
							"range at which sleepers can be safely brought over for revival is %s light-years.",
							opad, h,
							"" + Misc.getRoundedValueMaxOneAfterDecimal(p.two), 
							"" + (int)MAX_BONUS_DIST_LY);
				} else {
					text.addPara("The nearest cryosleeper is located in the " + 
							p.one.getContainingLocation().getNameWithLowercaseType() + ", %s " + lights + " away, allowing " +
									"a Cryorevival Facility built here to operate at %s effectiveness.",
							opad, h,
							"" + Misc.getRoundedValueMaxOneAfterDecimal(p.two), 
							"" + (int)Math.round(distMult * 100f) + "%");
				}
			}
		}
		
		public String getOtherFactorId() {
			return "cryosleeper";
		}
		
		public String getOtherFactorButtonText() {
			return "Domain-era Cryosleeper within range";
		}
		
		@Override
		public boolean accept(SectorEntityToken entity, Map<String, String> params) {
			if (!params.containsKey(getOtherFactorId())) return true;
			
			if (entity.getMarket() == null) return false;
			
			Pair<SectorEntityToken, Float> p = Cryorevival.getNearestCryosleeper(entity.getLocationInHyperspace(), false);
			if (p == null || p.two > Cryorevival.MAX_BONUS_DIST_LY) return false;
			return true;
		}

		@Override
		public boolean shouldShow() {
			return Global.getSector().getIntelManager().getIntelCount(CryosleeperIntel.class, true) > 0;
		}
		@Override
		public void createTooltip(TooltipMakerAPI info, float width, String param) {
			float opad = 10f;
			Color h = Misc.getHighlightColor();
			IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(Industries.CRYOREVIVAL);
			info.addTitle("Cryosleeper");
			info.addPara("Only show planets within %s light-years of a Domain-era Cryosleeper. Colonies "
					+ "within range can build a %s and benefit from hugely increased population growth.", 
					opad, h, "" + (int)Math.round(Cryorevival.MAX_BONUS_DIST_LY), spec.getName());
		}
	}
	
}



