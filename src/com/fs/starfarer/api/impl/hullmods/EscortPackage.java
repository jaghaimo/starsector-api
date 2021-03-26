package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class EscortPackage extends BaseHullMod {

//	private static float MANEUVER_PERCENT = 25;
//	private static float PD_RANGE = 75;
//	public static float FIGHTER_DAMAGE_BONUS = 25f;
//	public static float MISSILE_DAMAGE_BONUS = 25f;
	private static float MANEUVER_PERCENT = 10;
	private static float PD_RANGE = 30;
	public static float FIGHTER_DAMAGE_BONUS = 10f;
	public static float MISSILE_DAMAGE_BONUS = 10f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		float mult = MilitarizedSubsystems.getEffectMult(stats);
		stats.getDamageToFighters().modifyFlat(id, FIGHTER_DAMAGE_BONUS / 100f * mult);
		stats.getDamageToMissiles().modifyFlat(id, MISSILE_DAMAGE_BONUS / 100f * mult);
		
		stats.getBeamPDWeaponRangeBonus().modifyFlat(id, PD_RANGE * mult);
		stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id, PD_RANGE * mult);

		stats.getAcceleration().modifyPercent(id, MANEUVER_PERCENT * mult);
		stats.getDeceleration().modifyPercent(id, MANEUVER_PERCENT * mult);
		stats.getTurnAcceleration().modifyPercent(id, MANEUVER_PERCENT * 2f * mult);
		stats.getMaxTurnRate().modifyPercent(id, MANEUVER_PERCENT * mult);
		
		stats.getDynamic().getMod(Stats.ACT_AS_COMBAT_SHIP).modifyFlat(id, 1f);
	}
	


	public String getDescriptionParam(int index, HullSize hullSize) {
		float mult = MilitarizedSubsystems.getEffectMult(null);
		if (index == 0) return "" + (int) Math.round(MANEUVER_PERCENT * mult) + "%";
		if (index == 1) return "" + (int) Math.round(PD_RANGE * mult);
		if (index == 2) return "" + (int)Math.round(FIGHTER_DAMAGE_BONUS * mult) + "%";
		return null;
	}
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (shipHasOtherModInCategory(ship, spec.getId(), HullMods.TAG_CIV_PACKAGE)) return false;
		return ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS);
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (shipHasOtherModInCategory(ship, spec.getId(), HullMods.TAG_CIV_PACKAGE)) {
			return "Can only install one combat package on a civilian-grade hull";
		}
		if (!ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) {
			return "Can only be installed on civilian-grade hulls with Militarized Subsystems";
		}
		return super.getUnapplicableReason(ship);
	}
	
	
	
}

