package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ImpactMitigation {
	
	public static final float ARMOR_BONUS = 50;
//	public static final float MAX_DAMAGE_REDUCTION_BONUS = 0.05f;
//	public static final float MIN_ARMOR_FRACTION_BONUS = 0.1f;
	public static final float ARMOR_DAMAGE_REDUCTION = 25f;
	public static final float ARMOR_KINETIC_REDUCTION = 50f;
	
	public static final float DAMAGE_TO_MODULES_REDUCTION = 50;
	
	public static class Level1 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			//stats.getArmorBonus().modifyFlat(id, ARMOR_BONUS);
			stats.getEffectiveArmorBonus().modifyFlat(id, ARMOR_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			//stats.getArmorBonus().unmodify(id);
			stats.getEffectiveArmorBonus().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(ARMOR_BONUS) + " armor for damage reduction calculation only";
			//return "+" + (int)(ARMOR_BONUS) + " maximum armor";
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
			stats.getArmorDamageTakenMult().modifyMult(id, 1f - ARMOR_DAMAGE_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getArmorDamageTakenMult().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "-" + (int)(ARMOR_DAMAGE_REDUCTION) + "% armor damage taken";
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
			stats.getKineticArmorDamageTakenMult().modifyMult(id, 1f - ARMOR_KINETIC_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getKineticArmorDamageTakenMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)(ARMOR_KINETIC_REDUCTION) + "% kinetic damage taken by armor";
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
			stats.getEngineDamageTakenMult().modifyMult(id, 1f - DAMAGE_TO_MODULES_REDUCTION / 100f);
			stats.getWeaponDamageTakenMult().modifyMult(id, 1f - DAMAGE_TO_MODULES_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getEngineDamageTakenMult().unmodify(id);
			stats.getWeaponDamageTakenMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)(DAMAGE_TO_MODULES_REDUCTION) + "% weapon and engine damage taken";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
//	public static class Level1 implements ShipSkillEffect {
//
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getMaxArmorDamageReduction().modifyFlat(id, MAX_DAMAGE_REDUCTION_BONUS);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getMaxArmorDamageReduction().unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "Maximum damage reduction by armor raised from 85% to 90%";
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

//	public static class Level2 implements ShipSkillEffect {
//
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getMinArmorFraction().modifyFlat(id, MIN_ARMOR_FRACTION_BONUS);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getMinArmorFraction().unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "Minimum armor value for damage reduction raised from 5% to 15% of maximum";
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
