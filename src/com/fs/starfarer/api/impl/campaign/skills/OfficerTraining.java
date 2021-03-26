package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class OfficerTraining {
	
	public static final float MAX_LEVEL_BONUS = 1;
	public static final float MAX_ELITE_SKILLS_BONUS = 1;
	public static final float CP_BONUS = 2f;
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			int base = (int)Global.getSettings().getInt("officerMaxLevel");
			return "The base maximum officer level is " + base + "."; 
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getDarkHighlightColor();
			return new Color[] {h};
		}
		public String[] getHighlights() {
			int base = (int)Global.getSettings().getInt("officerMaxLevel");
			return new String [] {"" + base};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	public static class Level1 implements CharacterStatsSkillEffect {

		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.OFFICER_MAX_LEVEL_MOD).modifyFlat(id, MAX_LEVEL_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.OFFICER_MAX_LEVEL_MOD).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) MAX_LEVEL_BONUS + " to maximum level of officers under your command";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}
	
	public static class Level2 implements CharacterStatsSkillEffect {
		
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).modifyFlat(id, MAX_ELITE_SKILLS_BONUS);
		}
		
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) MAX_ELITE_SKILLS_BONUS + " to maximum number of elite skills for officers under your command";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}
	
	public static class Level3 implements CharacterStatsSkillEffect {

		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getCommandPoints().modifyFlat(id, CP_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getCommandPoints().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) CP_BONUS + " command points";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
}
