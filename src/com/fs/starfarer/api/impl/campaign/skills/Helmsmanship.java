package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class Helmsmanship {
	
	public static float ACCELERATION_BONUS = 50f;
	public static float SPEED_BONUS = 10f;
	public static float ZERO_FLUX_LEVEL = 1f;
	
	

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getAcceleration().modifyPercent(id, ACCELERATION_BONUS);
			stats.getDeceleration().modifyPercent(id, ACCELERATION_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getAcceleration().unmodify(id);
			stats.getDeceleration().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(ACCELERATION_BONUS) + "% acceleration";
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
			stats.getMaxSpeed().modifyPercent(id, SPEED_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxSpeed().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(SPEED_BONUS) + "% top speed";
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
			stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_LEVEL * 0.01f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getZeroFluxMinimumFluxLevel().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "0-flux speed boost activated at up to " + (int)(ZERO_FLUX_LEVEL) + "% flux";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
}
