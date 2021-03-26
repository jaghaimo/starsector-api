package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

public interface EmpArcEntityAPI extends CombatEntityAPI {

	float getCoreWidthOverride();
	void setCoreWidthOverride(float coreWidthOverride);
	void setTargetToShipCenter(Vector2f sourceSlotPos, ShipAPI ship);
	Vector2f getTargetLocation();
	void setSingleFlickerMode();

}
