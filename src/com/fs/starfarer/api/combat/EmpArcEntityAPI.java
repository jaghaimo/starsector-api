package com.fs.starfarer.api.combat;

import java.awt.Color;

import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

public interface EmpArcEntityAPI extends CombatEntityAPI {

	public static class EmpArcParams {
		/**
		 * Setting to a really high value produces straight lines,
		 * higher values = more performant but also more angular-looking.
		 */
		public float segmentLengthMult = 1f;
		
		/**
		 * 0.25f is pretty reasonable to reduce how far out it bends. Increases the volatility of 
		 * the zig-zagging, reducing the extremes it gets to.
		 */
		public float zigZagReductionFactor = 0f;
		
		public float maxZigZagMult = 1f;
		
		/**
		 * Only used if arc.setFadedOutAtStart(true).
		 */
		public float fadeOutDist = 100f;
		
		/**
		 * Arc fades in over at most 1f/minFadeOutMult of its length.
		 */
		public float minFadeOutMult = 2f;
		
		public float flickerRateMult = 1f;
		public float glowSizeMult = 1f;
		public float glowAlphaMult = 1f;
		
		/**
		 * Only used if arc.setSingleFlickerMode(true).
		 * Not supported for arcs from an EMP-type ship system, only those spawned using CombatEngineAPI, i.e.
		 * where it's possible to call setSingleFlickerMode().
		 */
		public float movementDurOverride = -1f; // defaults to value based on flicker duration
		public float movementDurMax = 0.1f;
		public float movementDurMin = 0f;
		
		/**
		 * How large the moving bright area is, if arc.setSingleFlickerMode(true).
		 */
		public float brightSpotFullFraction = 0.33f;
		public float brightSpotFadeFraction = 0.33f;
		public float nonBrightSpotMinBrightness = 0f;
		public Color glowColorOverride = null;
		
		public boolean flamesOutMissiles = true;
		
		public void loadFromSystemJson(JSONObject json) {
			if (json == null) return;
			segmentLengthMult = (float) json.optDouble("emp_segmentLengthMult", 1f);
			zigZagReductionFactor = (float) json.optDouble("emp_zigZagReductionFactor", 0f);
			maxZigZagMult = (float) json.optDouble("emp_maxZigZagMult", 1f);
			fadeOutDist = (float) json.optDouble("emp_fadeOutDist", 100f);
			minFadeOutMult = (float) json.optDouble("emp_minFadeOutMult", 2f);
			flickerRateMult = (float) json.optDouble("emp_flickerRateMult", 1f);
			glowSizeMult = (float) json.optDouble("emp_glowSizeMult", 1f);
			glowAlphaMult = (float) json.optDouble("emp_glowAlphaMult", 1f);
			flamesOutMissiles = json.optBoolean("emp_flamesOutMissiles", true);
		}
		
	}
	
	
	float getCoreWidthOverride();
	void setCoreWidthOverride(float coreWidthOverride);
	void setTargetToShipCenter(Vector2f sourceSlotPos, ShipAPI ship);
	Vector2f getTargetLocation();
	void setSingleFlickerMode();
	void setUpdateFromOffsetEveryFrame(boolean updateFromOffsetEveryFrame);
	void setRenderGlowAtStart(boolean renderGlowAtStart);
	void setRenderGlowAtEnd(boolean renderGlowAtEnd);
	
	/**
	 * Makes the rendering MUCH slower, use with caution.
	 * @param fadedOutAtStart
	 */
	void setFadedOutAtStart(boolean fadedOutAtStart);
//	void setDelay(float delay);
//	float getDelay();
	void setSingleFlickerMode(boolean withMovement);
	void setLayer(CombatEngineLayers layer);
	void setWarping(float dur);
	boolean isShieldHit();

}
