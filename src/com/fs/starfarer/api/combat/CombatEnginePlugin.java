package com.fs.starfarer.api.combat;


/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface CombatEnginePlugin {
	
	/**
	 * Deprecated, not guaranteed to be called before advance() is called for
	 * an EveryFrameCombatPlugin. Can still be relied on if the
	 * EveryFrameCombatPlugin.advance() method checks for any fields being set in
	 * init() being null. 
	 * @param engine
	 */
	@Deprecated public void init(CombatEngineAPI engine);
}
