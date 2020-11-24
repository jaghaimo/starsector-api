package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolFleetManager.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.api.util.Misc.FleetFilter;

public class PatrolAssignmentAI implements EveryFrameScript {

	private CampaignFleetAPI fleet;
	private PatrolFleetData data;
	
	private IntervalUtil tracker = new IntervalUtil(0.5f, 1.5f);

	public PatrolAssignmentAI(CampaignFleetAPI fleet, PatrolFleetData data) {
		this.fleet = fleet;
		this.data = data;
		giveInitialAssignment();
	}

	private void giveInitialAssignment() {
	
		float daysToOrbit = getDaysToOrbit() * 0.25f;
		if (daysToOrbit < 0.2f) {
			daysToOrbit = 0.2f;
		}
		//fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.sourceMarket.getPrimaryEntity(), daysToOrbit,
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.sourceMarket.getPrimaryEntity(), daysToOrbit,
				"preparing for patrol duty");
	}

	private boolean orderedReturn = false;
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		
//		if (!orderedReturn) {
//			tracker.advance(days);
//			if (tracker.intervalElapsed()) {
//				checkNPCCustomsInspection();
//			}
//			performInspectionIfNeeded();
//		}
		
		
		
//		if (fleet.getFaction().getId().equals("knights_of_ludd")) {
//			System.out.println("wesdfsd");
//		}
		if (fleet.getAI().getCurrentAssignment() != null) {
			float fp = fleet.getFleetPoints();
			if (fp < data.startingFleetPoints && !orderedReturn) {
				orderedReturn = true;
				fleet.clearAssignments();
				
				fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, data.sourceMarket.getPrimaryEntity(), 1000,
									"returning to " + data.sourceMarket.getName());
				fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.sourceMarket.getPrimaryEntity(), 1f,
									"standing down from patrol duty");
				fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, data.sourceMarket.getPrimaryEntity(), 1000);
			}
		} else {
//			if (fleet.getFaction().getId().equals("knights_of_ludd")) {
//				System.out.println("wesdfsd");
//			}
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
					
//					for (SectorEntityToken relay : system.getEntitiesWithTag(Tags.COMM_RELAY)) {
//						float weight = 10;
//						if (data.type == PatrolType.FAST) weight = 40;
//						defenseTargets.add(relay, weight);
//					}
//					defenseTargets.add(data.sourceMarket.getPrimaryEntity(), 30);
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

	
	private void performInspectionIfNeeded() {
		if (inspectionTarget != null) {
//			if (fleet.isInCurrentLocation()) {
//				System.out.println("fe423fr");
//			}
			List<CampaignFleetAPI> hostiles = Misc.findNearbyFleets(fleet, 200, new FleetFilter() {
				public boolean accept(CampaignFleetAPI curr) {
					//return curr.getFaction().isHostileTo(fleet.getFaction());
					return curr.isHostileTo(fleet);
				}
			});
			if (inspectionTarget != null) {
				float dist = Misc.getDistance(inspectionTarget.getLocation(), fleet.getLocation());
				if (dist > 2000 || inspectionTarget.getContainingLocation() != fleet.getContainingLocation()) {
					inspectionTarget.getAI().removeFirstAssignmentIfItIs(FleetAssignment.HOLD);
					fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.HOLD);
					fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.FOLLOW);
					fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.HOLD);
					fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.FOLLOW);
					//fleet.getAI().addAssignmentAtStart(FleetAssignment.FOLLOW, target, 2.5f, "performing customs inspection", null);
					inspectionTarget = null;
					fleet.getMemoryWithoutUpdate().unset("$performingNPCInspection"); 
					return;
				}
			}
			
			if (fleet.getMemoryWithoutUpdate().contains("$performingNPCInspection") && hostiles.isEmpty()) {
				float dist = Misc.getDistance(inspectionTarget.getLocation(), fleet.getLocation());
				float radSum = inspectionTarget.getRadius() + fleet.getRadius();
				FleetAssignmentDataAPI curr = fleet.getAI().getCurrentAssignment();
				Vector2f offset = Vector2f.sub(fleet.getLocation(), inspectionTarget.getLocation(), new Vector2f());
				Misc.normalise(offset);
				offset.scale(radSum);
				Vector2f.add(inspectionTarget.getLocation(), offset, offset);
				SectorEntityToken loc = fleet.getContainingLocation().createToken(offset.x, offset.y);
				
				boolean forceReapproach = false;
				if (curr != null && curr.getTarget() != null) {
					float locDist = Misc.getDistance(inspectionTarget.getLocation(), curr.getTarget().getLocation());
					forceReapproach = locDist > radSum + 5f;
				}
//				if (fleet.isInCurrentLocation()) {
//					System.out.println("dsfwefe");
//				}
				if ((dist - radSum > 5 || dist - radSum < -5) && (curr == null || curr.getAssignment() != FleetAssignment.FOLLOW || forceReapproach)) {
					fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.HOLD);
					fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.FOLLOW);
					//fleet.getAI().addAssignmentAtStart(FleetAssignment.FOLLOW, inspectionTarget, 2f, "approaching " + inspectionTarget.getName(), null);
					if (dist - radSum <= 50) {
						fleet.getAI().addAssignmentAtStart(FleetAssignment.FOLLOW, loc, 0.1f, "performing customs inspection", null);
					} else {
						fleet.getAI().addAssignmentAtStart(FleetAssignment.FOLLOW, loc, 0.1f, "approaching " + inspectionTarget.getName(), null);
					}
				} else if ((dist - radSum <= 5 && dist - radSum >= -5) && (curr == null || curr.getAssignment() != FleetAssignment.HOLD)) {
					fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.FOLLOW);
					fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.HOLD);
					
					//fleet.getAI().addAssignmentAtStart(FleetAssignment.HOLD, inspectionTarget, 2f, "performing customs inspection", null);
					//fleet.getAI().addAssignmentAtStart(FleetAssignment.HOLD, loc, 2f, "performing customs inspection", null);
					fleet.getAI().addAssignmentAtStart(FleetAssignment.HOLD, null, 0.1f, "performing customs inspection", null);
				}
			} else {
				inspectionTarget.getAI().removeFirstAssignmentIfItIs(FleetAssignment.HOLD);
				fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.HOLD);
				fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.FOLLOW);
				fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.HOLD);
				fleet.getAI().removeFirstAssignmentIfItIs(FleetAssignment.FOLLOW);
				//fleet.getAI().addAssignmentAtStart(FleetAssignment.FOLLOW, target, 2.5f, "performing customs inspection", null);
				inspectionTarget = null;
				fleet.getMemoryWithoutUpdate().unset("$performingNPCInspection");
			}
		}
	}

	private CampaignFleetAPI inspectionTarget = null;
	private void checkNPCCustomsInspection() {
		
		//if (!fleet.isInCurrentLocation()) return;
		//if ((float) Math.random() < 0.75f) return;
		
		if (inspectionTarget != null) return;
		if (fleet.getMemoryWithoutUpdate().contains(MemFlags.FLEET_BUSY)) return;
		if (!fleet.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR)) return;
		
		if (fleet.getAI() != null && 
				!fleet.getAI().isCurrentAssignment(FleetAssignment.PATROL_SYSTEM) && 
				!fleet.getAI().isCurrentAssignment(FleetAssignment.DEFEND_LOCATION)) return;
		
		MarketAPI closest = Misc.findNearestLocalMarketWithSameFaction(fleet, 1500);
		if (closest == null || !closest.getFactionId().equals(fleet.getFaction().getId())) return;
		
		List<CampaignFleetAPI> hostiles = Misc.findNearbyFleets(fleet, 600, new FleetFilter() {
			public boolean accept(CampaignFleetAPI curr) {
				//return curr.getFaction().isHostileTo(fleet.getFaction());
				return curr.isHostileTo(fleet);
			}
		});
		if (!hostiles.isEmpty()) return;
		
		
		List<CampaignFleetAPI> allFleets = new ArrayList<CampaignFleetAPI>(fleet.getContainingLocation().getFleets());
		allFleets.addAll(fleet.getContainingLocation().getFleets());
		WeightedRandomPicker<CampaignFleetAPI> picker = new WeightedRandomPicker<CampaignFleetAPI>();
		for (final CampaignFleetAPI curr : allFleets) {
			if (curr == fleet) continue;
			//if (curr.getFaction().isHostileTo(fleet.getFaction())) continue;
			if (curr.isHostileTo(fleet)) continue;

			if (curr.isInHyperspaceTransition()) continue;
			
			if (!curr.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_SMUGGLER) && 
					!curr.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_TRADE_FLEET)) continue;

			if (curr.getMemoryWithoutUpdate().contains("$recentlyInspected")) return;
			if (curr.getMemoryWithoutUpdate().contains(MemFlags.FLEET_BUSY)) return;
			
			float dist = Misc.getDistance(curr.getLocation(), fleet.getLocation());
			if (dist > 1000) continue;
			if (dist < 100) dist = 100f;
			
			hostiles = Misc.findNearbyFleets(curr, 600, new FleetFilter() {
				public boolean accept(CampaignFleetAPI curr) {
					//return curr.getFaction().isHostileTo(curr.getFaction());
					return curr.isHostileTo(fleet);
				}
			});
			if (!hostiles.isEmpty()) continue;

			picker.add(curr, 1000f / dist);
		}
		
		if (picker.isEmpty()) return;
		
		CampaignFleetAPI target = picker.pick();

		target.getMemoryWithoutUpdate().set("$recentlyInspected", true, 15f);
		target.getMemoryWithoutUpdate().set(MemFlags.FLEET_BUSY, true, 3f);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_BUSY, true, 3f);
		fleet.getMemoryWithoutUpdate().set("$performingNPCInspection", true, 2f);
		//fleet.getMemoryWithoutUpdate().set("$performingNPCInspection", true, 3f);
		
		target.getAI().addAssignmentAtStart(FleetAssignment.HOLD, null, 2f, "standing by for inspection", null);
		//fleet.getAI().addAssignmentAtStart(FleetAssignment.FOLLOW, target, 2.5f, "performing customs inspection", null);
		
		inspectionTarget = target;
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





