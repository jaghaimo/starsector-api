package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ExtractionProtocolSystemScript extends BaseEnergyLashActivatedSystem {
	
	public static float SPEED_BONUS = 100f;
	
	protected boolean vented = false;
	
	public void applyImpl(ShipAPI ship, MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS * effectLevel);
		stats.getAcceleration().modifyFlat(id, 2f * SPEED_BONUS * effectLevel);
		stats.getDeceleration().modifyFlat(id, 2f * SPEED_BONUS * effectLevel);
		
		//stats.getFluxDissipation().modifyMult(id, 1f + (FLUX_DISSIPATION_MULT - 1f) * effectLevel);
		
		//ship.getEngineController().fadeToOtherColor(this, color, new Color(0,0,0,0), effectLevel, 0.67f);
		ship.getEngineController().extendFlame(ship.getSystem(), 0.01f * effectLevel, 0f * effectLevel, 1f * effectLevel);
		
		if (effectLevel <= 0f) return;
		
		ship.getAIFlags().setFlag(AIFlags.BACK_OFF, 1f);
		ship.getAIFlags().setFlag(AIFlags.DO_NOT_VENT, 1f);
		ship.getAIFlags().setFlag(AIFlags.BACK_OFF_MIN_RANGE, 10000f);
		ship.getAIFlags().unsetFlag(AIFlags.DO_NOT_BACK_OFF);
		
		if (state == State.IN || state == State.ACTIVE) {
			vented = false;
			makeAllGroupsAutofireOneFrame(ship);
		}
		if (state == State.OUT) {
			if (!vented) {
				ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
				
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative()) continue;
					if (w.getSlot().isSystemSlot()) continue;
					if (w.usesAmmo() && w.getAmmo() < w.getMaxAmmo()) {
						w.setAmmo(w.getMaxAmmo());
					}
				}
			}
		}

		setStandardJitter(ship, state, effectLevel);
	}
	
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (effectLevel <= 0f) return null;
		
		if (index == 0) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("+" + (int)SPEED_BONUS + " top speed", false);
		}
		return null;
	}

	@Override
	public float getCurrentUsefulnessLevel(ShipAPI overseer, ShipAPI ship) {
		if (ship.getSystem().isActive() || ship.getSystem().isChargedown() ||
				ship.getSystem().isChargeup() || ship.getSystem().isCoolingDown()) {
			return 0f;
		}
		
		float level = ship.getFluxLevel();
		
		float needAmmo = 0f;
		float useAmmo = 0f;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative()) continue;
			if (w.getSlot().isSystemSlot()) continue;
			if (w.usesAmmo()) {
				float op = w.getSpec().getOrdnancePointCost(null);
				useAmmo += op;
				// the system reloads regenerating weapons, but for this don't count them
				if (w.getAmmo() < w.getMaxAmmo() && w.getMaxAmmo() > 0 && w.getAmmoPerSecond() > 0) {
					needAmmo += (1f - (float)w.getAmmo() / (float) w.getMaxAmmo()) * op;
				}
			}
		}
		if (useAmmo > 0f) {
			level += 0.25f * needAmmo / useAmmo;
			if (level > 1f) level = 1f;
		}
		
		
		float threshold = 0.7f;
		Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
		if (test instanceof ShipAPI) {
			ShipAPI target = (ShipAPI) test;
			if (target.getFluxLevel() > ship.getFluxLevel()) {
				threshold = 0.9f; 
			}
			//threshold = Math.max(0.7f, level + 0.05f);
		}
		
		if (ship.getAIFlags().hasFlag(AIFlags.BACKING_OFF) && level > threshold) {
			return (level - threshold) / (1f - threshold);
		}
		
		return 0f;
	}
	
	
}








