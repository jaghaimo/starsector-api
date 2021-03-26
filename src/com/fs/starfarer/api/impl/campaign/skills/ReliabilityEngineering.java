package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ReliabilityEngineering {
	
	public static final float PEAK_TIME_BONUS = 60;
	public static final float DEGRADE_REDUCTION_PERCENT = 25f;
	public static final float MAX_CR_BONUS = 15;
	
	public static final float OVERLOAD_REDUCTION = 30f;

	public static class Level1 implements ShipSkillEffect {
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "If lost in combat, ship is almost always recoverable";
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
	
	public static class Level3 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getCRLossPerSecondPercent().modifyMult(id, 1f - DEGRADE_REDUCTION_PERCENT / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getCRLossPerSecondPercent().unmodifyMult(id);
		}	
		
		public String getEffectDescription(float level) {
			return "-" + (int)(DEGRADE_REDUCTION_PERCENT) + "% combat readiness degradation rate after peak performance time runs out";
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
			stats.getMaxCombatReadiness().modifyFlat(id, MAX_CR_BONUS * 0.01f, "Reliabilty Engineering skill");
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
	
	public static class Level5 implements ShipSkillEffect {
		
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
	
	

//	public static class Level2 implements ShipSkillEffect {
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getCriticalMalfunctionChance().modifyMult(id, 1f - CRITICAL_MALFUNCTION_REDUCTION / 100f);
//			stats.getWeaponMalfunctionChance().modifyMult(id, 1f - MALFUNCTION_REDUCTION / 100f);
//			stats.getEngineMalfunctionChance().modifyMult(id, 1f - MALFUNCTION_REDUCTION / 100f);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getCriticalMalfunctionChance().unmodify(id);
//			stats.getWeaponMalfunctionChance().unmodify(id);
//			stats.getEngineMalfunctionChance().unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			//return "" + (int)(RECOVERY_RATE_BONUS) + "% faster repairs and CR recovery";
//			//return "-" + (int)(CRITICAL_MALFUNCTION_REDUCTION) + "% chance of critical malfunctions when at low combat readiness";
//			return "-" + (int)(CRITICAL_MALFUNCTION_REDUCTION) + "% chance of malfunctions when at low combat readiness";
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
