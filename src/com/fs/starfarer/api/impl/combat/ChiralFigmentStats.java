package com.fs.starfarer.api.impl.combat;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.hullmods.ShardSpawner;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.util.Misc;

public class ChiralFigmentStats extends BaseShipSystemScript {
	
	public static Map<String, String> FIGMENTS = new HashMap<String, String>();
	static {
		FIGMENTS.put("shard_left_Attack", "shard_right_Attack");
		FIGMENTS.put("shard_left_Attack2", "shard_right_Attack");
		FIGMENTS.put("shard_right_Attack", "shard_left_Attack");
		FIGMENTS.put("shard_left_Armorbreaker", "shard_right_Attack");
		FIGMENTS.put("shard_left_Shieldbreaker", "shard_right_Shieldbreaker");
		FIGMENTS.put("shard_right_Shieldbreaker", "shard_left_Shieldbreaker");
		FIGMENTS.put("shard_left_Defense", "shard_right_Shock");
		FIGMENTS.put("shard_right_Shock", "shard_left_Defense");
		FIGMENTS.put("shard_left_Missile", "shard_right_Missile");
		FIGMENTS.put("shard_right_Missile", "shard_left_Missile");
	}
	
	public static final Color JITTER_COLOR = ShardSpawner.JITTER_COLOR;
	public static final Color JITTER_UNDER_COLOR = Misc.setAlpha(JITTER_COLOR, 155);

	
	public static float DAMAGE_TAKEN_MULT = 1f;
	public static float JITTER_PER_DAMAGE_DIVISOR = 100f;
	public static float JITTER_PER_DAMAGE_DECAY_SECONDS = 1f;
	
	
	public static float getSpawnAngle(ShipAPI ship) {
		String variant = FIGMENTS.get(ship.getVariant().getHullVariantId());
		if (ship.getVariant().getOriginalVariant() != null && variant == null) {
			variant = FIGMENTS.get(ship.getVariant().getOriginalVariant());
		}
		if (variant != null && variant.contains("_left")) {
			return 90f;
		}
		return -90f;
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		
		float jitterLevel = effectLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
		float maxRangeBonus = 25f;
		float jitterRangeBonus = jitterLevel * maxRangeBonus;
		if (state == State.OUT) {
		}
		
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);
		
		if (state == State.IN) {
		} else if (effectLevel >= 1) {
			String variant = FIGMENTS.get(ship.getVariant().getHullVariantId());
			if (ship.getVariant().getOriginalVariant() != null && variant == null) {
				variant = FIGMENTS.get(ship.getVariant().getOriginalVariant());
			}
			if (variant != null) {
				float angle = getSpawnAngle(ship);
				float dist = ship.getCollisionRadius() * 1.25f;
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(ship.getFacing() + angle);
				loc.scale(dist);
				Vector2f.add(loc, ship.getLocation(), loc);
				if (isLocationClear(ship, loc, dist / 2f)) {
					FigmentPlugin figment = new FigmentPlugin(variant, ship, 1f, 20f, loc);
					Global.getCombatEngine().addPlugin(figment);
				}
			}
		} else if (state == State.OUT ) {
		}
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	
	private boolean isLocationClear(ShipAPI ship, Vector2f loc, float minDist) {
		for (ShipAPI other : Global.getCombatEngine().getShips()) {
			if (other.isShuttlePod()) continue;
			if (other.isFighter()) continue;
			if (other == ship) continue;
			
			Vector2f otherLoc = other.getShieldCenterEvenIfNoShield();
			float otherR = other.getShieldRadiusEvenIfNoShield();
			
			
			float dist = Misc.getDistance(loc, otherLoc);
			float r = otherR;
			if (dist < r + minDist) {
				return false;
			}
		}
		for (CombatEntityAPI other : Global.getCombatEngine().getAsteroids()) {
			float dist = Misc.getDistance(loc, other.getLocation());
			if (dist < other.getCollisionRadius() + minDist) {
				return false;
			}
		}
		
		return true;
	}

	public static class FigmentDamageListener implements DamageListener {
		public FigmentPlugin plugin;
		public FigmentDamageListener(FigmentPlugin plugin) {
			this.plugin = plugin;
		}

		public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
			float totalDamage = result.getDamageToHull() + result.getDamageToShields() + result.getTotalDamageToArmor();
			//plugin.recentHits += totalDamage / JITTER_PER_DAMAGE_DIVISOR;
			float max = 1f;
			if (result.isDps()) max = 0.1f;
			plugin.recentHits += Math.min(max, totalDamage / 10f);
		}
	}
	public static class FigmentPlugin extends BaseEveryFrameCombatPlugin {
		float elapsed = 0f;
		ShipAPI [] ships = null;
		CollisionClass collisionClass;

		String variantId;
		ShipAPI source;
		float fadeInTime;
		Vector2f loc;
		float dur;
		
		boolean finishedFadingIn = false;
		
		float recentHits = 0f;

		public FigmentPlugin(String variantId, ShipAPI source, float fadeInTime, float dur, Vector2f loc) {
			this.variantId = variantId;
			this.source = source;
			this.fadeInTime = fadeInTime;
			this.dur = dur;
			this.loc = loc;
		}


		@Override
		public void advance(float amount, List<InputEventAPI> events) {
			if (Global.getCombatEngine().isPaused()) return;

			elapsed += amount;

			CombatEngineAPI engine = Global.getCombatEngine();
			
			if (ships == null) {
				float facing = source.getFacing() + 15f * ((float) Math.random() - 0.5f);
				CombatFleetManagerAPI fleetManager = engine.getFleetManager(source.getOriginalOwner());
				boolean wasSuppressed = fleetManager.isSuppressDeploymentMessages();
				fleetManager.setSuppressDeploymentMessages(true);
				if (variantId.endsWith("_wing")) {
					FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(variantId);
					ships = new ShipAPI[spec.getNumFighters()];
					PersonAPI captain = Global.getSettings().createPerson();
					captain.setPersonality(Personalities.RECKLESS); // doesn't matter for fighters
					captain.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
					captain.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
					captain.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
					ShipAPI leader = engine.getFleetManager(source.getOriginalOwner()).spawnShipOrWing(variantId, loc, facing, 0f, captain);
					for (int i = 0; i < ships.length; i++) {
						ships[i] = leader.getWing().getWingMembers().get(i);
						ships[i].getLocation().set(loc);
					}
					collisionClass = ships[0].getCollisionClass();
				} else {
					ships = new ShipAPI[1];
					ships[0] = engine.getFleetManager(source.getOriginalOwner()).spawnShipOrWing(variantId, loc, facing, 0f, source.getOriginalCaptain());
				}
				fleetManager.setSuppressDeploymentMessages(wasSuppressed);
				collisionClass = ships[0].getCollisionClass();
			}


			float progress = elapsed / fadeInTime;
			
			float maxAlpha = 0.67f;
			if (progress <= 1f) {
				for (int i = 0; i < ships.length; i++) {
					ShipAPI ship = ships[i];
					ship.setAlphaMult(progress * maxAlpha);
	
					if (progress < 0.5f) {
						ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
						ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT);
						ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT);
						ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
						ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT);
					}
	
					ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
					ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
					ship.blockCommandForOneFrame(ShipCommand.FIRE);
					ship.blockCommandForOneFrame(ShipCommand.PULL_BACK_FIGHTERS);
					ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
					ship.setHoldFireOneFrame(true);
					ship.setHoldFire(true);
	
	
					ship.setCollisionClass(CollisionClass.NONE);
					ship.getMutableStats().getHullDamageTakenMult().modifyMult("FigmentInvuln", 0f);
					if (progress < 0.5f) {
						ship.getVelocity().set(source.getVelocity());
					} else if (progress > 0.75f){
						ship.setCollisionClass(collisionClass);
						ship.getMutableStats().getHullDamageTakenMult().unmodifyMult("FigmentInvuln");
					}
	
					float jitterLevel = progress;
//					if (jitterLevel < 0.5f) {
//						jitterLevel *= 2f;
//					} else {
//						jitterLevel = (1f - jitterLevel) * 2f;
//					}
	
					float jitterRange = 1f - progress * 0.5f;
					float maxRangeBonus = 50f;
					maxRangeBonus -= 30f * progress;
					float jitterRangeBonus = jitterRange * maxRangeBonus;
					Color c = JITTER_COLOR;
					float num = 18f * (1f - progress * progress) + 7f;
	
					ship.setJitter(this, c, jitterLevel, (int)Math.round(num), 0f, jitterRangeBonus);
				}
			}

			if (elapsed > fadeInTime && !finishedFadingIn) {
				for (int i = 0; i < ships.length; i++) {
					ShipAPI ship = ships[i];
					ship.setAlphaMult(maxAlpha);
					ship.setHoldFire(false);
					ship.setCollisionClass(collisionClass);
					ship.getMutableStats().getHullDamageTakenMult().unmodifyMult("FigmentInvuln");
					
					DAMAGE_TAKEN_MULT = 1f;
					ship.getMutableStats().getHullDamageTakenMult().modifyMult("FigmentExtraDamage", DAMAGE_TAKEN_MULT);
					ship.getMutableStats().getShieldDamageTakenMult().modifyMult("FigmentExtraDamage", DAMAGE_TAKEN_MULT);
					ship.getMutableStats().getArmorDamageTakenMult().modifyMult("FigmentExtraDamage", DAMAGE_TAKEN_MULT);
					
					ship.addListener(new FigmentDamageListener(this));
				}
				finishedFadingIn = true;
			}
			
			if (elapsed > fadeInTime) {
				for (int i = 0; i < ships.length; i++) {
					ShipAPI ship = ships[i];
					
					if (!ship.isAlive() || !engine.isInPlay(ship)) {
						engine.removePlugin(this);
						return;
					}
					
					ship.setAlphaMult(maxAlpha);
					ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
					ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
					
					
					//float minus = recentHits * amount / JITTER_PER_DAMAGE_DECAY_SECONDS;
					float minus = recentHits * amount * 2f;
					minus += 0.1f * amount;
					recentHits -= minus;
					if (recentHits < 0) recentHits = 0;
					if (recentHits > 5f) recentHits = 5f;
					
					float jitterLevel = 1f;
					float jitterRange = 0.5f;
					float maxRangeBonus = 50f;
					maxRangeBonus = 20f;
					maxRangeBonus += recentHits * 40f;
					float jitterRangeBonus = jitterRange * maxRangeBonus;
					Color c = JITTER_COLOR;
	
					float numCopies = 7f;
					numCopies += recentHits * 2f;
					
					ship.setJitter(this, c, jitterLevel, (int) Math.round(numCopies), 0f, jitterRangeBonus);
				}
			}
			
			if (elapsed > dur) {
				// destroy ship here?
				//engine.removePlugin(this);
			}
		}
	}
	
}








