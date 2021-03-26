package com.fs.starfarer.api.impl.campaign.skills;

import java.awt.Color;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.CustomSkillDescription;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class FighterDoctrine {
	
	public static final float FIGHTER_CREW_LOSS_REDUCTION = 15f;
	public static final float FIGHTER_RAMAGE_REDUCTION = 15f;
	public static final float FIGHTER_REPLACEMENT_RATE_BONUS = 15f;
	
	
	public static class Test implements ShipSkillEffect, CustomSkillDescription {

		protected float getReplacementRateBonus(MutableShipStatsAPI stats) {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return 0f;
			FleetDataAPI data = member.getFleetDataForStats();
			if (data == null) data = member.getFleetData();
			if (data == null) return 0f;
			
			return getReplacementRateBonus(data);
		}
		
		
		protected float getReplacementRateBonus(FleetDataAPI data) {
			String key = "fd1";
			Float bonus = (Float) data.getCacheClearedOnSync().get(key);
			if (bonus != null) return bonus;
			
			float bays = 0;
			for (FleetMemberAPI curr : data.getMembersListCopy()) {
				bays += curr.getNumFlightDecks();
			}
			
			bonus = (float) Math.round(300f / (Math.max(bays, 6)));
			data.getCacheClearedOnSync().put(key, bonus);
			return bonus;
		}

		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			float bonus = getReplacementRateBonus(stats);
			float timeMult = 1f / ((100f + bonus) / 100f);
			stats.getFighterRefitTimeMult().modifyMult(id, timeMult);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFighterRefitTimeMult().unmodify(id);
		}	

		
		public boolean hasCustomDescription() {
			return true;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			
			Color textColor = Misc.getTextColor();
			Color highlightColor = Misc.getHighlightColor();
			Color darkHighlightColor = Misc.setAlpha(highlightColor, 155);
			int alpha = 255;
			float level = stats.getSkillLevel(skill.getId());
			if (level <= 0) {
				textColor = Misc.getGrayColor();
				highlightColor = darkHighlightColor;
				alpha = 155;
			}
			
			if (Global.getCurrentState() == GameState.CAMPAIGN) {
				float bonus = getReplacementRateBonus(Global.getSector().getPlayerFleet().getFleetData());
				info.addPara("%s faster fighter replacements " +
							 "(based on number of fighter bays in fleet)", 0f, textColor, highlightColor,
							 "" + (int)(bonus) + "%");
			}
		}
		
		public String getEffectDescription(float level) {
			float bonus = getReplacementRateBonus(Global.getSector().getPlayerFleet().getFleetData());
			return "" + (int)(bonus) + "% faster fighter replacements (based on number of fighter bays in fleet)";
		}
		
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, 1f - FIGHTER_CREW_LOSS_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)(FIGHTER_CREW_LOSS_REDUCTION) + "% crew lost due to fighter losses in combat";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}

	public static class Level2 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getHullDamageTakenMult().modifyMult(id, 1f - FIGHTER_RAMAGE_REDUCTION / 100f);
			stats.getArmorDamageTakenMult().modifyMult(id, 1f - FIGHTER_RAMAGE_REDUCTION / 100f);
			stats.getShieldDamageTakenMult().modifyMult(id, 1f - FIGHTER_RAMAGE_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getHullDamageTakenMult().unmodify(id);
			stats.getArmorDamageTakenMult().unmodify(id);
			stats.getShieldDamageTakenMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)(FIGHTER_RAMAGE_REDUCTION) + "% damage taken";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_FIGHTERS;
		}
	}
	
	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			float timeMult = 1f / ((100f + FIGHTER_REPLACEMENT_RATE_BONUS) / 100f);
			stats.getFighterRefitTimeMult().modifyMult(id, timeMult);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFighterRefitTimeMult().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "" + (int)(FIGHTER_REPLACEMENT_RATE_BONUS) + "% faster fighter replacements";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
}
