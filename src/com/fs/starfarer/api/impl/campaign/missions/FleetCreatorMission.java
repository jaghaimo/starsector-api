package com.fs.starfarer.api.impl.campaign.missions;

import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.util.Misc;

public class FleetCreatorMission extends HubMissionWithTriggers {
	
	public static enum FleetStyle {
		QUANTITY,
		STANDARD,
		QUALITY,
	}
	
	protected static Object STAGE = new Object();
	
	protected String fleetTypeSmall = FleetTypes.PATROL_SMALL;
	protected String fleetTypeMedium = FleetTypes.PATROL_MEDIUM;
	protected String fleetTypeLarge = FleetTypes.PATROL_LARGE;
	

	public FleetCreatorMission(Random random) {
		super();
		setMissionId("fcm_" + Misc.genUID());
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
		
		CampaignFleetAPI fleet = fleets.get(0);
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		return fleet;
	}

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		return false;
	}
	
	
	public void createFleet(FleetStyle style, int difficulty, String factionId, Vector2f locInHyper) {
		if (style == FleetStyle.STANDARD || style == null) {
			createStandardFleet(difficulty, factionId, locInHyper);
		} else if (style == FleetStyle.QUANTITY) {
			createQuantityFleet(difficulty, factionId, locInHyper);
		} else if (style == FleetStyle.QUALITY) {
			createQualityFleet(difficulty, factionId, locInHyper);
		}
	}
	
	
	public void createStandardFleet(int difficulty, String factionId, Vector2f locInHyper) {
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.DEFAULT;
		String type = fleetTypeMedium;
		OfficerQuality oQuality = OfficerQuality.DEFAULT;
		OfficerNum oNum = OfficerNum.DEFAULT;
		
		//Global.getSector().getEconomy().getMarket("b07a");
		
		if (difficulty <= 0) {
			size = FleetSize.TINY;
			quality = FleetQuality.VERY_LOW;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FC_ONLY;
			type = fleetTypeSmall;
		} else if (difficulty == 1) {
			size = FleetSize.VERY_SMALL;
			quality = FleetQuality.VERY_LOW;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FC_ONLY;
			type = fleetTypeSmall;
		} else if (difficulty == 2) {
			size = FleetSize.SMALL;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FEWER;
			type = fleetTypeSmall;
		} else if (difficulty == 3) {
			size = FleetSize.SMALL;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeMedium;
		} else if (difficulty == 4) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeMedium;
		} else if (difficulty == 5) {
			size = FleetSize.LARGE;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeLarge;
		} else if (difficulty == 6) {
			size = FleetSize.LARGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = fleetTypeLarge;
		} else if (difficulty == 7) {
			size = FleetSize.LARGER;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = fleetTypeLarge;
		} else if (difficulty == 8) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = fleetTypeLarge;
		} else if (difficulty == 9) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = fleetTypeLarge;
		} else {// if (difficulty == 10) {
			size = FleetSize.HUGE;
			quality = FleetQuality.HIGHER;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			//oNum = OfficerNum.ALL_SHIPS;
			type = fleetTypeLarge;
		}
		
		triggerCreateFleet(size, quality, factionId, type, locInHyper);
		triggerSetFleetOfficers(oNum, oQuality);
		//triggerAutoAdjustFleetSize(size, size.next());
	}
	
	
	public void createQualityFleet(int difficulty, String factionId, Vector2f locInHyper) {
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.DEFAULT;
		String type = fleetTypeMedium;
		OfficerQuality oQuality = OfficerQuality.DEFAULT;
		OfficerNum oNum = OfficerNum.DEFAULT;
		
		if (difficulty <= 0) {
			size = FleetSize.TINY;
			quality = FleetQuality.VERY_HIGH;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.FC_ONLY;
			type = fleetTypeSmall;
		} else if (difficulty == 1) {
			size = FleetSize.VERY_SMALL;
			quality = FleetQuality.VERY_HIGH;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.FC_ONLY;
			type = fleetTypeSmall;
		} else if (difficulty == 2) {
			size = FleetSize.VERY_SMALL;
			quality = FleetQuality.VERY_HIGH;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeSmall;
		} else if (difficulty == 3) {
			size = FleetSize.SMALL;
			quality = FleetQuality.VERY_HIGH;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeSmall;
		} else if (difficulty == 4) {
			size = FleetSize.SMALL;
			quality = FleetQuality.VERY_HIGH;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = fleetTypeSmall;
		} else if (difficulty == 5) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.SMOD_1;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeMedium;
		} else if (difficulty == 6) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.SMOD_1;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = fleetTypeMedium;
		} else if (difficulty == 7) {
			size = FleetSize.LARGE;
			quality = FleetQuality.SMOD_1;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeMedium;
		} else if (difficulty == 8) {
			size = FleetSize.LARGE;
			quality = FleetQuality.SMOD_1;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = fleetTypeLarge;
		} else if (difficulty == 9) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.SMOD_1;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = fleetTypeLarge;
		} else {// if (difficulty == 10) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.SMOD_2;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.MORE;
			type = fleetTypeLarge;
		}
		
		triggerCreateFleet(size, quality, factionId, type, locInHyper);
		triggerSetFleetOfficers(oNum, oQuality);
		
		// don't do this - quality is always 0 anyway, and it can mess up fleet type names
		//triggerAutoAdjustFleetSize(size, size.next());
	}
	
	
	public void createQuantityFleet(int difficulty, String factionId, Vector2f locInHyper) {
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.DEFAULT;
		String type = fleetTypeMedium;
		OfficerQuality oQuality = OfficerQuality.DEFAULT;
		OfficerNum oNum = OfficerNum.DEFAULT;
		
		if (difficulty <= 0) {
			size = FleetSize.SMALL;
			quality = FleetQuality.LOWER;
			oQuality = OfficerQuality.LOWER;
			oNum = OfficerNum.FC_ONLY;
			type = fleetTypeSmall;
		} else if (difficulty == 1) {
			size = FleetSize.SMALL;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeSmall;
		} else if (difficulty == 2) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.LOWER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeMedium;
		} else if (difficulty == 3) {
			size = FleetSize.MEDIUM;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeMedium;
		} else if (difficulty == 4) {
			size = FleetSize.LARGE;
			quality = FleetQuality.LOWER;
			oQuality = OfficerQuality.HIGHER;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeMedium;
		} else if (difficulty == 5) {
			size = FleetSize.LARGE;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeMedium;
		} else if (difficulty == 6) {
			size = FleetSize.LARGER;
			quality = FleetQuality.LOWER;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeLarge;
		} else if (difficulty == 7) {
			size = FleetSize.LARGER;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeLarge;
		} else if (difficulty == 8) {
			size = FleetSize.VERY_LARGE;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeLarge;
		} else if (difficulty == 9) {
			size = FleetSize.HUGE;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.DEFAULT;
			type = fleetTypeLarge;
		} else {// if (difficulty == 10) {
			size = FleetSize.MAXIMUM;
			quality = FleetQuality.DEFAULT;
			oQuality = OfficerQuality.DEFAULT;
			oNum = OfficerNum.MORE;
			type = fleetTypeLarge;
		}
		
		triggerCreateFleet(size, quality, factionId, type, locInHyper);
		triggerSetFleetOfficers(oNum, oQuality);
		//triggerAutoAdjustFleetSize(size, size.next());
	}
	
	
	public void setFleetTypes(String small, String medium, String large) {
		fleetTypeSmall = small;
		fleetTypeMedium = medium;
		fleetTypeLarge = large;
	}
	
	public String getFleetTypeSmall() {
		return fleetTypeSmall;
	}

	public void setFleetTypeSmall(String fleetTypeSmall) {
		this.fleetTypeSmall = fleetTypeSmall;
	}

	public String getFleetTypeMedium() {
		return fleetTypeMedium;
	}

	public void setFleetTypeMedium(String fleetTypeMedium) {
		this.fleetTypeMedium = fleetTypeMedium;
	}

	public String getFleetTypeLarge() {
		return fleetTypeLarge;
	}

	public void setFleetTypeLarge(String fleetTypeLarge) {
		this.fleetTypeLarge = fleetTypeLarge;
	}
}




