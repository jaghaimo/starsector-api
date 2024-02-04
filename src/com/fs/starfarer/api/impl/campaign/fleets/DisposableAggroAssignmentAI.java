package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class DisposableAggroAssignmentAI implements EveryFrameScript {

	protected StarSystemAPI system;
	protected CampaignFleetAPI fleet;
	protected DisposableFleetManager manager;
	
	
	public DisposableAggroAssignmentAI(CampaignFleetAPI fleet, StarSystemAPI system, 
									   DisposableFleetManager manager, float probStayInHyper) {
		this.fleet = fleet;
		this.system = system;
		this.manager = manager;
		
		giveInitialAssignments(probStayInHyper);
	}
	
	protected void giveInitialAssignments(float probStayInHyper) {
		boolean playerInSameLocation = fleet.getContainingLocation() == Global.getSector().getCurrentLocation();
		
		SectorEntityToken target = null;
		if (!playerInSameLocation && !fleet.isInHyperspace()) { // in system, player is in hyper
			target = pickEntityToGuard(new Random(), system, fleet);
			if (target != null) {
				Vector2f loc = Misc.getPointAtRadius(target.getLocation(), target.getRadius() + 100f);
				fleet.setLocation(loc.x, loc.y);
			} else {
				Vector2f loc = Misc.getPointAtRadius(new Vector2f(), 5000f);
				fleet.setLocation(loc.x, loc.y);
			}
		} else if (fleet.isInHyperspace()) { // fleet in hyper; don't care about player being there or not
			SectorEntityToken from = Misc.getSourceEntity(fleet);
			if (from != null && system != null) {
				float angle = Misc.getAngleInDegrees(system.getLocation(), from.getLocationInHyperspace());
				float arc = 90f;
				angle = angle - arc / 2f + arc * (float) Math.random();
				//float dist = Global.getSettings().getMaxSensorRangeHyper() + 500f + 1000f * (float) Math.random();
				float dist = Global.getSettings().getMaxSensorRangeHyper() + 1000f;
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(dist);
				Vector2f.add(loc, system.getLocation(), loc);
				
				loc = Misc.pickHyperLocationNotNearPlayer(new Vector2f(loc), Global.getSettings().getMaxSensorRangeHyper() + 500f);
				
				fleet.setLocation(loc.x, loc.y);
			} else {
				Vector2f loc = Misc.pickHyperLocationNotNearPlayer(system.getLocation(), Global.getSettings().getMaxSensorRangeHyper() + 500f);
				fleet.setLocation(loc.x, loc.y);
			}
		} else { // player in same location, and in-system
			target = pickEntityToGuard(new Random(), system, fleet);
			Vector2f loc = new Vector2f(5000, 0);
			if (target != null) {
				Vector2f from = target.getLocation();
				loc = Misc.pickLocationNotNearPlayer(system, from, 
						Global.getSettings().getMaxSensorRange() + 500f);
			} else { // no jump points case; ???
				loc = Misc.pickLocationNotNearPlayer(system, 
									Misc.getPointAtRadius(new Vector2f(0, 0), 8000),
									Global.getSettings().getMaxSensorRange() + 500f);
			}
			fleet.setLocation(loc.x, loc.y);
		}
		pickNext(target, probStayInHyper);
	}
	
	protected void pickNext(SectorEntityToken target, float probStayInHyper) {
		if (fleet.isInHyperspace()) {
			Vector2f dest = Misc.getPointAtRadius(system.getLocation(), 1000);
			LocationAPI hyper = Global.getSector().getHyperspace();
			SectorEntityToken token = hyper.createToken(dest.x, dest.y);
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, token, 1000, 
								manager.getTravelText(system, fleet));
		}
		
		if (fleet.isInHyperspace() && (float) Math.random() < probStayInHyper) {
			fleet.addAssignment(FleetAssignment.RAID_SYSTEM, system.getHyperspaceAnchor(), 10000,
								manager.getActionOutsideText(system, fleet));
		} else {
			if (target == null) target = pickEntityToGuard(new Random(), system, fleet);
			if (target != null) {
				float speed = Misc.getSpeedForBurnLevel(8);
				float dist = Misc.getDistance(fleet.getLocation(), target.getLocation());
				float seconds = dist / speed;
				float days = seconds / Global.getSector().getClock().getSecondsPerDay();
				days += 30f + 10f * (float) Math.random();
				fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, days,
									manager.getActionInsideText(system, fleet));
				return;
			} else {
				float days = 5f + 5f * (float) Math.random();
				fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, null, days,
									manager.getActionInsideText(system, fleet));
			}
		}
	}

	public void advance(float amount) {
		if (fleet.getCurrentAssignment() == null) {
			pickNext(null, 0f);
		}
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
	
	public static SectorEntityToken pickEntityToGuard(Random random, StarSystemAPI system, CampaignFleetAPI fleet) {
		WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(random);
		
		for (SectorEntityToken entity : system.getJumpPoints()) {
			float mult = getWeightMultForPatrols(system, fleet, entity);
			picker.add(entity, 5f * mult);
		}
		
		// gas giants are exits from hyperspace, so hang around there too
		for (PlanetAPI planet : system.getPlanets()) {
			if (planet.isGasGiant()) {
				float mult = getWeightMultForPatrols(system, fleet, planet);
				picker.add(planet, 5f * mult);
			}
		}
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
			if (market.getFaction() != fleet.getFaction()) continue;
			
			float mult = getWeightMultForPatrols(system, fleet, market.getPrimaryEntity());
			picker.add(market.getPrimaryEntity(), 5f * mult);
		}
		
		return picker.pick();
	}
	
	public static float getWeightMultForPatrols(StarSystemAPI system, CampaignFleetAPI fleet, SectorEntityToken entity) {
		float count = countNearbyPatrols(system, fleet, entity);
		return 1f / (count * 10f + 1f);
	}
	
	public static float countNearbyPatrols(StarSystemAPI system, CampaignFleetAPI fleet, SectorEntityToken entity) {
		float count = 0;
		for (CampaignFleetAPI other : system.getFleets()) {
			float dist = Misc.getDistance(other, entity);
			if (dist > 3000) continue;
			
			if (!Misc.isPatrol(other)) continue;
			
			if (fleet.isHostileTo(other)) {
				count++;
			}
		}
		return count;
	}

}










