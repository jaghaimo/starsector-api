package com.fs.starfarer.api.impl.campaign.terrain;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;

public class AsteroidFieldTerrainPlugin extends AsteroidBeltTerrainPlugin {
	
	public static class AsteroidFieldParams extends RingParams {
		public float minRadius;
		public float maxRadius;
		public int minAsteroids;
		public int maxAsteroids;
		public float minSize;
		public float maxSize;
		public int numAsteroids;
		public AsteroidFieldParams(float minRadius, float maxRadius,
				int minAsteroids, int maxAsteroids, float minSize,
				float maxSize, String name) {
			super(maxRadius, maxRadius/2f, null, name);
			this.minRadius = minRadius;
			this.maxRadius = maxRadius;
			this.minAsteroids = minAsteroids;
			this.maxAsteroids = maxAsteroids;
			this.minSize = minSize;
			this.maxSize = maxSize;
		}
	}
	
	//float size = 8f + (float) Math.random() * 25f;
	public AsteroidFieldParams params;
	public void init(String terrainId, SectorEntityToken entity, Object param) {
		super.init(terrainId, entity, param);
		params = (AsteroidFieldParams) param;
		name = params.name;
		if (name == null) {
			name = "Asteroid Field";
		}
		params.numAsteroids = params.minAsteroids;
		if (params.maxAsteroids > params.minAsteroids) {
			params.numAsteroids += new Random().nextInt(params.maxAsteroids - params.minAsteroids);
		}
	}
	
	
	private transient SpriteAPI icon = null;
	@Override
	public void renderOnMap(float factor, float alphaMult) {
//		if (params == null) return;
//		if (icon == null) {
//			icon = Global.getSettings().getSprite("systemMap", "asteroid_field");
//		}
//		
//		float size = 64;
//		icon.setSize(size, size);
//		icon.setAlphaMult(alphaMult);
//		Vector2f loc = entity.getLocation();
//		icon.renderAtCenter(loc.x * factor, loc.y * factor);
	}



	public void regenerateAsteroids() {
		createAsteroidField();
	}
	
	//protected boolean needToCreateAsteroidField = true;
	protected void createAsteroidField() {
		if (!(params instanceof AsteroidFieldParams)) return;
		
		Random rand = new Random(Global.getSector().getClock().getTimestamp() + entity.getId().hashCode());
		
		float fieldRadius = params.minRadius + (params.maxRadius - params.minRadius) * rand.nextFloat();
		params.bandWidthInEngine = fieldRadius;
		params.middleRadius = fieldRadius / 2f;
		
		
		LocationAPI location = entity.getContainingLocation();
		if (location == null) return;
		for (int i = 0; i < params.numAsteroids; i++) {
			float size = params.minSize + (params.maxSize - params.minSize) * rand.nextFloat();
			AsteroidAPI asteroid = location.addAsteroid(size);
			asteroid.setFacing(rand.nextFloat() * 360f);
			
			float r = rand.nextFloat();
			r = 1f - r * r;
			
			float currRadius = fieldRadius * r;
			
			float minOrbitDays = Math.max(1f, currRadius * 0.05f);
			float maxOrbitDays = Math.max(2f, currRadius * 2f * 0.05f);
			float orbitDays = minOrbitDays + rand.nextFloat() * (maxOrbitDays - minOrbitDays);
			
			float angle = rand.nextFloat() * 360f;
			asteroid.setCircularOrbit(this.entity, angle, currRadius, orbitDays);
			Misc.setAsteroidSource(asteroid, this);
		}
		needToCreateAsteroids = false;
	}
	
	public void advance(float amount) {
		if (needToCreateAsteroids) {
			createAsteroidField();
		}
		super.advance(amount);
	}
		
	public String getNameForTooltip() {
		return "Asteroid Field";
		//return params.name;
	}

	@Override
	public void reportAsteroidPersisted(SectorEntityToken asteroid) {
		if (Misc.getAsteroidSource(asteroid) == this) {
			params.numAsteroids--;
		}
	}
	
	
}



