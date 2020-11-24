package com.fs.starfarer.api.campaign;

import java.awt.Color;
import java.util.Random;

import org.json.JSONException;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface SpecialItemPlugin {
	
	public interface SpecialItemRendererAPI {
		void renderSchematic(SpriteAPI sprite, float cx, float cy, float alphaMult);
		void renderScanlines(SpriteAPI sprite, float cx, float cy, float alphaMult);
		void renderScanlinesWithCorners(float blX, float blY, float tlX, float tlY,
										float trX, float trY, float brX, float brY,
										float alphaMult, boolean additive);
		void renderSchematicWithCorners(SpriteAPI sprite, Color color, 
									   float blX, float blY, float tlX, float tlY,
									   float trX, float trY, float brX, float brY,
									   float alphaMult);
		void renderShipWithCorners(String hullOrWingId, Color bgColor,
				float blX, float blY, float tlX, float tlY, float trX,
				float trY, float brX, float brY, float alphaMult, float glowMult, boolean withSpotlight);
		void renderBGWithCorners(Color bgColor, float blX, float blY,
				float tlX, float tlY, float trX, float trY, float brX,
				float brY, float alphaMult, float glowMult, boolean additive);
		void renderWeaponWithCorners(String weaponId, float blX, float blY,
				float tlX, float tlY, float trX, float trY, float brX,
				float brY, float alphaMult, float glowMult, boolean withSpotlight);

	}
	
	/**
	 * Called before init(). init() may not be called if there's no stack.
	 * @param id
	 */
	void setId(String id);
	void init(CargoStackAPI stack);
	
	String getName();
	int getPrice(MarketAPI market, SubmarketAPI submarket);
	
	boolean hasRightClickAction();
	void performRightClickAction();
	boolean shouldRemoveOnRightClickAction();
	
	boolean isTooltipExpandable();
	float getTooltipWidth();
	void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource);
	
	void render(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemRendererAPI renderer);
	
	/**
	 * Return null to have this item turn into nothing, or an empty string if it has no parameters.
	 * @param params
	 * @return
	 * @throws JSONException
	 */
	String resolveDropParamsToSpecificItemData(String params, Random random) throws JSONException;
	String getDesignType();
}








