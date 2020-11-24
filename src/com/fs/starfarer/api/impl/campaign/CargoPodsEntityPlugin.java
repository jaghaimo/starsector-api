package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.util.Misc;

public class CargoPodsEntityPlugin extends BaseCustomEntityPlugin {

	public static float computeDetectionRange(float radius) {
		float range = 500f + radius * 20f;
		if (range > 2000) range = 2000;
		return range;
	}
	
	
	//protected CustomCampaignEntityAPI entity;
	protected transient GenericFieldItemManager manager;
	
	protected float elapsed = 0;
	protected float maxDays = 1f;
	protected float extraDays = 0f;
	protected Boolean neverExpire = null; 
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		entity.setDetectionRangeDetailsOverrideMult(0.5f);
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
		//manager.numPieces = 15;
		
		return this;
	}
	
	public void advance(float amount) {
		if (entity.isInCurrentLocation()) {
			float days = Global.getSector().getClock().convertToDays(amount);
			elapsed += days;
			
			if (!isNeverExpire()) {
				if (elapsed >= maxDays + extraDays && maxDays >= 0) {
					VisibilityLevel vis = entity.getVisibilityLevelToPlayerFleet();
					boolean playerCanSee = entity.isInCurrentLocation() && 
											(vis == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS ||
											 vis == VisibilityLevel.COMPOSITION_DETAILS);
					if (!playerCanSee) {
						maxDays = -1;
						Misc.fadeAndExpire(entity);
						neverExpire = true;
					}
				}
			}
			
			updateBaseMaxDays();
			float radius = 10f + 10f * (float) Math.sqrt(manager.numPieces);
			
			float range = computeDetectionRange(radius);
			entity.getDetectedRangeMod().modifyFlat("gen", range);
		}
		
		manager.advance(amount);
	}
	
	public void updateBaseMaxDays() {
		if (entity == null || entity.getCargo() == null) return;
		
		float totalCapacity = entity.getCargo().getSpaceUsed() + 
							  entity.getCargo().getFuel() + 
							  entity.getCargo().getTotalPersonnel();

		int minPieces = 5;
		int numPieces = (int) (Math.sqrt(totalCapacity) / 1);
		if (numPieces < minPieces) numPieces = minPieces;
		if (numPieces > 40) numPieces = 40;

		boolean cryo = entity.getCargo().getTotalPersonnel() > entity.getCargo().getSpaceUsed() + entity.getCargo().getFuel();
		if (cryo) {
			entity.setCustomDescriptionId("cryopods");
			entity.setName("Cryo Pods");
		} else {
			entity.setCustomDescriptionId(Entities.CARGO_PODS);
			entity.setName("Cargo Pods");
		}

		manager.numPieces = numPieces;
		
		float radius = 10f + 10f * (float) Math.sqrt(manager.numPieces - (minPieces - 1));
		((CustomCampaignEntityAPI)entity).setRadius(radius);

		maxDays = 5f + (numPieces - minPieces);
	}

	public float getRenderRange() {
		return entity.getRadius() + 100f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		manager.render(layer, viewport);
	}

	public void setNeverExpire(Boolean neverExpire) {
		this.neverExpire = neverExpire;
	}

	public Boolean isNeverExpire() {
		return neverExpire != null && neverExpire;
	}
	
	public float getDaysLeft() {
		return maxDays + extraDays - elapsed;
	}

	
	public float getElapsed() {
		return elapsed;
	}

	public void setElapsed(float elapsed) {
		this.elapsed = elapsed;
	}

	public float getExtraDays() {
		return extraDays;
	}

	public void setExtraDays(float extraDays) {
		this.extraDays = extraDays;
	}
	
}



