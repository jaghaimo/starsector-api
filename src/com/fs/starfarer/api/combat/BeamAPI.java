package com.fs.starfarer.api.combat;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.graphics.SpriteAPI;


public interface BeamAPI {
	
	Vector2f getFrom();
	Vector2f getTo();
	
	WeaponAPI getWeapon();
	ShipAPI getSource();
	
	boolean didDamageThisFrame();
	CombatEntityAPI getDamageTarget();
	
	float getBrightness();
	
	
	void setHitGlow(SpriteAPI sprite);
	SpriteAPI getHitGlow();
	float getHitGlowBrightness();
	
	float getWidth();
	void setWidth(float width);
	
	float getPixelsPerTexel();
	void setPixelsPerTexel(float pixelsPerTexel);
	
	Color getCoreColor();
	void setCoreColor(Color coreColor);
	Color getFringeColor();
	void setFringeColor(Color fringeColor);
	
	DamageAPI getDamage();
	float getHitGlowRadius();
	
	void setFringeTexture(String spriteName);
	void setCoreTexture(String spriteName);
	float getLength();
	Vector2f getRayEndPrevFrame();
	float getLengthPrevFrame();
}




