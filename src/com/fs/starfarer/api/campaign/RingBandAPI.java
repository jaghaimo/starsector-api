package com.fs.starfarer.api.campaign;

public interface RingBandAPI extends SectorEntityToken {

	boolean isSpiral();
	void setSpiral(boolean spiral);
	void setMinSpiralRadius(float minSpiralRadius);
	float getMinSpiralRadius();
	float getSpiralFactor();
	void setSpiralFactor(float spiralFactor);
	
	String getSpriteKey();
	String getSpriteCategory();
}
