package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Highlights;

public interface CampaignMissionPlugin {
	String getId();
	
	String getName();
	
	String getPostingStage();
	
	void playerAccept(SectorEntityToken entity);
	
	void advance(float amount);
	
	CampaignEventPlugin getPrimedEvent();
	PersonAPI getImportantPerson();
	long getCreationTimestamp();
	
	
	/**
	 * ID of the faction offering this mission.
	 * @return
	 */
	String getFactionId();
	
	
	boolean showAcceptTooltipNextToButton();
	boolean canPlayerAccept();
	String getAcceptTooltip();
	Highlights getAcceptTooltipHighlights();

	/**
	 * Called when mission is removed from board with withCleanup = true 
	 */
	void cleanup();
}
