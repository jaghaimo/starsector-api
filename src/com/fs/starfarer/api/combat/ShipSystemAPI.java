package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

public interface ShipSystemAPI {
	
	public static enum SystemState {
		IDLE,
		IN,
		ACTIVE,
		OUT,
		COOLDOWN,
	}

	String getId();
	
	float getCooldownRemaining();
	boolean isOutOfAmmo();
	/**
	 * @return true if the system is charging up, down, or is on.
	 */
	boolean isActive();
	
	boolean isCoolingDown();
	
	int getAmmo();
	
	float getFluxPerUse();
	float getFluxPerSecond();
	
	String getDisplayName();
	
	/**
	 * @return true if the system is charging up or is on.
	 */
	boolean isOn();

	boolean isChargeup();
	boolean isChargedown();
	boolean isStateActive();

	int getMaxAmmo();

	void setAmmo(int ammo);

	float getEffectLevel();
	float getCooldown();
	void setFluxPerUse(float fluxPerUse);
	void setFluxPerSecond(float fluxPerSecond);

	SystemState getState();
	
	float getChargeUpDur();
	float getChargeDownDur();
	float getChargeActiveDur();

	ShipSystemSpecAPI getSpecAPI();

	void deactivate();

	void setCooldownRemaining(float remaining);
	void setCooldown(float cooldown);

	Vector2f getTargetLoc();

	void forceState(SystemState state, float progress);

	float getAmmoPerSecond();

	float getAmmoReloadProgress();
	void setAmmoReloadProgress(float progress);
}
