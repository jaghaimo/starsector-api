package com.fs.starfarer.api.impl.combat.threat;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.util.Misc;

public class DisplacerGlowScript extends BaseEnergyLashActivatedSystem {
	
	public void applyImpl(ShipAPI ship, MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (state != State.IDLE && state != State.OUT) return;
		
		float ammo = ship.getSystem().getAmmo();
		float maxAmmo = ship.getSystem().getMaxAmmo();
		
		if (ammo <= 0 && (state == State.IDLE || state == State.OUT)) return;
		
		if (ship.isHulk()) return;
		
		//float jitterLevel = effectLevel;
		float jitterLevel = ammo / maxAmmo;
		jitterLevel = 0.5f + 0.5f * jitterLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
//		float maxRangeBonus = 50f;
//		float jitterRangeBonus = jitterLevel * maxRangeBonus;
		
		Color base = VoltaicDischargeOnFireEffect.EMP_FRINGE_COLOR;
		Color underColor = Misc.setAlpha(base, 255);
		Color overColor = Misc.setAlpha(base, 255);;
		ship.setJitterUnder(this, underColor, jitterLevel, 7, 4f, 4f);
		ship.setJitter(this, overColor, jitterLevel, 2, 0f, 4f);
		
		ship.setJitterShields(false);
		ship.setCircularJitter(true);
	}

	@Override
	public void hitWithEnergyLash(ShipAPI overseer, ShipAPI ship) {
		if (ship.getSystem() == null) return;
		
		ship.getSystem().setAmmo(ship.getSystem().getMaxAmmo());
	}

	@Override
	public float getCurrentUsefulnessLevel(ShipAPI overseer, ShipAPI ship) {
		float ammo = ship.getSystem().getAmmo();
		float maxAmmo = ship.getSystem().getMaxAmmo();
		if (ammo >= maxAmmo) return 0f;
		
		if (maxAmmo < 1) maxAmmo = 1f;
		float f = ammo / maxAmmo;
		
		float mult = 0.5f;
		if (ship.getAIFlags().hasFlag(AIFlags.BACKING_OFF) || 
				ship.getAIFlags().hasFlag(AIFlags.NEEDS_HELP)) {
			mult += 0.5f;
		}
		
		float fluxFactor = Math.min(0.25f, ship.getFluxLevel() * 0.25f);
		
		return fluxFactor + 0.75f * (1f - f) * mult;
	}
}








