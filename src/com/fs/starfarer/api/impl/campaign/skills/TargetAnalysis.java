package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class TargetAnalysis {
	
	public static final float DAMAGE_TO_MODULES_BONUS = 100;
//	public static final float DAMAGE_TO_SHIELDS_BONUS = 15;
//	public static final float HIT_STRENGTH_BONUS = 50;
	
	public static final float DAMAGE_TO_DESTROYERS = 10;
	public static final float DAMAGE_TO_CRUISERS = 15;
	public static final float DAMAGE_TO_CAPITALS = 20;
	

	public static class Level1 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDamageToDestroyers().modifyPercent(id, DAMAGE_TO_DESTROYERS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToDestroyers().unmodifyPercent(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DAMAGE_TO_DESTROYERS) + "% damage to destroyers";
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
			stats.getDamageToCruisers().modifyPercent(id, DAMAGE_TO_CRUISERS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToCruisers().unmodifyPercent(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DAMAGE_TO_CRUISERS) + "% damage to cruisers";
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
			stats.getDamageToCapital().modifyPercent(id, DAMAGE_TO_CAPITALS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToCapital().unmodifyPercent(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DAMAGE_TO_CAPITALS) + "% damage to capital ships";
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
			stats.getDamageToTargetEnginesMult().modifyPercent(id, DAMAGE_TO_MODULES_BONUS);
			stats.getDamageToTargetWeaponsMult().modifyPercent(id, DAMAGE_TO_MODULES_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToTargetEnginesMult().unmodify(id);
			stats.getDamageToTargetWeaponsMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DAMAGE_TO_MODULES_BONUS) + "% damage to weapons and engines";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}

//	public static class Level2 implements ShipSkillEffect {
//
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getDamageToTargetShieldsMult().modifyPercent(id, DAMAGE_TO_SHIELDS_BONUS);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getDamageToTargetShieldsMult().unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "+" + (int)(DAMAGE_TO_SHIELDS_BONUS) + "% damage to shields";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.PILOTED_SHIP;
//		}
//
//	}
//	
//	public static class Level3 implements ShipSkillEffect {
//
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getHitStrengthBonus().modifyPercent(id, HIT_STRENGTH_BONUS);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getHitStrengthBonus().unmodify(id);
//		}	
//		
//		public String getEffectDescription(float level) {
//			return "+" + (int)(HIT_STRENGTH_BONUS) + "% hit strength for armor damage reduction calculation only";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.PILOTED_SHIP;
//		}
//	}
	
}
