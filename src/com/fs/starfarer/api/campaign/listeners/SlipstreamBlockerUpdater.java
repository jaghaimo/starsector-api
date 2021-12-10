package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.combat.CollisionGridAPI;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamManager;

public interface SlipstreamBlockerUpdater {
	void updateSlipstreamBlockers(CollisionGridAPI grid, SlipstreamManager manager);
}
