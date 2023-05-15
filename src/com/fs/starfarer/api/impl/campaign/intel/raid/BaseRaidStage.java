package com.fs.starfarer.api.impl.campaign.intel.raid;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class BaseRaidStage implements RaidStage {

	public static final String STRAGGLER = "raid_straggler";
	
	protected RaidIntel intel;
	
	protected IntervalUtil statusInterval = new IntervalUtil(0.1f, 0.2f);
	protected RaidStageStatus status = RaidStageStatus.ONGOING;
	protected float elapsed = 0f;
	protected float maxDays = 1f;
	
	protected float abortFP = 0;
	
	public BaseRaidStage(RaidIntel raid) {
		this.intel = raid;
	}

	public float getAbortFP() {
		return abortFP;
	}

	public void setAbortFP(float abortFP) {
		this.abortFP = abortFP;
	}

//	protected float getTotalRouteFP() {
//		float total = 0f;
//		for (RouteData route : getRoutes()) {
//			total += route.
//		}
//	}

	public void resetRoutes() {
		List<RouteData> routes = RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
		for (RouteData route : routes) {
			resetRoute(route);
		}
	}
	
	public void resetRoute(RouteData route) {
		CampaignFleetAPI fleet = route.getActiveFleet();
		if (fleet != null) {
			fleet.clearAssignments();
		}
		route.getSegments().clear();
		route.setCurrent(null);
	}
	
	public List<RouteData> getRoutes() {
		List<RouteData> routes = RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());
		List<RouteData> result = new ArrayList<RouteData>();
		for (RouteData route : routes) {
			if (!STRAGGLER.equals(route.getCustom())) {
				result.add(route);
			}
		}
		return result;
	}
	
	public void giveReturnOrdersToStragglers(List<RouteData> stragglers) {
		for (RouteData route : stragglers) {
			SectorEntityToken from = Global.getSector().getHyperspace().createToken(route.getInterpolatedHyperLocation());
			
			route.setCustom(STRAGGLER);
			resetRoute(route);

			float travelDays = RouteLocationCalculator.getTravelDays(from, route.getMarket().getPrimaryEntity());
			if (DebugFlags.RAID_DEBUG || DebugFlags.FAST_RAIDS) {
				travelDays *= 0.1f;
			}
			
			float orbitDays = 1f + 1f * (float) Math.random();
			route.addSegment(new RouteSegment(travelDays, from, route.getMarket().getPrimaryEntity()));
			route.addSegment(new RouteSegment(orbitDays, route.getMarket().getPrimaryEntity()));
			
			//route.addSegment(new RouteSegment(2f + (float) Math.random() * 1f, route.getMarket().getPrimaryEntity()));
		}
	}
	
	public void advance(float amount) {
		float days = Misc.getDays(amount);
		
		elapsed += days;
		
		statusInterval.advance(days);
		if (statusInterval.intervalElapsed()) {
			updateStatus();
		}
	}

	public RaidStageStatus getStatus() {
		return status;
	}


	public void notifyStarted() {
		
	}
	
	
	protected boolean enoughMadeIt(List<RouteData> routes, List<RouteData> stragglers) {
		float madeItFP = 0;
		for (RouteData route : routes) {
			if (stragglers.contains(route)) continue;
			CampaignFleetAPI fleet = route.getActiveFleet();
			if (fleet != null) {
//				float mult = Misc.getAdjustedFP(1f, route.getMarket());
//				if (mult < 1) mult = 1f;
				float mult = Misc.getSpawnFPMult(fleet);
				madeItFP += fleet.getFleetPoints() / mult;
			} else {
				madeItFP += route.getExtra().fp;
			}
		}
		return madeItFP >= abortFP;
	}
	
	protected void updateStatus() {
		abortIfNeededBasedOnFP(true);
	}
	
	protected void abortIfNeededBasedOnFP(boolean giveReturnOrders) {
		List<RouteData> routes = getRoutes();
		List<RouteData> stragglers = new ArrayList<RouteData>();
		
		boolean enoughMadeIt = enoughMadeIt(routes, stragglers);
		//enoughMadeIt = false;
		if (!enoughMadeIt) {
			//enoughMadeIt = enoughMadeIt(routes, stragglers);
			status = RaidStageStatus.FAILURE;
			if (giveReturnOrders) {
				giveReturnOrdersToStragglers(routes);
			}
			return;
		}
	}
	
	protected void updateStatusBasedOnReaching(SectorEntityToken dest, boolean giveReturnOrders) {
		updateStatusBasedOnReaching(dest, giveReturnOrders, true);
	}
	protected void updateStatusBasedOnReaching(SectorEntityToken dest, boolean giveReturnOrders, boolean requireNearTarget) {
		List<RouteData> routes = getRoutes();
		float maxRange = 2000f;
		if (!requireNearTarget) {
			maxRange = 10000000f;
		}
		List<RouteData> stragglers = getStragglers(routes, dest, maxRange);
		
		boolean enoughMadeIt = enoughMadeIt(routes, stragglers);
		
		if (stragglers.isEmpty() && enoughMadeIt) {
			status = RaidStageStatus.SUCCESS;
			return;
		}
		
		if (elapsed > maxDays + intel.getExtraDays()) {
			if (enoughMadeIt) {
				status = RaidStageStatus.SUCCESS;
				if (giveReturnOrders) {
					giveReturnOrdersToStragglers(stragglers);
				}
			} else {
				status = RaidStageStatus.FAILURE;
				if (giveReturnOrders) {
					giveReturnOrdersToStragglers(routes);
				}
			}
			return;
		}
	}
	
	public float getExtraDaysUsed() {
		return Math.max(0, elapsed - maxDays);
	}
	
	public List<RouteData> getStragglers(List<RouteData> routes, SectorEntityToken dest, float maxRange) {
		List<RouteData> stragglers = new ArrayList<RouteData>();
		
		for (RouteData route : routes) {
			CampaignFleetAPI fleet = route.getActiveFleet();
			if (fleet != null) {
				if (fleet.getContainingLocation() == dest.getContainingLocation()) {
					float dist = Misc.getDistance(fleet, dest);
					if (dist > maxRange) {
						stragglers.add(route);
					}
				} else {
					stragglers.add(route);
				}
			} else if (!route.isExpired()) {
				boolean waiting = false;
				if (route.getCurrent() != null && AssembleStage.WAIT_STAGE.equals(route.getCurrent().custom)) {
					waiting = true;
				}
				if (!waiting) {
					stragglers.add(route);
				}
			}
		}
		
		return stragglers;
	}

	
	public float getElapsed() {
		return elapsed;
	}

	public float getMaxDays() {
		return maxDays;
	}

	public void showStageInfo(TooltipMakerAPI info) {
	}
	
	
	
}


