package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ViewportAPI;

public class SupplyCacheEntityPlugin extends BaseCustomEntityPlugin {

//	private CustomCampaignEntityAPI entity;
	private transient GenericFieldItemManager manager;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		//this.entity = (CustomCampaignEntityAPI) entity;
		readResolve();
	}
	
	Object readResolve() {
		manager = new GenericFieldItemManager(entity);
		manager.category = "misc";
		manager.key = "cargoPods";
		manager.cellSize = 32;
		
		manager.minSize = 10;
		manager.maxSize = 10;
		
		//manager.initDebrisIfNeeded();
		//manager.numPieces = 15;
		
		return this;
	}
	
	public void advance(float amount) {
		if (entity.isInCurrentLocation()) {
			float totalCapacity = entity.getRadius();
			int minPieces = 5;
			int numPieces = (int) (totalCapacity / 4);
			if (numPieces < minPieces) numPieces = minPieces;
			if (numPieces > 40) numPieces = 40;
			
			manager.numPieces = numPieces;
		}
		
		manager.advance(amount);
	}

	public float getRenderRange() {
		return entity.getRadius() + 100f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		manager.render(layer, viewport);
	}

}



