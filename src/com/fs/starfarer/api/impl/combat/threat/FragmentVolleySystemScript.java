package com.fs.starfarer.api.impl.combat.threat;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

public class FragmentVolleySystemScript extends BaseEnergyLashActivatedSystem implements WeaponRangeModifier {
	
	//public static float FRAGMENT_RANGE_PERCENT = 100;
	public static float FRAGMENT_RANGE_MULT = 2;
	public static float MAX_VOLLEY_RANGE = 3000f;
	
	public static float FRAGMENT_REGEN_RATE_MULT = 10f;
	
	public void applyImpl(ShipAPI ship, MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (effectLevel <= 0f) {
			stats.getDynamic().getStat(Stats.FRAGMENT_SWARM_RESPAWN_RATE_MULT).unmodifyMult(id);
			return;
		}
		
		stats.getDynamic().getStat(Stats.FRAGMENT_SWARM_RESPAWN_RATE_MULT).modifyMult(id, 
								1f + (FRAGMENT_REGEN_RATE_MULT - 1f) * effectLevel);
		
		Color glowColor = Misc.setAlpha(VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR, 50);
		for (WeaponGroupAPI g : ship.getWeaponGroupsCopy()) {
			for (WeaponAPI w : g.getWeaponsCopy()) {
				if (w.hasAIHint(AIHints.PD)) continue;
				if (w.hasAIHint(AIHints.NO_MANUAL_FIRE)) continue;
				
				if (w.getSpec().hasTag(Tags.FRAGMENT)) {
					w.setGlowAmount(effectLevel, glowColor);
				}
			}
		}
		
		setStandardJitter(ship, state, effectLevel);
		
		if (state == State.OUT) {
			ship.removeListenerOfClass(FragmentVolleySystemScript.class);
			return;
		}
		if (state != State.ACTIVE) {
			return;
		}
		
		if (!ship.hasListenerOfClass(FragmentVolleySystemScript.class)) {
			ship.addListener(this);
		}
			
		for (WeaponGroupAPI g : ship.getWeaponGroupsCopy()) {
			for (WeaponAPI w : g.getWeaponsCopy()) {
				if (w.hasAIHint(AIHints.NO_MANUAL_FIRE)) continue;
				
				if (!w.hasAIHint(AIHints.PD) && w.getSpec().hasTag(Tags.FRAGMENT)) {
					if (w.getCooldownRemaining() > 1f) {
						w.setRemainingCooldownTo(1f);
					}
					if (w.usesAmmo()) {
						w.setAmmo(w.getMaxAmmo());
					}
					w.setForceFireOneFrame(true);
				} else {
					w.setForceNoFireOneFrame(true);
				}
			}
		}
		
		Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
		if (test instanceof ShipAPI) {
			ShipAPI target = (ShipAPI) test;
			ship.setShipTarget(target);
		}
	}
	
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (effectLevel <= 0f) return null;
		if (index == 0) {
			return new StatusData("firing fragments", false);
		} else if (index == 1) {
			return new StatusData("" + (int)FRAGMENT_RANGE_MULT + "x fragment range", false);
		}
		return null;
	}

	@Override
	public float getCurrentUsefulnessLevel(ShipAPI overseer, ShipAPI ship) {
		if (ship.getSystem().isActive() || ship.getSystem().isChargedown() ||
				ship.getSystem().isChargeup() || ship.getSystem().isCoolingDown()) {
			return 0f;
		}
		
		Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
		if (test instanceof ShipAPI) {
			ShipAPI target = (ShipAPI) test;
			
			float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
			dist -= ship.getCollisionRadius() + target.getCollisionRadius();
			
			float range = getMinFragmentWeaponRange(ship) * FRAGMENT_RANGE_MULT;
			if (dist < range * 0.8f && dist < MAX_VOLLEY_RANGE) {
				float distToOverseer = Misc.getDistance(ship.getLocation(), overseer.getLocation());
				distToOverseer -= ship.getCollisionRadius() + overseer.getCollisionRadius();
				float overseerDistFactor = 0f;
				if (distToOverseer < 1000f) {
					float min = 500f;
					overseerDistFactor = (1f - Math.max(0f, distToOverseer - min) / (1000f - min)) * 0.25f;
				}
				return Math.min(1f, 0.5f + Math.min(0.5f, ship.getFluxLevel() * 1f) + overseerDistFactor);
			}
		}
		
		return 0f;
	}
	
	
	public static float getMinFragmentWeaponRange(ShipAPI ship) {
		float min = Float.MAX_VALUE;
		boolean found = false;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative()) continue;
			if (!w.hasAIHint(AIHints.PD) &&
					!w.hasAIHint(AIHints.NO_MANUAL_FIRE) &&
					w.getSpec().hasTag(Tags.FRAGMENT)) {
				min = Math.min(min, w.getRange());
				found = true;
			}
		}
		if (!found) min = 0;
		
		return min;
	}

	@Override
	public float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
		return 0;
	}

	@Override
	public float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
		if (!weapon.hasAIHint(AIHints.PD) && 
				!weapon.hasAIHint(AIHints.NO_MANUAL_FIRE) &&
				weapon.getSpec().hasTag(Tags.FRAGMENT)) {
			return FRAGMENT_RANGE_MULT;
		}
		return 1f;
	}

	@Override
	public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
		return 0;
	}
	
}








