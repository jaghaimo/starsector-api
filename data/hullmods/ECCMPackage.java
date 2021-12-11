package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ECCMPackage extends BaseHullMod {

	// original values from MissileSpecialization
//	public static final float MISSILE_SPEC_SPEED_BONUS = 25f;
//	public static final float MISSILE_SPEC_RANGE_MULT = 0.8f;
//	public static final float MISSILE_SPEC_ACCEL_BONUS = 50f;
//	public static final float MISSILE_TURN_RATE_BONUS = 50f;
//	public static final float MISSILE_TURN_ACCEL_BONUS = 100f;
	// original values from ECCM
//	stats.getMissileMaxSpeedBonus().modifyPercent(id, 10f);
//	stats.getMissileAccelerationBonus().modifyPercent(id, 100f);
//	stats.getMissileMaxTurnRateBonus().modifyPercent(id, 10f);
//	stats.getMissileTurnAccelerationBonus().modifyPercent(id, 50f);
	
	
	public static final float MISSILE_SPEED_BONUS = 25f;
	public static final float MISSILE_RANGE_MULT = 0.8f;
	public static final float MISSILE_ACCEL_BONUS = 150f;
	public static final float MISSILE_RATE_BONUS = 50f;
	public static final float MISSILE_TURN_ACCEL_BONUS = 150f;
	
	public static final float EW_PENALTY_MULT = 0.5f;
	
	public static final float ECCM_CHANCE = 0.5f;
	public static final float GUIDANCE_IMPROVEMENT = 1f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEccmChance().modifyFlat(id, ECCM_CHANCE);
		stats.getMissileGuidance().modifyFlat(id, GUIDANCE_IMPROVEMENT);
		
//		stats.getMissileMaxSpeedBonus().modifyPercent(id, 10f);
//		stats.getMissileAccelerationBonus().modifyPercent(id, 100f);
//		stats.getMissileMaxTurnRateBonus().modifyPercent(id, 10f);
//		stats.getMissileTurnAccelerationBonus().modifyPercent(id, 50f);
		
		stats.getMissileMaxSpeedBonus().modifyPercent(id, MISSILE_SPEED_BONUS);
		stats.getMissileWeaponRangeBonus().modifyMult(id, MISSILE_RANGE_MULT);
		stats.getMissileAccelerationBonus().modifyPercent(id, MISSILE_ACCEL_BONUS);
		stats.getMissileMaxTurnRateBonus().modifyPercent(id, MISSILE_RATE_BONUS);
		stats.getMissileTurnAccelerationBonus().modifyPercent(id, MISSILE_TURN_ACCEL_BONUS);
		
		
		stats.getDynamic().getStat(Stats.ELECTRONIC_WARFARE_PENALTY_MULT).modifyMult(id, EW_PENALTY_MULT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) (ECCM_CHANCE * 100f) + "%";
		if (index == 1) return "" + (int) (MISSILE_SPEED_BONUS) + "%";
		if (index == 2) return "" + (int) (MISSILE_RATE_BONUS) + "%";
		if (index == 3) return "" + (int) ((1f - EW_PENALTY_MULT) * 100f) + "%";
		return null;
	}


}



