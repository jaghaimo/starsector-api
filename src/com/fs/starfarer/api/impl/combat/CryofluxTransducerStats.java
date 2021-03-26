package com.fs.starfarer.api.impl.combat;

import java.awt.Color;
import java.util.EnumSet;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

/**
 * UNUSED, not related to CryofluxTransducerEffect.
 */
public class CryofluxTransducerStats extends BaseShipSystemScript {
	public static final Object KEY_SHIP = new Object();
	public static final Color DEFAULT_JITTER_COLOR = new Color(100,165,255,75);
	public static final float INCOMING_DAMAGE_MULT = 0.5f;
	public static final float FLUX_USE_MULT = 0.5f;
	
	public static class TargetData {
		public ShipAPI target;
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
	
		ship.fadeToColor(KEY_SHIP, new Color(75,75,75,255), 0.1f, 0.1f, effectLevel);
		//ship.fadeToColor(KEY_SHIP, new Color(100,100,100,255), 0.1f, 0.1f, effectLevel);
		ship.setWeaponGlow(effectLevel, new Color(100,165,255,255), EnumSet.of(WeaponType.BALLISTIC, WeaponType.ENERGY, WeaponType.MISSILE));
		ship.getEngineController().fadeToOtherColor(KEY_SHIP, new Color(0,0,0,0), new Color(0,0,0,0), effectLevel, 0.75f * effectLevel);
		//ship.setJitter(KEY_SHIP, new Color(100,165,255,55), effectLevel, 1, 0f, 5f);
		ship.setJitterUnder(KEY_SHIP, new Color(100,165,255,255), effectLevel, 15, 0f, 15f);
		//ship.setShowModuleJitterUnder(true);
		
		effectLevel = 1f;
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (1f - FLUX_USE_MULT) * effectLevel);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (1f - FLUX_USE_MULT) * effectLevel);
		stats.getMissileWeaponFluxCostMod().modifyMult(id, 1f - (1f - FLUX_USE_MULT) * effectLevel);
		
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - INCOMING_DAMAGE_MULT) * effectLevel);
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
		stats.getMissileWeaponFluxCostMod().unmodify(id);
		
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		effectLevel = 1f;
		float percent = (1f - FLUX_USE_MULT) * effectLevel * 100;
		if (index == 0) {
			return new StatusData((int) percent + "% less flux generated", false);
		}
		percent = (1f - INCOMING_DAMAGE_MULT) * effectLevel * 100;
		if (index == 1) {
			return new StatusData((int) percent + "% less damage taken", false);
		}
		return null;
	}

}








