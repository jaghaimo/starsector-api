package com.fs.starfarer.api.campaign;

import java.awt.Color;

import org.lwjgl.util.vector.Vector3f;



public interface PlanetAPI extends SectorEntityToken {
	String getTypeId();
	
	boolean isStar();
	/**
	 * Star, not a black hole or a pulsar or a nebula center.
	 * @return
	 */
	boolean isNormalStar();
	boolean isGasGiant();
	boolean isMoon();
	
	
	/**
	 * The object returned by this method can be changed to control how this specific
	 * planet looks, without affecting other planets of the same type.
	 * 
	 * applySpecChanges() must be called for the changes to take effect.
	 * @return
	 */
	PlanetSpecAPI getSpec();
	
	/**
	 * Applies any changes made using getSpec().setXXX to the planet's graphics.
	 */
	void applySpecChanges();
	
	void setRadius(float radius);

	Color getLightColorOverrideIfStar();

	void setLightColorOverrideIfStar(Color lightColorOverrideIfStar);

	String getTypeNameWithWorld();

	String getTypeNameWithLowerCaseWorld();

	void setTypeId(String typeId);

	boolean hasCondition(String id);

	void setSecondLight(Vector3f location, Color color);

	String getTypeNameWithWorldLowerCase();
}
