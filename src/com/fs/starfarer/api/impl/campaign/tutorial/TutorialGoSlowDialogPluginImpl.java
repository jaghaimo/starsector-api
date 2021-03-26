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
import com.fs.starfarer.api.util.Misc;

public class TutorialGoSlowDialogPluginImpl implements InteractionDialogPlugin {

	public static enum OptionId {
		INIT,
		CONT1,
		CONT2,
		;
	}
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;
	
	public TutorialGoSlowDialogPluginImpl() {
	}

	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		
		//visual.showImagePortion("illustrations", "jump_point_hyper", 640, 400, 0, 0, 480, 300);
		visual.showFleetInfo("Your fleet", playerFleet, null, null);
	
		//dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		
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
			//textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
			dialog.addOptionSelectedText(option);
		}
		
		
		String control = Global.getSettings().getControlStringForEnumName("GO_SLOW");
		
		switch (option) {
		case INIT:
			textPanel.addParagraph("You're coming up on an asteroid belt!");
			
			textPanel.addParagraph("If you go through at full speed, there's a chance your fleet may be knocked off course, " +
								  "and some of your ships might even suffer damage from asteroid impacts.");
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT1, null);
			break;
		case CONT1:
			textPanel.addPara("A slow-moving fleet avoids this danger. Press and hold %s to move slowly, until " +
					"you're through the asteroid belt.", 
					Misc.getHighlightColor(), control);

			textPanel.addPara("The danger is low, however - especially since the belt is thin, " +
							  "and you're only going across it - so you could also get away with just going full speed.");
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT2, null);
			break;
		case CONT2:
			Global.getSector().setPaused(false);
			dialog.dismiss();
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



