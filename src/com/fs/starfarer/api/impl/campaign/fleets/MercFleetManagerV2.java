package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.MercType;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.SystemBountyManager;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class MercFleetManagerV2 extends BaseRouteFleetManager {

	public static final Integer ROUTE_PREPARE = 1;
	public static final Integer ROUTE_TRAVEL = 2;
	public static final Integer ROUTE_PATROL = 3;
	public static final Integer ROUTE_RETURN = 4;
	public static final Integer ROUTE_STAND_DOWN = 5;
	
	public MercFleetManagerV2() {
		super(0.5f, 1.5f);
	}
	
	@Override
	protected void addRouteFleetIfPossible() {
		MarketAPI from = pickMarket();
		if (from == null) return;
		
		MarketAPI to = pickNearbyMarketToDefend(from);
		if (to == null) return;
		
		Long seed = new Random().nextLong();
		String id = getRouteSourceId();
		
		OptionalFleetData extra = new OptionalFleetData(from);
		
		RouteData route = RouteManager.getInstance().addRoute(id, from, seed, extra, this);
		
		float orbitDays = 2f + (float) Math.random() * 3f;
		float deorbitDays = 2f + (float) Math.random() * 3f;
		float patrolDays = 2f + (float) Math.random() * 3f;
		
		SectorEntityToken target = to.getPrimaryEntity();
		if ((float) Math.random() > 0.25f && to.getStarSystem() != null) {
			if ((float) Math.random() > 0.25f) {
				target = to.getStarSystem().getCenter();
			} else {
				target = to.getStarSystem().getHyperspaceAnchor();
			}
		}
		
		if (from.getContainingLocation() == to.getContainingLocation() && !from.getContainingLocation().isHyperspace()) {
			route.addSegment(new RouteSegment(ROUTE_PREPARE, orbitDays, from.getPrimaryEntity()));
			route.addSegment(new RouteSegment(ROUTE_PATROL, patrolDays, target));
			route.addSegment(new RouteSegment(ROUTE_STAND_DOWN, deorbitDays, from.getPrimaryEntity()));
		} else {
			route.addSegment(new RouteSegment(ROUTE_PREPARE, orbitDays, from.getPrimaryEntity()));
			route.addSegment(new RouteSegment(ROUTE_TRAVEL, from.getPrimaryEntity(), to.getPrimaryEntity()));
			route.addSegment(new RouteSegment(ROUTE_PATROL, patrolDays, target));
			route.addSegment(new RouteSegment(ROUTE_RETURN, to.getPrimaryEntity(), from.getPrimaryEntity()));
			route.addSegment(new RouteSegment(ROUTE_STAND_DOWN, deorbitDays, from.getPrimaryEntity()));
		}
	}
	
	public MercType pickFleetType(Random random) {
		WeightedRandomPicker<MercType> picker = new WeightedRandomPicker<MercType>(random);
		picker.add(MercType.SCOUT, 10f); 
		picker.add(MercType.BOUNTY_HUNTER, 10f); 
		picker.add(MercType.PRIVATEER, 10f); 
		picker.add(MercType.PATROL, 10f); 
		picker.add(MercType.ARMADA, 3f); 
		MercType type = picker.pick();
		return type;
	}
	
	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = route.getRandom();
		
		MercType type = pickFleetType(random);
		
		float combat = 0f;
		float tanker = 0f;
		float freighter = 0f;
		String fleetType = type.fleetType;
		switch (type) {
		case SCOUT:
			combat = Math.round(1f + random.nextFloat() * 2f);
			break;
		case PRIVATEER:
		case BOUNTY_HUNTER:
			combat = Math.round(3f + random.nextFloat() * 2f);
			break;
		case PATROL:
			combat = Math.round(9f + random.nextFloat() * 3f);
			tanker = Math.round(random.nextFloat()) * 5f;
			break;
		case ARMADA:
			combat = Math.round(10f + random.nextFloat() * 4f);
			tanker = Math.round(random.nextFloat()) * 10f;
			freighter = Math.round(random.nextFloat()) * 10f;
			break;
		}

		combat *= 5f;
		
		FleetParamsV3 params = new FleetParamsV3(
				route.getMarket(), 
				null,
				Factions.INDEPENDENT,
				route.getQualityOverride() + 0.2f,
				fleetType,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				0f, // transportPts
				0f, // linerPts
				0f, // utilityPts
				0f // qualityMod
				);
		params.timestamp = route.getTimestamp();
		params.random = random;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		route.getMarket().getContainingLocation().addEntity(fleet);
		fleet.setFacing((float) Math.random() * 360f);
		// this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
		fleet.setLocation(route.getMarket().getPrimaryEntity().getLocation().x, route.getMarket().getPrimaryEntity().getLocation().x);
		
		MercAssignmentAIV2 ai = new MercAssignmentAIV2(fleet, route);
		fleet.addScript(ai);
		
		
		return fleet;
	}

	
	protected MarketAPI pickMarket() {
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (market.getFactionId().equals(Factions.PIRATES)) continue;
			if (market.getFaction().isHostileTo(Factions.INDEPENDENT)) continue;
			
			if (!market.hasSpaceport()) continue;
			
			if (market.getStarSystem() != null && market.getStarSystem().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) {
				continue;
			}
			
			if (market.getDaysInExistence() < Global.getSettings().getFloat("minMarketExistenceDaysForMercs")) {
				continue;
			}
			if (market.getSize() < Global.getSettings().getFloat("minMarketSizeForMercs")) {
				continue;
			}
			
			
			float mult = Misc.getSpawnChanceMult(market.getLocationInHyperspace());
			float w = market.getStabilityValue() + market.getSize();
			
			w *= mult;
			picker.add(market, w);
		}
		return picker.pick();
	}

	
	protected MarketAPI pickNearbyMarketToDefend(MarketAPI source) {
		WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>();
		
		//CampaignEventManagerAPI eventManager = Global.getSector().getEventManager();
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			//if (market.getFactionId().equals(Factions.PIRATES)) continue;
			if (market.getFaction().isHostileTo(Factions.INDEPENDENT)) continue;
			if (market.getStarSystem() == null) continue;
			if (market.isHidden()) continue;
			
			if (market.getStarSystem() != null && market.getStarSystem().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) {
				continue;
			}
			
			float dist = Misc.getDistance(market.getLocationInHyperspace(), source.getLocationInHyperspace());
			if (dist < 1000) dist = 1000;
			float weight = 10000f / dist;
			
			if (SystemBountyManager.getInstance().isActive(market)) {
				weight *= 5f;
			}
			
			picker.add(market, weight);
		}
		MarketAPI market = picker.pick();
//		if (market == null) return null;
//		return market.getStarSystem();
		return market;
	}
	
	
	
	@Override
	protected int getMaxFleets() {
		return (int) Global.getSettings().getFloat("maxMercFleets");
	}

	@Override
	protected String getRouteSourceId() {
		return "mercs_global";
	}

	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
		
	}

	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

	public boolean shouldRepeat(RouteData route) {
		return false;
	}


	
}















