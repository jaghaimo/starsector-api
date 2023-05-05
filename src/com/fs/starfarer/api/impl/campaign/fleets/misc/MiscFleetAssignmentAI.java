package com.fs.starfarer.api.impl.campaign.fleets.misc;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.fleets.misc.MiscFleetRouteManager.MiscRouteData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class MiscFleetAssignmentAI extends RouteFleetAssignmentAI {
	
	
	private String origFaction;
	private IntervalUtil factionChangeTracker = new IntervalUtil(0.1f, 0.3f);
	public MiscFleetAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
		super(fleet, route);
		//origFaction = fleet.getFaction().getId();
		origFaction = route.getFactionId();
		if (!getData().smuggling) {
			origFaction = null;
			factionChangeTracker = null;
		} else {
			factionChangeTracker.forceIntervalElapsed();
			doSmugglingFactionChangeCheck(0.1f);
		}
	}
	
	@Override
	protected String getStartingActionText(RouteSegment segment) {
		MiscRouteData data = getData();
		MiscFleetCreatorPlugin creator = MiscFleetRouteManager.getCreator(data.creatorId);
		if (creator != null) {
			return creator.getStartingActionText(fleet, segment, data);
		}
		return super.getStartingActionText(segment);
	}
	@Override
	protected String getEndingActionText(RouteSegment segment) {
		MiscRouteData data = getData();
		MiscFleetCreatorPlugin creator = MiscFleetRouteManager.getCreator(data.creatorId);
		if (creator != null) {
			return creator.getEndingActionText(fleet, segment, data);
		}
		return super.getEndingActionText(segment);
	}
	
	@Override
	protected String getTravelActionText(RouteSegment segment) {
		Integer id = segment.getId();
		MiscRouteData data = getData();
		MiscFleetCreatorPlugin creator = MiscFleetRouteManager.getCreator(data.creatorId);
		if (creator != null) {
			if (id == MiscFleetRouteManager.ROUTE_TRAVEL_DST) {
				return creator.getTravelToDestActionText(fleet, segment, data);
			} else if (id == MiscFleetRouteManager.ROUTE_TRAVEL_SRC) {
				return creator.getTravelReturnActionText(fleet, segment, data);
			}
		}
		return super.getTravelActionText(segment);
	}
	
	@Override
	protected String getInSystemActionText(RouteSegment segment) {
		Integer id = segment.getId();
		MiscRouteData data = getData();
		MiscFleetCreatorPlugin creator = MiscFleetRouteManager.getCreator(data.creatorId);
		if (creator != null) {
			if (id == MiscFleetRouteManager.ROUTE_DST_UNLOAD) {
				return creator.getAtDestUnloadActionText(fleet, segment, data);
			} else if (id == MiscFleetRouteManager.ROUTE_DST_LOAD) {
				return creator.getAtDestLoadActionText(fleet, segment, data);
			}
		}
		return super.getInSystemActionText(segment);
	}

	protected MiscRouteData getData() {
		MiscRouteData data = (MiscRouteData) route.getCustom();
		return data;
	}
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		doSmugglingFactionChangeCheck(amount);
	}
	
	
	public void doSmugglingFactionChangeCheck(float amount) {
		MiscRouteData data = getData();
		if (!data.smuggling) return;
		float days = Global.getSector().getClock().convertToDays(amount);
		
//		if (fleet.isInCurrentLocation()) {
//			System.out.println("23wefwf23");
//			days *= 100000f;
//		}
		
		factionChangeTracker.advance(days);
		if (factionChangeTracker.intervalElapsed() && fleet.getAI() != null) {
			List<MarketAPI> markets = Misc.getMarketsInLocation(fleet.getContainingLocation());
			MarketAPI align = null;
			int max = 0;
			for (MarketAPI market : markets) {
				int size = market.getSize();
				if (market.getFaction().isHostileTo(data.factionId)) {
					if (size > max) {
						max = size;
						align = market;
					}
				} else if (size > max) {
					max = size;
					align = null;
				}
			}
			
			if (align == null || fleet.isInHyperspace() ||
					fleet.getContainingLocation() == data.from.getContainingLocation()) {
				align = data.from; 
			}
			
			
			if (align != null) {
				String targetFac = origFaction;
				boolean hostile = align.getFaction().isHostileTo(targetFac);
				if (hostile) {
					targetFac = Factions.INDEPENDENT;
					hostile = align.getFaction().isHostileTo(targetFac);
				}
				if (hostile) {
					targetFac = align.getFactionId();
				}
				if (!fleet.getFaction().getId().equals(targetFac)) {
					fleet.setFaction(targetFac, true);
				}
			} else {
				String targetFac = origFaction;
				if (fleet.isInHyperspace()) {
					targetFac = Factions.INDEPENDENT;
				}
				if (!fleet.getFaction().getId().equals(targetFac)) {
					fleet.setFaction(targetFac, true);
				}
			}
		}
	}
	
}










