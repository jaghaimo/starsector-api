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
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.util.Misc;

public class TutorialTransponderDialogPluginImpl implements InteractionDialogPlugin {

	public static enum OptionId {
		INIT,
		CONT1,
		CONT2,
		CONT3,
		CONT4,
		CONT5,
		CONT6,
		;
	}
	
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI textPanel;
	protected OptionPanelAPI options;
	protected VisualPanelAPI visual;
	
	protected CampaignFleetAPI playerFleet;
	
	protected MarketAPI ancyra;
	
	public TutorialTransponderDialogPluginImpl(MarketAPI ancyra) {
		this.ancyra = ancyra;
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
		
		switch (option) {
		case INIT:
			textPanel.addParagraph("Your fleet is getting closer to " + name + ", which is controlled by the Hegemony - " +
					"a major militaristic faction in the Sector.");
			
			textPanel.addParagraph("While in Hegemony space, a fleet is required by law to identify itself by keeping its transponder turned on. " +
					"This is a view shared by most, though not all, major factions.");
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT1, null);
			break;
		case CONT1:
			textPanel.addParagraph("Turning on the transponder makes your fleet highly visible, " +
					"and everyone seeing it will know who you are - unlike that pirate fleet you fought earlier, " +
					"which you had to be very close to to positively identify.");
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT2, null);
			break;
		case CONT2:
			textPanel.addParagraph("Keeping your transponder on is a crippling disadvantage in hostile space, " +
					"but as we're getting closer to port and we'd like to dock there, it's a good idea to turn it on.");
			
			AbilitySpecAPI ability = Global.getSettings().getAbilitySpec(Abilities.TRANSPONDER);
			Global.getSector().getCharacterData().addAbility(ability.getId());
			AbilitySlotsAPI slots = Global.getSector().getUIData().getAbilitySlotsAPI();
			slots.setCurrBarIndex(0);
			
			int slotIndex = 0;
			slots.getCurrSlotsCopy().get(slotIndex).setAbilityId(ability.getId());
			AddRemoveCommodity.addAbilityGainText(ability.getId(), textPanel);
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT3, null);
		case CONT3:
			ability = Global.getSettings().getAbilitySpec(Abilities.TRANSPONDER);
			textPanel.addPara("Activate the %s before getting closer to " + ancyra.getName() + ", both to " +
					"avoid unwanted attention from patrols and to receive docking clearance.",
					Misc.getHighlightColor(),
					"\"" + ability.getName() + "\"");
			
			textPanel.addPara("Since turning it on and off has major consequences, " +
					"it requires a double-tap to turn on or off - once to prime, and once more to confirm.",
					Misc.getHighlightColor(),
					"double-tap");
			
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



