package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class SpaceOperations {
	
	public static final float ACCESS = 0.3f;
	public static final float FLEET_SIZE = 25f;
	
	
	public static class Level1 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getAccessibilityMod().modifyFlat(id, ACCESS, "Space operations");
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
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, FLEET_SIZE / 100f, "Fleet logistics");
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
}


