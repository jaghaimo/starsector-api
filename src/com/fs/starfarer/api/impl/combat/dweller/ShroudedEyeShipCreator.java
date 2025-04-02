package com.fs.starfarer.api.impl.combat.dweller;

import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.dweller.DwellerCombatPlugin.WobblyPart;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ShroudedEyeShipCreator extends BaseDwellerShipCreator {

	public static class PlasmaEyePart extends BaseDwellerShipPart {
		public float radius;
		public IntervalUtil interval = new IntervalUtil(0.75f, 1.25f);
		public ShipAPI ship;
		public NegativeExplosionVisual visual;

		public PlasmaEyePart(Vector2f offset, float facingOffset, ShipAPI ship, float radius) {
			super(offset, facingOffset);
			this.ship = ship;
			this.radius = radius;
			
			NEParams p = new NEParams();
			p.hitGlowSizeMult = 0.75f;
			p.noiseMag = 1f;
//			p.radius = 35f;
//			p.noiseMult = 3f;
			p.radius = 15f;
			p.noiseMult = 2f;
			p.color = new Color(255,55,255,155);
			p.blackColor = new Color(255,155,255,255);
			p.color = new Color(255,55,55,155);
			p.blackColor = new Color(255,155,155,255);

			//p.thickness = 25f;
			p.underglow = null;
			p.spawnHitGlowAt = 0f;
			p.withHitGlow = false;
			//p.additiveBlend = true;
			
			
			visual = new NegativeExplosionVisual(p);
			visual.init(ship);
			visual.getFader().forceIn();
		}

		@Override
		public void advance(float amount) {
			super.advance(amount);
			
			visual.getFader().fadeIn();
			visual.advance(amount * 0.5f);
			
			interval.advance(amount * 30f);
			if (interval.intervalElapsed() && !ship.getFluxTracker().isOverloadedOrVenting()) {
				EmpArcParams params = new EmpArcParams();
				params.segmentLengthMult = 4f;
				params.glowSizeMult = 2.5f;
				
				if ((float) Math.random() < 0.1f) {
					params.glowSizeMult = 7f;
				}
				//params.glowSizeMult = 1.5f;
				//params.zigZagReductionFactor = 0f;
				//params.brightSpotFadeFraction = 0.33f;
				
				//params.brightSpotFullFraction = 0.33f;
				
				params.flickerRateMult = 0.5f + (float) Math.random() * 0.5f;
				
				params.fadeOutDist = 30f;
				params.minFadeOutMult = 5f;
				params.flickerRateMult *= 0.3f;
				params.movementDurOverride = 0.3f;
				
				Color fringe = DwellerShroud.SHROUD_OVERLOAD_FRINGE_COLOR;
				fringe = new Color(150, 30, 40, 255);
				Color core = Color.white;
				fringe = new Color(150, 30, 40, 255);
				core = new Color(255, 150, 190, 255);
				
				fringe = new Color(150, 30, 30, 255);
				core = new Color(255, 150, 150, 255);

				float thickness = 80f;
				thickness = 40f;
				
				Vector2f loc = ship.getLocation();
				float r = radius * 1.1f;
				r = r * 0.5f + r * 0.5f * (float) Math.random();
				Vector2f from = Misc.getPointAtRadius(loc, r);
				
				Vector2f to = new Vector2f(loc);
				to = Misc.getPointWithinRadius(to, radius * 0.1f);
				
				boolean goingOutside = false;
				if ((float) Math.random() < 0.4f && false) {
					Vector2f temp = to;
					to = from;
					from = temp;
					goingOutside = true;
				}
				
				CombatEngineAPI engine = Global.getCombatEngine();
				EmpArcEntityAPI arc = (EmpArcEntityAPI)engine.spawnEmpArcVisual(
						from, ship, to, ship, thickness, fringe, core, params);
				
				arc.setCoreWidthOverride(60f);
				arc.setCoreWidthOverride(20f);
				arc.setCoreWidthOverride(25f);
				
				if (goingOutside) {
					arc.setRenderGlowAtEnd(false);
				} else {
					arc.setRenderGlowAtStart(false);
					arc.setFadedOutAtStart(true);
				}
				
				arc.setSingleFlickerMode(true);
				
//				arc.setRenderGlowAtStart(false);
//				arc.setFadedOutAtStart(true);
				
				if (ship.getSystem() != null && ship.getSystem().getEffectLevel() > 0f) {
					float level = ship.getSystem().getEffectLevel();
					Vector2f pt = Misc.getPointWithinRadius(loc, 20f * level);
					//engine.addHitParticle(pt, ship.getVelocity(), 75f, 0.5f, core);
					engine.addNebulaParticle(pt, ship.getVelocity(), 35f + 30f * level, 2f,
							0f, 0f, 1f, core);
				}
			}
			
			if (!ship.getFluxTracker().isOverloadedOrVenting()) {
				Global.getSoundPlayer().playLoop("shrouded_eye_loop", 
						ship, 1f, 0.5f + 0.5f * ship.getFluxLevel(),
						ship.getLocation(), ship.getVelocity());
			}
		}

		@Override
		protected void renderImpl(float x, float y, float alphaMult, float angle, CombatEngineLayers layer) {
			super.renderImpl(x, y, alphaMult, angle, layer);
			
			ViewportAPI viewport = Global.getCombatEngine().getViewport();
			float vAlpha = viewport.getAlphaMult();
			viewport.setAlphaMult(alphaMult);
			if (layer == CombatEngineLayers.BELOW_INDICATORS_LAYER) {
				visual.render(CombatEngineLayers.ABOVE_PARTICLES_LOWER, viewport);
				visual.render(CombatEngineLayers.ABOVE_PARTICLES, viewport);
			}
//			if (layer == CombatEngineLayers.ABOVE_PARTICLES_LOWER) {
//				visual.render(CombatEngineLayers.ABOVE_PARTICLES_LOWER, viewport);
//			}
//			if (layer == CombatEngineLayers.ABOVE_PARTICLES) {
//				visual.render(CombatEngineLayers.ABOVE_PARTICLES, viewport);
//			}
			viewport.setAlphaMult(vAlpha);
		}
		
	}
	
	
	public static float BEAM_RANGE_BONUS = 1200f;
	public static float HARD_FLUX_DISSIPATION_PERCENT = 100f;
	public static float DAMAGE_MULT = 0.75f;
	
	@Override
	public void initBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		super.initBeforeShipCreation(hullSize, stats, id);
		
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 0);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 0);
		stats.getMissileWeaponFluxCostMod().modifyMult(id, 0);
		
		stats.getBeamWeaponRangeBonus().modifyFlat(id, BEAM_RANGE_BONUS);
		
		stats.getBeamWeaponDamageMult().modifyMult(id, DAMAGE_MULT);
		
		// basically instant
		stats.getShieldUnfoldRateMult().modifyPercent(id, 10000f);
		// doesn't matter if unfold is instant
		//stats.getShieldTurnRateMult().modifyPercent(id, SHIELD_BONUS_TURN);
		
		stats.getHardFluxDissipationFraction().modifyFlat(id, (float)HARD_FLUX_DISSIPATION_PERCENT * 0.01f);
	}


	@Override
	protected DwellerCombatPlugin createPlugin(ShipAPI ship) {
		DwellerCombatPlugin plugin = super.createPlugin(ship);
		
		List<DwellerShipPart> parts = plugin.getParts();
		parts.clear();
		
		//WobblyPart part = new WobblyPart("shrouded_eye_base", 1f, 5, 5, 1f, new Vector2f(0, 0), 0f);
		WobblyPart part = new WobblyPart("shrouded_eye_base_dark", 1f, 5, 5, 1f, new Vector2f(0, 0), 0f);
		parts.add(part);
		
		Color glow = DwellerCombatPlugin.STANDARD_PART_GLOW_COLOR;
		//glow = new Color(150, 30, 80, 255);
		
		part = new WobblyPart("shrouded_eye_iris", 0.6f, 3, 3, 1f, new Vector2f(0, 0), 0f);
		part.color = glow;
		part.additiveBlend = true;
		part.alphaMult = 0.5f;
		//part.setWeaponActivated();
		parts.add(part);
		
		PlasmaEyePart eyePart = new PlasmaEyePart(new Vector2f(0, 0), 0f, ship, 110f);
		parts.add(eyePart);
		
//		plugin.getActiveLayers().add(CombatEngineLayers.ABOVE_PARTICLES_LOWER);
//		plugin.getActiveLayers().add(CombatEngineLayers.ABOVE_PARTICLES);
		
		return plugin;
	}

	@Override
	protected void modifyBaselineShroudParams(ShipAPI ship, DwellerShroudParams params) {
//		params.maxOffset = 50f;
//		params.initialMembers = 50;
//		params.negativeParticleAreaMult = 1.5f;
		
		params.maxOffset = 130f;
//		params.initialMembers = 2000;
//		params.baseMembersToMaintain = params.initialMembers;
//		params.numToRespawn = 100;
		
		//params.negativeParticleClearCenterAreaRadius = 150f;
		params.negativeParticleClearCenterAreaRadius = 50f;
		
//		public static Color SHROUD_COLOR = new Color(100, 0, 25, 255);
//		public static Color SHROUD_GLOW_COLOR = new Color(150, 0, 30, 255);
//		params.color = new Color(100, 10, 10, 255);
//		params.flashFringeColor = new Color(150, 12, 12, 255);
//		params.color = new Color(100, 0, 0, 255);
//		params.flashFringeColor = new Color(150, 0, 0, 255);
//		params.flashCoreColor = Misc.setBrightness(params.color, 255);
		
		//params.negativeParticleHighContrastMode = true;
//		params.negativeParticleNumBase = 4;
//		params.negativeParticleNumOverloaded = 4;
		//params.negativeParticleClearCenterAreaRadius = 200f;
		//params.negativeParticleAreaMult = 0.75f;
	}
	
}









