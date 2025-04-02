package com.fs.starfarer.api.impl.combat.threat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class VoltaicCannonEffect implements OnHitEffectPlugin, OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

	public int MIN_NUM_ARCS = 10;
	public int MAX_NUM_ARCS = 14;
	
	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		
	}
	
	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
	}
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		int numArcs = MIN_NUM_ARCS + Misc.random.nextInt(MAX_NUM_ARCS - MIN_NUM_ARCS + 1);
		float pierceChance = 0f;
		if (target instanceof ShipAPI) {
			pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
			pierceChance *= ((ShipAPI)target).getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
		}
		
		float emp = projectile.getEmpAmount();
		float dam = 0;
		
		EmpArcParams params = new EmpArcParams();
		params.segmentLengthMult = 8f;
		params.zigZagReductionFactor = 0.5f;
		params.flickerRateMult = 1f;
		params.fadeOutDist = 1000f;
		params.minFadeOutMult = 1f;
		params.glowSizeMult = 0.5f;
		params.glowAlphaMult = 0.75f;
		
		for (int i = 0; i < numArcs; i++) {
			boolean piercedShield = shieldHit && (float) Math.random() < pierceChance;
			//piercedShield = true;
			
			if (!shieldHit || piercedShield) {
				EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(
								   projectile.getSource(), point, target, target,
								   DamageType.ENERGY, 
								   dam, // damage
								   emp, // emp 
								   100000f, // max range 
								   "voltaic_discharge_emp_impact",
								   20f,
								   projectile.getProjectileSpec().getFringeColor(),
								   Color.white,
								   params);
				arc.setRenderGlowAtStart(false);
				//arc.setFadedOutAtStart(true);
			}
		}
	}
	
}







