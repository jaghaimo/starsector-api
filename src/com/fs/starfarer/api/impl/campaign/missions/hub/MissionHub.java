package com.fs.starfarer.api.impl.campaign.missions.hub;

import com.fs.starfarer.api.characters.PersonAPI;


/**
 * What additionally defines a MissionHub is that it must support certain actions in its
 * callEvent() method; see BaseMissionHub for details.
 */
public interface MissionHub {

	PersonAPI getPerson();
	void setPerson(PersonAPI person);
	
	//List<HubMission> getOfferedMissions();
	String getOpenOptionText();

}
