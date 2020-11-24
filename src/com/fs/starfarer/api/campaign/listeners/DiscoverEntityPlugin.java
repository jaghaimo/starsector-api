package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI.GenericPlugin;

public interface DiscoverEntityPlugin extends GenericPlugin {
	void discoverEntity(SectorEntityToken entity);
}
