package com.fs.starfarer.api.campaign;

import java.awt.Color;

public interface RingBandAPI extends SectorEntityToken {

	boolean isSpiral();
	void setSpiral(boolean spiral);
	void setMinSpiralRadius(float minSpiralRadius);
	float getMinSpiralRadius();
	float getSpiralFactor();
	void setSpiralFactor(float spiralFactor);
	
	String getSpriteKey();
	String getSpriteCategory();
	SectorEntityToken getFocus();
	void setFocus(SectorEntityToken focus);
	float getBandWidthInTexture();
	void setBandWidthInTexture(float bandWidthInTexture);
	int getBandIndex();
	void setBandIndex(int bandIndex);
	Color getColor();
	void setColor(Color color);
	float getBandWidthInEngine();
	void setBandWidthInEngine(float bandWidthInEngine);
	float getMiddleRadius();
	void setMiddleRadius(float middleRadius);
	String getCategory();
	void setCategory(String category);
	float getOrbitDays();
	void setOrbitDays(float orbitDays);
	String getKey();
}
