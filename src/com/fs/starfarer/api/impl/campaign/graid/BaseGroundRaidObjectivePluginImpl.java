package com.fs.starfarer.api.impl.campaign.graid;

import java.awt.Color;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.IconGroupAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public abstract class BaseGroundRaidObjectivePluginImpl implements GroundRaidObjectivePlugin {

//	protected String commodityId;
//	protected String nameOverride;
//	protected CargoItemType type = CargoItemType.RESOURCES;
//	protected boolean isSpecificItem;
	
	protected String id;
	protected MarketAPI market;
	protected Industry source;
	protected int marinesAssigned;
	protected int marinesRequired;
	protected String nameOverride;
	protected String assignedForcesColumnText;
	protected Color assignedForcesColumnColor = Misc.getNegativeHighlightColor();
	
	protected int quantityLooted;
	protected int xpGained;
	
	public BaseGroundRaidObjectivePluginImpl(MarketAPI market, String id) {
		this.market = market;
		this.id = id;
	}
	
	public abstract float getQuantity(int marines);
	public abstract int getValue(int marines);	
	
	public int getCargoSpaceNeeded() { return 0; }
	public int getFuelSpaceNeeded() { return 0; }
	
	public CargoStackAPI getStackForIcon() {
		return null;
	}
	
	public String getIconName() {
		return null;
	}
	
	public void addIcons(IconGroupAPI iconGroup) {
		
	}
	public int getDeficitCaused() {
		return 0;
	}

	public String getDisruptedAlreadyString() {
		return "";
	}
	public Color getDisruptedAlreadyColor() {
		return Misc.getHighlightColor();
	}
	
	public int getDisruptionDaysSort(int marines) {
		return 0;
	}
	
	public String getDisruptionDaysString(int marines) {
		return "";
	}
	public Color getDisruptionDaysColor(int marines) {
		if (marines <= 0) {
			return Misc.getGrayColor();
		}
		return Misc.getHighlightColor();
	}
	
	
//	public int getQuantitySortValue() {
//		CommoditySpecAPI spec = getCommoditySpec();
//		SpecialItemSpecAPI item = getItemSpec();
//		if (isSpecificItem()) {
//			int add = 0;
//			if (spec != null) {
//				add = (int) spec.getOrder();
//			} else if (item != null) {
//				add = (int) item.getOrder() + 1000;
//			}
//			return 1000000 + add; 
//		}
//		return (int) getQuantity(0);
//	}	
	
//	public CommoditySpecAPI getCommoditySpec() {
//		return Global.getSettings().getCommoditySpec(commodityId);
//	}
//	
//	public SpecialItemSpecAPI getItemSpec() {
//		return Global.getSettings().getSpecialItemSpec(commodityId);
//	}

	public String getNameOverride() {
		return nameOverride;
	}

	public void setNameOverride(String nameOverride) {
		this.nameOverride = nameOverride;
	}

	public RaidDangerLevel getDangerLevel() {
		return RaidDangerLevel.MEDIUM;
	}

	public String getQuantityString(int marines) {
		return Misc.getWithDGS(getQuantity(Math.max(1, marines)));
	}
	
	public Color getQuantityColor(int marines) {
		if (marines <= 0) {
			return Misc.getGrayColor();
		}
		return Misc.getHighlightColor();
	}
	
	public String getValueString(int marines) {
		return Misc.getDGSCredits(getValue(Math.max(1, marines)));
	}
	
	public Color getValueColor(int marines) {
		if (marines <= 0) {
			return Misc.getGrayColor();
		}
		return Misc.getHighlightColor();
	}
	
	
	public float getValueSortValue() {
		float add = 100000000f;
		if (getMarinesAssigned() == 0) add = 0f;
		return getValue(Math.max(1, getMarinesAssigned())) + add;
	}

	public int getMarinesAssigned() {
		return marinesAssigned;
	}

	public void setMarinesAssigned(int marines) {
		marinesAssigned = marines;
	}

	public int getMarinesRequired() {
		return marinesRequired;
	}
	
	public void setMarinesRequired(int marines) {
		marines = Math.min(MarketCMD.MAX_MARINE_TOKENS, marines);
		if (marines < 0) marines = 0;
		marinesRequired = marines;
	}
	
	public String getSourceString() {
		if (source != null) return source.getCurrentName();
		return "";
	}

	public Industry getSource() {
		return source;
	}

	public void setSource(Industry source) {
		this.source = source;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MarketAPI getMarket() {
		return market;
	}

	public void setMarket(MarketAPI market) {
		this.market = market;
	}
	
	public String getCommodityIdForDeficitIcons() {
		return null;
	}

	public String getAssignedForcesColumnText() {
		return assignedForcesColumnText;
	}
	
	public void setAssignedForcesColumnText(String assignedForcesColumnText) {
		this.assignedForcesColumnText = assignedForcesColumnText;
	}

	public Color getAssignedForcesColumnColor() {
		return assignedForcesColumnColor;
	}

	public void setAssignedForcesColumnColor(Color assignedForcesColumnColor) {
		this.assignedForcesColumnColor = assignedForcesColumnColor;
	}

	
	public boolean hasTooltip() {
		return false;
	}

	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		
	}

	public float getTooltipWidth() {
		return 500f;
	}

	public boolean isTooltipExpandable() {
		return false;
	}
	

	public int getQuantityLooted() {
		return quantityLooted;
	}

	public void setQuantityLooted(int quantityLooted) {
		this.quantityLooted = quantityLooted;
	}

	public int getXpGained() {
		return xpGained;
	}

	public void setXpGained(int xpGained) {
		this.xpGained = xpGained;
	}
	
	public boolean withContinueBeforeResult() {
		return false;
	}
	
}
