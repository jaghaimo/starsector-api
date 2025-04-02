package com.fs.starfarer.api.impl.combat.dweller;

import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.DCPPlugin;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;

public class ShroudedEjectaShipCreator extends BaseDwellerShipCreator implements DCPPlugin {

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
			plugin = new DwellerCombatPlugin(ship);
		}
		
		//plugin.setPlugin(this);
		
//		List<DwellerShipPart> parts = plugin.getParts();
//		parts.clear();
//		
//		float scale = 1f;
//		//scale = 0.5f;
//		
//		float spinMult = 1f;
//		
//		boolean mirror = (float) Math.random() > 0.5f;
//		//mirror = true;
//		if (mirror) {
//			ship.addTag(TAG_MIRRORED_VORTEX);
//		}
//		
//		WobblyPart part = new WobblyPart("shrouded_vortex_base", 2f * scale, 1f, new Vector2f(0, 0), 0f);
//		part.setId(ID_BODY_ONE);
//		part.renderer.setMirror(mirror);
//		//part.setSpin(360f * spinMult, 360f * 2f * spinMult, 360f * spinMult);
//		parts.add(part);
//		
//		part = new WobblyPart("shrouded_vortex_base2", 3f * scale, 3, 3, 1f, new Vector2f(0, 0), 0f);
//		//part.setSpin(270f * spinMult, 360f * spinMult, 270f * spinMult);
//		part.setId(ID_BODY_TWO);
//		part.renderer.setMirror(mirror);
//		part.alphaMult = 0.5f;
//		parts.add(part);
//		
////		Color glow = DwellerCombatPlugin.STANDARD_PART_GLOW_COLOR;
////		
////		part = new WobblyPart("clusterA", 1f * scale, 3, 3, 2f, new Vector2f(70f * scale, 0), 0f);
////		part.color = glow;
////		part.additiveBlend = true;
////		//part.setWeaponActivated();
////		//parts.add(part);
////		
////		part = new WobblyPart("clusterB", 1f * scale, 3, 3, 2f, new Vector2f(30f * scale, 0), 0f);
////		part.color = glow;
////		part.additiveBlend = true;
////		//part.setFluxActivated();
//////		part.setWeaponActivated();
//////		part.setShieldActivated();
//////		part.setSystemActivated();
////		parts.add(part);
////		
////		part = new WobblyPart("coronet_stalks", 0.5f * scale, 3, 3, 2f, new Vector2f(100f * scale, 0), 0f);
////		part.color = glow;
////		part.additiveBlend = true;
//		//part.setShieldActivated();
//		//parts.add(part);
		
		return plugin;
	}

	@Override
	protected void modifyBaselineShroudParams(ShipAPI ship, DwellerShroudParams params) {
		params.maxOffset = 120f;
		params.initialMembers = 30;
		params.baseMembersToMaintain = params.initialMembers;
		params.baseSpriteSize *= 1.33f;
		//params.baseSpriteSize *= 1.5f;
		
		params.spawnOffsetMult = 1f;
		params.spawnOffsetMultForInitialSpawn = params.spawnOffsetMult;
		
		//params.negativeParticleAreaMult = 1.25f;
		//params.negativeParticleDurMult *= 0.5f;
		//params.negativeParticleSizeMult *= 1.4f;
		//params.negativeParticleGenRate *= 1f;
		
//		params.negativeParticleGenRate = 0f;
		//params.negativeParticleGenRate *= 0.5f;
		params.negativeParticleNumBase = 3;
		params.negativeParticleHighContrastMode = true;
		params.negativeParticleSizeMult = 1.5f;
		
//		params.alphaMult = 0.25f;
//		params.negativeParticleGenRate = 0f;
		
		//params.overloadGlowSizeMult *= 0.5f;
		
//		params.overloadArcThickness *= 0.5f;
//		params.overloadArcCoreThickness *= 0.5f;
		
//		params.maxSpeed += 500f;
		params.springStretchMult = 1f;
//		params.baseFriction *= 10f;
//		params.frictionRange *= 1f;
	}
	
	

	@Override
	public void initInCombat(ShipAPI ship) {
		super.initInCombat(ship);
		
		ship.setExplosionScale(0f); // no explosion sound or visual, just dissipates and fades out
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
}









