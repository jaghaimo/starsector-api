package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class CommandAndControl {
	
	public static final float BASE_SECONDS_PER_POINT = Global.getSettings().getFloat("baseSecondsPerCommandPoint");
	public static final float RATE_BONUS = 50f;
	public static final float CP_BONUS = 3f;
	
	public static final float CM_BONUS = 5f;
	public static final float EW_BONUS = 5f;
	

	public static class Level1A implements CharacterStatsSkillEffect {

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
	
	public static class Level1B implements CharacterStatsSkillEffect {
		
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.COMMAND_POINT_RATE_COMMANDER).modifyFlat(id, RATE_BONUS / 100f);
		}
		
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.COMMAND_POINT_RATE_COMMANDER).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "" + (int) RATE_BONUS + "% faster command point recovery";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	
	
	
	
	
	
	public static class Level3A  implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_MAX).modifyFlat(id, CM_BONUS);
		}
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.COORDINATED_MANEUVERS_MAX).unmodify(id);
		}
		public String getEffectDescription(float level) {
			return "" + (int) CM_BONUS + "% maximum bonus from Coordinated Maneuvers";
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
			stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_MAX).modifyFlat(id, EW_BONUS);
		}
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_MAX).unmodify(id);
		}
		public String getEffectDescription(float level) {
			return "" + (int) CM_BONUS + "% maximum bonus from Electronic Warfare";
		}
		public String getEffectPerLevelDescription() {
			return null;
		}
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.NONE;
		}
	}
	
}
