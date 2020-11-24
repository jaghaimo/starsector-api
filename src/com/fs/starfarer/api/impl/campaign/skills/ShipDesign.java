package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;

public class ShipDesign {
	
	public static final float CAPACITORS_BONUS = 20f;
	public static final float VENTS_BONUS = 20f;
	public static final float OP_BONUS = 10f;
	
	

	public static class Level1 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getMaxCapacitorsBonus().modifyPercent(id, CAPACITORS_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getMaxCapacitorsBonus().unmodify(id);
		}

		public String getEffectDescription(float level) {
			return "+" + (int) CAPACITORS_BONUS + "% maximum flux capacitors";
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
			stats.getMaxVentsBonus().modifyPercent(id, VENTS_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getMaxVentsBonus().unmodify(id);
		}

		public String getEffectDescription(float level) {
			return "+" + (int) VENTS_BONUS + "% maximum flux vents";
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
			stats.getShipOrdnancePointBonus().modifyPercent(id, OP_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getShipOrdnancePointBonus().unmodify(id);
		}

		public String getEffectDescription(float level) {
			return "+" + (int) OP_BONUS + "% ordnance points";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
}
