package com.fs.starfarer.api.loading;

import java.awt.Color;

import org.json.JSONObject;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public interface ProjectileSpecAPI {
	String getOnHitClassName();
	void setOnHitClassName(String effectClassName);
	OnHitEffectPlugin getOnHitEffect();
	String getId();
	float getGlowRadius();
	void setGlowRadius(float glowRadius);
	Color getGlowColor();
	void setGlowColor(Color glowColor);
	float getHitGlowRadius();
	void setHitGlowRadius(float hitGlowSize);
	String getFringeTex();
	void setFringeTex(String fringeTex);
	String getCoreTex();
	void setCoreTex(String coreTex);
	Color getFringeColor();
	void setFringeColor(Color fringeColor);
	Color getCoreColor();
	void setCoreColor(Color coreColor);
	boolean isDarkCore();
	void setDarkCore(boolean darkCore);
	String getBulletSpriteName();
	void setBulletSpriteName(String bulletSpriteName);
	float getPixelsPerTexel();
	void setPixelsPerTexel(float pixelsPerTexel);
	float getTextureScrollSpeed();
	void setTextureScrollSpeed(float textureScrollSpeed);
	float getLength();
	void setLength(float length);
	float getMaxRange();
	void setMaxRange(float maxRange);
	
	/**
	 * Can pass in shipStats == null to get the base value.
	 * @param shipStats
	 * @param weapon
	 * @return
	 */
	float getMoveSpeed(MutableShipStatsAPI shipStats, WeaponAPI weapon);
	void setMoveSpeed(float moveSpeed);
	float getFadeTime();
	void setFadeTime(float fadeTime);
	float getImpactMass();
	void setImpactStrength(float impactMass);
	CollisionClass getCollisionClass();
	void setCollisionClass(CollisionClass collisionClass);
	CollisionClass getCollisionClassIfByFighter();
	void setCollisionClassIfByFighter(CollisionClass collisionClassIfByFighter);
	DamageAPI getDamage();
	float getWidth();
	void setWidth(float width);
	ProjectileSpawnType getSpawnType();
	void setSpawnType(ProjectileSpawnType style);
	float getMaxHealth();
	void setMaxHealth(float maxHealth);
	boolean isPassThroughMissiles();
	void setPassThroughMissiles(boolean passThroughMissiles);
	JSONObject getBehaviorJSON();
	int getDarkCoreIter();
	void setDarkCoreIter(int darkCoreIter);
	int getDarkFringeIter();
	void setDarkFringeIter(int darkFringeIter);
	float getCoreWidthMult();
	void setCoreWidthMult(float coreWidthMult);
	void setOnFireClassName(String onFireClassName);
	String getOnFireClassName();
	OnFireEffectPlugin getOnFireEffect();
	void setNoNonShieldImpactSounds(boolean noNonShieldImpactSounds);
	void setNoImpactSounds(boolean noImpactSounds);
	boolean isNoShieldImpactSounds();
	void setNoShieldImpactSounds(boolean noShieldImpactSounds);
	boolean isNoNonShieldImpactSounds();
	boolean isNoImpactSounds();
	boolean isPassThroughFighters();
	void setPassThroughFighters(boolean passThroughFighters);
	boolean isPassThroughFightersOnlyWhenDestroyed();
	void setPassThroughFightersOnlyWhenDestroyed(boolean passThroughFightersOnlyWhenDestroyed);
	boolean isApplyOnHitEffectWhenPassThrough();
	void setApplyOnHitEffectWhenPassThrough(boolean applyOnHitEffectWhenPassThrough);
	ShotBehaviorSpecAPI getBehaviorSpec();

}
