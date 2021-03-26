package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class PointDefense {
	
	public static float FIGHTER_DAMAGE_BONUS = 100f;
	public static float MISSILE_DAMAGE_BONUS = 50f;
//	public static float FIGHTER_DAMAGE_BONUS = 75f;
//	public static float MISSILE_DAMAGE_BONUS = 75f;
	
	public static float PD_RANGE_BONUS_FLAT = 100f;
	
	
	
	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDamageToFighters().modifyFlat(id, FIGHTER_DAMAGE_BONUS / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToFighters().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(FIGHTER_DAMAGE_BONUS) + "% damage to fighters";
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
			stats.getDamageToMissiles().modifyFlat(id, MISSILE_DAMAGE_BONUS / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToMissiles().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MISSILE_DAMAGE_BONUS) + "% damage to missiles";
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
			stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id, PD_RANGE_BONUS_FLAT);
			stats.getBeamPDWeaponRangeBonus().modifyFlat(id, PD_RANGE_BONUS_FLAT);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getNonBeamPDWeaponRangeBonus().unmodifyFlat(id);
			stats.getBeamPDWeaponRangeBonus().unmodifyFlat(id);
		}	
		
		public String getEffectDescription(float level) {
			//return "+" + (int)(PD_RANGE_BONUS_FLAT) + " range ";
			return "Extends the range of point-defense weapons by " + (int)(PD_RANGE_BONUS_FLAT) + "";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}
