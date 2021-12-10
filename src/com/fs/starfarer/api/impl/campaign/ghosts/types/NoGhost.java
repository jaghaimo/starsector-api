package com.fs.starfarer.api.impl.campaign.ghosts.types;

import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost;
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager;

public class NoGhost extends BaseSensorGhost {

	public NoGhost(SensorGhostManager manager) {
		super(manager, 0);
	}
}
