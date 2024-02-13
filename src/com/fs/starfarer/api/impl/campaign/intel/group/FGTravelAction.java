package com.fs.starfarer.api.impl.campaign.intel.group;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class FGTravelAction extends BaseFGAction {

	protected SectorEntityToken from, to;
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	protected String travelText;
	protected String waitingAtDestinationText;
	protected String rendezvousTravelText;
	protected String waitingRendezvousText;
	protected boolean doNotGetSidetracked = true;
	
	public FGTravelAction(SectorEntityToken from, SectorEntityToken to) {
		this.from = from;
		this.to = to;
		
		travelText = "traveling to " + to.getName();
		waitingAtDestinationText = "orbiting " + to.getName();
		waitingRendezvousText = "waiting at rendezvous point";
		rendezvousTravelText = "traveling to rendezvous point";
		
		if (to.getContainingLocation() instanceof StarSystemAPI) {
			StarSystemAPI system = (StarSystemAPI) to.getContainingLocation();
			if (to == system.getCenter()) {
				travelText = "traveling to the " + system.getNameWithLowercaseTypeShort();
			}
		}
		
		interval.forceIntervalElapsed();
	}

	@Override
	public void addRouteSegment(RouteData route) {
		float travelDays = RouteLocationCalculator.getTravelDays(from, to);
		travelDays *= 1.5f;
		RouteSegment segment = new RouteSegment(null, travelDays, from, to, null);
		route.addSegment(segment);
	}
	
	public static float computeETADays(CampaignFleetAPI fleet, SectorEntityToken dest) {
		boolean sameLoc = fleet.getContainingLocation() == dest.getContainingLocation();
		float dist = 0f;
		boolean destIsSystem = dest.getStarSystem() != null && dest.getStarSystem().getCenter() == dest;
		if (sameLoc) {
			dist = Misc.getDistance(fleet, dest);
			if (destIsSystem) dist = 0f;
		} else {
			dist = Misc.getDistance(fleet.getLocationInHyperspace(), dest.getLocationInHyperspace());

			if (!destIsSystem) {
				dist += 5000f; // fudge factor for traveling in-system
			}
			
			if (fleet.getContainingLocation() != null && 
					dest.getContainingLocation() != null && 
					!fleet.getContainingLocation().isHyperspace() && 
					!dest.getContainingLocation().isHyperspace()) {
				dist += 5000f; // two legs of travel are in-system
			}
		}
		
		return dist / 1000f;
	}

	public float getEstimatedDaysToComplete() {
		if (intel.isSpawnedFleets()) {
			float totalETA = 0f;
			float totalStr = 0f;
			for (CampaignFleetAPI fleet : intel.getFleets()) {
				float eta = computeETADays(fleet, to); 
				float w = fleet.getEffectiveStrength();
				totalETA += eta * w;
				totalStr += w;
			}
			
			float eta = totalETA / Math.max(1f, totalStr);
			return eta;
		} else {
			RouteSegment segment = intel.getSegmentForAction(this);
			if (segment == null) return 0f;
			return Math.max(0f, segment.daysMax - segment.elapsed);
		}
	}
	
	
	@Override
	public void notifySegmentFinished(RouteSegment segment) {
		super.notifySegmentFinished(segment);
		
		if (FleetGroupIntel.DEBUG) {
			System.out.println("FGTravelAction.notifySegmentFinished() " + segment.getTransitProgress() + 
					" [" + from.getName() + " -> " + to.getName() + "]");
		}
	}

	@Override
	public void notifyFleetsSpawnedMidSegment(RouteSegment segment) {
		super.notifyFleetsSpawnedMidSegment(segment);
		
		if (FleetGroupIntel.DEBUG) {
			System.out.println("FGTravelAction.notifyFleetsSpawnedMidSegment() " + segment.getTransitProgress() + 
					" [" + from.getName() + " -> " + to.getName() + "]");
		}
	}

	@Override
	public void directFleets(float amount) {
		List<CampaignFleetAPI> fleets = intel.getFleets();
		if (fleets.isEmpty()) {
			setActionFinished(true);
			return;
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		interval.advance(days);
		
		if (!interval.intervalElapsed()) return;
		
		
		
		//JumpPointAPI jp = RouteLocationCalculator.findJumpPointToUse(fleet, current.from);

		Set<LocationAPI> locations = new LinkedHashSet<LocationAPI>();
		Set<LocationAPI> locationsWithBattles = new LinkedHashSet<LocationAPI>();
		int inFrom = 0;
		int inTo = 0;
		int inHyper = 0;
		for (CampaignFleetAPI fleet : fleets) {
			LocationAPI conLoc = fleet.getContainingLocation();
			locations.add(conLoc);
			if (fleet.getBattle() != null) {
				locationsWithBattles.add(conLoc);
			}
			
			if (to.getContainingLocation() == conLoc) {
				inTo++;
			} else if (from.getContainingLocation() == conLoc && !conLoc.isHyperspace()) {
				inFrom++;
			} else {
				inHyper++;
			}
			
			//fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.4f);
		}
			
		for (CampaignFleetAPI fleet : fleets) {			
			if (doNotGetSidetracked && !locationsWithBattles.contains(fleet.getContainingLocation())) {
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true, 0.4f);
			}
		}
		
		boolean allInSameLocation = locations.size() == 1;
		
		if (allInSameLocation && inTo > 0 && to.getContainingLocation() instanceof StarSystemAPI) {
			// was the travel order just to the system, rather than a specific entity in it?
			StarSystemAPI system = (StarSystemAPI) to.getContainingLocation();
			if (to == system.getCenter()) {
				setActionFinished(true);
				return;
			}
		}
		
		
		if (allInSameLocation) {
			boolean allNear = true;
			
			Vector2f com = new Vector2f();
			float weight = 0f;
			String key = "$FGTravelAction_ignoreFleetForCenterOfMass";
			for (CampaignFleetAPI fleet : fleets) {
				boolean near = fleet.getContainingLocation() == to.getContainingLocation() &&
									Misc.getDistance(fleet, to) < to.getRadius() + 500f;
				allNear &= near;
				
				if (Misc.isSlowMoving(fleet)) {
					fleet.getMemoryWithoutUpdate().set(key, true, 2f);
				}
				if (fleet.getMemoryWithoutUpdate().getBoolean(key)) {
					continue;
				}
				
				float w = fleet.getFleetPoints();
				Vector2f loc = new Vector2f(fleet.getLocation());
				loc.scale(w);
				Vector2f.add(com, loc, com);
				weight += w;
			}
			
			if (weight < 1f) {
				weight = 1f;
				if (!fleets.isEmpty()) {
					com.set(fleets.get(0).getLocation());
				}
			}
			com.scale(1f / weight);
			
			Vector2f dest = null;
			if (inFrom > 0) {
				JumpPointAPI jp = RouteLocationCalculator.findJumpPointToUse(fleets.get(0), from);
				dest = jp.getLocation();
			} else if (inHyper > 0) {
				JumpPointAPI jp = RouteLocationCalculator.findJumpPointToUse(fleets.get(0), to);
				SectorEntityToken jumpExit = null;
				for (JumpDestination jd : jp.getDestinations()) {
					if (jd.getDestination() != null && jd.getDestination().isInHyperspace()) {
						jumpExit = jd.getDestination();
						break;
					}
				}
				if (jumpExit != null) {
					dest = jumpExit.getLocation();
				} else {
					dest = to.getLocationInHyperspace();
				}
			} else {
				dest = to.getLocation();
			}
			
			if (dest == null) {
				setActionFinished(true);
				return;
			}
			
			float angle = Misc.getAngleInDegrees(com, dest);
			float distComToDest = Misc.getDistance(com, dest);
			float offset = Math.min(distComToDest, 5000f);

			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
			dir.scale(offset);
			dest = new Vector2f(dest);
			Vector2f.add(com, dir, dest);
			
			SectorEntityToken movementToken = fleets.get(0).getContainingLocation().createToken(dest);
			//SectorEntityToken comToken = fleets.get(0).getContainingLocation().createToken(com);
			
			float comLeashRange = 750f;
			int numFleets = fleets.size();
			comLeashRange += Math.min(Math.max(numFleets - 5, 0) * 100f, 500f);
				
			for (CampaignFleetAPI fleet : fleets) {				
				fleet.clearAssignments();
//				if (fleet.getName().contains("Operations")) {
//					System.out.println("efwefwe");
//				}
				
//				if (from.isInCurrentLocation() && from.getName().contains("Tactistar")) {
//					System.out.println("efwfe");
//				}
				
				//fleet.getMemoryWithoutUpdate().set("$preferJumpPointAt", dest, 2f);
				
				float toCom = Misc.getDistance(fleet.getLocation(), com);
				float toDest = Misc.getDistance(fleet.getLocation(), dest);
				
				if (inTo > 0 && distComToDest < 500f + to.getRadius() && toCom < 500f) {
					fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, to, 3f, waitingAtDestinationText);
				} else if (inTo <= 0 && toCom < 1000 && (toDest < 750 || distComToDest < 500f)) {
					// close to the jump-point; take it by having the move order use to rather than movementToken as the target
					fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, to, 3f, travelText);
					//fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.4f);
				} else if (toCom > comLeashRange) {
					angle = Misc.getAngleInDegrees(fleet.getLocation(), com);
					dir = Misc.getUnitVectorAtDegreeAngle(angle);
					// need to overshoot to make sure Sustained Burn is used
					dir.scale(5000f);
					Vector2f overshootCom = Vector2f.add(com, dir, new Vector2f());
					SectorEntityToken comToken = fleets.get(0).getContainingLocation().createToken(overshootCom);
					fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, comToken, 3f, travelText);
				} else {
					fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, movementToken, 3f, travelText);
				}
			}
			
			if (allNear) {
				setActionFinished(true);
				return;
			}
			
		} else {
			SectorEntityToken rendezvous = null;
			if (inTo <= 0) {
				JumpPointAPI jp = RouteLocationCalculator.findJumpPointToUse(fleets.get(0), from);
//				JumpPointAPI jp = null;
//				for (CampaignFleetAPI fleet : fleets) {
//					if (fleet.getContainingLocation() == from.getContainingLocation()) {
//						jp = Misc.findNearestJumpPointTo(fleet);
//						break;
//					}
//				}
				SectorEntityToken jumpExit = null;
				for (JumpDestination jd : jp.getDestinations()) {
					if (jd.getDestination() != null && jd.getDestination().isInHyperspace()) {
						jumpExit = jd.getDestination();
						break;
					}
				}
				if (jumpExit != null) {
					rendezvous = jumpExit;
				}
			} else { 
				//rendezvous = RouteLocationCalculator.findJumpPointToUse(fleets.get(0), to);
				List<SectorEntityToken> potential = new ArrayList<SectorEntityToken>();
				for (CampaignFleetAPI fleet : fleets) {
					if (fleet.getContainingLocation() == to.getContainingLocation()) {
						SectorEntityToken test = Misc.findNearestJumpPointTo(fleet);
						if (test != null) {
							potential.add(test);
						}
//						rendezvous = Misc.findNearestJumpPointTo(fleet);
//						break;
					}
				}
				float bestScore = Float.MAX_VALUE; // want a low score
				for (SectorEntityToken curr : potential) {
					float score = 0f;
					for (CampaignFleetAPI fleet : fleets) {
						if (fleet.getContainingLocation() == to.getContainingLocation()) {
							float dist = Misc.getDistance(curr, fleet);
							score += dist * fleet.getFleetPoints();
						}
					}
					if (score < bestScore) {
						bestScore = score;
						rendezvous = curr;
					}
				}
			}
			
			if (rendezvous == null) {
				// something is majorly wrong, i.e. a system not connected to hyperspace
				setActionFinished(true);
				return;
			}

			for (CampaignFleetAPI fleet : fleets) {
				fleet.clearAssignments();

				if (fleet.getContainingLocation() != rendezvous.getContainingLocation()) {
					// catching up to other fleets
					fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, to, 3f, travelText);
				} else {
					float dist = Misc.getDistance(rendezvous, fleet);
					if (dist < 500f) {
						fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, rendezvous, 3f, waitingRendezvousText);
					} else {
						fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, rendezvous, 3f, rendezvousTravelText);
					}
				}
			}
		}
	}

	public String getTravelText() {
		return travelText;
	}

	public void setTravelText(String travelText) {
		this.travelText = travelText;
	}

	public String getWaitingAtDestinationText() {
		return waitingAtDestinationText;
	}

	public void setWaitingAtDestinationText(String waitingAtDestinationText) {
		this.waitingAtDestinationText = waitingAtDestinationText;
	}

	public String getRendezvousTravelText() {
		return rendezvousTravelText;
	}

	public void setRendezvousTravelText(String rendezvousTravelText) {
		this.rendezvousTravelText = rendezvousTravelText;
	}

	public String getWaitingRendezvousText() {
		return waitingRendezvousText;
	}

	public void setWaitingRendezvousText(String waitingRendezvousText) {
		this.waitingRendezvousText = waitingRendezvousText;
	}

	public SectorEntityToken getFrom() {
		return from;
	}

	public SectorEntityToken getTo() {
		return to;
	}

	public void setFrom(SectorEntityToken from) {
		this.from = from;
	}

	public void setTo(SectorEntityToken to) {
		this.to = to;
	}

	public boolean isDoNotGetSidetracked() {
		return doNotGetSidetracked;
	}

	public void setDoNotGetSidetracked(boolean doNotGetSidetracked) {
		this.doNotGetSidetracked = doNotGetSidetracked;
	}
	
}




