package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CombatEndurance {
	
	public static final float MALFUNCTION_REDUCTION = 50f;
	public static final float CRITICAL_MALFUNCTION_REDUCTION = 50f;
	public static final float PEAK_TIME_BONUS = 25;
	public static final float MASTERY_PEAK_TIME_BONUS = 10;
	//public static final float RECOVERY_RATE_BONUS = 25;
	public static final float MAX_CR_BONUS = 15;

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getPeakCRDuration().modifyPercent(id, PEAK_TIME_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getPeakCRDuration().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(PEAK_TIME_BONUS) + "% peak operating time";
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
			stats.getCriticalMalfunctionChance().modifyMult(id, 1f - CRITICAL_MALFUNCTION_REDUCTION / 100f);
			stats.getWeaponMalfunctionChance().modifyMult(id, 1f - MALFUNCTION_REDUCTION / 100f);
			stats.getEngineMalfunctionChance().modifyMult(id, 1f - MALFUNCTION_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getCriticalMalfunctionChance().unmodify(id);
			stats.getWeaponMalfunctionChance().unmodify(id);
			stats.getEngineMalfunctionChance().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			//return "" + (int)(RECOVERY_RATE_BONUS) + "% faster repairs and CR recovery";
			//return "-" + (int)(CRITICAL_MALFUNCTION_REDUCTION) + "% chance of critical malfunctions when at low combat readiness";
			return "-" + (int)(CRITICAL_MALFUNCTION_REDUCTION) + "% chance of malfunctions when at low combat readiness";
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
//			stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(id, RECOVERY_RATE_BONUS);
//			//stats.getRepairRatePercentPerDay().modifyPercent(id, RECOVERY_RATE_BONUS);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getBaseCRRecoveryRatePercentPerDay().unmodify(id);
//			//stats.getRepairRatePercentPerDay().unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			//return "" + (int)(RECOVERY_RATE_BONUS) + "% faster repairs and CR recovery";
//			return "" + (int)(RECOVERY_RATE_BONUS) + "% faster CR recovery";
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
	
	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMaxCombatReadiness().modifyFlat(id, MAX_CR_BONUS * 0.01f, "Combat endurance skill");
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxCombatReadiness().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MAX_CR_BONUS) + "% maximum combat readiness";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	
	public static class Mastery implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getPeakCRDuration().modifyPercent(id, MASTERY_PEAK_TIME_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getPeakCRDuration().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MASTERY_PEAK_TIME_BONUS) + "% peak operating time";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
}
