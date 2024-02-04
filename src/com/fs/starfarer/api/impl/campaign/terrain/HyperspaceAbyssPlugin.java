package com.fs.starfarer.api.impl.campaign.terrain;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;

public interface HyperspaceAbyssPlugin {
	boolean isInAbyss(Vector2f loc);
	boolean isInAbyss(SectorEntityToken entity);
	float getAbyssalDepth(Vector2f loc);
	float getAbyssalDepth(SectorEntityToken entity);
	void advance(float amount);
	List<StarSystemAPI> getAbyssalSystems();
}
