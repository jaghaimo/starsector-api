package com.fs.starfarer.api.impl.combat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.CombatReadinessPlugin;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class CRPluginImpl implements CombatReadinessPlugin {

	public static final float NO_SYSTEM_THRESHOLD = 0.0f;
	
	public static final float IMPROVE_START = 0.7f;
	public static final float DEGRADE_START = 0.5f;
	public static final float SHIELD_MALFUNCTION_START = 0.1f;
	public static final float CRITICAL_MALFUNCTION_START = 0.2f;
	public static final float MALFUNCTION_START = 0.4f;
	public static final float MISSILE_AMMO_REDUCTION_START = 0.4f;
	
	public static final float MAX_MOVEMENT_CHANGE = 10f; // percent
	public static final float MAX_DAMAGE_TAKEN_CHANGE = 10f; // percent
	//public static final float MAX_ROF_CHANGE = 25f; // percent
	public static final float MAX_DAMAGE_CHANGE = 10f; // percent
	public static final float MAX_REFIT_CHANGE = 10f; // percent
	
	public static final float MAX_SHIELD_MALFUNCTION_CHANCE = 5f; // percent
	public static final float MAX_CRITICAL_MALFUNCTION_CHANCE = 25f; // percent
	public static final float MAX_ENGINE_MALFUNCTION_CHANCE = 7.5f; // percent
	public static final float MAX_WEAPON_MALFUNCTION_CHANCE = 10f; // percent
	
	
	public void applyMaxCRCrewModifiers(FleetMemberAPI member) {
		//float maxCRBasedOnLevel = (40f + member.getCrewFraction() * 10f) / 100f;
		float maxCRBasedOnLevel = 0.7f;
		member.getStats().getMaxCombatReadiness().modifyFlat("crew skill bonus", maxCRBasedOnLevel, "Basic maintenance");
		
		float cf = member.getCrewFraction();
		if (cf < 1) {
			float penalty = 0.5f * (1f - cf);
			//float penalty = (1f - cf);
			member.getStats().getMaxCombatReadiness().modifyFlat("crew understrength", -penalty, "Crew understrength");
		} else {
			member.getStats().getMaxCombatReadiness().unmodifyFlat("crew understrength");
		}
	}
	
	
	
	public List<CRStatusItemData> getCRStatusDataForShip(ShipAPI ship) {
		float startingCR = ship.getCRAtDeployment();
		float cr = ship.getCurrentCR();
		
		List<CRStatusItemData> list = new ArrayList<CRStatusItemData>();
		
		String icon = null;
		
		if (cr > getImproveThreshold(ship.getMutableStats())) {
			icon = Global.getSettings().getSpriteName("ui", "icon_tactical_cr_bonus");
		} else if (cr < getDegradeThreshold(ship.getMutableStats())) {
			icon = Global.getSettings().getSpriteName("ui", "icon_tactical_cr_penalty");
		} else {
			icon = Global.getSettings().getSpriteName("ui", "icon_tactical_cr_neutral");
			//return list;
		}
		
		String title = "Combat Readiness " + Math.round(cr * 100f) + "%";
		
		String malfStr = getMalfunctionString(ship.getMutableStats(), cr);
		
		if (cr <= NO_SYSTEM_THRESHOLD && ship.getShield() != null) {
			CRStatusItemData itemData = new CRStatusItemData(statusKeys[9], icon, title,
					"Shields offline", true);
			list.add(itemData);
		}
		
		if (cr <= NO_SYSTEM_THRESHOLD) {
			boolean hasWings = false;
			for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
				if (bay.getWing() != null) {
					hasWings = true;
					break;
				}
			}
			if (hasWings) {
				CRStatusItemData itemData = new CRStatusItemData(statusKeys[10], icon, title,
						"Fighter bays offline", true);
				list.add(itemData);
			}
		}
		
		if (cr <= NO_SYSTEM_THRESHOLD && ship.getPhaseCloak() != null) {
			CRStatusItemData itemData = new CRStatusItemData(statusKeys[8], icon, title,
					ship.getPhaseCloak().getDisplayName() + " offline", true);
			list.add(itemData);
		}
		
		if (cr <= NO_SYSTEM_THRESHOLD && ship.getSystem() != null) {
			CRStatusItemData itemData = new CRStatusItemData(statusKeys[7], icon, title,
					 ship.getSystem().getDisplayName() + " offline", true);
			list.add(itemData);
		}
		
		if (cr < getMalfunctionThreshold(ship.getMutableStats())) {
			CRStatusItemData itemData = new CRStatusItemData(statusKeys[0], icon, title,
					 "malfunction risk: " + malfStr, true);
			list.add(itemData);
		}
		if (startingCR < getMissileAmmoReductionThreshold(ship.getMutableStats())) {
			CRStatusItemData itemData = new CRStatusItemData(statusKeys[2], icon, title,
					 "missiles not fully loaded", true);
			list.add(itemData);
		}
		
		if (cr < getDegradeThreshold(ship.getMutableStats())) {
			CRStatusItemData itemData = new CRStatusItemData(statusKeys[3], icon, title,
					"degraded performance", true);
			list.add(itemData);
		} else if (cr > getImproveThreshold(ship.getMutableStats())) {
			CRStatusItemData itemData = new CRStatusItemData(statusKeys[4], icon, title,
					"improved performance", false);
			list.add(itemData);
		} else {
			CRStatusItemData itemData = new CRStatusItemData(statusKeys[8], icon, title,
					"standard performance", false);
			list.add(itemData);
		}
		
		if (ship.losesCRDuringCombat() && cr > 0) {
			//float noLossTime = ship.getHullSpec().getNoCRLossTime();
			float noLossTime = ship.getMutableStats().getPeakCRDuration().computeEffective(ship.getHullSpec().getNoCRLossTime());
			if (noLossTime > ship.getTimeDeployedForCRReduction()) {
				CRStatusItemData itemData = new CRStatusItemData(statusKeys[5], icon, "peak active performance",
						"remaining time: " + (int) (noLossTime - ship.getTimeDeployedForCRReduction()) + " sec", false);
				list.add(itemData);
			} else {
				CRStatusItemData itemData = new CRStatusItemData(statusKeys[6], icon, "combat stresses",
						"degrading readiness", true);
				list.add(itemData);
			}
		}
		
		return list;
	}
	
	
	private float getWeaponMalfuctionPercent(MutableShipStatsAPI stats, float cr) {
		return MAX_WEAPON_MALFUNCTION_CHANCE * (getMalfunctionThreshold(stats) - cr) / getMalfunctionThreshold(stats);
	}
	private float getEngineMalfuctionPercent(MutableShipStatsAPI stats, float cr) {
		return MAX_ENGINE_MALFUNCTION_CHANCE * (getMalfunctionThreshold(stats)- cr) / getMalfunctionThreshold(stats);
	}
	private float getCriticalMalfuctionPercent(MutableShipStatsAPI stats, float cr) {
		return MAX_CRITICAL_MALFUNCTION_CHANCE * (getCriticalMalfunctionThreshold(stats)- cr) / getCriticalMalfunctionThreshold(stats);
	}
	private float getShieldMalfuctionPercent(MutableShipStatsAPI stats, float cr) {
		return MAX_SHIELD_MALFUNCTION_CHANCE * (getShieldMalfunctionThreshold(stats)- cr) / getShieldMalfunctionThreshold(stats);
	}
	
	
	private float getMovementChangePercent(MutableShipStatsAPI stats, float cr) {
//		if (cr > 0) {
//			System.out.println("wefwefe");
//		}
		float movementChange = 0f;
		float d = getDegradeThreshold(stats);
		float i = getImproveThreshold(stats);
		if (cr < d) {
			float f = (d - cr) / d;
			movementChange = -1f * f * MAX_MOVEMENT_CHANGE;
		} else if (cr > i) {
			float f = (cr - i) / (1f - i);
			movementChange = 1f * f * MAX_MOVEMENT_CHANGE;
		}
		return movementChange;
	}
	
	private float getDamageTakenChangePercent(MutableShipStatsAPI stats, float cr) {
		float damageTakenChange = 0f;
		float d = getDegradeThreshold(stats);
		float i = getImproveThreshold(stats);
		if (cr < d) {
			float f = (d - cr) / d;
			damageTakenChange = 1f * f * MAX_DAMAGE_TAKEN_CHANGE;
		} else if (cr > i) {
			float f = (cr - i) / (1f - i);
			damageTakenChange = -1f * f * MAX_DAMAGE_TAKEN_CHANGE;
		}
		return damageTakenChange;
	}
	
	private float getRefitTimeChangePercent(MutableShipStatsAPI stats, float cr) {
		float refitTimeChange = 0f;
		float d = getDegradeThreshold(stats);
		float i = getImproveThreshold(stats);
		if (cr < d) {
			float f = (d - cr) / d;
			refitTimeChange = 1f * f * MAX_REFIT_CHANGE;
		} else if (cr > i) {
			float f = (cr - i) / (1f - i);
			refitTimeChange = -1f * f * MAX_REFIT_CHANGE;
		}
		return refitTimeChange;
	}
	
	private float getDamageChangePercent(MutableShipStatsAPI stats, float cr) {
		float damageChange = 0f;
		float d = getDegradeThreshold(stats);
		float i = getImproveThreshold(stats);
		if (cr < d) {
			float f = (d - cr) / d;
			damageChange = -1f * f * MAX_DAMAGE_CHANGE;
		} else if (cr > i) {
			float f = (cr - i) / (1f - i);
			damageChange = 1f * f * MAX_DAMAGE_CHANGE;
		}
		return damageChange;
	}
//	private float getRateOfFireChangePercent(float cr) {
//		float rateOfFireChange = 0f;
//		if (cr < DEGRADE_START) {
//			float f = (DEGRADE_START - cr) / DEGRADE_START;
//			rateOfFireChange = -1f * f * MAX_ROF_CHANGE;
//		} else if (cr > IMPROVE_START) {
//			float f = (cr - IMPROVE_START) / (1f - IMPROVE_START);
//			rateOfFireChange = 1f * f * MAX_ROF_CHANGE;
//		}
//		return rateOfFireChange;
//	}
	
	/**
	 * From negative whatever to best accuracy of 1.
	 * @param cr
	 * @return
	 */
	private float getAimAccuracy(float cr) {
		return cr * 1.5f - 0.5f;
	}
	
	public void applyCRToStats(float cr, MutableShipStatsAPI stats, HullSize hullSize) {
		String id = "cr_effect";
		//System.out.println("CR: " + cr);
		boolean fighter = hullSize == HullSize.FIGHTER;

//		if (hullSize == HullSize.CAPITAL_SHIP) {
//			System.out.println("Applying CR value of " + cr + " to stats " + stats + "(" + hullSize.name() + ")");
//			//new RuntimeException().printStackTrace();
//		}

		if (!fighter) {
			if (cr < getMalfunctionThreshold(stats)) {
				stats.getWeaponMalfunctionChance().modifyFlat(id, 0.01f * getWeaponMalfuctionPercent(stats, cr));
				stats.getEngineMalfunctionChance().modifyFlat(id, 0.01f * getEngineMalfuctionPercent(stats, cr));
			} else {
				stats.getWeaponMalfunctionChance().unmodify(id);
				stats.getEngineMalfunctionChance().unmodify(id);
			}
		}
		
		if (!fighter) {
			if (cr < getCriticalMalfunctionThreshold(stats)) {
				stats.getCriticalMalfunctionChance().modifyFlat(id, 0.01f * getCriticalMalfuctionPercent(stats, cr));
			} else {
				stats.getCriticalMalfunctionChance().unmodify(id);
			}
		}
		
		if (!fighter) {
			if (cr < getShieldMalfunctionThreshold(stats)) {
				stats.getShieldMalfunctionChance().modifyFlat(id, 0.01f * getShieldMalfuctionPercent(stats, cr));
				stats.getShieldMalfunctionFluxLevel().modifyFlat(id, 0.75f);
			} else {
				stats.getShieldMalfunctionChance().unmodify(id);
				stats.getShieldMalfunctionFluxLevel().unmodify(id);
			}
		}
	
		if (!fighter) {
			if (stats.getEntity() instanceof ShipAPI) {
				ShipAPI ship = (ShipAPI)stats.getEntity();
				if (cr <= NO_SYSTEM_THRESHOLD) {
					ship.setShipSystemDisabled(true);
					ship.setDefenseDisabled(true);
				} else {
					ship.setShipSystemDisabled(false);
					ship.setDefenseDisabled(false);
				}
			}
		}
		
		float movementChange = getMovementChangePercent(stats, cr);
		float damageTakenChange = getDamageTakenChangePercent(stats, cr);
		float damageChange = getDamageChangePercent(stats, cr);
		float refitTimeChange = getRefitTimeChangePercent(stats, cr);
		
		if (refitTimeChange != 0) {
			stats.getFighterRefitTimeMult().modifyPercent(id, refitTimeChange);
		} else {
			stats.getFighterRefitTimeMult().unmodify(id);
		}
		
		if (movementChange != 0) {
			stats.getMaxSpeed().modifyPercent(id, movementChange);
			stats.getAcceleration().modifyPercent(id, movementChange);
			stats.getDeceleration().modifyPercent(id, movementChange);
			stats.getTurnAcceleration().modifyPercent(id, movementChange);
			stats.getMaxTurnRate().modifyPercent(id, movementChange);
		} else {
			stats.getMaxSpeed().unmodify(id);
			stats.getAcceleration().unmodify(id);
			stats.getDeceleration().unmodify(id);
			stats.getTurnAcceleration().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
		}
		
		if (damageTakenChange != 0) {
			stats.getArmorDamageTakenMult().modifyPercent(id, damageTakenChange);
			stats.getHullDamageTakenMult().modifyPercent(id, damageTakenChange);
			stats.getShieldDamageTakenMult().modifyPercent(id, damageTakenChange);
		} else {
			stats.getArmorDamageTakenMult().unmodify(id);
			stats.getHullDamageTakenMult().unmodify(id);
			stats.getShieldDamageTakenMult().unmodify(id);
		}
		
		if (damageChange != 0) {
			stats.getBallisticWeaponDamageMult().modifyPercent(id, damageChange);
			stats.getEnergyWeaponDamageMult().modifyPercent(id, damageChange);
			stats.getMissileWeaponDamageMult().modifyPercent(id, damageChange);
		} else {
			stats.getBallisticWeaponDamageMult().unmodify(id);
			stats.getEnergyWeaponDamageMult().unmodify(id);
			stats.getMissileWeaponDamageMult().unmodify(id);
		}
		
		float aimAccuracy = getAimAccuracy(cr);
		stats.getAutofireAimAccuracy().modifyFlat(id, aimAccuracy);
	}
	
	public void applyCRToShip(float cr, ShipAPI ship) {
		if (!ship.isFighter() && cr < getMissileAmmoReductionThreshold(ship.getMutableStats())) {
			for (WeaponAPI weapon : ship.getAllWeapons()) {
				if (weapon.getType() == WeaponType.MISSILE) {
					float ammo = (float) weapon.getMaxAmmo() * getMissileLoadedFraction(ship.getMutableStats(), cr);
					if (ammo < 0) ammo = 0;
					weapon.setAmmo(Math.round(ammo));
				}
			}
		}
		ship.setCRAtDeployment(cr);
		
		float c = getCriticalMalfunctionThreshold(ship.getMutableStats());
		if (cr < c && !ship.controlsLocked() && !ship.isFighter()) {
			float severity = (c - cr) / (c);
			if (Global.getCombatEngine() != null) { // can be null if coming from refit on a fresh app start in campaign
				
				float criticalMult = 1f;
				for (StatMod mod : ship.getMutableStats().getCriticalMalfunctionChance().getMultMods().values()) { 
					criticalMult *= mod.getValue();
				}
				severity *= criticalMult;
				Global.getCombatEngine().addPlugin(new LowCRShipDamageSequence(ship, severity));
			}
		}
	}
	
	public float getMissileLoadedFraction(MutableShipStatsAPI stats, float cr) {
		if (true) return 1f;
		float test = Global.getSettings().getFloat("noDeployCRPercent") * 0.01f;
		float f = (cr - test) / (getMissileAmmoReductionThreshold(stats) - test);
		if (f > 1) f = 1;
		if (f < 0) f = 0;
		return f;
	}
	
	
	public float getMalfunctionThreshold(MutableShipStatsAPI stats) {
		float mult = 1f;
		if (stats != null) mult *= stats.getDynamic().getStat(Stats.CR_MALFUNCION_RANGE).getModifiedValue();
		return MALFUNCTION_START * mult;
	}
	public float getCriticalMalfunctionThreshold(MutableShipStatsAPI stats) {
		float mult = 1f;
		if (stats != null) mult *= stats.getDynamic().getStat(Stats.CR_MALFUNCION_RANGE).getModifiedValue();
		return CRITICAL_MALFUNCTION_START * mult;
	}
	
	public float getShieldMalfunctionThreshold(MutableShipStatsAPI stats) {
		float mult = 1f;
		if (stats != null) mult *= stats.getDynamic().getStat(Stats.CR_MALFUNCION_RANGE).getModifiedValue();
		return SHIELD_MALFUNCTION_START * mult;
	}
	
	public float getMissileAmmoReductionThreshold(MutableShipStatsAPI stats) {
		float mult = 1f;
		if (stats != null) mult *= stats.getDynamic().getStat(Stats.CR_MALFUNCION_RANGE).getModifiedValue();
		return MISSILE_AMMO_REDUCTION_START * mult;
	}
	
	public float getDegradeThreshold(MutableShipStatsAPI stats) {
		float mult = 1f;
		if (stats != null) mult *= stats.getDynamic().getStat(Stats.CR_MALFUNCION_RANGE).getModifiedValue();
		return DEGRADE_START * mult;
	}
	
	public float getImproveThreshold(MutableShipStatsAPI stats) {
		float mult = 1f;
		//if (stats != null) mult *= stats.getDynamic().getStat(Stats.CR_MALFUNCION_RANGE).getModifiedValue();
		return IMPROVE_START * mult;
	}
	
	
	/**
	 * @param cr from 0 to 1
	 * @param shipOrWing "ship" or "fighter wing".
	 * @return
	 */
	public CREffectDescriptionForTooltip getCREffectDescription(float cr, String shipOrWing, FleetMemberAPI member) {
		CREffectDescriptionForTooltip result = new CREffectDescriptionForTooltip();
		
		List<CREffectDetail> details = getCREffectDetails(cr, member);
		boolean hasPositive = false;
		boolean hasNegative = false;
		for (CREffectDetail detail : details) {
			if (detail.getType() == CREffectDetailType.BONUS) hasPositive = true;
			if (detail.getType() == CREffectDetailType.PENALTY) hasNegative = true;
		}
		
		float noDeploy = Global.getSettings().getFloat("noDeployCRPercent") * 0.01f;
		String crStr = (int)(cr * 100f) + "%";
		String str;
		if (cr < noDeploy) {
			str = String.format("The %s is not ready for combat and can not be deployed in battle.", shipOrWing);
		} else if (cr < getCriticalMalfunctionThreshold(member.getStats())) {
			str = String.format("The %s suffers from degraded performance and runs the risk of permanent and damaging malfunctions if deployed.", shipOrWing);
		} else if (cr < getMalfunctionThreshold(member.getStats())) {
			str = String.format("The %s suffers from degraded performance and runs the risk of weapon and engine malfunctions during combat.", shipOrWing);
		} else if (cr < getDegradeThreshold(member.getStats()) && hasNegative) {
			str = String.format("The %s suffers from degraded performance during combat.", shipOrWing);
		} else if (cr < getImproveThreshold(member.getStats()) || !hasPositive) {
			str = String.format("The %s has standard combat performance.", shipOrWing);
		} else {
			str = String.format("The %s benefits from improved combat performance.", shipOrWing);
		}
		
		//result.getHighlights().add(crStr);
		

		if (member.isFighterWing()) {
			boolean canReplaceFighters = false;
			FleetDataAPI data = member.getFleetData();
			if (data != null) {
				for (FleetMemberAPI curr : data.getMembersListCopy()) {
					if (curr.isMothballed()) continue;
					if (curr.getNumFlightDecks() > 0) {
						canReplaceFighters = true;
						break;
					}
				}
			}
			if (canReplaceFighters) {
				details.add(new CREffectDetail("", "", CREffectDetailType.NEUTRAL));
				float costPer = member.getStats().getCRPerDeploymentPercent().computeEffective(member.getVariant().getHullSpec().getCRToDeploy()) / 100f;
				String numStr = "" + (int) Math.ceil((float)((int) (cr * 100f)) / (costPer * 100f));
				
				str += " " + numStr + " fighter chassis are ready to replace combat losses.";
				result.getHighlights().add(numStr);
				
			} else {
				details.add(new CREffectDetail("Replacement chassis", "None", CREffectDetailType.PENALTY));
				str += " " + "Fighter losses can not be replaced due to the lack of a ship with proper facilities (i.e. a flight deck).";
			}
		}
		
		
		result.setString(str);
		
		return result;
	}
	
	private String getMalfunctionString(MutableShipStatsAPI stats, float cr) {
		String malfStr = "None";
		if (cr < getCriticalMalfunctionThreshold(stats)) {
			malfStr = "Critical";
		} else if (cr < 0.3f) {
			malfStr = "Serious";
		} else if (cr < getMalfunctionThreshold(stats)) {
			malfStr = "Low";
		}
		return malfStr;
	}
	
	public List<CREffectDetail> getCREffectDetails(float cr, FleetMemberAPI member) {
		List<CREffectDetail> result = new ArrayList<CREffectDetail>();
		
		int engine = (int) getEngineMalfuctionPercent(member.getStats(), cr);
		int weapon = (int) getWeaponMalfuctionPercent(member.getStats(), cr);
		
		int speed = (int) Math.round(getMovementChangePercent(member.getStats(), cr));
		int damage = (int) Math.round(getDamageTakenChangePercent(member.getStats(), cr));
		int damageDealt = (int) Math.round(getDamageChangePercent(member.getStats(), cr));
		int refit = (int) Math.round(getRefitTimeChangePercent(member.getStats(), cr));
		
		float acc = getAimAccuracy(cr);
		
		String malfStr = getMalfunctionString(member.getStats(), cr);
		
		String accString;
		CREffectDetailType accType;
		if (acc < 0) {
			accString = "Very poor";
			accType = CREffectDetailType.PENALTY;
		} else if (acc < 0.25f) {
			accString = "Poor";
			accType = CREffectDetailType.PENALTY;
		} else if (acc < 0.67) {
			accString = "Standard";
			accType = CREffectDetailType.NEUTRAL;
		} else {
			accString = "Excellent";
			accType = CREffectDetailType.BONUS;
		}
		
		String speedStr = speed + "%";
		if (speed >= 0) {
			speedStr = "+" + speedStr;
		}
		String damageStr = damage + "%";
		if (damage >= 0) {
			damageStr = "+" + damageStr;
		}
		String rofStr = damageDealt + "%";
		if (damageDealt >= 0) {
			rofStr = "+" + rofStr;
		}
		
		String refitStr = refit + "%";
		if (refit >= 0) {
			refitStr = "+" + refitStr;
		}
		
		result.add(new CREffectDetail("Maneuverability", speedStr, getTypeFor(speed, false)));
		result.add(new CREffectDetail("Damage taken", damageStr, getTypeFor(damage, true)));
		result.add(new CREffectDetail("Damage dealt", rofStr, getTypeFor(damageDealt, false)));
		
		if (member.getNumFlightDecks() > 0) {
			result.add(new CREffectDetail("Fighter refit time", refitStr, getTypeFor(refit, true)));
		}
		
		result.add(new CREffectDetail("Autofire accuracy", accString, accType));
		

		
		CREffectDetailType malfType = CREffectDetailType.NEUTRAL;
		if (getWeaponMalfuctionPercent(member.getStats(), cr) > 0) {
			malfType = CREffectDetailType.PENALTY;
		}

		result.add(new CREffectDetail("Malfunction risk", malfStr, malfType));
		
		Collection<String> slots = member.getVariant().getFittedWeaponSlots();
		boolean hasMissiles = false;
		for (String slotId : slots) {
			WeaponSpecAPI w = member.getVariant().getWeaponSpec(slotId);
			if (w.getType() == WeaponType.MISSILE) {
				hasMissiles = true;
				break;
			}
		}
		if (hasMissiles) {
			float missileFraction = getMissileLoadedFraction(member.getStats(), cr);
			if (missileFraction < 0) missileFraction = 0;
			String missileStr = (int)(missileFraction * 100f) + "%";
			
			if (missileFraction < 1f) {
				result.add(new CREffectDetail("Missile magazines", missileStr, missileFraction < 1 ? CREffectDetailType.PENALTY : CREffectDetailType.NEUTRAL));
			}
		}
		
		return result;
	}
	
	private CREffectDetailType getTypeFor(int val, boolean invert) {
		if (invert) {
			if (val < 0) return CREffectDetailType.BONUS;
			else if (val > 0) return CREffectDetailType.PENALTY;
			return CREffectDetailType.NEUTRAL;
		} else {
			if (val > 0) return CREffectDetailType.BONUS;
			else if (val < 0) return CREffectDetailType.PENALTY;
			return CREffectDetailType.NEUTRAL;
		}
	}
	
	protected static Object [] statusKeys  = new Object [] {
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
			new Object(),
		};

	
	
	public boolean isOkToPermanentlyDisable(ShipAPI ship, Object module) {
		return isOkToPermanentlyDisableStatic(ship, module);
	}
	
	public static boolean isOkToPermanentlyDisableStatic(ShipAPI ship, Object module) {
		if (module instanceof ShipEngineAPI) {
			float fractionIfDisabled = ((ShipEngineAPI) module).getContribution() + ship.getEngineFractionPermanentlyDisabled();
			if (fractionIfDisabled > 0.66f) {
				return false;
			} else {
				return true;
			}
		}
		
		if (module instanceof WeaponAPI) {
			WeaponType type = ((WeaponAPI)module).getType();
			if (type == WeaponType.DECORATIVE || type == WeaponType.LAUNCH_BAY || type == WeaponType.SYSTEM) {
				return false;
			}
			
			if (ship.getCurrentCR() <= 0) {
				return true;
			}
			
			List<Object> usableWeapons = new ArrayList<Object>();
			for (WeaponAPI weapon : ship.getAllWeapons()) {
				if (weapon.isPermanentlyDisabled()) continue;
				if (weapon.isDecorative()) continue;
				if (weapon.getSlot().isSystemSlot()) continue;
				if (weapon.getSlot().isDecorative()) continue;
				if (weapon.getSlot().isStationModule()) continue;
				if (weapon.getAmmo() > 0 && (weapon.getMaxAmmo() > 20 || weapon.getSpec().getAmmoPerSecond() > 0)) {
					usableWeapons.add(weapon);
				}
			}
			usableWeapons.remove(module);
			
			return usableWeapons.size() >= 1;
		}
		return false;
	}
}


