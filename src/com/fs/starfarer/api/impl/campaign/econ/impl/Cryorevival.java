package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public class Cryorevival extends BaseIndustry implements MarketImmigrationModifier {

	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		
		demand(Commodities.HEAVY_MACHINERY, size);
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
			
			tooltip.addPara("Population growth: %s (max for colony size: %s)", opad, h, "+" + Math.round(bonus), "+" + Math.round(max));
		}
	}
	
	
	@Override
	public boolean isAvailableToBuild() {
		//if (Global.getSettings().isDevMode()) return true;
		
		StarSystemAPI system = market.getStarSystem();
		if (system == null) return false;
		for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.CRYOSLEEPER)) {
			if (entity.getMemoryWithoutUpdate().contains("$usable")) {
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean showWhenUnavailable() {
		return false;
	}
	

	@Override
	public String getUnavailableReason() {
		return "Requires in-system cryosleeper";
	}

	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		if (isFunctional()) {
			incoming.add(Factions.SLEEPER, getImmigrationBonus() * 2f);
			incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), getNameForModifier());
			
			if (Commodities.ALPHA_CORE.equals(getAICoreId())) {
				incoming.getWeight().modifyFlat(getModId(1), getImmigrationBonus() * ALPHA_CORE_BONUS, "Alpha core (" + getNameForModifier() + ")");
			}
		}
	}
	
	protected float getImmigrationBonus() {
		Pair<String, Integer> deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY);
		float demand = getDemand(Commodities.HEAVY_MACHINERY).getQuantity().getModifiedValue();
		float def = deficit.two;
		if (def > demand) def = demand;
		
		float mult = 1f;
		if (def > 0 && demand > 0) {
			mult = (demand - def) / demand;
		}
		
		return getMaxImmigrationBonus() * mult;
	}
	
	protected float getMaxImmigrationBonus() {
		//return market.getSize() * 10f;
		return getSizeMult() * 10f;
	}
	
	
	public static float ALPHA_CORE_BONUS = 1f;
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
		String str = "+" + Math.round(a);
		
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
}



