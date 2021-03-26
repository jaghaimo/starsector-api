package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class AssaultPackage extends BaseHullMod {

//	public static float FLUX_PERCENT = 25f;
//	public static float HULL_PERCENT = 25f;
//	public static float ARMOR_PERCENT = 25f;
	public static float FLUX_CAPACITY_PERCENT = 10f;
	public static float HULL_PERCENT = 10f;
	public static float ARMOR_PERCENT = 5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		float mult = MilitarizedSubsystems.getEffectMult(stats);
		stats.getHullBonus().modifyPercent(id, HULL_PERCENT * mult);
		stats.getArmorBonus().modifyPercent(id, ARMOR_PERCENT * mult);
		stats.getFluxCapacity().modifyPercent(id, FLUX_CAPACITY_PERCENT * mult);
		
		stats.getDynamic().getMod(Stats.ACT_AS_COMBAT_SHIP).modifyFlat(id, 1f);
	}
	


	public String getDescriptionParam(int index, HullSize hullSize) {
		float mult = MilitarizedSubsystems.getEffectMult(null);
		if (index == 0) return "" + (int) Math.round(HULL_PERCENT * mult) + "%";
		if (index == 1) return "" + (int) Math.round(ARMOR_PERCENT * mult) + "%";
		if (index == 2) return "" + (int)Math.round(FLUX_CAPACITY_PERCENT * mult) + "%";
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

