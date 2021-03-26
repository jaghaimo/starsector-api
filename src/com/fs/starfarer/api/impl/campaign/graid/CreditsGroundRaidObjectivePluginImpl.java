package com.fs.starfarer.api.impl.campaign.graid;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.IconGroupAPI;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CreditsGroundRaidObjectivePluginImpl extends BaseGroundRaidObjectivePluginImpl {

	public static float CREDITS_PER_MARKET_SIZE = 2500;
	
	public CreditsGroundRaidObjectivePluginImpl(MarketAPI market) {
		super(market, Commodities.CREDITS);
		setSource(null);
	}

	public void addIcons(IconGroupAPI iconGroup) {
		CommoditySpecAPI spec = getCommoditySpec();
		if (spec == null) return;
		
		int base = (int) Math.round(getBaseSizeValue());
		int minus = getNumMinusTokens();
		int plus = getNumPlusTokens();
		
		if (base > 0) {
			iconGroup.addIconGroup(id, IconRenderMode.NORMAL, base, null);
		}
		if (minus > 0) {
			iconGroup.addIconGroup(id, IconRenderMode.RED, minus, null);
		}
		if (plus > 0) {
			iconGroup.addIconGroup(id, IconRenderMode.GREEN, plus, null);
		}
	}
	
	@Override
	public String getSourceString() {
		return null;
		//return "Colony";
	}

	public int getProjectedCreditsValue() {
		return (int) getQuantity(getMarinesAssigned());
	}
	
	
	public CommoditySpecAPI getCommoditySpec() {
		return Global.getSettings().getCommoditySpec(getId());
	}
	
	public RaidDangerLevel getDangerLevel() {
		RaidDangerLevel danger = getCommoditySpec().getBaseDanger();
		
		float mult = getCreditsLootedMult();
		if (mult <= 0.75f) {
			danger = danger.next();
		} else if (mult >= 1.25f) {
			danger = danger.prev();
		}
		return danger;
	}

	public float getQuantitySortValue() {
		return QUANTITY_SORT_TIER_0 + 100000f;
	}
	
	@Override
	public String getQuantityString(int marines) {
		//return Misc.getWithDGS(getQuantity(Math.max(1, marines)));
		//return Misc.getDGSCredits(getQuantity(Math.max(1, marines)));
		return "";
	}
	
	public int getValue(int marines) {
		return (int) (getQuantity(marines) * getCommoditySpec().getBasePrice());
	}

	public float getQuantity(int marines) {
		//marines = Math.max(1, marines);
		float base = getBaseRaidQuantity();
		return base * marines;
	}
	
	public static float MAX_TOKENS = 5;
	public int getNumPlusTokens() {
		float incomeMult = market.getIncomeMult().getModifiedValue();
		int num = 0;
		if (incomeMult > 1f) num++;
		if (incomeMult > 1.25f) num++;
//		
//		float stability = market.getStabilityValue();
//		if (stability >= 8) num++;
//		if (stability >= 10) num++;
		
		float a = market.getAccessibilityMod().computeEffective(0f);
		num += Math.round((a - 1f) / 0.2f);
		num = Math.max(num, 0);
		num = (int) Math.min(num, MAX_TOKENS);
		return num;
		
	}
	
	public int getNumMinusTokens() {
		if (getNumPlusTokens() > 0) return 0;
		
		float incomeMult = market.getIncomeMult().getModifiedValue();
		int num = 0;
		if (incomeMult < 1f) num++;
		if (incomeMult < 0.75f) num++;
		
		float a = market.getAccessibilityMod().computeEffective(0f);
		num += Math.round((1f - a) / 0.1f);
		num = Math.max(num, 0);
		num = (int) Math.min(num, MAX_TOKENS);
		return num;
	}
	
	
	public float getCreditsLootedMult() {
		//float mult = market.getIncomeMult().getModifiedValue();
		//float base = getBaseSizeValue();
		float mult = 1f + (getNumPlusTokens() - getNumMinusTokens()) / (MAX_TOKENS + 1f);
		mult = Math.round(mult * 100f) / 100f;
		return mult;
	}

	public float getBaseSizeValue() {
		return Math.max(1, market.getSize() - 3);
	}
	
	public float getBaseRaidQuantity() {
		float result = getBaseSizeValue() * CREDITS_PER_MARKET_SIZE;
		result *= getCreditsLootedMult();
		return result;
	}

	public String getName() {
		return getCommoditySpec().getName();
	}

	public CargoStackAPI getStackForIcon() {
		CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.RESOURCES, getId(), null);
		return stack;
	}
	
	
	public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
		if (marinesAssigned <= 0) return 0;
		
		float base = getQuantity(marinesAssigned);
		base *= lootMult;
		
		float mult = 0.9f + random.nextFloat() * 0.2f;
		base *= mult;
		
		quantityLooted = (int) base;
		if (quantityLooted < 1) quantityLooted = 1;
		
		loot.getCredits().add(quantityLooted);
		
		xpGained = (int) (quantityLooted * getCommoditySpec().getBasePrice() * XP_GAIN_VALUE_MULT * 0.01f);
		return xpGained;
	}
	
	@Override
	public boolean hasTooltip() {
		return true;
	}
	
	@Override
	public void createTooltip(TooltipMakerAPI t, boolean expanded) {
		float opad = 10f;
		float pad = 3f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color good = Misc.getPositiveHighlightColor();
	}
}








