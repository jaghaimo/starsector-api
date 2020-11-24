package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

public interface ViewportAPI {
	boolean isNearViewport(Vector2f loc, float nearDistance);
	Vector2f getCenter();
	
	/**
	 * @return x coordinate of the lower left corner, in world units.
	 */
	float getLLX();
	/**
	 * @return y coordinate of the lower left corner, in world units.
	 */
	float getLLY();
	/**
	 * @return Visible area's width, in world units.
	 */
	float getVisibleWidth();
	/**
	 * @return Visible area's height, in world units.
	 */
	float getVisibleHeight();
	float getWorldXtoScreenX();
	float getWorldYtoScreenY();
	
	/**
	 * @return Current level zoom multiplier.
	 */
	float getViewMult();
	
	float getAlphaMult();
	float convertScreenXToWorldX(float x);
	float convertScreenYToWorldY(float y);
	float convertWorldXtoScreenX(float x);
	float convertWorldYtoScreenY(float y);
	float convertWorldWidthToScreenWidth(float w);
	float convertWorldHeightToScreenHeight(float h);
	float convertScreenWidthToWorldWidth(float w);
	float convertScreenHeightToWorldHeight(float h);
	
	void set(float llx, float lly, float visibleWidth, float visibleHeight);
	void setViewMult(float zoom);
	
	boolean isExternalControl();
	
	/**
	 * Tells the game not to set the viewport parameters every frame. Allows a mod to override viewport behavior.
	 * @return
	 */
	void setExternalControl(boolean externalControl);
	void setCenter(Vector2f c);
	void setAlphaMult(float alphaMult);
}
