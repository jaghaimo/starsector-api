package com.fs.starfarer.api.combat;

public interface ProximityExplosionEffect {
	void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile);
}
