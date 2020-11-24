package com.fs.starfarer.api.campaign.econ;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.ui.TooltipMakerAPI;


public interface MarketConditionPlugin {
	
	void advance(float amount);
	
	void init(MarketAPI market, MarketConditionAPI condition);
	void apply(String id);
	void unapply(String id);
	
	
	/**
	 * Only used for conditions that come from events.
	 * @return
	 */
	List<String> getRelatedCommodities();
	
	
	/**
	 * For the description that shows up in the tooltip.
	 * @return
	 */
	Map<String, String> getTokenReplacements();
	
	/**
	 * For the description, which is shown in the tooltip.
	 * @return
	 */
	String [] getHighlights();
	Color [] getHighlightColors();
	
	void setParam(Object param);
	
	/**
	 * Return false if the plugin has data that needs to be in the savefile. Otherwise, it won't be saved.
	 * @return
	 */
	boolean isTransient();
	boolean showIcon();
	boolean isPlanetary();
	
	
	boolean hasCustomTooltip();
	void createTooltip(TooltipMakerAPI tooltip, boolean expanded);
	boolean isTooltipExpandable();
	float getTooltipWidth();

	boolean runWhilePaused();
	
	String getName();
	String getIconName();
}



