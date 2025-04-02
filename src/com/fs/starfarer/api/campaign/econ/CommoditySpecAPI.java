package com.fs.starfarer.api.campaign.econ;

import java.util.Set;

import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.loading.WithSourceMod;

public interface CommoditySpecAPI extends WithSourceMod {
	String getOrigin();
	String getIconName();
	String getIconLargeName();
	
	void setOrder(float displayNumber);
	
	int getStackSize();
	float getOrder();
	float getCargoSpace();
	PriceVariability getPriceVariability();
	boolean isPrimary();
	boolean isExotic();
	boolean isMeta();
	boolean isNonEcon();
	String getId();
	String getName();
	
	/**
	 * Lower-case unless exotic.
	 * @return
	 */
	String getLowerCaseName();
	float getBasePrice();
	Set<String> getTags();
	boolean hasTag(String tag);
	
	void setBasePrice(float price);
	
	
	/**
	 * You probably want the CommodityOnMarket.getUtilityOnMarket() method instead, as that takes into
	 * account the varying utility of exotic goods.
	 * @return
	 */
	float getUtility();
	String getDemandClass();
	
	float getEconomyTier();
	float getEconUnit();

	boolean isPersonnel();
	boolean isFuel();
	boolean isSupplies();
	float getIconWidthMult();
	String getSoundIdDrop();
	
	float getExportValue();
	void setExportValue(float exportValue);
	String getSoundId();
	
	RaidDangerLevel getBaseDanger();
	void setBaseDanger(RaidDangerLevel danger);
	
	void setName(String displayName);
	void setIconName(String iconName);
	void setDemandClass(String demandClass);
}



