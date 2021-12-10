package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class Hypercognition {
	
	public static float ACCESS = 0.1f;
	public static float FLEET_SIZE = 20f;
	public static int DEFEND_BONUS = 50;
	public static float STABILITY_BONUS = 1;
	
	
	public static class Level1 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getAccessibilityMod().modifyFlat(id, ACCESS, "Hypercognition");
		}

		public void unapply(MarketAPI market, String id) {
			market.getAccessibilityMod().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)Math.round(ACCESS * 100f) + "% accessibility";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Level2 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, FLEET_SIZE / 100f, "Hypercognition");
		}
		
		public void unapply(MarketAPI market, String id) {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			//return "" + (int)Math.round(FLEET_SIZE) + "% larger fleets";
			return "+" + (int)Math.round(FLEET_SIZE) + "% fleet size";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Level3 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + DEFEND_BONUS * 0.01f, "Hypercognition");
		}

		public void unapply(MarketAPI market, String id) {
			//market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyPercent(id);
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DEFEND_BONUS) + "% effectiveness of ground defenses";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Level4 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStability().modifyFlat(id, STABILITY_BONUS, "Hypercognition");
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
}


