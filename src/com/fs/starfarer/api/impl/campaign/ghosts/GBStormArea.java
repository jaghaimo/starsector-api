package com.fs.starfarer.api.impl.campaign.ghosts;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin.CellState;
import com.fs.starfarer.api.util.Misc;

public class GBStormArea extends BaseGhostBehavior {

	protected float radius;
	
	public GBStormArea(float radius) {
		super(1f);
		this.radius = radius;
	}
	
	@Override
	public void advance(float amount, SensorGhost ghost) {
		super.advance(amount, ghost);
		
		SectorEntityToken entity = ghost.getEntity();
		HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		if (plugin != null && entity.isInHyperspace()) {
			plugin.setTileState(entity.getLocation(), 1000f, 
					CellState.SIGNAL, 
					0f, 0.1f, 0.8f);
		}
		end();
	}
}
