package com.fs.starfarer.api.impl.campaign.missions.hub;

import com.fs.starfarer.api.campaign.econ.MarketAPI;



public abstract class HubMissionWithBarEvent extends HubMissionWithSearch {

	/**
	 * Called BEFORE the mission is create()'ed.
	 * 
	 * Why use this vs aborting mission during creation? Mainly just for clarity/organizational purposes,
	 * but either way is fine.
	 * 
	 * @param market
	 * @return
	 */
	public boolean shouldShowAtMarket(MarketAPI market) {
		return true;
	}
	
//	transient protected InteractionDialogAPI dialog;
//	transient protected TextPanelAPI text;
//	transient protected OptionPanelAPI options;
//	transient protected Map<String, MemoryAPI> memoryMap;
//	
//	public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
////		this.dialog = dialog;
////		this.memoryMap = memoryMap;
////		if (dialog != null) {
////			text = dialog.getTextPanel();
////			options = dialog.getOptionPanel();
////		}
////		FireBest.fire(null, dialog, memoryMap, getTriggerPrefix() + "_startBar true");
//	}
//	
//	public boolean shouldShowAtMarket(MarketAPI market) {
//		return true;
//	}
//	
//	public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
//		setMissionId("cheapCom");
//		setGenRandom(new Random());
//		create();
//		setGenRandom(null);
//		if (isMissionCreationAborted()) return;
//		
//		updateInteractionData(dialog, memoryMap);
//		
//		FireBest.fire(null, dialog, memoryMap, getTriggerPrefix() + "_blurbBar true");		
//		FireBest.fire(null, dialog, memoryMap, getTriggerPrefix() + "_optionBar true");		
//	}
//
//	public void wasShownAtMarket(MarketAPI market) {
//
//	}
//	
//	public boolean endWithContinue() {
//		return false;
//	}
//
//	public boolean isDialogFinished() {
//		return false;
//	}
//
//	public void optionSelected(String optionText, Object optionData) {
//	
//	}
//
//	public boolean shouldRemoveEvent() {
//		return false;
//	}

}
