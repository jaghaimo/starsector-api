package com.fs.starfarer.api.impl.combat.dweller;

import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.impl.combat.RiftTrailEffect;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.DCPPlugin;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.WobblyPart;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ShroudedVortexShipCreator extends BaseDwellerShipCreator implements DCPPlugin {

	public static float EXPLOSION_DAMAGE = 2000f;
	public static DamageType EXPLOSION_DAMAGE_TYPE = DamageType.ENERGY;
	
	public static String ID_BODY_ONE = "id_body_one";
	public static String ID_BODY_TWO = "id_body_two";
	
	public static String TAG_MIRRORED_VORTEX = "tag_mirrored_vortex";
	
	@Override
	protected DwellerCombatPlugin createPlugin(ShipAPI ship) {
		//DwellerCombatPlugin plugin = super.createPlugin(ship);
		
		DwellerCombatPlugin plugin = DwellerCombatPlugin.getDwellerPluginFor(ship);
		if (plugin == null) {
			plugin = new DwellerCombatPlugin(ship) {
				protected boolean exploded = false;
				@Override
				public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
					if (damageAmount >= ship.getHitpoints() && !exploded) {
						DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
						if (shroud != null) {
							for (SwarmMember p : shroud.getMembers()) {
								Vector2f.sub(p.vel, ship.getVelocity(), p.vel);
							}
						}
						
						DamagingProjectileAPI explosion = Global.getCombatEngine().spawnDamagingExplosion(
								createExplosionSpec(ship), ship, ship.getLocation());
						exploded = true;
						//ship.getVelocity().set(0, 0);
						ship.getVelocity().scale(0.1f);
					}
					
					return super.notifyAboutToTakeHullDamage(param, ship, point, damageAmount);
				}
				
			};
		}
		
		//plugin.setPlugin(this);
		
		List<DwellerShipPart> parts = plugin.getParts();
		parts.clear();
		
		float scale = 1f;
		scale = 0.5f;
		
		float spinMult = 1f;
		
		boolean mirror = (float) Math.random() > 0.5f;
		//mirror = true;
		if (mirror) {
			ship.addTag(TAG_MIRRORED_VORTEX);
		}
		
		WobblyPart part = new WobblyPart("shrouded_vortex_base", 2f * scale, 1f, new Vector2f(0, 0), 0f);
		part.setId(ID_BODY_ONE);
		part.renderer.setMirror(mirror);
		//part.setSpin(360f * spinMult, 360f * 2f * spinMult, 360f * spinMult);
		parts.add(part);
		
		part = new WobblyPart("shrouded_vortex_base2", 3f * scale, 3, 3, 1f, new Vector2f(0, 0), 0f);
		//part.setSpin(270f * spinMult, 360f * spinMult, 270f * spinMult);
		part.setId(ID_BODY_TWO);
		part.renderer.setMirror(mirror);
		part.alphaMult = 0.5f;
		parts.add(part);
		
//		Color glow = DwellerCombatPlugin.STANDARD_PART_GLOW_COLOR;
//		
//		part = new WobblyPart("clusterA", 1f * scale, 3, 3, 2f, new Vector2f(70f * scale, 0), 0f);
//		part.color = glow;
//		part.additiveBlend = true;
//		//part.setWeaponActivated();
//		//parts.add(part);
//		
//		part = new WobblyPart("clusterB", 1f * scale, 3, 3, 2f, new Vector2f(30f * scale, 0), 0f);
//		part.color = glow;
//		part.additiveBlend = true;
//		//part.setFluxActivated();
////		part.setWeaponActivated();
////		part.setShieldActivated();
////		part.setSystemActivated();
//		parts.add(part);
//		
//		part = new WobblyPart("coronet_stalks", 0.5f * scale, 3, 3, 2f, new Vector2f(100f * scale, 0), 0f);
//		part.color = glow;
//		part.additiveBlend = true;
		//part.setShieldActivated();
		//parts.add(part);
		
		return plugin;
	}

	@Override
	protected void modifyBaselineShroudParams(ShipAPI ship, DwellerShroudParams params) {
		params.maxOffset = 50f;
		params.initialMembers = 30;
		params.baseMembersToMaintain = params.initialMembers;
		
		params.baseSpriteSize *= 0.65f;
		//params.baseSpriteSize *= 0.75f;
		
		params.flashFrequency /= 3f;
		//params.numToFlash = 1;
		//params.flashFrequency = 17f;
		//params.numToFlash = 2;
		
		params.negativeParticleAreaMult = 1.25f;
		params.negativeParticleDurMult *= 0.5f;
		params.negativeParticleSizeMult *= 1.4f;
		//params.negativeParticleGenRate *= 1f;
		
//		params.negativeParticleGenRate = 0f;
		params.negativeParticleGenRate *= 0.5f;
		
		params.overloadGlowSizeMult *= 0.5f;
		
//		params.overloadArcThickness *= 0.5f;
//		params.overloadArcCoreThickness *= 0.5f;
		
//		params.maxSpeed += 500f;
//		params.springStretchMult = 30f;
//		params.baseFriction *= 10f;
//		params.frictionRange *= 1f;
	}
	
	

	@Override
	public void initInCombat(ShipAPI ship) {
		super.initInCombat(ship);
		
		RiftTrailEffect trail = new RiftTrailEffect(ship, null) {
			@Override
			protected Color getUndercolor() {
				//return super.getUndercolor();
				return DwellerShroud.SHROUD_COLOR;
				//return new Color(125, 0, 25, 255);
			}
			@Override
			protected Color getDarkeningColor() {
				//return super.getDarkeningColor();
				return RiftLanceEffect.getColorForDarkening(getUndercolor());
			}
			@Override
			protected float getBaseParticleSize() {
				return ship.getCollisionRadius() * 1f;
			}
			@Override
			protected float getBaseParticleDuration() {
				return 1.5f;
			}
			
		};
		Global.getCombatEngine().addPlugin(trail);
	}

	@Override
	public void advance(DwellerCombatPlugin plugin, float amount) {
//		CombatEntityAPI attachedTo = plugin.getAttachedTo();
//		if (attachedTo instanceof ShipAPI) {
//			ShipAPI ship = (ShipAPI) attachedTo;
//			float hullLevel = ship.getHullLevel();
//			float mult = 1f - hullLevel;
//			if (mult < 0f) mult = 0f;
//			if (mult > 1f) mult = 1f;
//			
//			mult *= 3f;
//			
//			DwellerShipPart part = plugin.getPart(ID_BODY_ONE);
//			if (part != null) {
//				((WobblyPart)part).getSpin().setValueMult(mult);
//			}
//			part = plugin.getPart(ID_BODY_TWO);
//			if (part != null) {
//				((WobblyPart)part).getSpin().setValueMult(mult);
//			}
			
//			CombatEngineAPI engine = Global.getCombatEngine();
//			engine.applyDamage(ship, ship.getLocation(), 100f, DamageType.ENERGY, 0f, true, false, ship, false);
//			//ship.setCollisionClass(CollisionClass.SHIP);
//			//ship.getAIFlags().setFlag(AIFlags.DO_NOT_BACK_OFF, 1f);
//		}
	}
	
	public DamagingExplosionSpec createExplosionSpec(ShipAPI ship) {
		float damage = EXPLOSION_DAMAGE;
		
		float radius = DamagingExplosionSpec.getShipExplosionRadius(ship);
		float coreRadius = ship.getCollisionRadius();
		
		DamagingExplosionSpec spec = new DamagingExplosionSpec(
				0.1f, // duration
				radius, // radius
				coreRadius, // coreRadius
				damage, // maxDamage
				0f, // minDamage
				CollisionClass.PROJECTILE_NO_FF, // collisionClass - no friendly fire damage!
				CollisionClass.PROJECTILE_NO_FF, // collisionClassByFighter
				3f, // particleSizeMin
				3f, // particleSizeRange
				0.5f, // particleDuration
				0, // particleCount
				new Color(255,255,255,0), // particleColor
				new Color(255,100,100,0)  // explosionColor
				);

		spec.setDamageType(DamageType.ENERGY);
		spec.setUseDetailedExplosion(false);
		spec.setSoundSetId(null);
		//spec.setSoundVolume(1f);
		return spec;		
	}
}









