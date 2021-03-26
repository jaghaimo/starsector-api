package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

public class GroundDefenses extends BaseIndustry {

	public static float DEFENSE_BONUS_BASE = 1f;
	public static float DEFENSE_BONUS_BATTERIES = 2f;
	
	public static float IMPROVE_DEFENSE_BONUS = 0.25f;
	
	
	public void apply() {
		super.apply(true);
		
		int size = market.getSize();
		boolean hb = Industries.HEAVYBATTERIES.equals(getId());
		
		demand(Commodities.SUPPLIES, size);
		demand(Commodities.MARINES, size);
		demand(Commodities.HAND_WEAPONS, size - 2);
		
		//supply(Commodities.MARINES, size - 2);
		
//		Pair<String, Integer> deficit = getMaxDeficit(Commodities.HAND_WEAPONS);
//		applyDeficitToProduction(1, deficit, Commodities.MARINES);
		
		modifyStabilityWithBaseMod();
		
		float mult = getDeficitMult(Commodities.SUPPLIES, Commodities.MARINES, Commodities.HAND_WEAPONS);
		String extra = "";
		if (mult != 1) {
			String com = getMaxDeficit(Commodities.SUPPLIES, Commodities.MARINES, Commodities.HAND_WEAPONS).one;
			extra = " (" + getDeficitText(com).toLowerCase() + ")";
		}
		float bonus = hb ? DEFENSE_BONUS_BATTERIES : DEFENSE_BONUS_BASE;
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
						.modifyMult(getModId(), 1f + bonus * mult, getNameForModifier() + extra);
//		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
//		.modifyPercent(getModId(), bonus * mult * 100f, getNameForModifier() + extra);
		
		
		if (!isFunctional()) {
			supply.clear();
			unapply();
		}
		
	}

	@Override
	public void unapply() {
		super.unapply();
		
		unmodifyStabilityWithBaseMod();
		
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
		//market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyPercent(getModId());
		//market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyFlat(getModId());
	}

	@Override
	public String getCurrentImage() {
		boolean batteries = Industries.HEAVYBATTERIES.equals(getId());
		if (batteries) {
			PlanetAPI planet = market.getPlanetEntity();
			if (planet == null || planet.isGasGiant()) {
				return Global.getSettings().getSpriteName("industry", "heavy_batteries_orbital");
			}
		}
		return super.getCurrentImage();
	}

	public boolean isDemandLegal(CommodityOnMarketAPI com) {
		return true;
	}

	public boolean isSupplyLegal(CommodityOnMarketAPI com) {
		return true;
	}
	
	
	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		//return mode == IndustryTooltipMode.NORMAL && isFunctional();
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		//if (mode == IndustryTooltipMode.NORMAL && isFunctional()) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			addStabilityPostDemandSection(tooltip, hasDemand, mode);
			
			boolean hb = Industries.HEAVYBATTERIES.equals(getId());
			float bonus = hb ? DEFENSE_BONUS_BATTERIES : DEFENSE_BONUS_BASE;
			addGroundDefensesImpactSection(tooltip, bonus, Commodities.SUPPLIES, Commodities.MARINES, Commodities.HAND_WEAPONS);
		}
	}
	
	@Override
	protected int getBaseStabilityMod() {
		return 1;
	}
	
	@Override
	protected Pair<String, Integer> getStabilityAffectingDeficit() {
		return getMaxDeficit(Commodities.SUPPLIES, Commodities.MARINES, Commodities.HAND_WEAPONS);
	}
	
	
	public static float ALPHA_CORE_BONUS = 0.5f;
	@Override
	protected void applyAlphaCoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(
				getModId(1), 1f + ALPHA_CORE_BONUS, "Alpha core (" + getNameForModifier() + ")");
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId(1));
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
		float a = ALPHA_CORE_BONUS;
		//String str = Strings.X + (int)Math.round(a * 100f) + "%";
		String str = Strings.X + (1f + a) + "";
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"Increases ground defenses by %s.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
					str);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"Increases ground defenses by %s.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
				str);
		
	}
	
	
	@Override
	public boolean canImprove() {
		return true;
	}
	
	protected void applyImproveModifiers() {
		if (isImproved()) {
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult("ground_defenses_improve",
							1f + IMPROVE_DEFENSE_BONUS,
							getImprovementsDescForModifiers() + " (" + getNameForModifier() + ")");
		} else {
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult("ground_defenses_improve");
		}
	}
	
	public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		float a = IMPROVE_DEFENSE_BONUS;
		String str = Strings.X + (1f + a) + "";
		
		if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
			info.addPara("Ground defenses increased by %s.", 0f, highlight, str);
		} else {
			info.addPara("Increases ground defenses by %s.", 0f, highlight, str);
		}

		info.addSpacer(opad);
		super.addImproveDesc(info, mode);
	}
	
	@Override
	public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
		return level.next();
	}

	@Override
	public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
		return level.next();
	}

}




