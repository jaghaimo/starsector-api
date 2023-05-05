package com.fs.starfarer.api.combat;

public interface AmmoTrackerAPI {

	void setAmmoPerSecond(float ammoPerSecond);
	float getReloadProgress();
	void setAmmo(int ammo);
	boolean usesAmmo();
	void addOneAmmo();
	boolean deductOneAmmo();
	int getAmmo();
	float getAmmoPerSecond();
	int getMaxAmmo();
	void resetAmmo();
	void setMaxAmmo(int maxAmmo);
	float getReloadSize();
	void setReloadSize(float reloadSize);
	void setReloadProgress(float progress);

}
