package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;


public class Ping extends BaseCommandPlugin {
	
	//Ping <type> <optional entity id>
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		String type = params.get(0).getString(memoryMap);
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		if (params.size() >= 2) {
			String id = params.get(1).getString(memoryMap);
			entity = Global.getSector().getEntityById(id);
		}
		if (entity != null) {
			Global.getSector().addPing(entity, type);
		}
		
		return true;
	}
}






