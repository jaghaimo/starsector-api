package com.fs.starfarer.api.combat;

import java.util.List;

import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.DynamicStatsAPI;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface MutableShipStatsAPI {
	
	/**
	 * Only returns non-null during combat.
	 * @return entity (ShipAPI, MissileAPI, something else) if these mutable stats have one associated with them, null otherwise.
	 */
	public CombatEntityAPI getEntity();
	
	/**
	 * Could be null, or a faked-up one for the stats of fighter wings deployed in combat.
	 * @return
	 */
	public FleetMemberAPI getFleetMember();
	
	
	public MutableStat getMaxSpeed();
	public MutableStat getAcceleration();
	public MutableStat getDeceleration();
	public MutableStat getMaxTurnRate();
	public MutableStat getTurnAcceleration();
	
	public MutableStat getFluxCapacity();
	public MutableStat getFluxDissipation();
	
	/**
	 * Check made once per second on average. Range is 0 (no chance) to 1 (100% chance).
	 */
	public MutableStat getWeaponMalfunctionChance();
	
	/**
	 * Check made once per second on average. Range is 0 (no chance) to 1 (100% chance).
	 */
	public MutableStat getEngineMalfunctionChance();
	
	
	/**
	 * Chance that a regular malfunction is critical (i.e. deals damage and permanently disables weapon or engine).
	 * More than half the engine nozzles can not suffer a critical malfunction.
	 * @return
	 */
	public MutableStat getCriticalMalfunctionChance();

	public MutableStat getShieldMalfunctionChance();
	public MutableStat getShieldMalfunctionFluxLevel();
	
	/**
	 * Base value is 0, modified by crew etc. modifyPercent will do nothing since the base value is 0.
	 * Range is 0 to 1.
	 * @return
	 */
	public MutableStat getMaxCombatReadiness();
	
	/**
	 * As percentage, i.e 0 to 100.
	 * @return
	 */
	public StatBonus getCRPerDeploymentPercent();
	
	/**
	 * In seconds.
	 * @return
	 */
	public StatBonus getPeakCRDuration();
	
	/**
	 * As percentage, i.e 0 to 100.
	 * @return
	 */
	public StatBonus getCRLossPerSecondPercent();
	

	/**
	 * Use getEmpDamageTaken() instead.
	 * @return
	 */
	@Deprecated public MutableStat getFluxDamageTakenMult();
	public MutableStat getEmpDamageTakenMult();
	
	
	public MutableStat getHullDamageTakenMult();
	public MutableStat getArmorDamageTakenMult();
	public MutableStat getShieldDamageTakenMult();
	public MutableStat getEngineDamageTakenMult();
	public MutableStat getWeaponDamageTakenMult();
	
	/**
	 * Applies to damage taken by hull and armor.
	 * @return
	 */
	public MutableStat getBeamDamageTakenMult();
	/**
	 * Applies to damage taken by hull and armor.
	 * @return
	 */
	public MutableStat getMissileDamageTakenMult();
	/**
	 * Applies to damage taken by hull and armor.
	 * @return
	 */
	public MutableStat getProjectileDamageTakenMult();
	/**
	 * Applies to damage taken by hull and armor.
	 * @return
	 */
	public MutableStat getEnergyDamageTakenMult();
	/**
	 * Applies to damage taken by hull and armor.
	 * @return
	 */
	public MutableStat getKineticDamageTakenMult();
	/**
	 * Applies to damage taken by hull and armor.
	 * @return
	 */
	public MutableStat getHighExplosiveDamageTakenMult();
	/**
	 * Applies to damage taken by hull and armor.
	 * @return
	 */
	public MutableStat getFragmentationDamageTakenMult();
	
	/**
	 * Applies to damage taken by shields.
	 * @return
	 */
	public MutableStat getBeamShieldDamageTakenMult();
	/**
	 * Applies to damage taken by shields.
	 * @return
	 */
	public MutableStat getMissileShieldDamageTakenMult();
	/**
	 * Applies to damage taken by shields.
	 * @return
	 */
	public MutableStat getProjectileShieldDamageTakenMult();
	/**
	 * Applies to damage taken by shields.
	 * @return
	 */
	public MutableStat getEnergyShieldDamageTakenMult();
	/**
	 * Applies to damage taken by shields.
	 * @return
	 */
	public MutableStat getKineticShieldDamageTakenMult();
	/**
	 * Applies to damage taken by shields.
	 * @return
	 */
	public MutableStat getHighExplosiveShieldDamageTakenMult();
	/**
	 * Applies to damage taken by shields.
	 * @return
	 */
	public MutableStat getFragmentationShieldDamageTakenMult();
	
	
	
	public MutableStat getBeamWeaponDamageMult();
	public MutableStat getEnergyWeaponDamageMult();
	public MutableStat getBallisticWeaponDamageMult();
	public MutableStat getMissileWeaponDamageMult();
	
	public StatBonus getEnergyWeaponFluxCostMod();
	public StatBonus getBallisticWeaponFluxCostMod();
	public StatBonus getMissileWeaponFluxCostMod();
	public MutableStat getBeamWeaponFluxCostMult();
	
	public MutableStat getShieldUpkeepMult();
	public MutableStat getShieldAbsorptionMult();
	public MutableStat getShieldTurnRateMult();
	public MutableStat getShieldUnfoldRateMult();
	
	public MutableStat getMissileRoFMult();
	public MutableStat getBallisticRoFMult();
	public MutableStat getEnergyRoFMult();
	
	public StatBonus getPhaseCloakActivationCostBonus();
	public StatBonus getPhaseCloakUpkeepCostBonus();

	public StatBonus getEnergyWeaponRangeBonus();
	public StatBonus getBallisticWeaponRangeBonus();
	public StatBonus getMissileWeaponRangeBonus();
	public StatBonus getBeamWeaponRangeBonus();
	
	/**
	 * Does not include beam weapons, which have a separate bonus.
	 * @return
	 */
	public StatBonus getWeaponTurnRateBonus();
	public StatBonus getBeamWeaponTurnRateBonus();
	//public StatBonus getRepairTimeBonus();
	public MutableStat getCombatEngineRepairTimeMult();
	public MutableStat getCombatWeaponRepairTimeMult();
	public StatBonus getWeaponHealthBonus();
	public StatBonus getEngineHealthBonus();
	public StatBonus getArmorBonus();
	public StatBonus getHullBonus();
	
	public StatBonus getShieldArcBonus();
	
	public StatBonus getBallisticAmmoBonus();
	public StatBonus getEnergyAmmoBonus();
	public StatBonus getMissileAmmoBonus();
	
	public MutableStat getEccmChance();
	public MutableStat getMissileGuidance();
	
	public StatBonus getSightRadiusMod();
	
	public MutableStat getHullCombatRepairRatePercentPerSecond();
	public MutableStat getMaxCombatHullRepairFraction();
	@Deprecated public MutableStat getHullRepairRatePercentPerSecond();
	@Deprecated public MutableStat getMaxHullRepairFraction();
	
	/**
	 * For hit strength only.
	 * @return
	 */
	public StatBonus getEffectiveArmorBonus();
	
	/**
	 * Affects damage reduction by target's armor.
	 * @return
	 */
	public StatBonus getHitStrengthBonus();
	public MutableStat getDamageToTargetEnginesMult();
	public MutableStat getDamageToTargetWeaponsMult();
	public MutableStat getDamageToTargetShieldsMult();
	
	/**
	 * Clamped to a maximum of 1. Green crew at 0.
	 * @return
	 */
	public MutableStat getAutofireAimAccuracy();
	
	public MutableStat getMaxRecoilMult();
	public MutableStat getRecoilPerShotMult();
	public MutableStat getRecoilDecayMult();
	
	public StatBonus getOverloadTimeMod();
	
	public MutableStat getZeroFluxSpeedBoost();
	public MutableStat getZeroFluxMinimumFluxLevel();
	
	public MutableStat getCrewLossMult();
	
	//public MutableStat getPhaseCloakUpkeepMult();
	public MutableStat getHardFluxDissipationFraction();
	
	public StatBonus getFuelMod();
	public StatBonus getFuelUseMod();
	public StatBonus getMinCrewMod();
	public StatBonus getMaxCrewMod();
	public StatBonus getCargoMod();
	public StatBonus getHangarSpaceMod();
	
	public StatBonus getMissileMaxSpeedBonus();
	public StatBonus getMissileAccelerationBonus();
	public StatBonus getMissileMaxTurnRateBonus();
	public StatBonus getMissileTurnAccelerationBonus();
	
	public MutableStat getProjectileSpeedMult();
	public MutableStat getVentRateMult();
	
	//public MutableStat getBaseSupplyUsePerDay();
	//public MutableStat getBaseRepairRatePercentPerDay();
	public MutableStat getBaseCRRecoveryRatePercentPerDay();
	
	public MutableStat getMaxBurnLevel();
	
	
	/**
	 * Only applicable for ships with flight decks. Modifies the amount of time it
	 * takes a flight deck to spawn a replacement fighter.
	 * @return
	 */
	public MutableStat getFighterRefitTimeMult();

	MutableStat getRepairRatePercentPerDay();

	
	//MutableStat getSupplyConsumptionAtMaxCRMult();

	MutableStat getSensorProfile();

	MutableStat getSensorStrength();

	
	
	DynamicStatsAPI getDynamic();

	MutableStat getSuppliesToRecover();

	MutableStat getSuppliesPerMonth();

	MutableStat getWeaponRangeThreshold();
	MutableStat getWeaponRangeMultPastThreshold();

	MutableStat getTimeMult();

	StatBonus getBeamPDWeaponRangeBonus();
	StatBonus getNonBeamPDWeaponRangeBonus();

	MutableStat getMinArmorFraction();

	MutableStat getMaxArmorDamageReduction();

	MutableStat getNumFighterBays();

	StatBonus getMissileHealthBonus();

	StatBonus getPhaseCloakCooldownBonus();
	StatBonus getSystemCooldownBonus();
	StatBonus getSystemRegenBonus();
	StatBonus getSystemUsesBonus();
	StatBonus getSystemRangeBonus();
	
	MutableStat getKineticArmorDamageTakenMult();
	MutableStat getDamageToFighters();
	MutableStat getDamageToMissiles();

	MutableStat getDamageToFrigates();
	MutableStat getDamageToDestroyers();
	MutableStat getDamageToCruisers();
	MutableStat getDamageToCapital();

	StatBonus getCriticalMalfunctionDamageMod();

	MutableStat getBreakProb();

	StatBonus getFighterWingRange();

	ShipVariantAPI getVariant();

	MutableStat getRecoilPerShotMultSmallWeaponsOnly();

	MutableStat getEnergyWeaponFluxBasedBonusDamageMagnitude();
	MutableStat getEnergyWeaponFluxBasedBonusDamageMinLevel();

	MutableStat getAllowZeroFluxAtAnyLevel();

	CombatListenerManagerAPI getListenerManager();
	void addListener(Object listener);
	void removeListener(Object listener);
	void removeListenerOfClass(Class<?> c);
	boolean hasListener(Object listener);
	boolean hasListenerOfClass(Class<?> c);
	<T> List<T> getListeners(Class<T> c);

	MutableStat getBallisticProjectileSpeedMult();

	MutableStat getEnergyProjectileSpeedMult();

	MutableStat getMissileAmmoRegenMult();
	MutableStat getEnergyAmmoRegenMult();
	MutableStat getBallisticAmmoRegenMult();

}





