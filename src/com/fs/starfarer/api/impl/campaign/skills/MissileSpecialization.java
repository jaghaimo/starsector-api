package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MissileSpecialization {
	
	public static final float MISSILE_SPEC_SPEED_BONUS = 25f;
	public static final float MISSILE_SPEC_RANGE_MULT = 0.8f;
	public static final float MISSILE_SPEC_ACCEL_BONUS = 50f;
	public static final float MISSILE_TURN_RATE_BONUS = 50f;
	public static final float MISSILE_TURN_ACCEL_BONUS = 100f;
	
	public static final float MISSILE_SPEC_PERK_HEALTH_BONUS = 50f;
	
	public static final float MISSILE_SPEC_PERK_DAMAGE_BONUS = 25f;

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMissileMaxSpeedBonus().modifyPercent(id, MISSILE_SPEC_SPEED_BONUS);
			stats.getMissileWeaponRangeBonus().modifyMult(id, MISSILE_SPEC_RANGE_MULT);
			
			stats.getMissileAccelerationBonus().modifyPercent(id, MISSILE_SPEC_ACCEL_BONUS);
			stats.getMissileMaxTurnRateBonus().modifyPercent(id, MISSILE_TURN_RATE_BONUS);
			stats.getMissileTurnAccelerationBonus().modifyPercent(id, MISSILE_TURN_ACCEL_BONUS);
			
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMissileMaxSpeedBonus().unmodify(id);
			stats.getMissileWeaponRangeBonus().unmodify(id);
			
			stats.getMissileAccelerationBonus().unmodify(id);
			stats.getMissileMaxTurnRateBonus().unmodify(id);
			stats.getMissileTurnAccelerationBonus().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MISSILE_SPEC_SPEED_BONUS) + "% missile speed and maneuverability";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}

	public static class Level2 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMissileHealthBonus().modifyPercent(id, MISSILE_SPEC_PERK_HEALTH_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMissileHealthBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MISSILE_SPEC_PERK_HEALTH_BONUS) + "% missile, rocket, bomb, and torpedo hitpoints";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}

	}
	
	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMissileWeaponDamageMult().modifyPercent(id, MISSILE_SPEC_PERK_DAMAGE_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMissileWeaponDamageMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MISSILE_SPEC_PERK_DAMAGE_BONUS) + "% missile damage";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}

	}
}
