package com.fs.starfarer.api.campaign;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetLogisticsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;

/**
 * @author Alex Mosolov
 *
 * Copyright 2012 Fractal Softworks, LLC
 */
public interface CampaignFleetAPI extends SectorEntityToken, FleetOrStubAPI {

	/**
	 * @return whether the fleet's LocationAPI is the same one the player's fleet is currently in.
	 */
	boolean isInCurrentLocation();
	boolean isInHyperspace();
	
	/**
	 * Use this to set the location. DO NOT use getLocation().x = <new x> etc, that won't work.
	 * @param x
	 * @param y
	 */
	void setLocation(float x, float y);
	
	//boolean isAlive();
	
	void despawn();
	void despawn(FleetDespawnReason reason, Object param);
	
	void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, Script onCompletion);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText, Script onCompletion);
	void clearAssignments();
	void setPreferredResupplyLocation(SectorEntityToken token);
	
	
	//FactionAPI getFaction();
	Vector2f getVelocity();
	
	/* (non-Javadoc)
	 * Do not use to set the location, it won't work.
	 * 
	 * Use setLocation instead.
	 * @see com.fs.starfarer.api.campaign.SectorEntityToken#getLocation()
	 */
	Vector2f getLocation();
	
	FleetLogisticsAPI getLogistics();
	
	LocationAPI getContainingLocation();
	
	PersonAPI getCommander();
	MutableCharacterStatsAPI getCommanderStats();
	FleetMemberAPI getFlagship();
	boolean isPlayerFleet();
	
	FleetDataAPI getFleetData();
//	List<FleetMemberAPI> getMembersListCopy();
//	List<FleetMemberAPI> getCombatReadyMembersListCopy();
//	int getFleetPoints();
//	void addFleetMember(FleetMemberAPI member);
//	void removeFleetMember(FleetMemberAPI member);
	
	void removeFleetMemberWithDestructionFlash(FleetMemberAPI member);
	
	void setName(String name);
	float getTotalSupplyCostPerDay();
	int getNumCapitals();
	int getNumCruisers();
	int getNumDestroyers();
	int getNumFrigates();
	int getNumFighters();
	/**
	 * Updates numCapitals/numCruisers/etc
	 */
	void updateCounts();
	
	float getTravelSpeed();
	
	CampaignFleetAIAPI getAI();

	int getFleetPoints();

	String getNameWithFaction();
	String getName();
	
	
	/**
	 * @return true if the fleet is not empty and doesn't consist entirely of fighter wings.
	 */
	boolean isValidPlayerFleet();

	void setNoEngaging(float seconds);

	MutableFleetStatsAPI getStats();

	
	/**
	 * Used by the AI to control the fleet as well, so it's not a reliable way to
	 * order a fleet around as the AI will be calling this method every frame.
	 * @param x
	 * @param y
	 */
	void setMoveDestination(float x, float y);
	
	/**
	 * Overrides AI and player input.
	 * @param x
	 * @param y
	 */
	void setMoveDestinationOverride(float x, float y);
	
	/**
	 * The fleet is trying to interact with this entity - i.e. engage an enemy fleet, use a wormhole, etc.
	 * @return
	 */
	SectorEntityToken getInteractionTarget();
	
	void setInteractionTarget(SectorEntityToken target);
	
	boolean isInHyperspaceTransition();
	
	
	/**
	 * Turns off supplies/fuel use, accidents, and ship crew requirements. Locks the LR to 100%.
	 * @param aiMode
	 */
	void setAIMode(boolean aiMode);
	boolean isAIMode();
	
	
	/**
	 * fighter, frigate: 1
	 * destroyer: 2
	 * cruiser: 3
	 * capital: 5
	 * @return
	 */
	int getFleetSizeCount();
	
	
	void setNoFactionInName(boolean noFactionInName);
	boolean isNoFactionInName();
	void setCommander(PersonAPI commander);
	
	
	/**
	 * Makes sure the fleet's capacities, crew levels, etc match the fleet composition and cargo.
	 * Also reapplies any fleet buffs, effects of hullmods, skills, etc. 
	 */
	void forceSync();
	
	/**
	 * True if the player's transponder is on, or it has seen the player with the transponder on and
	 * hasn't lost track of them since that point.
	 * @return
	 */
	boolean knowsWhoPlayerIs();
	
	
	/**
	 * Eventually falls back to faction.isHostile(), but may return true if there's hostility
	 * due to MakeOtherFleetHostile true, a captain's relationship to the player/other captain, etc.
	 * @param other
	 * @return
	 */
	boolean isHostileTo(SectorEntityToken other);
	
	
	
	/**
	 * Returns the "view" of the fleet member in the campaign - i.e. the little ships flying around.
	 * Not every fleet member necessarily has a view.
	 * Fleets don't have any member views when they're not currently visible to the player.
	 * @return
	 */
	List<FleetMemberViewAPI> getViews();
	
	/**
	 * Returns the "view" of the fleet member in the campaign - i.e. the little ships flying around.
	 * Not every fleet member necessarily has a view.
	 * Fleets don't have any member views when they're not currently visible to the player.
	 * @return
	 */
	FleetMemberViewAPI getViewForMember(FleetMemberAPI member);
	
	
	/**
	 * Current burn level - not maximum, but how fast it's currently going.
	 * @return
	 */
	float getCurrBurnLevel();
	
	/**
	 * In pixels per second, not per day.
	 */
	void setVelocity(float x, float y);
	
	
	/**
	 * In pixels per second, not per day.
	 * @return
	 */
	float getAcceleration();
	void setFaction(String factionId, boolean includeCaptains);
	BattleAPI getBattle();
	void setBattle(BattleAPI battle);
	
	void setAI(CampaignFleetAIAPI campaignFleetAI);
	String getNameWithFactionKeepCase();
	boolean isFriendlyTo(SectorEntityToken other);
	float getBaseSensorRangeToDetect(float sensorProfile);
	
	Boolean isDoNotAdvanceAI();
	void setDoNotAdvanceAI(Boolean doNotAdvanceAI);
	List<FleetMemberAPI> getMembersWithFightersCopy();
	
//	FleetStubAPI getStub();
//	void setStub(FleetStubAPI stub);
//	
//	boolean isConvertToStub();
//	void setConvertToStub(boolean convertToStub);
	
	void setNullAIActionText(String nullAIActionText);
	String getNullAIActionText();
	
	void setStationMode(Boolean stationMode);
	boolean isStationMode();
	Boolean wasMousedOverByPlayer();
	void setWasMousedOverByPlayer(Boolean wasMousedOverByPlayer);
	boolean isDespawning();
	Vector2f getMoveDestination();
	List<FleetEventListener> getEventListeners();
	
	FleetInflater getInflater();
	void setInflater(FleetInflater inflater);
	void inflateIfNeeded();
	void deflate();
	boolean isEmpty();
	Boolean getForceNoSensorProfileUpdate();
	void setForceNoSensorProfileUpdate(Boolean forceNoSensorProfileUpdate);
	
	boolean isInflated();
	void setInflated(Boolean inflated);
	Boolean isNoAutoDespawn();
	void setNoAutoDespawn(Boolean noAutoDespawn);
	void addAssignment(FleetAssignment assignment, SectorEntityToken target,
			float maxDurationInDays, String actionText, boolean addTimeToNext,
			Script onStart, Script onCompletion);
	
	boolean isHidden();
	void setHidden(Boolean hidden);
	Boolean getAbortDespawn();
	void setAbortDespawn(Boolean abortDespawn);
	
	
	/**
	 * Sum of Misc.getMemberStrength(member, true, true, true) for all members. Cached and
	 * updated as needed.
	 * @return
	 */
	float getEffectiveStrength();
	int getNumMembersFast();
	
	void goSlowOneFrame(boolean stop);
	boolean wasSlowMoving();
	int getNumShips();
	void updateFleetView();
	
	/**
	 * Only works for the player fleet.
	 * @return
	 */
	boolean hasShipsWithUniqueSig();
	boolean getGoSlowStop();
	void goSlowOneFrame();
	boolean getGoSlowOneFrame();
	Vector2f getVelocityFromMovementModule();
	void fadeOutIndicator();
	void fadeInIndicator();
	void forceOutIndicator();
}






