package com.fs.starfarer.api.impl.campaign.skills;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CombatEndurance {
	
	public static float PEAK_TIME_BONUS = 60;
	public static float DEGRADE_REDUCTION_PERCENT = 25f;
	public static float MAX_CR_BONUS = 15;
	
	//public static float OVERLOAD_REDUCTION = 30f;
	
	public static float MAX_REGEN_LEVEL = 0.5f;
	public static float REGEN_RATE = 0.005f;
	public static float TOTAL_REGEN_MAX_POINTS = 2000f;
	public static float TOTAL_REGEN_MAX_HULL_FRACTION = 0.5f;

	public static class Level1 implements ShipSkillEffect {
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
	
	public static class Level2 implements ShipSkillEffect {
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

	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMaxCombatReadiness().modifyFlat(id, MAX_CR_BONUS * 0.01f, "Combat Endurance skill");
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
	
	public static class Level4 extends BaseSkillEffectDescription implements ShipSkillEffect, AfterShipCreationSkillEffect {
		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
			ship.addListener(new CombatEnduranceRegen(ship));
		}

		public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
			ship.removeListenerOfClass(CombatEnduranceRegen.class);
		}
		
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
								TooltipMakerAPI info, float width) {
			initElite(stats, skill);
			
			info.addPara("When below %s hull, repair %s per second; maximum total repair is "
					+ "the higher of %s points or %s of maximum hull", 0f, hc, hc,
					"" + (int)Math.round(MAX_REGEN_LEVEL * 100f) + "%",
					//"" + (int)Math.round(REGEN_RATE * 100f) + "%",
					"" + Misc.getRoundedValueMaxOneAfterDecimal(REGEN_RATE * 100f) + "%",
					"" + (int)Math.round(TOTAL_REGEN_MAX_POINTS) + "",
					"" + (int)Math.round(TOTAL_REGEN_MAX_HULL_FRACTION * 100f) + "%"
			);

			//info.addSpacer(5f);
		}
	}
	
	public static class CombatEnduranceRegen implements DamageTakenModifier, AdvanceableListener {
		protected ShipAPI ship;
		protected boolean inited = false;
		protected float limit = 0f;
		protected float repaired = 0f;
		protected String repKey1;
		protected String repKey2;
		public CombatEnduranceRegen(ShipAPI ship) {
			this.ship = ship;
		}
		
		protected void init() {
			inited = true;
			
			float maxHull = ship.getMaxHitpoints();
			limit = Math.max(TOTAL_REGEN_MAX_POINTS, TOTAL_REGEN_MAX_HULL_FRACTION * maxHull);
			
			repKey1 = "CombatEnduranceRegen_ " + ship.getId() + "_repaired";
			repKey2 = "CombatEnduranceRegen_ " + ship.getCaptain().getId() + "_repaired";
			float r1 = getRepaired(repKey1);
			float r2 = getRepaired(repKey2);
			
			repaired = Math.max(repaired, r1);
			repaired = Math.max(repaired, r2);
		}
		
		protected float getRepaired(String key) {
			Float r = (Float) Global.getCombatEngine().getCustomData().get(key);
			if (r == null) r = 0f;
			return r;
		}
		
		public void advance(float amount) {
			if (!inited) {
				init();
			}
			
			if (repaired >= limit) return;
			if (ship.getHullLevel() >= MAX_REGEN_LEVEL) return;
			if (ship.isHulk()) return;
			
			float maxHull = ship.getMaxHitpoints();
			float currHull = ship.getHitpoints();
			float maxPoints = maxHull * MAX_REGEN_LEVEL;
			
			float repairAmount = Math.min(limit - repaired, maxHull * REGEN_RATE * amount);
			// fix up remainder instantly so that there's no incentive to wait to finish a fight
			// so that hull is higher when it's over
			// actually - don't need to do this due to ship.getLowestHullLevelReached()
			// always being the hull level after combat
//			if (Global.getCombatEngine().isCombatOver()) {
//				repairAmount = limit - repaired;
//			}
			if (repairAmount > maxPoints - currHull) repairAmount = maxPoints - currHull;
			
			if (repairAmount > 0) {
				ship.setHitpoints(ship.getHitpoints() + repairAmount);
				repaired += repairAmount;
				Global.getCombatEngine().getCustomData().put(repKey1, repaired);
				// allow *some* extra repairs when switching to another ship
				Global.getCombatEngine().getCustomData().put(repKey2, repaired * 0.5f);
			}
		}

		public String modifyDamageTaken(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			return null;
		}

	}

	
//	public static final float MALFUNCTION_REDUCTION = 50f;
//	public static final float CRITICAL_MALFUNCTION_REDUCTION = 50f;
//	public static final float PEAK_TIME_BONUS = 25;
//	public static final float MASTERY_PEAK_TIME_BONUS = 10;
//	//public static final float RECOVERY_RATE_BONUS = 25;
//	public static final float MAX_CR_BONUS = 15;
//
//	public static class Level1 implements ShipSkillEffect {
//
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getPeakCRDuration().modifyPercent(id, PEAK_TIME_BONUS);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getPeakCRDuration().unmodify(id);
//		}	
//		
//		public String getEffectDescription(float level) {
//			return "+" + (int)(PEAK_TIME_BONUS) + "% peak operating time";
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
//
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
//	
//	
////	public static class Level2 implements ShipSkillEffect {
////
////		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
////			stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(id, RECOVERY_RATE_BONUS);
////			//stats.getRepairRatePercentPerDay().modifyPercent(id, RECOVERY_RATE_BONUS);
////		}
////		
////		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
////			stats.getBaseCRRecoveryRatePercentPerDay().unmodify(id);
////			//stats.getRepairRatePercentPerDay().unmodify(id);
////		}
////		
////		public String getEffectDescription(float level) {
////			//return "" + (int)(RECOVERY_RATE_BONUS) + "% faster repairs and CR recovery";
////			return "" + (int)(RECOVERY_RATE_BONUS) + "% faster CR recovery";
////		}
////		
////		public String getEffectPerLevelDescription() {
////			return null;
////		}
////
////		public ScopeDescription getScopeDescription() {
////			return ScopeDescription.PILOTED_SHIP;
////		}
////
////	}
//	
//	public static class Level3 implements ShipSkillEffect {
//
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getMaxCombatReadiness().modifyFlat(id, MAX_CR_BONUS * 0.01f, "Combat endurance skill");
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getMaxCombatReadiness().unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "+" + (int)(MAX_CR_BONUS) + "% maximum combat readiness";
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
//	
//	
//	public static class Mastery implements ShipSkillEffect {
//
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getPeakCRDuration().modifyPercent(id, MASTERY_PEAK_TIME_BONUS);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getPeakCRDuration().unmodify(id);
//		}	
//		
//		public String getEffectDescription(float level) {
//			return "+" + (int)(MASTERY_PEAK_TIME_BONUS) + "% peak operating time";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.ALL_SHIPS;
//		}
//	}
}
