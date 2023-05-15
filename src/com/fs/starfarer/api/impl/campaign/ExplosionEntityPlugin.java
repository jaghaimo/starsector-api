package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.ShoveFleetScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class ExplosionEntityPlugin extends BaseCustomEntityPlugin {

	public static enum ExplosionFleetDamage {
		NONE,
		LOW,
		MEDIUM,
		HIGH,
		EXTREME,
	}
	public static class ExplosionParams {
		public Color color;
		public ExplosionFleetDamage damage = ExplosionFleetDamage.NONE;
		public float radius;
		public float durationMult = 1f;
		public Vector2f loc;
		public LocationAPI where;
		public ExplosionParams(Color color, LocationAPI where, Vector2f loc, float radius, float durationMult) {
			this.color = color;
			this.where = where;
			this.loc = loc;
			this.radius = radius;
			this.durationMult = durationMult;
		}
	}

	
	public static class ParticleData {
		public Vector2f offset = new Vector2f();
		public Vector2f vel = new Vector2f();
		public float scale = 1f;
		public float scaleDelta = 1f;
		public float turnDir = 1f;
		public float angle = 1f;
		public float size;
		
		public float maxDur;
		public float elapsed;
		public float swImpact = 1f;
		
		public int i;
		public int j;
		
		public Color color;
		
		public ParticleData(Color color, float size, float maxDur, float endScale) {
			i = Misc.random.nextInt(4);
			j = Misc.random.nextInt(4);
			
			this.color = color;
			this.size = size;
			angle = (float) Math.random() * 360f;
			
			this.maxDur = maxDur;
			scaleDelta = (endScale - 1f) / maxDur;
			scale = 1f;
			
			turnDir = Math.signum((float) Math.random() - 0.5f) * 10f * (float) Math.random();
			
			//turnDir = 0f;
		}
		
		public void setVelocity(float direction, float minSpeed, float maxSpeed) {
			vel = Misc.getUnitVectorAtDegreeAngle(direction);
			vel.scale(minSpeed + (maxSpeed - minSpeed) * (float) Math.random());
		}
		
		public void setOffset(float direction, float minDist, float maxDist) {
			offset = Misc.getUnitVectorAtDegreeAngle(direction);
			offset.scale(minDist + (maxDist - minDist) * (float) Math.random());
		}
		
		public void advance(float amount) {
			scale += scaleDelta * amount;
			if (scale < 0) scale = 0f;
			
			offset.x += vel.x * amount;
			offset.y += vel.y * amount;
			
			angle += turnDir * amount;
			
			elapsed += amount;
		}
		
		public float getBrightness() {
			float b = 1f - (elapsed / maxDur);
			if (b < 0) b = 0;
			if (b > 1) b = 1;
			return b;
		}
	}	
	
	
	protected ExplosionParams params;
	protected List<ParticleData> particles = new ArrayList<ParticleData>();
	
	transient protected SpriteAPI sprite;
	
	protected float shockwaveRadius;
	protected float shockwaveWidth;
	protected float shockwaveSpeed;
	protected float shockwaveDuration;
	protected float shockwaveAccel;
	
	protected float maxParticleSize;
	
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
		
		params = (ExplosionParams) pluginParams;
		
		
		if (params.where.isCurrentLocation()) {
			Global.getSoundPlayer().playSound("gate_explosion", 1f, 1f, params.loc, Misc.ZERO);
		}
		
		float baseSize = params.radius * 0.08f;
		maxParticleSize = baseSize * 2f;
		
		float fullArea = (float) (Math.PI * params.radius * params.radius);
		float particleArea = (float) (Math.PI * baseSize * baseSize);
		
		int count = (int) Math.round(fullArea / particleArea * 1f);
		
		float durMult = 2f;
		durMult = params.durationMult;

		//baseSize *= 0.5f;
		for (int i = 0; i < count; i++) {
			float size = baseSize * (1f + (float) Math.random());
			
			Color randomColor = new Color(Misc.random.nextInt(256), 
						Misc.random.nextInt(256), Misc.random.nextInt(256), params.color.getAlpha());			
			Color adjustedColor = Misc.interpolateColor(params.color, randomColor, 0.2f);
			adjustedColor = params.color;
			ParticleData data = new ParticleData(adjustedColor, size, 
						(0.25f + (float) Math.random()) * 2f * durMult, 3f);
			
			float r = (float) Math.random();
			float dist = params.radius * 0.2f * (0.1f + r * 0.9f);
			float dir = (float) Math.random() * 360f;
			data.setOffset(dir, dist, dist);
			
			dir = Misc.getAngleInDegrees(data.offset);
//			data.setVelocity(dir, baseSize * 0.25f, baseSize * 0.5f);
//			data.vel.scale(1f / durMult);
			
			data.swImpact = (float) Math.random();
			if (i > count / 2) data.swImpact = 1;
			
			particles.add(data);
		}
		
		Vector2f loc = new Vector2f(params.loc);
		loc.x -= params.radius * 0.01f;
		loc.y += params.radius * 0.01f;
		
		float b = 1f;
		params.where.addHitParticle(loc, new Vector2f(), params.radius * 1f, b, 1f * durMult, params.color);
		loc = new Vector2f(params.loc);
		params.where.addHitParticle(loc, new Vector2f(), params.radius * 0.4f, 0.5f, 1f * durMult, Color.white);
		
		shockwaveAccel = baseSize * 70f / durMult;
		//shockwaveRadius = -1500f;
		shockwaveRadius = 0f;
		shockwaveRadius = -params.radius * 0.5f;
		shockwaveSpeed = params.radius * 2f / durMult;
		shockwaveDuration = params.radius * 2f / shockwaveSpeed;
		shockwaveWidth = params.radius * 0.5f;
		
//		shockwaveAccel = baseSize * 1500f / durMult;
//		//shockwaveRadius = -1500f;
//		shockwaveRadius = 0f;
//		shockwaveSpeed = params.radius * 4f / durMult;
//		shockwaveDuration = params.radius * 2f / shockwaveSpeed;
//		shockwaveWidth = params.radius * 0.5f;
		
//		shockwaveAccel = baseSize * 10f / durMult;
//		//shockwaveRadius = -1500f;
//		shockwaveRadius = 0f;
//		shockwaveSpeed = params.radius * 0.2f / durMult;
//		shockwaveDuration = params.radius * 2f / shockwaveSpeed;
//		shockwaveWidth = params.radius * 0.4f;
	}
	
	Object readResolve() {
		sprite = Global.getSettings().getSprite("misc", "nebula_particles");
		return this;
	}
	

	public void advance(float amount) {
		for (ParticleData p : new ArrayList<ParticleData>(particles)) {
			p.advance(amount);
			if (p.elapsed >= p.maxDur) {
				particles.remove(p);
			}
		}
		if (particles.isEmpty()) {
			entity.setExpired(true);
		}
		
		applyDamageToFleets();

		if (shockwaveDuration > 0) {
			shockwaveRadius += shockwaveSpeed * amount;
			//shockwaveSpeed -= amount * shockwaveSpeed * 5f;
			if (shockwaveSpeed < 0) shockwaveSpeed = 0;
			shockwaveDuration -= amount;
			for (ParticleData p : particles) {
				float dist = p.offset.length();
				
				float impact = 0f;
				if (dist < shockwaveRadius && dist > shockwaveRadius - shockwaveWidth) {
					impact = 1f - (shockwaveRadius - dist) / shockwaveWidth;
					impact = -impact;
				} else if (dist > shockwaveRadius && dist < shockwaveRadius + shockwaveWidth) {
					impact = 1f - (dist - shockwaveRadius) / shockwaveWidth;
				}
				
				float speed = p.vel.length();
				float dot = Vector2f.dot(p.offset, p.vel);
				float threshold = shockwaveSpeed * 0.5f;
				if (speed > threshold) {// && dot > 0) {
					impact *= threshold / speed;
				}
				if (dot < 0) {
					impact *= 0.2f;
				}
				//impact *= 0.1f + 0.9f * (1f - p.size / maxParticleSize);
				impact *= p.swImpact;
				
				Vector2f accel = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(p.offset));
				accel.scale(impact * shockwaveAccel);
				p.vel.x += accel.x * amount;
				p.vel.y += accel.y * amount;
			}
		}
		
	}
	

	public float getRenderRange() {
		float extra = 2000f;
		if (params != null) extra = params.radius * 3;
		return entity.getRadius() + extra;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		alphaMult *= entity.getSensorFaderBrightness();
		alphaMult *= entity.getSensorContactFaderBrightness();
		if (alphaMult <= 0) return;
		
		float x = entity.getLocation().x;
		float y = entity.getLocation().y;
		
		//Color color = params.color;
		//color = Misc.setAlpha(color, 30);
		float b = alphaMult;
		
		sprite.setTexWidth(0.25f);
		sprite.setTexHeight(0.25f);
		sprite.setAdditiveBlend();
		
		for (ParticleData p : particles) {
			float size = p.size;
			size *= p.scale;
			
			Vector2f loc = new Vector2f(x + p.offset.x, y + p.offset.y);
			
			float a = 1f;
			a = 0.33f;
			
			sprite.setTexX(p.i * 0.25f);
			sprite.setTexY(p.j * 0.25f);
			
			sprite.setAngle(p.angle);
			sprite.setSize(size, size);
			sprite.setAlphaMult(b * a * p.getBrightness());
			sprite.setColor(p.color);
			sprite.renderAtCenter(loc.x, loc.y);
		}
	}
	
	protected LinkedHashSet<String> damagedAlready = new LinkedHashSet<String>();
	public void applyDamageToFleets() {
		if (params.damage == null || params.damage == ExplosionFleetDamage.NONE) {
			return;
		}
		
		float shockwaveDist = 0f;
		for (ParticleData p : particles) {
			shockwaveDist = Math.max(shockwaveDist, p.offset.length());
		}
		
		for (CampaignFleetAPI fleet : entity.getContainingLocation().getFleets()) {
			String id = fleet.getId();
			if (damagedAlready.contains(id)) continue;
			float dist = Misc.getDistance(fleet, entity);
			if (dist < shockwaveDist) {
				float damageMult = 1f - (dist / params.radius);
				if (damageMult > 1f) damageMult = 1f;
				if (damageMult < 0.1f) damageMult = 0.1f;
				if (dist < entity.getRadius() + params.radius * 0.1f) damageMult = 1f;
				
				damagedAlready.add(id);
				applyDamageToFleet(fleet, damageMult);
			}
		}
		
	}
	
	public void applyDamageToFleet(CampaignFleetAPI fleet, float damageMult) {
		
		List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
		if (members.isEmpty()) return;
		
		float totalValue = 0;
		for (FleetMemberAPI member : members) {
			totalValue += member.getStats().getSuppliesToRecover().getModifiedValue();
		}
		if (totalValue <= 0) return;
		
		
		float damageFraction = 0f;
		switch (params.damage) {
		case NONE:
			return;
		case LOW:
			damageFraction = 0.1f;
			break;
		case MEDIUM:
			damageFraction = 0.3f;
			break;
		case HIGH:
			damageFraction = 0.6f;
			break;
		case EXTREME:
			damageFraction = 0.9f;
			break;
		}
		
		damageFraction *= damageMult;
		
		float shoveDir = Misc.getAngleInDegrees(entity.getLocation(), fleet.getLocation());
		fleet.addScript(new ShoveFleetScript(fleet, shoveDir, damageFraction));
		
		if (fleet.isInCurrentLocation()) {
			float dist = Misc.getDistance(fleet, Global.getSector().getPlayerFleet());
			if (dist < HyperspaceTerrainPlugin.STORM_STRIKE_SOUND_RANGE) {
				float volumeMult = 0.5f + 0.5f * damageFraction;
				Global.getSoundPlayer().playSound("gate_explosion_fleet_impact", 1f, volumeMult, fleet.getLocation(), Misc.ZERO);
			}
		}
		
		//float strikeValue = totalValue * damageFraction * (0.5f + (float) Math.random() * 0.5f);
		
		WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<FleetMemberAPI>();
		for (FleetMemberAPI member : members) {
			float w = 1f;
			if (member.isFrigate()) w *= 0.1f;
			if (member.isDestroyer()) w *= 0.2f;
			if (member.isCruiser()) w *= 0.5f;
			picker.add(member, w);
		}
		
		int numStrikes = picker.getItems().size();
		
		for (int i = 0; i < numStrikes; i++) {
			FleetMemberAPI member = picker.pick();
			if (member == null) return;
			
			float crPerDep = member.getDeployCost();
			//if (crPerDep <= 0) continue;			
			float suppliesPerDep = member.getStats().getSuppliesToRecover().getModifiedValue();
			if (suppliesPerDep <= 0 || crPerDep <= 0) return;
			float suppliesPer100CR = suppliesPerDep * 1f / Math.max(0.01f, crPerDep);

			// half flat damage, half scaled based on ship supply cost cost
			float strikeSupplies = (250f + suppliesPer100CR) * 0.5f * damageFraction;  
			//strikeSupplies = suppliesPerDep * 0.5f * damageFraction;  
			
			float strikeDamage = strikeSupplies / suppliesPer100CR * (0.75f + (float) Math.random() * 0.5f);
			
			//float strikeDamage = damageFraction * (0.75f + (float) Math.random() * 0.5f);
			
			float resistance = member.getStats().getDynamic().getValue(Stats.CORONA_EFFECT_MULT);
			strikeDamage *= resistance;
			
			if (strikeDamage > HyperspaceTerrainPlugin.STORM_MAX_STRIKE_DAMAGE) {
				strikeDamage = HyperspaceTerrainPlugin.STORM_MAX_STRIKE_DAMAGE;
			}
			
			if (strikeDamage > 0) {
				float currCR = member.getRepairTracker().getBaseCR();
				float crDamage = Math.min(currCR, strikeDamage);
				
				if (crDamage > 0) {
					member.getRepairTracker().applyCREvent(-crDamage, "explosion_" + entity.getId(),
														"Damaged by explosion");
				}
				
				float hitStrength = member.getStats().getArmorBonus().computeEffective(member.getHullSpec().getArmorRating());
				//hitStrength *= strikeDamage / crPerDep;
				int numHits = (int) (strikeDamage / 0.1f);
				if (numHits < 1) numHits = 1;
				for (int j = 0; j < numHits; j++) {
					member.getStatus().applyDamage(hitStrength);
				}
				//member.getStatus().applyHullFractionDamage(1f);
				if (member.getStatus().getHullFraction() < 0.01f) {
					member.getStatus().setHullFraction(0.01f);
					picker.remove(member);
				} else {
					float w = picker.getWeight(member);
					picker.setWeight(picker.getItems().indexOf(member), w * 0.5f);
				}
			}
			//picker.remove(member);
		}
		
		if (fleet.isPlayerFleet()) {
			Global.getSector().getCampaignUI().addMessage(
					"Your fleet suffers damage from being caught in an explosion", Misc.getNegativeHighlightColor());
		}
	}

}









