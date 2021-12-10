package com.fs.starfarer.api.impl.campaign.ghosts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;


public class GBEchoMovement extends BaseGhostBehavior {
	
	public static class GBEchoSnapshot {
		Vector2f vel;
		long timestamp;
	}
	
	protected List<GBEchoSnapshot> snapshots = new ArrayList<GBEchoSnapshot>();
	protected SectorEntityToken other;
	protected float delayDays;
	
	
	public GBEchoMovement(SectorEntityToken other, float delayDays, float duration) {
		super(duration);
		this.other = other;
		this.delayDays = delayDays;
	}

	@Override
	public void advance(float amount, SensorGhost ghost) {
		if (other.getContainingLocation() != ghost.getEntity().getContainingLocation() || !other.isAlive()) {
			end();
			return;
		}
		
		super.advance(amount, ghost);
		
		GBEchoSnapshot curr = new GBEchoSnapshot();
		curr.vel = new Vector2f(other.getVelocity());
		curr.timestamp = Global.getSector().getClock().getTimestamp();
		snapshots.add(curr);
		
		Iterator<GBEchoSnapshot> iter = snapshots.iterator();
		GBEchoSnapshot use = null;
		while (iter.hasNext()) {
			curr = iter.next();
			float ago = Global.getSector().getClock().getElapsedDaysSince(curr.timestamp);
			if (ago >= delayDays) {
				use = curr;
				iter.remove();
			} else {
				break;
			}
		}
		
		if (use != null) {
			ghost.getMovement().getVelocity().set(use.vel);
		}
	}
	
	
	
}









