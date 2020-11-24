package com.fs.starfarer.api.combat;

import org.lwjgl.util.vector.Vector2f;


public interface ShipSystemAIScript {
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine);
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target);
}
