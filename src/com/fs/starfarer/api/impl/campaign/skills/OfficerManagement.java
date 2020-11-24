package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.util.Misc;

public class OfficerManagement {
	
	public static final float LEVEL_1_BONUS = 2f;
	public static final float LEVEL_2_BONUS = 2f;
	public static final float LEVEL_3_BONUS = 2f;
	
	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			int baseOfficers = (int)Global.getSector().getPlayerStats().getOfficerNumber().getBaseValue();
			
			String officers = "officers";
			if (baseOfficers == 1) officers = "officer";
			
			return "At a base level, able to command up to " + 
					baseOfficers + " " + officers + ".";
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			return new Color[] {h};
		}
		public String[] getHighlights() {
			String baseOfficers = "" + (int)Global.getSector().getPlayerStats().getOfficerNumber().getBaseValue();
			return new String [] {baseOfficers};
		}
		public Color getTextColor() {
			return null;
		}
	}
	public static class Level1  implements CharacterStatsSkillEffect {

		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getOfficerNumber().modifyFlat(id, LEVEL_1_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getOfficerNumber().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			int max = (int) Global.getSector().getCharacterData().getPerson().getStats().getOfficerNumber().getBaseValue();
			max += LEVEL_1_BONUS;
			return "Able to command up to " + (int) (max) + " officers";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}
	
	public static class Level2  implements CharacterStatsSkillEffect {
		
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getOfficerNumber().modifyFlat(id, LEVEL_2_BONUS);
		}
		
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getOfficerNumber().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			int max = (int) Global.getSector().getCharacterData().getPerson().getStats().getOfficerNumber().getBaseValue();
			max += LEVEL_1_BONUS;
			max += LEVEL_2_BONUS;
			return "Able to command up to " + (int) (max) + " officers";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}
	
	public static class Level3  implements CharacterStatsSkillEffect {
		
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getOfficerNumber().modifyFlat(id, LEVEL_3_BONUS);
		}
		
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getOfficerNumber().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			int max = (int) Global.getSector().getCharacterData().getPerson().getStats().getOfficerNumber().getBaseValue();
			max += LEVEL_1_BONUS;
			max += LEVEL_2_BONUS;
			max += LEVEL_3_BONUS;
			return "Able to command up to " + (int) (max) + " officers";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}
	
}
