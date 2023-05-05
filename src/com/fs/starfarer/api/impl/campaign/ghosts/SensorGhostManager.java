package com.fs.starfarer.api.impl.campaign.ghosts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ghosts.types.ChargerGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.EchoGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.EncounterTricksterGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.GuideGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.LeviathanCalfGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.LeviathanGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.MinnowGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.NoGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.RacerGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.RemnantGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.RemoraGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.ShipGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.StormTricksterGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.StormcallerGhostCreator;
import com.fs.starfarer.api.impl.campaign.ghosts.types.ZigguratGhostCreator;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class SensorGhostManager implements EveryFrameScript {

	public static List<SensorGhostCreator> CREATORS = new ArrayList<SensorGhostCreator>();
	static {
		//CREATORS.add(new TestGhostCreator());
		CREATORS.add(new ChargerGhostCreator());
		CREATORS.add(new EchoGhostCreator());
		CREATORS.add(new EncounterTricksterGhostCreator());
		CREATORS.add(new GuideGhostCreator());
		CREATORS.add(new LeviathanGhostCreator());
		CREATORS.add(new LeviathanCalfGhostCreator());
		CREATORS.add(new MinnowGhostCreator());
		CREATORS.add(new NoGhostCreator());
		CREATORS.add(new RacerGhostCreator());
		CREATORS.add(new RemnantGhostCreator());
		CREATORS.add(new RemoraGhostCreator());
		CREATORS.add(new ShipGhostCreator());
		CREATORS.add(new StormcallerGhostCreator());
		CREATORS.add(new StormTricksterGhostCreator());
		CREATORS.add(new ZigguratGhostCreator());
	}
	
	
	public static float GHOST_SPAWN_RATE_MULT = 0.75f;
	
	public static float SB_ATTRACT_GHOSTS_PROBABILITY = 0.5f;
	public static float SB_FAILED_TO_ATTRACT_TIMEOUT_MULT = 0.25f;
	public static float MIN_SB_TIMEOUT = 5f;
	public static float MAX_SB_TIMEOUT = 20f;
	public static float MIN_FULL_GHOST_TIMEOUT_DAYS = 10f;
	public static float MAX_FULL_GHOST_TIMEOUT_DAYS = 40f;
	public static float MIN_SHORT_GHOST_TIMEOUT_DAYS = 0f;
	public static float MAX_SHORT_GHOST_TIMEOUT_DAYS = 0.2f;
	public static float FULL_TIMEOUT_TRIGGER_PROBABILITY = 0.95f; // chance spawning a ghost triggers the full timeout
	
	
	public static float MIN_FAILED_CREATOR_TIMEOUT_DAYS = 0.8f;
	public static float MAX_FAILED_CREATOR_TIMEOUT_DAYS = 1.2f;
	
	
	protected TimeoutTracker<String> perCreatorTimeouts = new TimeoutTracker<String>();
	protected float timeoutRemaining = 0f;
	protected float sbTimeoutRemaining = 0f;
	protected Random random = new Random(Misc.genRandomSeed());
	protected List<SensorGhost> ghosts = new ArrayList<SensorGhost>();
	protected boolean spawnTriggeredBySensorBurst = false;

	public static SensorGhostManager getGhostManager() {
		String ghostManagerKey = "$ghostManager";
		SensorGhostManager manager = (SensorGhostManager) Global.getSector().getMemoryWithoutUpdate().get(ghostManagerKey);
		if (manager == null) {
			for (EveryFrameScript curr : Global.getSector().getScripts()) {
				if (curr instanceof SensorGhostManager) {
					manager = (SensorGhostManager) curr;
					Global.getSector().getMemoryWithoutUpdate().set(ghostManagerKey, manager);
					break;
				}
			}
		}
		return manager;
	}

	public static SensorGhost getGhostFor(SectorEntityToken entity) {
		SensorGhostManager manager = getGhostManager();
		if (manager == null) return null;
		
		for (SensorGhost ghost : manager.ghosts) {
			if (ghost.getEntity() == entity) {
				return ghost;
			}
		}
		return null;
	}
	
	public void advance(float amount) {
		if (amount == 0) return;
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		if (pf == null) return;
		
		float days = Global.getSector().getClock().convertToDays(amount);
		perCreatorTimeouts.advance(days);
		
		sbTimeoutRemaining -= days;
		if (sbTimeoutRemaining <= 0f) {
			sbTimeoutRemaining = 0f;
			checkSensorBursts();
		}
		
		timeoutRemaining -= days * GHOST_SPAWN_RATE_MULT;
		if (timeoutRemaining <= 0f) {
			spawnGhost();
			spawnTriggeredBySensorBurst = false;
		}
		
		Iterator<SensorGhost> iter = ghosts.iterator();
		while (iter.hasNext()) {
			SensorGhost curr = iter.next();
			curr.advance(amount);
			if (curr.isDone()) {
				iter.remove();
			}
		}
	}
	
	public boolean isSpawnTriggeredBySensorBurst() {
		return spawnTriggeredBySensorBurst;
	}

	public void checkSensorBursts() {
		if (!Global.getSector().getCurrentLocation().isHyperspace()) return;
		if (timeoutRemaining < 1f) return;
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean(MemFlags.GLOBAL_SENSOR_BURST_JUST_USED_IN_CURRENT_LOCATION)) {
			if (random.nextFloat() > SB_ATTRACT_GHOSTS_PROBABILITY) {
				sbTimeoutRemaining = MIN_SB_TIMEOUT + (MAX_SB_TIMEOUT - MIN_SB_TIMEOUT) * random.nextFloat();
				sbTimeoutRemaining *= SB_FAILED_TO_ATTRACT_TIMEOUT_MULT;
				return;
			}
			CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
			float range = 2000f;
			for (CampaignFleetAPI fleet : Global.getSector().getCurrentLocation().getFleets()) {
				float dist = Misc.getDistance(fleet.getLocation(), pf.getLocation());
				if (dist > range) continue;
				if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.JUST_DID_SENSOR_BURST)) {
					timeoutRemaining = 0.2f + 0.8f * random.nextFloat();
					spawnTriggeredBySensorBurst = true;
					sbTimeoutRemaining = MIN_SB_TIMEOUT + (MAX_SB_TIMEOUT - MIN_SB_TIMEOUT) * random.nextFloat();
					break;
				}
			}
		}
	}
	
	public void spawnGhost() {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		boolean nearStream = Misc.isInsideSlipstream(pf.getLocation(), 1000f, pf.getContainingLocation());
		
		WeightedRandomPicker<SensorGhostCreator> picker = new WeightedRandomPicker<SensorGhostCreator>(random);
		for (SensorGhostCreator creator : CREATORS) {
			if (perCreatorTimeouts.contains(creator.getId())) continue;
			if (nearStream && !creator.canSpawnWhilePlayerInOrNearSlipstream()) continue;
			
			float freq = creator.getFrequency(this);
			picker.add(creator, freq);
		}
		
		SensorGhostCreator creator = picker.pick();
		if (creator == null) return;
		
		//System.out.println("Picked: " + creator.getId());
		
		boolean canSpawn = true;
		// important: the creator that can't spawn a ghost can still be picked, just won't fire
		// otherwise moving in/out of slipstreams would manipulate ghost spawning
		// can still manipulate it since it causes a "failed to create" timeout rather than a "created" one,
		// but that should be a bit less noticeable
//		if (!creator.canSpawnWhilePlayerInOrNearSlipstream()) {
//			//CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
//			canSpawn = !Misc.isInsideSlipstream(pf.getLocation(), 1000f, pf.getContainingLocation());
//		}
		
		List<SensorGhost> ghosts = null;
		if (canSpawn) {
			ghosts = creator.createGhost(this);
		}
		boolean anyFailed = false; // bit of a failsafe if a creator returns a failed-to-spawn ghost
		if (ghosts != null) {
			for (SensorGhost curr : ghosts) {
				anyFailed |= curr.isCreationFailed();
			}
		}
		if (!canSpawn) {
			anyFailed = true;
		}
		
		if (ghosts == null || ghosts.isEmpty() || anyFailed) {
			float timeout = MIN_FAILED_CREATOR_TIMEOUT_DAYS + 
					random.nextFloat() * (MAX_FAILED_CREATOR_TIMEOUT_DAYS - MIN_FAILED_CREATOR_TIMEOUT_DAYS);
			perCreatorTimeouts.set(creator.getId(), timeout);
		} else {
			this.ghosts.addAll(ghosts);
			if (random.nextFloat() < FULL_TIMEOUT_TRIGGER_PROBABILITY) {
				timeoutRemaining = MIN_FULL_GHOST_TIMEOUT_DAYS + 
							random.nextFloat() * (MAX_FULL_GHOST_TIMEOUT_DAYS - MIN_FULL_GHOST_TIMEOUT_DAYS);
			} else {
				timeoutRemaining = MIN_SHORT_GHOST_TIMEOUT_DAYS + 
						random.nextFloat() * (MAX_SHORT_GHOST_TIMEOUT_DAYS - MIN_SHORT_GHOST_TIMEOUT_DAYS);
			}
			perCreatorTimeouts.set(creator.getId(), creator.getTimeoutDaysOnSuccessfulCreate(this));
		}
	}
	
	
	public boolean hasGhostOfClass(Class<?> clazz) {
		for (SensorGhost ghost : ghosts) {
			if (clazz.isInstance(ghost)) return true;
		}
		return false;
	}
	
	public Random getRandom() {
		return random;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
	public boolean isDone() {
		return false;
	}

	public List<SensorGhost> getGhosts() {
		return ghosts;
	}

	
}
