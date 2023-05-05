package com.fs.starfarer.api.campaign;

import java.util.Set;

import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;

public interface SpecialItemSpecAPI {
	String getIconName();
	
	int getStackSize();
	float getOrder();
	float getCargoSpace();
	
	String getId();
	String getName();
	float getBasePrice();
	
	Set<String> getTags();
	boolean hasTag(String tag);

	/**
	 * If null is passed in for a stack, the init() method of the plugin will not be called.
	 * @param stack
	 * @return
	 */
	SpecialItemPlugin getNewPluginInstance(CargoStackAPI stack);

	String getDesc();

	void setDesc(String desc);

	String getParams();

	float getRarity();

	String getSoundId();

	String getSoundIdDrop();

	String getDescFirstPara();

	void setBaseDanger(RaidDangerLevel danger);
	RaidDangerLevel getBaseDanger();

	String getManufacturer();

	void setManufacturer(String manufacturer);

	void setParams(String params);

	void setIconName(String iconName);

	void setName(String displayName);

	void setBasePrice(float baseValue);

	void setOrder(float displayNumber);
	
}



