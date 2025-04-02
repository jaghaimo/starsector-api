package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;

public interface DetectedEntityListener extends GenericPlugin {
	void reportDetectedEntity(SectorEntityToken entity, VisibilityLevel level);
}
