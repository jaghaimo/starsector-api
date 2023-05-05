package com.fs.starfarer.api.campaign.listeners;

import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StoryPointActionDelegate;

public interface DialogCreatorUI {

	/**
	 * target can be null.
	 * @param target
	 * @param plugin
	 */
	void showDialog(SectorEntityToken target, InteractionDialogPlugin plugin);

	
	/**
	 * target can be null.
	 * @param target
	 * @param trigger
	 */
	void showDialog(SectorEntityToken target, String trigger);

	void showDialog(StoryPointActionDelegate delegate);


	void showDialog(float customPanelWidth, float customPanelHeight, CustomDialogDelegate delegate);

	
}
