package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class PhaseMastery {
	
	public static final float FLUX_UPKEEP_REDUCTION = 25f;
	public static final float PHASE_COOLDOWN_REDUCTION = 50f;
	public static final float PHASE_SPEED_BONUS = 100f;
	

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1f - FLUX_UPKEEP_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getPhaseCloakUpkeepCostBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)(FLUX_UPKEEP_REDUCTION) + "% flux generated by active phase cloak";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}

	public static class Level2 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getPhaseCloakCooldownBonus().modifyMult(id, 1f - PHASE_COOLDOWN_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getPhaseCloakCooldownBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)(PHASE_COOLDOWN_REDUCTION) + "% phase cloak cooldown";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).modifyFlat(id, PHASE_SPEED_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).unmodifyFlat(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(PHASE_SPEED_BONUS) + "% top speed while phase cloak active";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	
}