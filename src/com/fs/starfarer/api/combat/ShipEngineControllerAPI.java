package com.fs.starfarer.api.combat;

import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.util.ColorShifterAPI;
import com.fs.starfarer.api.util.ValueShifterAPI;

public interface ShipEngineControllerAPI {
	public interface ShipEngineAPI {
		/**
		 * @return location, in absolute coordinates.
		 */
		Vector2f getLocation();
		
		/**
		 * @return whether this engine is currently engaged (some engines are only "active" when a ship system is in use, for example.)
		 */
		boolean isActive();
		
		/**
		 * @return whether this engine is only shown when the ship system is active.
		 */
		boolean isSystemActivated();
		
		String getStyleId();

		boolean isDisabled();
		void disable();
		void disable(boolean permanent);
		
		/**
		 * Fraction of total engine power this engine provides.
		 * @return
		 */
		float getContribution();

		boolean isPermanentlyDisabled();
		void applyDamage(float damAmount, Object source);
		float getMaxHitpoints();
		float getHitpoints();
		EngineSlotAPI getEngineSlot();

		void setHitpoints(float hp);

		Color getEngineColor();

		Color getContrailColor();
	}
	
	
	boolean isAccelerating();
	boolean isAcceleratingBackwards();
	boolean isDecelerating();
	boolean isTurningLeft();
	boolean isTurningRight();
	boolean isStrafingLeft();
	boolean isStrafingRight();
	
	
	List<ShipEngineAPI> getShipEngines();
	void fadeToOtherColor(Object key, Color other, Color contrailColor, float effectLevel, float maxBlend);
	void extendFlame(Object key, float extendLengthFraction, float extendWidthFraction, float extendGlowFraction);
	void forceFlameout();
	void forceFlameout(boolean suppressFloaty);
	float getMaxSpeedWithoutBoost();
	float computeDisabledFraction();
	float getFlameoutFraction();
	void computeEffectiveStats(boolean forceShowFloaty);
	boolean isFlamedOut();
	boolean isDisabled();
	boolean isFlamingOut();
	
	/**
	 * How extended the engine flame is. 1.0 = maximum, 0 = not at all, 0.4 = default idle level.
	 * @param slot
	 * @param level
	 */
	void setFlameLevel(EngineSlotAPI slot, float level);
	ValueShifterAPI getExtendLengthFraction();
	ValueShifterAPI getExtendWidthFraction();
	ValueShifterAPI getExtendGlowFraction();
	void forceShowAccelerating();
	ColorShifterAPI getFlameColorShifter();
	
}
