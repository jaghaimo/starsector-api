package com.fs.starfarer.api.impl.combat.threat;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class SwarmLauncherEffect implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin, SwarmLaunchingWeapon {

	public static String CONSTRUCTION_SWARM_WING = "construction_swarm_wing";
	public static String CONSTRUCTION_SWARM_VARIANT = "attack_swarm_Construction";
	public static String RECLAMATION_SWARM_WING = "reclamation_swarm_wing";
	public static String RECLAMATION_SWARM_VARIANT = "attack_swarm_Reclamation";
	
	public static String ATTACK_SWARM_HULL = "attack_swarm";
	public static String ATTACK_SWARM_WING = "attack_swarm_wing";
	public static String ATTACK_SWARM_VARIANT = "attack_swarm_Attack";
	public static String SWARM_LAUNCHER = "swarm_launcher";
	public static float IMPACT_VOLUME_MULT = 0.33f;
	
	public static float INITIAL_SPAWN_DELAY = 1f;
	
	public static Map<String, Integer> FRAGMENT_NUM = new HashMap<>();
	public static Map<String, Integer> SWARM_RADIUS = new HashMap<>();
	public static Map<String, Integer> WING_SIZE = new HashMap<>();
	public static Map<String, String> WING_IDS = new HashMap<>();
	
	static {
		FRAGMENT_NUM.put(ATTACK_SWARM_WING, 50);
		SWARM_RADIUS.put(ATTACK_SWARM_WING, 20);
		WING_SIZE.put(ATTACK_SWARM_WING, 4);
		WING_IDS.put(SWARM_LAUNCHER, ATTACK_SWARM_WING);
		
		FRAGMENT_NUM.put(CONSTRUCTION_SWARM_WING, 50);
		SWARM_RADIUS.put(CONSTRUCTION_SWARM_WING, 50);
	}
	
	protected FighterWingAPI currWing = null;
	protected boolean waitUntilOneLeft = false;
	protected float elapsed = 0f;
	
	public SwarmLauncherEffect() {
		
	}
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		ShipAPI ship = weapon.getShip();
		if (ship == null) return;

		elapsed += amount;
		if (elapsed < INITIAL_SPAWN_DELAY || ship.hasTag(ThreatShipConstructionScript.SHIP_UNDER_CONSTRUCTION)) {
			weapon.setForceDisabled(true);
			return;
		}
		
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		int active = swarm == null ? 0 : swarm.getNumActiveMembers();
		int required = FRAGMENT_NUM.get(getWingId(weapon));
		boolean disable = active < required;
		//disable = false;
		int max = getWingSize(weapon);
		int swarmsActive = 0;
		if (currWing != null) swarmsActive = currWing.getWingMembers().size();
		waitUntilOneLeft |= currWing != null && swarmsActive >= max;
		if (currWing != null && currWing.getWingMembers().size() == 1 && currWing.getLeader() != null) {
			float dist = Misc.getDistance(currWing.getLeader().getLocation(), weapon.getLocation());
			if (dist > 1000f) {
				waitUntilOneLeft = true;
			}
		}
		if (waitUntilOneLeft) {
			if (currWing != null && ((swarmsActive <= 1 && max > 1) || swarmsActive <= 0 && max <= 1)) {
				waitUntilOneLeft = false;
				currWing = null;
			} else {
				disable = true;
			}
		}
		
		if (currWing == null && swarm != null) {
			int preferred = getPreferredNumFragmentsToFireConsideringAllWeapons(ship);
			preferred = (int) Math.min(preferred, swarm.params.baseMembersToMaintain * 0.9f);
			if (active < preferred ) {
				disable = true;
			}
		}
		
		weapon.setForceDisabled(disable);
		
		boolean playerShip = Global.getCurrentState() == GameState.COMBAT &&
				Global.getCombatEngine() != null && Global.getCombatEngine().getPlayerShip() == ship;
		if (playerShip) {
			Global.getCombatEngine().maintainStatusForPlayerShip(this,
					Global.getSettings().getSpriteName("ui", "icon_tactical_swarm_launcher"),
					weapon.getDisplayName(), 
					"SWARMS ACTIVE: " + swarmsActive, // + " / " + max,
					swarmsActive <= 0);
		}
		
		weapon.setCustom(currWing);
		
		if (ship.getVariant().hasHullMod(HullMods.THREAT_HULLMOD)) {
			weapon.setAmmo(weapon.getMaxAmmo());
		}
		
		if (disable) {
			return;
		}
		
		weapon.setForceFireOneFrame(true);
	}
	
	public String getWingId(WeaponAPI weapon) {
		//String wingId = WING_IDS.get(SWARM_LAUNCHER);
		String wingId = WING_IDS.get(weapon.getId());
		if (wingId != null) return wingId;
		return ATTACK_SWARM_WING;
	}
	
	public int getWingSize(WeaponAPI weapon) {
		float wingSize = WING_SIZE.get(getWingId(weapon));
		if (weapon.getShip() != null) {
			wingSize = weapon.getShip().getMutableStats().getDynamic().getValue(
					Stats.SWARM_LAUNCHER_WING_SIZE_MOD, wingSize);
		}
		//wingSize = 20;
		//wingSize = 1;
		return (int) Math.round(wingSize);
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
		//FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(ATTACK_SWARM_WING);

		CombatFleetManagerAPI manager = engine.getFleetManager(projectile.getOwner());
		manager.setSuppressDeploymentMessages(true);
		ShipAPI leader = manager.spawnShipOrWing(getWingId(weapon), 
								projectile.getLocation(), projectile.getFacing(), 0f, null);
		leader.getWing().setSourceShip(projectile.getSource());
		manager.setSuppressDeploymentMessages(false);
		
		Vector2f takeoffVel = Misc.getUnitVectorAtDegreeAngle(projectile.getFacing());
		takeoffVel.scale(leader.getMaxSpeed() * 1f);
		
		//Global.getSoundPlayer().playSound("threat_swarm_launched", 1f, 1f, projectile.getLocation(), takeoffVel);
		
		for (ShipAPI curr : leader.getWing().getWingMembers()) {
			curr.setDoNotRender(true);
			curr.setExplosionScale(0f);
			curr.setHulkChanceOverride(0f);
			curr.setImpactVolumeMult(IMPACT_VOLUME_MULT);
			curr.getArmorGrid().clearComponentMap(); // no damage to weapons/engines

			if (currWing != null) {
				if (curr.getWing() != null) {
					curr.getWing().removeMember(curr);
					// really important, otherwise this doesn't get cleaned up
					manager.removeDeployed(curr.getWing(), false);
				}
				curr.setWing(currWing);
				currWing.addMember(curr);
			}
			
			Vector2f.add(curr.getVelocity(), takeoffVel, curr.getVelocity());
		}
		
		currWing = leader.getWing();
		
		RoilingSwarmEffect sourceSwarm = RoilingSwarmEffect.getSwarmFor(weapon.getShip());
		if (sourceSwarm != null) {
			RoilingSwarmEffect swarm = FragmentSwarmHullmod.createSwarmFor(leader);
			
			int required = FRAGMENT_NUM.get(getWingId(weapon));
			int transfer = required;
			
			if (transfer > 0) {
				Vector2f loc = new Vector2f(takeoffVel);
				loc.scale(0.5f);
				Vector2f.add(loc, leader.getLocation(), loc);
				sourceSwarm.transferMembersTo(swarm, transfer, loc, 100f);
			}
			
		}
		
//		if (currWing.getWingMembers().size() >= getWingSize(weapon)) {
//			currWing = null;
//		}
		
		engine.removeEntity(projectile);
	}
	
	public int getPreferredNumFragmentsToFireConsideringAllWeapons(ShipAPI ship) {
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		if (swarm == null) return 0;
		
		int req = 0;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getEffectPlugin() instanceof SwarmLaunchingWeapon) {
				if (w.isFiring()) continue;
				SwarmLaunchingWeapon effect = (SwarmLaunchingWeapon) w.getEffectPlugin();
				req += effect.getPreferredNumFragmentsToFire(w);
			}
		}
		return req;
	}

	@Override
	public int getPreferredNumFragmentsToFire(WeaponAPI weapon) {
		Integer required = FRAGMENT_NUM.get(getWingId(weapon));
		if (required == null) return 0;
		int wingSize = getWingSize(weapon);
		int num = Math.min(wingSize, 2);
		return required * num;
	}
	
	
//	
//	public static RoilingSwarmEffect createTestDwellerSwarmFor(ShipAPI ship) {
//		RoilingSwarmEffect existing = RoilingSwarmEffect.getSwarmFor(ship);
//		if (existing != null) {
//			if (!"dweller_pieces".equals(existing.params.spriteKey)) {
//				existing.setForceDespawn(true);
//			}
//			//return existing;
//		}
//		
//		RoilingSwarmParams params = new RoilingSwarmParams();
//		float radius = 20f;
//		int numMembers = 50;
//
//		
////		"fx_particles1":"graphics/fx/fx_clouds00.png",
////		"fx_particles2":"graphics/fx/fx_clouds01.png",
////		"nebula_particles":"graphics/fx/nebula_colorless.png",
////		"nebula_particles2":"graphics/fx/cleaner_clouds00.png",
////		"dust_particles":"graphics/fx/dust_clouds_colorless.png",
//		
////		params.spriteKey = "dust_particles";
//		params.spriteKey = "fx_particles1";
//		params.spriteKey = "nebula_particles";
//		params.spriteKey = "dweller_pieces";
//		
//		params.baseDur = 1f;
//		params.durRange = 2f;
//		params.memberRespawnRate = 100f;
//		
//		params.memberExchangeClass = null;
//		params.flockingClass = null;
//		params.maxSpeed = ship.getMaxSpeedWithoutBoost() + 
//					Math.max(ship.getMaxSpeedWithoutBoost() * 0.25f + 50f, 100f);
//		
//		params.baseSpriteSize = 256f;
//		params.baseSpriteSize = 128f * 1.5f * 0.67f;
//		params.maxTurnRate = 120f;
//		
//		radius = 100f;
//		numMembers = 100;
//		
//		params.flashCoreRadiusMult = 0f;
//		//params.flashRadius = 0f;
//		params.flashRadius = 200f;
//		params.flashRateMult = 0.1f;
//		params.flashFrequency = 10f;
//		params.flashProbability = 1f;
//		
//		
//		params.alphaMult = 1f;
//		params.alphaMultBase = 0.1f;
//		params.alphaMultFlash = 1f;
//		params.color = RiftCascadeEffect.STANDARD_RIFT_COLOR;
//		params.color = Misc.setAlpha(RiftCascadeEffect.EXPLOSION_UNDERCOLOR, 255);
//		params.color = Misc.setBrightness(params.color, 155);
//		params.flashFringeColor = params.color;
//		params.flashCoreColor = params.color;
//		
//		params.color = new Color(100, 50, 255, 255);
//		params.flashFringeColor = new Color(100, 100, 255, 255);
//		params.flashCoreColor = new Color(100, 100, 255, 255);
//		
//		//params.renderFlashOnSameLayer = true;
//		
//		params.maxOffset = radius;
//		
//		//params.despawnDist = params.maxOffset + 300f;
//		
//		params.initialMembers = numMembers;
//		params.baseMembersToMaintain = params.initialMembers;
//		
//		//params.springStretchMult = 1f;
//		
//		
//		return new RoilingSwarmEffect(ship, params) {
//			protected IntervalUtil interval = new IntervalUtil(0.075f, 0.125f);
//			
//			@Override
//			public int getNumMembersToMaintain() {
//				return super.getNumMembersToMaintain();
//			}
//
//			@Override
//			public void advance(float amount) {
//				super.advance(amount);
//
//				interval.advance(amount);
//				if (interval.intervalElapsed() && false) {
//					
//					CombatEngineAPI engine = Global.getCombatEngine();
//					
//					//Color c = RiftLanceEffect.getColorForDarkening(VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR);
//					Color c = RiftLanceEffect.getColorForDarkening(params.color);
//					c = Misc.setAlpha(c, 50);
//					float baseDuration = 2f;
//					Vector2f vel = new Vector2f(ship.getVelocity());
//					
//					float baseSize = params.maxOffset * 2f;
//					
//					//float size = ship.getCollisionRadius() * 0.35f;
//					float size = baseSize * 0.33f;
//					
//					float extraDur = 0f;
//					
//					//for (int i = 0; i < 3; i++) {
//					for (int i = 0; i < 11; i++) {
//						Vector2f point = new Vector2f(ship.getLocation());
//						point = Misc.getPointWithinRadiusUniform(point, baseSize * 0.75f, Misc.random);
//						float dur = baseDuration + baseDuration * (float) Math.random();
//						dur += extraDur;
//						float nSize = size;
//						Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
//						Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
//						v.scale(nSize + nSize * (float) Math.random() * 0.5f);
//						v.scale(0.2f);
//						Vector2f.add(vel, v, v);
//						
//						float maxSpeed = nSize * 1.5f * 0.2f; 
//						float minSpeed = nSize * 1f * 0.2f; 
//						float overMin = v.length() - minSpeed;
//						if (overMin > 0) {
//							float durMult = 1f - overMin / (maxSpeed - minSpeed);
//							if (durMult < 0.1f) durMult = 0.1f;
//							dur *= 0.5f + 0.5f * durMult;
//						}
//						engine.addNegativeNebulaParticle(pt, v, nSize * 1f, 2f,
//														0.5f / dur, 0f, dur, c);
//					}
//				}
//				
//			}
//			
//		};
//	}
}








