package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class EvasiveAction {
	
	public static final float MANEUVERABILITY_BONUS = 50;
	public static final float DAMAGE_TO_MODULES_REDUCTION = 50;
	public static final float EFFECTIVE_ARMOR_BONUS = 50;
	

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS);
			stats.getDeceleration().modifyPercent(id, MANEUVERABILITY_BONUS);
			stats.getTurnAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS * 2f);
			stats.getMaxTurnRate().modifyPercent(id, MANEUVERABILITY_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getAcceleration().unmodify(id);
			stats.getDeceleration().unmodify(id);
			stats.getTurnAcceleration().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MANEUVERABILITY_BONUS) + "% maneuverability";
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
			stats.getEngineDamageTakenMult().modifyMult(id, 1f - DAMAGE_TO_MODULES_REDUCTION / 100f);
			stats.getWeaponDamageTakenMult().modifyMult(id, 1f - DAMAGE_TO_MODULES_REDUCTION / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getEngineDamageTakenMult().unmodify(id);
			stats.getWeaponDamageTakenMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "-" + (int)(DAMAGE_TO_MODULES_REDUCTION) + "% weapon and engine damage taken";
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
			stats.getEffectiveArmorBonus().modifyPercent(id, EFFECTIVE_ARMOR_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getEffectiveArmorBonus().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(EFFECTIVE_ARMOR_BONUS) + "% armor for damage reduction calculation only";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
}
