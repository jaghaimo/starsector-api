package com.fs.starfarer.api.campaign.listeners;

/**
 * Only called when time passes, i.e. not on economy updates when opening a colony UI etc.
 * @author Alex Mosolov
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public interface EconomyTickListener {
	void reportEconomyTick(int iterIndex);
	void reportEconomyMonthEnd();
}
