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

public class CoordinatedManeuvers {
	
	public static final float LEVEL_1_BONUS = 0f;
	public static final float LEVEL_2_BONUS = 5f;
	public static final float LEVEL_3_BONUS = 5f;

	
	public static float getBase(HullSize hullSize) {
		float value = 0f;
		switch (hullSize) {
		case CAPITAL_SHIP: value = 4f; break;
		case CRUISER: value = 3f; break;
		case DESTROYER: value = 2f; break;
		case FRIGATE: value = 1f; break;
		}
		return value;
	}

	public static class Level0 implements DescriptionSkillEffect {
		public String getString() {
			String max = (int)CoordinatedManeuversScript.BASE_MAXIMUM + "%";
			String buoy = "+" + (int)CoordinatedManeuversScript.PER_BUOY + "%";
//			return "Does not apply to fighters. Bonus from each ship only applies to other ships.\n" +
//				   "Nav buoys grant " + buoy + " each, up to a maximum of " + max + " without skill.";
			return "Nav buoys grant " + buoy + " top speed each, up to a maximum of " + max + " without skills. " +
					"Does not apply to fighters. Bonus from each ship does not apply to itself.";
		}
		public Color[] getHighlightColors() {
			Color h = Misc.getHighlightColor();
			return new Color[] {h, h};
			//return null;
		}
		public String[] getHighlights() {
			String max = (int)CoordinatedManeuversScript.BASE_MAXIMUM + "%";
			String buoy = "+" + (int)CoordinatedManeuversScript.PER_BUOY + "%";
			return new String [] {buoy, max};
			//return null;
		}
		public Color getTextColor() {
			return null;
		}
	}
	
	public static class Level1A implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_FLAT).modifyFlat(id, getBase(hullSize));
		}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_FLAT).unmodify(id);
		}
		public String getEffectDescription(float level) {
			//return "+1-4" + "% to top speed of allied ships, depending on ship size";
			return "Every deployed ship grants +1-4% (depending on ship size) to top speed of allied ships";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	
	public static class Level1B  implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_MAX).modifyFlat(id, LEVEL_1_BONUS);
		}
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_MAX).unmodify(id);
		}
		public String getEffectDescription(float level) {
			float max = CoordinatedManeuversScript.BASE_MAXIMUM;
			max += LEVEL_1_BONUS;
			return "" + (int) max + "% upper limit for top speed bonus";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}

	
	public static class Level2B  implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_MAX).modifyFlat(id, LEVEL_2_BONUS);
		}
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_MAX).unmodify(id);
		}
		public String getEffectDescription(float level) {
			float max = CoordinatedManeuversScript.BASE_MAXIMUM;
			max += LEVEL_1_BONUS;
			max += LEVEL_2_BONUS;
			return "" + (int) max + "% upper limit for top speed bonus";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}
	
	
	public static class Level3B  implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_MAX).modifyFlat(id, LEVEL_2_BONUS);
		}
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_MAX).unmodify(id);
		}
		public String getEffectDescription(float level) {
			float max = CoordinatedManeuversScript.BASE_MAXIMUM;
			max += LEVEL_1_BONUS;
			max += LEVEL_2_BONUS;
			max += LEVEL_3_BONUS;
			return "" + (int) max + "% upper limit for top speed bonus";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}

}
