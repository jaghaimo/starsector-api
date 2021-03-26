package com.fs.starfarer.api.impl.combat;

import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;

public class RiftBeamMineExplosion implements ProximityExplosionEffect {
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		NEParams p = RiftCascadeMineExplosion.createStandardRiftParams("riftbeam_minelayer", 10f);
		//p.hitGlowSizeMult = 0.5f;
		p.thickness = 50f;
		RiftCascadeMineExplosion.spawnStandardRift(explosion, p);
	}
}



