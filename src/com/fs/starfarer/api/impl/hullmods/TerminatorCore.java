package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class TerminatorCore extends BaseLogisticsHullMod {

	//public static float ROF_MULT = 2f;
	//public static float EXTRA_CHARGES = 1000f;
	
	public static float DAMAGE_MISSILES_PERCENT = 100f;
	public static float DAMAGE_FIGHTERS_PERCENT = 100f;
	
	public static float BEAM_RANGE_BONUS = 300f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		//stats.getEnergyAmmoBonus().modifyFlat(id, EXTRA_CHARGES);
		//stats.getEnergyRoFMult().modifyMult(id, ROF_MULT);
		
//		stats.getRecoilDecayMult().modifyMult(id, 2f);
//		stats.getRecoilPerShotMult().modifyMult(id, 0f);
		stats.getDamageToMissiles().modifyPercent(id, DAMAGE_MISSILES_PERCENT);
		stats.getDamageToFighters().modifyPercent(id, DAMAGE_FIGHTERS_PERCENT);
		//stats.getProjectileSpeedMult().modifyMult(id, 100f);
		stats.getBeamWeaponTurnRateBonus().modifyMult(id, 2f);
		stats.getBeamWeaponRangeBonus().modifyFlat(id, BEAM_RANGE_BONUS);
		//stats.getBeamWeaponRangeBonus().modifyFlat(id, 300f);
		stats.getAutofireAimAccuracy().modifyFlat(id, 1f);
		
		stats.getEngineDamageTakenMult().modifyMult(id, 0f);
		
		stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f);
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		super.applyEffectsAfterShipCreation(ship, id);
		
//		ship.getShield().setRingColor(new Color(255, 255, 255, 255));
//		ship.getShield().setInnerColor(new Color(255, 0, 0, 75));
		//ship.getEngineController().extendFlame(this, 0f, 0f, 1.5f);
	}



	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) DAMAGE_MISSILES_PERCENT + "%";
		if (index == 1) return "" + (int) BEAM_RANGE_BONUS;
		return null;
	}

}







