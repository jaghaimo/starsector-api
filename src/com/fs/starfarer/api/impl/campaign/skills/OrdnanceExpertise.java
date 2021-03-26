package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class OrdnanceExpertise {
	
	public static final float PROJ_SPEED_BONUS = 25;
	public static final float WEAPON_HITPOINTS_BONUS = 50;
	public static final float DAMAGE_BONUS = 15;

	public static class Level1 implements ShipSkillEffect {

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getProjectileSpeedMult().modifyPercent(id, PROJ_SPEED_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getProjectileSpeedMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(PROJ_SPEED_BONUS) + "% ballistic and energy projectile speed";
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
			stats.getWeaponHealthBonus().modifyPercent(id, WEAPON_HITPOINTS_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getWeaponHealthBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(WEAPON_HITPOINTS_BONUS) + "% weapon hitpoints";
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
			stats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS);
			stats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS);
			stats.getMissileWeaponDamageMult().modifyPercent(id, DAMAGE_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getBallisticWeaponDamageMult().unmodify(id);
			stats.getEnergyWeaponDamageMult().unmodify(id);
			stats.getMissileWeaponDamageMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(DAMAGE_BONUS) + "% weapon damage";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}

	}
}
