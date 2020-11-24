package com.fs.starfarer.api.combat;


/**
 * NOTE: An implementation of this should almost always also implement GuidedMissileAI, this is required
 * for the missile to be properly affected by flares.
 * 
 * If missile is tracking a target that phases or gets its collision class set to NONE, 
 * the default missile AI will fly in a straight line and continue to accelerate until
 * the target is again trackable, at which point it will start actively tracking it again.
 * It will *not* switch targets when its current target phases/has collision class set to NONE.
 * 
 * Custom guided missile AI implementations don't have to preserve this behavior, but if they
 * intend to mimic the vanilla missile AI, they should.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public interface MissileAIPlugin {
	/**
	 * The AI should do its main work here.
	 * @param amount
	 */
	void advance(float amount);
}
