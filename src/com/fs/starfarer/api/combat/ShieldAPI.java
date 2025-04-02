package com.fs.starfarer.api.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface ShieldAPI {
	
	public static enum ShieldType {NONE, FRONT, OMNI, PHASE}

	void setType(ShieldType type);
	ShieldType getType();
	
	float getFacing();
	
	
	/**
	 * @return maximum arc.
	 */
	float getArc();
	/**
	 * @return currently open arc (0 if not on)
	 */
	float getActiveArc();
	void setActiveArc(float activeArc);
	float getRadius();
	boolean isOn();
	boolean isOff();
	
	/**
	 * @return location of the center of the shield, in engine coordinates.
	 */
	Vector2f getLocation();
	
	boolean isWithinArc(Vector2f point);
	
	void toggleOff();
	
	/**
	 * Does not include shield damage taken mult, but does include absorbption mult.
	 * @return
	 */
	float getFluxPerPointOfDamage();
	
	/**
	 * Set the maximum arc.
	 * @param arc
	 */
	void setArc(float arc);
	
	
	void setInnerColor(Color color);
	void setRingColor(Color ringColor);
	Color getInnerColor();
	Color getRingColor();
	
	/**
	 * Flux/second while shield is on.
	 * @return
	 */
	float getUpkeep();
	void forceFacing(float facing);
	
	void setRadius(float radius);
	
	/**
	 * Textures should be already loaded (i.e. via settings.json).
	 * @param radius
	 * @param textureInner
	 * @param textureRing
	 */
	void setRadius(float radius, String textureInner, String textureRing);
	void toggleOn();
	float getUnfoldTime();
	void setCenter(float x, float y);
	
	/**
	 * For shield textures, visual only.
	 * @return
	 */
	float getInnerRotationRate();
	/**
	 * For shield textures, visual only.
	 * @return
	 */
	void setInnerRotationRate(float innerRotationRate);
	/**
	 * For shield textures, visual only.
	 * @return
	 */
	float getRingRotationRate();
	/**
	 * For shield textures, visual only.
	 * @return
	 */
	void setRingRotationRate(float ringRotationRate);
	boolean isSkipRendering();
	void setSkipRendering(boolean skipRendering);
	void applyShieldEffects(Color innerColor, Color ringColor, float thicknessBonus, float fluctuationBonus, float effectLevel);
	
	
	
}
