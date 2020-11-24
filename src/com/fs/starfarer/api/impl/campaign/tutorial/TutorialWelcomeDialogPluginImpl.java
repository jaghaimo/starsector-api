package com.fs.starfarer.api.impl.campaign.tutorial;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI.AbilitySlotsAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.util.Misc;

public class TutorialWelcomeDialogPluginImpl implements InteractionDialogPlugin {

	public static enum OptionId {
		INIT,
		CONT1,
		CONT2,
		//LEAVE,
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
			textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
		}
		
		switch (option) {
		case INIT:
			textPanel.addParagraph("Welcome to the Sector! " +
								   "Your fleet is in the middle of nowhere and critically low on supplies.");
	
			textPanel.addParagraph("If you don't acquire more supplies, " +
						   		   "your fleet will suffer through a slow but ultimately fatal decline.");
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT1, null);
			break;
		case CONT1:
			
			AbilitySpecAPI ability = Global.getSettings().getAbilitySpec(Abilities.SCAVENGE);
			textPanel.addPara("Fortunately, there's a debris field nearby. " +
							  "Move up into it and activate your %s ability to search it for useful cargo.",
							  Misc.getHighlightColor(),
							  "\"" + ability.getName() + "\"");

			textPanel.addParagraph("It's possible to scavenge through the same debris field multiple times, but there are diminishing returns and increased risk with each attempt. Only scavenge once here.");
			
			Global.getSector().getCharacterData().addAbility(ability.getId());
			AbilitySlotsAPI slots = Global.getSector().getUIData().getAbilitySlotsAPI();
			slots.setCurrBarIndex(0);
			
			int slotIndex = 5;
			slots.getCurrSlotsCopy().get(slotIndex).setAbilityId(ability.getId());
			AddRemoveCommodity.addAbilityGainText(ability.getId(), textPanel);
			
			textPanel.addParagraph("Make sure to take all of the supplies and any other valuable cargo, but feel free to leave the cheap and bulky metals behind.");
			textPanel.addParagraph("To get your fleet moving, click on empty space in the direction you want to move.");
			
			options.clearOptions();
			options.addOption("Finish", OptionId.CONT2, null);
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



