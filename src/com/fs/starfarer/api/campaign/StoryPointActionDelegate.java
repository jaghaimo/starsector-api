/**
 * 
 */
package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface StoryPointActionDelegate {
	String getTitle();
	boolean withDescription();
	boolean withSPInfo();
	void createDescription(TooltipMakerAPI info);
	float getBonusXPFraction();
	TextPanelAPI getTextPanel();
	void preConfirm();
	void confirm();
	String getConfirmSoundId();
	int getRequiredStoryPoints();
	String getLogText();
}