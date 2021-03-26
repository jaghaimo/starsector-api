package com.fs.starfarer.api.impl.campaign.missions.cb;

import java.awt.Color;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class CBPatrol extends BaseCustomBountyCreator {

	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		return super.getFrequency(mission, difficulty) * CBStats.PATROL_FREQ;
	}
	
	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Patrol";
	}
	
	public void addTargetLocationAndDescriptionBulletPoint(TooltipMakerAPI info,
			Color tc, float pad, HubMissionWithBarEvent mission,
			CustomBountyData data) {
		if (data.system != null) {
			//info.addPara("Target is in the " + data.system.getNameWithLowercaseTypeShort() + "", tc, pad);
			info.addPara("Target is a %s patrol located in the " + 
					data.system.getNameWithLowercaseType() + ".", pad, 
					tc, data.fleet.getFaction().getBaseUIColor(), data.fleet.getFaction().getPersonNamePrefix());
		}
	}
	
	public void addTargetLocationAndDescription(TooltipMakerAPI info, float width, float height, HubMissionWithBarEvent mission, CustomBountyData data) {
		float opad = 10f;
		float pad = 3f;
		Color h = Misc.getHighlightColor();
		if (data.system != null && data.fleet != null && data.market != null) {
			info.addPara("The target is a %s patrol and is located in the " + data.system.getNameWithLowercaseType() + ".", opad, 
					data.fleet.getFaction().getBaseUIColor(), data.fleet.getFaction().getPersonNamePrefix());
			info.addPara("It will most likely be found either in orbit around " + 
					data.market.getName() + ", or patrolling one of the system's objectives "
							+ "(such as a comm relay) or jump-points.", opad);
		}
	}
	
	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		
		mission.requireMarketSizeAtLeast(4);
		mission.requireMarketNotHidden();
		mission.requireMarketHasSpaceport();
		mission.requireMarketNotInHyperspace();
		mission.requireMarketFactionCustom(ReqMode.NOT_ANY, Factions.CUSTOM_DECENTRALIZED);
		mission.requireMarketFactionNot(Factions.PIRATES); // redundant, given the above 
		mission.requireMarketFactionNotPlayer();
		mission.requireMarketLocationNot(createdAt.getContainingLocation());
		MarketAPI target = mission.pickMarket();
		
		if (target == null || target.getStarSystem() == null) return null;

		
		StarSystemAPI system = target.getStarSystem();
		data.system = system;
		data.market = target;
		
		int num = 1;
		if (difficulty > 6) num = 2;
		if (difficulty > 8) num = 3;
		float protectorDiff = difficulty - 3;
		
		FleetSize size = FleetSize.MEDIUM;
		FleetQuality quality = FleetQuality.DEFAULT;
		String type = FleetTypes.PATROL_MEDIUM;
		OfficerQuality oQuality = OfficerQuality.DEFAULT;
		OfficerNum oNum = OfficerNum.DEFAULT;
	
		for (int i = 0; i < num; i++) {
			float diff = difficulty;
			if (i > 0) diff = protectorDiff;
			
			if (diff <= 2) {
				size = FleetSize.TINY;
				type = FleetTypes.PATROL_SMALL;
			} else if (diff <= 3) {
				size = FleetSize.VERY_SMALL;
				type = FleetTypes.PATROL_SMALL;
			} else if (diff <= 4) {
				size = FleetSize.SMALL;
				type = FleetTypes.PATROL_SMALL;
			} else if (difficulty <= 5) {
				size = FleetSize.MEDIUM;
				type = FleetTypes.PATROL_MEDIUM;
			} else {
				size = FleetSize.LARGE;
				type = FleetTypes.PATROL_LARGE;
			}
			
			beginFleet(mission, data);
			mission.triggerCreateFleet(size, quality, target.getFactionId(), type, data.system);
			mission.triggerSetFleetOfficers(oNum, oQuality);
			mission.triggerAutoAdjustFleetSize(size, size.next());
			mission.triggerFleetAllowLongPursuit();
			mission.triggerFleetSetAllWeapons();
			
			mission.triggerSetPatrol();
			
			if (i == 0) {
				mission.triggerSpawnFleetNear(target.getPrimaryEntity(), null, null);
				mission.triggerFleetSetPatrolActionText("patrolling");
				mission.triggerOrderFleetPatrol(data.system, true, Tags.JUMP_POINT, Tags.OBJECTIVE);
				mission.triggerOrderExtraPatrolPoints(target.getPrimaryEntity());
			} else {
				mission.triggerSpawnFleetNear(data.fleet, null, null);
				mission.triggerFleetSetPatrolActionText("guarding " + data.fleet.getName().toLowerCase());
				mission.triggerFleetSetPatrolLeashRange(100f);
				mission.triggerOrderFleetPatrol(data.system, true, data.fleet);
			}
			
			CampaignFleetAPI fleet = createFleet(mission, data);
			if (i == 0) {
				data.fleet = fleet;
			}
		}
		
		if (data.fleet == null) return null;
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.PATROL_MULT, mission);
		
		return data;
	}
	
	@Override
	public void updateInteractionData(HubMissionWithBarEvent mission, CustomBountyData data) {
		String id = mission.getMissionId();
		String faction = data.fleet.getFaction().getPersonNamePrefix();
		Color factionColor = data.fleet.getFaction().getBaseUIColor();
		mission.set("$" + id + "_patrolFaction", faction);
		mission.set("$bcb_patrolFaction", faction);
		mission.set("$" + id + "_patrolFactionColor", factionColor);
		mission.set("$bcb_patrolFactionColor", factionColor);
	}
	

	@Override
	public int getMaxDifficulty() {
		return super.getMaxDifficulty();
	}

	@Override
	public int getMinDifficulty() {
		return 0;
	}

}






