package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class TradeCenter extends BaseIndustry implements MarketImmigrationModifier {

	public static float BASE_BONUS = 25f;
	public static float ALPHA_CORE_BONUS = 25f;
	public static float IMPROVE_BONUS = 25f;
	
	public static float STABILITY_PELANTY = 3f;
	
	//protected transient CargoAPI savedCargo = null;
	protected transient SubmarketAPI saved = null;
	
	public void apply() {
		super.apply(true);

		if (isFunctional() && market.isPlayerOwned()) {
			SubmarketAPI open = market.getSubmarket(Submarkets.SUBMARKET_OPEN);
			if (open == null) {
				if (saved != null) {
					market.addSubmarket(saved);
				} else {
					market.addSubmarket(Submarkets.SUBMARKET_OPEN);
					SubmarketAPI sub = market.getSubmarket(Submarkets.SUBMARKET_OPEN);
					sub.setFaction(Global.getSector().getFaction(Factions.INDEPENDENT));
					Global.getSector().getEconomy().forceStockpileUpdate(market);
				}
				
//				if (savedCargo != null) {
//					open = market.getSubmarket(Submarkets.SUBMARKET_OPEN);
//					if (open != null) {
//						open.getCargo().clear();
//						open.getCargo().addAll(savedCargo);
//						if (open.getPlugin() instanceof BaseSubmarketPlugin) {
//							BaseSubmarketPlugin base = (BaseSubmarketPlugin) open.getPlugin();
//							base.setSinceLastCargoUpdate(0);
//							base.setSinceSWUpdate(0);
//						}
//					}
//				}
			}
		} else if (market.isPlayerOwned()) {
			market.removeSubmarket(Submarkets.SUBMARKET_OPEN);
		}
		
		//modifyStabilityWithBaseMod();
		market.getStability().modifyFlat(getModId(), -STABILITY_PELANTY, getNameForModifier());
		
		market.getIncomeMult().modifyPercent(getModId(0), BASE_BONUS, getNameForModifier());
		
		if (!isFunctional()) {
			unapply();
		}
	}

	
	@Override
	public void unapply() {
		super.unapply();
		
		if (market.isPlayerOwned()) {
			SubmarketAPI open = market.getSubmarket(Submarkets.SUBMARKET_OPEN);
			saved = open;
//			if (open.getPlugin() instanceof BaseSubmarketPlugin) {
//				BaseSubmarketPlugin base = (BaseSubmarketPlugin) open.getPlugin();
//				if (base.getSinceLastCargoUpdate() < 30) {
//					savedCargo = open.getCargo();
//				}
//			}
			market.removeSubmarket(Submarkets.SUBMARKET_OPEN);
		}
		
		market.getStability().unmodifyFlat(getModId());
		
		market.getIncomeMult().unmodifyPercent(getModId(0));
	}
	
	protected void addStabilityPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		Color h = Misc.getHighlightColor();
		float opad = 10f;
		
		float a = BASE_BONUS;
		String aStr = "+" + (int)Math.round(a * 1f) + "%";
		tooltip.addPara("Colony income: %s", opad, h, aStr);
		
		h = Misc.getNegativeHighlightColor();
		tooltip.addPara("Stability penalty: %s", opad, h, "" + -(int)STABILITY_PELANTY);
	}
	
	@Override
	protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
		if (market.isPlayerOwned() || currTooltipMode == IndustryTooltipMode.ADD_INDUSTRY) {
			tooltip.addPara("Adds an independent \'Open Market\' that the colony's owner is able to trade with. "
					+ "Depending on the level of hostile activity, a low-level semi-permanent bounty may be posted as well.", 10f);
		}
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			addStabilityPostDemandSection(tooltip, hasDemand, mode);
		}
	}
	
	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.TRITACHYON, 10f);
	}
	
	
	public boolean isAvailableToBuild() {
		return market.hasSpaceport();
	}
	
	public String getUnavailableReason() {
		return "Requires a functional spaceport";
	}
	
	@Override
	public String getCurrentImage() {
		float size = market.getSize();
		if (size <= SIZE_FOR_SMALL_IMAGE) {
			return Global.getSettings().getSpriteName("industry", "commerce_low");
		}
		if (size >= SIZE_FOR_LARGE_IMAGE) {
			return Global.getSettings().getSpriteName("industry", "commerce_high");
		}
		
		return super.getCurrentImage();
	}
	
	
	//market.getIncomeMult().modifyMult(id, INCOME_MULT, "Industrial planning");
	@Override
	protected void applyAlphaCoreModifiers() {
		market.getIncomeMult().modifyPercent(getModId(1), ALPHA_CORE_BONUS, "Alpha core (" + getNameForModifier() + ")");
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
		market.getIncomeMult().unmodifyPercent(getModId(1));
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
		String str = "" + (int) Math.round(a) + "%";
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"Increases colony income by %s.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
					str);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"Increases colony income by %s.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
				str);
		
	}
	
	
	@Override
	public boolean canImprove() {
		return true;
	}
	
	protected void applyImproveModifiers() {
		if (isImproved()) {
			market.getIncomeMult().modifyPercent(getModId(2), IMPROVE_BONUS,
							getImprovementsDescForModifiers() + " (" + getNameForModifier() + ")");
		} else {
			market.getIncomeMult().unmodifyPercent(getModId(2));
		}
	}
	
	public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		float a = IMPROVE_BONUS;
		String aStr = "" + (int)Math.round(a * 1f) + "%";
		
		if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
			info.addPara("Colony income increased by %s.", 0f, highlight, aStr);
		} else {
			info.addPara("Increases colony income by %s.", 0f, highlight, aStr);
		}

		info.addSpacer(opad);
		super.addImproveDesc(info, mode);
	}
}




