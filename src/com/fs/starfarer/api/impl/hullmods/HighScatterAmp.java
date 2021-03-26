package com.fs.starfarer.api.impl.hullmods;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;

public class HighScatterAmp extends BaseHullMod {

	public static float RANGE_PENALTY_PERCENT = 50f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBeamWeaponRangeBonus().modifyMult(id, 1f - RANGE_PENALTY_PERCENT * 0.01f);
		
		// test code for WeaponOPCostModifier, FighterOPCostModifier
//		stats.addListener(new WeaponOPCostModifier() {
//			public int getWeaponOPCost(MutableShipStatsAPI stats, WeaponSpecAPI weapon, int currCost) {
//				if (weapon.getWeaponId().equals("amblaster")) {
//					return 1;
//				}
//				return currCost;
//			}
//		});
//		stats.addListener(new FighterOPCostModifier() {
//			public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
//				if (fighter.getId().equals("talon_wing")) {
//					return 20;
//				}
//				return currCost;
//			}
//		});
	}
	
//	@Override
//	public boolean affectsOPCosts() {
//		return true;
//	}



	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new HighScatterAmpDamageDealtMod(ship));
		
		/* test code for WeaponRangeModifier
		ship.addListener(new WeaponRangeModifier() {
			public float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
				return 0;
			}
			public float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
				return 1f;
			}
			public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
				if (weapon.getId().equals("amblaster")) {
					return 500;
				}
				return 0f;
			}
		});
		*/
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)RANGE_PENALTY_PERCENT + "%";
		return null;
	}
	
	public static class HighScatterAmpDamageDealtMod implements DamageDealtModifier {
		protected ShipAPI ship;
		public HighScatterAmpDamageDealtMod(ShipAPI ship) {
			this.ship = ship;
		}
		
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			
			if (!(param instanceof DamagingProjectileAPI) && param instanceof BeamAPI) {
				damage.setForceHardFlux(true);
			}
			return null;
		}
	}
}









