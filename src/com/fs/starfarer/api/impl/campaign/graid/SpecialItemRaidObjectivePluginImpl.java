package com.fs.starfarer.api.impl.campaign.graid;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SpecialItemRaidObjectivePluginImpl extends BaseGroundRaidObjectivePluginImpl {

	protected String data;
	
	public SpecialItemRaidObjectivePluginImpl(MarketAPI market, String id, String data, Industry source) {
		super(market, id);
		this.data = data;
		setSource(source);
	}
	
	@Override
	public void setSource(Industry source) {
		super.setSource(source);
		RaidDangerLevel level = getDangerLevel();
		int marines = level.marineTokens;
		if (source != null) {
			marines = source.adjustMarineTokensToRaidItem(id, data, marines); 
		}
		setMarinesRequired(marines);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public float getQuantity(int marines) {
		return 1;
	}
	
	public int getValue(int marines) {
		return (int) (getQuantity(marines) * getItemSpec().getBasePrice());
	}
	
	public int getCargoSpaceNeeded() {
		return (int) getItemSpec().getCargoSpace();
	}
	
	public int getFuelSpaceNeeded() {
		return 0;
	}
	
	public int getProjectedCreditsValue() {
		return (int) getItemSpec().getBasePrice();
	}
	
	public int getDeficitCaused() {
		return 0;
	}
	
	public SpecialItemSpecAPI getItemSpec() {
		return Global.getSettings().getSpecialItemSpec(id);
	}
	
	public RaidDangerLevel getDangerLevel() {
		RaidDangerLevel level = getItemSpec().getBaseDanger();
		if (source != null) {
			level = source.adjustItemDangerLevel(id, null, level);
		}
		return level;
	}

	public float getQuantitySortValue() {
		SpecialItemSpecAPI spec = getItemSpec();
		float add = 0;
		if (spec != null) {
			add = spec.getOrder();
		}
		return QUANTITY_SORT_TIER_2 + add + 1000;
	}
	
	public String getName() {
		return getItemSpec().getName();
	}

	public CargoStackAPI getStackForIcon() {
		CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.SPECIAL,
										new SpecialItemData(getId(), getData()), null);
		return stack;
	}
	
	public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
		if (marinesAssigned <= 0) return 0;
		
		if (source != null) {
			SpecialItemData sid = source.getSpecialItem();
			if ((getId() == null || getId().equals(sid.getId())) &&
					(getData() == null || getData().equals(sid.getData()))) {
				source.setSpecialItem(null);
			}
		}
		loot.addSpecial(new SpecialItemData(getId(), getData()), 1);
		
		int xpGained = (int) (1 * getItemSpec().getBasePrice() * XP_GAIN_VALUE_MULT);
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

		//Description desc = Global.getSettings().getDescription(id, Type.RESOURCE);
		
		t.addPara(getItemSpec().getDescFirstPara(), 0f);
		
		t.addPara("Base value: %s per unit", opad, h, Misc.getDGSCredits(getItemSpec().getBasePrice()));
	}
}




