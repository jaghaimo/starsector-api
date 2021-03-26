package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.EnumSet;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.loading.TerrainSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface CampaignTerrainPlugin {
	void init(String terrainId, SectorEntityToken entity, Object param);
	
	String getTerrainId();
	
	/**
	 * How far away from the viewport the center of this entity can be before it stops being rendered.
	 * Should at least be the radius of the entity; sometimes more may be necessary depending on the
	 * visual effect desired.
	 * @return
	 */
	float getRenderRange();
	
	
	/**
	 * Set of layers can not change.
	 * @return
	 */
	EnumSet<CampaignEngineLayers> getActiveLayers();
	void render(CampaignEngineLayers layer, ViewportAPI viewport);
	
	void advance(float amount);
	
	/**
	 * First pass.
	 * @param factor conversion factor from world coordinates to map screen coordinates, including current zoom level.
	 * @param alphaMult
	 */
	void renderOnMap(float factor, float alphaMult);
	
	/**
	 * Second pass.
	 * @param factor conversion factor from world coordinates to map screen coordinates, including current zoom level.
	 * @param alphaMult
	 */
	void renderOnMapAbove(float factor, float alphaMult);
	
	
	boolean containsEntity(SectorEntityToken other);
	boolean containsPoint(Vector2f point, float radius);
	
	boolean hasMapIcon();
	
	boolean hasTooltip();
	void createTooltip(TooltipMakerAPI tooltip, boolean expanded);
	boolean isTooltipExpandable();
	float getTooltipWidth();
	String getTerrainName();
	//String getTerrainNameLowerCase();
	Color getNameColor();
	
	boolean canPlayerHoldStationIn();
	
	TerrainSpecAPI getSpec();
	
	
	boolean hasAIFlag(Object flag);
	boolean hasAIFlag(Object flag, CampaignFleetAPI fleet);
	
	/**
	 * Mainly intended for AI use.
	 * @param locFrom
	 * @return
	 */
	float getMaxEffectRadius(Vector2f locFrom);
	
	/**
	 * Mainly intended for AI use.
	 * @param locFrom
	 * @return
	 */
	float getMinEffectRadius(Vector2f locFrom);
	
	/**
	 * Mainly intended for AI use.
	 * @param locFrom
	 * @return
	 */
	float getOptimalEffectRadius(Vector2f locFrom);

	void setTerrainName(String name);

	String getIconSpriteName();

	void renderOnRadar(Vector2f radarCenter, float factor, float alphaMult);

	String getNameAOrAn();

	String getNameForTooltip();
}




