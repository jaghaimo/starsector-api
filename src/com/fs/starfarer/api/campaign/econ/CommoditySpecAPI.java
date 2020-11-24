package com.fs.starfarer.api.campaign.econ;

import java.util.Set;

public interface CommoditySpecAPI {
	String getOrigin();
	String getIconName();
	String getIconLargeName();
	
	int getStackSize();
	float getOrder();
	float getCargoSpace();
	PriceVariability getPriceVariability();
	boolean isPrimary();
	boolean isExotic();
	boolean isMeta();
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
	
}



