package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class PlanetaryOperations {
	
	public static int LEVEL_1_BONUS = 25;
	public static int LEVEL_2_BONUS = 25;
	
	public static float STABILITY_BONUS = 2;
	
	public static class Level1A implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			if (level <= 1) {
				//stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyPercent(id, LEVEL_1_BONUS, "Planetary operations");
				stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyMult(id, 1f + LEVEL_1_BONUS * 0.01f, "Planetary operations");
			} else if (level >= 2) {
				//stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyPercent(id, LEVEL_1_BONUS + LEVEL_2_BONUS, "Planetary operations");
				stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyMult(id, 1f + (LEVEL_1_BONUS + LEVEL_2_BONUS) * 0.01f, "Planetary operations");
			}
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			//stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).unmodifyPercent(id);
			stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).unmodifyMult(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(LEVEL_1_BONUS) + "% effectiveness of ground operations"; 
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level1B implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			if (level <= 1) {
				market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + LEVEL_1_BONUS * 0.01f, "Planetary operations");
			} else if (level >= 2) {
				market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + (LEVEL_1_BONUS + LEVEL_2_BONUS) * 0.01f, "Planetary operations");
			}
		}

		public void unapply(MarketAPI market, String id) {
			//market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyPercent(id);
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(LEVEL_1_BONUS) + "% effectiveness of ground defenses";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	
	public static class Level2A implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(LEVEL_2_BONUS + LEVEL_1_BONUS) + "% effectiveness of ground operations"; 
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level2B implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
		}

		public void unapply(MarketAPI market, String id) {
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(LEVEL_2_BONUS + LEVEL_1_BONUS) + "% effectiveness of ground defenses";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	
	
	public static class Level3A implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStability().modifyFlat(id, STABILITY_BONUS, "Planetary operations");
		}

		public void unapply(MarketAPI market, String id) {
			market.getStability().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)STABILITY_BONUS + " stability";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
//	public static void main(String[] args) {
////		System.out.println((int)((1.331 - 1.) * 1000.));
////		System.out.println(Math.round(0.4999999f));
//		//System.out.println(Math.round(11,111,113 + 11111112));
//	}
	
}


