package com.fs.starfarer.api.campaign;

public interface CampaignTerrainAPI extends SectorEntityToken {
	CampaignTerrainPlugin getPlugin();
	String getType();
	void setRadius(float radius);
}
