package com.fs.starfarer.api.combat;

public interface ShipSystemSpecAPI {

	String getIconSpriteName();

	boolean isCanUseWhileRightClickSystemOn();

	void setCanUseWhileRightClickSystemOn(boolean canUseWhileRightClickSystemOn);

	float getRange();

}
