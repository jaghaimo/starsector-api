package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;



public class Spaceport extends BaseIndustry implements MarketImmigrationModifier {

	public static float UPKEEP_MULT_PER_DEFICIT = 0.1f;
	
	public static final float BASE_ACCESSIBILITY = 0.5f;
	public static final float MEGAPORT_ACCESSIBILITY = 0.8f;
	
	public static final float ALPHA_CORE_ACCESSIBILITY = 0.2f;
//	public static final float BASE_ACCESSIBILITY = 0f;
//	public static final float MEGAPORT_ACCESSIBILITY = 0.2f;
	
	
	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		
		boolean megaport = Industries.MEGAPORT.equals(getId());
		int extraSize = 0;
		if (megaport) extraSize = 2;
		
		demand(Commodities.FUEL, size - 2 + extraSize);
		demand(Commodities.SUPPLIES, size - 2 + extraSize);
		demand(Commodities.SHIPS, size - 2 + extraSize);
		
		supply(Commodities.CREW, size - 1 + extraSize);
		
		
		String desc = getNameForModifier();
		
		Pair<String, Integer> deficit = getUpkeepAffectingDeficit();
		
		if (deficit.two > 0) {
			float loss = getUpkeepPenalty(deficit);
			getUpkeep().modifyMult("deficit", 1f + loss, getDeficitText(deficit.one));
		} else {
			getUpkeep().unmodifyMult("deficit");
		}
		
		market.setHasSpaceport(true);
		
		float a = BASE_ACCESSIBILITY;
		if (megaport) {
			a = MEGAPORT_ACCESSIBILITY;
		}
		if (a > 0) {
			market.getAccessibilityMod().modifyFlat(getModId(0), a, desc);
		}
		
		if (!isFunctional()) {
//			if (isDisrupted() && !isBuilding()) {
//				market.getAccessibilityMod().modifyFlat(getModId(2), -1f, "Spaceport operations disrupted");
//				supply(Commodities.CREW, size - 1 + extraSize);
//			} else {
				supply.clear();
				unapply();
//			}
		}
	}

	@Override
	public void unapply() {
		super.unapply();
		
		market.setHasSpaceport(false);
		market.getAccessibilityMod().unmodifyFlat(getModId(0));
		market.getAccessibilityMod().unmodifyFlat(getModId(1));
		market.getAccessibilityMod().unmodifyFlat(getModId(2));
	}
	
	protected float getUpkeepPenalty(Pair<String, Integer> deficit) {
		float loss = deficit.two * UPKEEP_MULT_PER_DEFICIT;
		if (loss < 0) loss = 0;
		return loss;
	}
	
	protected Pair<String, Integer> getUpkeepAffectingDeficit() {
		return getMaxDeficit(Commodities.FUEL, Commodities.SUPPLIES, Commodities.SHIPS);
	}
	
	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		//return mode == IndustryTooltipMode.NORMAL && isFunctional();
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		//if (mode == IndustryTooltipMode.NORMAL && isFunctional()) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {			
			MutableStat fake = new MutableStat(0);
			
			boolean megaport = Industries.MEGAPORT.equals(getId());
			String desc = getNameForModifier();
			float a = BASE_ACCESSIBILITY;
			if (megaport) {
				a = MEGAPORT_ACCESSIBILITY;
			}
			if (a > 0) {
				fake.modifyFlat(getModId(0), a, desc);
			}
			float total = a;
//			Pair<String, Integer> deficit = getAccessibilityAffectingDeficit();
//			float loss = getAccessibilityPenalty(deficit);
//			if (deficit.two > 0) {
//				fake.modifyFlat(getModId(1), -loss, getDeficitText(deficit.one));
//			}
//			
//			float total = a - loss; 
			String totalStr = "+" + (int)Math.round(total * 100f) + "%";
			Color h = Misc.getHighlightColor();
			if (total < 0) {
				h = Misc.getNegativeHighlightColor();
				totalStr = "" + (int)Math.round(total * 100f) + "%";
			}
			float opad = 10f;
			float pad = 3f;
			if (total >= 0) {
				tooltip.addPara("Accessibility bonus: %s", opad, h, totalStr);
			} else {
				tooltip.addPara("Accessibility penalty: %s", opad, h, totalStr);
			}
			
			float bonus = getPopulationGrowthBonus();
			tooltip.addPara("Population growth: %s", opad, h, "+" + (int)bonus);
//			tooltip.addStatModGrid(400, 50, opad, pad, fake, new StatModValueGetter() {
//				public String getPercentValue(StatMod mod) {
//					return null;
//				}
//				public String getMultValue(StatMod mod) {
//					return null;
//				}
//				public Color getModColor(StatMod mod) {
//					if (mod.value < 0) return Misc.getNegativeHighlightColor();
//					return null;
//				}
//				public String getFlatValue(StatMod mod) {
//					String prefix = mod.value >= 0 ? "+" : "";
//					return prefix + (int)Math.round(mod.value * 100f) + "%";
//				}
//			});
			
		}
	}

	
	public float getPopulationGrowthBonus() {
		boolean megaport = Industries.MEGAPORT.equals(getId());
		float bonus = 2;
		if (megaport) {
			bonus = market.getSize();
		}
		return bonus;
	}
	
	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		float bonus = getPopulationGrowthBonus();
		
		incoming.getWeight().modifyFlat(getModId(), bonus, getNameForModifier());
	}
	
	
	@Override
	protected void applyAlphaCoreModifiers() {
		market.getAccessibilityMod().modifyFlat(getModId(2), ALPHA_CORE_ACCESSIBILITY, "Alpha core (" + getNameForModifier() + ")");
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
		market.getAccessibilityMod().unmodifyFlat(getModId(2));
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
		float a = ALPHA_CORE_ACCESSIBILITY;
		String aStr = "" + (int)Math.round(a * 100f) + "%";
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"Increases accessibility by %s.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
					aStr);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"Increases accessibility by %s.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
				aStr);
		
	}
}




