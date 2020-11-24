package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class FleetLogistics {
	
	public static final float ACCESS_1 = 0.15f;
	public static final float ACCESS_2 = 0.15f;
	public static final float FLEET_SIZE = 25f;
	
	public static final float REPAIR_RATE_INCREASE = 25f;
	
	public static final float OFFICER_SHIP_RECOVERY_MOD = 1000f;
	public static final float MAINTENANCE_COST_REDUCTION = 25f;
	public static final float MAX_CR_BONUS = 15f;
	
	

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getRepairRatePercentPerDay().modifyPercent(id, REPAIR_RATE_INCREASE);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getRepairRatePercentPerDay().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "" + (int)(REPAIR_RATE_INCREASE) + "% faster repair rate";
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
			stats.getDynamic().getMod(Stats.OFFICER_SHIP_RECOVERY_MOD).modifyFlat(id, OFFICER_SHIP_RECOVERY_MOD * 0.01f);
		}
		
		public void unapply(MutableFleetStatsAPI stats, String id) {
			stats.getDynamic().getMod(Stats.OFFICER_SHIP_RECOVERY_MOD).unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "Flagship and ships with an officer in command nearly guaranteed to be recoverable if lost"; 
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
			stats.getSuppliesPerMonth().modifyMult(id, 1f - MAINTENANCE_COST_REDUCTION/100f, "Fleet logistics skill");
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getSuppliesPerMonth().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int) (MAINTENANCE_COST_REDUCTION) + "% supply use for maintenance";
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
			stats.getMaxCombatReadiness().modifyFlat(id, MAX_CR_BONUS * 0.01f, "Fleet logistics skill");
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxCombatReadiness().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MAX_CR_BONUS) + "% maximum combat readiness";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}
	}
	
	
	public static class Market1 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			if (level <= 1) {
				market.getAccessibilityMod().modifyFlat(id, ACCESS_1, "Fleet logistics");
			} else if (level >= 2) {
				market.getAccessibilityMod().modifyFlat(id, ACCESS_1 + ACCESS_2, "Fleet logistics");
			}
		}

		public void unapply(MarketAPI market, String id) {
			market.getAccessibilityMod().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)Math.round(ACCESS_1 * 100f) + "% accessibility";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	
	public static class Market2 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			//market.getAccessibilityMod().modifyFlat(id, ACCESS_2, "Fleet logistics");
		}

		public void unapply(MarketAPI market, String id) {
			//market.getAccessibilityMod().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)Math.round(ACCESS_2 * 100f) + "% accessibility";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
	public static class Market3 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, FLEET_SIZE / 100f, "Fleet logistics");
		}
		
		public void unapply(MarketAPI market, String id) {
			market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return "" + (int)Math.round(FLEET_SIZE) + "% larger fleets";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
}
