package com.fs.starfarer.api.impl.campaign.intel.events;

import java.awt.Color;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

public class BaseEventFactor implements EventFactor {

	public static float TOOLTIP_WIDTH = 400f;
	
	public int getProgress(BaseEventIntel intel) {
		return 0;
	}
	
	public float getAllProgressMult(BaseEventIntel intel) {
		return 1f;
	}
	
	public boolean shouldShow(BaseEventIntel intel) {
		return getProgress(intel) != 0;
	}

	public String getDesc(BaseEventIntel intel) {
		return null;
	}

	public String getProgressStr(BaseEventIntel intel) {
		int p = getProgress(intel);
		if (p <= 0) return "" + p;
		return "+" + p;
	}

	public Color getDescColor(BaseEventIntel intel) {
		return Misc.getTextColor();
	}

	public Color getProgressColor(BaseEventIntel intel) {
		return intel.getProgressColor(getProgress(intel));
	}

	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		// to make mods that don't implement this method and only implement the older 
		// getMainRowTooltip() with no parameter work
		return getMainRowTooltip();
	}
	public TooltipCreator getMainRowTooltip() {
		return null;
	}

	public boolean isOneTime() {
		return false;
	}

	public boolean isExpired() {
		return false;
	}

	public void addExtraRows(TooltipMakerAPI info, BaseEventIntel intel) {
		
	}

	public void notifyEventEnding() {
		
	}

	public void notifyEventEnded() {
		
	}
	
	/**
	 * The first element in the stage info needs to have a before-padding of 5 pixels.
	 * @param info
	 */
	public void addBorder(TooltipMakerAPI info, Color c) {
		float small = 5f;
		info.addSpacer(small);
		UIComponentAPI rect = info.createRect(c, 2f);
		float extra = 0f;
		extra = 64f + 14f;
		info.addCustomDoNotSetPosition(rect).getPosition().inTL(-small - extra, 0).setSize(
				info.getWidthSoFar() + small * 2f + extra + 10f, Math.max(64f, info.getHeightSoFar() + 3f));
	}

	public void addBulletPointForOneTimeFactor(BaseEventIntel intel, TooltipMakerAPI info, Color tc, float initPad) {

	}

	public void notifyFactorRemoved() {
		
	}

	public void advance(float amount) {
		// TODO Auto-generated method stub
		
	}


}
