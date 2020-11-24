package com.fs.starfarer.api.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface FleetDataAPI {
	/**
	 * Returns:
	 * "logistical priority" members first, then non-mothballed, then mothballed.
	 * Retains normal order within each category.
	 * @return
	 */
	List<FleetMemberAPI> getMembersInPriorityOrder();
	
	List<FleetMemberAPI> getMembersListCopy();
	List<FleetMemberAPI> getCombatReadyMembersListCopy();
	float getFleetPointsUsed();
	void addFleetMember(FleetMemberAPI member);
	void removeFleetMember(FleetMemberAPI member);
	
	void clear();
	
	/**
	 * Removes from the fleet, adds fuel/supplies gained from scuttling, adds
	 * any equipped weapons to cargo.
	 * @param member
	 */
	void scuttle(FleetMemberAPI member);
	
	/**
	 * Maximum burn level of fastest ship in the fleet. Includes getStats().getFleetwideMaxBurnMod().
	 * Does NOT include mothballed ships.
	 * @return
	 */
	float getMaxBurnLevel();
	
	/**
	 * Maximum burn level of slowest ship in the fleet. Includes getStats().getFleetwideMaxBurnMod()
	 * @return
	 */
	float getMinBurnLevel();
	
	
	/**
	 * Effective burn level this fleet can go at; includes effect of being in deep hyperspace (where getMinBurnLevel() does not).
	 * @return
	 */
	float getBurnLevel();

	/**
	 * Will also set the captains of all the other ships to a new person with all-0 stats.
	 * @param flagship
	 */
	void setFlagship(FleetMemberAPI flagship);

	
	CampaignFleetAPI getFleet();

	
	/**
	 * In pixels/second. There are 10 real seconds in a day.
	 * @return
	 */
	float getTravelSpeed();

	
	/**
	 * Makes a copy of the current fleet members.  The snapshot is transient and will
	 * not be in the save file.
	 */
	void takeSnapshot();
	
	/**
	 * Returns fleet members at time snapshot was taken.
	 * Useful to get the state of the fleet before a battle/prior to it being destroyed/etc.
	 * @return
	 */
	ArrayList<FleetMemberAPI> getSnapshot();

	boolean areAnyShipsPerformingRepairs();

	void sort();

	List<OfficerDataAPI> getOfficersCopy();
	void addOfficer(PersonAPI person);
	void removeOfficer(PersonAPI person);
	OfficerDataAPI getOfficerData(PersonAPI person);
	FleetMemberAPI getMemberWithCaptain(PersonAPI captain);

	int getNumMembers();

	void syncMemberLists();
	boolean isOnlySyncMemberLists();
	void setOnlySyncMemberLists(boolean onlySyncMemberLists);

	void syncIfNeeded();

	void setSyncNeeded();

	List<FleetMemberAPI> getMembersListWithFightersCopy();

	PersonAPI getCommander();

	float getMinCrew();

	void ensureHasFlagship();

	FleetMemberAPI addFleetMember(String variantId);

	void addOfficer(OfficerDataAPI officer);

	void updateCargoCapacities();

	String pickShipName(FleetMemberAPI member, Random random);

	float getEffectiveStrength();

}



