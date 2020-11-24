package com.fs.starfarer.api.campaign;

import java.util.Set;

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
}
