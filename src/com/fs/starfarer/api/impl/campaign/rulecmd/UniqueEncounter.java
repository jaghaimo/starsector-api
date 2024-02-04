package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData.UniqueEncounterData;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	UniqueEncounter <action> <parameters>
 */
public class UniqueEncounter extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI mem = memoryMap.get(MemKeys.LOCAL);
		if (mem == null) return false; // should not be possible unless there are other big problems already
		
		UniqueEncounterData data = SharedData.getData().getUniqueEncounterData();
		
		if ("setInteractedWith".equals(action)) {
			String id = params.get(1).getString(memoryMap);
			data.setWasInteractedWith(id);
		}
		
		return false;
	}

	
	
}
