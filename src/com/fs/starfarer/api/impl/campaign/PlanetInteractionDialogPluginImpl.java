package com.fs.starfarer.api.impl.campaign;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;

public class PlanetInteractionDialogPluginImpl implements InteractionDialogPlugin {

	private static enum OptionId {
		INIT,
		LEAVE,
	}
	
	private InteractionDialogAPI dialog;
	private TextPanelAPI textPanel;
	private OptionPanelAPI options;
	private VisualPanelAPI visual;
	
	private CampaignFleetAPI playerFleet;
	private PlanetAPI planet;
	
	private static final Color HIGHLIGHT_COLOR = Global.getSettings().getColor("buttonShortcut");
	
	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		
//		dialog.hideVisualPanel();
//		dialog.setTextWidth(700);
		
		textPanel = dialog.getTextPanel();
		options = dialog.getOptionPanel();
		visual = dialog.getVisualPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		planet = (PlanetAPI) dialog.getInteractionTarget();
		
		visual.setVisualFade(0.25f, 0.25f);
		
		if (planet.getCustomInteractionDialogImageVisual() != null) {
			visual.showImageVisual(planet.getCustomInteractionDialogImageVisual());
		} else {
			if (!Global.getSettings().getBoolean("3dPlanetBGInInteractionDialog")) {
				visual.showPlanetInfo(planet);
			}
		}
	
		dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
		
		
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
		
		if (optionData == DumpMemory.OPTION_ID) {
			Map<String, MemoryAPI> memoryMap = new HashMap<String, MemoryAPI>();
			MemoryAPI memory = dialog.getInteractionTarget().getMemory();
			
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
			
			new DumpMemory().execute(null, dialog, null, memoryMap);
			
			return;
		} else if (DevMenuOptions.isDevOption(optionData)) {
			DevMenuOptions.execute(dialog, (String) optionData);
			return;
		}
		
		OptionId option = (OptionId) optionData;
		
		if (text != null) {
			textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
		}
		
		switch (option) {
		case INIT:
			addText(getString("approach"));
			
			Description desc = Global.getSettings().getDescription(planet.getCustomDescriptionId(), Type.CUSTOM);
			if (desc != null && desc.hasText3()) {
				addText(desc.getText3());
			}
			createInitialOptions();
			break;
		case LEAVE:
			Global.getSector().setPaused(false);
			dialog.dismiss();
			break;
		}
	}
	
	private void createInitialOptions() {
		options.clearOptions();
		options.addOption("Leave", OptionId.LEAVE, null);
		
		if (Global.getSettings().isDevMode()) {
			DevMenuOptions.addOptions(dialog);
		}
	}
	
	
	private OptionId lastOptionMousedOver = null;
	public void optionMousedOver(String optionText, Object optionData) {

	}
	
	public void advance(float amount) {
		
	}
	
	private void addText(String text) {
		textPanel.addParagraph(text);
	}
	
	private void appendText(String text) {
		textPanel.appendToLastParagraph(" " + text);
	}
	
	private String getString(String id) {
		String str = Global.getSettings().getString("planetInteractionDialog", id);

		String fleetOrShip = "fleet";
		if (playerFleet.getFleetData().getMembersListCopy().size() == 1) {
			fleetOrShip = "ship";
			if (playerFleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
				fleetOrShip = "fighter wing";
			}
		}
		str = str.replaceAll("\\$fleetOrShip", fleetOrShip);
		str = str.replaceAll("\\$planetName", planet.getName());
		
		return str;
	}
	

	public Object getContext() {
		return null;
	}
}



