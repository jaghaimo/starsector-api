package com.fs.starfarer.api.impl.campaign.ghosts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.abilities.InterdictionPulseAbility;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.SmoothMovementUtil;

public class BaseSensorGhost implements SensorGhost {
	
	public static enum DespawnReason {
		FLEET_IN_RANGE,
		SCRIPT_ENDED,
	}
	
	protected CustomCampaignEntityAPI entity;
	protected float despawnRange = 100f;
	protected boolean despawnOutsideSector = true;
	protected boolean despawnInAbyss = true;
	protected boolean fleeing = false;
	protected int fleeBurnLevel = 30;
	protected float accelMult = 1f;
	protected transient boolean creationFailed = false;
	
	protected SmoothMovementUtil movement = new SmoothMovementUtil();
	
	protected List<GhostBehavior> script = new ArrayList<GhostBehavior>();
	protected SensorGhostManager manager;
	
	public BaseSensorGhost(SensorGhostManager manager, int fleeBurnLevel) {
		this.manager = manager;
		this.fleeBurnLevel = fleeBurnLevel;
	}

	protected Object readResolve() {
		if (movement == null) {
			movement = new SmoothMovementUtil();
		}
		return this;
	}
	
	public void addBehavior(GhostBehavior b) {
		script.add(b);
	}
	
	public void addInterrupt(GhostBehaviorInterrupt interrupt) {
		if (script.isEmpty()) return;
		script.get(script.size() - 1).addInterrupt(interrupt);
	}
	
	public float getDespawnRange() {
		return despawnRange;
	}

	public void setDespawnRange(float despawnRange) {
		this.despawnRange = despawnRange;
	}

	public Random getRandom() {
		Random random = Misc.random;
		if (manager != null) random = manager.getRandom();
		return random;
	}
	
	public float genSmallSensorProfile() {
		return 700f + getRandom().nextFloat() * 300f;
	}
	public float genMediumSensorProfile() {
		return 1000f + getRandom().nextFloat() * 500f;
	}
	public float genLargeSensorProfile() {
		return 1500f + getRandom().nextFloat() * 500f;
	}
	
	public float genHugeSensorProfile() {
		return 2500f + getRandom().nextFloat() * 1000f;
	}
	
	public float genTinyRadius() {
		return 10f + getRandom().nextFloat() * 5f;
	}
	public float genVerySmallRadius() {
		return 20f + getRandom().nextFloat() * 10f;
	}
	public float genSmallRadius() {
		return 22f + getRandom().nextFloat() * 28f;
	}
	public float genMediumRadius() {
		return 50f + getRandom().nextFloat() * 25f;
	}
	public float genLargeRadius() {
		return 75f + getRandom().nextFloat() * 25f;
	}
	
	public float genFloat(float min, float max) {
		return min + (max - min) * getRandom().nextFloat(); 
	}
	public float genInt(int min, int max) {
		return min + getRandom().nextInt(max - min + 1); 
	}
//	public float genRadius(float min, float max) {
//		return min + (max - min) * getRandom().nextFloat(); 
//	}
//	public int genBurn(int min, int max) {
//		return min + getRandom().nextInt(max - min + 1); 
//	}
	public float genDelay(float base) {
		return base * (0.75f + 0.5f * getRandom().nextFloat()); 
	}
	
	public boolean placeNearPlayer() {
		return placeNearPlayer(1400f, 2200f); // 2000 is max range at which sensor ping plays
		//placeNearPlayer(1000f, 1500f);
	}
	public boolean placeNearPlayer(float minDist, float maxDist) {
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		
		Random random = getRandom();
		Vector2f loc = new Vector2f();
		
		for (int i = 0; i < 20; i++) {
			float r = minDist + random.nextFloat() * (maxDist - minDist); 
			loc = Misc.getPointAtRadius(pf.getLocation(), r, random);
			if (!Misc.isInsideSlipstream(loc, 500f, pf.getContainingLocation())) {
				break;
			}
		}
		if (Misc.isInsideSlipstream(loc, 500f, pf.getContainingLocation())) {
			return false;
		}
		
		getMovement().getLocation().set(loc);
		entity.getLocation().set(movement.getLocation());
		entity.getVelocity().set(movement.getVelocity());
		
		return true;
	}
	
	public void placeNearEntity(SectorEntityToken entity, float minDist, float maxDist) {
		Random random = getRandom();
		Vector2f loc = new Vector2f();
		
		float r = minDist + random.nextFloat() * (maxDist - minDist); 
		loc = Misc.getPointAtRadius(entity.getLocation(), r, random);
		
		getMovement().getLocation().set(loc);
		entity.getLocation().set(movement.getLocation());
		entity.getVelocity().set(movement.getVelocity());
	}
	
	public void setLoc(Vector2f loc) {
		getMovement().getLocation().set(loc);
		entity.getLocation().set(movement.getLocation());
	}
	public void setVel(Vector2f vel) {
		getMovement().getVelocity().set(vel);
		entity.getVelocity().set(movement.getVelocity());
	}
	
	public void initEntity(float sensorProfile, float radius) {
		float maxFleetRadius = Global.getSettings().getFloat("maxFleetSelectionRadius");
		int extraInds = 0; 
		if (radius > maxFleetRadius) {
			extraInds = (int) Math.round((radius - maxFleetRadius) / 20f);
		}
		initEntity(sensorProfile, radius, extraInds);
	}
	public void initEntity(float sensorProfile, float radius, int extraSensorInds) {
		initEntity(sensorProfile, radius, extraSensorInds, Global.getSector().getHyperspace());
	}
	public void initEntity(float sensorProfile, float radius, int extraSensorInds, LocationAPI where) {
		entity = where.addCustomEntity(null, null,
				Entities.SENSOR_GHOST, Factions.NEUTRAL);
		entity.setDiscoverable(true);
		entity.setSensorProfile(sensorProfile);
		entity.setDiscoveryXP(0f);
		entity.setDetectionRangeDetailsOverrideMult(-100f);
		entity.setRadius(radius);
		entity.forceSensorFaderOut();
		
		despawnRange = Math.max(100f, sensorProfile * 0.25f);
		//if (despawnRange > 200f) despawnRange = 200f;
		despawnRange = 100f;
		
		if (extraSensorInds > 0) {
			entity.getMemoryWithoutUpdate().set(MemFlags.EXTRA_SENSOR_INDICATORS, extraSensorInds);
		}
	}
	
	public void setNumSensorIndicators(int min, int max, Random random) {
		if (random == null) random = Misc.random;
		int num = min + random.nextInt(max - min + 1);
		entity.getMemoryWithoutUpdate().set(MemFlags.SENSOR_INDICATORS_OVERRIDE, num);
	}
	
	protected void reportDespawning(DespawnReason reason, Object param) {
		
	}
	
	public void advance(float amount) {
		if (entity == null) {
			return;
		}
		if (!entity.hasTag(Tags.FADING_OUT_AND_EXPIRING)) {
			if (script.isEmpty() ||
					(despawnOutsideSector && Misc.isOutsideSector(entity.getLocation())) ||
					(despawnInAbyss && Misc.isInAbyss(entity))
					) {
				Misc.fadeAndExpire(entity, 1f);
				reportDespawning(DespawnReason.SCRIPT_ENDED, null);
				entity = null;
				return;
			} else {
				for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
					float dist = Misc.getDistance(entity, fleet);
					dist -= entity.getRadius() + fleet.getRadius();
					if (dist < despawnRange) {
						Misc.fadeAndExpire(entity, 1f);
						reportDespawning(DespawnReason.FLEET_IN_RANGE, fleet);
						entity = null;
						return;
					}
				}
			}
		}
		
		if (fleeBurnLevel > 0 && !fleeing &&
				Global.getSector().getMemoryWithoutUpdate().getBoolean(MemFlags.GLOBAL_INTERDICTION_PULSE_JUST_USED_IN_CURRENT_LOCATION)) {
			if (entity.getContainingLocation() != null) {
				for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
					float range = InterdictionPulseAbility.getRange(fleet);
					float dist = Misc.getDistance(fleet.getLocation(), entity.getLocation());
					if (dist > range) continue;
					if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.JUST_DID_INTERDICTION_PULSE)) {
						fleeing = true;
						script.clear();
						entity.addScript(new SpeedReduction(entity, 0.75f));
						addBehavior(new GBStayInPlace(0.1f + 0.2f * Misc.random.nextFloat()));
						addBehavior(new GBGoAwayFrom(3f + Misc.random.nextFloat() * 2f, fleet, fleeBurnLevel));
						break;
					}
				}
			}
		}
		
		if (!script.isEmpty()) {
			GhostBehavior curr = script.get(0);
			curr.advance(amount, this);
			
			if (curr.isDone()) {
				script.remove(curr);
			}
		}

		
		movement.advance(amount);
//		if (this instanceof LeviathanGhost) {
//			Vector2f prev = entity.getLocation();
//			Vector2f next = movement.getLocation();
//			if (Misc.getDistance(prev, next) > 100f) {
//				System.out.println("LOCATION JUMP");
//				movement.advance(amount);
//			}
//		}
		
		
		entity.getLocation().set(movement.getLocation());
		entity.getVelocity().set(movement.getVelocity());
		//entity.getVelocity().set(0f, 0f);
	}
	
	
	public float getAccelMult() {
		return accelMult;
	}

	public void setAccelMult(float accelMult) {
		this.accelMult = accelMult;
	}

	public void moveTo(Vector2f dest, float maxBurn) {
		moveTo(dest, null, maxBurn);
	}
	
	public void moveTo(Vector2f dest, Vector2f destVel, float maxBurn) {
		float speed = Misc.getSpeedForBurnLevel(maxBurn);
		float accelMult = speed / Misc.getSpeedForBurnLevel(20f);;
		if (accelMult < 0.5f) accelMult = 0.5f;
		if (accelMult > 10f) accelMult = 10f;
		movement.setAcceleration(speed * accelMult * this.accelMult);
		movement.setMaxSpeed(speed);
		movement.setDest(dest, destVel);
	}
	
	public int getMaxBurn() {
		return (int) Misc.getBurnLevelForSpeed(movement.getMaxSpeed());
	}
	public int getCurrBurn() {
		return (int) Misc.getBurnLevelForSpeed(entity.getVelocity().length());
	}
	
	public float getAcceleration() {
		return movement.getAcceleration();
	}
	
	public SmoothMovementUtil getMovement() {
		return movement;
	}
	
	public CustomCampaignEntityAPI getEntity() {
		return entity;
	}

	public boolean isDone() {
		return entity == null;
	}

	public boolean runWhilePaused() {
		return false;
	}

	public boolean isDespawnOutsideSector() {
		return despawnOutsideSector;
	}

	public void setDespawnOutsideSector(boolean despawnOutsideSector) {
		this.despawnOutsideSector = despawnOutsideSector;
	}
	
	public boolean isDespawnInAbyss() {
		return despawnInAbyss;
	}

	public void setDespawnInAbyss(boolean despawnInAbyss) {
		this.despawnInAbyss = despawnInAbyss;
	}

	public boolean isCreationFailed() {
		return creationFailed;
	}

	public void setCreationFailed() {
		this.creationFailed = true;
		if (entity != null && entity.getContainingLocation() != null) {
			entity.getContainingLocation().removeEntity(entity);
		}
	}

	public List<GhostBehavior> getScript() {
		return script;
	}
	
	public void clearScript() {
		script.clear();
	}
}



