package com.fs.starfarer.api.loading;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public interface ProjectileWeaponSpecAPI extends WeaponSpecAPI {

	float getEnergyPerShot();
	void setEnergyPerShot(float energyPerShot);
	float getRefireDelay();
	void setRefireDelay(float refireDelay);
	int getBurstSize();
	void setBurstSize(int burstSize);
	boolean isInterruptibleBurst();
	void setInterruptibleBurst(boolean interruptibleBurst);
	float getBurstDelay();
	void setBurstDelay(float burstDelay);
	float getMinSpread();
	void setMinSpread(float minSpread);
	float getMaxSpread();
	void setMaxSpread(float maxSpread);
	float getSpreadDecayRate();
	void setSpreadDecayRate(float spreadDecayRate);
	float getSpreadBuildup();
	void setSpreadBuildup(float spreadBuildup);
	boolean isAutoCharge();
	void setAutoCharge(boolean autoCharge);
	float getEnergyPerSecond();
	void setEnergyPerSecond(float energyPerSecond);
	boolean isRequiresFullCharge();
	void setRequiresFullCharge(boolean requiresFullCharge);
	float getProjectileSpeed(MutableShipStatsAPI shipStats, WeaponAPI weapon);
	void setProjectileSpeed(float projectileSpeed);
	void setSeparateRecoilForLinkedBarrels(boolean individualLinkedRecoil);
	boolean isSeparateRecoilForLinkedBarrels();
	Object getProjectileSpec();
	float getChargeTime();
	void setChargeTime(float chargeTime);
	String getHardpointGunSpriteName();
	void setHardpointGunSpriteName(String hardpointGunSpriteName);
	String getTurretGunSpriteName();
	void setTurretGunSpriteName(String turretGunSpriteName);
	String getHardpointGlowSpriteName();
	void setTurretGlowSpriteName(String turretGlowSpriteName);
	String getTurretGlowSpriteName();
	void setHardpointGlowSpriteName(String hardpointGlowSpriteName);
	float getVisualRecoil();
	void setVisualRecoil(float visualRecoil);
	String getAccuracyDisplayName();

}
