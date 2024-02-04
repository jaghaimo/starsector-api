package com.fs.starfarer.api.impl.campaign.terrain;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;

public abstract class BaseHyperspaceAbyssPlugin implements HyperspaceAbyssPlugin {

	public BaseHyperspaceAbyssPlugin() {
		
	}

	public float getAbyssalDepth(SectorEntityToken entity) {
		return getAbyssalDepth(entity.getLocation());
	}
	public abstract float getAbyssalDepth(Vector2f loc);
	
	public boolean isInAbyss(Vector2f loc) {
		return getAbyssalDepth(loc) > 0;
	}

	public boolean isInAbyss(SectorEntityToken entity) {
		return isInAbyss(entity.getLocation());
	}

	public void advance(float amount) {
		
	}

	public List<StarSystemAPI> getAbyssalSystems() {
		return new ArrayList<StarSystemAPI>();
	}
	
	
	
}





