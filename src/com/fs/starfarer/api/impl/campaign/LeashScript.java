package com.fs.starfarer.api.impl.campaign;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;

public class LeashScript implements EveryFrameScript {

	private CampaignFleetAPI fleet;
	private float range;
	private Script onBroken;

	private SectorEntityToken anchor;
	private Vector2f offset;
	
	public LeashScript(CampaignFleetAPI fleet, float range, SectorEntityToken anchor, Vector2f offset, Script onBroken) {
		this.fleet = fleet;
		this.range = range;
		this.onBroken = onBroken;
		this.anchor = anchor;
		this.offset = offset;
	}

	private boolean broken = false;
	public void advance(float amount) {
		if (fleet.getContainingLocation() != anchor.getContainingLocation()) {
			broken = true;
			onBroken.run();
		}
		if (broken) return;
		
		
		Vector2f dest = new Vector2f(anchor.getLocation().x + offset.x, anchor.getLocation().y + offset.y);
		float distToDest = Misc.getDistance(dest, fleet.getLocation());
		fleet.setMoveDestination(dest.x, dest.y);
		if (distToDest < 2) {
			fleet.setLocation(dest.x, dest.y);
		}
		
		float dist = Misc.getDistance(anchor.getLocation(), fleet.getLocation());
		if (dist > range + anchor.getRadius() + fleet.getRadius()) {
			broken = true;
			onBroken.run();
		}
		
	}
	
	public boolean isDone() {
		return broken;
	}

	public boolean runWhilePaused() {
		return false;
	}
}





