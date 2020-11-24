package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface CustomCampaignEntityPlugin {

	void init(SectorEntityToken entity, Object params);
	
	/**
	 * @param amount in seconds. Use SectorAPI.getClock() to figure out how many campaign days that is.
	 */
	void advance(float amount);
	
	
	/**
	 * Should only render for specified layer. Will be called once per each layer, per frame.
	 * Needs to respect viewport.getAlphaMult() - i.e. use that alpha value for rendering.
	 * 
	 * Needs to render at the entity's location - there's no translation before this method call.
	 * 
	 * If a sprite is specified, it will be rendered in the bottommost layer of the layers this entity renders
	 * for. This method will be called after the sprite has rendered.
	 * 
	 * @param layer
	 * @param viewport
	 */
	void render(CampaignEngineLayers layer, ViewportAPI viewport);
	
	
	/**
	 * How far away from the viewport the center of this entity can be before it stops being rendered.
	 * Should at least be the radius of the entity; sometimes more may be necessary depending on the
	 * visual effect desired.
	 * @return
	 */
	float getRenderRange();

	boolean hasCustomMapTooltip();
	float getMapTooltipWidth();
	boolean isMapTooltipExpandable();
	void createMapTooltip(TooltipMakerAPI tooltip, boolean expanded);
	void appendToCampaignTooltip(TooltipMakerAPI tooltip, VisibilityLevel level);

}
