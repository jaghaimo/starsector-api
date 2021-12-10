package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.util.Misc;

/**
 * Up to a configurable number of fleets. Instant despawn when player is far enough.
 * 
 * New fleets generated after respawnDelay if some are destroyed.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2017 Fractal Softworks, LLC
 */
public abstract class SourceBasedFleetManager implements FleetEventListener, EveryFrameScript {

	public static float DESPAWN_THRESHOLD_PAD_LY = 1;
	public static float DESPAWN_MIN_DIST_LY = 3;
	
	protected List<CampaignFleetAPI> fleets = new ArrayList<CampaignFleetAPI>();
	protected float thresholdLY = 4f;
	protected SectorEntityToken source;
	
	public static boolean DEBUG = true;
	protected int minFleets;
	protected int maxFleets;
	protected float respawnDelay;
	
	protected float destroyed = 0;
	
	protected Vector2f sourceLocation = new Vector2f();
	
	public SourceBasedFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay) {
		this.source = source;
		this.thresholdLY = thresholdLY;
		this.minFleets = minFleets;
		this.maxFleets = maxFleets;
		this.respawnDelay = respawnDelay;
	}
	
	public float getThresholdLY() {
		return thresholdLY;
	}

	public SectorEntityToken getSource() {
		return source;
	}

	protected abstract CampaignFleetAPI spawnFleet();

	public void advance(float amount) {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		
//		if (destroyed > 0) {
//			System.out.println("destroyed: " + destroyed);
//		}
		float days = Global.getSector().getClock().convertToDays(amount);
		destroyed -= days / respawnDelay;
		if (destroyed < 0) destroyed = 0;
		
		
		// clean up orphaned, juuust in case - could've been directly removed elsewhere, say, instead of despawned.
		Iterator<CampaignFleetAPI> iter = fleets.iterator();
		while (iter.hasNext()) {
			if (!iter.next().isAlive()) {
				iter.remove();
			}
		}

//		if (source != null && source.getContainingLocation().getName().toLowerCase().contains("idimmeron")) {
//			System.out.println("wefwefwefw");
//		}
		
		if (source != null) {
			if (!source.isAlive()) {
				source = null;
			} else {
				sourceLocation.set(source.getLocationInHyperspace());
			}
		}
		
		float distFromSource = Misc.getDistanceLY(player.getLocationInHyperspace(), sourceLocation);
		float f = 0f;
		if (distFromSource < thresholdLY) {
			f = (thresholdLY - distFromSource) / (thresholdLY * 0.1f);
			if (f > 1) f = 1;
		}
		int currMax = minFleets + Math.round((maxFleets - minFleets) * f);
		currMax -= Math.ceil(destroyed);
		
		if (source == null) {
			currMax = 0;
		}
		
		// try to despawn some fleets if above maximum
		if (currMax < fleets.size()) {
			for (CampaignFleetAPI fleet : new ArrayList<CampaignFleetAPI>(fleets)) {
				float distFromPlayer = Misc.getDistanceLY(player.getLocationInHyperspace(), fleet.getLocationInHyperspace());
				if (distFromPlayer > DESPAWN_MIN_DIST_LY && distFromPlayer > thresholdLY + DESPAWN_THRESHOLD_PAD_LY) {
					fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null);
					if (fleets.size() <= currMax) break;
				}
			}
			
		}
		
		// spawn some if below maximum
		if (currMax > fleets.size()) {
			CampaignFleetAPI fleet = spawnFleet();
			if (fleet != null) {
				fleets.add(fleet);
				//if (shouldAddEventListenerToFleet()) {
					fleet.addEventListener(this);
				//}
			}
		}
		
		if (source == null && fleets.size() == 0) {
			setDone(true);
		}
	}
	
//	protected boolean shouldAddEventListenerToFleet() {
//		return true;
//	}

	private boolean done = false;
	public boolean isDone() {
		return done;
	}
	
	public void setDone(boolean done) {
		this.done = done;
	}

	public boolean runWhilePaused() {
		return false;
		//return Global.getSettings().isDevMode();
	}

	
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
			destroyed++;
		}
		fleets.remove(fleet);
	}
	
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
	}
	
//	public static void main(String[] args) {
//		int minFleets = 4;
//		int maxFleets = 10;
//		float thresholdLY = 1f;
//		
//		for (float d = 0; d < 3f; d += 0.03f) {
//			float f = 0f;
//			if (d < thresholdLY) {
//				f = (thresholdLY - d) / (thresholdLY * 0.1f);
//				if (f > 1) f = 1;
//			}
//			int numFleets = minFleets + Math.round((maxFleets - minFleets) * f);
//			System.out.println("Num fleets: " + numFleets + " at range " + d);
//		} 
//	}
}




