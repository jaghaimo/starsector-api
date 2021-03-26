package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.FleetTotalSource;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class FieldRepairs {
	
//	public static final float MIN_HULL = 20f;
//	public static final float MAX_HULL = 40f;
//	
//	public static final float MIN_CR = 20f;
//	public static final float MAX_CR = 40f;
	
	public static final float REPAIR_RATE_BONUS = 100f;
	public static final float INSTA_REPAIR_PERCENT = 50f;
	public static final float DMOD_REDUCTION = 2f;

//	public static class Level1 implements FleetStatsSkillEffect {
//		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			stats.getDynamic().getMod(Stats.RECOVERED_HULL_MIN).modifyFlat(id, MIN_HULL * 0.01f);
//			stats.getDynamic().getMod(Stats.RECOVERED_HULL_MAX).modifyFlat(id, MAX_HULL * 0.01f);
//		}
//		
//		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getDynamic().getMod(Stats.RECOVERED_HULL_MIN).unmodify(id);
//			stats.getDynamic().getMod(Stats.RECOVERED_HULL_MAX).unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "Recovered ships start with " + (int) MIN_HULL + "-" + (int)MAX_HULL + "% hull integrity";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.FLEET;
//		}
//	}
//	
//	public static class Level1B implements FleetStatsSkillEffect {
//		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			stats.getDynamic().getMod(Stats.RECOVERED_CR_MIN).modifyFlat(id, MIN_CR * 0.01f);
//			stats.getDynamic().getMod(Stats.RECOVERED_CR_MAX).modifyFlat(id, MAX_CR * 0.01f);
//		}
//		
//		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getDynamic().getMod(Stats.RECOVERED_CR_MIN).unmodify(id);
//			stats.getDynamic().getMod(Stats.RECOVERED_CR_MAX).unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "Recovered ships start with " + (int) MIN_CR + "-" + (int)MAX_CR + "% combat readiness";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.FLEET;
//		}
//	}
	
//	public static class Level1 implements CharacterStatsSkillEffect {
//		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
//			stats.getRepairRateMult().modifyPercent(id, REPAIR_RATE_BONUS);
//		}
//
//		public void unapply(MutableCharacterStatsAPI stats, String id) {
//			stats.getRepairRateMult().unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "" + (int) REPAIR_RATE_BONUS + "% faster ship repairs";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.ALL_SHIPS;
//		}
//	}
	
	public static class Level1 extends BaseSkillEffectDescription implements CharacterStatsSkillEffect, FleetTotalSource {
		
		public FleetTotalItem getFleetTotalItem() {
			return getOPTotal();
		}
		
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			//if (!isCivilian(stats)) {
				FleetDataAPI data = null;
				if (stats != null && stats.getFleet() != null) {
					data = stats.getFleet().getFleetData();
				}
				float repBonus = computeAndCacheThresholdBonus(data, stats, "fr_repRate", REPAIR_RATE_BONUS, ThresholdBonusType.OP_ALL_LOW);
				stats.getRepairRateMult().modifyPercent(id, repBonus);
			//}
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getRepairRateMult().unmodify(id);
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			FleetDataAPI data = null;
			if (stats != null && stats.getFleet() != null) {
				data = stats.getFleet().getFleetData();
			}
			float damBonus = computeAndCacheThresholdBonus(data, stats, "fr_repRate", REPAIR_RATE_BONUS, ThresholdBonusType.OP_ALL_LOW);
			
			info.addPara("+%s ship repair rate outside of combat (maximum: %s)", 0f, hc, hc,
					"" + (int) damBonus + "%",
					"" + (int) REPAIR_RATE_BONUS + "%");
			//addOPThresholdInfo(info, data, stats, OP_LOW_THRESHOLD);
			//info.addSpacer(5f);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level2 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {
		
		public FleetTotalItem getFleetTotalItem() {
			return getOPTotal();
		}
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			//if (!isCivilian(stats)) {
				float instaRep = computeAndCacheThresholdBonus(stats, "fr_instaRep", INSTA_REPAIR_PERCENT, ThresholdBonusType.OP_ALL_LOW);
				stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).modifyFlat(id, instaRep * 0.01f);
			//}
		}
			
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
			
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			FleetDataAPI data = getFleetData(null);
			float instaRep = computeAndCacheThresholdBonus(data, stats, "fr_instaRep", INSTA_REPAIR_PERCENT, ThresholdBonusType.OP_ALL_LOW);
			
			info.addPara("%s of hull and armor damage taken repaired after combat ends, at no cost (maximum: %s)", 0f, hc, hc,
					"" + (int) instaRep + "%",
					"" + (int) INSTA_REPAIR_PERCENT + "%");
			addOPThresholdAll(info, data, stats, OP_ALL_LOW_THRESHOLD);
			
			info.addSpacer(5f);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	
	
//	public static class Level2 implements ShipSkillEffect {
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).modifyFlat(id, INSTA_REPAIR);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).unmodify(id);
//		}	
//		
//		public String getEffectDescription(float level) {
//			return "" + (int) Math.round(INSTA_REPAIR * 100f) + "% of hull and armor damage taken repaired after combat ends, at no cost";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.ALL_SHIPS;
//		}
//	}
	
	
	public static class Level3 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.SHIP_DMOD_REDUCTION).modifyFlat(id, DMOD_REDUCTION);	
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SHIP_DMOD_REDUCTION).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			//return "Recovered non-friendly ships have an average of " + (int) DMOD_REDUCTION + " less subsystem with lasting damage";
			//return "Recovered ships have up to " + (int) DMOD_REDUCTION + " less d-mods";
			return "Recovered ships have fewer d-mods on average";
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
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
		}
		
		public String getEffectDescription(float level) {
			if (FieldRepairsScript.MONTHS_PER_DMOD_REMOVAL == 1) {
				return "Chance to remove one d-mod per month from a randomly selected ship in your fleet";
			} else if (FieldRepairsScript.MONTHS_PER_DMOD_REMOVAL == 2) {
				return "Chance to remove a d-mod from a randomly selected ship in your fleet every two months";
			} else {
				return "Chance to remove a d-mod from a randomly selected ship in your fleet every " +
							FieldRepairsScript.MONTHS_PER_DMOD_REMOVAL + " months";
			}
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	
//	public static class Level3B implements ShipSkillEffect {
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getDynamic().getMod(Stats.DMOD_REDUCE_MAINTENANCE).modifyFlat(id, 1f);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getDynamic().getMod(Stats.DMOD_REDUCE_MAINTENANCE).unmodify(id);
//		}	
//		
//		public String getEffectDescription(float level) {
//			return "(D) hull deployment cost reduction also applies to maintenance cost";
//		}
//		
//		public String getEffectPerLevelDescription() {
//			return null;
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.ALL_SHIPS;
//		}
//	}
}
