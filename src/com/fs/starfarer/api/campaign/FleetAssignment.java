package com.fs.starfarer.api.campaign;

/**
 * @author Alex Mosolov
 *
 * Copyright 2014 Fractal Softworks, LLC
 */
public enum FleetAssignment {
	
	DELIVER_RESOURCES("Delivering resources to", true),
	DELIVER_SUPPLIES("Delivering supplies to", true),
	DELIVER_FUEL("Delivering fuel to", true),
	DELIVER_PERSONNEL("Delivering personnel to", true),
	DELIVER_CREW("Delivering crew to", true),
	DELIVER_MARINES("Delivering marines to", true),
	RESUPPLY("Resupplying at", true),
	GO_TO_LOCATION("Travelling", false),
	PATROL_SYSTEM("Patrolling system", false),
	DEFEND_LOCATION("Defending", false),
	ORBIT_PASSIVE("Orbiting", true),
	ORBIT_AGGRESSIVE("Orbiting", true),
	ATTACK_LOCATION("Attacking", true),
	RAID_SYSTEM("Raiding", false),
	GO_TO_LOCATION_AND_DESPAWN("Returning to", true),
	INTERCEPT("Intercepting", true),
	FOLLOW("Following", true),
	HOLD("Holding", false),
	STANDING_DOWN("Standing down", false),
	;
	
	String description;
	boolean addTargetName;
	private FleetAssignment(String description, boolean addTargetName) {
		this.description = description;
		this.addTargetName = addTargetName;
	}

	public String getDescription() {
		return description;
	}

	public boolean isAddTargetName() {
		return addTargetName;
	}
	
}
