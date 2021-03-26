package com.fs.starfarer.api.impl.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.loading.MissileSpecAPI;

/**
 * IMPORTANT: will be multiple instances of this, one for the the OnFire (per weapon) and one for the OnHit (per torpedo) effects.
 * 
 * (Well, no data members, so not *that* important.)
 */
public class RiftTorpedoEffect implements OnFireEffectPlugin, OnHitEffectPlugin {

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		Color color = RiftCascadeEffect.STANDARD_RIFT_COLOR;
		Object o = projectile.getWeapon().getSpec().getProjectileSpec();
		if (o instanceof MissileSpecAPI) {
			MissileSpecAPI spec = (MissileSpecAPI) o;
			color = spec.getExplosionColor();
		}
		
		NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(color, 40f);
		p.fadeOut = 2f;
		p.hitGlowSizeMult = 1f;
		// want a red rift, but still blue for subtracting from the red clouds
		// or not - actually looks better with the red being inverted and subtracted
		// despite this not matching the trail
		//p.invertForDarkening = NSProjEffect.STANDARD_RIFT_COLOR;
		RiftCascadeMineExplosion.spawnStandardRift(projectile, p);
		
		Vector2f vel = new Vector2f();
		if (target != null) vel.set(target.getVelocity());
		Global.getSoundPlayer().playSound("rifttorpedo_explosion", 1f, 1f, point, vel);
	}
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		RiftTrailEffect trail = new RiftTrailEffect((MissileAPI) projectile, "rifttorpedo_loop");
		((MissileAPI) projectile).setEmpResistance(1000);
		((MissileAPI) projectile).setEccmChanceOverride(1f);
		Global.getCombatEngine().addPlugin(trail);
	}
}




