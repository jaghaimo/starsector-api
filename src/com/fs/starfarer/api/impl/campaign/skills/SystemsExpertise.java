package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SystemsExpertise {
	
	public static float PEAK_TIME_BONUS = 30f;
	
	public static final float CHARGES_PERCENT = 100f;
	public static final float REGEN_PERCENT = 50f;
	public static final float SYSTEM_COOLDOWN_REDUCTION_PERCENT = 33f;
	public static final float RANGE_PERCENT = 50f;
	

	public static class Level1 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			//stats.getSystemUsesBonus().modifyPercent(id, CHARGES_PERCENT);
			stats.getSystemUsesBonus().modifyFlat(id, 1);
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
			return "+" + (int)(PEAK_TIME_BONUS) + " seconds peak operating time\n";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	
}
