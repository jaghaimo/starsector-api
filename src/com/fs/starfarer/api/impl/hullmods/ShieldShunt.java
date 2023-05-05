package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public class ShieldShunt extends BaseHullMod {

	//public static float EMP_RESISTANCE = 50f;
	//public static float VENT_RATE_BONUS = 50f;
	public static float ARMOR_BONUS = 15f;
	public static float SMOD_ARMOR_BONUS = 15f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		
		//stats.getVentRateMult().modifyPercent(id, VENT_RATE_BONUS);
		stats.getArmorBonus().modifyPercent(id, ARMOR_BONUS + (sMod ? SMOD_ARMOR_BONUS : 0));
		//stats.getEmpDamageTakenMult().modifyMult(id, 1f - EMP_RESISTANCE * 0.01f);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.setShield(ShieldType.NONE, 0f, 1f, 1f);
	}


	public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return "" + (int) EMP_RESISTANCE + "%";
		//if (index == 0) return "" + (int) VENT_RATE_BONUS + "%";
		if (index == 0) return "" + (int) ARMOR_BONUS + "%";
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().getHullSpec().getShieldType() == ShieldType.NONE && 
				!ship.getVariant().hasHullMod("frontshield")) return false;
		if (ship.getVariant().hasHullMod(HullMods.SHIELD_SHUNT)) return true;
		if (ship.getVariant().hasHullMod(HullMods.MAKESHIFT_GENERATOR)) return false;
		return ship != null && ship.getShield() != null;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(HullMods.MAKESHIFT_GENERATOR)) {
			return "Incompatible with Makeshift Shield Generator";
		}
		return "Ship has no shields";
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SMOD_ARMOR_BONUS + "%";
		return null;
	}
	
	public boolean hasSModEffect() {
		// breaks something if it can be built in - I think something to do with preconditions for
		// shield-related hullmods; not 100% sure on details but sure there was a problem -am
		// Ah! The issue was being able to build it in and then some kind of order-of-operations with
		// Makeshift Shield Generator. Made those incompatible. -am
		return true;
	}
	
}









