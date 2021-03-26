package com.fs.starfarer.api.impl.campaign.graid;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class AICoreGroundRaidObjectivePluginImpl extends BaseGroundRaidObjectivePluginImpl {

	public AICoreGroundRaidObjectivePluginImpl(MarketAPI market, String id, Industry source) {
		super(market, id);
		setSource(source);
	}
	
	@Override
	public void setSource(Industry source) {
		super.setSource(source);
		RaidDangerLevel level = getDangerLevel();
		int marines = level.marineTokens;
		if (source != null) {
			marines = source.adjustMarineTokensToRaidItem(id, null, marines); 
		}
		setMarinesRequired(marines);
	}

	public float getQuantity(int marines) {
		return 1;
	}
	
	public int getValue(int marines) {
		return (int) (getQuantity(marines) * getCommoditySpec().getBasePrice());
	}
	
	public int getCargoSpaceNeeded() {
		return (int) getCommoditySpec().getCargoSpace();
	}
	
	public int getFuelSpaceNeeded() {
		return 0;
	}
	
	public int getProjectedCreditsValue() {
		return (int) getCommoditySpec().getBasePrice();
	}
	
	public CommoditySpecAPI getCommoditySpec() {
		return Global.getSettings().getCommoditySpec(id);
	}
	
	public RaidDangerLevel getDangerLevel() {
		RaidDangerLevel level = getCommoditySpec().getBaseDanger();
		if (source != null) {
			level = source.adjustItemDangerLevel(id, null, level);
		}
		return level;
	}

	public float getQuantitySortValue() {
		CommoditySpecAPI spec = getCommoditySpec();
		float add = 0;
		if (spec != null) {
			add = spec.getOrder();
		}
		return QUANTITY_SORT_TIER_2 + add; 
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
		
		if (source != null) {
			if (getId() == null || getId().equals(source.getAICoreId())) {
				source.setAICoreId(null);
			}
		}
		loot.addCommodity(getId(), 1f);
		
		int xpGained = (int) (1 * getCommoditySpec().getBasePrice() * XP_GAIN_VALUE_MULT);
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

		Description desc = Global.getSettings().getDescription(id, Type.RESOURCE);
		
		t.addPara(desc.getText1FirstPara(), 0f);
		
		t.addPara("Base value: %s per unit", opad, h, Misc.getDGSCredits(getCommoditySpec().getBasePrice()));
	}

}


