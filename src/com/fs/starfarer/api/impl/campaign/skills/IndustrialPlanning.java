package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class IndustrialPlanning {
	
	public static int SUPPLY_BONUS = 1;
	public static float CUSTOM_PRODUCTION_BONUS = 50f;
	
	public static class Level1 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.SUPPLY_BONUS_MOD).modifyFlat(id, SUPPLY_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SUPPLY_BONUS_MOD).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "All industries supply " + SUPPLY_BONUS + " more unit of all the commodities they produce";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Level2 extends BaseSkillEffectDescription implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			//stats.getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).modifyPercent(id, CUSTOM_PRODUCTION_BONUS, "Industrial planning");
			stats.getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).modifyMult(id, 1f + CUSTOM_PRODUCTION_BONUS/100f, "Industrial planning");
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			//stats.getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).unmodifyPercent(id);
			stats.getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).unmodifyMult(id);
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			float opad = 10f;
			Color c = Misc.getBasePlayerColor();
			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "all colonies");
			info.addSpacer(opad);
			info.addPara("+%s maximum value of custom ship and weapon production per month", 0f, hc, hc,
					"" + (int) CUSTOM_PRODUCTION_BONUS + "%");
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_OUTPOSTS;
		}
	}
	
//	public static class Level1A implements CharacterStatsSkillEffect {
//
//		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
//			stats.getDynamic().getMod(Stats.DEMAND_REDUCTION_MOD).modifyFlat(id, DEMAND_REDUCTION);
//		}
//
//		public void unapply(MutableCharacterStatsAPI stats, String id) {
//			stats.getDynamic().getMod(Stats.DEMAND_REDUCTION_MOD).unmodifyFlat(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "All industries require " + DEMAND_REDUCTION + " less unit of all the commodities they need";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.GOVERNED_OUTPOST;
//		}
//	}
//	
//	public static class Level1B implements MarketSkillEffect {
//		public void apply(MarketAPI market, String id, float level) {
//			market.getUpkeepMult().modifyMult(id, UPKEEP_MULT, "Industrial planning");
//		}
//
//		public void unapply(MarketAPI market, String id) {
//			market.getUpkeepMult().unmodifyMult(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "-" + (int)Math.round(Math.abs((1f - UPKEEP_MULT)) * 100f) + "% upkeep for colonies";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.GOVERNED_OUTPOST;
//		}
//	}
//	
//	public static class Level3A implements MarketSkillEffect {
//		public void apply(MarketAPI market, String id, float level) {
//			market.getIncomeMult().modifyMult(id, INCOME_MULT, "Industrial planning");
//		}
//
//		public void unapply(MarketAPI market, String id) {
//			market.getIncomeMult().unmodifyMult(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "+" + (int)Math.round((INCOME_MULT - 1f) * 100f) + "% income from colonies, including exports";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.GOVERNED_OUTPOST;
//		}
//	}
	
//	public static void main(String[] args) {
//		System.out.println((int)((1.331 - 1.) * 1000.));
//	}
	
}


