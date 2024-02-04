package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.util.Misc;

public class RouteFleetAssignmentAI extends BaseAssignmentAI {

	public static enum TravelState {
		IN_SYSTEM,
		LEAVING_SYSTEM,
		IN_HYPER_TRANSIT,
		ENTERING_SYSTEM,
	}
	
	protected RouteData route;
	protected Boolean gaveReturnAssignments = null;


	public RouteFleetAssignmentAI(CampaignFleetAPI fleet, RouteData route, FleetActionDelegate delegate) {
		super();
		this.fleet = fleet;
		this.route = route;
		this.delegate = delegate;
		giveInitialAssignments();
	}
	public RouteFleetAssignmentAI(CampaignFleetAPI fleet, RouteData route) {
		super();
		this.fleet = fleet;
		this.route = route;
		giveInitialAssignments();
	}
	
	protected TravelState getTravelState(RouteSegment segment) {
		if (segment.isInSystem()) {
			return TravelState.IN_SYSTEM;
		}
		
		if (segment.hasLeaveSystemPhase() && segment.getLeaveProgress() < 1f) {
			return TravelState.LEAVING_SYSTEM;
		}
		if (segment.hasEnterSystemPhase() && segment.getEnterProgress() > 0f) {
			return TravelState.ENTERING_SYSTEM;
		}
		
		return TravelState.IN_HYPER_TRANSIT;
	}
	
	protected LocationAPI getLocationForState(RouteSegment segment, TravelState state) {
		switch (state) {
		case ENTERING_SYSTEM: {
			if (segment.to != null) {
				return segment.to.getContainingLocation();
			}
			return segment.from.getContainingLocation();
		}
		case IN_HYPER_TRANSIT: return Global.getSector().getHyperspace();
		case IN_SYSTEM: return segment.from.getContainingLocation();
		case LEAVING_SYSTEM: return segment.from.getContainingLocation();
		}
		return null;
	}
	
	protected void giveInitialAssignments() {
		TravelState state = getTravelState(route.getCurrent());
		LocationAPI conLoc = getLocationForState(route.getCurrent(), state);
		
		if (fleet.getContainingLocation() != null) {
			fleet.getContainingLocation().removeEntity(fleet);
		}
		conLoc.addEntity(fleet);
		
//		Vector2f loc = route.getInterpolatedLocation();
//		fleet.setLocation(loc.x, loc.y);
		fleet.setFacing((float) Math.random() * 360f);
		
		pickNext(true);
	}
	
	@Override
	public void advance(float amount) {
		advance(amount, true);	
	}
	
	protected void advance(float amount, boolean withReturnAssignments) {
		if (withReturnAssignments && route.isExpired() && gaveReturnAssignments == null) {
			RouteSegment current = route.getCurrent();
			if (current != null && current.from != null &&
					Misc.getDistance(fleet.getLocation(), current.from.getLocation()) < 1000f) {
				fleet.clearAssignments();
				fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, current.from, 1000f,
								    "returning to " + current.from.getName());
			} else {
				Misc.giveStandardReturnToSourceAssignments(fleet);
			}
			gaveReturnAssignments = true;
			return;
		}
		super.advance(amount);
	}

	protected String getTravelActionText(RouteSegment segment) {
		return "traveling";
	}
	
	protected String getInSystemActionText(RouteSegment segment) {
		return "patrolling";
	}
	
	protected String getStartingActionText(RouteSegment segment) {
		if (segment.from == route.getMarket().getPrimaryEntity()) {
			return "orbiting " + route.getMarket().getName();
		}
		return "patrolling";
	}
	
	protected String getEndingActionText(RouteSegment segment) {
		SectorEntityToken to = segment.to;
		if (to == null) to = segment.from;
		if (to == null) to = route.getMarket().getPrimaryEntity();
		return "returning to " + to.getName();
		//return "returning to " + route.getMarket().getName());
		//return "orbiting " + route.getMarket().getName();
	}
	
	protected void pickNext() {
		pickNext(false);
	}
	
	protected void pickNext(boolean justSpawned) {
		RouteSegment current = route.getCurrent();
		if (current == null) return;
		
		List<RouteSegment> segments = route.getSegments();
		int index = route.getSegments().indexOf(route.getCurrent());
		
		
		if (index == 0 && route.getMarket() != null && !current.isTravel()) {
			if (current.getFrom() != null && (current.getFrom().isSystemCenter() || current.getFrom().getMarket() != route.getMarket())) {
				addLocalAssignment(current, justSpawned);
			} else {
				addStartingAssignment(current, justSpawned);
			}
			return;
		}
		
		if (index == segments.size() - 1 && route.getMarket() != null && !current.isTravel()
				&& (current.elapsed >= current.daysMax || current.getFrom() == route.getMarket().getPrimaryEntity())) {
			addEndingAssignment(current, justSpawned);
			return;
		}
		
		// transiting from current to next; may or may not be in the same star system
		if (current.isTravel()) {
			if (index == segments.size() - 1 && 
					fleet.getContainingLocation() == current.to.getContainingLocation() && 
					current.elapsed >= current.daysMax) {
				addEndingAssignment(current, justSpawned);
			} else {
				addTravelAssignment(current, justSpawned);
			}
			return;
		}
		
		// in a system or in a hyperspace location for some time
		if (!current.isTravel()) {
			addLocalAssignment(current, justSpawned);
		}
	}
	
	protected void addStartingAssignment(final RouteSegment current, boolean justSpawned) {
		SectorEntityToken from = current.getFrom();
		if (from == null) from = route.getMarket().getPrimaryEntity();
		
		if (justSpawned) {
			float progress = current.getProgress();
			RouteLocationCalculator.setLocation(fleet, progress, from, from);
		}
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, from, 
						    current.daysMax - current.elapsed, getStartingActionText(current),
						    goNextScript(current));
	}
	
	protected Script goNextScript(final RouteSegment current) {
		return new Script() {
			public void run() {
				route.goToAtLeastNext(current);
			}
		};
	}
	
	protected void addEndingAssignment(final RouteSegment current, boolean justSpawned) {
		if (justSpawned) {
			float progress = current.getProgress();
			RouteLocationCalculator.setLocation(fleet, progress, 
									current.getDestination(), current.getDestination());
		}
//		if (justSpawned) {
//			//Vector2f loc = route.getMarket().getPrimaryEntity().getLocation();
//			Vector2f loc = current.getDestination().getLocation();
//			loc = Misc.getPointWithinRadius(loc, 
//						   current.getDestination().getRadius() + 100 + (float) Math.random() * 100f);
//			fleet.setLocation(loc.x, loc.y);
//		}
		
		SectorEntityToken to = current.to;
		if (to == null) to = current.from;
		if (to == null) to = route.getMarket().getPrimaryEntity();
		
		if (to == null || !to.isAlive()) {
			Vector2f loc = Misc.getPointAtRadius(fleet.getLocationInHyperspace(), 5000);
			SectorEntityToken token = Global.getSector().getHyperspace().createToken(loc);
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, token, 1000f);
			return;
		}
		
		
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, to, 1000f,
							"returning to " + to.getName());
		if (current.daysMax > current.elapsed) {
			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, to, 
								//current.daysMax - current.elapsed, "orbiting " + to.getName());
								current.daysMax - current.elapsed, getEndingActionText(current));
		}
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, to, 
				1000f, getEndingActionText(current),
				goNextScript(current));
	}
	
	protected void addLocalAssignment(final RouteSegment current, boolean justSpawned) {
		if (justSpawned) {
			float progress = current.getProgress();
			RouteLocationCalculator.setLocation(fleet, progress, 
									current.from, current.getDestination());
		}
		if (current.from != null && current.to == null && !current.isFromSystemCenter()) {
//			if (justSpawned) {
//				Vector2f loc = Misc.getPointWithinRadius(current.from.getLocation(), 500);
//				fleet.setLocation(loc.x, loc.y);
//			}
			fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, current.from, 
					current.daysMax - current.elapsed, getInSystemActionText(current),
					goNextScript(current));		
			return;
		}
		
//		if (justSpawned) {
//			Vector2f loc = Misc.getPointAtRadius(new Vector2f(), 8000);
//			fleet.setLocation(loc.x, loc.y);
//		}
		
		SectorEntityToken target = null;
		if (current.from.getContainingLocation() instanceof StarSystemAPI) {
			target = ((StarSystemAPI)current.from.getContainingLocation()).getCenter();
		} else {
			target = Global.getSector().getHyperspace().createToken(current.from.getLocation().x, current.from.getLocation().y);
		}
		
		fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, 
						    current.daysMax - current.elapsed, getInSystemActionText(current));
	}
	
	protected void addTravelAssignment(final RouteSegment current, boolean justSpawned) {
		if (justSpawned) {
			TravelState state = getTravelState(current);
			if (state == TravelState.LEAVING_SYSTEM) {
				float p = current.getLeaveProgress();
				SectorEntityToken jp = RouteLocationCalculator.findJumpPointToUse(fleet, current.from);
				if (jp == null) jp = current.from;
				RouteLocationCalculator.setLocation(fleet, p, 
						current.from, jp);
				
//				JumpPointAPI jp = Misc.findNearestJumpPointTo(current.from);
//				if (jp != null) {
//					Vector2f loc = Misc.interpolateVector(current.from.getLocation(),
//														  jp.getLocation(),
//														  p);
//					fleet.setLocation(loc.x, loc.y);
//				} else {
//					fleet.setLocation(current.from.getLocation().x, current.from.getLocation().y);
//				}
//				randomizeFleetLocation(p);
			}
			else if (state == TravelState.ENTERING_SYSTEM) {
				float p = current.getEnterProgress();
				SectorEntityToken jp = RouteLocationCalculator.findJumpPointToUse(fleet, current.to);
				if (jp == null) jp = current.to;
				RouteLocationCalculator.setLocation(fleet, p, 
													jp, current.to);
				
//				JumpPointAPI jp = Misc.findNearestJumpPointTo(current.to);
//				if (jp != null) {
//					Vector2f loc = Misc.interpolateVector(jp.getLocation(),
//														  current.to.getLocation(),
//														  p);
//					fleet.setLocation(loc.x, loc.y);
//				} else {
//					fleet.setLocation(current.to.getLocation().x, current.to.getLocation().y);
//				}
//				randomizeFleetLocation(p);
			}
			else if (state == TravelState.IN_SYSTEM) {
				float p = current.getTransitProgress();
				RouteLocationCalculator.setLocation(fleet, p, 
													current.from, current.to);
//				Vector2f loc = Misc.interpolateVector(current.from.getLocation(),
//													  current.to.getLocation(),
//													  p);
//				fleet.setLocation(loc.x, loc.y);
//				randomizeFleetLocation(p);
			}
			else if (state == TravelState.IN_HYPER_TRANSIT) {
				float p = current.getTransitProgress();
				SectorEntityToken t1 = Global.getSector().getHyperspace().createToken(
															   current.from.getLocationInHyperspace().x, 
															   current.from.getLocationInHyperspace().y);
				SectorEntityToken t2 = Global.getSector().getHyperspace().createToken(
															   current.to.getLocationInHyperspace().x, 
						   									   current.to.getLocationInHyperspace().y);				
				RouteLocationCalculator.setLocation(fleet, p, t1, t2);
				
//				Vector2f loc = Misc.interpolateVector(current.getContainingLocationFrom().getLocation(),
//													  current.getContainingLocationTo().getLocation(),
//													  p);
//				fleet.setLocation(loc.x, loc.y);
//				randomizeFleetLocation(p);
			}
			
//			
//			Vector2f loc = route.getInterpolatedLocation();
//			Random random = new Random();
//			if (route.getSeed() != null) {
//				random = Misc.getRandom(route.getSeed(), 1);
//			}
//			loc = Misc.getPointWithinRadius(loc, 2000f, random);
//			fleet.setLocation(loc.x, loc.y);
		}
		
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, current.to, 10000f, getTravelActionText(current), 
				goNextScript(current));
		
//		if (current.isInSystem()) {
//			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, current.to, 10000f, getTravelActionText(current), 
//					goNextScript(current));
//		} else {
//			SectorEntityToken target = current.to;
////			if (current.to.getContainingLocation() instanceof StarSystemAPI) {
////				target = ((StarSystemAPI)current.to.getContainingLocation()).getCenter();
////			}
//			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, target, 10000f, getTravelActionText(current), 
//					goNextScript(current));
//		}
	}

	
	
}










