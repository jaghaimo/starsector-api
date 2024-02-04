package com.fs.starfarer.api.impl.campaign.ghosts;

import java.util.List;

public interface SensorGhostCreator {
	String getId();
	List<SensorGhost> createGhost(SensorGhostManager manager);
	float getFrequency(SensorGhostManager manager);
	float getTimeoutDaysOnSuccessfulCreate(SensorGhostManager manager);
	boolean canSpawnWhilePlayerInOrNearSlipstream();
	boolean canSpawnWhilePlayerInAbyss();
	boolean canSpawnWhilePlayerOutsideAbyss();
}
