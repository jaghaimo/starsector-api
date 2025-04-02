package com.fs.starfarer.api.impl.combat.dweller;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class DwellerShroud extends RoilingSwarmEffect {

	public static Color SHROUD_COLOR = new Color(100, 0, 25, 255);
	public static Color SHROUD_GLOW_COLOR = new Color(150, 0, 30, 255);
	public static Color SHROUD_OVERLOAD_FRINGE_COLOR = new Color(150, 0, 0, 255);
	
	public static interface ShroudNegativeParticleFilter {
		boolean isParticleOk(DwellerShroud shroud, Vector2f loc);
	}
	
	public static class DwellerShroudParams extends RoilingSwarmParams {
		public float overloadGlowSizeMult = 1f;
		public float overloadArcThickness = 40f;
		public float overloadArcCoreThickness = 20f;
		public Color overloadArcFringeColor = SHROUD_OVERLOAD_FRINGE_COLOR;
		public float overloadArcGenRate = 0.25f;
		public float overloadArcOffsetMult = 1f;
		
		public float negativeParticleGenRate = 1f;
		public float negativeParticleSizeMult = 1f;
		public float negativeParticleVelMult = 1f;
		public float negativeParticleDurMult = 1f;
		public float negativeParticleSpeedCapMult = 1.5f;
		public float negativeParticleSpeedCap = 10000f;
		public float negativeParticleAreaMult = 1f;
		public float negativeParticleClearCenterAreaRadius = 0f;
		public boolean negativeParticleHighContrastMode = false;
		public int negativeParticleNumBase = 11;
		public int negativeParticleNumOverloaded = 5;
		public int negativeParticleAlphaIntOverride = -1;
		public Color negativeParticleColorOverride = null;
		public ShroudNegativeParticleFilter negativeParticleFilter = null;
	}
	
	
	public static DwellerShroud getShroudFor(CombatEntityAPI entity) {
		RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(entity);
		if (swarm instanceof DwellerShroud) {
			return (DwellerShroud) swarm;
		}
		return null;
	}
	
	public static DwellerShroudParams createBaselineParams(CombatEntityAPI attachedTo) {
		if (!(attachedTo instanceof ShipAPI)) {
			return null;
		}
		
		ShipAPI ship = (ShipAPI) attachedTo;
		DwellerShroudParams params = new DwellerShroudParams();
		float radius = 20f;
		int numMembers = 50;

		
//		"fx_particles1":"graphics/fx/fx_clouds00.png",
//		"fx_particles2":"graphics/fx/fx_clouds01.png",
//		"nebula_particles":"graphics/fx/nebula_colorless.png",
//		"nebula_particles2":"graphics/fx/cleaner_clouds00.png",
//		"dust_particles":"graphics/fx/dust_clouds_colorless.png",
		
//		params.spriteKey = "dust_particles";
//		params.spriteKey = "nebula_particles";
//		params.spriteKey = "fx_particles1";
		
		params.spriteCat = "dweller";
		params.spriteKey = "dweller_pieces";
		
		params.despawnSound = null; // no free-flying swarms, all are ships that have an explosion sound
		
		params.baseDur = 1f;
		params.durRange = 2f;
		params.memberRespawnRate = 100f;
		
		params.memberExchangeClass = null;
		params.flockingClass = null;
		params.maxSpeed = ship.getMaxSpeedWithoutBoost() + 
					Math.max(ship.getMaxSpeedWithoutBoost() * 0.25f + 50f, 100f);
		
		params.baseSpriteSize = 256f;
		params.baseSpriteSize = 128f * 1.5f * 0.67f;
		params.maxTurnRate = 120f;
		
		numMembers = 100;
		radius = 150f;

//		radius = 100;
//		numMembers = 40;
		
		params.flashCoreRadiusMult = 0f;
		//params.flashRadius = 0f;
		params.flashRadius = 300f;
		params.flashRadius = 150f;
		params.renderFlashOnSameLayer = true;
		params.flashRateMult = 0.25f;
		//params.flashFrequency = 10f;
		//params.flashFrequency = 20f;
		//params.flashFrequency = 40f;
		params.flashFrequency = 17f;
		params.numToFlash = 2;
		//params.flashFrequency = 50f;
		params.flashProbability = 1f;
		
		params.swarmLeadsByFractionOfVelocity = 0f;
		
		params.alphaMult = 1f;
		params.alphaMultBase = 1f;
		params.alphaMultFlash = 1f;
		
//		params.alphaMult = 0.25f;
//		params.negativeParticleGenRate = 0f;
		
		//params.color = RiftCascadeEffect.EXPLOSION_UNDERCOLOR;
		params.color = SHROUD_COLOR;
		params.flashFringeColor = SHROUD_GLOW_COLOR;
		
//		params.color = new Color(121, 56, 171, 255);
//		params.color = Misc.setBrightness(params.color, 200);
//		//params.flashFringeColor = new Color(7, 163, 169, 255);
//		params.flashFringeColor = params.color;
//		params.flashFringeColor = Misc.setBrightness(params.flashFringeColor, 250);
		
		params.flashCoreColor = Misc.setBrightness(params.color, 255);
		
		
		//params.despawnDist = params.maxOffset + 300f;
		
		params.maxOffset = radius;
		params.initialMembers = numMembers;
		params.baseMembersToMaintain = params.initialMembers;
		
		return params;
	}
	
	
	
	
	protected IntervalUtil interval = new IntervalUtil(0.075f, 0.125f);
	protected IntervalUtil overloadInterval = new IntervalUtil(0.075f, 0.125f);
	//protected IntervalUtil ventingInterval = new IntervalUtil(0.075f, 0.125f);
	protected ShipAPI ship;
	protected DwellerShroudParams shroudParams;
	
	
	public DwellerShroud(CombatEntityAPI attachedTo) {
		this(attachedTo, createBaselineParams(attachedTo));
	}
	public DwellerShroud(CombatEntityAPI attachedTo, DwellerShroudParams params) {
		super(attachedTo, params);
		this.shroudParams = params;
		if (attachedTo instanceof ShipAPI) {
			ship = (ShipAPI) attachedTo;
		}
	}

	
	
	@Override
	public int getNumMembersToMaintain() {
		return super.getNumMembersToMaintain();
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);

		boolean venting = ship.getFluxTracker().isVenting();
		boolean overloaded = ship.getFluxTracker().isOverloaded() || venting;
//		overloaded = true;
//		overloaded = !ship.getShield().isOn();
		
		if (overloaded) {
			params.springStretchMult = 1f;
			params.flashProbability = 0.25f;
			params.despawnDist = ship.getCollisionRadius();
			//params.alphaMult = 0.1f;
		} else {
			params.springStretchMult = 10f;
			params.flashProbability = 1f;
			params.despawnDist = 0f;
			//params.alphaMult = 1f;
		}
		
		
//		ventingInterval.advance(amount * 1f);
//		if (ventingInterval.intervalElapsed() && ship != null && venting) {
//			
//		}
		
		float empArcGenRate = shroudParams.overloadArcGenRate;
		overloadInterval.advance(amount * empArcGenRate * 1f);
		if (overloadInterval.intervalElapsed() && ship != null && overloaded && !isDespawning()) {
			EmpArcParams params = new EmpArcParams();
			params.segmentLengthMult = 4f;
			params.glowSizeMult = 5f + ship.getFluxLevel() * 2f;
			params.glowSizeMult *= shroudParams.overloadGlowSizeMult;
			//params.zigZagReductionFactor = 0f;
			//params.brightSpotFadeFraction = 0.33f;
			//params.brightSpotFullFraction = 0.5f;
			//params.movementDurMax = 0.2f;
			//params.flickerRateMult = overloadRate;
			params.flickerRateMult = 0.5f + (float) Math.random() * 0.5f;
			
			Color fringe = this.params.flashFringeColor;
			//fringe = Misc.setAlpha(fringe, 127);
//			fringe = Misc.scaleColor(fringe, 0.75f);
			fringe = shroudParams.overloadArcFringeColor;
			//fringe = new Color(240,255,0,255);
			Color core = Color.white;

			float thickness = shroudParams.overloadArcThickness;
			
			Vector2f loc = ship.getLocation();
			float r = this.params.maxOffset;
			r = r * 0.5f + r * 0.5f * (float) Math.random();
			//r *= 1.5f;
			r *= shroudParams.overloadArcOffsetMult;
			Vector2f from = Misc.getPointAtRadius(loc, r);
			float angle = Misc.getAngleInDegrees(from, loc);
			angle = angle + 90f * ((float) Math.random() - 0.5f);
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
			float dist = this.params.maxOffset;
			dist = dist * 0.5f + dist * 0.5f * (float) Math.random();
			dist *= 1.5f;
			dist *= shroudParams.overloadArcOffsetMult;
			dir.scale(dist);
			Vector2f to = Vector2f.add(from, dir, new Vector2f());
			
//			float minBright = 200f;
//			if (dist * params.brightSpotFullFraction < minBright) {
//				params.brightSpotFullFraction = minBright / Math.max(minBright, dist);
//			}
			
			CombatEngineAPI engine = Global.getCombatEngine();
			EmpArcEntityAPI arc = (EmpArcEntityAPI)engine.spawnEmpArcVisual(
					from, ship, to, ship, thickness, fringe, core, params);
					
			arc.setCoreWidthOverride(shroudParams.overloadArcCoreThickness);
			
			Global.getSoundPlayer().playSound("dweller_venting_or_overloaded", 1f, 1f, to, ship.getVelocity());
			//arc.setSingleFlickerMode(true);
			//arc.setSingleFlickerMode(false);
		}
		
		//params.alphaMult = 1f;
		//params.alphaMult = 0f;
		interval.advance(amount * shroudParams.negativeParticleGenRate);
		if (interval.intervalElapsed()) {
			CombatEngineAPI engine = Global.getCombatEngine();
			
			boolean smallerDark = false;
			//smallerDark = true;
			Color c = RiftLanceEffect.getColorForDarkening(params.color);
			c = Misc.setAlpha(c, 100);
			int num = shroudParams.negativeParticleNumBase;
			if (smallerDark) num = 8;
			if (overloaded) {
				c = Misc.setAlpha(c, 150);
				num = shroudParams.negativeParticleNumOverloaded;
				if (smallerDark) num = 4; 
			}
			if (shroudParams.negativeParticleHighContrastMode) {
				c = Misc.setAlpha(c, 150);
			}
			if (shroudParams.negativeParticleColorOverride != null) {
				c = shroudParams.negativeParticleColorOverride;
			}
			if (shroudParams.negativeParticleAlphaIntOverride >= 0) {
				c = Misc.setAlpha(c, shroudParams.negativeParticleAlphaIntOverride);
			}
			
			float baseDuration = 2f;
			Vector2f vel = new Vector2f(attachedTo.getVelocity());
			float speed = vel.length();
			if (attachedTo instanceof ShipAPI) {
				float maxSpeed = ((ShipAPI)attachedTo).getMaxSpeed() * shroudParams.negativeParticleSpeedCapMult;
				maxSpeed = Math.min(maxSpeed, shroudParams.negativeParticleSpeedCap);
				if (speed > maxSpeed && speed > 1f) {
					vel.scale(maxSpeed / speed);
				}
			}
			
			float baseSize = params.maxOffset * 2f;
			//baseSize = params.maxOffset * 1f;
			
			//float size = ship.getCollisionRadius() * 0.35f;
			float size = baseSize * 0.33f;
			
			float extraDur = 0f;
			
			// so that switching the view to another ship near a dweller part
			// doesn't result in it not having negative particles
			Global.getCombatEngine().getViewport().setEverythingNearViewport(true);
			
			//for (int i = 0; i < 3; i++) {
			for (int i = 0; i < num; i++) {
			//for (int i = 0; i < 7; i++) {
				Vector2f point = new Vector2f(attachedTo.getLocation());
				float min = shroudParams.negativeParticleClearCenterAreaRadius;
				if (min > 0) {
					point = Misc.getPointWithinRadiusUniform(point, min, 
							Math.max(min, baseSize * 0.75f * (smallerDark ? 0.85f : 1f) * shroudParams.negativeParticleAreaMult), Misc.random);
				} else {
					point = Misc.getPointWithinRadiusUniform(point, 
							baseSize * 0.75f * (smallerDark ? 0.85f : 1f) * shroudParams.negativeParticleAreaMult, Misc.random);
				}
				
				float dur = baseDuration + baseDuration * (float) Math.random();
				dur += extraDur;
				float nSize = size;
				Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
				Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
				v.scale(nSize + nSize * (float) Math.random() * 0.5f);
				v.scale(0.2f * shroudParams.negativeParticleVelMult);
				Vector2f.add(vel, v, v);
				
				float maxSpeed = nSize * 1.5f * 0.2f; 
				float minSpeed = nSize * 1f * 0.2f; 
				float overMin = v.length() - minSpeed;
				if (overMin > 0) {
					float durMult = 1f - overMin / (maxSpeed - minSpeed);
					if (durMult < 0.1f) durMult = 0.1f;
					dur *= 0.5f + 0.5f * durMult;
				}
				
				dur *= shroudParams.negativeParticleDurMult;
				
				//nSize *= 1.5f;
				
				if (shroudParams.negativeParticleFilter != null && 
						!shroudParams.negativeParticleFilter.isParticleOk(this, pt)) {
					continue;
				}
				
				engine.addNegativeNebulaParticle(pt, v, nSize * 1f * shroudParams.negativeParticleSizeMult, 2f,
												0.5f / dur, 0f, dur, c);
			}
			Global.getCombatEngine().getViewport().setEverythingNearViewport(false);
		}
		
	}

	public DwellerShroudParams getShroudParams() {
		return shroudParams;
	}
}
