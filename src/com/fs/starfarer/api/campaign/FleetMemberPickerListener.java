package com.fs.starfarer.api.campaign;

import java.util.List;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface FleetMemberPickerListener {
	void pickedFleetMembers(List<FleetMemberAPI> members);
	void cancelledFleetMemberPicking();
}
