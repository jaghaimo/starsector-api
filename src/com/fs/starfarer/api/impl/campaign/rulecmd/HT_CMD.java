package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTPoints;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTScavengerDataFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.Objectives;
import com.fs.starfarer.api.impl.campaign.velfield.SlipstreamVisibilityManager;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * For Hyperspace Topography ("HT") related tasks.
 * 
 *	HT_CMD <action> <parameters>
 */
public class HT_CMD extends BaseCommandPlugin {

	public static int CREDITS_PER_TD_POINT = 200;
	
	public static float MIN_SCAVENGER_FP = 50;
	public static float MAX_SCAVENGER_FP = 150;
	

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
		StarSystemAPI system = null;
		if (dialog.getInteractionTarget().getContainingLocation() instanceof StarSystemAPI) {
			system = (StarSystemAPI) dialog.getInteractionTarget().getContainingLocation();
		}
				
		//HyperspaceTopographyEventIntel intel = HyperspaceTopographyEventIntel.get();
		//if (intel == null) return false;
		
		if ("hasRecentReadingsNearby".equals(action)) {
			return HyperspaceTopographyEventIntel.hasRecentReadingsNearPlayer();
		} else if ("computeDataStats".equals(action)) {
			if (entity instanceof CampaignFleetAPI) {
				CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
				float fp = fleet.getFleetPoints();
				int cost = getDataCost(fp);
				float range = getRevealRange(fp);
				memory.set("$ht_dataCost", Misc.getWithDGS(cost));
				memory.set("$ht_dataRange", "" + (int) range);
				return true;
			}
			return false;
		} else if ("getScavengerData".equals(action)) {
			if (entity instanceof CampaignFleetAPI) {
				CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
				float fp = fleet.getFleetPoints();
				float range = getRevealRange(fp);
				
				SlipstreamVisibilityManager.updateSlipstreamVisibility(entity.getLocationInHyperspace(), range);
				
				int points = getDataPoints(fp);
				boolean hasRecent = HyperspaceTopographyEventIntel.hasRecentReadingsNearPlayer();
				if (!hasRecent && points > 0) {
					HyperspaceTopographyEventIntel.addFactorCreateIfNecessary(new HTScavengerDataFactor(points), dialog);
					if (HyperspaceTopographyEventIntel.get() != null) {
						HyperspaceTopographyEventIntel.get().addRecentReadings(entity.getLocationInHyperspace());
					}
				}
				return true;
			}
			return false;
		}
		
		return false;
	}
	
	
	
	public static int getDataCost(float fp) {
		return getDataPoints(fp) * CREDITS_PER_TD_POINT;
	}
	
	public static int getRevealRange(float fp) {
		float min = Objectives.BURST_RANGE_SCAVENGER_MIN;
		float max = Objectives.BURST_RANGE_SCAVENGER_MAX;
		
		float f = Math.max(fp - MIN_SCAVENGER_FP, 0) / (MAX_SCAVENGER_FP - MIN_SCAVENGER_FP);
		if (f > 1f) f = 1f;
		if (f < 0f) f = 0f;
		
		int result = Math.round(min + (max - min) * f);
		return result;
	}
	public static int getDataPoints(float fp) {
		float min = HTPoints.SCAVENGER_MIN;
		float max = HTPoints.SCAVENGER_MAX;
		
		float f = Math.max(fp - MIN_SCAVENGER_FP, 0) / (MAX_SCAVENGER_FP - MIN_SCAVENGER_FP);
		if (f > 1f) f = 1f;
		if (f < 0f) f = 0f;
		
		int result = Math.round(min + (max - min) * f);
		return result;
	}

	
	
}
