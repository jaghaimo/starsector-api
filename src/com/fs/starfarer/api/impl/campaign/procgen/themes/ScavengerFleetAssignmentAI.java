package com.fs.starfarer.api.impl.campaign.procgen.themes;

import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class ScavengerFleetAssignmentAI extends RouteFleetAssignmentAI {

	protected boolean pirate;
	protected IntervalUtil piracyCheck = new IntervalUtil(0.2f, 0.4f);
	public ScavengerFleetAssignmentAI(CampaignFleetAPI fleet, RouteData route, boolean pirate) {
		super(fleet, route);
		this.pirate = pirate;
	}
	
	@Override
	protected String getTravelActionText(RouteSegment segment) {
		//if (segment.systemTo == route.getMarket().getContainingLocation()) {
		if (segment.to == route.getMarket().getPrimaryEntity()) {
			return "returning to " + route.getMarket().getName();
		}
		return "on a salvage expedition";
	}
	
	@Override
	protected String getInSystemActionText(RouteSegment segment) {
		return "exploring";
	}


	@Override
	protected void addLocalAssignment(RouteSegment segment, boolean justSpawned) {
		//boolean pickSpecificEntity = (float) Math.random() > 0.2f && segment.systemFrom instanceof StarSystemAPI;
		boolean pickSpecificEntity = (float) Math.random() > 0.2f && !segment.from.getContainingLocation().isHyperspace();
		if (pickSpecificEntity) {
			SectorEntityToken target = RemnantSeededFleetManager.pickEntityToGuard(new Random(), (StarSystemAPI) segment.from.getContainingLocation(), fleet);
			if (target != null) {
				if (justSpawned) {
					Vector2f loc = Misc.getPointAtRadius(new Vector2f(target.getLocation()), 500);
					fleet.setLocation(loc.x, loc.y);
				}
				
				float speed = Misc.getSpeedForBurnLevel(8);
				float dist = Misc.getDistance(fleet.getLocation(), target.getLocation());
				float seconds = dist / speed;
				float days = seconds / Global.getSector().getClock().getSecondsPerDay();
				days += 5f + 5f * (float) Math.random();
				fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, days, "investigating");
				return;
			} else {
				if (justSpawned) {
					Vector2f loc = Misc.getPointAtRadius(new Vector2f(), 8000);
					fleet.setLocation(loc.x, loc.y);
				}
				
				float days = 5f + 5f * (float) Math.random();
				fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, null, days, "exploring");
			}
		} else {
			super.addLocalAssignment(segment, justSpawned);
		}
	}

	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (pirate) {
			float days = Global.getSector().getClock().convertToDays(amount);
			piracyCheck.advance(days);
			if (piracyCheck.intervalElapsed()) {
				doPiracyCheck();
			}
		}
	}
	
	protected void doPiracyCheck() {
		if (fleet.getBattle() != null) return;
		
		
		boolean isCurrentlyPirate = fleet.getFaction().getId().equals(Factions.PIRATES);
		
		if (fleet.isTransponderOn() && !isCurrentlyPirate) {
			return;
		}
		
		if (isCurrentlyPirate) {
			List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);
			if (visible.isEmpty()) {
				fleet.setFaction(Factions.INDEPENDENT, true);
				Misc.clearTarget(fleet, true);
			}
			return;
		}
		
		List<CampaignFleetAPI> visible = Misc.getVisibleFleets(fleet, false);
		if (visible.size() == 1) {
			int weakerCount = 0;
			for (CampaignFleetAPI other : visible) {
				if (fleet.getAI() != null && 
						Global.getSector().getFaction(Factions.PIRATES).isHostileTo(other.getFaction())) {
					EncounterOption option = fleet.getAI().pickEncounterOption(null, other, true);
					if (option == EncounterOption.ENGAGE || option == EncounterOption.HOLD) {
						float dist = Misc.getDistance(fleet.getLocation(), other.getLocation());
						VisibilityLevel level = other.getVisibilityLevelTo(fleet);
						boolean seesComp = level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS ||
										   level == VisibilityLevel.COMPOSITION_DETAILS;
						if (dist < 800f && seesComp) {
							weakerCount++;
						}
					}
				}
			}
		
			if (weakerCount == 1) {
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
				fleet.setNoFactionInName(true);
				fleet.setFaction(Factions.PIRATES, true);
			}
		}
		
	}
	


	
	
}










