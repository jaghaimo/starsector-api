package com.fs.starfarer.api.impl.campaign.ghosts;

public class GBStayInPlace extends BaseGhostBehavior {

	public GBStayInPlace(float duration) {
		super(duration);
	}
	
	
	
	@Override
	public void advance(float amount, SensorGhost ghost) {
		super.advance(amount, ghost);
		
		ghost.moveTo(ghost.getEntity().getLocation(), 5);
	}
}
