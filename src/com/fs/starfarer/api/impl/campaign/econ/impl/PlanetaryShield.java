package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;



public class PlanetaryShield extends BaseIndustry {

	public static float DEFENSE_BONUS = 2f;
	
	public static float ALPHA_CORE_BONUS = 0.5f;
	public static float IMPROVE_DEFENSE_BONUS = 0.25f;
	
	public void apply() {
		super.apply(false);
		
		int size = 5;
		applyIncomeAndUpkeep(size);
		
		float bonus = DEFENSE_BONUS;
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
						.modifyMult(getModId(), 1f + bonus, getNameForModifier());
		
		if (isFunctional()) {
			applyVisuals(market.getPlanetEntity());
		} else {
			unapply();
		}
	}
	
	public static void applyVisuals(PlanetAPI planet) {
		if (planet == null) return;
		planet.getSpec().setShieldTexture(Global.getSettings().getSpriteName("industry", "shield_texture"));
		//planet.getSpec().setShieldThickness(0.07f);
		//planet.getSpec().setShieldColor(new Color(255,0,0,255));
		planet.getSpec().setShieldThickness(0.1f);
		planet.getSpec().setShieldColor(new Color(255,255,255,175));
		planet.applySpecChanges();
	}
	
	public static void unapplyVisuals(PlanetAPI planet) {
		if (planet == null) return;
		planet.getSpec().setShieldTexture(null);
		planet.getSpec().setShieldThickness(0f);
		planet.getSpec().setShieldColor(null);
		planet.applySpecChanges();
	}

	
	@Override
	public void unapply() {
		super.unapply();
		
		unapplyVisuals(market.getPlanetEntity());
		
		market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());
	}

	@Override
	public boolean isAvailableToBuild() {
		if (!Global.getSector().getPlayerFaction().knowsIndustry(getId())) {
			return false;
		}
		return market.getPlanetEntity() != null && !market.getPlanetEntity().isGasGiant();
	}
	
	@Override
	public String getUnavailableReason() {
		if (!super.isAvailableToBuild()) return super.getUnavailableReason();
		return "Can not be built at a gas giant";
	}
	
	public boolean showWhenUnavailable() {
		return Global.getSector().getPlayerFaction().knowsIndustry(getId());
	}

	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return mode != IndustryTooltipMode.NORMAL || isFunctional();
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			
			float bonus = DEFENSE_BONUS;
			addGroundDefensesImpactSection(tooltip, bonus, (String[])null);
		}
	}
	
	
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
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(2),
							1f + IMPROVE_DEFENSE_BONUS,
							getImprovementsDescForModifiers() + " (" + getNameForModifier() + ")");
		} else {
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId(3));
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
}
