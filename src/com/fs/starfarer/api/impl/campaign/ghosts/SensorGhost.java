package com.fs.starfarer.api.impl.campaign.ghosts;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.util.SmoothMovementUtil;

public interface SensorGhost extends EveryFrameScript {

	void addBehavior(GhostBehavior b);

	float getDespawnRange();
	void setDespawnRange(float despawnRange);

	void moveTo(Vector2f dest, float maxBurn);
	void moveTo(Vector2f dest, Vector2f destVel, float maxBurn);

	SmoothMovementUtil getMovement();

	CustomCampaignEntityAPI getEntity();

	float getAcceleration();

	int getMaxBurn();

	int getCurrBurn();

	List<GhostBehavior> getScript();

	void clearScript();

	boolean isCreationFailed();

	boolean isDespawnInAbyss();
	void setDespawnInAbyss(boolean despawnInAbyss);
}
