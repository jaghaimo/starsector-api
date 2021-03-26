package com.fs.starfarer.api.impl.campaign.missions.cb;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;

public class CBTrader extends BaseCustomBountyCreator {

	public static float PROB_IN_SYSTEM_WITH_BASE = 0.5f;
	
	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		return super.getFrequency(mission, difficulty) * CBStats.TRADER_FREQ;
	}

	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Trade Fleet";
	}
	
	@Override
	public float getBountyDays() {
		return 30f;
	}

	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		data.stage = bountyStage;
		
		//mission.requireSystem(this);
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
	
		FleetSize size = FleetSize.SMALL;
		FleetQuality quality = FleetQuality.DEFAULT;
		OfficerQuality oQuality = OfficerQuality.LOWER;
		OfficerNum oNum = OfficerNum.FEWER;
		String type = FleetTypes.TRADE;
		
		if (difficulty <= 3) {
			size = FleetSize.TINY;
			type = FleetTypes.TRADE_SMALL;
		}
		
		
		beginFleet(mission, data);
		mission.triggerCreateFleet(size, quality, Factions.INDEPENDENT, type, data.system);
		mission.triggerSetFleetOfficers(oNum, oQuality);
		mission.triggerSetFleetNoCommanderSkills();
		mission.triggerSetFleetComposition(1f, 0.2f, 0f, 0f, 0f);
		mission.triggerSetFleetProbabilityCombatFreighters(0f);
		mission.triggerPickLocationAtInSystemJumpPoint(data.system);
		mission.triggerSpawnFleetAtPickedLocation(null, null);
		
		String expensive = mission.pickOne(Commodities.LUXURY_GOODS, 
							Commodities.RARE_METALS, Commodities.VOLATILES, Commodities.LOBSTER);
		String cheap = mission.pickOne(Commodities.DOMESTIC_GOODS, Commodities.FOOD, Commodities.ORGANICS);
		
		mission.triggerAddCommodityFractionDrop(expensive, 0.25f); 
		mission.triggerAddCommodityFractionDrop(cheap, 0.25f); 
		mission.triggerFleetSetPatrolActionText("waiting for customs inspection");
		mission.triggerOrderFleetPatrol(data.system, true, target.getPrimaryEntity());
		mission.triggerFleetSetPatrolLeashRange(800f);
		
		data.fleet = createFleet(mission, data);
		if (data.fleet == null) return null;
		
		data.custom1 = target;
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.TRADER_MULT, mission);
		
		return data;
	}
	
	@Override
	public void notifyAccepted(MarketAPI createdAt, HubMissionWithBarEvent mission, CustomBountyData data) {
		
		FleetSize size = FleetSize.SMALL;
		FleetQuality quality = FleetQuality.DEFAULT;
		OfficerQuality oQuality = OfficerQuality.DEFAULT;
		OfficerNum oNum = OfficerNum.DEFAULT;
		String type = FleetTypes.PATROL_MEDIUM;
		
		int difficulty = data.difficulty;
		int num = 1;
		if (difficulty > 6) num = 2;
		
		for (int i = 0; i < num; i++) {
			if (difficulty <= 2) {
				size = FleetSize.TINY;
				type = FleetTypes.PATROL_SMALL;
			} else if (difficulty <= 3) {
				size = FleetSize.VERY_SMALL;
				type = FleetTypes.PATROL_SMALL;
			} else if (difficulty <= 4) {
				size = FleetSize.SMALL;
				type = FleetTypes.PATROL_SMALL;
			} else if (difficulty <= 5) {
				size = FleetSize.MEDIUM;
				type = FleetTypes.PATROL_MEDIUM;
			} else {
				size = FleetSize.LARGE;
				type = FleetTypes.PATROL_LARGE;
			}
			
			MarketAPI market = (MarketAPI) data.custom1;
			mission.beginWithinHyperspaceRangeTrigger(market, 1f, false, data.stage);
			mission.triggerCreateFleet(size, quality, market.getFactionId(), type, market.getPrimaryEntity());
			mission.triggerSetFleetOfficers(oNum, oQuality);
			mission.triggerSetPatrol();
			mission.triggerSpawnFleetNear(data.fleet, null, null);
			mission.triggerFleetSetPatrolActionText("guarding " + data.fleet.getName().toLowerCase());
			mission.triggerFleetSetPatrolLeashRange(100f);
			mission.triggerOrderFleetPatrol(data.system, true, data.fleet);
			mission.endTrigger();
			
			difficulty -= 4;
		}
		
	}

	
	@Override
	public int getMaxDifficulty() {
		return MAX_DIFFICULTY;
	}

	@Override
	public int getMinDifficulty() {
		return 0;
	}

}






