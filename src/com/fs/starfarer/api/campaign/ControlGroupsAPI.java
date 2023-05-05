package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface ControlGroupsAPI {

	List<String> getGroup(int index);
	void addToGroup(int index, FleetMemberAPI member);
	int getGroupIndexForFleetMember(FleetMemberAPI member);

}
