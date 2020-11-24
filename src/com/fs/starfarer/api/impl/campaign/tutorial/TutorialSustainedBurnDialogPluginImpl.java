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

public class TutorialSustainedBurnDialogPluginImpl implements InteractionDialogPlugin {

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
	
	public TutorialSustainedBurnDialogPluginImpl(MarketAPI ancyra) {
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
			textPanel.addParagraph(name + " is pretty far away, and it'll take a while to get there at this rate.");
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT1, null);
			break;
		case CONT1:
			AbilitySpecAPI ability = Global.getSettings().getAbilitySpec(Abilities.SUSTAINED_BURN);
			textPanel.addPara("The %s ability is useful for long-distance travel. " +
					"Activating it will briefly stop the fleet and reduce its acceleration to a minimum, " +
					"but the maximum burn level will be much higher. A sustained burn can be interrupted " +
					"by other fleets activating an interdiction pulse.",
							  Misc.getHighlightColor(),
							  "\"" + ability.getName() + "\"");
			
			Global.getSector().getCharacterData().addAbility(ability.getId());
			AbilitySlotsAPI slots = Global.getSector().getUIData().getAbilitySlotsAPI();
			slots.setCurrBarIndex(0);
			
			int slotIndex = 4;
			slots.getCurrSlotsCopy().get(slotIndex).setAbilityId(ability.getId());
			AddRemoveCommodity.addAbilityGainText(ability.getId(), textPanel);
			
			Global.getSector().getCharacterData().addAbility(Abilities.INTERDICTION_PULSE);
			slots.getCurrSlotsCopy().get(6).setAbilityId(Abilities.INTERDICTION_PULSE);
			AddRemoveCommodity.addAbilityGainText(Abilities.INTERDICTION_PULSE, textPanel);
			
			options.clearOptions();
			options.addOption("Continue", OptionId.CONT2, null);
			
			break;
		case CONT2:
			ability = Global.getSettings().getAbilitySpec(Abilities.SUSTAINED_BURN);
			
			String control = Global.getSettings().getControlStringForEnumName("FAST_FORWARD");
			
			textPanel.addPara("Activate %s to get to " + name + " more quickly.",
					Misc.getHighlightColor(),
					"\"" + ability.getName() + "\"");
			
			textPanel.addPara("You can also press and hold %s to speed up time.",
					Misc.getHighlightColor(),
					"\"" + control + "\"");
			
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



