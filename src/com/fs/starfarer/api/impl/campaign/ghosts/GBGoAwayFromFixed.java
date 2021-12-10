package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;


/**
 * Direction to go in is fixed when behavior starts.
 */
public class GBGoAwayFromFixed extends BaseGhostBehavior {
	
	protected boolean dirSet = false;
	protected float dir;
	protected SectorEntityToken from;
	protected int maxBurn;
	
	public GBGoAwayFromFixed(float duration, SectorEntityToken from, int maxBurn) {
		super(duration);
		this.from = from;
		this.maxBurn = maxBurn;
	}



	@Override
	public void advance(float amount, SensorGhost ghost) {
		if (from.getContainingLocation() != ghost.getEntity().getContainingLocation() || !from.isAlive()) {
			end();
			return;
		}
		super.advance(amount, ghost);
		
		if (!dirSet) {
			dir = Misc.getAngleInDegrees(from.getLocation(), ghost.getEntity().getLocation());
			dirSet = true;
		}
		
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(dir);
		loc.scale(10000f);
		Vector2f.add(loc, ghost.getEntity().getLocation(), loc);
		ghost.moveTo(loc, maxBurn);
		
	}
	
	
	
}













