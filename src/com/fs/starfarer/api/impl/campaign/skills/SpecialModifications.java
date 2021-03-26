package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.DescriptionSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class SpecialModifications {

	public static int VENTS_BONUS = 10;
	public static int CAPACITORS_BONUS = 10;
	public static int EXTRA_MODS = 1;
	public static float BUILD_IN_XP_BONUS = 0.20f;
	

	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			int max = Misc.MAX_PERMA_MODS;
			return "*The base maximum number of permanent hullmods you're able to build into a ship is " +
					max + "."
					;
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			h = Misc.getDarkHighlightColor();
			return new Color[] {h, h, h};
		}
		public String[] getHighlights() {
			int max = Misc.MAX_PERMA_MODS;
			return new String [] {"" + max};
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	
	public static class Level1 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getMaxCapacitorsBonus().modifyFlat(id, CAPACITORS_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getMaxCapacitorsBonus().unmodify(id);
		}

		public String getEffectDescription(float level) {
			return "+" + (int) CAPACITORS_BONUS + " maximum flux capacitors";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}

	public static class Level2 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getMaxVentsBonus().modifyFlat(id, VENTS_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getMaxVentsBonus().unmodify(id);
		}

		public String getEffectDescription(float level) {
			return "+" + (int) VENTS_BONUS + " maximum flux vents";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level3 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.BUILD_IN_BONUS_XP_MOD).modifyFlat(id, BUILD_IN_XP_BONUS);
		}
		
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.BUILD_IN_BONUS_XP_MOD).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) Math.round(BUILD_IN_XP_BONUS * 100f) + "% bonus experience from building permanent hullmods* into ships";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level4 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(id, EXTRA_MODS);
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "Able to build " + EXTRA_MODS + " more permanent hullmod* into ships";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}

}





