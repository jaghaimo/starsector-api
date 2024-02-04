package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.FleetTotalSource;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ContainmentProcedures {
	
//	public static final float CR_MALFUNCTION_RANGE_MULT = 0.5f;
	//public static final float DMOD_EFFECT_MULT = 0.5f;
	
	public static float FUEL_PROD_BONUS = 1f;
	public static float CREW_LOSS_REDUCTION = 50f;
	public static float FUEL_SALVAGE_BONUS = 25f;
	public static float FUEL_USE_REDUCTION_MAX_PERCENT = 25;
	public static float FUEL_USE_REDUCTION_MAX_FUEL = 25;
	

	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {
		
		public FleetTotalItem getFleetTotalItem() {
			return getOPTotal();
		}
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			float lossPercent = computeAndCacheThresholdBonus(stats, "sp_crewloss", CREW_LOSS_REDUCTION, ThresholdBonusType.OP_ALL);
			stats.getCrewLossMult().modifyMult(id, 1f - (lossPercent * 0.01f));
		}
			
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getCrewLossMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
			
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			//info.addSpacer(5f);
			FleetDataAPI data = getFleetData(null);
			float damBonus = computeAndCacheThresholdBonus(data, stats, "sp_crewloss", CREW_LOSS_REDUCTION, ThresholdBonusType.OP_ALL);
			
			info.addPara("-%s crew lost due to hull damage in combat (maximum: %s)", 0f, hc, hc,
					"" + (int) damBonus + "%",
					"" + (int) CREW_LOSS_REDUCTION + "%");
			addOPThresholdAll(info, data, stats, OP_ALL_THRESHOLD);
			
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}	
	
	public static class Level2 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.EMERGENCY_BURN_CR_MULT).modifyMult(id, 0f);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.EMERGENCY_BURN_CR_MULT).unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "The \"Emergency Burn\" ability no longer reduces combat readiness";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level3 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.FUEL_SALVAGE_VALUE_MULT_FLEET).modifyFlat(id, FUEL_SALVAGE_BONUS * 0.01f);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.FUEL_SALVAGE_VALUE_MULT_FLEET).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			float max = 0f;
			max += FUEL_SALVAGE_BONUS;
			return "+" + (int) max + "% fuel salvaged";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static String FUEL_EFFECT_ID = "sp_fuel_use_mod";
	public static class Level4 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			id = FUEL_EFFECT_ID;
			float useMult = getFuelUseMult(id, getFleetData(stats));
			stats.getFuelUseMod().modifyMult(id, useMult);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			id = FUEL_EFFECT_ID;
			stats.getFuelUseMod().unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
		
		protected float getFuelUseBase(String id, FleetDataAPI data) {
			if (data == null) return 0f;
			
			float fuelUse = 0;
			for (FleetMemberAPI curr : data.getMembersListCopy()) {
				StatBonus stat = curr.getStats().getFuelUseMod();
				StatMod mod = stat.getMultBonus(id);
				if (mod != null) {
					stat.unmodifyMult(mod.source);
				}
				fuelUse += curr.getFuelUse();
				if (mod != null) {
					stat.modifyMult(mod.source, mod.value, mod.desc);
				}
			}
			return fuelUse;
		}
		
		protected float getFuelUseMult(String id, FleetDataAPI data) {
			if (data == null) return 0f;
			
			String key = "conproc1";
			Float bonus = (Float) data.getCacheClearedOnSync().get(key);
			if (bonus != null) return bonus;
			
			float fuelUse = getFuelUseBase(id, data);

			float useMult = 0f;
			
			if (fuelUse > 0) {
				float maxReduced = Math.min(fuelUse * (FUEL_USE_REDUCTION_MAX_PERCENT * 0.01f), 
											FUEL_USE_REDUCTION_MAX_FUEL);
				useMult = 1f - maxReduced / fuelUse;
				//useMult = Math.round(useMult * 100f) / 100f;
			}
			
			data.getCacheClearedOnSync().put(key, useMult);
			return useMult;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
//			FUEL_USE_REDUCTION_MAX_PERCENT = 25;
//			FUEL_USE_REDUCTION_MAX_FUEL = 20;
			
			info.addSpacer(5f);
			info.addPara("Reduces fuel consumption by %s or %s units, whichever is lower",
					0f, hc, hc,
					"" + (int) FUEL_USE_REDUCTION_MAX_PERCENT + "%", 
					"" + (int) FUEL_USE_REDUCTION_MAX_FUEL
					);
			
			if (isInCampaign()) {
				FleetDataAPI data = Global.getSector().getPlayerFleet().getFleetData();
				String id = FUEL_EFFECT_ID;
				float fuelUse = getFuelUseBase(id, data);
				float useMult = getFuelUseMult(id, data);
				
				float reduction = fuelUse * (1f - useMult);
				
				boolean has = stats.getSkillLevel(skill.getId()) > 0;
				String is = "is";
				if (!has) is = "would be";
				info.addPara(indent + "Your fleet has a base fuel consumption of %s, which " + is + " reduced by %s, or %s units",
						0f, tc, hc, 
						"" + Misc.getRoundedValueMaxOneAfterDecimal(fuelUse), 
						"" + (int)(Math.round((1f - useMult) * 100f)) + "%",
						"" + Misc.getRoundedValueMaxOneAfterDecimal(reduction) 
						);
				info.addSpacer(5f);
			}
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
	
	public static class Level5 extends BaseSkillEffectDescription implements CharacterStatsSkillEffect {

		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.FUEL_SUPPLY_BONUS_MOD).modifyFlat(id, FUEL_PROD_BONUS);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.FUEL_SUPPLY_BONUS_MOD).unmodifyFlat(id);
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
				TooltipMakerAPI info, float width) {
			init(stats, skill);

			float opad = 10f;
			Color c = Misc.getBasePlayerColor();
			info.addPara("Affects: %s", opad + 5f, Misc.getGrayColor(), c, "governed colony");
			info.addSpacer(opad);
			info.addPara("+%s fuel production", 0f, hc, hc,
			"" + (int) FUEL_PROD_BONUS);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
//	public static class Level1 implements ShipSkillEffect {
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getCrewLossMult().modifyMult(id, 1f - CREW_LOSS_REDUCTION / 100f);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getCrewLossMult().unmodify(id);
//		}	
//		
//		public String getEffectDescription(float level) {
//			//return "-" + (int)(CREW_LOSS_REDUCTION) + "% crew lost due to hull damage in combat";
//			return "-" + (int)(CREW_LOSS_REDUCTION) + "% crew lost in combat and non-combat operations";
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
//	
//	public static class Level2 implements FleetStatsSkillEffect {
//		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			stats.getDynamic().getStat(Stats.NON_COMBAT_CREW_LOSS_MULT).modifyMult(id, 1f - CREW_LOSS_REDUCTION / 100f);
//		}
//		
//		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getDynamic().getStat(Stats.NON_COMBAT_CREW_LOSS_MULT).unmodify(id);
//		}	
//		
//		public String getEffectDescription(float level) {
//			//return "-" + (int)(CREW_LOSS_REDUCTION) + "% crew lost in non-combat operations";
//			return null;
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
//	public static class Level3 implements ShipSkillEffect {
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			//stats.getDynamic().getStat(Stats.HULL_DAMAGE_CR_LOSS).modifyMult(id, HULL_DAMAGE_CR_MULT);
//			stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_MULT);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			//stats.getDynamic().getStat(Stats.HULL_DAMAGE_CR_LOSS).unmodify(id);
//			stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).unmodify(id);
//		}	
//		
//		public String getEffectDescription(float level) {
//			return "-" + (int) Math.round((1f - CORONA_EFFECT_MULT) * 100f) + "% combat readiness lost from being in star corona or similar terrain";
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
	
//	public static class Level2B implements ShipSkillEffect {
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).modifyMult(id, DMOD_EFFECT_MULT);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).unmodify(id);
//		}	
//		
//		public String getEffectDescription(float level) {
//			return "Negative effects of \"lasting damage\" hullmods (d-mods) reduced by 50%";
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
//
//	
//	public static class Level3 implements ShipSkillEffect {
//
//		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			stats.getDynamic().getStat(Stats.CR_MALFUNCION_RANGE).modifyMult(id, CR_MALFUNCTION_RANGE_MULT);
//		}
//		
//		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getDynamic().getStat(Stats.CR_MALFUNCION_RANGE).unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "" + (int) (100f * (1f - CR_MALFUNCTION_RANGE_MULT)) + "% reduced combat readiness range in which malfunctions and other negative effects occur";
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
