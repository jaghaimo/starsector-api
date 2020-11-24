package com.fs.starfarer.api.campaign;

import java.util.Map;

import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;


public interface RuleBasedDialog {
	void notifyActivePersonChanged();
	void setActiveMission(CampaignEventPlugin mission);
	
	void updateMemory();
	
	void reinit(boolean withContinueOnRuleFound);
	Map<String, MemoryAPI> getMemoryMap();
}
