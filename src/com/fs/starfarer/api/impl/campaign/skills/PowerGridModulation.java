package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class PowerGridModulation {
	
	public static final float FLUX_CAPACITY_BONUS = 10f;
	public static final float VENT_RATE_BONUS = 25f;
	public static final float FLUX_DISSIPATION_BONUS = 10f;
	
	

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getFluxCapacity().modifyPercent(id, FLUX_CAPACITY_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFluxCapacity().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(FLUX_CAPACITY_BONUS) + "% flux capacity";
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
			stats.getVentRateMult().modifyFlat(id, VENT_RATE_BONUS * 0.01f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getVentRateMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int) VENT_RATE_BONUS + "% flux dissipation rate while venting";
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
			stats.getFluxDissipation().modifyPercent(id, FLUX_DISSIPATION_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFluxDissipation().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(FLUX_DISSIPATION_BONUS) + "% flux dissipation rate";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
}
