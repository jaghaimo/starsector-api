package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MissileSpecialization {
	
	public static final float MISSILE_AMMO_BONUS = 100f;
	public static final float MISSILE_SPEC_PERK_HEALTH_BONUS = 50f;
	public static final float MISSILE_SPEC_ROF_BONUS = 50f;
	public static final float MISSILE_SPEC_DAMAGE_BONUS = 10f;

	public static class Level1 implements ShipSkillEffect {
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMissileAmmoBonus().modifyPercent(id, MISSILE_AMMO_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMissileAmmoBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MISSILE_AMMO_BONUS) + "% missile weapon ammo capacity";
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
			stats.getMissileHealthBonus().modifyPercent(id, MISSILE_SPEC_PERK_HEALTH_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMissileHealthBonus().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			//return "+" + (int)(MISSILE_SPEC_PERK_HEALTH_BONUS) + "% missile, rocket, bomb, and torpedo hitpoints";
			return "+" + (int)(MISSILE_SPEC_PERK_HEALTH_BONUS) + "% missile hitpoints";
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
			//stats.getMissileWeaponDamageMult().modifyPercent(id, MISSILE_SPEC_PERK_DAMAGE_BONUS);
			stats.getMissileRoFMult().modifyPercent(id, MISSILE_SPEC_ROF_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			//stats.getMissileWeaponDamageMult().unmodify(id);
			stats.getMissileRoFMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MISSILE_SPEC_ROF_BONUS) + "% rate of fire for missile weapons";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	
	public static class Level4 implements ShipSkillEffect {
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			stats.getMissileWeaponDamageMult().modifyPercent(id, MISSILE_SPEC_DAMAGE_BONUS);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getMissileWeaponDamageMult().unmodify(id);
		}
		
		public String getEffectDescription(float level) {
			return "+" + (int)(MISSILE_SPEC_DAMAGE_BONUS) + "% damage dealt by missile weapons";
		}
		
		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}
