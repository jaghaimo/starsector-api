package com.fs.starfarer.api.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

public interface EngineSlotAPI {
	float getWidth();
	float getContrailWidth();
	float getLength();
	Color getColor();
	Color getContrailColor();
	void setContrailColor(Color contrailColor);
	void setColor(Color color);
	void setAngle(float angle);
	float getAngle();
	void setContrailWidth(float contrailWidth);
	float getContrailDuration();
	void setContrailDuration(float contrailDuration);
	float getContrailSpeedMultMaxSpeed();
	float getContrailSpawnDistMult();
	void setContrailSpawnDistMult(float spawnDistMult);
	void setContrailSpeedMultMaxSpeed(float contrailSpeedMultMaxSpeed);
	float getContrailSpeedMultAngVel();
	void setContrailSpeedMultAngVel(float contrailSpeedMultAngVel);
	float getContrailMinSegLength();
	void setContrailMinSegLength(float contrailMinSegLength);
	float getContrailMaxSegLength();
	void setContrailMaxSegLength(float contrailMaxSegLength);
	float getContrailWidthMultiplier();
	void setContrailWidthMultiplier(float contrailWidthMultiplier);
	float computeMidArcAngle(float entityFacing);
	Vector2f computePosition(Vector2f entityLocation, float entityFacing);
	float getGlowSizeMult();
	void setGlowSizeMult(float glowSizeMult);
	Color getGlowAlternateColor();
	void setGlowAlternateColor(Color glowAlternateColor);
	boolean isFlickerWhenMissileFlamedOut();
	void setFlickerWhenMissileFlamedOut(boolean flickerWhenMissileFlamedOut);
}
