package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;

public class RouteLocationCalculator {
	
	
	public static enum IntervalType {
		DAYS,
		TRAVEL_TIME,
		FRACTION_OF_REMAINING
	}
	
	public static class TaskInterval {
		public IntervalType type;
		public float value;
		
		public TaskInterval(IntervalType type, float value) {
			this.type = type;
			this.value = value;
		}

		public TaskInterval(IntervalType type) {
			super();
			this.type = type;
		}


		public static TaskInterval days(float days) {
			return new TaskInterval(IntervalType.DAYS, days);
		}
		public static TaskInterval travel() {
			return new TaskInterval(IntervalType.TRAVEL_TIME, 0f);
		}
		public static TaskInterval remaining(float fraction) {
			return new TaskInterval(IntervalType.FRACTION_OF_REMAINING, fraction);
		}
	}
	
	public static float getTravelDays(SectorEntityToken from, SectorEntityToken to) {
		float dist = 0f;
		if (from.getContainingLocation() != to.getContainingLocation()) {
			dist = Misc.getDistance(from.getLocationInHyperspace(), to.getLocationInHyperspace());
			if (!from.isInHyperspace()) {
				dist += 5000; // random guess at distance to/from jump-point
			}
			if (!to.isInHyperspace()) {
				dist += 5000; // random guess at distance to/from jump-point
			}
		} else {
			dist = Misc.getDistance(from, to);
			if (from.isSystemCenter() || to.isSystemCenter()) dist = 0f;
		}
		
		
		float travelTime = dist / 1500f;
		return travelTime;
	}
	
	public static void computeIntervalsAndSetLocation(CampaignFleetAPI fleet, float daysElapsed, float maxDays,
											boolean onlyComputeIntervals,
											TaskInterval [] intervals,
											SectorEntityToken ... sequence) {
		
		float totalDays = 0f;
		float totalFraction = 0f;
		for (int i = 0; i < intervals.length; i++) {
			TaskInterval t = intervals[i];
			if (t.type == IntervalType.TRAVEL_TIME) {
				SectorEntityToken from = sequence[i];
				SectorEntityToken to = sequence[i + 1];
				
				float travelTime = getTravelDays(from, to);
				t.value = travelTime;
			}
			
			if (t.type == IntervalType.FRACTION_OF_REMAINING) {
				totalFraction += t.value;
			} else {
				totalDays += t.value;
			}
		}
		
		if (totalFraction > 0) {
			float remaining = maxDays - totalDays;
			for (TaskInterval t : intervals) {
				if (t.type == IntervalType.FRACTION_OF_REMAINING) {
					t.value = Math.max(0.1f, t.value / totalFraction * remaining);
				}
			}
		}
		
		totalDays = 0f;
		for (TaskInterval t : intervals) {
			totalDays += t.value;
		}
		
		if (totalDays > maxDays) {
			for (TaskInterval t : intervals) {
				t.value *= maxDays / totalDays;
			}
		}
		
		float soFar = 0f;
		float progress = 0f;
		int index = 0;
		SectorEntityToken from = null;
		SectorEntityToken to = null;
		//for (float curr : intervals) {
		for (int i = 0; i < intervals.length; i++) {
			float curr = intervals[i].value;
			if (curr < 0) curr = 0;
			
			if (soFar + curr > daysElapsed && curr > 0) {
				progress = (daysElapsed - soFar) / curr;
				if (progress < 0) progress = 0;
				if (progress > 1) progress = 1;
				
				from = sequence[index];
				to = sequence[index + 1];
				intervals[i].value *= (1f - progress);
				break;
			}
			soFar += curr;
			intervals[i].value = 0;
			index++;
		}
		if (from == null) {
			index = intervals.length - 1;
			from = sequence[sequence.length - 2];
			to = sequence[sequence.length - 1];
			progress = 1f;
		}
		
		if (onlyComputeIntervals) {
			return;
		}
		
		setLocation(fleet, progress, from, to);
	}
	

	/**
	 * Used to assign a reasonable location to a fleet that was just spawned by RouteManager.
	 * 
	 * Will normalize the intervals array to not exceed maxDays total, and will then set
	 * its values to the days remaining for each section.
	 * 
	 * 
	 * @param fleet
	 * @param daysElapsed
	 * @param intervals
	 * @param sequence Must have length = intervals.length + 1.
	 */
	public static int setLocation(CampaignFleetAPI fleet,
								   float daysElapsed, float maxDays, int overflowIndex, 
								   boolean onlyAdjustIntervals,
								   float [] intervals, SectorEntityToken ... sequence) {
		
		float total = 0f;
		for (float curr : intervals) {
			total += curr;
		}
		
		// either add/subtract any extra days to the overflow interval, or scale all if there isn't one
		// e.g. treat intervals as weights if there is no overflow interval specified
		if (total != maxDays) {
			if (overflowIndex >= 0) {
				float extra = total - maxDays;
				intervals[overflowIndex] -= extra;
				if (intervals[overflowIndex] <= 0) {
					total = maxDays - intervals[overflowIndex];
					intervals[overflowIndex] = 0;
					for (int i = 0; i < intervals.length; i++) {
						intervals[i] *= maxDays / total;
					}
				}
			} else {
				for (int i = 0; i < intervals.length; i++) {
					intervals[i] *= maxDays / total;
				}		
			}
		}
		
		
		float soFar = 0f;
		float progress = 0f;
		int index = 0;
		SectorEntityToken from = null;
		SectorEntityToken to = null;
		//for (float curr : intervals) {
		for (int i = 0; i < intervals.length; i++) {
			float curr = intervals[i];
			if (curr < 0) curr = 0;
			
			if (soFar + curr > daysElapsed && curr > 0) {
				progress = (daysElapsed - soFar) / curr;
				if (progress < 0) progress = 0;
				if (progress > 1) progress = 1;
				
				from = sequence[index];
				to = sequence[index + 1];
				intervals[i] *= (1f - progress);
				break;
			}
			soFar += curr;
			intervals[i] = 0;
			index++;
		}
		if (from == null) {
			index = intervals.length - 1;
			from = sequence[sequence.length - 2];
			to = sequence[sequence.length - 1];
			progress = 1f;
		}
		
		
		if (onlyAdjustIntervals) {
			return index;
		}
		
		setLocation(fleet, progress, from, to);
		
		return index;
	}
	
	public static void setLocation(CampaignFleetAPI fleet,
			   					   float progress, SectorEntityToken from, SectorEntityToken to) {
//		Cases to handle:
//		from/to are same
//		hyper - hyper
//		system - system
//		system - hyper
//		hyper - system
//		system - other_system
		
//		Subcase: 
//		one end is system center - spawn "somewhere in system" in that case
		
		if (progress < 0) progress = 0;
		if (progress > 1) progress = 1;
		
		if (to == null) to = from;
		if (from == null) from = to;
		
		if (from == null && to == null) return;
		
		float varianceMult = getVarianceMult(progress);
		
		SectorEntityToken forSystemCenterCheck = null;
		LocationAPI conLoc = null;
		Vector2f loc = null;
		if (from == to) {
			conLoc = from.getContainingLocation();
			forSystemCenterCheck = from;

			if (!conLoc.isHyperspace()) {
				if (progress > 0.03f) {
					// at a typical orbiting radius
					loc = Misc.getPointAtRadius(from.getLocation(), from.getRadius() + 100f + (float) Math.random() * 100f);
				} else {
					loc = new Vector2f(from.getLocation());
				}
			} else {
				loc = Misc.getPointWithinRadius(from.getLocation(), 100f + 900f * varianceMult);
			}
		}
		// hyper to hyper
		else if (from.isInHyperspace() && to.isInHyperspace()) {
			conLoc = from.getContainingLocation();
			loc = Misc.interpolateVector(from.getLocation(),
 					 					 to.getLocation(),
 					 					 progress);
			loc = Misc.getPointWithinRadius(loc, 100f + 900f * varianceMult);
		}
		// different locations in same system (not hyper)
		else if (from.getContainingLocation() == to.getContainingLocation()) {
			conLoc = from.getContainingLocation();
			if (from.isSystemCenter()) forSystemCenterCheck = from;
			if (to.isSystemCenter()) forSystemCenterCheck = to;
			
			loc = Misc.interpolateVector(from.getLocation(),
 					 					to.getLocation(),
 					 					progress);
			if (conLoc instanceof StarSystemAPI && 
					Misc.getDistance(loc, new Vector2f()) < 2000) { // quick hack to avoid primary star
				loc = Misc.getPointAtRadius(new Vector2f(), 2000f);
			} else {
				loc = Misc.getPointWithinRadius(loc, 100f + 900f * varianceMult);
			}
		}
		// one in hyper, one isn't
		else if (from.isInHyperspace() != to.isInHyperspace()) {
			SectorEntityToken inSystem = from;
			SectorEntityToken inHyper = to;
			float p = progress;
			if (from.isInHyperspace()) {
				inSystem = to;
				inHyper = from;
				p = 1f - progress;
			}
			
			JumpPointAPI jp = findJumpPointToUse(fleet, inSystem);
			float d1 = Misc.getDistance(inSystem, jp);
			float d2 = Misc.getDistance(jp.getLocationInHyperspace(), inHyper.getLocation());
			if (d1 < 1) d1 = 1;
			if (d2 < 1) d2 = 1;
			
			float t = d1 / (d1 + d2);
			if (p < t) { // in system on way to jump-point
				conLoc = inSystem.getContainingLocation();
				forSystemCenterCheck = inSystem;
				
				loc = Misc.interpolateVector(inSystem.getLocation(),
											jp.getLocation(),
											p / t);
				varianceMult = getVarianceMult(p / t);
			} else { // in hyper on way from jump-point to location
				conLoc = inHyper.getContainingLocation();
				loc = Misc.interpolateVector(Misc.getSystemJumpPointHyperExitLocation(jp),
											inHyper.getLocation(),
											(p - t) / (1f - t));
				varianceMult = getVarianceMult((p - t) / (1f - t));
			}
			loc = Misc.getPointWithinRadius(loc, 100f + 900f * varianceMult);
		}
		// from one system to a different system
		else if (from.getContainingLocation() != to.getContainingLocation()) {
//			JumpPointAPI jp1 = Misc.findNearestJumpPointTo(from);
//			JumpPointAPI jp2 = Misc.findNearestJumpPointTo(to);
			JumpPointAPI jp1 = findJumpPointToUse(fleet, from);
			JumpPointAPI jp2 = findJumpPointToUse(fleet, to);
			
			float d1 = Misc.getDistance(from, jp1);
			float d2 = Misc.getDistance(Misc.getSystemJumpPointHyperExitLocation(jp1),
										Misc.getSystemJumpPointHyperExitLocation(jp1));
			float d3 = Misc.getDistance(jp2, to);
			if (d1 < 1) d1 = 1;
			if (d2 < 1) d2 = 1;
			if (d3 < 1) d3 = 1;
			
			float t1 = d1 / (d1 + d2 + d3);			
			float t2 = (d1 + d2) / (d1 + d2 + d3);
			
			if (progress < t1) { // from "from" to jump-point
				conLoc = from.getContainingLocation();
				forSystemCenterCheck = from;
				
				loc = Misc.interpolateVector(from.getLocation(),
											 jp1.getLocation(),
											 progress / t1);
				varianceMult = getVarianceMult(progress / t1);
			} else if (progress < t2) { // in hyperspace, traveling between systems
				conLoc = Global.getSector().getHyperspace();
				loc = Misc.interpolateVector(Misc.getSystemJumpPointHyperExitLocation(jp1),
											 Misc.getSystemJumpPointHyperExitLocation(jp2),
						 				    (progress - t1) / (t2 - t1));
				varianceMult = getVarianceMult((progress - t1) / (t2 - t1));
			} else { // in the "to" system, going from jp2 to to
				conLoc = to.getContainingLocation();
				forSystemCenterCheck = to;
				
				loc = Misc.interpolateVector(jp2.getLocation(),
						 					 to.getLocation(),
						 					 (progress - t2) / (1f - t2));
				varianceMult = getVarianceMult((progress - t2) / (1f - t2));
			}
			loc = Misc.getPointWithinRadius(loc, 100f + 900f * varianceMult);
		}

		
		if (forSystemCenterCheck != null && forSystemCenterCheck.isSystemCenter() &&
				conLoc == forSystemCenterCheck.getContainingLocation()) {
			loc = Misc.getPointAtRadius(forSystemCenterCheck.getLocation(), forSystemCenterCheck.getRadius() + 3000f + (float) Math.random() * 2000f);
		}
		
//		loc = Misc.getPointWithinRadius(loc, 
//				route.getMarket().getPrimaryEntity().getRadius() + 100 + (float) Math.random() * 100f);
		
		// failsafes
		if (conLoc == null) conLoc = from.getContainingLocation();
		if (loc == null) loc = new Vector2f(from.getLocation());
		
		
		if (fleet.getContainingLocation() != conLoc) {
			fleet.getContainingLocation().removeEntity(fleet);
			conLoc.addEntity(fleet);
		}
		fleet.setLocation(loc.x, loc.y);
	}
	
	
	public static float getVarianceMult(float p) {
		float varianceMult = 1f;
		if (p < 0.1f) {
			varianceMult = p / 0.1f;
		} else if (p > 0.9f) {
			varianceMult = (1f - p) / 0.1f;
		}
		return varianceMult;
	}
	
	
	public static JumpPointAPI findJumpPointToUse(CampaignFleetAPI fleet, SectorEntityToken from) {
		return findJumpPointToUse(fleet.getFaction(), from);
	}
	public static JumpPointAPI findJumpPointToUse(FactionAPI faction, SectorEntityToken from) {
		float min = Float.MAX_VALUE;
		JumpPointAPI result = null;
		float fringeMax = 0;
		JumpPointAPI fringe = null;
		
		LocationAPI location = from.getContainingLocation();
		List<JumpPointAPI> points = location.getEntities(JumpPointAPI.class);
		
		
		for (JumpPointAPI curr : points) {
			float dist = Misc.getDistance(from.getLocation(), curr.getLocation());
			if (dist < min) {
				min = dist;
				result = curr;
			}
			dist = Misc.getDistance(new Vector2f(), curr.getLocation());
			if (dist > fringeMax) {
				fringe = curr;
				fringeMax = dist;
			}
		}
		
		if (from.getContainingLocation() instanceof StarSystemAPI) {
			StarSystemAPI system = (StarSystemAPI) from.getContainingLocation();
			boolean useFringeOnly = !isInControlOfSystemOrEven(faction, system);
			if (useFringeOnly && fringe != null) {
				return fringe;
			}
		}
		
		return result;
	}
	
	public static boolean isInControlOfSystemOrEven(FactionAPI faction, StarSystemAPI system) {
		List<MarketAPI> markets = Misc.getMarketsInLocation(system);
		int hostileMax = 0;
		int ourMax = 0;
		for (MarketAPI market : markets) {
			if (market.getFaction().isHostileTo(faction)) {
				hostileMax = Math.max(hostileMax, market.getSize());
			} else if (market.getFaction() == faction) {
				ourMax = Math.max(ourMax, market.getSize());
			}
		}
		boolean inControl = ourMax >= hostileMax;
		return inControl;
	}
}








