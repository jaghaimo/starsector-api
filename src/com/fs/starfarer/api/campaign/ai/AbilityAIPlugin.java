package com.fs.starfarer.api.campaign.ai;

import com.fs.starfarer.api.characters.AbilityPlugin;

public interface AbilityAIPlugin {

	void init(AbilityPlugin ability);
	
	void advance(float days);

}
