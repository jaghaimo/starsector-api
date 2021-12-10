package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.FleetTotalSource;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class FluxRegulation {

	public static int VENTS_BONUS = 5;
	public static int CAPACITORS_BONUS = 5;
	
	public static float DISSIPATION_PERCENT = 10;
	public static float CAPACITY_PERCENT = 10;
	
	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {
		
		public FleetTotalItem getFleetTotalItem() {
			return getCombatOPTotal();
		}
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			if (!isCivilian(stats)) {
				float disBonus = computeAndCacheThresholdBonus(stats, "fr_dis", DISSIPATION_PERCENT, ThresholdBonusType.OP);
				float capBonus = computeAndCacheThresholdBonus(stats, "fr_cap", CAPACITY_PERCENT, ThresholdBonusType.OP);
				stats.getFluxDissipation().modifyPercent(id, disBonus);
				stats.getFluxCapacity().modifyPercent(id, capBonus);
			}
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFluxDissipation().unmodifyPercent(id);
			stats.getFluxCapacity().unmodifyPercent(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
		
//			float op = getTotalOP(data, cStats);
//			bonus = getThresholdBasedRoundedBonus(DISSIPATION_PERCENT, op, OP_THRESHOLD);
//			
//			float op = getTotalOP(data, cStats);
//			bonus = getThresholdBasedRoundedBonus(CAPACITY_PERCENT, op, FIGHTER_BAYS_THRESHOLD);
			
			
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			//info.addSpacer(5f);
			
			FleetDataAPI data = getFleetData(null);
			float disBonus = computeAndCacheThresholdBonus(data, stats, "fr_dis", DISSIPATION_PERCENT, ThresholdBonusType.OP);
			float capBonus = computeAndCacheThresholdBonus(data, stats, "fr_cap", CAPACITY_PERCENT, ThresholdBonusType.OP);

			float opad = 10f;
			Color c = Misc.getBasePlayerColor();
			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "all combat ships, including carriers and militarized civilian ships");
			
			info.addPara("+%s flux dissipation for combat ships (maximum: %s)", opad, hc, hc,
					"" + (int) disBonus + "%",
					"" + (int) DISSIPATION_PERCENT + "%");
			
//			addFighterBayThresholdInfo(info, data);
//			info.addSpacer(5f);
			
			info.addPara("+%s flux capacity for combat ships (maximum: %s)", 0f, hc, hc,
					"" + (int) capBonus + "%",
					"" + (int) CAPACITY_PERCENT + "%");
			addOPThresholdInfo(info, data, stats);
			
			//info.addSpacer(5f);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level2 implements CharacterStatsSkillEffect {
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

	public static class Level3 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			//stats.getShipOrdnancePointBonus().modifyPercent(id, 50f);
			stats.getMaxVentsBonus().modifyFlat(id, VENTS_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			//stats.getShipOrdnancePointBonus().unmodifyPercent(id);
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

}





