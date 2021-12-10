package com.fs.starfarer.api.impl.campaign.missions;

import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;

public class FleetCreatorMission extends HubMissionWithTriggers {
	
	protected static Object STAGE = new Object();
	
	public FleetCreatorMission(Random random) {
		super();
		setGenRandom(random);
	}
	
	public void beginFleet() {
		beginStageTrigger(STAGE);
		triggerMakeAllFleetFlagsPermanent();
	}
	
	public CampaignFleetAPI createFleet() {
		endTrigger();
		
		List<CampaignFleetAPI> fleets = runStageTriggersReturnFleets(STAGE);
		if (fleets.isEmpty()) return null;
		
		return fleets.get(0);
	}

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		return false;
	}
	
	
	public void createStandardFleet(int difficulty, String factionId, Vector2f locInHyper) {
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.DEFAULT;
		String type = FleetTypes.PATROL_MEDIUM;
		OfficerQuality oQuality = OfficerQuality.DEFAULT;
		OfficerNum oNum = OfficerNum.DEFAULT;
		
		if (difficulty <= 0) {
			size = FleetSize.TINY;
			quality = FleetQuality.VERY_LOW;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FC_ONLY;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 1) {
			size = FleetSize.VERY_SMALL;
			quality = FleetQuality.VERY_LOW;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FC_ONLY;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 2) {
			size = FleetSize.SMALL;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FEWER;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 3) {
			size = FleetSize.SMALL;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 4) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 5) {
			size = FleetSize.LARGE;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 6) {
			size = FleetSize.LARGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 7) {
			size = FleetSize.LARGER;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 8) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 9) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		} else {// if (difficulty == 10) {
			size = FleetSize.HUGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			//oNum = OfficerNum.ALL_SHIPS;
			type = FleetTypes.PATROL_LARGE;
		}
		
		triggerCreateFleet(size, quality, factionId, type, locInHyper);
		triggerSetFleetOfficers(oNum, oQuality);
		triggerAutoAdjustFleetSize(size, size.next());
	}
	
	
	public void createQualityFleet(int difficulty, String factionId, Vector2f locInHyper) {
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.DEFAULT;
		String type = FleetTypes.PATROL_MEDIUM;
		OfficerQuality oQuality = OfficerQuality.DEFAULT;
		OfficerNum oNum = OfficerNum.DEFAULT;
		
		if (difficulty <= 0) {
			size = FleetSize.TINY;
			quality = FleetQuality.VERY_HIGH;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.FC_ONLY;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 1) {
			size = FleetSize.VERY_SMALL;
			quality = FleetQuality.VERY_HIGH;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.FC_ONLY;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 2) {
			size = FleetSize.VERY_SMALL;
			quality = FleetQuality.VERY_HIGH;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 3) {
			size = FleetSize.SMALL;
			quality = FleetQuality.VERY_HIGH;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 4) {
			size = FleetSize.SMALL;
			quality = FleetQuality.VERY_HIGH;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 5) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.SMOD_1;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 6) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.SMOD_1;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 7) {
			size = FleetSize.LARGE;
			quality = FleetQuality.SMOD_1;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 8) {
			size = FleetSize.LARGE;
			quality = FleetQuality.SMOD_1;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 9) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.SMOD_1;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		} else {// if (difficulty == 10) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.SMOD_2;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		}
		
		triggerCreateFleet(size, quality, factionId, type, locInHyper);
		triggerSetFleetOfficers(oNum, oQuality);
		triggerAutoAdjustFleetSize(size, size.next());
	}
	
	
	public void createQuantityFleet(int difficulty, String factionId, Vector2f locInHyper) {
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.DEFAULT;
		String type = FleetTypes.PATROL_MEDIUM;
		OfficerQuality oQuality = OfficerQuality.DEFAULT;
		OfficerNum oNum = OfficerNum.DEFAULT;
		
		if (difficulty <= 0) {
			size = FleetSize.SMALL;
			quality = FleetQuality.LOWER;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FC_ONLY;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 1) {
			size = FleetSize.SMALL;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_SMALL;
		} else if (difficulty == 2) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.LOWER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 3) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 4) {
			size = FleetSize.LARGE;
			quality = FleetQuality.LOWER;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 5) {
			size = FleetSize.LARGE;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_MEDIUM;
		} else if (difficulty == 6) {
			size = FleetSize.LARGER;
			quality = FleetQuality.LOWER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 7) {
			size = FleetSize.LARGER;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 8) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_LARGE;
		} else if (difficulty == 9) {
			size = FleetSize.HUGE;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = FleetTypes.PATROL_LARGE;
		} else {// if (difficulty == 10) {
			size = FleetSize.MAXIMUM;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = FleetTypes.PATROL_LARGE;
		}
		
		triggerCreateFleet(size, quality, factionId, type, locInHyper);
		triggerSetFleetOfficers(oNum, oQuality);
		triggerAutoAdjustFleetSize(size, size.next());
	}
}




