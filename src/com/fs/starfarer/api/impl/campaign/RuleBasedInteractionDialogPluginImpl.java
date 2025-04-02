package com.fs.starfarer.api.impl.campaign;

import java.util.HashMap;
import java.util.Map;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.rules.RulesAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.DismissDialog;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.util.Misc;

/**
 * @author Alex Mosolov
 *
 * Uses the data in data/campaign/rules.csv to drive the dialog interactions.
 *
 * Copyright 2014 Fractal Softworks, LLC
 */
public class RuleBasedInteractionDialogPluginImpl implements InteractionDialogPlugin, RuleBasedDialog {

	public static final String FAILSAFE_LEAVE = "rbid_failsafe_leave";
	
	private InteractionDialogAPI dialog;
	private TextPanelAPI textPanel;
	private OptionPanelAPI options;
	private VisualPanelAPI visual;
	
	private RulesAPI rules;
	private MemoryAPI memory;
	
	private CampaignFleetAPI playerFleet;
	
	private Object custom1;
	private Object custom2;
	private Object custom3;
	
	private static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	private boolean embeddedMode = false;
	public void setEmbeddedMode(boolean embeddedMode) {
		this.embeddedMode = embeddedMode;
	}

	private final String initialTrigger;
	
	public RuleBasedInteractionDialogPluginImpl() {
		this("OpenInteractionDialog");
	}
	public RuleBasedInteractionDialogPluginImpl(String initialTrigger) {
		this.initialTrigger = initialTrigger;
	}


	public void reinit(boolean withContinueOnRuleFound) {
		init(dialog);
	}
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		
		if (!embeddedMode) {
			visual.setVisualFade(0.25f, 0.25f);
		}
		
		rules = Global.getSector().getRules();
		
		updateMemory();
		
		if (!embeddedMode) {
			fireBest(initialTrigger);
			if (!options.hasOptions()) {
				options.clearOptions();
				options.addOption("Leave", FAILSAFE_LEAVE);
				if (Global.getSettings().isDevMode()) {
					DevMenuOptions.addOptions(dialog);
				}
			}
		}
	}
	
	public void updateMemory() {
		if (memoryMap == null) {
			memoryMap = new HashMap<String, MemoryAPI>();
		} else {
			memoryMap.clear();
		}
		memory = dialog.getInteractionTarget().getMemory();
		
		memoryMap.put(MemKeys.LOCAL, memory);
		if (dialog.getInteractionTarget().getFaction() != null) {
			memoryMap.put(MemKeys.FACTION, dialog.getInteractionTarget().getFaction().getMemory());
		} else {
			memoryMap.put(MemKeys.FACTION, Global.getFactory().createMemory());
		}
		memoryMap.put(MemKeys.GLOBAL, Global.getSector().getMemory());
		memoryMap.put(MemKeys.PLAYER, Global.getSector().getCharacterData().getMemory());
		
		if (dialog.getInteractionTarget().getMarket() != null) {
			memoryMap.put(MemKeys.MARKET, dialog.getInteractionTarget().getMarket().getMemory());
		}
		
		if (memory.contains(MemFlags.MEMORY_KEY_SOURCE_MARKET)) {
			String marketId = memory.getString(MemFlags.MEMORY_KEY_SOURCE_MARKET);
			MarketAPI market = Global.getSector().getEconomy().getMarket(marketId);
			if (market != null) {
				memoryMap.put(MemKeys.SOURCE_MARKET, market.getMemory());
			}
		}
		
		updatePersonMemory();
	}
	
	private void updatePersonMemory() {
		PersonAPI person = dialog.getInteractionTarget().getActivePerson();
//		if (person != null) {
//			memoryMap.put(MemKeys.PERSON, person.getMemory());
//		} else {
//			memoryMap.remove(MemKeys.PERSON);
//		}
		if (person != null) {
			memory = person.getMemory();
			memoryMap.put(MemKeys.LOCAL, memory);
			memoryMap.put(MemKeys.PERSON_FACTION, person.getFaction().getMemory());
			memoryMap.put(MemKeys.ENTITY, dialog.getInteractionTarget().getMemory());
		} else {
			memory = dialog.getInteractionTarget().getMemory();
			memoryMap.put(MemKeys.LOCAL, memory);
			memoryMap.remove(MemKeys.ENTITY);
			memoryMap.remove(MemKeys.PERSON_FACTION);
			
		}
	}
	
	
	public void notifyActivePersonChanged() {
		updatePersonMemory();
	}
	
	public void setActiveMission(CampaignEventPlugin mission) {
		if (mission == null) {
			memoryMap.remove(MemKeys.MISSION);
		} else {
			MemoryAPI memory = mission.getMemory();
			if (memory != null) {
				memoryMap.put(MemKeys.MISSION, memory);
			} else {
				memoryMap.remove(MemKeys.MISSION);
			}
		}
	}
	
	
	public boolean fireAll(String trigger) {
		return FireAll.fire(null, dialog, memoryMap, trigger);
	}
	
	public boolean fireBest(String trigger) {
		return FireBest.fire(null, dialog, memoryMap, trigger);
	}
	
	public void backFromEngagement(EngagementResultAPI result) {
		// no combat here, so this won't get called
	}
	
	public void optionSelected(String text, Object optionData) {
		if (optionData == null || !(optionData instanceof String)) return;
		
		String optionId = (String) optionData;
		
		if (text != null) {
			//textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
			dialog.addOptionSelectedText(optionData);
		}
		
		if (optionId == FAILSAFE_LEAVE) {
			new DismissDialog().execute(null, dialog, null, memoryMap);
			return;
		}
		
		if (optionId == DumpMemory.OPTION_ID) {
			new DumpMemory().execute(null, dialog, null, memoryMap);
			return;
		} else if (DevMenuOptions.isDevOption(optionData)) {
			DevMenuOptions.execute(dialog, (String) optionData);
			return;
		}
		
		memory.set("$option", optionId);
		memory.expire("$option", 0);

		boolean foundRule = fireBest("DialogOptionSelected");
		if (!foundRule && !dialog.isCurrentOptionHadAConfirm()) {
			textPanel.addPara("ERROR: no rule found for option " + optionId + 
					", adding a failsafe option to exit dialog.", Misc.getNegativeHighlightColor());
			textPanel.addPara("Note: this may break any mission interaction in the current dialog, "
							  + "it's recommended that you reload an earlier save if you use this option.");
			textPanel.highlightInLastPara(Misc.getNegativeHighlightColor(), "recommended that you reload an earlier save");
			options.addOption("Exit dialog", FAILSAFE_LEAVE);
		}
		
	}
	
	
	private String lastOptionMousedOver = null;
	private Map<String, MemoryAPI> memoryMap;
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
//		if (Global.getSettings().isDevMode() && Keyboard.isKeyDown(Keyboard.KEY_F2)) {
//			new DumpMemory().execute(dialog, new ArrayList<Token>(), memoryMap);
//		}
	}
	
	private void addText(String text) {
		if (text == null || text.isEmpty()) return;
		
		textPanel.addParagraph(text);
	}
	
	private void appendText(String text) {
		textPanel.appendToLastParagraph(" " + text);
	}
	
	public Object getContext() {
		return null;
	}

	public Map<String, MemoryAPI> getMemoryMap() {
		return memoryMap;
	}
	public Object getCustom1() {
		return custom1;
	}
	public void setCustom1(Object custom1) {
		this.custom1 = custom1;
	}
	public Object getCustom2() {
		return custom2;
	}
	public void setCustom2(Object custom2) {
		this.custom2 = custom2;
	}
	public Object getCustom3() {
		return custom3;
	}
	public void setCustom3(Object custom3) {
		this.custom3 = custom3;
	}
	
}



