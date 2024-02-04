package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SystemsExpertise {
	
	
	//public static final float CHARGES_PERCENT = 100f;
	public static float BONUS_CHARGES = 1f;
	public static float REGEN_PERCENT = 50f;
	public static float SYSTEM_COOLDOWN_REDUCTION_PERCENT = 33f;
	public static float RANGE_PERCENT = 50f;
	
	public static float PEAK_TIME_BONUS = 30f;
	public static float OVERLOAD_REDUCTION = 25f;
	public static float MALFUNCTION_CHANCE_MULT = 0.5f;
	
	public static float ELITE_DAMAGE_REDUCTION = 10f;
	

	public static class Level1 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			//stats.getSystemUsesBonus().modifyPercent(id, CHARGES_PERCENT);
			stats.getSystemUsesBonus().modifyFlat(id, BONUS_CHARGES);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getSystemUsesBonus().unmodifyPercent(id);
		}
		
		public String getEffectDescription(float level) {
			//return "If the ship's system has charges: +" + (int)(CHARGES_PERCENT) + "% charges";
			return "If the ship's system has charges: +1 charge";
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
			stats.getSystemRegenBonus().modifyPercent(id, REGEN_PERCENT);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getSystemRegenBonus().unmodifyPercent(id);
		}
		
		public String getEffectDescription(float level) {
			return "If the ship's system regenerates charges: +" + (int)(REGEN_PERCENT) + "% regeneration rate";
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
			stats.getSystemRangeBonus().modifyPercent(id, RANGE_PERCENT);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getSystemRangeBonus().unmodifyPercent(id);
		}
		
		public String getEffectDescription(float level) {
			return "If the ship's system has range: +" + (int)(RANGE_PERCENT) + "% range";
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
			stats.getSystemCooldownBonus().modifyMult(id, 1f - SYSTEM_COOLDOWN_REDUCTION_PERCENT / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getSystemCooldownBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "If the ship's system has a cooldown: -" + (int)(SYSTEM_COOLDOWN_REDUCTION_PERCENT) + "% cooldown";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}


	public static class Level5 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getPeakCRDuration().modifyFlat(id, PEAK_TIME_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getPeakCRDuration().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(PEAK_TIME_BONUS) + " seconds peak operating time";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level6 implements ShipSkillEffect {
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getOverloadTimeMod().modifyMult(id, 1f - OVERLOAD_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getOverloadTimeMod().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "-" + (int)(OVERLOAD_REDUCTION) + "% overload duration";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level7 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getWeaponMalfunctionChance().modifyMult(id, MALFUNCTION_CHANCE_MULT);	
			stats.getEngineMalfunctionChance().modifyMult(id, MALFUNCTION_CHANCE_MULT);	
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getWeaponMalfunctionChance().unmodifyMult(id);	
			stats.getEngineMalfunctionChance().unmodifyMult(id);	
		}
		
		public String getEffectDescription(float level) {
			String percent = "" + (int)Math.round((1f - MALFUNCTION_CHANCE_MULT) * 100f) + "%";
			return "Chance of malfunctions when at low combat readiness reduced by " + percent;
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level8 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getArmorDamageTakenMult().modifyMult(id, 1f - ELITE_DAMAGE_REDUCTION / 100f);
			stats.getHullDamageTakenMult().modifyMult(id, 1f - ELITE_DAMAGE_REDUCTION / 100f);
			stats.getShieldDamageTakenMult().modifyMult(id, 1f - ELITE_DAMAGE_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getArmorDamageTakenMult().unmodifyMult(id);
			stats.getHullDamageTakenMult().unmodifyMult(id);
			stats.getShieldDamageTakenMult().unmodifyMult(id);	
		}
		
		public String getEffectDescription(float level) {
			String percent = "-" + (int)ELITE_DAMAGE_REDUCTION + "%";
			return percent + " damage taken";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	
}
