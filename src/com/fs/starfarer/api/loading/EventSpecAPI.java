package com.fs.starfarer.api.loading;

public interface EventSpecAPI {
	String getId();
	String getPluginClass();
	String getImage();
	float getProbabilityMult();
	void setProbabilityMult(float probabilityMult);
	int getMaxOngoing();
	void setMaxOngoing(int maxOngoing);
	void setImage(String image);
}
