package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
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

public class Salvaging {
	
	public static float CREW_LOSS_REDUCTION = 75f;
	public static float SALVAGE_BONUS = 50f;
	public static final float COMBAT_SALVAGE = 20f;
	
	public static float CARGO_CAPACITY_MAX_PERCENT = 50;
	public static float CARGO_CAPACITY_MAX_VALUE = 1000;
	public static float FUEL_SALVAGE_BONUS = 25f;
	

	public static class Level1 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			String desc = "Salvaging skill";
			stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_NOT_RARE).modifyFlat(id, SALVAGE_BONUS * 0.01f, desc);
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_NOT_RARE).unmodify(id);
		}

		public String getEffectDescription(float level) {
			//return "+" + (int) max + "% resources and rare items recovered from abandoned stations and other derelicts";
			return "+" + (int) SALVAGE_BONUS + "% resources - but not rare items, such as blueprints - recovered from abandoned stations and other derelicts";
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
	
	public static class Level3 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).modifyFlat(id, COMBAT_SALVAGE * 0.01f);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.BATTLE_SALVAGE_MULT_FLEET).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) COMBAT_SALVAGE + "% post-battle salvage";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
	
	public static class Level5 implements FleetStatsSkillEffect {
		public void apply(MutableFleetStatsAPI stats, String id, float level) {
			stats.getDynamic().getStat(Stats.NON_COMBAT_CREW_LOSS_MULT).modifyMult(id, 1f - CREW_LOSS_REDUCTION / 100f);
		}

		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getStat(Stats.NON_COMBAT_CREW_LOSS_MULT).unmodify(id);
		}	

		public String getEffectDescription(float level) {
			return "-" + (int)(CREW_LOSS_REDUCTION) + "% crew lost in non-combat operations";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}

	
	public static String CARGO_EFFECT_ID = "sal_cargo_cap_mod";
	public static class Level4 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			id = CARGO_EFFECT_ID;
			float capMult = getCargoCapacityMult(id, getFleetData(stats));
			stats.getCargoMod().modifyMult(id, capMult);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			id = CARGO_EFFECT_ID;
			stats.getCargoMod().unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
		
		protected float getCargoCapacityBase(String id, FleetDataAPI data) {
			if (data == null) return 0f;
			
			float cargoCap = 0;
			for (FleetMemberAPI curr : data.getMembersListCopy()) {
				StatBonus stat = curr.getStats().getCargoMod();
				StatMod mod = stat.getMultBonus(id);
				if (mod != null) {
					stat.unmodifyMult(mod.source);
				}
				cargoCap += curr.getCargoCapacity();
				if (mod != null) {
					stat.modifyMult(mod.source, mod.value, mod.desc);
				}
			}
			return cargoCap;
		}
		
		protected float getCargoCapacityMult(String id, FleetDataAPI data) {
			if (data == null) return 0f;
			
			String key = "salvaging1";
			Float bonus = (Float) data.getCacheClearedOnSync().get(key);
			if (bonus != null) return bonus;
			
			float cargoBase = getCargoCapacityBase(id, data);

			float capMult = 0f;
			
			if (cargoBase > 0) {
				float addCapacity = Math.min(cargoBase * (CARGO_CAPACITY_MAX_PERCENT * 0.01f), 
											CARGO_CAPACITY_MAX_VALUE);
				capMult = 1f + addCapacity / cargoBase;
				capMult = Math.round(capMult * 100f) / 100f;
			}
			
			data.getCacheClearedOnSync().put(key, capMult);
			return capMult;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			info.addSpacer(5f);
			info.addPara("Cargo capacity increased by %s or %s units, whichever is lower",
					0f, hc, hc,
					"" + (int) CARGO_CAPACITY_MAX_PERCENT + "%", 
					"" + (int) CARGO_CAPACITY_MAX_VALUE
					);
			
			if (isInCampaign()) {
				FleetDataAPI data = Global.getSector().getPlayerFleet().getFleetData();
				String id = CARGO_EFFECT_ID;
				float cargoCap = getCargoCapacityBase(id, data);
				float capMult = getCargoCapacityMult(id, data);
				
				float increase = cargoCap * (capMult - 1f);
				
				boolean has = stats.getSkillLevel(skill.getId()) > 0;
				String is = "is";
				if (!has) is = "would be";
				info.addPara(indent + "Your fleet has a base cargo capacity of %s, which " + is + " increased by %s, or approximately %s units",
						0f, tc, hc, 
						"" + Misc.getRoundedValueMaxOneAfterDecimal(cargoCap), 
						"" + (int)(Math.round((capMult - 1f) * 100f)) + "%",
						"" + Misc.getRoundedValueMaxOneAfterDecimal(increase) 
						);
				//info.addSpacer(5f);
			}
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.FLEET;
		}
	}
}



