package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FleetActionTextProvider;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator.TaskInterval;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class PatrolAssignmentAIV4 extends RouteFleetAssignmentAI implements FleetActionTextProvider {

	public static final String PREP_STAGE = "a";
	public static final String TRAVEL_TO_STAGE = "b";
	public static final String PATROL_STAGE = "c";
	public static final String RETURN_STAGE = "d";
	public static final String STAND_DOWN_STAGE = "e";
	
	
	public PatrolAssignmentAIV4(CampaignFleetAPI fleet, RouteData route) {
		super(fleet, route);
	}
	

	@Override
	protected void giveInitialAssignments() {
		//super.giveInitialAssignments();
		
		
		SectorEntityToken target = pickEntityToGuard();
		if (target == null) return;
		
		RouteSegment current = route.getCurrent();
		SectorEntityToken source = route.getMarket().getPrimaryEntity();

		TaskInterval [] intervals = new TaskInterval[] {
				TaskInterval.days(3f + (float) Math.random() * 3f),
				TaskInterval.travel(),
				TaskInterval.remaining(1f),
				TaskInterval.travel(),
				TaskInterval.days(3f + (float) Math.random() * 3f),
		};
		
		RouteLocationCalculator.computeIntervalsAndSetLocation(fleet, current.elapsed, current.daysMax,
															false, intervals, 
															source, source, target, target, source, source);
		
		fleet.clearAssignments();
		
		// time to spend traveling to location and patrolling it
		float combinedTravelAndPatrolTime = intervals[1].value + intervals[2].value;
		
		if (intervals[0].value > 0) {
			fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source, intervals[0].value, PREP_STAGE);
		}
		if (intervals[1].value > 0) {
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, target, combinedTravelAndPatrolTime, TRAVEL_TO_STAGE, 
								true, null, null);
			combinedTravelAndPatrolTime = 0f;
		}
		if (intervals[2].value > 0) {
			fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, combinedTravelAndPatrolTime, PATROL_STAGE,
					false,
				target.isSystemCenter() ? new Script() {
					public void run() {
						fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
					}} : null,
				target.isSystemCenter() ? new Script() {
					public void run() {
						fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT);
					}} : null
			);
		}
		
		
		if (intervals[3].value > 0) { // return for as long as it takes, not just the interval value
			fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, source, 1000f, RETURN_STAGE);
		}
		
		// if there was no return stage, that means we spawned right in orbit, since
		// here always justSpawned == true 
		fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, source, intervals[4].value, STAND_DOWN_STAGE);
		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, source, 1000f,
							STAND_DOWN_STAGE, goNextScript(current));
		
		fleet.getAI().setActionTextProvider(this);
		
	}

	public String getActionText(CampaignFleetAPI fleet) {
//		if (Misc.getDistance(Global.getSector().getPlayerFleet(), fleet) < fleet.getRadius()) {
//			System.out.println("ewfwefwe");
//		}
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (curr == null) return null;
		
		String stage = curr.getActionText();
		SectorEntityToken target = curr.getTarget();
		
		String name = "";
		if (target != null) {
			name = target.getName();
			if (target instanceof CustomCampaignEntityAPI) {
				CustomCampaignEntityAPI cce = (CustomCampaignEntityAPI) target;
				if (name.equals(cce.getCustomEntitySpec().getDefaultName())) {
					//name = name.toLowerCase();
					name = cce.getCustomEntitySpec().getNameInText();
				}
			}
		}
		
		boolean pirate = fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_PIRATE);
		
		if (pirate) {
			if (PREP_STAGE.equals(stage)) {
				return "preparing for patrol duty";
			} else if (TRAVEL_TO_STAGE.equals(stage) && target != null && !target.isSystemCenter() && !target.isInHyperspace()) {
				return "traveling to " + name;
			} else if (TRAVEL_TO_STAGE.equals(stage)) {
				return "traveling";
			} else if (PATROL_STAGE.equals(stage) && target != null) {
				if (target.hasTag(Tags.OBJECTIVE)) {
					return "guarding " + name;
				} else if (target.hasTag(Tags.JUMP_POINT)) {
					return "guarding " + name;
				} else if (target.getMarket() != null) {
					return "defending " + name;
				} else {
					return "patrolling";
				}
			} else if (RETURN_STAGE.equals(stage) && target != null && !target.isSystemCenter()) {
				return "returning to " + name;
			} else if (STAND_DOWN_STAGE.equals(stage)) {
				return "standing down";
			}
		} else {
			if (PREP_STAGE.equals(stage)) {
				return "preparing for patrol duty";
			} else if (TRAVEL_TO_STAGE.equals(stage) && target != null && !target.isSystemCenter() && !target.isInHyperspace()) {
				return "traveling to " + name;
			} else if (TRAVEL_TO_STAGE.equals(stage)) {
				return "traveling";
			} else if (PATROL_STAGE.equals(stage) && target != null) {
				if (target.hasTag(Tags.OBJECTIVE)) {
					return "guarding " + name;
				} else if (target.hasTag(Tags.JUMP_POINT)) {
					return "guarding " + name;
				} else if (target.getMarket() != null) {
					return "patrolling around " + name;
				} else {
					return "patrolling";
				}
			} else if (RETURN_STAGE.equals(stage) && target != null && !target.isSystemCenter()) {
				return "returning to " + name;
			} else if (STAND_DOWN_STAGE.equals(stage)) {
				return "standing down from patrol duty";
			}
		}
		
		//"traveling to " + target.getName()
		//return "patrolling";
		return null;
	}

	@Override
	public void advance(float amount) {
//		if (Misc.getDistance(Global.getSector().getPlayerFleet(), fleet) < fleet.getRadius()) {
//			System.out.println("ewfwefwe");
//		}
		super.advance(amount);
		
		checkCapture(amount);
		checkBuild(amount);
	}
	
	
	public SectorEntityToken pickEntityToGuard() {
		Random random = route.getRandom(1);
		
		PatrolFleetData custom = (PatrolFleetData) route.getCustom();
		PatrolType type = custom.type;
		
		LocationAPI loc = fleet.getContainingLocation();
		if (loc == null) return null;
		
		WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(random);
		
		CountingMap<SectorEntityToken> existing = new CountingMap<SectorEntityToken>();
		for (RouteData data : RouteManager.getInstance().getRoutesForSource(route.getSource())) {
			CampaignFleetAPI other = data.getActiveFleet();
			if (other == null) continue;
			FleetAssignmentDataAPI curr = other.getCurrentAssignment();
			if (curr == null || curr.getTarget() == null || 
					curr.getAssignment() != FleetAssignment.PATROL_SYSTEM) {
				continue;
			}
			existing.add(curr.getTarget());
		}

		List<MarketAPI> markets = Misc.getMarketsInLocation(fleet.getContainingLocation());
		int hostileMax = 0;
		int ourMax = 0;
		for (MarketAPI market : markets) {
			if (market.getFaction().isHostileTo(fleet.getFaction())) {
				hostileMax = Math.max(hostileMax, market.getSize());
			} else if (market.getFaction() == fleet.getFaction()) {
				ourMax = Math.max(ourMax, market.getSize());
			}
		}
		boolean inControl = ourMax > hostileMax;
		
		for (SectorEntityToken entity : loc.getEntitiesWithTag(Tags.OBJECTIVE)) {
			if (entity.getFaction() != fleet.getFaction()) continue;
			
			float w = 2f;
			for (int i = 0; i < existing.getCount(entity); i++) w *= 0.1f;

			if (type == PatrolType.HEAVY) w *= 0.1f;
			
			picker.add(entity, w);
		}
		
		// patrol stable locations, will build there
		for (SectorEntityToken entity : loc.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
			float w = 2f;
			for (int i = 0; i < existing.getCount(entity); i++) w *= 0.1f;

			if (type == PatrolType.HEAVY) w *= 0.1f;
			
			picker.add(entity, w);
		}
		
		if (inControl) {
			for (SectorEntityToken entity : loc.getJumpPoints()) {
				float w = 2f;
				for (int i = 0; i < existing.getCount(entity); i++) w *= 0.1f;
				
				if (type == PatrolType.HEAVY) w *= 0.1f;
				
				picker.add(entity, w);
			}
			
			if (loc instanceof StarSystemAPI && custom.type == PatrolType.HEAVY) {
				StarSystemAPI system = (StarSystemAPI) loc;
				if (system.getHyperspaceAnchor() != null) {
					float w = 3f;
					for (int i = 0; i < existing.getCount(system.getHyperspaceAnchor()); i++) w *= 0.1f;
					picker.add(system.getHyperspaceAnchor(), w);
				}
			}
		}
		
		for (MarketAPI market : markets) {
			if (market.getFaction().isHostileTo(fleet.getFaction())) continue;
			
			float w = 0f;
			if (market == route.getMarket()) {
				w = 5f;
			} else {
				// defend on-hostile non-military markets; prefer own faction
				//if (!market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
				if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_PATROL)) {
					if (market.getFaction() != fleet.getFaction()) {
						w = 0f; // don't patrol near patrolHQ/military markets of another faction
					} else {
						w = 4f;
					}
				}
			}
			
			for (int i = 0; i < existing.getCount(market.getPrimaryEntity()); i++) w *= 0.1f;
			picker.add(market.getPrimaryEntity(), w);
		}
		
		if (fleet.getContainingLocation() instanceof StarSystemAPI && type != PatrolType.HEAVY) {
			StarSystemAPI system = (StarSystemAPI) fleet.getContainingLocation();
			float w = 1f;
			for (int i = 0; i < existing.getCount(system.getCenter()); i++) w *= 0.1f;
			picker.add(system.getCenter(), w);
		}
		
		SectorEntityToken target = picker.pick();
		if (target == null) {
			target = route.market.getPrimaryEntity();
		}
		
		return target;
	}


	
}















