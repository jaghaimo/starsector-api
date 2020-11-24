package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class Salvaging {
	
	public static final float LEVEL_1_SALVAGE_RATING = 15f;
	public static final float LEVEL_2_SALVAGE_RATING = 15f;
	public static final float LEVEL_3_SALVAGE_RATING = 20f;
	
//	public static final float LEVEL_1_COMBAT_SALVAGE = 5f;
//	public static final float LEVEL_2_COMBAT_SALVAGE = 5f;
	public static final float LEVEL_3_COMBAT_SALVAGE = 10f;
	
	

	public static class Level1A implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			String desc = "Salvaging skill";
			if (level <= 1) {
				stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).modifyFlat(id, LEVEL_1_SALVAGE_RATING * 0.01f, desc);
			} else if (level <= 2) {
				stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).modifyFlat(id, 
						(LEVEL_1_SALVAGE_RATING + LEVEL_2_SALVAGE_RATING) * 0.01f, desc);
			} else {
				stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).modifyFlat(id, 
						(LEVEL_1_SALVAGE_RATING + LEVEL_2_SALVAGE_RATING + LEVEL_3_SALVAGE_RATING) * 0.01f, desc);
			}
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).unmodify(id);
		}

		public String getEffectDescription(float level) {
			float max = 0f;
			max += LEVEL_1_SALVAGE_RATING;
			return "+" + (int) max + "% resources and rare items recovered from abandoned stations and other derelicts";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
//	public static class Level1B implements FleetStatsSkillEffect {
//		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).modifyFlat(id, LEVEL_1_COMBAT_SALVAGE * 0.01f);
//		}
//		
//		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			float max = 0f;
//			max += LEVEL_1_COMBAT_SALVAGE;
//			return "+" + (int) max + "% post-battle salvage";
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
	
	
	public static class Level2A implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
		}

		public String getEffectDescription(float level) {
			float max = 0f;
			max += LEVEL_1_SALVAGE_RATING;
			max += LEVEL_2_SALVAGE_RATING;
			return "+" + (int) max + "% resources and rare items recovered from abandoned stations and other derelicts";
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
//			stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).modifyFlat(id, LEVEL_2_COMBAT_SALVAGE * 0.01f);
//		}
//		
//		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			float max = 0f;
//			max += LEVEL_2_COMBAT_SALVAGE;
//			return "+" + (int) max + "% post-battle salvage";
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
	
	
	public static class Level3A implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
		}

		public String getEffectDescription(float level) {
			float max = 0f;
			max += LEVEL_1_SALVAGE_RATING;
			max += LEVEL_2_SALVAGE_RATING;
			max += LEVEL_3_SALVAGE_RATING;
			return "+" + (int) max + "% resources and rare items recovered from abandoned stations and other derelicts";
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
			stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).modifyFlat(id, LEVEL_3_COMBAT_SALVAGE * 0.01f);
			//stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).modifyFlat(id, LEVEL_3_COMBAT_SALVAGE * 0.01f);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).unmodify(id);
			//stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			float max = 0f;
			max += LEVEL_3_COMBAT_SALVAGE;
			return "+" + (int) max + "% post-battle salvage";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
}



