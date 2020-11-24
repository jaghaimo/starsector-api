package com.fs.starfarer.api.combat;

import java.awt.Color;

public interface FluxTrackerAPI {

	float getTimeToVent();
	float getOverloadTimeRemaining();
	
	boolean isOverloaded();
	boolean isVenting();
	boolean isOverloadedOrVenting();
	
	boolean isEngineBoostActive();
	/**
	 * @return flux level, from 0 to 1
	 */
	float getFluxLevel();
	
	float getCurrFlux();
	float getHardFlux();
	float getMaxFlux();
	
	void setHardFlux(float minFlux);
	void setCurrFlux(float currFlux);
	
	/**
	 * @param fluxAmount
	 * @param hardFlux
	 * @return false if flux couldn't be raised successfully. Hard flux increases always return true and can overload the ship.
	 */
	boolean increaseFlux(float fluxAmount, boolean hardFlux);
	void decreaseFlux(float fluxAmount);
	
	void forceOverload(float extraOverloadTime);
	void stopOverload();
	void stopVenting();
	void beginOverloadWithTotalBaseDuration(float totalDur);
	
	/**
	 * Whether the "Overload!" floaty should be shown.
	 * @return
	 */
	boolean showFloaty();
	
	/**
	 * "Overload" floaty font size.
	 * @return
	 */
	float getFloatySize();
	void playOverloadSound();
	void showOverloadFloatyIfNeeded();
	void showOverloadFloatyIfNeeded(String text, Color color, float fontSizeBonus, boolean alwaysShow);
	
	float getEngineBoostLevel();
}
