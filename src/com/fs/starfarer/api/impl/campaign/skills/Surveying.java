package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class Surveying {
	
	public static final float LEVEL_1_HAZARD = 50f;
	public static final float LEVEL_2_HAZARD = 50f;
	public static final float LEVEL_3_HAZARD = 1000f;
	
	public static final float LEVEL_1_COST = -0.25f;
	public static final float LEVEL_2_COST = -0.25f;
	public static final float LEVEL_3_COST = -0.25f;
	

	public static class Level1B implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			String desc = "Surveying skill";
			if (level <= 1) {
				stats.getDynamic().getStat(Stats.SURVEY_COST_MULT).modifyFlat(id, LEVEL_1_COST, desc);
			} else if (level <= 2) {
				stats.getDynamic().getStat(Stats.SURVEY_COST_MULT).modifyFlat(id, 
						LEVEL_1_COST + LEVEL_2_COST, desc);
			} else {
				stats.getDynamic().getStat(Stats.SURVEY_COST_MULT).modifyFlat(id, 
						LEVEL_1_COST + LEVEL_2_COST + LEVEL_3_COST, desc);
			}
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SURVEY_COST_MULT).unmodify(id);
		}

		public String getEffectDescription(float level) {
			float max = 0f;
			max += LEVEL_1_COST;
			return "-" + (int) Math.round(max * 100f) + "% resources required and used to survey planets";
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
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
		}

		public String getEffectDescription(float level) {
			float max = 0f;
			max += LEVEL_1_COST;
			max += LEVEL_2_COST;
			return "-" + (int) Math.round(max * 100f) + "% resources required and used to survey planets";
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
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
		}
		
		public String getEffectDescription(float level) {
			float max = 0f;
			max += LEVEL_1_COST;
			max += LEVEL_2_COST;
			max += LEVEL_3_COST;
			return "-" + (int) Math.round(max * 100f) + "% resources required and used to survey planets";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	
	public static class Level1 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.SURVEY_MAX_HAZARD).modifyFlat(id, LEVEL_1_HAZARD * 0.01f);
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SURVEY_MAX_HAZARD).unmodify(id);
		}

		public String getEffectDescription(float level) {
			float max = 0f;
			max += LEVEL_1_HAZARD;
			return "Able to survey planets with a hazard rating of up to " + (int) max + "%";
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
			stats.getDynamic().getMod(Stats.SURVEY_MAX_HAZARD).modifyFlat(id, LEVEL_2_HAZARD * 0.01f);
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SURVEY_MAX_HAZARD).unmodify(id);
		}

		public String getEffectDescription(float level) {
			float max = 0f;
			max += LEVEL_1_HAZARD;
			max += LEVEL_2_HAZARD;
			return "Able to survey planets with a hazard rating of up to " + (int) max + "%";
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
			stats.getDynamic().getMod(Stats.SURVEY_MAX_HAZARD).modifyFlat(id, LEVEL_3_HAZARD * 0.01f);
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SURVEY_MAX_HAZARD).unmodify(id);
		}

		public String getEffectDescription(float level) {
			return "Able to survey planets with any hazard rating";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
}



