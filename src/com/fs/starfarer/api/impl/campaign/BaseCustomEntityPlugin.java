package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CustomCampaignEntityPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class BaseCustomEntityPlugin implements CustomCampaignEntityPlugin {

	protected SectorEntityToken entity;
	
	public void init(SectorEntityToken entity, Object pluginParams) {
		this.entity = entity;
	}
	
	public void advance(float amount) {
		
	}

	public float getRenderRange() {
		return entity.getRadius() + 100f;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		
	}
	
	public boolean hasCustomMapTooltip() {
		return false;
	}
	
	public float getMapTooltipWidth() {
		return 300f;
	}
	
	public boolean isMapTooltipExpandable() {
		return false;
	}
	
	public void createMapTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		
	}
	
	public void appendToCampaignTooltip(TooltipMakerAPI tooltip, VisibilityLevel level) {
		
	}

//	@Override
//	public boolean isRenderWhenViewportAlphaMultIsZero() {
//		return false;
//	}
}



