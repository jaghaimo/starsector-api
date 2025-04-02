package com.fs.starfarer.api.impl.combat.threat;

import java.util.ArrayList;
import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FindShipFilter;

public class EnergyLashSystemScript extends BaseShipSystemScript {
	
	public static float MAX_LASH_RANGE = 1500f;
	
	public static float DAMAGE = 0;
	public static float EMP_DAMAGE = 1500;
	
	public static float MIN_COOLDOWN = 2f;
	public static float MAX_COOLDOWN = 10f;
	public static float COOLDOWN_DP_MULT = 0.33f;
	
	public static float MIN_HIT_ENEMY_COOLDOWN = 2f;
	public static float MAX_HIT_ENEMY_COOLDOWN = 5f;
	public static float HIT_PHASE_ENEMY_COOLDOWN_MULT = 2f;
	
	public static float SWARM_TIMEOUT = 10f;
	
	public static float PHASE_OVERLOAD_DUR = 1f;
	

	
	public static class DelayedCombatActionPlugin extends BaseEveryFrameCombatPlugin {
		float elapsed = 0f;
		float delay;
		Runnable r;
		
		public DelayedCombatActionPlugin(float delay, Runnable r) {
			this.delay = delay;
			this.r = r;
		}
			
		@Override
		public void advance(float amount, List<InputEventAPI> events) {
			if (Global.getCombatEngine().isPaused()) return;
		
			elapsed += amount;
			if (elapsed < delay) return;
			
			r.run();
	
			CombatEngineAPI engine = Global.getCombatEngine();
			engine.removePlugin(this);
		}
	}
	
	
	
	protected WeaponSlotAPI mainSlot;
	protected List<WeaponSlotAPI> slots;
	protected boolean readyToFire = true;
	protected float sinceSwarmTargeted = SWARM_TIMEOUT;
	protected float cooldownToSet = -1f;
	
	protected void findSlots(ShipAPI ship) {
		if (slots != null) return;
		slots = new ArrayList<>();
		for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			if (slot.isSystemSlot()) {
				slots.add(slot);
				if (slot.getSlotSize() == WeaponSize.MEDIUM) {
					mainSlot = slot;
				}
			}
		}
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			//player = ship == Global.getCombatEngine().getPlayerShip();
		} else {
			return;
		}
		
		sinceSwarmTargeted += Global.getCombatEngine().getElapsedInLastFrame();
		
		if ((state == State.COOLDOWN || state == State.IDLE) && cooldownToSet >= 0f) {
			ship.getSystem().setCooldown(cooldownToSet);
			ship.getSystem().setCooldownRemaining(cooldownToSet);
			cooldownToSet = -1f;
			
		}
		
		if (state == State.IDLE || state == State.COOLDOWN || effectLevel <= 0f) {
			readyToFire = true;
		}
		
		if (state == State.IN || state == State.OUT) {
			float jitterLevel = effectLevel;

			float maxRangeBonus = 150f;
			//float jitterRangeBonus = jitterLevel * maxRangeBonus;
			float jitterRangeBonus = (1f - effectLevel * effectLevel) * maxRangeBonus;
			
			float brightness = 0f;
			float threshold = 0.1f;
			if (effectLevel < threshold) {
				brightness = effectLevel / threshold;
			} else {
				brightness = 1f - (effectLevel - threshold) / (1f - threshold);
			}
			if (brightness < 0) brightness = 0;
			if (brightness > 1) brightness = 1;
			if (state == State.OUT) {
				jitterRangeBonus = 0f;
				brightness = effectLevel * effectLevel;
			}
			Color color = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;
			//color = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR_BRIGHT;
			//ship.setJitterUnder(this, color, jitterLevel, 21, 0f, 3f + jitterRangeBonus);
			//ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus * 0.67f);
			//ship.setJitter(this, color, jitterLevel, 1, 0f, 3f);
			ship.setJitter(this, color, jitterLevel, 5, 0f, 3f + jitterRangeBonus);
		}
		
		if (effectLevel == 1 && readyToFire) {
			ShipAPI target = findTarget(ship);
			readyToFire = false;
			if (target != null) {
				CombatEngineAPI engine = Global.getCombatEngine();
				findSlots(ship);

				Vector2f slotLoc = mainSlot.computePosition(ship);

				EmpArcParams params = new EmpArcParams();
				params.segmentLengthMult = 8f;
				params.zigZagReductionFactor = 0.15f;
				params.fadeOutDist = 500f;
				params.minFadeOutMult = 2f;
				params.flickerRateMult = 0.7f;
				
				//params.movementDurMax = 0.1f;
//				params.movementDurMin = 0.25f;
//				params.movementDurMax = 0.25f;
				
				
				if (ship.getOwner() == target.getOwner()) {
					//params.flickerRateMult = 0.6f;
					params.flickerRateMult = 0.3f;
					
					Color color = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;
					if (ThreatSwarmAI.isAttackSwarm(target)) {
						color = VoltaicDischargeOnFireEffect.PHASE_FRINGE_COLOR;
					}
					float emp = 0;
					float dam = 0;
					EmpArcEntityAPI arc = (EmpArcEntityAPI)engine.spawnEmpArcPierceShields(ship, slotLoc, ship, target,
							DamageType.ENERGY, 
							dam,
							emp, // emp 
							100000f, // max range 
							"energy_lash_friendly_impact",
							100f, // thickness
							//new Color(100,165,255,255),
							color,
							new Color(255,255,255,255),
							params
							);
					arc.setTargetToShipCenter(slotLoc, target);
					arc.setCoreWidthOverride(50f);
	
					arc.setSingleFlickerMode(true);
					//arc.setFadedOutAtStart(true);
					Global.getSoundPlayer().playSound("energy_lash_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
				} else {
					params.flickerRateMult = 0.4f;
					
					int numArcs = slots.size();
					//numArcs = 1;
					
					float emp = EMP_DAMAGE;
					float dam = DAMAGE;
					
					for (int i = 0; i < numArcs; i++) {
						float delay = 0.03f * i;
						//delay = 0f;
						
//						EmpArcParams params2 = new EmpArcParams();
//						params2.segmentLengthMult = 8f;
//						params2.zigZagReductionFactor = 0.15f;
//						params2.fadeOutDist = 500f;
//						params2.minFadeOutMult = 2f;
//						params2.flickerRateMult = 0.8f - i * 0.1f;
//						params2.flickerRateMult = 0.8f;
						
						int index = i;
						ShipAPI ship2 = ship;
						Runnable r = new Runnable() {
							@Override
							public void run() {
								Vector2f slotLoc = slots.get(index).computePosition(ship2);
								Color color = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;
								Color core = new Color(255,255,255,255);
								if (target.isPhased()) {
									color = VoltaicDischargeOnFireEffect.PHASE_FRINGE_COLOR;
									core = VoltaicDischargeOnFireEffect.PHASE_CORE_COLOR;
								}
								//color = Misc.interpolateColor(color, new Color(255,0,255), 0.25f);
								EmpArcEntityAPI arc = (EmpArcEntityAPI)engine.spawnEmpArc(ship2, slotLoc, ship2, target,
										DamageType.ENERGY, 
										dam,
										emp, // emp 
										100000f, // max range 
										"energy_lash_enemy_impact",
										60f, // thickness
										//new Color(100,165,255,255),
										color,
										core,
										params
										);
								arc.setCoreWidthOverride(40f);
								arc.setSingleFlickerMode(true);
							}
						};
						if (delay <= 0f) {
							r.run();
						} else {
							Global.getCombatEngine().addPlugin(new DelayedCombatActionPlugin(delay, r));
						}
						
						Global.getSoundPlayer().playSound("energy_lash_fire_at_enemy", 1f, 1f, ship.getLocation(), ship.getVelocity());
						
//						arc.setFadedOutAtStart(true);
//						arc.setRenderGlowAtStart(false);
					}
				}
				
				applyEffectToTarget(ship, target);
			}
		}
	}
	
	
	
	
	protected void applyEffectToTarget(ShipAPI ship, ShipAPI target) {
		boolean isSwarm = ThreatSwarmAI.isAttackSwarm(target);
		if (!isSwarm) {
			if (target == null || target.getSystem() == null || target.isHulk()) return;
		}
		if (ship == null || ship.getSystem() == null || ship.isHulk()) return;
		
		if (ship.getOwner() == target.getOwner()) {
			if (target.getSystem() != null && target.getSystem().getScript() instanceof EnergyLashActivatedSystem) {
				EnergyLashActivatedSystem script = (EnergyLashActivatedSystem) target.getSystem().getScript();
				script.hitWithEnergyLash(ship, target);
			} else if (isSwarm) {
				VoltaicDischargeOnFireEffect.setSwarmPhaseMode(target);
				sinceSwarmTargeted = 0f;
			}
			
			float cooldown = target.getHullSpec().getSuppliesToRecover();
			//float cooldown = target.getMutableStats().getSuppliesToRecover().getBaseValue();
			//cooldown = (int)Math.round(target.getMutableStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).computeEffective(cooldown));
			
			cooldown = MIN_COOLDOWN + cooldown * COOLDOWN_DP_MULT;
			if (cooldown > MAX_COOLDOWN) cooldown = MAX_COOLDOWN;
			if (target.isFighter()) cooldown = MIN_COOLDOWN;
//			ship.getSystem().setCooldown(cooldown);
//			ship.getSystem().setCooldownRemaining(cooldown);
			cooldownToSet = cooldown;
		} else {
			boolean hitPhase = false;
			if (target.isPhased()) {
				target.setOverloadColor(VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR_BRIGHT);
				target.getFluxTracker().beginOverloadWithTotalBaseDuration(PHASE_OVERLOAD_DUR);
				if (target.getFluxTracker().showFloaty() || 
						ship == Global.getCombatEngine().getPlayerShip() ||
						target == Global.getCombatEngine().getPlayerShip()) {
					target.getFluxTracker().playOverloadSound();
					target.getFluxTracker().showOverloadFloatyIfNeeded("Phase Field Disruption!",
									VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR, 4f, true);
				}
				
				Global.getCombatEngine().addPlugin(new BaseEveryFrameCombatPlugin() {
					@Override
					public void advance(float amount, List<InputEventAPI> events) {
						if (!target.getFluxTracker().isOverloadedOrVenting()) {
							target.resetOverloadColor();
							Global.getCombatEngine().removePlugin(this);
						}
					}
				});
				
				hitPhase = true;
			}
			
			float cooldown = MIN_HIT_ENEMY_COOLDOWN + 
					(MAX_HIT_ENEMY_COOLDOWN - MIN_HIT_ENEMY_COOLDOWN) * (float) Math.random();
			if (hitPhase) {
				cooldown *= HIT_PHASE_ENEMY_COOLDOWN_MULT;
			}
			if (cooldown > MAX_COOLDOWN) cooldown = MAX_COOLDOWN;
//			ship.getSystem().setCooldown(cooldown);
//			ship.getSystem().setCooldownRemaining(cooldown);
			cooldownToSet = cooldown;
		}
		
//		ship.getSystem().setCooldown(0.2f);
//		ship.getSystem().setCooldownRemaining(0.2f);
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}
	
	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		ShipAPI target = findTarget(ship);
		if (target != null && target != ship) {
			return "READY";
		}
		if ((target == null || target == ship) && ship.getShipTarget() != null) {
			return "OUT OF RANGE";
		}
		return "NO TARGET";
	}

	public boolean isInRange(ShipAPI ship, ShipAPI target) {
		float range = getRange(ship);
		float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
		float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
		return dist <= range + radSum;
	}
	
	public boolean isValidLashTarget(ShipAPI ship, ShipAPI other) {
		if (other == null) return false;
		if (other.isHulk() || other.getOwner() == 100) return false;
		if (other.isShuttlePod()) return false;
		if (other.hasTag(ThreatShipConstructionScript.SHIP_UNDER_CONSTRUCTION)) return false;
		if (other.isFighter() && other.getOwner() == ship.getOwner()) {
			return ThreatSwarmAI.isAttackSwarm(other) && sinceSwarmTargeted > SWARM_TIMEOUT;
		}
		
		if (other.isFighter()) return false;
		if (other.getOwner() == ship.getOwner()) {
			if (other.getSystem() == null) return false;
			if (!(other.getSystem().getScript() instanceof EnergyLashActivatedSystem)) return false;
			if (other.getSystem().getCooldownRemaining() > 0) return false;
			if (other.getSystem().isActive()) return false;
			if (other.getFluxTracker().isOverloadedOrVenting()) return false;
		}
		return true;
		//return !other.isFighter();
	}
	
	
	protected ShipAPI findTarget(ShipAPI ship) {
		float range = getRange(ship);
		boolean player = ship == Global.getCombatEngine().getPlayerShip();
		ShipAPI target = ship.getShipTarget();

		float extraRange = 0f;
		if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.CUSTOM1)){
			target = (ShipAPI) ship.getAIFlags().getCustom(AIFlags.CUSTOM1);
			extraRange += 500f;
		}
		
		
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
			float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
			if (dist > range + radSum + extraRange) target = null;
		} else {
			FindShipFilter filter = s -> isValidLashTarget(ship, s);
			
			if (target == null || target.getOwner() == ship.getOwner()) {
				if (player) {
					target = Misc.findClosestShipTo(ship, ship.getMouseTarget(), HullSize.FIGHTER, range, true, false, filter);
				} else {
					Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
					if (test instanceof ShipAPI) {
						target = (ShipAPI) test;
						float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
						float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
						if (dist > range + radSum) target = null;
					}
				}
			}
			if (target == null) {
				target = Misc.findClosestShipTo(ship, ship.getLocation(), HullSize.FIGHTER, range, true, false, filter);
			}
		}
		
		return target;
	}
	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		ShipAPI target = findTarget(ship);
		return target != null && target != ship;
		//return super.isUsable(system, ship);
	}
	
	public static float getRange(ShipAPI ship) {
		if (ship == null) return MAX_LASH_RANGE;
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(MAX_LASH_RANGE);
	}
	
}








