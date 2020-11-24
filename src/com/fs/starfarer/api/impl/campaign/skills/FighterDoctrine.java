package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class FighterDoctrine {
	
	public static final float FIGHTER_CREW_LOSS_REDUCTION = 15f;
	public static final float FIGHTER_RAMAGE_REDUCTION = 15f;
	public static final float FIGHTER_REPLACEMENT_RATE_BONUS = 15f;
	
	

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
