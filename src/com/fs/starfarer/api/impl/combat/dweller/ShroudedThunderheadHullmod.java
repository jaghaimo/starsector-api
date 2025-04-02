package com.fs.starfarer.api.impl.combat.dweller;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.impl.combat.threat.EnergyLashSystemScript.DelayedCombatActionPlugin;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ShroudedThunderheadHullmod extends HumanShipShroudedHullmod {
	
	public static float MAX_RANGE = 3000f;
	
	public static float RECENT_HIT_DUR = 5f;
	public static float MAX_TIME_SINCE_RECENT_HIT = 0.1f;
	public static float WEIGHT_PER_RECENT_HIT = 1f;
	public static float MISFIRE_WEIGHT = 10f;
	
	public static float MIN_REFIRE_DELAY = 0.22f;
	public static float MAX_REFIRE_DELAY = 0.44f;
	public static float REFIRE_RATE_MULT = 1f;

	public static float FLUX_PER_DAMAGE = 1f;
	public static float MIN_DAMAGE = 200f;
	public static float MAX_DAMAGE = 500f;
	public static float EMP_MULT = 2f;
	
	public static class ShroudedThunderheadDamageDealtMod implements DamageDealtModifier {
		public ShipAPI ship;
		public ShroudedThunderheadDamageDealtMod(ShipAPI ship) {
			this.ship = ship;
		}
		public String modifyDamageDealt(Object param,
								   		CombatEntityAPI target, DamageAPI damage,
								   		Vector2f point, boolean shieldHit) {
			if (param instanceof DamagingProjectileAPI) {
				DamagingProjectileAPI proj = (DamagingProjectileAPI) param;
				DamagingExplosionSpec spec = proj.getExplosionSpecIfExplosion();
				if (spec != null && spec.getCollisionClassIfByFighter() == CollisionClass.GAS_CLOUD) {
					return null;
				}
			} else if ((damage.isDps() && !damage.isForceHardFlux()) || damage.getDamage() <= 0f) {
				return null;
			}
			
			if (target != null) {
				ShroudedThunderheadHullmodData data = getData(ship);
				RecentHitData hit = new RecentHitData();
				hit.param = param;
				hit.target = target;
				hit.point = new Vector2f(point);
				hit.damage = damage;
				hit.shieldHit = shieldHit;
				//data.recentHits.add(hit, 1.5f);
				data.recentHits.add(hit, RECENT_HIT_DUR);
			}
			return null;
		}
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		super.applyEffectsAfterShipCreation(ship, id);
		ship.addListener(new ShroudedThunderheadDamageDealtMod(ship));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public CargoStackAPI getRequiredItem() {
		return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, 
								new SpecialItemData(Items.SHROUDED_THUNDERHEAD, null), null);
	}
	
	public static String DATA_KEY = "core_ShroudedThunderheadHullmod_data_key";
	public static class RecentHitData {
		Object param;
		CombatEntityAPI target;
		Vector2f point;
		DamageAPI damage;
		boolean shieldHit;
	}
	public static class ShroudedThunderheadHullmodData {
		float untilArc = 0f;
		TimeoutTracker<RecentHitData> recentHits = new TimeoutTracker<RecentHitData>();

		boolean hasRecentEnoughHits() {
			for (RecentHitData curr : recentHits.getItems()) {
				float remaining = recentHits.getRemaining(curr);
				if (remaining >= RECENT_HIT_DUR - MAX_TIME_SINCE_RECENT_HIT) return true;
			}
			return false;
		}
		
		float getHitProbability() {
			float recent = recentHits.getItems().size() * WEIGHT_PER_RECENT_HIT;
			return recent / (recent + MISFIRE_WEIGHT);
		}
		
		RecentHitData pickRecentHit() {
			if (!hasRecentEnoughHits()) return null;
			WeightedRandomPicker<RecentHitData> picker = new WeightedRandomPicker<>();
			for (RecentHitData curr : recentHits.getItems()) {
				float remaining = recentHits.getRemaining(curr);
				if (remaining < RECENT_HIT_DUR - MAX_TIME_SINCE_RECENT_HIT * 2f) continue;
				picker.add(curr, WEIGHT_PER_RECENT_HIT);
			}
			RecentHitData misfire = new RecentHitData();
			picker.add(misfire, MISFIRE_WEIGHT);
			return picker.pick();
		}
	}
	
	public static ShroudedThunderheadHullmodData getData(ShipAPI ship) {
		CombatEngineAPI engine = Global.getCombatEngine();
		String key = DATA_KEY + "_" + ship.getId();
		ShroudedThunderheadHullmodData data = (ShroudedThunderheadHullmodData) engine.getCustomData().get(key);
		if (data == null) {
			data = new ShroudedThunderheadHullmodData();
			engine.getCustomData().put(key, data);
		}
		return data;
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);
		
		if (!ship.isAlive()) return;
		if (amount <= 0f) return;
		
		ShroudedThunderheadHullmodData data = getData(ship);
		
		
		float prob = data.getHitProbability();
		DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
		shroud.getParams().flashProbability = Math.min(0.1f + prob * 1.4f, 1f);
		//shroud.getParams().alphaMult = 0.5f; 
		
		data.untilArc -= amount * REFIRE_RATE_MULT;
		if (data.untilArc <= 0f) {
			boolean hasRecentHits = data.hasRecentEnoughHits();
			if (hasRecentHits) {
				RecentHitData hit = data.pickRecentHit();
				if (hit != null && hit.target != null) {
					data.recentHits.remove(hit);
					spawnLightning(ship, hit);
				}
				data.untilArc = MIN_REFIRE_DELAY + (float) Math.random() * (MAX_REFIRE_DELAY - MIN_REFIRE_DELAY);
			}
		}
		
		data.recentHits.advance(amount);
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
	
	public static float getDamage(HullSize size) {
		float mult = getPowerMult(size);
		return MIN_DAMAGE + (MAX_DAMAGE - MIN_DAMAGE) * mult;	
	}
	public static float getEMPDamage(HullSize size) {
		return getDamage(size) * EMP_MULT;	
	}
	public static float getFluxCost(HullSize size) {
		return getDamage(size) * FLUX_PER_DAMAGE;	
	}
	
	public void spawnLightning(ShipAPI ship, RecentHitData hit) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Vector2f from = ship.getLocation();
		Vector2f point = hit.point;

		float dist = Misc.getDistance(from, point);

		if (dist > MAX_RANGE) return;
		
		float mult = getPowerMult(ship.getHullSize());
		float damage = getDamage(ship.getHullSize());
		float emp = getEMPDamage(ship.getHullSize());
		
		if (FLUX_PER_DAMAGE > 0f) {
			float fluxCost = getFluxCost(ship.getHullSize());
			if (!deductFlux(ship, fluxCost)) {
				return;
			}
		}
		
		DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
		if (shroud != null) {
			float angle = Misc.getAngleInDegrees(ship.getLocation(), point);
			from = Misc.getUnitVectorAtDegreeAngle(angle + 90f - 180f * (float) Math.random());
			from.scale((0.5f + (float) Math.random() * 0.25f) * shroud.getShroudParams().maxOffset * shroud.getShroudParams().overloadArcOffsetMult);
			Vector2f.add(ship.getLocation(), from, from);
		}
		
		
		float arcSpeed = RiftLightningEffect.RIFT_LIGHTNING_SPEED;
		
		EmpArcParams params = new EmpArcParams();
		params.segmentLengthMult = 8f;
		params.zigZagReductionFactor = 0.15f;
		//params.fadeOutDist = ship.getCollisionRadius() * 0.5f;
		params.fadeOutDist = 50f;
		params.minFadeOutMult = 10f;
//		params.flickerRateMult = 0.7f;
		params.flickerRateMult = 0.3f;
//		params.flickerRateMult = 0.05f;
//		params.glowSizeMult = 3f;
//		params.brightSpotFullFraction = 0.5f;
		
		params.movementDurOverride = Math.max(0.05f, dist / arcSpeed);
		
		float arcWidth = 40f + mult * 40f;
		float explosionRadius = 40f + mult * 40f;
		
		//Color color = weapon.getSpec().getGlowColor();
		Color color = RiftLightningEffect.RIFT_LIGHTNING_COLOR;
		EmpArcEntityAPI arc = (EmpArcEntityAPI)engine.spawnEmpArcVisual(from, ship, point, null,
				arcWidth, // thickness
				color,
				new Color(255,255,255,255),
				params
				);
		arc.setCoreWidthOverride(arcWidth / 2f);
		
		arc.setRenderGlowAtStart(false);
		arc.setFadedOutAtStart(true);
		arc.setSingleFlickerMode(true);

		float volume = 0.75f + 0.25f * mult;
		float pitch = 1f + 0.25f * (1f - mult);
		Global.getSoundPlayer().playSound("rift_lightning_fire", pitch, volume, from, ship.getVelocity());
		
		if (shroud != null) {
			DwellerShroudParams shroudParams = shroud.getShroudParams();
			params = new EmpArcParams();
			params.segmentLengthMult = 4f;
			params.glowSizeMult = 4f;
			params.flickerRateMult = 0.5f + (float) Math.random() * 0.5f;
			params.flickerRateMult *= 1.5f;
			
			//Color fringe = shroudParams.overloadArcFringeColor;
			Color fringe = color;
			Color core = Color.white;

			float thickness = shroudParams.overloadArcThickness;
			
			//Vector2f to = Misc.getPointAtRadius(from, 1f);
			
			float angle = Misc.getAngleInDegrees(from, ship.getLocation());
			angle = angle + 90f * ((float) Math.random() - 0.5f);
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
			dist = shroudParams.maxOffset * shroud.getShroudParams().overloadArcOffsetMult;
			dist = dist * 0.5f + dist * 0.5f * (float) Math.random();
			//dist *= 1.5f;
			dist *= 0.5f;
			dir.scale(dist);
			Vector2f to = Vector2f.add(from, dir, new Vector2f());
			
			arc = (EmpArcEntityAPI)engine.spawnEmpArcVisual(
					from, ship, to, ship, thickness, fringe, core, params);
			
			arc.setCoreWidthOverride(shroudParams.overloadArcCoreThickness);
			arc.setSingleFlickerMode(false);
			//arc.setRenderGlowAtStart(false);
		}
		
		
		
		float explosionDelay = params.movementDurOverride * 0.8f;
		Global.getCombatEngine().addPlugin(new DelayedCombatActionPlugin(explosionDelay, new Runnable() {
			@Override
			public void run() {
				DamagingExplosionSpec spec = new DamagingExplosionSpec(
						0.1f, // duration
						explosionRadius, // radius
						explosionRadius * 0.5f, // coreRadius
						damage, // maxDamage
						damage / 2f, // minDamage
						CollisionClass.PROJECTILE_NO_FF, // collisionClass
						CollisionClass.GAS_CLOUD, // collisionClassByFighter - using to flag it as from this effect
						3f, // particleSizeMin
						3f, // particleSizeRange
						0.5f, // particleDuration
						0, // particleCount
						new Color(255,255,255,0), // particleColor
						new Color(255,100,100,0)  // explosionColor
						);
				spec.setMinEMPDamage(emp * 0.5f);
				spec.setMaxEMPDamage(emp);

				spec.setDamageType(DamageType.ENERGY);
				spec.setUseDetailedExplosion(false);
				spec.setSoundSetId("rift_lightning_explosion");
				spec.setSoundVolume(0.5f + 0.5f * mult);

				DamagingProjectileAPI explosion = engine.spawnDamagingExplosion(spec, ship, point);
				
				//explosion.addDamagedAlready(target);
				//color = new Color(255,75,75,255);

				//		float baseSize = 10f;
				//
				//		NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(
				//				color, baseSize);
				//		//p.hitGlowSizeMult = 0.5f;
				//		p.noiseMult = 6f;
				//		p.thickness = 25f;
				//		p.fadeOut = 0.5f;
				//		p.spawnHitGlowAt = 1f;
				//		p.additiveBlend = true;
				//		p.blackColor = Color.white;
				//		p.underglow = null;
				//		p.withNegativeParticles = false;
				//		p.withHitGlow = false;
				//		p.fadeIn = 0f;
				//		//p.numRiftsToSpawn = 1;
				//
				//		RiftCascadeMineExplosion.spawnStandardRift(explosion, p);

				Color color = RiftLightningEffect.RIFT_LIGHTNING_COLOR;
				color = new Color(255,75,75,255);
				NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(
						color, 14f + 6f * mult);
				p.fadeOut = 0.5f + 0.5f * mult;
				p.hitGlowSizeMult = 0.6f;
				p.thickness = 50f;
				//p.thickness = 25f;


				//		p.hitGlowSizeMult = 0.5f;
				//		p.thickness = 25f;
				//		p.fadeOut = 0.25f;

				RiftCascadeMineExplosion.spawnStandardRift(explosion, p);
			}
		}));
		
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
		
		
		tooltip.addPara("Fires rift lightning bolts at locations recently hit by this ship's weapons.", opad);
		
		tooltip.addPara("The amount of damage and the flux generated depend on the size of the ship the "
				+ "hullmod is installed on. The probability of a bolt being fired increases with the number "
				+ "of hits landed over the previous %s seconds. "
				+ "Bolts are not triggered by weapons dealing soft flux damage, such as beams.", opad,
				Misc.getHighlightColor(), "" + (int)Math.round(RECENT_HIT_DUR));
		
		//tooltip.addSectionHeading("Reload capacity", Alignment.MID, opad);
		
		if (isForModSpec || (ship == null && !Global.CODEX_TOOLTIP_MODE)) return;
		
		tooltip.setBgAlpha(0.9f);
		
		HullSize [] sizes = new HullSize[] {HullSize.FRIGATE, HullSize.DESTROYER, HullSize.CRUISER, HullSize.CAPITAL_SHIP}; 
		
		float damW = 87f;
		float empW = 87f;
		float fluxW = 87f;
		float sizeW = width - damW - empW - fluxW - 10f;
		tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
				   20f, true, true, 
				   new Object [] {"Ship size", sizeW, "Damage", damW, "EMP", empW, "Flux cost", fluxW});
		
		for (HullSize size : sizes) {
			float damage = getDamage(size);
			float emp = getEMPDamage(size);
			float fluxCost = getFluxCost(size);
			
			Color c = Misc.getGrayColor();
			if (size == hullSize || Global.CODEX_TOOLTIP_MODE) {
				c = Misc.getHighlightColor();
			}
			tooltip.addRow(Alignment.MID, c, Misc.getHullSizeStr(size),
					   	   Alignment.MID, c, "" + (int) Math.round(damage),
					   	   Alignment.MID, c, "" + (int) Math.round(emp),
					   	   Alignment.MID, c, "" + "" + (int) Math.round(fluxCost));
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
}














