package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;

public interface EventFactor {

	public static String NEGATED_FACTOR_PROGRESS = "---";
	
	int getProgress(BaseEventIntel intel);
	
	/**
	 * For all factors, not just this one.
	 */
	float getAllProgressMult(BaseEventIntel intel);
	
	
	String getDesc(BaseEventIntel intel);
	String getProgressStr(BaseEventIntel intel);
	
	Color getDescColor(BaseEventIntel intel);
	Color getProgressColor(BaseEventIntel intel);
	
	@Deprecated TooltipCreator getMainRowTooltip();
	TooltipCreator getMainRowTooltip(BaseEventIntel intel);
	
	boolean shouldShow(BaseEventIntel intel);
	
	boolean isOneTime();
	boolean isExpired();
	
	void addExtraRows(TooltipMakerAPI info, BaseEventIntel intel);
	void notifyEventEnding();
	void notifyEventEnded();
	void notifyFactorRemoved();
	
	
	void addBulletPointForOneTimeFactor(BaseEventIntel intel, TooltipMakerAPI info, Color tc, float initPad);
	
	void advance(float amount);
}
