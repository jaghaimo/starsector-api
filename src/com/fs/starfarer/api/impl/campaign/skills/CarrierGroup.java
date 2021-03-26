package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.FleetTotalSource;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class CarrierGroup {
	
	public static float REPLACEMENT_RATE_PERCENT = 50;
	
	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {
		
		public FleetTotalItem getFleetTotalItem() {
			return getFighterBaysTotal();
		}
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (hasFighterBays(stats)) {
				float rateBonus = computeAndCacheThresholdBonus(stats, "cg_rep_rate", REPLACEMENT_RATE_PERCENT, ThresholdBonusType.FIGHTER_BAYS);
				float timeMult = 1f / ((100f + rateBonus) / 100f);
				stats.getFighterRefitTimeMult().modifyMult(id, timeMult);
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFighterRefitTimeMult().unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
			
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			FleetDataAPI data = getFleetData(null);
			float rateBonus = computeAndCacheThresholdBonus(data, stats, "cg_rep_rate", REPLACEMENT_RATE_PERCENT, ThresholdBonusType.FIGHTER_BAYS);
			
			info.addPara("+%s faster fighter replacement rate (maximum: %s)", 0f, hc, hc,
					"" + (int) rateBonus + "%",
					"" + (int) REPLACEMENT_RATE_PERCENT + "%");
			addFighterBayThresholdInfo(info, data);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_CARRIERS;
		}

	}
	


}





