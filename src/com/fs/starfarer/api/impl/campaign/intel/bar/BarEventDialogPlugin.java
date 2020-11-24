package com.fs.starfarer.api.impl.campaign.intel.bar;

import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.rulecmd.DumpMemory;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.BarCMD;

public class BarEventDialogPlugin implements InteractionDialogPlugin {
	protected InteractionDialogAPI dialog;
	protected InteractionDialogPlugin originalPlugin;
	protected Map<String, MemoryAPI> memoryMap;
	protected PortsideBarEvent event;
	protected BarCMD cmd;

	public BarEventDialogPlugin(BarCMD cmd, InteractionDialogPlugin originalPlugin, PortsideBarEvent event, Map<String, MemoryAPI> memoryMap) {
		this.cmd = cmd;
		this.originalPlugin = originalPlugin;
		this.event = event;
		this.memoryMap = memoryMap;
	}

	public void init(InteractionDialogAPI dialog) {
		this.dialog = dialog;
		
		event.init(dialog);
		if (event.isDialogFinished()) {
			endEvent();
		} else {
			if (Global.getSettings().isDevMode()) {
				DevMenuOptions.addOptions(dialog);
			}
		}
	}
	public void optionSelected(String optionText, Object optionData) {
		if (optionText != null) {
			dialog.getTextPanel().addParagraph(optionText, Global.getSettings().getColor("buttonText"));
		}
		if (optionData == DumpMemory.OPTION_ID) {
			new DumpMemory().execute(null, dialog, null, getMemoryMap());
			return;
		} else if (DevMenuOptions.isDevOption(optionData)) {
			DevMenuOptions.execute(dialog, (String) optionData);
			return;
		}
		
		
		event.optionSelected(optionText, optionData);
		if (event.isDialogFinished()) {
			endEvent();
		} else {
			if (Global.getSettings().isDevMode()) {
				DevMenuOptions.addOptions(dialog);
			}
		}
	}
	
	public void endEvent() {
		dialog.setPlugin(originalPlugin);
		cmd.returningFromEvent(event);
//		if (withContinue) {
//			FireBest.fire(null, dialog, memoryMap, "BarEventFinished");
//		} else {
//			FireBest.fire(null, dialog, memoryMap, "BarEventFinishedNoContinue");
//		}
	}
	
	public void advance(float amount) {
	}
	public void backFromEngagement(EngagementResultAPI battleResult) {
	}
	public Object getContext() {
		return null;
	}
	public Map<String, MemoryAPI> getMemoryMap() {
		return memoryMap;
	}
	public void optionMousedOver(String optionText, Object optionData) {
	}
}
