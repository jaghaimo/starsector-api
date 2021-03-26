package com.fs.starfarer.api.campaign;

import java.awt.Color;

import org.lwjgl.util.vector.Vector3f;

public interface PlanetSpecAPI {
	String getName();
	float getTilt();
	float getPitch();
	float getRotation();
	Color getPlanetColor();
	float getAtmosphereThickness();
	Color getAtmosphereColor();
	boolean isStar();
	String getPlanetType();
	float getAtmosphereThicknessMin();
	Vector3f getLightPosition();
	float getCloudRotation();
	//float getCloudOffset();
	Color getIconColor();
	Color getCloudColor();
	
	void setTilt(float tilt);
	void setPitch(float pitch);
	void setRotation(float rotation);
	void setPlanetColor(Color planetColor);
	void setCloudRotation(float cloudRotation);
	void setAtmosphereThickness(float atmosphereThickness);
	void setAtmosphereThicknessMin(float atmosphereThicknessMin);
	void setAtmosphereColor(Color atmosphereColor);
	void setCloudColor(Color cloudColor);
	void setIconColor(Color iconColor);
	//void setCloudOffset(float cloudOffset);

	float getCoronaSize();
	void setCoronaSize(float coronaSize);
	Color getCoronaColor();
	void setCoronaColor(Color coronaColor);
	
	/**
	 * Use SettingsAPI.getSpriteName(String category, String id) to get the texture name to pass in here.
	 * The texture needs to already be loaded (which textures from settings.json will be).
	 * Do NOT just pass in a filename for a texture that's not already loaded. 
	 * @return
	 */
	String getCloudTexture();
	void setCloudTexture(String textureName);
	
	/**
	 * Use SettingsAPI.getSpriteName(String category, String id) to get the texture name to pass in here.
	 * The texture needs to already be loaded (which textures from settings.json will be).
	 * Do NOT just pass in a filename for a texture that's not already loaded. 
	 * @return
	 */
	void setTexture(String texture);
	String getTexture();
	
	String getCoronaTexture();
	
	/**
	 * Use SettingsAPI.getSpriteName(String category, String id) to get the texture name to pass in here.
	 * The texture needs to already be loaded (which textures from settings.json will be).
	 * Do NOT just pass in a filename for a texture that's not already loaded. 
	 * @return
	 */
	void setCoronaTexture(String coronaTexture);
	
	/**
	 * Use SettingsAPI.getSpriteName(String category, String id) to get the texture name to pass in here.
	 * The texture needs to already be loaded (which textures from settings.json will be).
	 * Do NOT just pass in a filename for a texture that's not already loaded. 
	 * @return
	 */	
	void setGlowTexture(String glowTexture);
	
	String getGlowTexture();
	Color getGlowColor();
	void setGlowColor(Color glowColor);
	boolean isUseReverseLightForGlow();
	void setUseReverseLightForGlow(boolean useReverseLightForGlow);
	String getIconTexture();
	String getAOrAn();
	boolean isBlackHole();
	void setBlackHole(boolean isBlackHole);
	boolean isNebulaCenter();
	void setNebulaCenter(boolean isNebulaCenter);
	float getScaleMultMapIcon();
	void setScaleMultMapIcon(float scaleMultMapIcon);
	float getScaleMultStarscapeIcon();
	void setScaleMultStarscapeIcon(float scaleMultStarscapeIcon);
	String getStarscapeIcon();
	void setStarscapeIcon(String starscapeIcon);
	boolean isPulsar();
	void setPulsar(boolean isPulsar);
	
	float getShieldThickness();
	void setShieldThickness(float shieldThickness);
	String getShieldTexture();
	void setShieldTexture(String shieldTexture);
	Color getShieldColor();
	void setShieldColor(Color shieldColor);
	
	String getShieldTexture2();
	void setShieldTexture2(String shieldTexture2);
	float getShieldThickness2();
	void setShieldThickness2(float shieldThickness2);
	Color getShieldColor2();
	void setShieldColor2(Color shieldColor2);
	boolean isDoNotShowInCombat();
	void setDoNotShowInCombat(boolean doNotShowInCombat);

}
	
	
