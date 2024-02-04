package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class BallisticMastery {
	
	public static float PROJ_SPEED_BONUS = 33;
	
	public static float DAMAGE_BONUS = 10f;
	public static float DAMAGE_ELITE = 5f;
	public static float RANGE_BONUS = 10f;
	
	
	public static class Level1 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getBallisticWeaponDamageMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DAMAGE_BONUS) + "% damage dealt by ballistic weapons";
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
			stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getBallisticWeaponRangeBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(RANGE_BONUS) + "% ballistic weapon range";
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
			stats.getBallisticProjectileSpeedMult().modifyPercent(id, PROJ_SPEED_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getBallisticProjectileSpeedMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(PROJ_SPEED_BONUS) + "% ballistic projectile speed";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}


	public static class Level4 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_ELITE);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getBallisticWeaponDamageMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DAMAGE_ELITE) + "% damage dealt by ballistic weapons";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}








}