package com.fs.starfarer.api.impl.campaign.tutorial;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

public class TutorialRespawnDialogPluginImpl implements InteractionDialogPlugin {

	public static enum OptionId {
		INIT,
		LOAD,
		EXIT,
		;
	}
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		
		visual.showImagePortion("illustrations", "space_wreckage", 640, 400, 0, 0, 480, 300);
		
		optionSelected(null, OptionId.INIT);
	}
	
	public Map<String, MemoryAPI> getMemoryMap() {
		return null;
	}
	
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
		}
		
		switch (option) {
		case INIT:
			textPanel.addParagraph("Your fleet has been defeated!");
			
			textPanel.addParagraph("Normally, if this happens you and your remaining crew will " +
					"spend some time \"behind the scenes\" assembling a smaller fleet of recovered " +
					"ships.");
	
			textPanel.addParagraph("During the course of this tutorial, the only available option is to reload the last save.");
			
			options.clearOptions();
			options.addOption("Load last save", OptionId.LOAD, null);
			options.addOption("Exit to main menu", OptionId.EXIT, null);
			break;
		case LOAD:
			
			dialog.dismiss();
			Global.getSector().getCampaignUI().quickLoad();
			break;
		case EXIT:
			dialog.dismiss();
			Global.getSector().getCampaignUI().cmdExitWithoutSaving();
			break;
		}
	}
	

	
	
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	public Object getContext() {
		return null;
	}
}



