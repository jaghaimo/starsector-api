package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.graphics.SpriteAPI;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface BattleObjectiveAPI extends CombatEntityAPI, AssignmentTargetAPI {
	public static enum Importance {
		//MINOR,
		//IMPORTANT,
		//CRITICAL,
		NORMAL,
	}
	
	
	public Vector2f getLocation();
	public int getOwner();
	public String getType();
	public Importance getImportance();
	public String getDisplayName();
	
	SpriteAPI getSprite();
	void setSprite(SpriteAPI sprite);
	float getBattleSizeFractionBonus();
}
