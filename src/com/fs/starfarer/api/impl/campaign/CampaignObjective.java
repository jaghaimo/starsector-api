package com.fs.starfarer.api.impl.campaign;

import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface CampaignObjective {
	void printEffect(TooltipMakerAPI text, float pad);
	void addHackStatusToTooltip(TooltipMakerAPI text, float pad);
	void printNonFunctionalAndHackDescription(TextPanelAPI text);
	

	Boolean isHacked();
	void setHacked(boolean hacked);
	void setHacked(boolean hacked, float days);
	
	Boolean isReset();
	void setReset(boolean reset);
	void setReset(boolean reset, float days);

}
