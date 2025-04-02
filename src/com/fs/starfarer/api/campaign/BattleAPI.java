package com.fs.starfarer.api.campaign;

import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface BattleAPI {
	
	public static enum BattleSide {
		ONE,
		TWO,
		NO_JOIN,
	}

	void genCombined();
	void genCombinedDoNotRemoveEmpty();
	void uncombine();
	CampaignFleetAPI getCombinedOne();
	CampaignFleetAPI getCombinedTwo();
	boolean canJoin(CampaignFleetAPI fleet);
	BattleSide pickSide(CampaignFleetAPI fleet);
	boolean join(CampaignFleetAPI fleet);
	boolean isPlayerInvolved();
	List<CampaignFleetAPI> getFleetsFor(EngagementResultForFleetAPI side);
	boolean isPlayerSide(EngagementResultForFleetAPI side);
	CampaignFleetAPI getCombinedFor(CampaignFleetAPI participantOrCombined);
	CampaignFleetAPI getSourceFleet(FleetMemberAPI member);
	List<CampaignFleetAPI> getSideFor(CampaignFleetAPI participantOrCombined);
	CampaignFleetAPI getPrimary(List<CampaignFleetAPI> side);
	boolean isPlayerSide(List<CampaignFleetAPI> side);
	List<CampaignFleetAPI> getPlayerSide();
	//void finish();
	void removeEmptyFleets();
	boolean isPlayerPrimary();
	//void setPlayerPrimary(boolean playerPrimary);
	
	
	boolean isDone();
	List<CampaignFleetAPI> getSideOne();
	List<CampaignFleetAPI> getSideTwo();
	List<CampaignFleetAPI> getNonPlayerSide();
	CampaignFleetAPI getPlayerCombined();
	CampaignFleetAPI getNonPlayerCombined();
	CampaignFleetAPI getCombined(BattleSide side);
	CampaignFleetAPI getOtherSideCombined(BattleSide side);
	void leave(CampaignFleetAPI fleet, boolean engagedInHostilities);
	List<CampaignFleetAPI> getSide(BattleSide side);
	List<CampaignFleetAPI> getOtherSide(BattleSide side);
	
	boolean knowsWhoPlayerIs(List<CampaignFleetAPI> side);
	BattleSide pickSide(CampaignFleetAPI fleet, boolean considerPlayerTransponderStatus);
	void takeSnapshots();
	List<CampaignFleetAPI> getSnapshotSideOne();
	List<CampaignFleetAPI> getSnapshotSideTwo();
	List<CampaignFleetAPI> getSnapshotSideFor(CampaignFleetAPI participantOrCombined);
	List<CampaignFleetAPI> getSnapshotFor(List<CampaignFleetAPI> side);
	List<CampaignFleetAPI> getBothSides();
	List<CampaignFleetAPI> getSnapshotBothSides();
	List<CampaignFleetAPI> getOtherSideFor(CampaignFleetAPI participantOrCombined);
	boolean isOnPlayerSide(CampaignFleetAPI participantOrCombined);
	List<CampaignFleetAPI> getOtherSideSnapshotFor(
			CampaignFleetAPI participantOrCombined);
	
	
	/**
	 * Snapshot before the battle, containing any fleets that may have been eliminated during.
	 * @return
	 */
	List<CampaignFleetAPI> getPlayerSideSnapshot();
	
	/**
	 * Snapshot before the battle, containing any fleets that may have been eliminated during.
	 * @return
	 */
	List<CampaignFleetAPI> getNonPlayerSideSnapshot();
	
	
	boolean isInvolved(CampaignFleetAPI test);
	float getPlayerInvolvementFraction();
	boolean hasSnapshots();
	void applyVisibilityMod(CampaignFleetAPI fleet);
	boolean onSameSide(CampaignFleetAPI one, CampaignFleetAPI two);
	boolean onPlayerSide(CampaignFleetAPI fleet);
	CampaignFleetAPI getClosestInvolvedFleetTo(CampaignFleetAPI fleet);
	//void finish(boolean engagedInHostilities);
	void finish(BattleSide winner);
	void finish(BattleSide winner, boolean engagedInHostilities);
	boolean isPlayerInvolvedAtStart();
	void setPlayerInvolvedAtStart(boolean playerInvolvedAtStart);
	void setPlayerInvolvementFraction(float playerInvolvementFraction);
	CampaignFleetAPI getPrimary(List<CampaignFleetAPI> side, boolean nonPlayer);
	Map<FleetMemberAPI, CampaignFleetAPI> getMemberSourceMap();
	long getSeed();
	Vector2f computeCenterOfMass();
	boolean isStationInvolved();
	boolean isStationInvolvedOnPlayerSide();
	boolean isStationInvolved(List<CampaignFleetAPI> side);
	List<CampaignFleetAPI> getStationSide();
	void genCombined(boolean withStation);
	boolean join(CampaignFleetAPI fleet, BattleSide side);
	boolean wasFleetDefeated(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner);
	boolean wasFleetVictorious(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner);
}
