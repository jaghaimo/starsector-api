package com.fs.starfarer.api.combat;


public interface GuidedMissileAI {
	CombatEntityAPI getTarget();
	
	/**
	 * Missile should switch to following target if this method is called.
	 * Called as a result of flares causing distractions and such.
	 * @param target
	 */
	void setTarget(CombatEntityAPI target);
}
