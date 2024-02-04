package com.fs.starfarer.api.combat;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;

import org.json.JSONObject;

import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public interface ShipSystemSpecAPI {

	String getIconSpriteName();

	boolean isCanUseWhileRightClickSystemOn();

	void setCanUseWhileRightClickSystemOn(boolean canUseWhileRightClickSystemOn);

	float getRange(MutableShipStatsAPI stats);

	boolean isPhaseCloak();
	void setPhaseCloak(boolean isPhaseCloak);

	float getCooldown(MutableShipStatsAPI stats);
	float getRegen(MutableShipStatsAPI stats);
	int getMaxUses(MutableShipStatsAPI stats);

	boolean isRunScriptWhilePaused();
	boolean isRunScriptWhileIdle();

	boolean isBlockActionsWhileChargingDown();
	
	float getPhaseChargedownVulnerabilityFraction();
	void setPhaseChargedownVulnerabilityFraction(float phaseChargedownVulnerabilityFraction);

	float getCrPerUse();

	void setCrPerUse(float crPerUse);

	boolean isRenderCopyDuringTeleport();

	boolean isVulnerableChargeup();

	boolean isVulnerableChargedown();

	boolean isFadeActivationSoundOnChargedown();

	JSONObject getSpecJson();

	boolean isEngineActivateHiddenNozzles();

	float getEngineGlowMaxBlend();

	float getShipAlpha();

	float getFilterGain();

	float getFilterGainLF();

	float getFilterGainHF();
	String getImpactSound();

	float getThreatRange(MutableShipStatsAPI stats);

	float getThreatAmount();

	float getThreatArc();

	float getThreatAngle();

	float getEmpDamage();

	float getDamage();

	DamageType getDamageType();

	Color getEffectColor1();

	Color getEffectColor2();

	boolean isAllowFreeRoam();

	float getLaunchDelay();

	int getMaxDrones();

	float getLaunchSpeed();

	String getDroneVariant();

	float getJitterMinRange();

	Color getJitterUnderEffectColor();

	int getJitterUnderCopies();

	float getJitterUnderMinRange();

	float getJitterUnderRange();

	float getJitterUnderRangeRadiusFraction();

	float getJitterRangeRadiusFraction();

	float getRandomRange();

	int getJitterCopies();

	float getJitterRange();

	Color getJitterEffectColor();

	Color getWeaponGlowColor();

	String getLoopSound();

	String getDeactivateSound();

	boolean isAlwaysAccelerate();

	String getStatsScriptClassName();

	ShipSystemStatsScript getStatsScript();

	ShipSystemAIScript getAIScript();

	String getAIScriptClassName();

	Color getShieldRingColor();

	Color getShieldInnerColor();

	float getShieldThicknessMult();

	float getShieldFluctuationMult();

	boolean isClampTurnRateAfter();

	boolean isClampMaxSpeedAfter();

	void setIconSpriteName(String iconSpriteName);

	float getMinFractionToReload();

	EnumSet<WeaponType> getWeaponTypes();

	float getFlameoutOnImpactChance();

	boolean isTriggersExtraEngines();

	void setTriggersExtraEngines(boolean triggersEngines);

	Color getEngineGlowContrailColor();

	boolean isHardDissipationAllowed();

	void setHardDissipationAllowed(boolean allowHardDissipation);

	boolean isVentingAllowed();

	void setVentingAllowed(boolean ventingAllowed);

	boolean generatesHardFlux();

	void setGeneratesHardFlux(boolean generatesHardFlux);

	void setToggle(boolean toggle);

	void setDissipationAllowed(boolean dissipationAllowed);

	boolean isDissipationAllowed();

	Color getEngineGlowColor();

	float getEngineGlowLengthMult();

	float getEngineGlowWidthMult();

	float getEngineGlowGlowMult();

	void setRegen(float regen);

	float getIn();

	void setIn(float in);

	float getActive();

	void setActive(float active);

	float getOut();

	void setOut(float out);

	void setCooldown(float cooldown);

	boolean isToggle();

	boolean isFiringAllowed();

	void setFiringAllowed(boolean firingAllowed);

	String getUseSound();

	void setUseSound(String useSound);

	String getOutOfUsesSound();

	void setOutOfUsesSound(String outOfAmmoSound);

	String getId();

	void setId(String id);

	String getName();

	void setName(String name);

	String getWeaponId();

	void setWeaponId(String weaponId);

	float getFluxPerSecond();

	void setFluxPerSecond(float fluxPerSecond);

	float getFluxPerUse();

	void setFluxPerUse(float fluxPerUse);

	void setMaxUses(int maxUses);

	boolean isTurningAllowed();

	void setTurningAllowed(boolean turnAllowed);

	boolean isStrafeAllowed();

	void setStrafeAllowed(boolean strafeAllowed);

	boolean isShieldAllowed();

	void setShieldAllowed(boolean shieldAllowed);

	boolean isAccelerateAllowed();

	void setAccelerateAllowed(boolean accelerateAllowed);

	float getFluxPerSecondBaseRate();

	void setFluxPerSecondBaseRate(float fluxPerSecondBaseRate);

	float getFluxPerSecondBaseCap();

	void setFluxPerSecondBaseCap(float fluxPerSecondBaseCap);

	float getFluxPerUseBaseRate();

	void setFluxPerUseBaseRate(float fluxPerUseBaseRate);

	float getFluxPerUseBaseCap();

	void setFluxPerUseBaseCap(float fluxPerUseBaseCap);

	boolean isCanNotCauseOverload();

	void setCanNotCauseOverload(boolean canNotCauseOverload);

	boolean isRequiresZeroFluxBoost();

	void setRequiresZeroFluxBoost(boolean requiresZeroFluxBoost);

	void addTag(String tag);

	Set<String> getTags();

	boolean hasTag(String tag);

	boolean isAllowFlameoutOnImpactWithFriendly();

	void setAllowFlameoutOnImpactWithFriendly(boolean allowFlameoutOnImpactWithFriendly);

	boolean isReloadBaseAmmoAmountOnly();
	void setReloadBaseAmmoAmountOnly(boolean reloadBaseAmmoAmountOnly);


}
