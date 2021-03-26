package com.fs.starfarer.api.impl.combat;

import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;

public class RiftLanceMineExplosion implements ProximityExplosionEffect {
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		NEParams p = RiftCascadeMineExplosion.createStandardRiftParams("nslance_minelayer", 20f);
		RiftCascadeMineExplosion.spawnStandardRift(explosion, p);
	}
}



