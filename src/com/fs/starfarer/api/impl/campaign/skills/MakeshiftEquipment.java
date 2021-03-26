package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class MakeshiftEquipment {
	
	public static float SUPPLY_USE_REDUCTION_MAX_PERCENT = 50;
	public static float SUPPLY_USE_REDUCTION_MAX_UNITS = 100;
	public static float SURVEY_COST_MULT = 0.5f;
	public static float MINING_VALUE_MULT = 1.5f;
	
	public static float UPKEEP_MULT = 0.8f;
	

	public static class Level1 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			String desc = "Surveying skill";
			stats.getDynamic().getStat(Stats.SURVEY_COST_MULT).modifyMult(id, SURVEY_COST_MULT, desc);
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.SURVEY_COST_MULT).unmodifyMult(id);
		}

		public String getEffectDescription(float level) {
			return "-" + (int) Math.round((1f - SURVEY_COST_MULT) * 100f) + "% resources required to survey planets";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level2 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			String desc = "Surveying skill";
			stats.getDynamic().getStat(Stats.PLANET_MINING_VALUE_MULT).modifyMult(id, SURVEY_COST_MULT, desc);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.PLANET_MINING_VALUE_MULT).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) Math.round((1f - SURVEY_COST_MULT) * 100f) + "% resources extracted from surface deposits on uncolonized planets";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static String SUPPLIES_EFFECT_ID = "surveying_supply_use_mod";
	public static class Level3 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			id = SUPPLIES_EFFECT_ID;
			float useMult = getSupplyUseMult(id, getFleetData(stats));
			stats.getSuppliesPerMonth().modifyMult(id, useMult);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			id = SUPPLIES_EFFECT_ID;
			stats.getSuppliesPerMonth().unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
		
		protected float getSupplyUseBase(String id, FleetDataAPI data) {
			if (data == null) return 0f;
			
			float supplyUse = 0;
			for (FleetMemberAPI curr : data.getMembersListCopy()) {
				MutableStat stat = curr.getStats().getSuppliesPerMonth();
				StatMod mod = stat.getMultStatMod(id);
				if (mod != null) {
					stat.unmodifyMult(mod.source);
				}
				supplyUse += stat.getModifiedValue();
				if (mod != null) {
					stat.modifyMult(mod.source, mod.value, mod.desc);
				}
			}
			return supplyUse;
		}
		
		protected float getSupplyUseMult(String id, FleetDataAPI data) {
			if (data == null) return 0f;
			
			String key = "nav1";
			Float bonus = (Float) data.getCacheClearedOnSync().get(key);
			if (bonus != null) return bonus;
			
			float supplyUse = getSupplyUseBase(id, data);

			float useMult = 0f;
			
			if (supplyUse > 0) {
				float maxReduced = Math.min(supplyUse * (SUPPLY_USE_REDUCTION_MAX_PERCENT * 0.01f), 
													   SUPPLY_USE_REDUCTION_MAX_UNITS);
				useMult = 1f - maxReduced / supplyUse;
				//useMult = Math.round(useMult * 100f) / 100f;
			}
			
			data.getCacheClearedOnSync().put(key, useMult);
			return useMult;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			//info.addSpacer(5f);
			info.addPara("Reduces monthly supply consumption for ship maintenance by %s or %s units, whichever is lower",
					0f, hc, hc,
					"" + (int) SUPPLY_USE_REDUCTION_MAX_PERCENT + "%", 
					"" + (int) SUPPLY_USE_REDUCTION_MAX_UNITS
					);
			
			if (isInCampaign()) {
				FleetDataAPI data = Global.getSector().getPlayerFleet().getFleetData();
				String id = SUPPLIES_EFFECT_ID;
				float supplyUse = getSupplyUseBase(id, data);
				float useMult = getSupplyUseMult(id, data);
				
				float reduction = supplyUse * (1f - useMult);
				
				boolean has = stats.getSkillLevel(skill.getId()) > 0;
				String is = "is";
				if (!has) is = "would be";
				info.addPara(indent + "Your fleet requires a base %s supplies per month for maintenance, which " + is + " reduced by %s, or %s units",
						0f, tc, hc, 
						"" + Misc.getRoundedValueMaxOneAfterDecimal(supplyUse), 
						"" + (int)(Math.round((1f - useMult) * 100f)) + "%",
						"" + Misc.getRoundedValueMaxOneAfterDecimal(reduction)
						//"" + Misc.getRoundedValueMaxOneAfterDecimal(Math.min(SUPPLY_USE_REDUCTION_MAX_UNITS, reduction))
						);
			}
			
			info.addSpacer(5f);
//			float opad = 10f;
//			Color c = Misc.getBasePlayerColor();
//			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "governed colony");
//			info.addSpacer(opad);
//			info.addPara("+%s fuel production", 0f, hc, hc,
//					"" + (int) 1f);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	
//	public static class Level2 extends BaseSkillEffectDescription implements MarketSkillEffect {
//
//		public void apply(MarketAPI market, String id, float level) {
//			market.getUpkeepMult().modifyMult(id, UPKEEP_MULT, "Surveying");
//		}
//
//		public void unapply(MarketAPI market, String id) {
//			market.getUpkeepMult().unmodifyMult(id);
//		}
//		
//		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
//				TooltipMakerAPI info, float width) {
//			init(stats, skill);
//
//			float opad = 10f;
//			Color c = Misc.getBasePlayerColor();
//			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "governed colony");
//			info.addSpacer(opad);
//			info.addPara("-%s colony upkeep", 0f, hc, hc,
//						 "" + (int)Math.round(Math.abs((1f - UPKEEP_MULT)) * 100f) + "%");
//		}
//		
//		public ScopeDescription getScopeDescription() {
//			return ScopeDescription.GOVERNED_OUTPOST;
//		}
//	}
}



