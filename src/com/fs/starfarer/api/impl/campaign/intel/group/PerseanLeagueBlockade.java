package com.fs.starfarer.api.impl.campaign.intel.group;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.api.impl.campaign.NPCHassler;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.PerseanLeagueHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.group.FGBlockadeAction.FGBlockadeParams;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;



public class PerseanLeagueBlockade extends BlockadeFGI {

	public static float NUM_OTHER_FLEETS_MULT = 0.25f;
	
	public static final String ARMADA = "$PLB_armada";
	public static final String SUPPLY = "$PLB_supply";
	public static final String GENERIC = "$PLB_generic";
	
	//public static final String ABORTED_OR_ENDING = "$PLB_abortedOrEnding";
	public static final String BLOCKADING = "$PLB_isBlockading";
	
	public static String KEY = "$plBlockade_ref";
	public static PerseanLeagueBlockade get() {
		return (PerseanLeagueBlockade) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	
	public PerseanLeagueBlockade(GenericRaidParams params, FGBlockadeParams blockadeParams) {
		super(params, blockadeParams);
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
		
		PersonAPI reynard = People.getPerson(People.REYNARD_HANNAN);
		if (reynard != null) {
			Misc.makeImportant(reynard, "PLB");
		}
	}
	
	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
		
		PersonAPI reynard = People.getPerson(People.REYNARD_HANNAN);
		if (reynard != null) {
			Misc.makeUnimportant(reynard, "PLB");
		}
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}





	protected boolean createdArmada = false;
	protected int supplyFleets = 0;
	
	@Override
	protected CampaignFleetAPI createFleet(int size, float damage) {
		
		Random r = getRandom();
		
		Vector2f loc = origin.getLocationInHyperspace();
		
		FleetCreatorMission m = new FleetCreatorMission(r);
		m.beginFleet();
		
		boolean armada = size == 10 && !createdArmada; 
		boolean supplyFleet = size == 1 && supplyFleets < 2;
		
		if (armada) {
			createdArmada = true;
			
			m.triggerCreateFleet(FleetSize.MAXIMUM, FleetQuality.SMOD_2, params.factionId, FleetTypes.LEAGUE_ARMADA, loc);
			m.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
			m.triggerSetFleetFlag(ARMADA);
			
			m.triggerSetFleetType(FleetTypes.LEAGUE_ARMADA);
			m.triggerSetFleetDoctrineQuality(5, 5, 5);
			m.triggerSetFleetDoctrineOther(5, 0);
			m.triggerSetFleetComposition(0f, 0f, 0f, 0f, 0f);
			m.triggerFleetMakeFaster(true, 1, false);
			
			m.triggerFleetAddCommanderSkill(Skills.CREW_TRAINING, 1);
			m.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
			m.triggerFleetAddCommanderSkill(Skills.TACTICAL_DRILLS, 1);
			m.triggerFleetAddCommanderSkill(Skills.CARRIER_GROUP, 1);
		} else if (supplyFleet) {
			supplyFleets++;
			
			int total = 0;
			for (Integer i : params.fleetSizes) total += i;
			
			FleetSize supplyFleetSize = FleetSize.MEDIUM;
			if (total < 50) {
				supplyFleetSize = FleetSize.SMALL;
			} else if (total >= 80) {
				supplyFleetSize = FleetSize.LARGE;
			}
			
			m.triggerCreateFleet(supplyFleetSize, FleetQuality.DEFAULT, params.factionId, FleetTypes.SUPPLY_FLEET, loc);
			m.triggerSetFleetOfficers(OfficerNum.DEFAULT, OfficerQuality.DEFAULT);
			m.triggerSetFleetFlag(SUPPLY);
		
			m.triggerSetFleetType(FleetTypes.SUPPLY_FLEET);
			m.triggerFleetMakeFaster(true, 0, false);
			
			m.triggerSetFleetComposition(0.5f, 0.5f, 0.1f, 0f, 0.1f);
		} else {
			m.createFleet(params.style, size, params.factionId, loc);
			m.triggerSetFleetFlag(GENERIC);
		}
		
		m.setFleetSource(params.source);
		setFleetCreatorQualityFromRoute(m);
		m.setFleetDamageTaken(damage);
	
		m.triggerSetPatrol();
		m.triggerMakeLowRepImpact();
		m.triggerMakeAlwaysSpreadTOffHostility();
	
	
		CampaignFleetAPI fleet = m.createFleet();
		
		if (fleet != null && !armada && !supplyFleet) {
			fleet.addScript(new NPCHassler(fleet, getTargetSystem()));
		}
		
		if (fleet != null && armada) {
			fleet.getCommander().setRankId(Ranks.SPACE_ADMIRAL);
			setNeverStraggler(fleet);
		}
		
		return fleet;
	}

//	@Override
//	public void abort() {
//		if (!isAborted()) {
//			for (CampaignFleetAPI curr : getFleets()) {
//				curr.getMemoryWithoutUpdate().set(ABORTED_OR_ENDING, true);
//			}
//		}
//		super.abort();
//	}
	
	

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		if (isSpawnedFleets()) {
			if (isEnded() || isEnding() || isAborted() || isCurrent(RETURN_ACTION)) {
				for (CampaignFleetAPI curr : getFleets()) {
					//curr.getMemoryWithoutUpdate().set(ABORTED_OR_ENDING, true);
					curr.getMemoryWithoutUpdate().set(BLOCKADING, false);
				}
				return;
			}
			
			if (isCurrent(PAYLOAD_ACTION)) {
				for (CampaignFleetAPI curr : getFleets()) {
					curr.getMemoryWithoutUpdate().set(BLOCKADING, true);
//					curr.getMemoryWithoutUpdate().set(ARMADA, true);
// 					curr.getMemoryWithoutUpdate().set(SUPPLY, true);
				}
			}
		}
	}

	@Override
	protected void periodicUpdate() {
		super.periodicUpdate();
		
		if (HostileActivityEventIntel.get() == null) { // ???
			abort();
			return;
		}
		
		FGAction action = getCurrentAction();
		if (action instanceof FGBlockadeAction) {
			MutableStatWithTempMods stat = HostileActivityEventIntel.get().getNumFleetsStat(getTargetSystem());
			stat.addTemporaryModMult(1f, "PLBlockade", null, NUM_OTHER_FLEETS_MULT);
		}
		
		if (!isSpawnedFleets() || isSpawning()) return;
		
		int armada = 0;
		int supply = 0;
		for (CampaignFleetAPI curr : getFleets()) {
			if (curr.getMemoryWithoutUpdate().getBoolean(ARMADA)) {
				armada++;
			}
			if (curr.getMemoryWithoutUpdate().getBoolean(SUPPLY)) {
				supply++;
			}
		}
		
		if (armada <= 0 || supply <= 0) {
			abort();
			return;
		}
		
		if (action instanceof FGBlockadeAction) {
			FGBlockadeAction blockade = (FGBlockadeAction) action;
			if (blockade.getPrimary() != null) {
				int supplyIndex = 0;
				for (CampaignFleetAPI curr : getFleets()) {
					if (blockade.getPrimary().getContainingLocation() != curr.getContainingLocation()) {
						continue;
					}
					if (curr.getMemoryWithoutUpdate().getBoolean(SUPPLY)) {
						Misc.setFlagWithReason(curr.getMemoryWithoutUpdate(), MemFlags.FLEET_BUSY, curr.getId(), true, -1f);
	
						curr.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true, 0.4f);
						curr.clearAssignments();
						
						SectorEntityToken resupplyLoc = blockade.getPrimary();
						if (supplyIndex == 1) {
							for (SectorEntityToken jp : blockade.getBlockadePoints()) {
								if (jp != resupplyLoc) {
									resupplyLoc = jp;
									break;
								}
							}
						}
						curr.addAssignment(FleetAssignment.ORBIT_PASSIVE, resupplyLoc, 3f,
									"standing by to provide resupply");
						supplyIndex++;
					} else if (curr.getMemoryWithoutUpdate().getBoolean(ARMADA)) {
						
					} else {
						curr.getMemoryWithoutUpdate().set(MemFlags.WILL_HASSLE_PLAYER, true, 2f);
						curr.getMemoryWithoutUpdate().set(MemFlags.HASSLE_TYPE, PerseanLeagueHostileActivityFactor.HASSLE_REASON, 2f);
					}
				}
			}
		}
	}


	
	@Override
	protected void addPostAssessmentSection(TooltipMakerAPI info, float width, float height, float opad) {
		
		info.addPara("The blockading forces are led by a Grand Armada and "
				+ "supported by a pair of supply fleets.", opad);
//		bullet(info);
//		info.addPara("Forcing the Grand Armada to withdraw will defeat the blockade", opad);
//		info.addPara("Forcing both supply fleets to withdraw will defeat the blockade", 0f);
//		unindent(info);
	}
	
	
	

}




