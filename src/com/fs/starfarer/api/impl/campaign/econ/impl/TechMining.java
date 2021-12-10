package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.impl.items.BlueprintProviderItem;
import com.fs.starfarer.api.campaign.impl.items.ModSpecItemPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class TechMining extends BaseIndustry implements MarketImmigrationModifier {

	public static final String TECH_MINING_MULT = "$core_techMiningMult";
	
	public static float ALPHA_CORE_FINDS_BONUS = 0.25f;
	public static float IMPROVE_FINDS_BONUS = 0.25f;
	
	public void apply() {
		super.apply(false);
		
		int size = market.getSize();

		int max = 0;
		if (market.hasCondition(Conditions.RUINS_VAST)) {
			//max = 7;
			max = 4;
		} else if (market.hasCondition(Conditions.RUINS_EXTENSIVE)) {
			//max = 6;
			max = 3;
		} else if (market.hasCondition(Conditions.RUINS_WIDESPREAD)) {
			//max = 5;
			max = 2;
		} else if (market.hasCondition(Conditions.RUINS_SCATTERED)) {
			//max = 4;
			max = 1;
		}
		
		size = Math.min(size, max);
		
		applyIncomeAndUpkeep(size);
		
//		supply(Commodities.METALS, size + 2);
//		supply(Commodities.HEAVY_MACHINERY, size);
//		supply(Commodities.FUEL, size);
//		supply(Commodities.SUPPLIES, size);
		
//		supply(Commodities.HAND_WEAPONS, size);
//		supply(Commodities.RARE_METALS, size - 2);
//		supply(Commodities.VOLATILES, size - 2);

		if (!isFunctional()) {
			supply.clear();
		}
		
		market.addTransientImmigrationModifier(this);
	}

	
	@Override
	public void unapply() {
		market.removeTransientImmigrationModifier(this);
	}


	@Override
	public boolean isAvailableToBuild() {
		if (!super.isAvailableToBuild()) return false;
		if (market.hasCondition(Conditions.RUINS_VAST) ||
				market.hasCondition(Conditions.RUINS_EXTENSIVE) ||
				market.hasCondition(Conditions.RUINS_WIDESPREAD) ||
				market.hasCondition(Conditions.RUINS_SCATTERED)) {
			return true;
		}
		return false;
	}

	@Override
	public String getUnavailableReason() {
		if (!super.isAvailableToBuild()) return super.getUnavailableReason();
		return "Requires ruins";
	}

	
	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.TRITACHYON, 10f);
	}
	
	public float getPatherInterest() {
		float base = 1f;
		if (market.hasCondition(Conditions.RUINS_VAST)) {
			base = 8;
		} else if (market.hasCondition(Conditions.RUINS_EXTENSIVE)) {
			base = 6;
		} else if (market.hasCondition(Conditions.RUINS_WIDESPREAD)) {
			base = 4;
		} else if (market.hasCondition(Conditions.RUINS_SCATTERED)) {
			base = 2;
		}
		return base + super.getPatherInterest();
	}
	
	
	protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
		return true;
	}
	
	@Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		float opad = 10f;
		tooltip.addPara("In addition to extracting basic resources, " +
				"there's also a chance to find blueprints and other rare items. Anything " +
				"found will be delivered to the designated production gathering point.", opad);
		
		boolean hasRuins = true;
		if (market.hasCondition(Conditions.RUINS_VAST)) {
			tooltip.addPara("The vast ruins here offer incredible potential for valuable finds.", opad);
		} else if (market.hasCondition(Conditions.RUINS_EXTENSIVE)) {
			tooltip.addPara("The extensive ruins here offer the opportunity for valuable finds.", opad);
		} else if (market.hasCondition(Conditions.RUINS_WIDESPREAD)) {
			tooltip.addPara("The widespread ruins here offer a solid chance of finding something valuable, given time.", opad);
		} else if (market.hasCondition(Conditions.RUINS_SCATTERED)) {
			tooltip.addPara("The scattered ruins here offer only a slight chance of finding something valuable, though one never knows what might be located given enough time.", opad);
		} else {
			hasRuins = false;
		}
		
		if (hasRuins) {
			float mult = getTechMiningMult();
			if (mult >= .9f) {
				tooltip.addPara("These ruins are largely untapped.", opad);
			} else if (mult >= .5f) {
				tooltip.addPara("These ruins have been stripped of easy pickings, but the more difficult areas remain, filled with promise.", opad);
			} else if (mult >= 0.25f) {
				tooltip.addPara("These ruins have been combed through, though the chance for a few new finds still remains.", opad);
			} else {
				tooltip.addPara("These ruins have been comprehensively combed over multiple times, and there is little chance of a new valuable find.", opad);
			}
		}
		// add something re: size of ruins and chance to find stuff
		
	}

	protected float getEffectivenessMult() {
		float mult = market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).getModifiedValue();
		return mult;
	}
	
	public float getTechMiningMult() {
		
		MemoryAPI mem = market.getMemoryWithoutUpdate();
		if (mem.contains(TECH_MINING_MULT)) {
			return mem.getFloat(TECH_MINING_MULT);
		}
		mem.set(TECH_MINING_MULT, 1f);
		return 1f;
	}
	
	public void setTechMiningMult(float value) {
		MemoryAPI mem = market.getMemoryWithoutUpdate();
		mem.set(TECH_MINING_MULT, value);
	}
	
	public float getTechMiningRuinSizeModifier() {
		return getTechMiningRuinSizeModifier(market);
	}
	
	public static float getTechMiningRuinSizeModifier(MarketAPI market) {
		float mod = 0f;
		if (market.hasCondition(Conditions.RUINS_VAST)) {
			mod = 1;
		} else if (market.hasCondition(Conditions.RUINS_EXTENSIVE)) {
			mod = 0.6f;
		} else if (market.hasCondition(Conditions.RUINS_WIDESPREAD)) {
			mod = 0.35f;
		} else if (market.hasCondition(Conditions.RUINS_SCATTERED)) {
			mod = 0.2f;
		}
		return mod;
	}
	
	public CargoAPI generateCargoForGatheringPoint(Random random) {
		if (!isFunctional()) return null;
		
		float mult = getTechMiningMult();
		float decay = Global.getSettings().getFloat("techMiningDecay");
		float base = getTechMiningRuinSizeModifier();
		
		setTechMiningMult(mult * decay);
		
		base *= getEffectivenessMult();
		
		
		List<DropData> dropRandom = new ArrayList<DropData>();
		List<DropData> dropValue = new ArrayList<DropData>();
		
		DropData d = new DropData();
		d.chances = 1;
		d.group = "blueprints_low";
		//d.addCustom("item_:{tags:[single_bp], p:{tags:[rare_bp]}}", 1f);
		dropRandom.add(d);
		
		d = new DropData();
		d.chances = 1;
		d.group = "rare_tech_low";
		d.valueMult = 0.1f;
		dropRandom.add(d);
		
		d = new DropData();
		d.chances = 1;
		d.group = "ai_cores3";
		//d.valueMult = 0.1f; // already a high chance to get nothing due to group setup, so don't reduce further
		dropRandom.add(d);
		
		d = new DropData();
		d.chances = 1;
		d.group = "any_hullmod_low";
		dropRandom.add(d);
		
		d = new DropData();
		d.chances = 5;
		d.group = "weapons2";
		dropRandom.add(d);
		
		d = new DropData();
		//d.chances = 100;
		d.group = "basic";
		d.value = 10000;
		dropValue.add(d);
		
		if (mult >= 1) {
			float num = base * (5f + random.nextFloat() * 2f);
			if (num < 1) num = 1;
			
			d = new DropData();
			d.chances = (int) Math.round(num);
			d.group = "techmining_first_find";
			dropRandom.add(d);
		}
		
		CargoAPI result = SalvageEntity.generateSalvage(random, 1f, 1f, base * mult, 1f, dropValue, dropRandom);
		
		FactionAPI pf = Global.getSector().getPlayerFaction();
		OUTER: for (CargoStackAPI stack : result.getStacksCopy()) {
			if (stack.getPlugin() instanceof BlueprintProviderItem) {
				BlueprintProviderItem bp = (BlueprintProviderItem) stack.getPlugin();
				List<String> list = bp.getProvidedShips();
				if (list != null) {
					for (String id : list) {
						if (!pf.knowsShip(id)) continue OUTER;
					}
				}
				
				list = bp.getProvidedWeapons();
				if (list != null) {
					for (String id : list) {
						if (!pf.knowsWeapon(id)) continue OUTER;
					}
				}
				
				list = bp.getProvidedFighters();
				if (list != null) {
					for (String id : list) {
						if (!pf.knowsFighter(id)) continue OUTER;
					}
				}
				
				list = bp.getProvidedIndustries();
				if (list != null) {
					for (String id : list) {
						if (!pf.knowsIndustry(id)) continue OUTER;
					}
				}
				result.removeStack(stack);
			} else if (stack.getPlugin() instanceof ModSpecItemPlugin) {
				ModSpecItemPlugin mod = (ModSpecItemPlugin) stack.getPlugin();
				if (!pf.knowsHullMod(mod.getModId())) continue OUTER;
				result.removeStack(stack);
			}
		}
		
		//result.addMothballedShip(FleetMemberType.SHIP, "hermes_d_Hull", null);
		
		return result;
	}
	
	
	
	@Override
	protected void applyAlphaCoreModifiers() {
		market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).modifyMult(getModId(0), 1f + ALPHA_CORE_FINDS_BONUS);
	}
	
	@Override
	protected void applyNoAICoreModifiers() {
		market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).unmodifyMult(getModId(0));
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
		float a = ALPHA_CORE_FINDS_BONUS;
		String aStr = "" + (int)Math.round(a * 100f) + "%";
		
		if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
			CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
			TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
			text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
					"Increases finds by %s.", 0f, highlight,
					"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
					aStr);
			tooltip.addImageWithText(opad);
			return;
		}
		
		tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " +
				"Increases finds by %s.", opad, highlight,
				"" + (int)((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION,
				aStr);
		
	}
	
	@Override
	public boolean canImprove() {
		return true;
	}
	
	protected void applyImproveModifiers() {
		if (isImproved()) {
			market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).modifyMult(getModId(1), 1f + IMPROVE_FINDS_BONUS);
		} else {
			market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).unmodifyMult(getModId(1));
		}
	}
	
	public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
		float opad = 10f;
		Color highlight = Misc.getHighlightColor();
		
		float a = IMPROVE_FINDS_BONUS;
		String aStr = "" + (int)Math.round(a * 100f) + "%";
		
		if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
			info.addPara("Finds increased by %s.", 0f, highlight, aStr);
		} else {
			info.addPara("Increases finds by %s.", 0f, highlight, aStr);
		}

		info.addSpacer(opad);
		super.addImproveDesc(info, mode);
	}
}







