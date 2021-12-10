package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class CyberneticAugmentation {
	
	public static final float MAX_ELITE_SKILLS_BONUS = 2;
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			int base = (int)Global.getSettings().getInt("officerMaxEliteSkills");
			return "*The base maximum number of elite skills per officer is " + base + "."; 
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getDarkHighlightColor();
			return new Color[] {h};
		}
		public String[] getHighlights() {
			int base = (int)Global.getSettings().getInt("officerMaxEliteSkills");
			return new String [] {"" + base};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	public static class Level1 implements CharacterStatsSkillEffect {
		
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).modifyFlat(id, MAX_ELITE_SKILLS_BONUS);
		}
		
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) MAX_ELITE_SKILLS_BONUS + " to maximum number of elite skills* for officers under your command";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}
}
