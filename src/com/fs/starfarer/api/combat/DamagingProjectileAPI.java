package com.fs.starfarer.api.combat;

import com.fs.starfarer.api.loading.ProjectileSpawnType;

public interface DamagingProjectileAPI extends CombatEntityAPI {
	
	DamageType getDamageType();
	float getDamageAmount();
	
	/**
	 * Does not include damage reduction from projectile fading out or having done damage
	 * @return
	 */
	float getBaseDamageAmount();
	float getEmpAmount();
	
	
	void setDamageAmount(float damage);
	
	
	/**
	 * @return Weapon that fired this projectile. Can be null (for example, if spawned without one via the API).
	 */
	WeaponAPI getWeapon();

	/**
	 * Whether the projectile already did its damage and is now fading out.
	 * @return
	 */
	boolean didDamage();
	
	/**
	 * @return What the damage was dealt to, once didDamage() returns true. Can be null.
	 */
	CombatEntityAPI getDamageTarget();
	
	String getProjectileSpecId();
	
	
	/**
	 * Generally a ShipAPI for the ship that ultimately fired this weapon. Can be null.
	 * 
	 * Projectiles can't hit their source, except for fizzled-out missiles.
	 * 
	 * @return
	 */
	ShipAPI getSource();
	void setSource(ShipAPI source);
	
	/**
	 * @return whether the projectile has started fading out due to exceeding its maximum range.
	 */
	boolean isFading();
	
	ProjectileSpawnType getSpawnType();
	
	
	/**
	 * Time the projectile has been alive.
	 * @return
	 */
	float getElapsed();
	
	
	/**
	 * Do not call for explosions.
	 * @return
	 */
	DamageAPI getDamage();
	
	boolean isFromMissile();
	
	/**
	 * Whether it was spawned by a missile (or by other non-directly-by-weapon means).
	 * Needed for incoming damage evaluation AI to function properly in these cases.
	 * @param fromMissile
	 */
	void setFromMissile(boolean fromMissile);
	
	
	/**
	 * Only supported by damaging explosions, not other types of projectiles.
	 * @param c
	 */
	void removeDamagedAlready(CombatEntityAPI c);
	
	/**
	 * Only supported by damaging explosions, not other types of projectiles.
	 * @param c
	 */
	void addDamagedAlready(CombatEntityAPI c);
	float getMoveSpeed();
	
}
