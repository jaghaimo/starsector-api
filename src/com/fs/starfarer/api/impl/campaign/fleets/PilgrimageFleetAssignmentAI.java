package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class PilgrimageFleetAssignmentAI extends RouteFleetAssignmentAI {
	
	public static class PilgrimageRouteData {
		public boolean smuggling = false;
		public MarketAPI from;
		public SectorEntityToken to;
		public float size;
		public String factionId;
	}
	
	
	private String origFaction;
	private IntervalUtil factionChangeTracker = new IntervalUtil(0.1f, 0.3f);
	public PilgrimageFleetAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
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
		return "preparing for pilgrimage to shrine on " + getData().to.getName();
	}
	@Override
	protected String getEndingActionText(RouteSegment segment) {
		return "disembarking pilgrims at " + getData().from.getName();
	}
	
	@Override
	protected String getTravelActionText(RouteSegment segment) {
		Integer id = segment.getId();
		if (id == PilgrimageFleetRouteManager.ROUTE_TRAVEL_DST) {
			return "taking pilgrims to shrine on " + getData().to.getName(); 
		} else if (id == PilgrimageFleetRouteManager.ROUTE_TRAVEL_SRC) {
			return "returning pilgrims to " + getData().from.getName(); 
		}
		return super.getTravelActionText(segment);
	}
	
	@Override
	protected String getInSystemActionText(RouteSegment segment) {
		Integer id = segment.getId();
		
		if (id == PilgrimageFleetRouteManager.ROUTE_DST_UNLOAD) {
			return "disembarking pilgrims at shrine on " + getData().to.getName();
		} else if (id == PilgrimageFleetRouteManager.ROUTE_DST_LOAD) {
			return "embarking pilgrims for return voyage to " + getData().from.getName();
		}
		return super.getInSystemActionText(segment);
	}

	protected PilgrimageRouteData getData() {
		PilgrimageRouteData data = (PilgrimageRouteData) route.getCustom();
		return data;
	}
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		doSmugglingFactionChangeCheck(amount);
	}
	
	
	public void doSmugglingFactionChangeCheck(float amount) {
		PilgrimageRouteData data = getData();
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
				if (market.getFaction().isHostileTo(data.factionId)) {
					int size = market.getSize();
					if (size > max) {
						max = size;
						align = market;
					}
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










