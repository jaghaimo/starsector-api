package com.fs.starfarer.api.campaign.econ;

import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;

public interface MarketConditionAPI {
	
	
	/**
	 * Id of the condition spec, i.e. "ore_sparse".
	 * @return
	 */
	String getId();
	String getName();
	MarketConditionPlugin getPlugin();
	
	/**
	 * Globally unique id for this specific condition, i.e. "ore_sparse_b44c3".
	 * @return
	 */
	String getIdForPluginModifications();
	boolean isSurveyed();
	void setSurveyed(boolean wasSurveyed);
	boolean requiresSurveying();
	ConditionGenDataSpec getGenSpec();
	
//	boolean isFromEvent();
//	void setFromEvent(boolean isFromEvent);
	
	/**
	 * Calls MarketConditionPlugin.isPlanetary().
	 * 
	 * "Planetary" conditions show up on the right side of the conditions widget.
	 * "Market" conditions show up in a group to the left of the planetary conditions.
	 * @return
	 */
	boolean isPlanetary();
	
	MarketConditionSpecAPI getSpec();
}
