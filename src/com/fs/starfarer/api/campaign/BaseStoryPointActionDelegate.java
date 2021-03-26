package com.fs.starfarer.api.campaign;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

public abstract class BaseStoryPointActionDelegate implements StoryPointActionDelegate {

	public void preConfirm() {
		
	}
	
	public void confirm() {
		
	}

	public void createDescription(TooltipMakerAPI info) {
		
	}

	public float getBonusXPFraction() {
		return 0f;
	}

	public String getConfirmSoundId() {
		return "ui_char_spent_story_point";
	}

	public TextPanelAPI getTextPanel() {
		return null;
	}

	public String getTitle() {
		return null;
	}

	public int getRequiredStoryPoints() {
		return 1;
	}

	public boolean withDescription() {
		return true;
	}

	public boolean withSPInfo() {
		return true;
	}

//	public String getLogText() {
//		return null;
//	}

	
}
