package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.LinkedHashMap;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.tutorial.TutorialMissionIntel;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

/**
 * Picks a star system near the player meeting certain criteria and spawns certain types of fleets there,
 * but outside the player's vision.
 * 
 * Despawns them as soon as possible when a different star system is picked.
 *
 * Copyright 2018 Fractal Softworks, LLC
 */
public abstract class DisposableFleetManager extends PlayerVisibleFleetManager {

	public static boolean DEBUG = false;
	
	public static final String KEY_SYSTEM = "$core_disposableFleetSpawnSystem";
	public static final String KEY_SPAWN_FP = "$core_disposableFleetSpawnFP";
	//public static final float MAX_RANGE_FROM_PLAYER_LY = 3f;
	public static final float MAX_RANGE_FROM_PLAYER_LY = RouteManager.SPAWN_DIST_LY;
	public static final float DESPAWN_RANGE_LY = MAX_RANGE_FROM_PLAYER_LY + 1.4f;
	
	protected IntervalUtil tracker2 = new IntervalUtil(0.75f, 1.25f);;
	protected LinkedHashMap<String, TimeoutTracker<Boolean>> recentSpawns = new LinkedHashMap<String, TimeoutTracker<Boolean>>();
	
	protected Object readResolve() {
		super.readResolve();
		return this;
	}
	
	protected float getExpireDaysPerFleet() {
		return 30f;
	}
	
	protected String getSpawnKey(StarSystemAPI system) {
		return "$core_recentSpawn_" + getSpawnId() + "_" + system.getName();
	}
	
	protected void addRecentSpawn(StarSystemAPI system) {
		String key = getSpawnKey(system);
		float e = Global.getSector().getMemoryWithoutUpdate().getExpire(key);
		if (e < 0) e = 0;
		e += getExpireDaysPerFleet();
		Global.getSector().getMemoryWithoutUpdate().set(key, true);
		Global.getSector().getMemoryWithoutUpdate().expire(key, e);
	}
	
	protected float getRecentSpawnsForSystem(StarSystemAPI system) {
		String key = getSpawnKey(system);
		float e = Global.getSector().getMemoryWithoutUpdate().getExpire(key);
		if (e < 0) e = 0;
		return e / getExpireDaysPerFleet();
	}
	
	@Override
	protected int getMaxFleets() {
		return 100; // limiting is based on spawnRateMult instead
	}

	@Override
	protected boolean isOkToDespawnAssumingNotPlayerVisible(CampaignFleetAPI fleet) {
		if (currSpawnLoc == null) return true;
		String system = fleet.getMemoryWithoutUpdate().getString(KEY_SYSTEM);
		float spawnFP = fleet.getMemoryWithoutUpdate().getFloat(KEY_SPAWN_FP);
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		float playerFP = player.getFleetPoints();
		
		if (system == null || !system.equals(currSpawnLoc.getName())) return true;
		
		if (spawnFP >= fleet.getFleetPoints() * 2f) {
			if (fleet.getAI() instanceof CampaignFleetAIAPI) {
				CampaignFleetAIAPI ai = (CampaignFleetAIAPI) fleet.getAI();
				EncounterOption option = ai.pickEncounterOption(null, player, true);
				if (option == EncounterOption.DISENGAGE) return true;
			} else {
				return fleet.getFleetPoints() <= playerFP * 0.5f;
			}
		}
		
		return false;
	}

	@Override
	public float getSpawnRateMult() {
		return spawnRateMult;
	}

	protected float spawnRateMult = 1f;
	protected StarSystemAPI currSpawnLoc = null;
	
	@Override
	public void advance(float amount) {
		if (TutorialMissionIntel.isTutorialInProgress()) {
			return;
		}
		
		super.advance(amount);
		
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		if (DEBUG) {
			days *= 100f;
		}
		
		tracker2.advance(days);
		if (tracker2.intervalElapsed()) {
			StarSystemAPI closest = pickCurrentSpawnLocation();
			if (closest != currSpawnLoc) {
				currSpawnLoc = closest;
			}
			
			//List<ManagedFleetData> remove = new ArrayList<ManagedFleetData>();
			for (ManagedFleetData data : active) {
				if (Misc.isFleetReturningToDespawn(data.fleet)) continue;
				// if it's player-visible/in the currently active location,
				// make it return to source when it's been beat up enough
				// to be worth despawning
				//if (isOkToDespawnAssumingNotPlayerVisible(data.fleet)) {
				
				float fp = data.fleet.getFleetPoints();
				float spawnFP = data.fleet.getMemoryWithoutUpdate().getFloat(KEY_SPAWN_FP);
				if (fp < spawnFP * 0.33f) {
					Misc.giveStandardReturnToSourceAssignments(data.fleet);
					//remove.add(data);
				}
			}
			
			//active.removeAll(remove);
			
			updateSpawnRateMult();
		}
	}
	
	public StarSystemAPI getCurrSpawnLoc() {
		return currSpawnLoc;
	}

	protected void updateSpawnRateMult() {
		if (currSpawnLoc == null) {
			if (DEBUG) {
				System.out.println("No target system, spawnRateMult is 1");
			}
			spawnRateMult = 1f;
			return;
		}
		
		float desiredNumFleets = getDesiredNumFleetsForSpawnLocation();
		float recentSpawns = getRecentSpawnsForSystem(currSpawnLoc);
		if (active != null) {
			float activeInSystem = 0f;
			for (ManagedFleetData data : active) {
				if (data.spawnedFor == currSpawnLoc || data.fleet.getContainingLocation() == currSpawnLoc) {
					activeInSystem++;
				}
			}
			recentSpawns = Math.max(recentSpawns, activeInSystem);
		}
		
		spawnRateMult = (float) Math.pow(Math.max(0, (desiredNumFleets - recentSpawns) * 1f), 4f);
		if (spawnRateMult < 0) spawnRateMult = 0;
		
		//if (DEBUG || this instanceof DisposableHostileActivityFleetManager) {
		if (DEBUG) {
			System.out.println(String.format("ID: %s, system: %s, recent: %s, desired: %s, spawnRateMult: %s",
					getSpawnId(),
					currSpawnLoc.getName(),
					"" + recentSpawns,
					"" + desiredNumFleets,
					"" + spawnRateMult));
		}
	}

	protected abstract int getDesiredNumFleetsForSpawnLocation();
	
	protected abstract CampaignFleetAPI spawnFleetImpl();
	protected abstract String getSpawnId();
	
	protected StarSystemAPI pickCurrentSpawnLocation() {
		return pickNearestPopulatedSystem();
	}
	protected StarSystemAPI pickNearestPopulatedSystem() {
		if (Global.getSector().isInNewGameAdvance()) return null;
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return null;
		StarSystemAPI nearest = null;
		float minDist = Float.MAX_VALUE;
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (market.getStarSystem() != null && market.getStarSystem().hasTag(Tags.SYSTEM_ABYSSAL)) continue;
			
			if (market.isPlayerOwned() && market.getSize() <= 3) continue;
			if (!market.hasSpaceport()) continue;
			
			float distToPlayerLY = Misc.getDistanceLY(player.getLocationInHyperspace(), market.getLocationInHyperspace());
			
			if (distToPlayerLY > MAX_RANGE_FROM_PLAYER_LY) continue;
			
			if (distToPlayerLY < minDist && market.getStarSystem() != null) {
				if (market.getStarSystem().getStar() != null) {
					if (market.getStarSystem().getStar().getSpec().isPulsar()) continue;
				}
				
				nearest = market.getStarSystem();
				minDist = distToPlayerLY;
			}
		}

		
		// stick with current system longer unless something else is closer
		if (nearest == null && currSpawnLoc != null) {
			float distToPlayerLY = Misc.getDistanceLY(player.getLocationInHyperspace(), currSpawnLoc.getLocation());
			if (distToPlayerLY <= DESPAWN_RANGE_LY) {
				nearest = currSpawnLoc;
			}
		}
		
		return nearest;
	}
	
	public CampaignFleetAPI spawnFleet() {
		if (currSpawnLoc == null) return null;
		
		// otherwise, possible for jump-point dialog to say there's nothing on other side
		// but there will be by the time the player comes out
		if (Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().isInHyperspaceTransition()) {
			return null;
		}
		
		CampaignFleetAPI fleet = spawnFleetImpl();
		if (fleet != null) {
			fleet.getMemoryWithoutUpdate().set(KEY_SYSTEM, currSpawnLoc.getName());
			fleet.getMemoryWithoutUpdate().set(KEY_SPAWN_FP, fleet.getFleetPoints());
		}
		
		// do this even if fleet is null, to avoid non-stop fail-spawning of fleets 
		// if spawnFleetImpl() can't spawn one, for whatever reason
		addRecentSpawn(currSpawnLoc);
		updateSpawnRateMult();
		
		return fleet;
	}

	protected String getTravelText(StarSystemAPI system, CampaignFleetAPI fleet) {
		return "traveling to the " + system.getBaseName() + " star system";
	}
	
	protected String getActionInsideText(StarSystemAPI system, CampaignFleetAPI fleet) {
		boolean patrol = fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET);
		String verb = "raiding";
		if (patrol) verb = "patrolling";
		return verb + " the " + system.getBaseName() + " star system";
	}
	
	protected String getActionOutsideText(StarSystemAPI system, CampaignFleetAPI fleet) {
		boolean patrol = fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PATROL_FLEET);
		String verb = "raiding";
		if (patrol) verb = "patrolling";
		return verb + " around the " + system.getBaseName() + " star system";
	}

	protected void setLocationAndOrders(CampaignFleetAPI fleet, float probStartInHyper, float probStayInHyper) {
		StarSystemAPI system = getCurrSpawnLoc();
		
		if ((float) Math.random() < probStartInHyper || 
				(Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().isInHyperspace())) {
			Global.getSector().getHyperspace().addEntity(fleet);
		} else {
			system.addEntity(fleet);
		}
		fleet.addScript(new DisposableAggroAssignmentAI(fleet, system, this, probStayInHyper));
	}
}








