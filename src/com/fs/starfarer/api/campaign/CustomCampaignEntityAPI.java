package com.fs.starfarer.api.campaign;

import java.util.EnumSet;

public interface CustomCampaignEntityAPI extends SectorEntityToken {
	void setRadius(float radius);

	CampaignFleetAPI getFleetForVisual();
	void setFleetForVisual(CampaignFleetAPI fleetForVisual);

	void setActiveLayers(CampaignEngineLayers ... layers);
	EnumSet<CampaignEngineLayers> getActiveLayers();

}
