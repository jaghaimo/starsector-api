package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.FleetStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class Navigation {
	
	public static final float TERRAIN_PENALTY_REDUCTION = 30f;
	public static final float FUEL_USE_REDUCTION = 25;
	//public static final float SUSTAINED_BURN_BONUS = 5;
	public static final float FLEET_BURN_BONUS = 1;
	public static final float SB_BURN_BONUS = 1;
	
	
	

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



