package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Misc;

public class HiddenCacheEntityPlugin extends BaseCustomEntityPlugin {

	private AsteroidAPI asteroid;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		readResolve();
	}
	
	Object readResolve() {
		return this;
	}
	
	protected void createAsteroidIfNeeded() {
		if (asteroid != null) return;
		if (entity.getContainingLocation() == null) return;
		
		asteroid = entity.getContainingLocation().addAsteroid(16f);
		asteroid.setLocation(entity.getLocation().x, entity.getLocation().y);
		asteroid.setFacing(Misc.random.nextFloat() * 360f);
		entity.getContainingLocation().removeEntity(asteroid);
//		float orbitDays = 100f;
//		asteroid.setCircularOrbit(this.entity, 0f, 0f, orbitDays);
//		asteroid.addTag(Tags.NON_CLICKABLE);
//		asteroid.addTag(Tags.NO_ENTITY_TOOLTIP);
	}
	
	public void advance(float amount) {
		if (entity.isInCurrentLocation()) {
			createAsteroidIfNeeded();
			if (asteroid != null) {
				asteroid.advance(amount);
			}
		}
	}

	public float getRenderRange() {
		return entity.getRadius() + 100f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		if (asteroid != null) {
			asteroid.setLocation(entity.getLocation().x, entity.getLocation().y);
			asteroid.setLightSource(entity.getLightSource(), entity.getLightColor());
			asteroid.forceRender();
		}
	}

}





