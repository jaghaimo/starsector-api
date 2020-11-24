package com.fs.starfarer.api.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FindShipFilter;

public class InterdictorArrayStats extends BaseShipSystemScript {
	public static final Object SHIP_KEY = new Object();
	public static final Object TARGET_KEY = new Object();
	
	public static final float WING_EFFECT_RANGE = 200f;
	
	public static final float RANGE = 1000f;
	public static final Color EFFECT_COLOR = new Color(100,165,255,75);
	
	
	public static class TargetData {
		public ShipAPI target;
		public float sinceLastAfterimage = 0f;
		public boolean lastAbove = false;
		public TargetData(ShipAPI target) {
			this.target = target;
		}
	}
	
	public void apply(MutableShipStatsAPI stats, final String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		final String targetDataKey = ship.getId() + "_interdictor_target_data";
		
		Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey); 
		if (state == State.IN && targetDataObj == null) {
			ShipAPI target = findTarget(ship);
			Global.getCombatEngine().getCustomData().put(targetDataKey, new TargetData(target));
		} else if (state == State.IDLE && targetDataObj != null) {
			Global.getCombatEngine().getCustomData().remove(targetDataKey);
		}
		if (targetDataObj == null || ((TargetData) targetDataObj).target == null) return;
		
		final TargetData targetData = (TargetData) targetDataObj;
		
		//ShipAPI target = targetData.target;
		List<ShipAPI> targets = new ArrayList<ShipAPI>();
		if (targetData.target.isFighter() || targetData.target.isDrone()) {
			CombatEngineAPI engine = Global.getCombatEngine();
			List<ShipAPI> ships = engine.getShips();
			for (ShipAPI other : ships) {
				if (other.isShuttlePod()) continue;
				if (other.isHulk()) continue;
				if (!other.isDrone() && !other.isFighter()) continue;
				if (other.getOriginalOwner() != targetData.target.getOriginalOwner()) continue;
				
				float dist = Misc.getDistance(other.getLocation(), targetData.target.getLocation());
				if (dist > WING_EFFECT_RANGE) continue;
				
				targets.add(other);
			}
		} else {
			targets.add(targetData.target);
		}
		
		boolean first = true;
		for (ShipAPI target : targets) {
			if (effectLevel >= 1) {
				Color color = getEffectColor(target);
				color = Misc.setAlpha(color, 255);

				if (first) {
					if (target.getFluxTracker().showFloaty() || 
							ship == Global.getCombatEngine().getPlayerShip() ||
							target == Global.getCombatEngine().getPlayerShip()) {
						target.getFluxTracker().showOverloadFloatyIfNeeded("Drive Interdicted!", color, 4f, true);
					}
					first = false;
				}

				ShipEngineControllerAPI ec = target.getEngineController();
				float limit = ec.getFlameoutFraction();
				if (target.isDrone() || target.isFighter()) {
					limit = 1f;
				}

				float disabledSoFar = 0f;
				boolean disabledAnEngine = false;
				List<ShipEngineAPI> engines = new ArrayList<ShipEngineAPI>(ec.getShipEngines());
				Collections.shuffle(engines);

				for (ShipEngineAPI engine : engines) {
					if (engine.isDisabled()) continue;
					float contrib = engine.getContribution();
					if (disabledSoFar + contrib <= limit) {
						engine.disable();
						disabledSoFar += contrib;
						disabledAnEngine = true;
					}
				}
				if (!disabledAnEngine) {
					for (ShipEngineAPI engine : engines) {
						if (engine.isDisabled()) continue;
						engine.disable();
						break;
					}
				}
				ec.computeEffectiveStats(ship == Global.getCombatEngine().getPlayerShip());
			}

			if (effectLevel > 0) {
				float jitterLevel = effectLevel;
				float maxRangeBonus = 20f + target.getCollisionRadius() * 0.25f;
				float jitterRangeBonus = jitterLevel * maxRangeBonus;
				if (state == State.OUT) {
					jitterRangeBonus = maxRangeBonus + (1f - jitterLevel) * maxRangeBonus;
				}
				target.setJitter(this,
						//target.getSpriteAPI().getAverageColor(),
						getEffectColor(target),
						jitterLevel, 6, 0f, 0 + jitterRangeBonus);

				if (first) {
					ship.setJitter(this,
							//target.getSpriteAPI().getAverageColor(),
							getEffectColor(targetData.target),
							jitterLevel, 6, 0f, 0 + jitterRangeBonus);
				}
			}
		}
	}
	

	protected Color getEffectColor(ShipAPI ship) {
		if (ship.getEngineController().getShipEngines().isEmpty()) {
			return EFFECT_COLOR;
		}
		return Misc.setAlpha(ship.getEngineController().getShipEngines().get(0).getEngineColor(), EFFECT_COLOR.getAlpha());
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		
	}
	
	protected ShipAPI findTarget(ShipAPI ship) {
		FindShipFilter filter = new FindShipFilter() {
			public boolean matches(ShipAPI ship) {
				return !ship.getEngineController().isFlamedOut();
			}
		};
		
		float range = getMaxRange(ship);
		boolean player = ship == Global.getCombatEngine().getPlayerShip();
		ShipAPI target = ship.getShipTarget();
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
			float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
			if (dist > range + radSum) target = null;
		} else {
			if (target == null || target.getOwner() == ship.getOwner()) {
				if (player) {
					target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FIGHTER, range, true, filter);
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
				target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FIGHTER, range, true, filter);
			}
		}
		
		return target;
	}
	
	
	protected float getMaxRange(ShipAPI ship) {
		return RANGE;
	}

	
	public StatusData getStatusData(int index, State state, float effectLevel) {
//		if (effectLevel > 0) {
//			if (index == 0) {
//				float damMult = 1f + (DAM_MULT - 1f) * effectLevel;
//				return new StatusData("" + (int)((damMult - 1f) * 100f) + "% more damage to target", false);
//			}
//		}
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
		if (target == null && ship.getShipTarget() != null) {
			return "OUT OF RANGE";
		}
		return "NO TARGET";
	}

	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		if (system.isActive()) return true;
		ShipAPI target = findTarget(ship);
		return target != null && target != ship;
	}

}








