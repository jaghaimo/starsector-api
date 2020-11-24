package com.fs.starfarer.api.impl.campaign.intel.raid;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.intel.inspection.HITravelStage;
import com.fs.starfarer.api.impl.campaign.intel.punitive.PETravelStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TravelStage extends BaseRaidStage {

	protected SectorEntityToken from;
	protected SectorEntityToken to;
	protected boolean requireNearTarget;


	public TravelStage(RaidIntel raid, SectorEntityToken from, SectorEntityToken to, boolean requireNearTarget) {
		super(raid);
		this.from = from;
		this.to = to;
		this.requireNearTarget = requireNearTarget;
	}

	@Override
	public void notifyStarted() {
		updateRoutes();
	}


	protected void updateRoutes() {
		resetRoutes();
		
		List<RouteData> routes = RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
		for (RouteData route : routes) {
			float travelDays = RouteLocationCalculator.getTravelDays(from, to);
			if (DebugFlags.RAID_DEBUG || DebugFlags.FAST_RAIDS || 
					(this instanceof PETravelStage && DebugFlags.PUNITIVE_EXPEDITION_DEBUG) ||
					(this instanceof HITravelStage && DebugFlags.HEGEMONY_INSPECTION_DEBUG)
					) {
				travelDays *= 0.1f;
			}
			
			route.addSegment(new RouteSegment(travelDays, from, to));
			route.addSegment(new RouteSegment(1000f, to, AssembleStage.WAIT_STAGE));
			
			maxDays = Math.max(maxDays, travelDays);
		}
	}
	
	protected void updateStatus() {
		abortIfNeededBasedOnFP(true);
		updateStatusBasedOnReaching(to, true, requireNearTarget);
	}
	
	public void showStageInfo(TooltipMakerAPI info) {
		int curr = intel.getCurrentStage();
		int index = intel.getStageIndex(this);
		
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		if (status == RaidStageStatus.FAILURE) {
			info.addPara("The raiding forces have failed to successfully reach the " +
					intel.getSystem().getNameWithLowercaseType() + ". The raid is now over.", opad);
		} else if (curr == index) {
			info.addPara("The raiding forces are currently travelling to the " + 
					intel.getSystem().getNameWithLowercaseType() + ".", opad);
		}
	}
}



