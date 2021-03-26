package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	MakeOtherFleetDoThing <entity id> <duration> <text> <withClear>
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class MakeOtherFleetDoThing extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		String id = params.get(0).getString(memoryMap); 
		String text = params.get(2).getString(memoryMap); 
		float dur = params.get(1).getFloat(memoryMap); 
		
		boolean clear = false;
		if (params.size() > 3) {
			clear = params.get(3).getBoolean(memoryMap);
		}
		
		SectorEntityToken entity = Global.getSector().getEntityById(id);
		if (entity == null) return false;
		
		CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
		
		if (clear) {
			fleet.clearAssignments();
		}

		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, entity, 1000f);
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, entity, dur, text);
		
		return true;
	}

}





