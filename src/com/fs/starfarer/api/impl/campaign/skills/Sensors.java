package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class Sensors {
	
	public static float DETECTED_BONUS = 25f;
	public static float SENSOR_BONUS = 25f;
	
	public static float SLOW_BURN_BONUS = 3f;
	
	public static float GO_DARK_MULT = 0.5f;
	public static float SENSOR_BURST_PENALTY_MULT = 0.5f;
	
	

	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			//int base = (int) RingSystemTerrainPlugin.MAX_SNEAK_BURN_LEVEL;
//			return "*A slow-moving fleet is harder to detect in some types of terrain, and can avoid some hazards. " +
//				"Several abilities also make the fleet move slowly when they are activated. The base burn " +
//				"level at which a fleet is considered to be slow-moving is " + base + ".";			
			//int reduction = (int)Math.round((1f - Misc.SNEAK_BURN_MULT) * 100f);
			return "*A slow-moving fleet is harder to detect in some types of terrain, and can avoid some hazards. " +
				"Some abilities also make the fleet move slowly when activated. A fleet is considered " +
				"slow-moving at a burn level of half that of its slowest ship.";			
		}
		public Color[] getHighlightColors() {
			return null;
//			Color h = Misc.getHighlightColor();
//			h = Misc.getDarkHighlightColor();
//			return new Color[] {h};
		}
		public String[] getHighlights() {
			return null;
//			int base = (int) RingSystemTerrainPlugin.MAX_SNEAK_BURN_LEVEL;
//			return new String [] {"" + base};
		}
		public Color getTextColor() {
			return null;
		}
	}
//	public static class Level1 implements FleetStatsSkillEffect {
//		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			stats.getDynamic().getMod(Stats.CAN_SEE_NASCENT_POINTS).modifyFlat(id, 1);
//		}
//		
//		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getDynamic().getMod(Stats.CAN_SEE_NASCENT_POINTS).unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "Can detect nascent gravity wells around star systems";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.FLEET;
//		}
//	}
	
	public static class Level1 implements FleetStatsSkillEffect {
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
	
	public static class Level2 implements FleetStatsSkillEffect {
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
	
	public static class Level3 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.MOVE_SLOW_SPEED_BONUS_MOD).modifyFlat(id, SLOW_BURN_BONUS);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.MOVE_SLOW_SPEED_BONUS_MOD).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) SLOW_BURN_BONUS + " to burn level at which the fleet is considered to be moving slowly*";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
//	public static class Level2B implements FleetStatsSkillEffect {
//		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			stats.getDynamic().getStat(Stats.GO_DARK_DETECTED_AT_MULT).modifyMult(id, GO_DARK_MULT);
//		}
//		
//		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getDynamic().getStat(Stats.GO_DARK_DETECTED_AT_MULT).unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "Doubles the effectiveness of the \"Go Dark\" ability";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.FLEET;
//		}
//	}
//	
//	
//	public static class Level3B implements FleetStatsSkillEffect {
//		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			stats.getDynamic().getStat(Stats.SENSOR_BURST_BURN_PENALTY_MULT).modifyMult(id, SENSOR_BURST_PENALTY_MULT);
//		}
//		
//		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getDynamic().getStat(Stats.SENSOR_BURST_BURN_PENALTY_MULT).unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "Halves the burn level penalty of the \"Active Sensor Burst\" ability";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.FLEET;
//		}
//	}
}



