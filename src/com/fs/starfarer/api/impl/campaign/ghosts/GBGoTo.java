package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;


public class GBGoTo extends BaseGhostBehavior {
	
	protected SectorEntityToken to;
	protected int maxBurn;
	
	public GBGoTo(float duration, SectorEntityToken to, int maxBurn) {
		super(duration);
		this.to = to;
		this.maxBurn = maxBurn;
	}



	@Override
	public void advance(float amount, SensorGhost ghost) {
		super.advance(amount, ghost);
		
		ghost.moveTo(to.getLocation(), new Vector2f(), maxBurn);
		
		float dist = Misc.getDistance(ghost.getEntity(), to);
		if (dist < ghost.getEntity().getRadius() + to.getRadius()) {
			end();
			return;
		}
		
	}
	
	
	
}













