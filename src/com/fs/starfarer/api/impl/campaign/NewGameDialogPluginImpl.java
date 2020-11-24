package com.fs.starfarer.api.impl.campaign;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;

public class NewGameDialogPluginImpl implements InteractionDialogPlugin {

	private static enum OptionId {
		INIT,
		CONTINUE_CHOICES,
		LEAVE,
	}
	
	private static enum State {
		OPTIONS,
		CHOICES,
	}
	
	private InteractionDialogAPI dialog;
	private TextPanelAPI textPanel;
	private OptionPanelAPI options;
	private VisualPanelAPI visual;
	
	private CharacterCreationData data;
	private SectorEntityToken entity;
	
	private State state = State.OPTIONS;
	private HashMap<String, MemoryAPI> memoryMap;
	private MemoryAPI memory;
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		createInitialOptions();
		
		entity = dialog.getInteractionTarget();
		memory = entity.getMemoryWithoutUpdate();
		data = (CharacterCreationData) memory.get("$characterData");
		memoryMap = new HashMap<String, MemoryAPI>();
		memoryMap.put(MemKeys.LOCAL, memory);
		memoryMap.put(MemKeys.GLOBAL, Global.getFactory().createMemory());
		if (Global.getSettings().isDevMode()) {
			memoryMap.get(MemKeys.GLOBAL).set("$isDevMode", true, 0);
		}
		
		//dialog.setPromptText("------------------------------");
		dialog.setPromptText("-");
		
		dialog.hideTextPanel();
		visual.showNewGameOptionsPanel(data);
		//optionSelected(null, OptionId.INIT);
	}
	
	public void advance(float amount) {
		if (state == State.OPTIONS) {
			String name = data.getCharacterData().getName();
			if (name == null || name.isEmpty()) {
				options.setEnabled(OptionId.CONTINUE_CHOICES, false);
			} else {
				options.setEnabled(OptionId.CONTINUE_CHOICES, true);
			}
		} else if (state == State.CHOICES) {
			if (data.isDone()) {
				dialog.dismiss();
			}
		}
	}
	
	public Map<String, MemoryAPI> getMemoryMap() {
		return memoryMap;
	}
	
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null) return;
		
		if (text != null && state == State.CHOICES) {
			textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
		}
		
		if (optionData instanceof String) {
			if (optionData == DumpMemory.OPTION_ID) {
				new DumpMemory().execute(null, dialog, null, memoryMap);
				return;
			} else if (DevMenuOptions.isDevOption(optionData)) {
				DevMenuOptions.execute(dialog, (String) optionData);
				return;
			}
			
			memory.set("$option", optionData);
			memory.expire("$option", 0);
			fireBest("NewGameOptionSelected");
		} else {
			OptionId option = (OptionId) optionData;
			switch (option) {
			case LEAVE:
				dialog.dismissAsCancel();
				break;
			case CONTINUE_CHOICES:
				dialog.showTextPanel();
				visual.showPersonInfo(data.getPerson(), true);
				options.clearOptions();
				state = State.CHOICES;
				fireBest("BeginNewGameCreation");
				break;
			}
		}
	}
	
	private void createInitialOptions() {
		options.clearOptions();
		boolean dev = Global.getSettings().isDevMode();
		options.addOption("Continue", OptionId.CONTINUE_CHOICES, null);
		//options.addOption("Leave", OptionId.LEAVE, null);
	}
	
	private OptionId lastOptionMousedOver = null;
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public Object getContext() {
		return null;
	}
	
	public boolean fireAll(String trigger) {
		return FireAll.fire(null, dialog, memoryMap, trigger);
	}
	
	public boolean fireBest(String trigger) {
		return FireBest.fire(null, dialog, memoryMap, trigger);
	}
}



