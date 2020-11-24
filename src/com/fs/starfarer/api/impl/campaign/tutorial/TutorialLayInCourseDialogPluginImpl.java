package com.fs.starfarer.api.impl.campaign.tutorial;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;

public class TutorialLayInCourseDialogPluginImpl implements InteractionDialogPlugin {

	public static enum OptionId {
		INIT,
		CONT1,
		CONT2,
		CONT3,
		;
	}
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;
	
	protected MarketAPI ancyra;
	protected PersonAPI contact;
	
	public TutorialLayInCourseDialogPluginImpl(MarketAPI ancyra, PersonAPI contact) {
		this.ancyra = ancyra;
		this.contact = contact;
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
			textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
		}
		
		
		String name = ancyra.getName();
		
		String personName = contact.getPost().toLowerCase() + " " + contact.getName().getLast();
		
		switch (option) {
		case INIT:
			textPanel.addParagraph("Shortly after dispatching the pirates, you receive a tight-beam communication from the " +
					"system's main inhabited world, " + name + ".");
			
			textPanel.addParagraph("The message is brief and asks you to travel there and contact " + personName + " as soon as possible.");
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT1, null);
			break;
		case CONT1:
			textPanel.addParagraph("Let's lay in a course for " + name + ". You don't need to do this to travel, " +
					"but it helps keep track of where you're going and how long it'll take to get there.");

			
			String intel = Global.getSettings().getControlStringForEnumName("CORE_INTEL");
			textPanel.addPara("After dismissing this dialog, press %s to open the intel screen to view the " +
					"details of the message you've just received.",
					Misc.getHighlightColor(), intel);
			
			textPanel.addPara("Select the message and click on the %s button to open the map centered directly on Ancyra. " +
					"Then, left-click-and-hold on the planet, and select %s from the menu that pops up. Alternatively, you can just right-click on Ancyra.",
					Misc.getHighlightColor(), "\"Show on map\"", "\"Lay in Course\"");
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT2, null);
			break;
		case CONT2:
			String map = Global.getSettings().getControlStringForEnumName("CORE_MAP");
			textPanel.addPara("You could also press %s to open the map and locate Ancyra manually.",
					Misc.getHighlightColor(), map);
			
			
			textPanel.addParagraph("Once you get to Ancyra, open the comm directory to contact " + personName + ".");
			
			options.clearOptions();
			options.addOption("Finish", OptionId.CONT3, null);
			break;
		case CONT3:
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



