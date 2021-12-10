package com.fs.starfarer.api.impl.campaign.graid;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.CommodityIconCounts;
import com.fs.starfarer.api.impl.campaign.econ.ShippingDisruption;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.IconGroupAPI;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CommodityGroundRaidObjectivePluginImpl extends BaseGroundRaidObjectivePluginImpl {
	// for causing deficit; higher value means less units need to be raided to cause same deficit	
	public static float ECON_IMPACT_MULT = 1f;
	
	public static float QUANTITY_MULT_NORMAL = 0.1f; 
	//public static float QUANTITY_MULT_NORMAL_FOR_DEFICIT = 0.5f; 
	public static float QUANTITY_MULT_NORMAL_FOR_DEFICIT = 1f; 
	public static float QUANTITY_MULT_EXCESS = 1f; 
	public static float QUANTITY_MULT_DEFICIT = -0.1f; 
	public static float QUANTITY_MULT_OVERALL = 0.1f;
	
	protected CommodityOnMarketAPI com;
	private int deficitActuallyCaused;
	
	public CommodityGroundRaidObjectivePluginImpl(MarketAPI market, String commodityId) {
		super(market, commodityId);
		com = market.getCommodityData(commodityId);
		setSource(computeCommoditySource(market, com));
	}

	public void addIcons(IconGroupAPI iconGroup) {
		CommoditySpecAPI spec = getCommoditySpec();
		if (spec == null) return;
		
		CommodityOnMarketAPI com = market.getCommodityData(id);
		CommodityIconCounts counts = new CommodityIconCounts(com);
		
		int deficit = counts.deficit;
		int available = Math.max(0, counts.available - counts.extra);
		int extra = counts.extra;
		
		if (available > 0) {
			if (counts.production >= counts.available) {
				Color c = Misc.interpolateColor(Misc.getPositiveHighlightColor(), Misc.zeroColor, 0.5f);
				iconGroup.addIconGroup(id, IconRenderMode.OUTLINE_CUSTOM, available, c);
			} else {
				iconGroup.addIconGroup(id, IconRenderMode.NORMAL, available, null);
			}
		}
		if (deficit > 0) {
			iconGroup.addIconGroup(id, IconRenderMode.RED, deficit, null);
		}
		if (extra > 0) {
			iconGroup.addIconGroup(id, IconRenderMode.GREEN, extra, null);
		}
	}

	public int getCargoSpaceNeeded() {
		CommoditySpecAPI spec = getCommoditySpec();
		if (!spec.isFuel() && !spec.isPersonnel()) {
			return (int) (spec.getCargoSpace() * getQuantity(getMarinesAssigned()));
		}
		return 0;
	}
	
	public int getFuelSpaceNeeded() {
		CommoditySpecAPI spec = getCommoditySpec();
		if (spec.isFuel()) {
			return (int) (spec.getCargoSpace() * getQuantity(getMarinesAssigned()));
		}
		return 0;
	}
	
	public int getProjectedCreditsValue() {
		CommoditySpecAPI spec = getCommoditySpec();
		return (int) (spec.getBasePrice() * getQuantity(getMarinesAssigned()));
	}
	
	public int getDeficitCaused() {
		float quantity = getQuantity(getMarinesAssigned(), true);
		quantity *= ECON_IMPACT_MULT;
		int diff = Misc.computeEconUnitChangeFromTradeModChange(com, -(int)quantity);
		diff = -diff;
		if (diff < 0) diff = 0;
		if (diff == 0 && getProjectedCreditsValue() > 1000) diff = 1;
		return diff;
	}
	
	public CommoditySpecAPI getCommoditySpec() {
		return com.getCommodity();
	}
	
	public RaidDangerLevel getDangerLevel() {
//		if (id.equals(Commodities.HAND_WEAPONS)) {
//			System.out.println("wefwefwe");
//		}
		RaidDangerLevel danger = com.getCommodity().getBaseDanger();
		CommodityIconCounts counts = new CommodityIconCounts(com);
		if (counts.production >= counts.available) {
			danger = danger.prev();
		}
		if (counts.deficit > 0) {
			danger = danger.next();
		}
		if (counts.extra > 0) {
			danger = danger.prev();
		}
		if (source != null) {
			danger = source.adjustCommodityDangerLevel(id, danger);
		}
		return danger;
	}

	public float getQuantitySortValue() {
		return QUANTITY_SORT_TIER_0 + getQuantity(1);
	}
	
	public float getQuantity(int marines) {
		return getQuantity(marines, false);
	}
	
	public float getQuantity(int marines, boolean forDeficit) {
		float base = getBaseRaidQuantity(forDeficit);
		return base * marines;
	}
	
	public int getValue(int marines) {
		return (int) (getQuantity(marines) * getCommoditySpec().getBasePrice());
	}
	
	
	public float getBaseRaidQuantity(boolean forDeficit) {
		//CommodityOnMarketAPI com = market.getCommodityData(id);
		float unit = com.getCommodity().getEconUnit();
		
		CommodityIconCounts counts = new CommodityIconCounts(com);
		
		float result = 0f;
		
		if (forDeficit) {
			result += Math.max(0, counts.available - counts.extra) * unit * QUANTITY_MULT_NORMAL_FOR_DEFICIT;
		} else {
			result += Math.max(0, counts.available - counts.extra) * unit * QUANTITY_MULT_NORMAL;
		}
		result += counts.extra * unit * QUANTITY_MULT_EXCESS;
		result += counts.deficit * unit * QUANTITY_MULT_DEFICIT;
		
		result *= QUANTITY_MULT_OVERALL;
		
		if (result < 0) result = 0;
		
		return result;
	}

	public static Industry computeCommoditySource(MarketAPI market, CommodityOnMarketAPI com) {
		Industry best = null;
		int score = 0;
		int available = com.getAvailable();
		RaidDangerLevel base = com.getCommodity().getBaseDanger();
		for (Industry ind : market.getIndustries()) {
			int supply = ind.getSupply(com.getId()).getQuantity().getModifiedInt();
			int metDemand = Math.min(available, ind.getDemand(com.getId()).getQuantity().getModifiedInt());
			int currScore = Math.max(supply, metDemand) * 1000;
			RaidDangerLevel danger = ind.adjustCommodityDangerLevel(com.getId(), base);
			currScore += 1000 - danger.ordinal();
			if (currScore > score) {
				score = currScore;
				best = ind;
			}
		}
		return best;
	}

	public String getName() {
		return com.getCommodity().getName();
	}

	public CargoStackAPI getStackForIcon() {
		CargoStackAPI stack = Global.getFactory().createCargoStack(CargoItemType.RESOURCES, getId(), null);
		return stack;
	}
	
	public String getCommodityIdForDeficitIcons() {
		return com.getId();
	}

	
	public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
		if (marinesAssigned <= 0) return 0;
		
		float base = getQuantity(marinesAssigned);
		base *= lootMult;
		
		float mult = 0.9f + random.nextFloat() * 0.2f;
		base *= mult;
		
		quantityLooted = (int) base;
		if (quantityLooted < 1) quantityLooted = 1;
		
		loot.addCommodity(getId(), quantityLooted);
		
		deficitActuallyCaused = getDeficitCaused();
		if (deficitActuallyCaused > 0) {
			com.getAvailableStat().addTemporaryModFlat(
					ShippingDisruption.ACCESS_LOSS_DURATION,
					Misc.genUID(), "Recent raid", -deficitActuallyCaused);
		}
		
		xpGained = (int) (quantityLooted * getCommoditySpec().getBasePrice() * XP_GAIN_VALUE_MULT);
		return xpGained;
	}

	public int getDeficitActuallyCaused() {
		return deficitActuallyCaused;
	}

	public void setDeficitActuallyCaused(int deficitActuallyCaused) {
		this.deficitActuallyCaused = deficitActuallyCaused;
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
		
		t.addPara("Base value: %s per unit", opad, h, Misc.getDGSCredits(com.getCommodity().getBasePrice()));
	}
	
}








