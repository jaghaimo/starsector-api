package com.fs.starfarer.api.loading;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.DerivedWeaponStatsAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;

public interface WeaponSpecAPI {
	float getOrdnancePointCost(MutableCharacterStatsAPI stats);
	EnumSet<WeaponAPI.AIHints> getAIHints();
	
	WeaponType getType();
	float getAmmoPerSecond();
	
	int getTier();
	float getBaseValue();
	boolean usesAmmo();
	int getMaxAmmo();
	String getWeaponId();
	WeaponSize getSize();
	String getWeaponName();
	
	int getBurstSize();
	Set<String> getTags();
	void addTag(String tag);
	boolean hasTag(String tag);
	float getRarity();
	void setRarity(float rarity);
	float getOrdnancePointCost(MutableCharacterStatsAPI stats, MutableShipStatsAPI shipStats);
	DerivedWeaponStatsAPI getDerivedStats();
	
	List<Vector2f> getHardpointFireOffsets();
	List<Float> getHardpointAngleOffsets();
	List<Vector2f> getTurretFireOffsets();
	List<Float> getTurretAngleOffsets();
	List<Vector2f> getHiddenFireOffsets();
	List<Float> getHiddenAngleOffsets();
	
	String getHardpointSpriteName();
	String getTurretSpriteName();
	String getHardpointUnderSpriteName();
	String getTurretUnderSpriteName();
	String getManufacturer();
	void setManufacturer(String manufacturer);
	String getAutofitCategory();
	List<String> getAutofitCategoriesInPriorityOrder();
	String getWeaponGroupTag();
	void setWeaponGroupTag(String weaponGroupTag);
	boolean isBeam();
	String getPrimaryRoleStr();
	void setPrimaryRoleStr(String primaryRoleStr);
	String getSpeedStr();
	void setSpeedStr(String speedStr);
	String getTrackingStr();
	void setTrackingStr(String trackingStr);
	String getTurnRateStr();
	void setTurnRateStr(String turnRateStr);
	String getAccuracyStr();
	void setAccuracyStr(String accuracyStr);
	String getCustomPrimary();
	void setCustomPrimary(String customPrimary);
	String getCustomPrimaryHL();
	void setCustomPrimaryHL(String customPrimaryHL);
	String getCustomAncillary();
	void setCustomAncillary(String customAncillary);
	String getCustomAncillaryHL();
	void setCustomAncillaryHL(String customAncillaryHL);
	boolean isNoDPSInTooltip();
	void setNoDPSInTooltip(boolean noDPSInTooltip);
	Color getGlowColor();
	boolean isInterruptibleBurst();
	boolean isNoImpactSounds();
	void setNoImpactSounds(boolean noImpactSounds);
	DamageType getDamageType();
	
	boolean isRenderAboveAllWeapons();
	void setRenderAboveAllWeapons(boolean renderAboveAllWeapons);
	boolean isNoShieldImpactSounds();
	void setNoShieldImpactSounds(boolean noShieldImpactSounds);
	boolean isNoNonShieldImpactSounds();
	void setNoNonShieldImpactSounds(boolean noNonShieldImpactSounds);
	
	float getMinSpread();
	float getMaxSpread();
	float getSpreadDecayRate();
	float getSpreadBuildup();
	void setMinSpread(float minSpread);
	void setMaxSpread(float maxSpread);
	void setSpreadDecayRate(float spreadDecayRate);
	void setSpreadBuildup(float spreadBuildup);
	
	/**
	 * For beam weapons only.
	 * @return
	 */
	float getBurstDuration();
	float getAutofireAccBonus();
	void setAutofireAccBonus(float autofireAccBonus);
	Object getProjectileSpec();
	float getBeamChargeupTime();
	float getBeamChargedownTime();
	boolean isUnaffectedByProjectileSpeedBonuses();
	void setUnaffectedByProjectileSpeedBonuses(boolean unaffectedByProjectileSpeedBonuses);
	float getChargeTime();
	WeaponType getMountType();
	void setMountType(WeaponType mountType);
	float getExtraArcForAI();
	void setExtraArcForAI(float extraArcForAI);
	void setWeaponName(String weaponName);
	float getMaxRange();
	void setMaxRange(float maxRange);
	void setOrdnancePointCost(float armamentCapacity);
	boolean isShowDamageWhenDecorative();
	boolean isBurstBeam();
	boolean isStopPreviousFireSound();
	void setStopPreviousFireSound(boolean stopPreviousFireSound);
	boolean isPlayFullFireSoundOne();
	void setPlayFullFireSoundOne(boolean playFullFireSoundOne);
	void setBeamSpeed(float beamSpeed);
	void setMaxAmmo(int maxAmmo);
	void setAmmoPerSecond(float ammoPerSecond);
	float getReloadSize();
	void setReloadSize(float reloadSize);
	void setProjectileSpeed(float projectileSpeed);
}
