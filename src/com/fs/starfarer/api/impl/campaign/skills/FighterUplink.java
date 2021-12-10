package com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.FleetTotalItem;
import com.fs.starfarer.api.characters.FleetTotalSource;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class FighterUplink {
	
	//public static float DAMAGE_PERCENT = 10;
	
	public static float MAX_SPEED_PERCENT = 20;
	public static float CREW_LOSS_PERCENT = 50;
	
	public static float TARGET_LEADING_BONUS = 50f;
	
	public static float OFFICER_MULT = 1.5f;
	public static boolean isOfficer(MutableShipStatsAPI stats) {
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship == null) return false;
			if (ship.isFighter() && ship.getWing() != null && ship.getWing().getSourceShip() != null) {
				ship = ship.getWing().getSourceShip();
			}
			return ship.getCaptain() != null && !ship.getCaptain().isDefault();
		} else {
			FleetMemberAPI member = stats.getFleetMember();
			if (member == null) return false;
			return !member.getCaptain().isDefault();
		}
	}
	
	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			float crewLossReduction = computeAndCacheThresholdBonus(stats, "fu_crew_loss", CREW_LOSS_PERCENT, ThresholdBonusType.FIGHTER_BAYS);
			if (isOfficer(stats)) crewLossReduction *= OFFICER_MULT;
			stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, 1f - crewLossReduction / 100f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).unmodifyMult(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);


			FleetDataAPI data = getFleetData(null);
			float crewLossReduction = computeAndCacheThresholdBonus(data, stats, "fu_crew_loss", CREW_LOSS_PERCENT, ThresholdBonusType.FIGHTER_BAYS);

			info.addPara("-%s crew lost due to fighter losses in combat (maximum: %s)", 0f, hc, hc,
					"" + (int) crewLossReduction + "%",
					"" + (int) CREW_LOSS_PERCENT + "%");
			//info.addSpacer(5f);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_SHIPS;
		}

	}

	
	public static class Level2 extends BaseSkillEffectDescription implements ShipSkillEffect, FleetTotalSource {
		
		public FleetTotalItem getFleetTotalItem() {
			return getFighterBaysTotal();
		}
		
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
//			float damBonus = getDamageBonus(stats);
//			stats.getBallisticWeaponDamageMult().modifyPercent(id, damBonus);
//			stats.getEnergyWeaponDamageMult().modifyPercent(id, damBonus);
//			stats.getMissileWeaponDamageMult().modifyPercent(id, damBonus);
			
			float speedBonus = getMaxSpeedBonus(stats);
			if (isOfficer(stats)) speedBonus *= OFFICER_MULT;
			stats.getMaxSpeed().modifyPercent(id, speedBonus);
			stats.getAcceleration().modifyPercent(id, speedBonus * 2f);
			stats.getDeceleration().modifyPercent(id, speedBonus * 2f);
			
			float aimBonus = getAimBonus(stats);
			if (isOfficer(stats)) aimBonus *= OFFICER_MULT;
			stats.getAutofireAimAccuracy().modifyFlat(id, aimBonus * 0.01f);
		}
		
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
//			stats.getBallisticWeaponDamageMult().unmodifyPercent(id);
//			stats.getEnergyWeaponDamageMult().unmodifyPercent(id);
//			stats.getMissileWeaponDamageMult().unmodifyPercent(id);
			
			stats.getMaxSpeed().unmodifyPercent(id);
			stats.getAcceleration().unmodifyPercent(id);
			stats.getDeceleration().unmodifyPercent(id);
			
			stats.getAutofireAimAccuracy().unmodifyFlat(id);
		}
		
		public String getEffectDescription(float level) {
			return null;
		}
		
//		protected float getDamageBonus(MutableShipStatsAPI stats) {
//			FleetDataAPI data = getFleetData(stats);
//			return getDamageBonus(data);
//		}
//		protected float getDamageBonus(FleetDataAPI data) {
//			if (data == null) return DAMAGE_PERCENT;
//			String key = "fighter_uplink_damage";
//			Float bonus = (Float) data.getCacheClearedOnSync().get(key);
//			if (bonus != null) return bonus;
//			
//			float bays = getNumFighterBays(data);
//			bonus = getThresholdBasedRoundedBonus(DAMAGE_PERCENT, bays, FIGHTER_BAYS_THRESHOLD);
//			
//			data.getCacheClearedOnSync().put(key, bonus);
//			return bonus;
//		}
		protected float getMaxSpeedBonus(MutableShipStatsAPI stats) {
			FleetDataAPI data = getFleetData(stats);
			return getMaxSpeedBonus(data);
		}
		
		protected float getMaxSpeedBonus(FleetDataAPI data) {
			if (data == null) return MAX_SPEED_PERCENT;
			String key = "fighter_uplink_max_speed";
			Float bonus = (Float) data.getCacheClearedOnSync().get(key);
			if (bonus != null) return bonus;
			
			float bays = getNumFighterBays(data);
			bonus = getThresholdBasedRoundedBonus(MAX_SPEED_PERCENT, bays, FIGHTER_BAYS_THRESHOLD);
			
			data.getCacheClearedOnSync().put(key, bonus);
			return bonus;
		}
		
		protected float getAimBonus(MutableShipStatsAPI stats) {
			FleetDataAPI data = getFleetData(stats);
			return getAimBonus(data);
		}
		
		protected float getAimBonus(FleetDataAPI data) {
			if (data == null) return TARGET_LEADING_BONUS;
			String key = "fighter_uplink_aim";
			Float bonus = (Float) data.getCacheClearedOnSync().get(key);
			if (bonus != null) return bonus;
			
			float bays = getNumFighterBays(data);
			bonus = getThresholdBasedRoundedBonus(TARGET_LEADING_BONUS, bays, FIGHTER_BAYS_THRESHOLD);
			
			data.getCacheClearedOnSync().put(key, bonus);
			return bonus;
		}
		
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, 
											TooltipMakerAPI info, float width) {
			init(stats, skill);
			
			/*
+37% target leading accuracy (maximum: +50%)
	Maximum at 6 or less fighter bays in fleet, your fleet has 8 fighter bays
			 */
			
			FleetDataAPI data = getFleetData(null);
			//float damBonus = getDamageBonus(data);
			float speedBonus = getMaxSpeedBonus(data);
			float aimBonus = getAimBonus(data);
			//float bays = getNumFighterBays(data);
			
//			info.addPara("+%s damage dealt (maximum: %s)", 0f, hc, hc,
//					"" + (int) damBonus + "%",
//					"" + (int) DAMAGE_PERCENT + "%");
//			addFighterBayThresholdInfo(info, data);
//			
//			info.addSpacer(5f);
			
			info.addPara("+%s top speed (maximum: %s)", 0f, hc, hc,
					"" + (int) speedBonus + "%",
					"" + (int) MAX_SPEED_PERCENT + "%");
			info.addPara("+%s target leading accuracy (maximum: %s)", 0f, hc, hc,
					"" + (int) aimBonus + "%",
					"" + (int) TARGET_LEADING_BONUS + "%");
			addFighterBayThresholdInfo(info, data);
			info.addPara(indent + "Effect increased by %s for ships with offcers, including flagship",
					0f, tc, hc, 
					"" + Misc.getRoundedValueMaxOneAfterDecimal(OFFICER_MULT) + Strings.X);
			
			//info.addSpacer(5f);
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.ALL_FIGHTERS;
		}

	}
	


}





