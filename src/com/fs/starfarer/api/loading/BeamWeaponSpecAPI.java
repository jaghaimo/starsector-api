package com.fs.starfarer.api.loading;

import java.awt.Color;
import java.util.EnumSet;

import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;

public interface BeamWeaponSpecAPI extends WeaponSpecAPI {

	float getDamagePerSecond();
	void setDamagePerSecond(float damagePerSecond);
	float getEnergyPerSecond();
	void setEnergyPerSecond(float energyPerSecond);
	String getEffectClassName();
	void setEffectClassName(String effectClassName);
	BeamEffectPlugin getBeamEffect();
	boolean isConvergeOnPoint();
	void setConvergeOnPoint(boolean convergeOnPoint);
	boolean isSkipIdleFrameIfZeroBurstDelay();
	void setSkipIdleFrameIfZeroBurstDelay(boolean skipIdleFrameIfZeroBurstDelay);
	float getHitGlowRadius();
	void setHitGlowRadius(float hitGlowRadius);
	float getEMPPerSecond();
	void setEMPPerSecond(float empPerSecond);
	CollisionClass getCollisionClass();
	void setCollisionClass(CollisionClass collisionClass);
	CollisionClass getCollisionClassIfByFighter();
	void setCollisionClassIfByFighter(CollisionClass collisionClassIfByFighter);
	String getFringeTex();
	float getBurstDuration();
	void setBurstDuration(float burstDuration);
	float getBurstCooldown();
	void setBurstCooldown(float burstCooldown);
	boolean isBurstBeam();
	void setBurstBeam(boolean isBurst);
	void setFringeTex(String fringeTex);
	String getCoreTex();
	void setCoreTex(String coreTex);
	boolean isDarkCore();
	void setDarkCore(boolean darkCore);
	boolean isBeamFireOnlyOnFullCharge();
	void setBeamFireOnlyOnFullCharge(boolean beamFireOnlyOnFullCharge);
	boolean isUseGlowColorForHitGlow();
	void setUseGlowColorForHitGlow(boolean useGlowColorForHitGlow);
	String getHardpointGlowSpriteName();
	void setHardpointGlowSpriteName(String hardpointGlowSpriteName);
	String getTurretGlowSpriteName();
	void setTurretGlowSpriteName(String glowSpriteName);
	Color getGlowColor();
	void setGlowColor(Color glowColor);
	float getChargeupTime();
	void setChargeupTime(float chargeupTime);
	float getChargedownTime();
	void setChargedownTime(float chargedownTime);
	float getBeamSpeed();
	void setBeamSpeed(float beamSpeed);
	float getFluxPerSecond();
	void setFluxPerSecond(float fluxPerSecond);
	Color getFringeColor();
	void setFringeColor(Color color);
	Color getCoreColor();
	void setCoreColor(Color coreColor);
	float getWidth();
	void setWidth(float width);
	float getTextureScrollSpeed();
	void setTextureScrollSpeed(float textureScrollSpeed);
	float getFringeScrollSpeedMult();
	void setFringeScrollSpeedMult(float fringeScrollSpeedMult);
	float getPixelsPerTexel();
	void setPixelsPerTexel(float pixelsPerTexel);
	float getImpactMass();
	void setImpactMass(float impactMass);
	EnumSet<CollisionClass> getPierceSet();
	void addPierced(CollisionClass pierced);
	void setStandardPiercing();
	int getDarkFringeIter();
	void setDarkFringeIter(int darkFringeIter);
	int getDarkCoreIter();
	void setDarkCoreIter(int darkCoreIter);
	float getCoreWidthMult();
	void setCoreWidthMult(float coreWidthMult);
	float getHitGlowBrightenDuration();
	void setHitGlowBrightenDuration(float hitGlowBrightenDuration);


}
