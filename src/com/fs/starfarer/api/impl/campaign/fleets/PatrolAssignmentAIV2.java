package com.fs.starfarer.api.impl.campaign.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.FleetOrStubAPI;
import com.fs.starfarer.api.campaign.FleetStubAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolFleetManagerV2.PatrolFleetData;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PatrolAssignmentAIV2 implements EveryFrameScript {

	private FleetStubAPI stub;
	private PatrolFleetData data;
	
	//private IntervalUtil tracker = new IntervalUtil(0.5f, 1.5f);

	public PatrolAssignmentAIV2(FleetStubAPI stub, PatrolFleetData data) {
		this.stub = stub;
		this.data = data;
		giveInitialAssignment();
	}

	private void giveInitialAssignment() {
		float daysToOrbit = getDaysToOrbit() * 0.25f;
		if (daysToOrbit < 0.2f) {
			daysToOrbit = 0.2f;
		}
		FleetOrStubAPI fleet = getAssignable();
		//fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.sourceMarket.getPrimaryEntity(), daysToOrbit,
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.sourceMarket.getPrimaryEntity(), daysToOrbit,
				"preparing for patrol duty");
	}

	private FleetOrStubAPI getAssignable() {
		FleetOrStubAPI fleet = stub;
		if (stub.getFleet() != null) {
			fleet = stub.getFleet();
		}
		return fleet;
	}
	
	private boolean orderedReturn = false;
	public void advance(float amount) {
		//float days = Global.getSector().getClock().convertToDays(amount);
		
		FleetOrStubAPI fleet = getAssignable();
		
		if (fleet.getCurrentAssignment() != null) {
			if (fleet instanceof CampaignFleetAPI) {
				float fp = ((CampaignFleetAPI)fleet).getFleetPoints();
				if (fp < data.startingFleetPoints && !orderedReturn) {
					orderedReturn = true;
					fleet.clearAssignments();
					
					fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, data.sourceMarket.getPrimaryEntity(), 1000,
										"returning to " + data.sourceMarket.getName());
					fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.sourceMarket.getPrimaryEntity(), 1f,
										"standing down from patrol duty");
					fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, data.sourceMarket.getPrimaryEntity(), 1000);
				}
			}
		} else {
			float daysToOrbit = getDaysToOrbit();
			StarSystemAPI system = data.sourceMarket.getStarSystem();
			if (system == null) {
				fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, data.sourceMarket.getPrimaryEntity(), 20,
									"patrolling around " + data.sourceMarket.getName());
				fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, data.sourceMarket.getPrimaryEntity(), 1000,
									"returning to " + data.sourceMarket.getName());
				fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.sourceMarket.getPrimaryEntity(), daysToOrbit,
									"standing down from patrol duty");
			} else {
				if ((float) Math.random() > 0.95f) {
					fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, system.getHyperspaceAnchor(), 20,
										"patrolling around the " + system.getBaseName() + " star system");
					fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, data.sourceMarket.getPrimaryEntity(), 1000,
										"returning to " + data.sourceMarket.getName());
					fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.sourceMarket.getPrimaryEntity(), daysToOrbit,
										"standing down from patrol duty");
				} else {
					WeightedRandomPicker<SectorEntityToken> defenseTargets = new WeightedRandomPicker<SectorEntityToken>();
					SectorEntityToken generalPatrol = data.sourceMarket.getPrimaryEntity().getContainingLocation().createToken(0, 0);
					
					defenseTargets.add(generalPatrol, 30);
					
					SectorEntityToken pick = defenseTargets.pick();
					
					if (pick == generalPatrol) {
						fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, system.getStar(), 30,
								"patrolling the " + system.getBaseName() + " star system");
						fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, data.sourceMarket.getPrimaryEntity(), 1000,
								"returning to " + data.sourceMarket.getName());
						fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.sourceMarket.getPrimaryEntity(), daysToOrbit,
								"standing down from patrol duty");
					} else {
						fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, pick, 30,
								"patrolling around " + pick.getName());
						fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, data.sourceMarket.getPrimaryEntity(), 1000,
								"returning to " + data.sourceMarket.getName());
						fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.sourceMarket.getPrimaryEntity(), daysToOrbit,
								"standing down from patrol duty");
					}
				}
			}
		}
	}

	
	private float getDaysToOrbit() {
		float daysToOrbit = 0f;
		switch (data.type) {
		case FAST:
			daysToOrbit += 2f;
			break;
		case COMBAT:
			daysToOrbit += 4f;
			break;
		case HEAVY:
			daysToOrbit += 6f;
			break;
		}
		
		daysToOrbit = daysToOrbit * (0.5f + (float) Math.random() * 0.5f);
		return daysToOrbit;
	}
	
	public boolean isDone() {
		return false;
	}
	public boolean runWhilePaused() {
		return false;
	}
}







