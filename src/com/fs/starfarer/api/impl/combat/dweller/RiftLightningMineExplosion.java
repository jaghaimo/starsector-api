package com.fs.starfarer.api.impl.combat.dweller;

import java.awt.Color;

import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;

public class RiftLightningMineExplosion implements ProximityExplosionEffect {
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		//System.out.println("EXPLOSION");
		Color color = RiftLightningEffect.RIFT_LIGHTNING_COLOR;
		color = new Color(255,75,75,255);
		NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(
									color, 20f);
		p.fadeOut = 1f;
		p.hitGlowSizeMult = 0.6f;
		//p.invertForDarkening = NSProjEffect.STANDARD_RIFT_COLOR;
		p.thickness = 50f;
		
		
//		p.hitGlowSizeMult = 0.5f;
//		p.thickness = 25f;
//		p.fadeOut = 0.25f;
		
		RiftCascadeMineExplosion.spawnStandardRift(explosion, p);
		
	}
}



