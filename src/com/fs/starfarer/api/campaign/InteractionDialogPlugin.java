package com.fs.starfarer.api.campaign;

import java.util.Map;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

public interface InteractionDialogPlugin {
	void init(InteractionDialogAPI dialog);
	void optionSelected(String optionText, Object optionData);
	void optionMousedOver(String optionText, Object optionData);
	
	void advance(float amount);
	

	void backFromEngagement(EngagementResultAPI battleResult);
	
	Object getContext();
	
	Map<String, MemoryAPI> getMemoryMap();
}
