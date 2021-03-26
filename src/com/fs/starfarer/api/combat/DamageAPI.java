package com.fs.starfarer.api.combat;


public interface DamageAPI {

	/**
	 * Emp damage.
	 * @param amount seconds, used when damage is dps.
	 * @return
	 */
	float computeFluxDealt(float amount);

	/**
	 * @param amount seconds, used when damage is dps.
	 * @return
	 */
	float computeDamageDealt(float amount);

	boolean isMissile();
	void setMissile(boolean isMissile);
	void setStats(MutableShipStatsAPI stats);
	MutableShipStatsAPI getStats();
	float getDamage();
	void setDamage(float amount);
	boolean isDps();
	float getMultiplier();
	void setMultiplier(float multiplier);
	DamageType getType();
	void setType(DamageType type);
	float getFluxComponent();
	void setFluxComponent(float fluxComponent);

	boolean isSoftFlux();
	/**
	 * Only useful for making non-beam weapons deal hard flux damage. For making
	 * beam weapons deal hard flux, use setForceHardFlux(true).
	 * @param isSoftFlux
	 */
	void setSoftFlux(boolean isSoftFlux);

	float getDpsDuration();
	void setDpsDuration(float dpsDuration);

	DamageAPI clone();

	MutableStat getModifier();

	boolean isForceHardFlux();
	/**
	 * Useful to make beam weapons deal hard flux.
	 * @param forceHardFlux
	 */
	void setForceHardFlux(boolean forceHardFlux);

	float getBaseDamage();
}
