package com.fs.starfarer.api.impl.campaign.ghosts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;


public class GBLeadPlayerTo extends BaseGhostBehavior {
	
	protected SectorEntityToken to;
	protected int maxBurn;
	protected float maxDistAhead;
	protected boolean returningToPlayer = false;
	
	public GBLeadPlayerTo(float duration, SectorEntityToken to, float maxDistAhead, int maxBurn) {
		super(duration);
		this.to = to;
		this.maxDistAhead = maxDistAhead;
		this.maxBurn = maxBurn;
	}



	@Override
	public void advance(float amount, SensorGhost ghost) {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf.getContainingLocation() != ghost.getEntity().getContainingLocation() || !pf.isAlive()) {
			end();
			return;
		}
		super.advance(amount, ghost);
		
		
		float goBackThreshold = maxDistAhead * 2f;
		if (returningToPlayer) {
			goBackThreshold = maxDistAhead * 1.5f;
		}
		Vector2f dir = Misc.getUnitVector(pf.getLocation(), to.getLocation());
		Vector2f diff = Vector2f.sub(ghost.getEntity().getLocation(), pf.getLocation(), new Vector2f());
		
		float distAheadOfPlayer = Vector2f.dot(dir, diff);
		float distFromPlayer = Misc.getDistance(pf, ghost.getEntity());
		if (distFromPlayer >= goBackThreshold) {
			ghost.moveTo(pf.getLocation(), pf.getVelocity(), maxBurn);
			returningToPlayer = true;
		} else if (distAheadOfPlayer > maxDistAhead) {
			int burn = (int) (maxBurn * maxDistAhead / distAheadOfPlayer * 0.5f);
			ghost.moveTo(to.getLocation(), new Vector2f(), burn);
			returningToPlayer = false;
		} else {
			ghost.moveTo(to.getLocation(), new Vector2f(), maxBurn);
			returningToPlayer = false;
		}
		
		
		
		float dist = Misc.getDistance(ghost.getEntity(), to);
		if (dist < ghost.getEntity().getRadius() + to.getRadius()) {
			end();
			return;
		}
		
	}
	
	
	
}













