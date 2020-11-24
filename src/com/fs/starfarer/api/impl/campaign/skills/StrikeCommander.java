package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class StrikeCommander {
	
	public static final float MISSILE_SPEED_BONUS = 25f;
	public static final float MISSILE_RANGE_MULT = 0.8f;
	
	public static final float MISSILE_HITPOINTS_BONUS = 50f;
	public static final float STRIKE_DAMAGE_BONUS = 20f;
	
	

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMissileMaxSpeedBonus().modifyPercent(id, MISSILE_SPEED_BONUS);
			stats.getMissileWeaponRangeBonus().modifyMult(id, MISSILE_RANGE_MULT);
			
			stats.getMissileAccelerationBonus().modifyPercent(id, MISSILE_SPEED_BONUS);
			stats.getMissileMaxTurnRateBonus().modifyPercent(id, MISSILE_SPEED_BONUS * 2f);
			stats.getMissileTurnAccelerationBonus().modifyPercent(id, MISSILE_SPEED_BONUS);
			
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMissileMaxSpeedBonus().unmodify(id);
			stats.getMissileWeaponRangeBonus().unmodify(id);
			
			stats.getMissileAccelerationBonus().unmodify(id);
			stats.getMissileMaxTurnRateBonus().unmodify(id);
			stats.getMissileTurnAccelerationBonus().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MISSILE_SPEED_BONUS) + "% missile speed and maneuverability";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.SHIP_FIGHTERS;
		}
	}

	public static class Level2 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMissileHealthBonus().modifyPercent(id, MISSILE_HITPOINTS_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMissileHealthBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MISSILE_HITPOINTS_BONUS) + "% missile, rocket, bomb, and torpedo hitpoints";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.SHIP_FIGHTERS;
		}
	}
	
	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDamageToDestroyers().modifyPercent(id, STRIKE_DAMAGE_BONUS);
			stats.getDamageToCruisers().modifyPercent(id, STRIKE_DAMAGE_BONUS);
			stats.getDamageToCapital().modifyPercent(id, STRIKE_DAMAGE_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToDestroyers().unmodify(id);
			stats.getDamageToCruisers().unmodify(id);
			stats.getDamageToCapital().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(STRIKE_DAMAGE_BONUS) + "% damage to ships of destroyer size and larger";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.SHIP_FIGHTERS;
		}
	}
	
}
