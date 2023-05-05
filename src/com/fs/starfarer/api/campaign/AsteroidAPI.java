package com.fs.starfarer.api.campaign;


public interface AsteroidAPI extends SectorEntityToken {

	float getRotation();

	void setRotation(float rotation);

	void forceRender();

}
