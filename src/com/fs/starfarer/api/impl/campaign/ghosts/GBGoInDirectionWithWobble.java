package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.util.Misc;


public class GBGoInDirectionWithWobble extends BaseGhostBehavior {
	
	protected float direction;
	protected int maxBurn;
	protected float phase = (float) Math.random();
	protected float wobbleRate;
	protected float maxWobble;
	
	public GBGoInDirectionWithWobble(float duration, float direction, float wobbleRate, float maxWobble, int maxBurn) {
		super(duration);
		this.direction = direction;
		this.wobbleRate = wobbleRate;
		this.maxWobble = maxWobble;
		this.maxBurn = maxBurn;
	}



	@Override
	public void advance(float amount, SensorGhost ghost) {
		super.advance(amount, ghost);
		
		float pi = (float) Math.PI;
		float sin = (float) Math.sin(phase * pi * 2f);
		phase += amount * wobbleRate;
		
		float maxAngleOffset = maxWobble;
		float angle = direction + sin * maxAngleOffset;
		
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
		loc.scale(10000f);
		Vector2f.add(loc, ghost.getEntity().getLocation(), loc);
		ghost.moveTo(loc, maxBurn);
		
		
		//System.out.println("Move angle: " + angle);
		//System.out.println("Velocity: [" + (int)ghost.getEntity().getVelocity().x + "," + (int)ghost.getEntity().getVelocity().y + "]");
		//System.out.println("Location: [" + (int)ghost.getEntity().getLocation().x + "," + (int)ghost.getEntity().getLocation().y + "]");
		
	}
	
	
	
}













