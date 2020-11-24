package com.fs.starfarer.api.impl.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class CustomFleets {

	/**
	 * To add a new fleet:
	 * 1) Make a copy of this method
	 * 2) Call it from spawn()
	 */
	private void spawnTestFleet() {
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.DIKTAT, "Custom Fleet Name", true);
		
		FleetDataAPI data = fleet.getFleetData();
		FleetMemberAPI member = null;
		
		// add a fleet member with a custom name
		member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "onslaught_Standard");
		member.setShipName("SDS Andrada");
		data.addFleetMember(member);
		
		// add a ship and a fighter
		data.addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP, "tempest_Attack"));
		data.addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, "wasp_wing"));
		
		
		
		
		// makes fleet not need supplies or fuel or crew
		FleetFactory.finishAndSync(fleet);
		
		// add fleet to a star system and set its location 
		LocationAPI location = Global.getSector().getStarSystem("askonia");
		location.addEntity(fleet);
		
		SectorEntityToken planet = location.getEntityById("sindria");
		fleet.setLocation(planet.getLocation().x, planet.getLocation().y - 500);

		// give the fleet an assignment (1000000f days ~= forever)
		// the fleet tooltip will show it as "<relationship level>, doing something" - i.e. "Neutral, doing something"
		fleet.getAI().addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, planet, 1000000f, "doing something", null);
	}
	
	
	/**
	 * This is called from CoreCampaignPluginImpl.onNewGameAfterTimePass().
	 */
	public void spawn() {
		//spawnTestFleet();
	}
}
