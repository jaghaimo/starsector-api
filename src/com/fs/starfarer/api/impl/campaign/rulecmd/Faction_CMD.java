package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.FactionHostilityManager;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTPoints;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTScavengerDataFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.Objectives;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamVisibilityManager;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * For Faction relation-type queries and commands that aren't elsewhere; relating to diplomatic states, relations, so on. Shamelessly copied from HT. - dgb
 * 
 *	Faction_CMD <action> <parameters>
 */
public class Faction_CMD extends BaseCommandPlugin {

	//public static int CREDITS_PER_TD_POINT = 200;
	//public static float MIN_SCAVENGER_FP = 50;
	//public static float MAX_SCAVENGER_FP = 150;
	

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		if (dialog == null) return false;
		
		OptionPanelAPI options = dialog.getOptionPanel();
		TextPanelAPI text = dialog.getTextPanel();
		CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
		CargoAPI cargo = pf.getCargo();
		
		SectorEntityToken entity = dialog.getInteractionTarget();
		if (entity == null) return false;
		
		String action = params.get(0).getString(memoryMap);
		
		MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false; // should not be possible unless there are other big problems already
		
		//MarketAPI market = dialog.getInteractionTarget().getMarket();
		/*StarSystemAPI system = null;
		if (dialog.getInteractionTarget().getContainingLocation() instanceof StarSystemAPI) {
			system = (StarSystemAPI) dialog.getInteractionTarget().getContainingLocation();
		}*/
		
		if ("commissioningFactionIsAtWarWith".equals(action)) {
			//faction.getId().
			if (params.size() >= 1) {
				
				// is this a real faction? I hope so.
				String target_faction_id = params.get(0).getString(memoryMap);
				FactionAPI target_faction = Global.getSector().getFaction(target_faction_id);
				//FactionAPI com_faction = Misc.getCommissionFaction();
				if(target_faction != null )
				{
					return Misc.getCommissionFaction().isHostileTo(target_faction);
				}
				else
				{	
					System.out.print(target_faction_id + " is an invalid FactionID!");
					// Maybe this should do something else?
				}
			}
		}

		return false;
	}
}
