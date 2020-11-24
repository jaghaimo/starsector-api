package com.fs.starfarer.api.characters;

public interface LevelBasedEffect {
	public String getEffectDescription(float level);
	public String getEffectPerLevelDescription();
	
	
	public static enum ScopeDescription {
		PILOTED_SHIP,
		ALL_SHIPS,
		ALL_FIGHTERS,
		SHIP_FIGHTERS,
		GOVERNED_OUTPOST,
		ALL_OUTPOSTS,
		FLEET,
		NONE,
	}
	
	public ScopeDescription getScopeDescription();
}
