package com.fs.starfarer.api.impl.combat.threat;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ThreatShipConstructionScript extends BaseEveryFrameCombatPlugin {
	
	public static String SWARM_CONSTRUCTING_SHIP = "swarm_constructing_ship";
	public static String SHIP_UNDER_CONSTRUCTION = "ship_under_construction";
	
	public static float FADE_IN_RATE_MULT_WHEN_DESTROYED = 10f;
	
	protected float elapsed = 0f;
	protected ShipAPI ship = null;
	protected CollisionClass collisionClass;
	
	protected String variantId;
	protected ShipAPI source;
	protected float delay;
	protected float fadeInTime;
	protected float origMaxSpeed = 500f;
	protected List<ShipAPI> explodedPieces = new ArrayList<>();
	
	protected IntervalUtil interval = new IntervalUtil(0.075f, 0.125f);
	
	public ThreatShipConstructionScript(String variantId, ShipAPI source, float delay, float fadeInTime) {
		this.variantId = variantId;
		this.source = source;
		this.delay = delay;
		this.fadeInTime = fadeInTime;
		
		interval.forceIntervalElapsed();
		
		spawnShip();
	}
	
	public ShipAPI getShip() {
		return ship;
	}

	protected void spawnShip() {
		float facing = source.getFacing() + 15f * ((float) Math.random() - 0.5f);
		
		Vector2f loc = new Vector2f(source.getLocation());
		
		CombatEngineAPI engine = Global.getCombatEngine();
		CombatFleetManagerAPI fleetManager = engine.getFleetManager(source.getOriginalOwner());
		boolean wasSuppressed = fleetManager.isSuppressDeploymentMessages();
		fleetManager.setSuppressDeploymentMessages(true);
	
		ship = engine.getFleetManager(source.getOriginalOwner()).spawnShipOrWing(variantId, loc, facing, 0f, null);
		if (Global.getCombatEngine().isInCampaign() || Global.getCombatEngine().isInCampaignSim()) {
			FactionAPI faction = Global.getSector().getFaction(Factions.THREAT);
			if (faction != null) {
				String name = faction.pickRandomShipName();
				ship.setName(name);
			}
		}
		fleetManager.setSuppressDeploymentMessages(wasSuppressed);
		collisionClass = ship.getCollisionClass();
		
		
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
		RoilingSwarmEffect sourceSwarm = RoilingSwarmEffect.getSwarmFor(source);
		if (swarm != null) {
			swarm.params.withInitialMembers = false;
			swarm.params.withRespawn = false;
		}
		if (sourceSwarm != null) {
			origMaxSpeed = sourceSwarm.params.maxSpeed;
//			sourceSwarm.params.maxSpeed *= 0.25f;
			sourceSwarm.params.outspeedAttachedEntityBy = 0f;
			if (swarm != null) {
				swarm.params.withInitialMembers = false;
				swarm.params.flashFringeColor	= sourceSwarm.params.flashFringeColor;
			}
		}
		
		ship.addTag(SHIP_UNDER_CONSTRUCTION);
		source.addTag(SWARM_CONSTRUCTING_SHIP);
		source.setCollisionClass(CollisionClass.NONE);
		source.getMutableStats().getHullDamageTakenMult().modifyMult("ThreatShipConstructionScript", 0f);

		ship.setShipAI(null);
		for (WeaponGroupAPI g : ship.getWeaponGroupsCopy()) {
			g.toggleOff();
		}
	}
	
	protected float hulkFor = 0f;
	
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		if (Global.getCombatEngine().isPaused()) return;
	
		if (ship.isHulk()) {
			hulkFor += amount;
			amount *= FADE_IN_RATE_MULT_WHEN_DESTROYED;
			// ship splitting into pieces doesn't happen immediately
			if (explodedPieces.isEmpty() || hulkFor < 0.25f) {
				explodedPieces.clear();
				for (ShipAPI curr : Global.getCombatEngine().getShips()) {
					if (curr.getFleetMember() == ship.getFleetMember() ||
							(curr.getParentPieceId() != null && curr.getParentPieceId().equals(ship.getId()))) {
						explodedPieces.add(curr);
					}
				}
			}
			//elapsed += delay + fadeInTime; // instant fade in while hidden by the explosion
		}
		elapsed += amount;
		if (elapsed < delay) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();

		float progress = (elapsed - delay) / fadeInTime;
		if (progress > 1f) progress = 1f;
		
		float remaining = fadeInTime - (elapsed - delay);
		
		ship.setAlphaMult(progress);
//		if (!explodedPieces.isEmpty()) {
//			System.out.println("Pieces: " + explodedPieces.size());
//		}
		for (ShipAPI curr : explodedPieces) {
			curr.setAlphaMult(progress);
		}
		ship.getMutableStats().getEffectiveArmorBonus().modifyMult("ThreatShipConstructionScript", progress * progress);
		
		Global.getSoundPlayer().playLoop("construction_swarm_loop", ship, 1f, 1f, ship.getLocation(), ship.getVelocity());
		
		
		
		if (remaining > 1f) {
			Vector2f deltaLoc = Vector2f.sub(ship.getLocation(), source.getLocation(), new Vector2f());
			source.getLocation().set(ship.getLocation());
			RoilingSwarmEffect sourceSwarm = RoilingSwarmEffect.getSwarmFor(source);
			if (sourceSwarm != null) {
				for (SwarmMember p : sourceSwarm.members) {
					Vector2f.add(p.loc, deltaLoc, p.loc);
				}
			}
			ship.giveCommand(ShipCommand.DECELERATE, null, 0);
		}
		
		float jitterLevel = progress;
		if (fadeInTime <= 4f) {
			if (jitterLevel < 0.5f) {
				jitterLevel *= 2f;
			} else {
				jitterLevel = (1f - jitterLevel) * 2f;
			}
		} else {
			if (jitterLevel < 0.5f) {
				jitterLevel *= 2f;
			} else if (remaining <= 2f) {
				jitterLevel = remaining / 2f;
			} else {
				jitterLevel = 1f;
			}
		}
		jitterLevel = (float) Math.sqrt(jitterLevel);
		
		//float jitterRange = 1f - progress;
		float jitterRange = 1f;
		if (remaining < 2f) {
			jitterRange = remaining / 2f;
		} else {
			jitterRange = (elapsed - delay) / Math.max(1f, fadeInTime - 2f);
		}
		float maxRangeBonus = 25f;
		float jitterRangeBonus = jitterRange * maxRangeBonus;
		
		Color c = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;
		c = Misc.setAlpha(c, 127);
		
		ship.setJitter(this, c, jitterLevel, 3, 0f, jitterRangeBonus);
		ship.getEngineController().fadeToOtherColor(this, Misc.zeroColor, Misc.zeroColor, 1f, 1f);
		
		
		RoilingSwarmEffect sourceSwarm = RoilingSwarmEffect.getSwarmFor(source);
		if (sourceSwarm != null) {
			float speedMult = 0.25f + 0.75f * Math.max(0f, 1f - (elapsed - delay) / 2f);
			sourceSwarm.params.maxSpeed = origMaxSpeed * speedMult;
					
			if (remaining > 3f) {
				float numFragMult = sourceSwarm.params.initialMembers / 150f;
				if (numFragMult < 0.25f) numFragMult = 0.25f;
				if (numFragMult > 1f) numFragMult = 1f;
				sourceSwarm.params.flashFrequency = 5f * progress * 2f * numFragMult;
				sourceSwarm.params.flashProbability = 1f;
				sourceSwarm.params.flashFringeColor = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;
				sourceSwarm.params.flashFringeColor = Misc.setAlpha(sourceSwarm.params.flashFringeColor, 200);
				//sourceSwarm.params.flashCoreRadiusMult = 0f;
				sourceSwarm.params.flashCoreRadiusMult = 1.5f;
				sourceSwarm.params.flashRadius = 50f;
				sourceSwarm.params.renderFlashOnSameLayer = true;
			} else {
				sourceSwarm.params.flashFrequency = 1f;
				sourceSwarm.params.flashProbability = 0f;
			}
		}
		
		
		spawnParticles(amount);
		

		if (elapsed > fadeInTime + delay) {
			ship.setDefaultAI(null);
			ship.removeTag(SHIP_UNDER_CONSTRUCTION);
			ship.setAlphaMult(1f);
			ship.setHoldFire(false);
			ship.setCollisionClass(collisionClass);
			ship.getMutableStats().getEffectiveArmorBonus().unmodifyMult("ThreatShipConstructionScript");
			engine.removePlugin(this);
			
			if (sourceSwarm != null) {
				sourceSwarm.getParams().despawnSound = null;
			}
			
			RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(ship);
			//RoilingSwarmEffect sourceSwarm = RoilingSwarmEffect.getSwarmFor(source);
			if (swarm != null && sourceSwarm != null) {
				int transfer = Math.min(swarm.params.baseMembersToMaintain, sourceSwarm.getNumActiveMembers());
				sourceSwarm.transferMembersTo(swarm, transfer);
			}
			if (swarm != null) {
				swarm.params.withRespawn = true; 
				swarm.params.withInitialMembers = true; 
			}
			
			source.setHitpoints(0f);
			source.setSpawnDebris(false);
			engine.applyDamage(source, source.getLocation(), 100f, DamageType.ENERGY, 0f, true, false, source, false);
		}
	}
	
	protected void spawnParticles(float amount) {
		if (ship == null) return;
		
		float remaining = fadeInTime - (elapsed - delay);
		
		interval.advance(amount);
		if (interval.intervalElapsed() && remaining > 1f) {
			
			RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(source);
			if (swarm != null) {
				for (SwarmMember p : swarm.members) {
					if ((float) Math.random() > 0.9f) {
						p.rollOffset(swarm.params, ship);
					}
				}
			}
			
			CombatEngineAPI engine = Global.getCombatEngine();
			
			Color c = RiftLanceEffect.getColorForDarkening(VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR);
			c = Misc.setAlpha(c, 50);
			float baseDuration = 2f;
			Vector2f vel = new Vector2f(ship.getVelocity());
			//float size = ship.getCollisionRadius() * 0.35f;
			float size = ship.getCollisionRadius() * 0.33f;
			
			float extraDur = 0f;
			if (remaining < 1f) extraDur = 1f;
			
			//for (int i = 0; i < 3; i++) {
			for (int i = 0; i < 11; i++) {
				Vector2f point = new Vector2f(ship.getLocation());
				point = Misc.getPointWithinRadiusUniform(point, ship.getCollisionRadius() * 0.75f, Misc.random);
				float dur = baseDuration + baseDuration * (float) Math.random();
				dur += extraDur;
				float nSize = size;
				Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
				Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
				v.scale(nSize + nSize * (float) Math.random() * 0.5f);
				v.scale(0.2f);
				Vector2f.add(vel, v, v);
				
				float maxSpeed = nSize * 1.5f * 0.2f; 
				float minSpeed = nSize * 1f * 0.2f; 
				float overMin = v.length() - minSpeed;
				if (overMin > 0) {
					float durMult = 1f - overMin / (maxSpeed - minSpeed);
					if (durMult < 0.1f) durMult = 0.1f;
					dur *= 0.5f + 0.5f * durMult;
				}
				engine.addNegativeNebulaParticle(pt, v, nSize * 1f, 2f,
												0.5f / dur, 0f, dur, c);
			}
		}
	}
	
	
}
