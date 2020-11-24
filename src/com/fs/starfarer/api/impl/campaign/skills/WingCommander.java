package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class WingCommander {
	
	public static final float SPEED_BONUS = 25f;
	public static final float DAMAGE_TO_FIGHTERS_BONUS = 30f;
	public static final float DAMAGE_TO_MISSILES_BONUS = 30f;
	public static final float TARGET_LEADING_BONUS = 50f;
	
	

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMaxSpeed().modifyPercent(id, SPEED_BONUS);
			stats.getAcceleration().modifyPercent(id, SPEED_BONUS);
			stats.getDeceleration().modifyPercent(id, SPEED_BONUS);
			stats.getTurnAcceleration().modifyPercent(id, SPEED_BONUS * 2f);
			stats.getMaxTurnRate().modifyPercent(id, SPEED_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMaxSpeed().unmodify(id);
			stats.getAcceleration().unmodify(id);
			stats.getDeceleration().unmodify(id);
			stats.getTurnAcceleration().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(SPEED_BONUS) + "% top speed and maneuverability";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.SHIP_FIGHTERS;
		}
	}

	public static class Level2A implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDamageToFighters().modifyFlat(id, DAMAGE_TO_FIGHTERS_BONUS / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToFighters().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DAMAGE_TO_FIGHTERS_BONUS) + "% damage to fighters";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.SHIP_FIGHTERS;
		}
	}
	
	public static class Level2B implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getDamageToMissiles().modifyFlat(id, DAMAGE_TO_MISSILES_BONUS / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDamageToMissiles().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DAMAGE_TO_MISSILES_BONUS) + "% damage to missiles";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.SHIP_FIGHTERS;
		}
	}
	
	public static class Level3 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getAutofireAimAccuracy().modifyFlat(id, TARGET_LEADING_BONUS * 0.01f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getAutofireAimAccuracy().unmodify(id);
		}	
		
		public String getEffectDescription(float level) {
			return "+" + (int)(TARGET_LEADING_BONUS) + "% target leading accuracy";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.SHIP_FIGHTERS;
		}
	}
	
}
