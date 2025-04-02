package com.fs.starfarer.api.impl.combat.dweller;

import java.util.Iterator;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ShroudedLensHullmod extends HumanShipShroudedHullmod {
	
	public static float MAX_RANGE = 400f;
	public static float RADIUS = 50f;
	
	public static float MIN_REFIRE_DELAY = 0.9f;
	public static float MAX_REFIRE_DELAY = 1.1f;

	public static float FLUX_PER_DAMAGE = 1f;
	
	public static float DAMAGE = 75f;
	public static float MIN_ROF_MULT = 1f;
	public static float MAX_ROF_MULT = 4f;
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public CargoStackAPI getRequiredItem() {
		return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, 
								new SpecialItemData(Items.SHROUDED_LENS, null), null);
	}
	
	public static String DATA_KEY = "core_ShroudedLensHullmod_data_key";
	
	public static class ShroudedLensHullmodData {
		//IntervalUtil interval = new IntervalUtil(0.75f, 1.25f);
		float untilAttack = 0f;
		float sinceAttack = 1000f;
	}
	
	public static ShroudedLensHullmodData getData(ShipAPI ship) {
		CombatEngineAPI engine = Global.getCombatEngine();
		String key = DATA_KEY + "_" + ship.getId();
		ShroudedLensHullmodData data = (ShroudedLensHullmodData) engine.getCustomData().get(key);
		if (data == null) {
			data = new ShroudedLensHullmodData();
			engine.getCustomData().put(key, data);
		}
		return data;
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);
		
		if (!ship.isAlive()) return;
		if (amount <= 0f) return;
		
		ShroudedLensHullmodData data = getData(ship);
		data.untilAttack -= amount * getRoF(ship.getHullSize());
		if (data.untilAttack <= 0f) {
			CombatEntityAPI target = findTarget(ship);
			if (target != null) {
				spawnExplosion(ship, target);
			}
			data.untilAttack = MIN_REFIRE_DELAY + (float) Math.random() * (MAX_REFIRE_DELAY - MIN_REFIRE_DELAY);
		}
	}
	
	public static float getPowerMult(HullSize size) {
		switch (size) {
		case CAPITAL_SHIP: return 1f;
		case CRUISER: return 0.6666666667f;
		case DESTROYER: return 0.3333333333f;
		case FIGHTER:
		case FRIGATE:
			return 0f;
		}
		return 1f;
	}
	
	public static float getRoF(HullSize size) {
		float mult = getPowerMult(size);
		return MIN_ROF_MULT + (MAX_ROF_MULT - MIN_ROF_MULT) * mult;	
	}
	public static float getFluxCost(HullSize size) {
		return DAMAGE * FLUX_PER_DAMAGE;	
	}
	public static float getDamage(HullSize size) {
		return DAMAGE;	
	}
	
	public void spawnExplosion(ShipAPI ship, CombatEntityAPI target) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		
		float angle = Misc.getAngleInDegrees(target.getLocation(), ship.getLocation());
		angle += 45f - 90f * (float) Math.random();
		Vector2f from = Misc.getUnitVectorAtDegreeAngle(angle);
		from.scale(10000f);
		
		float targetRadius = Misc.getTargetingRadius(from, target, false);		
		Vector2f point = Misc.getUnitVector(target.getLocation(), from);
		point.scale(targetRadius * (0.8f + (float) Math.random() * 0.4f));
		Vector2f.add(target.getLocation(), point, point);

		float dist = Misc.getDistance(from, point);
		
		//float mult = getPowerMult(ship.getHullSize());
		float damage = getDamage(ship.getHullSize());
		
		if (FLUX_PER_DAMAGE > 0f) {
			float fluxCost = getFluxCost(ship.getHullSize());
			//if (!ship.getFluxTracker().increaseFlux(fluxCost, false)) {
			if (!deductFlux(ship, fluxCost)) {
				return;
			}
		}
		
		DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
		if (shroud != null) {
			angle = Misc.getAngleInDegrees(ship.getLocation(), point);
			from = Misc.getUnitVectorAtDegreeAngle(angle + 90f - 180f * (float) Math.random());
			from.scale((0.5f + (float) Math.random() * 0.25f) * shroud.getShroudParams().maxOffset * shroud.getShroudParams().overloadArcOffsetMult);
			Vector2f.add(ship.getLocation(), from, from);
		}
		
		Color color = RiftLightningEffect.RIFT_LIGHTNING_COLOR;
		
		if (shroud != null) {
			DwellerShroudParams shroudParams = shroud.getShroudParams();
			EmpArcParams params = new EmpArcParams();
			params.segmentLengthMult = 4f;
			params.glowSizeMult = 4f;
			params.flickerRateMult = 0.5f + (float) Math.random() * 0.5f;
			params.flickerRateMult *= 1.5f;
			
			//Color fringe = shroudParams.overloadArcFringeColor;
			Color fringe = color;
			Color core = Color.white;

			float thickness = shroudParams.overloadArcThickness;
			
			//Vector2f to = Misc.getPointAtRadius(from, 1f);
			
			angle = Misc.getAngleInDegrees(from, ship.getLocation());
			angle = angle + 90f * ((float) Math.random() - 0.5f);
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
			dist = shroudParams.maxOffset * shroud.getShroudParams().overloadArcOffsetMult;
			dist = dist * 0.5f + dist * 0.5f * (float) Math.random();
			//dist *= 1.5f;
			dist *= 0.5f;
			dir.scale(dist);
			Vector2f to = Vector2f.add(from, dir, new Vector2f());
			
			EmpArcEntityAPI arc = (EmpArcEntityAPI)engine.spawnEmpArcVisual(
					from, ship, to, ship, thickness, fringe, core, params);
			
			arc.setCoreWidthOverride(shroudParams.overloadArcCoreThickness);
			arc.setSingleFlickerMode(false);
			//arc.setRenderGlowAtStart(false);
		}
		
		//float explosionRadius = 40f + mult * 40f;
		float explosionRadius = RADIUS;
		
		DamagingExplosionSpec spec = new DamagingExplosionSpec(
				0.1f, // duration
				explosionRadius, // radius
				explosionRadius * 0.5f, // coreRadius
				damage, // maxDamage
				damage, // / 2f, // minDamage - no damage dropoff with range
				CollisionClass.PROJECTILE_NO_FF, // collisionClass
				CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter - using to flag it as from this effect
				3f, // particleSizeMin
				3f, // particleSizeRange
				0.5f, // particleDuration
				0, // particleCount
				new Color(255,255,255,0), // particleColor
				new Color(255,100,100,0)  // explosionColor
				);

		spec.setDamageType(DamageType.ENERGY);
		spec.setUseDetailedExplosion(false);
		spec.setSoundSetId("abyssal_glare_explosion");
		//spec.setSoundVolume(0.5f + 0.5f * mult);
		spec.setSoundVolume(0.33f);

		DamagingProjectileAPI explosion = engine.spawnDamagingExplosion(spec, ship, point);
		
		//explosion.addDamagedAlready(target);
		//color = new Color(255,75,75,255);

		float baseSize = 7f;

		NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(
				color, baseSize);
		//p.hitGlowSizeMult = 0.5f;
		p.noiseMult = 6f;
		p.thickness = 25f;
		p.fadeOut = 0.5f;
		p.spawnHitGlowAt = 1f;
		p.additiveBlend = true;
		p.blackColor = Color.white;
		p.underglow = null;
		p.withNegativeParticles = false;
		p.withHitGlow = false;
		p.fadeIn = 0f;
		//p.numRiftsToSpawn = 1;

		RiftCascadeMineExplosion.spawnStandardRift(explosion, p);
		
		
		// the "beam"
		
		float thickness = 30f;
		//Color color = weapon.getSpec().getGlowColor();
		Color coreColor = Color.white;
		coreColor = Misc.zeroColor;
		coreColor = color;
		
		color = new Color(255,0,30,255);
		coreColor = new Color(255,10,255,255);
		
		color = DwellerShroud.SHROUD_GLOW_COLOR;
		coreColor = color;
		//coreColor = Color.white;
		
		float coreWidthMult = 1f;
		
		
//		from = ship.getLocation();
//		if (shroud != null) {
//			angle = Misc.getAngleInDegrees(ship.getLocation(), target.getLocation());
//			from = Misc.getUnitVectorAtDegreeAngle(angle + 90f - 180f * (float) Math.random());
//			from.scale((0.5f + (float) Math.random() * 0.25f) * shroud.getShroudParams().maxOffset);
//			Vector2f.add(ship.getLocation(), from, from);
//		}
		
		EmpArcParams params = new EmpArcParams();
		//params.segmentLengthMult = 10000f;
		params.segmentLengthMult = 4f;
		
//		params.maxZigZagMult = 0f;
//		params.zigZagReductionFactor = 1f;
		
		params.maxZigZagMult = 0.25f;
		//params.maxZigZagMult = 0f;
		params.zigZagReductionFactor = 1f;
		
		//params.glowColorOverride = new Color(255,10,155,255);
		
		//params.zigZagReductionFactor = 0.25f;
		//params.maxZigZagMult = 0f;
		//params.flickerRateMult = 0.75f;
//		params.flickerRateMult = 1f;
//		params.flickerRateMult = 0.75f;
		params.flickerRateMult = 0.75f + 0.25f * (float) Math.random();
		
		params.fadeOutDist = 150f;
		params.minFadeOutMult = 5f;
		
		params.glowSizeMult = 0.5f;
		
		//params.glowAlphaMult = 0.5f;
		//params.flamesOutMissiles = false;
		
//		params.movementDurOverride = 0.1f;
//		params.flickerRateMult = 0.5f;
//		params.glowSizeMult = 1f;
//		params.brightSpotFadeFraction = 0.1f;
//		params.brightSpotFullFraction = 0.9f;
		
//		params.maxZigZagMult = 1f;
//		params.zigZagReductionFactor = 0f;
//		params.flickerRateMult = 0.25f;
		
		Vector2f to = point;
		EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, ship, to, explosion, thickness, color, coreColor, params);
		arc.setCoreWidthOverride(thickness * coreWidthMult);
		arc.setSingleFlickerMode();
		arc.setRenderGlowAtStart(false);
		if (shroud != null) {
			arc.setFadedOutAtStart(true);
		}
		arc.setWarping(0.2f);
		
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, final ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		
		tooltip.addPara("The \"lens\" (in the loosest sense of the word) focuses on nearby objects, "
				+ "seemingly at random. Deals %s damage and generates %s flux.", opad, h,
				"" + (int) getDamage(hullSize) + " Energy", "" + (int) getFluxCost(hullSize));
		
		tooltip.addPara("The rate of fire depends on the size of the ship the "
					  + "hullmod is installed on. Can not be turned off during combat operations, and continues to "
					  + "function even if the ship is venting flux or overloaded.", opad);
		
		//tooltip.addSectionHeading("Reload capacity", Alignment.MID, opad);
		
		if (isForModSpec || (ship == null && !Global.CODEX_TOOLTIP_MODE)) return;
		
		tooltip.setBgAlpha(0.9f);
		
		HullSize [] sizes = new HullSize[] {HullSize.FRIGATE, HullSize.DESTROYER, HullSize.CRUISER, HullSize.CAPITAL_SHIP}; 
		
		float rofW = 130f;
		float fluxW = 130f;
		float sizeW = width - rofW - fluxW - 10f;
		tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
				   20f, true, true, 
				   new Object [] {"Ship size", sizeW, "Attacks / sec", rofW, "Flux / sec", fluxW});
		
		for (HullSize size : sizes) {
			float rof = getRoF(size);
			float flux = getFluxCost(size) * rof;
			Color c = Misc.getGrayColor();
			if (size == hullSize || Global.CODEX_TOOLTIP_MODE) {
				c = Misc.getHighlightColor();
			}
			tooltip.addRow(Alignment.MID, c, Misc.getHullSizeStr(size),
					   	   Alignment.MID, c, "" + (int) Misc.getRoundedValueFloat(rof) + "",
						   Alignment.MID, c, "" + (int) Misc.getRoundedValueFloat(flux) + "");
		}
		tooltip.addTable("", 0, opad);
		
		tooltip.addSpacer(5f);
		
		addCrewCasualties(tooltip, opad);
		
//		if (Global.CODEX_TOOLTIP_MODE) {
//			return;
//		}
	}	
	

//	@Override
//	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
//		if (index == 0) return "" + (int) Math.round(SMOD_CR_PENALTY * 100f) + "%";
//		if (index == 1) return "" + (int) Math.round(SMOD_MAINTENANCE_PENALTY) + "%";
//		return null;
//	}
//	
//	@Override
//	public boolean isSModEffectAPenalty() {
//		return true;
//	}
	
	
	public CombatEntityAPI findTarget(ShipAPI ship) {
		float range = MAX_RANGE;
		Vector2f from = ship.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = ship.getOwner();
		CombatEntityAPI best = null;
		float minScore = Float.MAX_VALUE;
		
		//boolean ignoreFlares = ship != null && ship.getMutableStats().getDynamic().getValue(Stats.PD_IGNORES_FLARES, 0) >= 1;
		//ignoreFlares |= weapon.hasAIHint(AIHints.IGNORES_FLARES);
		boolean ignoreFlares = false; // doesn't care one way or another
		
		WeightedRandomPicker<CombatEntityAPI> picker = new WeightedRandomPicker<>();
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof MissileAPI) &&
					//!(o instanceof CombatAsteroidAPI) &&
					!(o instanceof ShipAPI)) continue;
			CombatEntityAPI other = (CombatEntityAPI) o;
			if (other.getOwner() == owner) continue;
			
			if (other instanceof ShipAPI) {
				ShipAPI otherShip = (ShipAPI) other;
				//if (otherShip.isHulk()) continue;
				//if (!otherShip.isAlive()) continue;
				if (otherShip.isPhased()) continue;
				if (!otherShip.isTargetable()) continue;
			}
			
			if (other.getCollisionClass() == CollisionClass.NONE) continue;
			
			if (ignoreFlares && other instanceof MissileAPI) {
				MissileAPI missile = (MissileAPI) other;
				if (missile.isFlare()) continue;
			}

			float targetRadius = Misc.getTargetingRadius(from, other, false);
			float shipRadius = Misc.getTargetingRadius(other.getLocation(), ship, false);
			float dist = Misc.getDistance(from, other.getLocation()) - targetRadius - shipRadius;
			if (dist > range) continue;
			
			float score = dist;
			
			if (score < minScore) {
				minScore = score;
				best = other;
			}
			
			picker.add(other, 100f / Math.max(100f, score));
		}
		
		//return best;
		return picker.pick();
	}	
}














