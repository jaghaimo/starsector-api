package com.fs.starfarer.api.campaign.ai;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;

public interface FleetAssignmentDataAPI {

	boolean isExpired();
	
	void expire();
	String getActionText();
	void setActionText(String actionText);
	Script getOnCompletion();
	SectorEntityToken getTarget();
	FleetAssignment getAssignment();
	float getMaxDurationInDays();
	float getElapsedDays();
	
	@Deprecated boolean isExipred();

	Object getCustom();
	void setCustom(Object custom);

}