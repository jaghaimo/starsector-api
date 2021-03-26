package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/**
 *	HailPlayer
 *
 *	Equivalent to:
 *	AddText "You're being hailed by the $faction $otherShipOrFleet." $faction.baseColor
 *	$hailing = true 0
 *
 *	The latter changes the "open comm link" text to a yellow "accept the comm request". 
 *
 * Copyright 2015 Fractal Softworks, LLC
 */
public class HailPlayer extends BaseCommandPlugin {

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		if (dialog == null) return false;
		if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) return false;
		
		CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();

		String shipOrFleet = "ship";
		if (fleet.getFleetData().getMembersListCopy().size() > 1) {
			shipOrFleet = "fleet";
		}
		FactionAPI faction = fleet.getFaction();
		String factionName = faction.getEntityNamePrefix();
		if (factionName == null || factionName.isEmpty()) {
			factionName = faction.getDisplayName();
		}
		
		dialog.getTextPanel().addPara("You're being hailed by the " + factionName + " " + shipOrFleet + ".",
									  faction.getBaseUIColor());
		
		fleet.getMemoryWithoutUpdate().set("$hailing", true, 0);
		
		return true;
	}

}





