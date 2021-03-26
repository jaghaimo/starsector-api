package com.fs.starfarer.api.campaign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.CrewCompositionAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public interface FleetEncounterContextPlugin {
	
	public static enum Status {
		NORMAL,
		DISABLED,
		DESTROYED,
		REPAIRED,
		CAPTURED,
	}
	
	/**
	 * This, and any FleetEncounterContext code that uses this, is only used for battles involving the player,
	 * and not AI vs AI autoresloved ones.
	 * @author Alex Mosolov
	 *
	 * Copyright 2013 Fractal Softworks, LLC
	 */
	public static enum EngagementOutcome {
		MUTUAL_DESTRUCTION,
		BATTLE_PLAYER_WIN,
		BATTLE_PLAYER_WIN_TOTAL,
		BATTLE_ENEMY_WIN,
		BATTLE_ENEMY_WIN_TOTAL,
		ESCAPE_PLAYER_LOSS_TOTAL,
		ESCAPE_PLAYER_SUCCESS,
		ESCAPE_PLAYER_WIN,
		ESCAPE_PLAYER_WIN_TOTAL,
		ESCAPE_ENEMY_LOSS_TOTAL,
		ESCAPE_ENEMY_SUCCESS,
		ESCAPE_ENEMY_WIN,
		ESCAPE_ENEMY_WIN_TOTAL,
		
		BATTLE_PLAYER_OUT_FIRST_WIN,
		BATTLE_PLAYER_OUT_FIRST_LOSS,
		ESCAPE_PLAYER_OUT_FIRST_WIN,
		ESCAPE_PLAYER_OUT_FIRST_LOSS,
		PURSUIT_PLAYER_OUT_FIRST_WIN,
		PURSUIT_PLAYER_OUT_FIRST_LOSS,
	}

	public static class FleetMemberData {
		private Status status;
		private FleetMemberAPI member;
		public FleetMemberData(Status status, FleetMemberAPI member) {
			this.status = status;
			this.member = member;
		}
		public Status getStatus() {
			return status;
		}
		public FleetMemberAPI getMember() {
			return member;
		}
		public void setStatus(Status status) {
			this.status = status;
		}
		public void setMember(FleetMemberAPI member) {
			this.member = member;
		}
	}
	
	public static class DataForEncounterSide {
		public static class OfficerEngagementData {
			public CampaignFleetAPI sourceFleet;
			public PersonAPI person;
			public float timeDeployed;
			public OfficerEngagementData(CampaignFleetAPI sourceFleet) {
				this.sourceFleet = sourceFleet;
			}
		}
		private float maxTimeDeployed;
		private Map<PersonAPI, OfficerEngagementData> officerData = new HashMap<PersonAPI, OfficerEngagementData>();
		private Map<FleetMemberAPI, OfficerEngagementData> fleetMemberDeploymentData = new HashMap<FleetMemberAPI, OfficerEngagementData>();
		
		private CampaignFleetAPI fleet;
		
		private List<FleetMemberData> ownCasualties = new ArrayList<FleetMemberData>();
		private List<FleetMemberData> enemyCasualties = new ArrayList<FleetMemberData>();
		
		private List<FleetMemberAPI> deployedInLastEngagement = new ArrayList<FleetMemberAPI>();
		private List<FleetMemberAPI> retreatedFromLastEngagement = new ArrayList<FleetMemberAPI>();
		private List<FleetMemberAPI> inReserveDuringLastEngagement = new ArrayList<FleetMemberAPI>();
		private List<FleetMemberAPI> disabledInLastEngagement = new ArrayList<FleetMemberAPI>();
		private List<FleetMemberAPI> destroyedInLastEngagement = new ArrayList<FleetMemberAPI>();
		private Map<FleetMemberAPI, DeployedFleetMemberAPI> memberToDeployedMap = new HashMap<FleetMemberAPI, DeployedFleetMemberAPI>();
		
		private Set<FleetMemberAPI> membersWithOfficerOrPlayerAsOrigCaptain = new LinkedHashSet<FleetMemberAPI>();
		
		private CrewCompositionAPI crewLossesDuringLastEngagement = Global.getFactory().createCrewComposition();
		private CrewCompositionAPI recoverableCrewLosses = Global.getFactory().createCrewComposition();
		
		private boolean wonLastEngagement = false;
		private FleetGoal lastGoal = null;
		private boolean disengaged = false;
		private boolean didEnoughToDisengage = false;
		private boolean enemyCanCleanDisengage = false;
		//private boolean fleetCanCleanDisengage = false;
		
		public DataForEncounterSide(CampaignFleetAPI fleet) {
			this.fleet = fleet;
		}
		
		public Set<FleetMemberAPI> getMembersWithOfficerOrPlayerAsOrigCaptain() {
			return membersWithOfficerOrPlayerAsOrigCaptain;
		}

		public float getMaxTimeDeployed() {
			return maxTimeDeployed;
		}
		
		public void setMaxTimeDeployed(float maxTimeDeployed) {
			this.maxTimeDeployed = maxTimeDeployed;
		}

		public Map<PersonAPI, OfficerEngagementData> getOfficerData() {
			return officerData;
		}
		public Map<FleetMemberAPI, OfficerEngagementData> getFleetMemberDeploymentData() {
			return fleetMemberDeploymentData;
		}

		public CrewCompositionAPI getRecoverableCrewLosses() {
			return recoverableCrewLosses;
		}
		public CrewCompositionAPI getCrewLossesDuringLastEngagement() {
			return crewLossesDuringLastEngagement;
		}
		public CampaignFleetAPI getFleet() {
			return fleet;
		}
		public List<FleetMemberData> getOwnCasualties() {
			return ownCasualties;
		}
		public List<FleetMemberData> getEnemyCasualties() {
			return enemyCasualties;
		}
		public void addOwn(FleetMemberAPI member, Status status) {
			ownCasualties.add(new FleetMemberData(status, member));
		}
		public void removeOwnCasualty(FleetMemberAPI member) {
			for (FleetMemberData data : ownCasualties) {
				if (data.member == member) {
					ownCasualties.remove(data);
					break;
				}
			}
		}
		public void removeEnemyCasualty(FleetMemberAPI member) {
			for (FleetMemberData data : enemyCasualties) {
				if (data.member == member) {
					enemyCasualties.remove(data);
					break;
				}
			}
		}	
		public void changeOwn(FleetMemberAPI member, Status newStatus) {
			for (FleetMemberData data : ownCasualties) {
				if (data.member == member) {
					data.status = newStatus;
					break;
				}
			}
		}		
		public void changeEnemy(FleetMemberAPI member, Status newStatus) {
			for (FleetMemberData data : enemyCasualties) {
				if (data.member == member) {
					data.status = newStatus;
					break;
				}
			}
		}
		public void addEnemy(FleetMemberAPI member, Status status) {
			enemyCasualties.add(new FleetMemberData(status, member));
		}
		public boolean isWonLastEngagement() {
			return wonLastEngagement;
		}
		public void setWonLastEngagement(boolean wonLastEngagement) {
			this.wonLastEngagement = wonLastEngagement;
		}
		public FleetGoal getLastGoal() {
			return lastGoal;
		}
		public void setLastGoal(FleetGoal lastGoal) {
			this.lastGoal = lastGoal;
		}
		public boolean disengaged() {
			return disengaged;
		}
		public void setDisengaged(boolean disengaged) {
			this.disengaged = disengaged;
		}
		public List<FleetMemberAPI> getDeployedInLastEngagement() {
			return deployedInLastEngagement;
		}
		public List<FleetMemberAPI> getRetreatedFromLastEngagement() {
			return retreatedFromLastEngagement;
		}
		public List<FleetMemberAPI> getInReserveDuringLastEngagement() {
			return inReserveDuringLastEngagement;
		}
		public List<FleetMemberAPI> getDisabledInLastEngagement() {
			return disabledInLastEngagement;
		}
		public List<FleetMemberAPI> getDestroyedInLastEngagement() {
			return destroyedInLastEngagement;
		}
		public boolean isDidEnoughToDisengage() {
			return didEnoughToDisengage;
		}
		public void setDidEnoughToDisengage(boolean didEnoughToDisengage) {
			this.didEnoughToDisengage = didEnoughToDisengage;
		}
		/**
		 * Only matters for non-autoresolved engagements.
		 * @return
		 */
		public Map<FleetMemberAPI, DeployedFleetMemberAPI> getMemberToDeployedMap() {
			return memberToDeployedMap;
		}

		public boolean isEnemyCanCleanDisengage() {
			return enemyCanCleanDisengage;
		}

		public void setEnemyCanCleanDisengage(boolean enemyCanCleanDisengage) {
			this.enemyCanCleanDisengage = enemyCanCleanDisengage;
		}

//		public boolean isFleetCanCleanDisengage() {
//			return fleetCanCleanDisengage;
//		}
//
//		public void setFleetCanCleanDisengage(boolean fleetCanCleanDisengage) {
//			this.fleetCanCleanDisengage = fleetCanCleanDisengage;
//		}
		
		
	}
	
	public static enum PursueAvailability {
		AVAILABLE,
		TOO_SLOW,
		NO_READY_SHIPS,
		TOOK_SERIOUS_LOSSES,
		LOST_LAST_ENGAGEMENT,
	}
	
	public static enum DisengageHarryAvailability {
		AVAILABLE,
		NO_READY_SHIPS, 
		LOST_LAST_ENGAGEMENT,
	}
	
	
	DataForEncounterSide getDataFor(CampaignFleetAPI fleet);
	DataForEncounterSide getWinnerData();
	DataForEncounterSide getLoserData();
	CampaignFleetAPI getWinner();
	CampaignFleetAPI getLoser();
	
	boolean isEngagedInHostilities();
	
	EngagementOutcome getLastEngagementOutcome();
	
	PursueAvailability getPursuitAvailability(CampaignFleetAPI fleet, CampaignFleetAPI otherFleet);
	DisengageHarryAvailability getDisengageHarryAvailability(CampaignFleetAPI fleet, CampaignFleetAPI otherFleet);
	
	
	/**
	 * Returns average recovery per ship, in the range from 0 to 1.
	 * @param result
	 * @return
	 */
	float performPostVictoryRecovery(EngagementResultAPI result);
	BattleAPI getBattle();
	void setOtherFleetHarriedPlayer(boolean otherFleetHarriedPlayer);
	boolean isOtherFleetHarriedPlayer();
	boolean adjustPlayerReputation(InteractionDialogAPI dialog, String ffText);
	float computePlayerContribFraction();
	//boolean isLowRepImpact();
	
	//void applyPostEngagementOption(EngagementResultAPI result); 
	
}



