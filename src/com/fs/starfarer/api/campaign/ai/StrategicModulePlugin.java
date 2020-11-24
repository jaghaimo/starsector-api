package com.fs.starfarer.api.campaign.ai;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.TimeoutTracker;

public interface StrategicModulePlugin {
	
	void advance(float days);
	
	
	boolean isAllowedToEngage(SectorEntityToken other);
	boolean isAllowedToEvade(SectorEntityToken other);

	void dumpExcessCargoIfNeeded();

	TimeoutTracker<SectorEntityToken> getDoNotAttack();
}
