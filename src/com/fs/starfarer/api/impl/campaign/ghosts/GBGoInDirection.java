package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.util.Misc;


public class GBGoInDirection extends BaseGhostBehavior {
	
	protected float direction;
	protected int maxBurn;
	
	public GBGoInDirection(float duration, float direction, int maxBurn) {
		super(duration);
		this.direction = direction;
		this.maxBurn = maxBurn;
	}



	@Override
	public void advance(float amount, SensorGhost ghost) {
		super.advance(amount, ghost);
		
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(direction);
		loc.scale(10000f);
		Vector2f.add(loc, ghost.getEntity().getLocation(), loc);
		ghost.moveTo(loc, maxBurn);
		
	}
	
	
	
}













