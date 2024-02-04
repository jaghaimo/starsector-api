package com.fs.starfarer.api.campaign;

public interface FleetInflater {
	void inflate(CampaignFleetAPI fleet);
	boolean removeAfterInflating();
	void setRemoveAfterInflating(boolean removeAfterInflating);
	
	Object getParams();
	
	float getQuality();
	void setQuality(float quality);
	int getAverageNumSMods();
}
