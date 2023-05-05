package com.fs.starfarer.api.impl.hullmods;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class HighScatterAmp extends BaseHullMod {

	public static float RANGE_THRESHOLD = 200f;
	public static float RANGE_MULT = 0.5f;
	
//	public static float RANGE_FRIGATE = 500;
//	public static float RANGE_DESTROYER = 600;
//	public static float RANGE_LARGE = 700;
	
	public static float DAMAGE_BONUS_PERCENT = 10f;
	public static float SMOD_MODIFIER = 5f;
	
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return !ship.getVariant().getHullMods().contains(HullMods.ADVANCEDOPTICS);
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains(HullMods.ADVANCEDOPTICS)) {
			return "Incompatible with Advanced Optics";
		}
		return null;
	}
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		
		//stats.getBeamWeaponRangeBonus().modifyMult(id, 1f - RANGE_PENALTY_PERCENT * 0.01f);
		stats.getBeamWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS_PERCENT + (sMod ? SMOD_MODIFIER : 0));
		
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

	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) Math.round(SMOD_MODIFIER) + "%";
		return null;
	}


	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new HighScatterAmpDamageDealtMod(ship));
		ship.addListener(new HighScatterAmpRangeMod());
		
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
	
	public static class HighScatterAmpRangeMod implements WeaponBaseRangeModifier {
		public HighScatterAmpRangeMod() {
		}
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.isBeam()) {
				float range = weapon.getSpec().getMaxRange();
				if (range < RANGE_THRESHOLD) return 0;
				
				float past = range - RANGE_THRESHOLD;
				float penalty = past * (1f - RANGE_MULT);
				return -penalty;
			}
			return 0f;
		}
	}
//	public static class HighScatterAmpRangeMod implements WeaponBaseRangeModifier {
//		public HighScatterAmpRangeMod() {
//		}
//		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
//			return 0;
//		}
//		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
//			return 1f;
//		}
//		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
//			if (weapon.isBeam()) {
//				float range = weapon.getSpec().getMaxRange();
//				float max = range;
//				if (ship.isFighter() || ship.isFrigate()) {
//					max = RANGE_FRIGATE;
//				} else if (ship.isDestroyer()) {
//					max = RANGE_DESTROYER;
//				} else if (ship.isCruiser() || ship.isCapital()) {
//					max = RANGE_LARGE;
//				}
//				return Math.min(0f, max - range);
//			}
//			return 0f;
//		}
//	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return "" + (int)RANGE_PENALTY_PERCENT + "%";
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		tooltip.addPara("Beam weapons deal %s more damage and deal hard flux damage to shields.", opad, h,
				"" + (int)DAMAGE_BONUS_PERCENT + "%"
				);
		
		tooltip.addPara("Reduces the portion of the range of beam weapons that is above %s units by %s. The base range is affected.", opad, h,
				"" + (int)RANGE_THRESHOLD,
				"" + (int)Math.round((1f - RANGE_MULT) * 100f) + "%"
				);
//		tooltip.addPara("Reduces the base range of beam weapons to %s for frigates, %s for destroyers, "
//				+ "and %s for larger ships.", opad, h,
//				"" + (int)RANGE_FRIGATE,
//				"" + (int)RANGE_DESTROYER,
//				"" + (int)RANGE_LARGE
//				);
		
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
		tooltip.addPara("The base range is reduced, thus percentage and multiplicative modifiers - such as from Integrated Targeting Unit, "
				+ "skills, or similar sources - apply to the reduced base value.", opad);
	}
}









