package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.util.Misc;

public abstract class SeededFleetManager implements FleetEventListener, EveryFrameScript{

	public static float DESPAWN_PAD_LY = 1f;
	
	public static class SeededFleet {
		public long seed;
		public int points;
		public CampaignFleetAPI fleet;
	}
	
	protected List<SeededFleet> fleets = new ArrayList<SeededFleet>();
	protected float inflateRangeLY = 5f;
	protected StarSystemAPI system;
	
	public static boolean DEBUG = false;
	
	public SeededFleetManager(StarSystemAPI system, float inflateRangeLY) {
		this.system = system;
		this.inflateRangeLY = inflateRangeLY;
	}
	
	public void addSeed(long seed) {
		SeededFleet f = new SeededFleet();
		f.seed = seed;
		fleets.add(f);
	}
	
	public float getInflateRangeLY() {
		return inflateRangeLY;
	}

	public StarSystemAPI getSystem() {
		return system;
	}

	protected abstract CampaignFleetAPI spawnFleet(long seed);

	public void advance(float amount) {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		
		float distFromSystem = Misc.getDistanceLY(player.getLocationInHyperspace(), system.getLocation());
		if (distFromSystem < inflateRangeLY) {
			float index = 0f;
			for (SeededFleet curr : fleets) {
				float threshold = inflateRangeLY * (0.9f + 0.1f / fleets.size() * (index + 1f));
				if (distFromSystem < threshold) {
					if (curr.fleet == null) {
						curr.fleet = spawnFleet(curr.seed);
						if (curr.fleet != null) {
							if (DEBUG) {
								System.out.println("Created " + curr.fleet.getName() + " (seed: " + curr.seed + ")");
								//System.out.println("  Portrait: " + curr.fleet.getCommander().getPortraitSprite());
							}
							curr.fleet.addEventListener(this);
							curr.points = curr.fleet.getFleetPoints();
						}
					}
				}
				index++;
			}
		}
		
		for (SeededFleet curr : fleets) {
			if (curr.fleet != null) {
				float dist = Misc.getDistanceLY(player.getLocationInHyperspace(), curr.fleet.getLocationInHyperspace());
				if (dist > inflateRangeLY + DESPAWN_PAD_LY && curr.points == curr.fleet.getFleetPoints()) {
					if (DEBUG) System.out.println("Despawned " + curr.fleet.getName() + " (seed: " + curr.seed + ")");
					curr.fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null);
					curr.fleet = null;
					curr.points = 0;
				}
			}
		}
	}

	
	
	public boolean isDone() {
		return fleets.isEmpty();
	}

	public boolean runWhilePaused() {
		//return false;
		return Global.getSettings().isDevMode();
	}

	
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (reason == FleetDespawnReason.PLAYER_FAR_AWAY) return;
		
		for (SeededFleet curr : fleets) {
			if (curr.fleet != null && curr.fleet == fleet) {
				fleets.remove(curr);
				if (DEBUG) System.out.println("Removed " + curr.fleet.getName() + " (seed: " + curr.seed + "), remaiing: " + fleets.size());
				break;
			}
		}
	}
	
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
	}
}




