package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class PlanetaryOperations {
	
	public static int ATTACK_BONUS = 100;
	public static int DEFEND_BONUS = 100;
	public static float CASUALTIES_MULT = 0.75f;
	public static float STABILITY_BONUS = 2;
	
	public static class Level1 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + DEFEND_BONUS * 0.01f, "Ground operations");
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
	
	public static class Level2 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStability().modifyFlat(id, STABILITY_BONUS, "Ground operations");
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

	public static class Level3 extends BaseSkillEffectDescription implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			//stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyMult(id, 1f + ATTACK_BONUS * 0.01f, "Planetary operations");
			stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyPercent(id, ATTACK_BONUS, "Ground operations");
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			//stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).unmodifyMult(id);
			stats.getDynamic().getMod(Stats.PLANETARY_OPERATIONS_MOD).unmodifyPercent(id);
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
						TooltipMakerAPI info, float width) {
			init(stats, skill);

			float opad = 10f;
			Color c = Misc.getBasePlayerColor();
			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
			info.addSpacer(opad);
			info.addPara("+%s effectiveness of ground operations such as raids", 0f, hc, hc,
					"" + (int) ATTACK_BONUS + "%");
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(ATTACK_BONUS) + "% effectiveness of ground operations such as raids"; 
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level4 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.PLANETARY_OPERATIONS_CASUALTIES_MULT).modifyMult(id, CASUALTIES_MULT, "Ground operations");
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.PLANETARY_OPERATIONS_CASUALTIES_MULT).unmodifyMult(id);
		}
		
//		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
//			init(stats, skill);
//			
//			float opad = 10f;
//			Color c = Misc.getBasePlayerColor();
//			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "fleet");
//			info.addSpacer(opad);
//			info.addPara("+%s effectiveness of ground operations", 0f, hc, hc,
//					"" + (int) ATTACK_BONUS + "%");
//		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)Math.round((1f - CASUALTIES_MULT) * 100f) + "% marine casualties suffered during ground operations such as raids"; 
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
}


