package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class FieldRepairs {
	
	public static final float MIN_HULL = 20f;
	public static final float MAX_HULL = 40f;
	
	public static final float MIN_CR = 20f;
	public static final float MAX_CR = 40f;
	
	public static final float INSTA_REPAIR = 0.5f;
	
	public static final float REPAIR_RATE_BONUS = 100f;
	

	public static class Level1 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.RECOVERED_HULL_MIN).modifyFlat(id, MIN_HULL * 0.01f);
			stats.getDynamic().getMod(Stats.RECOVERED_HULL_MAX).modifyFlat(id, MAX_HULL * 0.01f);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.RECOVERED_HULL_MIN).unmodify(id);
			stats.getDynamic().getMod(Stats.RECOVERED_HULL_MAX).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "Recovered ships start with " + (int) MIN_HULL + "-" + (int)MAX_HULL + "% hull integrity";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level1B implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.RECOVERED_CR_MIN).modifyFlat(id, MIN_CR * 0.01f);
			stats.getDynamic().getMod(Stats.RECOVERED_CR_MAX).modifyFlat(id, MAX_CR * 0.01f);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.RECOVERED_CR_MIN).unmodify(id);
			stats.getDynamic().getMod(Stats.RECOVERED_CR_MAX).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "Recovered ships start with " + (int) MIN_CR + "-" + (int)MAX_CR + "% combat readiness";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	
	public static class Level2 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).modifyFlat(id, INSTA_REPAIR);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "" + (int) Math.round(INSTA_REPAIR * 100f) + "% of hull and armor damage taken repaired after combat ends, at no cost";
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
			stats.getRepairRateMult().modifyPercent(id, REPAIR_RATE_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getRepairRateMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "" + (int) REPAIR_RATE_BONUS + "% faster ship repairs";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level3B implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.DMOD_REDUCE_MAINTENANCE).modifyFlat(id, 1f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.DMOD_REDUCE_MAINTENANCE).unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "(D) hull deployment cost reduction also applies to maintenance cost";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
}
