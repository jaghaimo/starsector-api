package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class DamageControl {
	
	public static final float INSTA_REPAIR = 0.25f;
	public static final float CREW_LOSS_REDUCTION = 50;
	public static final float MODULE_REPAIR_BONUS = 50;
	public static final float HULL_DAMAGE_REDUCTION = 25;
//	public static final float OVERLOAD_REDUCTION = 25;

	public static class Level2 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getCrewLossMult().modifyMult(id, 1f - CREW_LOSS_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getCrewLossMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)(CREW_LOSS_REDUCTION) + "% crew lost due to hull damage in combat";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
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

	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			float timeMult = 1f / ((100f + MODULE_REPAIR_BONUS) / 100f);
			stats.getCombatWeaponRepairTimeMult().modifyMult(id, timeMult);
			stats.getCombatEngineRepairTimeMult().modifyMult(id, timeMult);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getCombatWeaponRepairTimeMult().unmodify(id);
			stats.getCombatEngineRepairTimeMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "" + (int)(MODULE_REPAIR_BONUS) + "% faster in-combat weapon and engine repairs";
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
			stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).modifyFlat(id, INSTA_REPAIR);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "" + (int) Math.round(INSTA_REPAIR * 100f) + "% of hull and armor damage taken repaired after combat ends, at no cost";
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
			stats.getHullDamageTakenMult().modifyMult(id, 1f - HULL_DAMAGE_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getHullDamageTakenMult().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "-" + (int)(HULL_DAMAGE_REDUCTION) + "% hull damage taken";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	/*
	public static class Level3B implements ShipSkillEffect {
		
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
	*/
}
