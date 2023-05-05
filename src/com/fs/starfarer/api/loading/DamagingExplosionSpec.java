package com.fs.starfarer.api.loading;

import java.awt.Color;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class DamagingExplosionSpec implements Cloneable {

	private float duration;
	private float radius;
	private float coreRadius;
	private float maxDamage;
	private float minDamage;
	
	private float particleSpawnRadius = -1f;
	
	private CollisionClass collisionClass;
	private CollisionClass collisionClassByFighter;
	
	
	private float particleSizeMin = 10f;
	private float particleSizeRange = 10f;
	private float particleDuration = 0.5f;
	private int particleCount = 10;
	
	private Color particleColor = new Color(255,255,255,255);
	private Color explosionColor = new Color(255,255,255,255);

	private DamageType damageType = DamageType.ENERGY;
	private String soundSetId;
	
	private boolean showGraphic = true;
	private boolean useDetailedExplosion = true;
	
	private float detailedExplosionRadius = -1f;
	private float detailedExplosionFlashRadius = -1f;
	private Color detailedExplosionFlashColorFringe = null;
	private Color detailedExplosionFlashColorCore = null;
	private float detailedExplosionFlashDuration = -1f;
	
	private OnHitEffectPlugin effect;
	
	private MutableStat modifier = new MutableStat(1f);
	
	public static float getShipExplosionRadius(ShipAPI ship) {
		float mult = ship.getMutableStats().getDynamic().getValue(Stats.EXPLOSION_RADIUS_MULT);
		//float mult = 1f;
//		if (ship.isStationModule()) {
//			mult = 0.5f;
//		}
		float radius = ship.getCollisionRadius() + Math.min(200f, ship.getCollisionRadius()) * mult;
		return radius;
	}
	
	public static DamagingExplosionSpec explosionSpecForShip(ShipAPI ship) {
		float radius = getShipExplosionRadius(ship);
		float coreRadius = ship.getCollisionRadius();
		
		float maxDamage = ship.getMaxFlux() * (0.5f + 0.5f * (float) Math.random());
		float minDamage = 0f;

		float mult = ship.getMutableStats().getDynamic().getValue(Stats.EXPLOSION_DAMAGE_MULT);
		maxDamage *= mult;
		
		DamagingExplosionSpec spec = new DamagingExplosionSpec(0.1f, 
															  radius,
															  coreRadius,
															  maxDamage,
															  minDamage,
															  CollisionClass.HITS_SHIPS_AND_ASTEROIDS,
															  CollisionClass.HITS_SHIPS_AND_ASTEROIDS, // irrelevant - no explosion for fighters
															  10f,
															  10f,
															  1f,
															  0,
															  Color.white, Color.white);
		spec.setShowGraphic(false);
		spec.setDamageType(DamageType.HIGH_EXPLOSIVE);
		return spec;
	}
	
	public OnHitEffectPlugin getEffect() {
		return effect;
	}

	public void setEffect(OnHitEffectPlugin effect) {
		this.effect = effect;
	}

	public String getSoundSetId() {
		return soundSetId;
	}
	public void setSoundSetId(String soundSetId) {
		this.soundSetId = soundSetId;
	}

	public static DamagingExplosionSpec loadFromJSON(JSONObject explosionSpecJson) throws JSONException {
		if (explosionSpecJson != null) {
			DamagingExplosionSpec spec = new DamagingExplosionSpec(
					(float) explosionSpecJson.getDouble("duration"),
					(float) explosionSpecJson.getDouble("radius"),
					(float) explosionSpecJson.getDouble("coreRadius"),
					(float) explosionSpecJson.optDouble("maxDamage", 0),
					(float) explosionSpecJson.optDouble("minDamage", 0),
					Misc.mapToEnum(explosionSpecJson, "collisionClass", CollisionClass.class, null),
					Misc.mapToEnum(explosionSpecJson, "collisionClassByFighter", CollisionClass.class, null),					
					(float) explosionSpecJson.optDouble("particleSizeMin", 0),
					(float) explosionSpecJson.optDouble("particleSizeRange", 0),
					(float) explosionSpecJson.optDouble("particleDuration", 0),
					explosionSpecJson.optInt("particleCount", 0),
					Misc.getColor(explosionSpecJson, "particleColor"),
					Misc.optColor(explosionSpecJson, "explosionColor", null)
			);

			spec.useDetailedExplosion = explosionSpecJson.optBoolean("useDetailedExplosion", false);
			
			spec.detailedExplosionRadius = (float) explosionSpecJson.optDouble("detailedExplosionRadius", -1f);
			spec.detailedExplosionFlashRadius = (float) explosionSpecJson.optDouble("detailedExplosionFlashRadius", -1f);
			spec.detailedExplosionFlashDuration = (float) explosionSpecJson.optDouble("detailedExplosionFlashDuration", -1f);
			spec.detailedExplosionFlashColorFringe = Misc.optColor(explosionSpecJson, "detailedExplosionFlashColorFringe", null);
			spec.detailedExplosionFlashColorCore = Misc.optColor(explosionSpecJson, "detailedExplosionFlashColorCore", null);
			
			spec.particleSpawnRadius = (float) explosionSpecJson.optDouble("particleSpawnRadius", -1f);
			
			
			if (spec.getParticleCount() == 0) {
				spec.setShowGraphic(false);
			}
			spec.setSoundSetId(explosionSpecJson.optString("sound", null));
			return spec;
		}
		return null;
	}

	public DamagingExplosionSpec(float duration, float radius, float coreRadius,
								float maxDamage, float minDamage, CollisionClass collisionClass,
								CollisionClass collisionClassByFighter,
								float particleSizeMin, float particleSizeRange,
								float particleDuration, int particleCount, Color particleColor, Color explosionColor) {
		this.duration = duration;
		this.radius = radius;
		this.coreRadius = coreRadius;
		this.maxDamage = maxDamage;
		this.minDamage = minDamage;
		this.collisionClass = collisionClass;
		this.collisionClassByFighter = collisionClassByFighter;
		this.particleSizeMin = particleSizeMin;
		this.particleSizeRange = particleSizeRange;
		this.particleDuration = particleDuration;
		this.particleCount = particleCount;
		this.particleColor = particleColor;
		if (explosionColor != null) {
			this.explosionColor = explosionColor;
		} else {
			this.explosionColor = particleColor;
		}
	}
	
	public float getParticleSpawnRadius() {
		return particleSpawnRadius;
	}

	public void setParticleSpawnRadius(float particleSpawnRadius) {
		this.particleSpawnRadius = particleSpawnRadius;
	}

	public Color getExplosionColor() {
		return explosionColor;
	}
	
	public void setExplosionColor(Color explosionColor) {
		this.explosionColor = explosionColor;
	}

	public DamageType getDamageType() {
		return damageType;
	}

	public void setDamageType(DamageType damageType) {
		this.damageType = damageType;
	}

	public float getDuration() {
		return duration;
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public float getMaxDamage() {
		return maxDamage;
	}

	public void setMaxDamage(float maxDamage) {
		this.maxDamage = maxDamage;
	}

	public float getMinDamage() {
		return minDamage;
	}

	public void setMinDamage(float minDamage) {
		this.minDamage = minDamage;
	}

	public CollisionClass getCollisionClass() {
		return collisionClass;
	}

	public void setCollisionClass(CollisionClass collisionClass) {
		this.collisionClass = collisionClass;
	}

	public CollisionClass getCollisionClassIfByFighter() {
		return collisionClassByFighter;
	}

	public void setCollisionClassByFighter(CollisionClass collisionClassByFighter) {
		this.collisionClassByFighter = collisionClassByFighter;
	}

	public float getParticleSizeMin() {
		return particleSizeMin;
	}

	public void setParticleSizeMin(float particleSizeMin) {
		this.particleSizeMin = particleSizeMin;
	}

	public float getParticleSizeRange() {
		return particleSizeRange;
	}

	public void setParticleSizeRange(float particleSizeRange) {
		this.particleSizeRange = particleSizeRange;
	}

	public float getParticleDuration() {
		return particleDuration;
	}

	public void setParticleDuration(float particleDuration) {
		this.particleDuration = particleDuration;
	}

	public int getParticleCount() {
		return particleCount;
	}

	public void setParticleCount(int particleCount) {
		this.particleCount = particleCount;
	}

	public Color getParticleColor() {
		return particleColor;
	}

	public void setParticleColor(Color particleColor) {
		this.particleColor = particleColor;
	}

	public float getCoreRadius() {
		return coreRadius;
	}

	public void setCoreRadius(float coreRadius) {
		this.coreRadius = coreRadius;
	}


	public boolean isShowGraphic() {
		return showGraphic;
	}


	public void setShowGraphic(boolean showGraphic) {
		this.showGraphic = showGraphic;
	}

	@Override
	public DamagingExplosionSpec clone() {
		try {
			return (DamagingExplosionSpec) super.clone();
		} catch (CloneNotSupportedException e) {
			return null; // can't happen
		}
	}

	public boolean isUseDetailedExplosion() {
		return useDetailedExplosion;
	}

	public void setUseDetailedExplosion(boolean useDetailedExplosion) {
		this.useDetailedExplosion = useDetailedExplosion;
	}

	public CollisionClass getCollisionClassByFighter() {
		return collisionClassByFighter;
	}

	public MutableStat getModifier() {
		return modifier;
	}

	public void setModifier(MutableStat modifier) {
		this.modifier = modifier;
	}

	public float getDetailedExplosionRadius() {
		return detailedExplosionRadius;
	}

	public void setDetailedExplosionRadius(float detailedExplosionRadius) {
		this.detailedExplosionRadius = detailedExplosionRadius;
	}

	public float getDetailedExplosionFlashRadius() {
		return detailedExplosionFlashRadius;
	}

	public void setDetailedExplosionFlashRadius(float detailedExplosionFlashRadius) {
		this.detailedExplosionFlashRadius = detailedExplosionFlashRadius;
	}

	public Color getDetailedExplosionFlashColorFringe() {
		return detailedExplosionFlashColorFringe;
	}

	public void setDetailedExplosionFlashColorFringe(Color detailedExplosionFlashColorFringe) {
		this.detailedExplosionFlashColorFringe = detailedExplosionFlashColorFringe;
	}

	public Color getDetailedExplosionFlashColorCore() {
		return detailedExplosionFlashColorCore;
	}

	public void setDetailedExplosionFlashColorCore(Color detailedExplosionFlashColorCore) {
		this.detailedExplosionFlashColorCore = detailedExplosionFlashColorCore;
	}

	public float getDetailedExplosionFlashDuration() {
		return detailedExplosionFlashDuration;
	}

	public void setDetailedExplosionFlashDuration(float detailedExplosionFlashDuration) {
		this.detailedExplosionFlashDuration = detailedExplosionFlashDuration;
	}
	
	
}
