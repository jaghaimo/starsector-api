package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class Navigation {
	
	public static float TERRAIN_PENALTY_REDUCTION = 30f;
	public static float FUEL_USE_REDUCTION = 25;
	//public static final float SUSTAINED_BURN_BONUS = 5;
	public static float FLEET_BURN_BONUS = 1;
	public static float SB_BURN_BONUS = 1;
	
	public static float FUEL_USE_REDUCTION_MAX_PERCENT = 50;
	public static float FUEL_USE_REDUCTION_MAX_FUEL = 25;
	
	public static String FUEL_EFFECT_ID = "nav_fuel_use_mod";
	
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
			
			String key = "nav1";
			Float bonus = (Float) data.getCacheClearedOnSync().get(key);
			if (bonus != null) return bonus;
			
			float fuelUse = getFuelUseBase(id, data);

			float useMult = 0f;
			
			if (fuelUse > 0) {
				float maxReduced = Math.min(fuelUse * (FUEL_USE_REDUCTION_MAX_PERCENT * 0.01f), 
											FUEL_USE_REDUCTION_MAX_FUEL);
				useMult = 1f - maxReduced / fuelUse;
				useMult = Math.round(useMult * 100f) / 100f;
			}
			
			data.getCacheClearedOnSync().put(key, useMult);
			return useMult;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
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
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	

	public static class Level1 implements CharacterStatsSkillEffect {
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).modifyFlat(id,
									-0.01f * TERRAIN_PENALTY_REDUCTION);
		}

		public void unapply(MutableCharacterStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int) (TERRAIN_PENALTY_REDUCTION) + "% terrain movement penalty from all applicable terrain";
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
			stats.getDynamic().getMod(Stats.CAN_SEE_NASCENT_POINTS).modifyFlat(id, 1);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.CAN_SEE_NASCENT_POINTS).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "Can detect nascent gravity wells in hyperspace around star systems";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
//	public static class Level1B implements FleetStatsSkillEffect {
//		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			stats.getDynamic().getMod(Stats.CAN_SEE_NASCENT_POINTS).modifyFlat(id, 1);
//		}
//		
//		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getDynamic().getMod(Stats.CAN_SEE_NASCENT_POINTS).unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "Can detect nascent gravity wells around star systems";
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
	
//	public static class Level2 implements FleetStatsSkillEffect {
//		public void apply(MutableFleetStatsAPI stats, String id, float level) {
//			stats.getFuelUseHyperMult().modifyMult(id, 1f - FUEL_USE_REDUCTION / 100f);
//			stats.getFuelUseNormalMult().modifyMult(id, 1f - FUEL_USE_REDUCTION / 100f);
//		}
//		
//		public void unapply(MutableFleetStatsAPI stats, String id) {
//			stats.getFuelUseHyperMult().unmodify(id);
//			stats.getFuelUseNormalMult().unmodify(id);
//		}
//		
//		public String getEffectDescription(float level) {
//			return "-" + (int) FUEL_USE_REDUCTION + "% fuel consumption";
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
	
	public static class Level2 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getFuelUseMod().modifyMult(id, 1f - FUEL_USE_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFuelUseMod().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "-" + (int) FUEL_USE_REDUCTION + "% fuel consumption";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	
	public static class Level3A implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getFleetwideMaxBurnMod().modifyFlat(id, FLEET_BURN_BONUS, "Navigation");
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getFleetwideMaxBurnMod().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) FLEET_BURN_BONUS + " maximum burn level";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level3B implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getMod(Stats.SUSTAINED_BURN_BONUS).modifyFlat(id, SB_BURN_BONUS);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.SUSTAINED_BURN_BONUS).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "Increases the burn bonus of the \"Sustained Burn\" ability by " + (int) SB_BURN_BONUS;
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
}



