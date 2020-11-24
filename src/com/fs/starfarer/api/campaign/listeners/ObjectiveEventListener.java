package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public interface ObjectiveEventListener {
	void reportObjectiveChangedHands(SectorEntityToken objective, FactionAPI from, FactionAPI to);
	void reportObjectiveDestroyed(SectorEntityToken objective, SectorEntityToken stableLocation, FactionAPI enemy);
}
