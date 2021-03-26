package com.fs.starfarer.api.characters;

public interface LevelBasedEffect {
	public String getEffectDescription(float level);
	public String getEffectPerLevelDescription();
	
	
	public static enum ScopeDescription {
		PILOTED_SHIP,
		ALL_SHIPS,
		ALL_COMBAT_SHIPS,
		ALL_CARRIERS,
		//ALL_SHIPS_WITH_FIGHTER_BAYS,
		ALL_FIGHTERS,
		SHIP_FIGHTERS,
		GOVERNED_OUTPOST,
		ALL_OUTPOSTS,
		FLEET,
		CUSTOM,
		NONE,
	}
	
	public ScopeDescription getScopeDescription();
}
