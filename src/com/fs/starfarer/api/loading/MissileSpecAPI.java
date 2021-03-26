package com.fs.starfarer.api.loading;

import java.awt.Color;

import org.json.JSONObject;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;

public interface MissileSpecAPI {

	String getOnHitClassName();
	void setOnHitClassName(String effectClassName);
	OnHitEffectPlugin getOnHitEffect();
	float getArmingTime();
	void setArmingTime(float armingTime);
	float getFlameoutTime();
	void setFlameoutTime(float flameoutTime);
	float getNoEngineGlowTime();
	void setNoEngineGlowTime(float noEngineGlowTime);
	float getFadeTime();
	void setFadeTime(float fadeTime);
	CollisionClass getAfterFlameoutCollisionClass();
	void setAfterFlameoutCollisionClass(CollisionClass afterFlameoutCollisionClass);
	Color getExplosionColor();
	float getGlowRadius();
	void setGlowRadius(float glowRadius);
	void setExplosionColor(Color explosionColor);
	float getExplosionRadius();
	void setExplosionRadius(float explosionRadius);
	Color getGlowColor();
	void setGlowColor(Color glowColor);
	String getGlowSpriteName();
	void setGlowSpriteName(String glowSpriteName);
	void setMaxFlightTime(float maxFlightTime);
	float getMaxFlightTime();
	ShipHullSpecAPI getHullSpec();
	DamageAPI getDamage();
	float getLaunchSpeed();
	void setLaunchSpeed(float launchSpeed);
	float getImpactStrength();
	void setImpactStrength(float impactStrength);
	DamagingExplosionSpec getExplosionSpec();
	void setExplosionSpec(DamagingExplosionSpec explosionSpec);
	String getSpeedDisplayName();
	String getManeuverabilityDisplayName();
	JSONObject getBehaviorJSON();
	String getOnFireClassName();
	void setOnFireClassName(String onFireClassName);
	OnFireEffectPlugin getOnFireEffect();
	float getDudProbabilityOnFlameout();
	void setDudProbabilityOnFlameout(float dudProbabilityOnFlameout);
	boolean isUseHitGlowWhenDestroyed();
	void setUseHitGlowWhenDestroyed(boolean useHitGlowWhenDestroyed);
	boolean isFizzleOnReachingWeaponRange();
	void setFizzleOnReachingWeaponRange(boolean fizzleOnReachingWeaponRange);
	boolean isUseHitGlowWhenDealingDamage();
	void setUseHitGlowWhenDealingDamage(boolean useHitGlowWhenDealingDamage);
	boolean isNoCollisionWhileFading();
	void setNoCollisionWhileFading(boolean noCollisionWhileFading);
	boolean isReduceDamageWhileFading();
	void setReduceDamageWhileFading(boolean reduceDamageWhileFading);
	boolean isAlwaysAccelerate();
	void setAlwaysAccelerate(boolean alwaysAccelerate);
	boolean isUseProjectileRangeCalculation();
	void setUseProjectileRangeCalculation(boolean useProjectileRangeCalculation);
	boolean isRenderTargetIndicator();
	void setRenderTargetIndicator(boolean renderTargetIndicator);
	boolean isNoDebrisWhenDestroyed();
	void setNoDebrisWhenDestroyed(boolean noDebrisWhenDestroyed);

}
