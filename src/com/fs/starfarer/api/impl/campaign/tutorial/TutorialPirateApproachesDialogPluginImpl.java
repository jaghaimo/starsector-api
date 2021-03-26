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

public class TutorialPirateApproachesDialogPluginImpl implements InteractionDialogPlugin {

	public static enum OptionId {
		INIT,
		CONT1,
		CONT2,
		CONT3,
		CONT4,
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
		
		
		switch (option) {
		case INIT:
			textPanel.addParagraph("A pirate fleet is approaching! First you'll spot it as a sensor contact, " +
					"then as an unidentified fleet, and then - when it gets very close - you'll see its true colors.");
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT1, null);
			break;
		case CONT1:
			
			String load = Global.getSettings().getControlStringForEnumName("QUICK_LOAD");

			textPanel.addPara("Don't worry - the pirate ship is a shoddy rust bucket, " +
					"and if you do lose, you can press %s to quick-load.",
					Misc.getHighlightColor(), load);
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT2, null);
			break;
		case CONT2:
			textPanel.addParagraph("Even so, combat can be expensive, especially if there's no bounty on the enemy you fight. " +
					"Deploying ships into battle reduces their combat readiness, and recovering CR consumes supplies. " +
					"Battle damage can cost even more supplies to repair.");
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT3, null);
			break;
		case CONT3:
			textPanel.addParagraph("However, fighting is often necessary to survive. Wait for the pirate fleet to approach, then defeat them!");
			
			options.clearOptions();
			options.addOption("Finish", OptionId.CONT4, null);
			break;
		case CONT4:
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



