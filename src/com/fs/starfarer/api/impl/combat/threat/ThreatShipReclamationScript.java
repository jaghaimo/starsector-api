package com.fs.starfarer.api.impl.combat.threat;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BoundsAPI.SegmentAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript.SwarmConstructableVariant;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ThreatShipReclamationScript extends BaseEveryFrameCombatPlugin {
	
	public static float CR_PER_RECLAMATION_SWARM = 0.02f;
	
	public static float RECLAMATION_SWARM_SPEED_MULT = 0.67f;
	public static float RECLAMATION_SWARM_COLLISION_MULT = 1.5f;
	public static float RECLAMATION_SWARM_RADIUS_MULT = 2f;
	public static float RECLAMATION_SWARM_HP_MULT = 2f;
	public static float RECLAMATION_SWARM_FRAGMENT_SIZE_MULT = 0.67f;
	
	
	
	protected float elapsed = 0f;
	protected ShipAPI primary = null;
	protected List<ShipAPI> pieces = new ArrayList<>();
	protected List<ShipAPI> swarms = new ArrayList<>();
	protected float delay;
	protected float fadeOutTime;
	protected float origMaxSpeed = 500f;
	
	protected IntervalUtil interval = new IntervalUtil(0.075f, 0.125f);
	protected IntervalUtil interval2 = new IntervalUtil(0.075f, 0.125f);
	protected boolean spawnedSwarms = false;
	
	public ThreatShipReclamationScript(ShipAPI ship, float delay) {
		this.delay = delay;
		
		this.primary = ship;
		for (ShipAPI curr : Global.getCombatEngine().getShips()) {
			if (curr.getFleetMember() == ship.getFleetMember()) {
				pieces.add(curr);
			}
		}
		
		switch (ship.getHullSize()) {
		case CAPITAL_SHIP:
			this.fadeOutTime = 15f;
			break;
		case CRUISER:
			this.fadeOutTime = 12f;
			break;
		case DESTROYER:
			this.fadeOutTime = 9f;
			break;
		case FRIGATE:
			this.fadeOutTime = 7f;
			break;
		default:
			this.fadeOutTime = 7f;
			break;
		}
		
		this.fadeOutTime += 3f;
		
		interval.forceIntervalElapsed();
		
		for (ShipAPI curr : pieces) {
			curr.addTag(ThreatHullmod.SHIP_BEING_RECLAIMED);
		}
	}

	public List<ShipAPI> getPieces() {
		return pieces;
	}

	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		if (Global.getCombatEngine().isPaused()) return;
	
		elapsed += amount;
		if (elapsed < delay) return;
		
		
		CombatEngineAPI engine = Global.getCombatEngine();

		float progress = (elapsed - delay) / fadeOutTime;
		if (progress < 0f) progress = 0f;
		if (progress > 1f) progress = 1f;
		
		
		float remaining = fadeOutTime - (elapsed - delay);
		
		
		if (elapsed > delay + 2f && remaining > 4f) {
			spawnSwarms(amount);
		}
		
		boolean first = true;
		boolean anyInEngine = false;
		for (ShipAPI ship : pieces) {
			if (!engine.isInEngine(ship)) continue;
			anyInEngine = true;
			
			Vector2f vel = ship.getVelocity();
			Vector2f acc = new Vector2f(vel);
			if (acc.length() != 0) {
				acc.normalise();
				acc.scale(-1f);
				acc.scale(amount * ship.getDeceleration());
				Vector2f.add(vel, acc, vel);
				float speed = vel.length();
				if (speed <= 1 || speed < acc.length()) {
					vel.set(0, 0);
				}
			}
			
			float alpha = 1f;
			if (progress > 0.5f) {
				alpha = (1f - progress) * 2f;
			}
			
			ship.setAlphaMult(alpha);
			
			if (first) {
				Global.getSoundPlayer().playLoop("reclamation_loop", ship, 1f, 1f, ship.getLocation(), ship.getVelocity());
				first = false;
			}
			
			if (false) {
				float jitterLevel = 1f - progress;
				if (fadeOutTime <= 4f) {
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
					jitterRange = (elapsed - delay) / Math.max(1f, fadeOutTime - 2f);
				}
				float maxRangeBonus = 25f;
				float jitterRangeBonus = jitterRange * maxRangeBonus;
				
				Color c = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;
				c = Misc.setAlpha(c, 127);
				
				ship.setJitter(this, c, jitterLevel, 3, 0f, jitterRangeBonus);
			}
			
			spawnParticles(ship, amount, progress);
		}

		if (elapsed > fadeOutTime + delay || !anyInEngine) {
			for (ShipAPI ship : pieces) {
				engine.removeEntity(ship);
				ship.setAlphaMult(0f);
			}
			engine.removePlugin(this);
		}
	}
	
	protected void spawnParticles(ShipAPI ship, float amount, float progress) {
		if (ship == null) return;
		
		float remaining = fadeOutTime - (elapsed - delay);
		
		interval.advance(amount);
		if (interval.intervalElapsed()) {
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
	
	protected void spawnSwarms(float amount) {
		if (!spawnedSwarms) {
			int numSwarms = 3;
			
			for (SwarmConstructableVariant curr : ConstructionSwarmSystemScript.CONSTRUCTABLE) {
				if (curr.variantId.equals(primary.getVariant().getHullVariantId())) {
					numSwarms = (int) Math.round(curr.cr * 100f);
					break;
				}
			}
			
			for (int i = 0; i < numSwarms; i++) {
				ShipAPI curr = launchSwarm();
				swarms.add(curr);
			}
			spawnedSwarms = true;
		}
		
		interval2.advance(amount * 2f);
		if (interval2.intervalElapsed()) {
			WeightedRandomPicker<ShipAPI> picker = new WeightedRandomPicker<>();
			picker.addAll(pieces);
			
			for (ShipAPI curr : swarms) {
				RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(curr);
				if (swarm == null) continue;
				if (swarm.getNumActiveMembers() > swarm.params.baseMembersToMaintain) continue;
				
				SwarmMember p = swarm.addMember();
				
				ShipAPI piece = picker.pick();
				if (piece == null) continue;
				
				Vector2f loc = Misc.getPointWithinRadius(piece.getLocation(), piece.getCollisionRadius() * 0.5f);
				p.loc.set(loc);
				p.fader.setDurationIn(0.3f);
			}
			
		}
	}
	
	protected ShipAPI launchSwarm() {
		String wingId = SwarmLauncherEffect.RECLAMATION_SWARM_WING;

		CombatEngineAPI engine = Global.getCombatEngine();
		CombatFleetManagerAPI manager = engine.getFleetManager(primary.getOriginalOwner());
		manager.setSuppressDeploymentMessages(true);
		
		Vector2f loc = primary.getLocation();
		float facing = (float) Math.random() * 360f;
		
		ShipAPI fighter = manager.spawnShipOrWing(wingId, loc, facing, 0f, null);
		fighter.getWing().setSourceShip(primary);
		
		manager.setSuppressDeploymentMessages(false);
		
		fighter.getMutableStats().getMaxSpeed().modifyMult("construction_swarm", RECLAMATION_SWARM_SPEED_MULT);
		
		Vector2f takeoffVel = Misc.getUnitVectorAtDegreeAngle(facing);
		takeoffVel.scale(fighter.getMaxSpeed() * 1f);
		
		fighter.setDoNotRender(true);
		fighter.setExplosionScale(0f);
		fighter.setHulkChanceOverride(0f);
		fighter.setImpactVolumeMult(SwarmLauncherEffect.IMPACT_VOLUME_MULT);
		fighter.getArmorGrid().clearComponentMap(); // no damage to weapons/engines
		Vector2f.add(fighter.getVelocity(), takeoffVel, fighter.getVelocity());
		
		RoilingSwarmEffect swarm = FragmentSwarmHullmod.createSwarmFor(fighter);
		RoilingSwarmEffect.getFlockingMap().remove(swarm.params.flockingClass, swarm);
		swarm.params.flockingClass = FragmentSwarmHullmod.RECLAMATION_SWARM_FLOCKING_CLASS;
		RoilingSwarmEffect.getFlockingMap().add(swarm.params.flockingClass, swarm);
		swarm.params.memberExchangeClass = FragmentSwarmHullmod.RECLAMATION_SWARM_EXCHANGE_CLASS;
		
		
		//swarm.params.flashFringeColor = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;
		swarm.params.flashFrequency = 5f;
		swarm.params.flashProbability = 1f;
		
		// brownish/rusty
		//swarm.params.flashFringeColor = new Color(255,95,50,50);
		
		swarm.params.flashFringeColor = new Color(255,70,30,50);
		swarm.params.flashCoreRadiusMult = 0f;
		
		swarm.params.springStretchMult = 1f;
		
		//swarm.params.baseSpriteSize *= RECLAMATION_SWARM_FRAGMENT_SIZE_MULT;
		//swarm.params.flashFringeColor = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;

		float collisionMult = RECLAMATION_SWARM_COLLISION_MULT;
		float hpMult = RECLAMATION_SWARM_HP_MULT;
		
		for (SegmentAPI s : fighter.getExactBounds().getOrigSegments()) {
			s.getP1().scale(collisionMult);
			s.getP2().scale(collisionMult);
			s.set(s.getP1().x, s.getP1().y, s.getP2().x, s.getP2().y);
		}
		fighter.setCollisionRadius(fighter.getCollisionRadius() * collisionMult);
		
		fighter.setMaxHitpoints(fighter.getMaxHitpoints() * hpMult);
		fighter.setHitpoints(fighter.getHitpoints() * hpMult);
		
		swarm.params.maxOffset *= RECLAMATION_SWARM_RADIUS_MULT;
		
		swarm.params.initialMembers = 0;
		swarm.params.baseMembersToMaintain = 50;
		
//		int transfer = Math.min(numFragments, sourceSwarm.getNumActiveMembers());
//		if (transfer > 0) {
//			loc = new Vector2f(takeoffVel);
//			loc.scale(0.5f);
//			Vector2f.add(loc, fighter.getLocation(), loc);
//			sourceSwarm.transferMembersTo(swarm, transfer, loc, 100f);
//		}
//		
//		int add = numFragments - transfer;
//		if (add > 0) {
//			swarm.addMembers(add);
//		}
		
		return fighter;
		
	}
}








