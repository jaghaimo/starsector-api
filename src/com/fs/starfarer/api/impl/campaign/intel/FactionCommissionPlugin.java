package com.fs.starfarer.api.impl.campaign.intel;

import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;

public interface FactionCommissionPlugin extends EconomyTickListener {

	String getNameOverride();
	
	
	
}
