package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class Sensors {
	
	public static final float DETECTED_BONUS = 25f;
	public static final float GO_DARK_MULT = 0.5f;
	
	public static final float SENSOR_BONUS = 25f;
	public static final float SENSOR_BURST_PENALTY_MULT = 0.5f;
	
	

	public static class Level1 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.CAN_SEE_NASCENT_POINTS).modifyFlat(id, 1);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.CAN_SEE_NASCENT_POINTS).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "Can detect nascent gravity wells around star systems";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level2A implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDetectedRangeMod().modifyMult(id, 1f - DETECTED_BONUS / 100f, "Sensors skill");
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDetectedRangeMod().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int) DETECTED_BONUS + "% detected-at range";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level2B implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.GO_DARK_DETECTED_AT_MULT).modifyMult(id, GO_DARK_MULT);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.GO_DARK_DETECTED_AT_MULT).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "Doubles the effectiveness of the \"Go Dark\" ability";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level3A implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getSensorRangeMod().modifyPercent(id, SENSOR_BONUS, "Sensors skill");
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getSensorRangeMod().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) SENSOR_BONUS + "% sensor range";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	
	public static class Level3B implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.SENSOR_BURST_BURN_PENALTY_MULT).modifyMult(id, SENSOR_BURST_PENALTY_MULT);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.SENSOR_BURST_BURN_PENALTY_MULT).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "Halves the burn level penalty of the \"Active Sensor Burst\" ability";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
}



