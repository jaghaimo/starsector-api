package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class SafetyProcedures {
	
	public static final float CORONA_EFFECT_MULT = 0.5f;
	public static final float CREW_LOSS_REDUCTION = 30f;
	public static final float CR_MALFUNCTION_RANGE_MULT = 0.5f;
	
	public static final float DMOD_EFFECT_MULT = 0.5f;
	
	

	public static class Level1A implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getCrewLossMult().modifyMult(id, 1f - CREW_LOSS_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getCrewLossMult().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "-" + (int)(CREW_LOSS_REDUCTION) + "% crew lost due to hull damage in combat";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level1B implements FleetStatsSkillEffect {
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
	
	public static class Level2 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			//stats.getDynamic().getStat(Stats.HULL_DAMAGE_CR_LOSS).modifyMult(id, HULL_DAMAGE_CR_MULT);
			stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_MULT);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			//stats.getDynamic().getStat(Stats.HULL_DAMAGE_CR_LOSS).unmodify(id);
			stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "-" + (int) Math.round((1f - CORONA_EFFECT_MULT) * 100f) + "% combat readiness loss from being in star corona or similar terrain";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	public static class Level2B implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).modifyMult(id, DMOD_EFFECT_MULT);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "Negative effects of \"lasting damage\" hullmods (d-mods) reduced by 50%";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}

	
	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getStat(Stats.CR_MALFUNCION_RANGE).modifyMult(id, CR_MALFUNCTION_RANGE_MULT);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getStat(Stats.CR_MALFUNCION_RANGE).unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "" + (int) (100f * (1f - CR_MALFUNCTION_RANGE_MULT)) + "% reduced combat readiness range in which malfunctions and other negative effects occur";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	
	public static class Level3B implements FleetStatsSkillEffect {
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
}
