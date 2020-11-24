/**
 * 
 */
package com.fs.starfarer.api.fleet;

/**
 * 
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public enum FleetGoal {
	ATTACK("goal_Attack"),
	
//	DEFEND("goal_Defend"),
		
	ESCAPE("goal_Escape");
	
	
	
	private String warroomTooltipId;

	private FleetGoal(String warroomTooltipId) {
		this.warroomTooltipId = warroomTooltipId;
	}
	public String getWarroomTooltipId() {
		return warroomTooltipId;
	}

}