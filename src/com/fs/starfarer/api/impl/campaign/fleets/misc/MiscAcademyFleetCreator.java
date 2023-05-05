package com.fs.starfarer.api.impl.campaign.fleets.misc;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.fleets.misc.MiscFleetRouteManager.MiscRouteData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class MiscAcademyFleetCreator extends BaseMiscFleetCreatorPlugin {

	public static String ACADEMY_FLEET_TYPE = "$academyFleetType";
	
	@Override
	public float getFrequency() {
		return Global.getSettings().getFloat("miscFleetAcademyFrequency");
	}

	@Override
	public int getMaxFleetsForThisCreator() {
		return Global.getSettings().getInt("miscFleetAcademyMaxFleets");
	}

	public static SectorEntityToken getAcademy() {
		StarSystemAPI system = Global.getSector().getStarSystem("Galatia");
		return system.getEntityById("station_galatia_academy");
	}

	
	@Override
	public MiscRouteData createRouteParams(MiscFleetRouteManager manager, Random random) {
		MarketAPI from = pickSourceMarket(manager);
		SectorEntityToken to = getAcademy();
		
		if (to == null || to.getContainingLocation().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) return null;
		
		
//		from = Global.getSector().getEconomy().getMarket("chalcedon");
//		to = Global.getSector().getEconomy().getMarket("eochu_bres").getPrimaryEntity();
//		to = Global.getSector().getEntityById("beholder_station");
		
		MiscRouteData result = createData(from, to);
		
		return result;
	}
	

	@Override
	public CampaignFleetAPI createFleet(MiscFleetRouteManager manager, RouteData route, Random random) {
		MiscRouteData data = (MiscRouteData) route.getCustom();

		FleetCreatorMission m = new FleetCreatorMission(random);
		m.beginFleet();
		
		
		m.triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.LOWER, data.factionId, 
							 FleetTypes.ACADEMY_FLEET, data.from.getLocationInHyperspace());
		m.triggerSetFleetOfficers(OfficerNum.FC_ONLY, OfficerQuality.LOWER);
		m.triggerSetFleetSizeFraction(data.size * 0.5f * (0.5f + random.nextFloat() * 0.5f));
		m.triggerFleetSetNoFactionInName();
		m.triggerSetTraderFleet();
		m.triggerSetFleetComposition(0f, 0f, 0f, 1f, 0f);
		m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, data.from);
		//m.triggerSetFleetMemoryValue("$destShrine", data.to.getId());
		m.triggerSetFleetMemoryValue(MemFlags.ACADEMY_FLEET, true);

		WeightedRandomPicker<String> types = new WeightedRandomPicker<String>(random);
		types.add("supplies");
		types.add("students");
		types.add("visitors");
		m.triggerSetFleetMemoryValue(ACADEMY_FLEET_TYPE, types.pick());
		
		
		CampaignFleetAPI fleet = m.createFleet();
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		
		return fleet;
	}

	
	public MiscRouteData createData(MarketAPI from, SectorEntityToken to) {
		MiscRouteData data = new MiscRouteData(getId());
		data.from = from;
		data.to = to;
		
		if (from.getFaction().isHostileTo(Factions.INDEPENDENT)) {
			data.factionId = from.getFactionId();
		} else {
			data.factionId = Factions.INDEPENDENT;
		}
		
		// in case the source colony is hostile to the Hegemony (which has Ancyra in the system)
		data.smuggling = true;
		
		float sizeBasis = from.getSize() / 8f;
		sizeBasis *= 0.25f;
		if (sizeBasis < 0.05f) sizeBasis = 0.05f;
		if (sizeBasis > 0.25f) sizeBasis = 0.25f;
		data.size = sizeBasis;
		
		return data;
	}
	
	public MarketAPI pickSourceMarket(MiscFleetRouteManager manager) {
		WeightedRandomPicker<MarketAPI> markets = new WeightedRandomPicker<MarketAPI>(manager.getRandom());
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (!market.hasSpaceport()) continue; // markets w/o spaceports don't launch fleets
			if (market.getContainingLocation().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) continue;
			
			if (manager.getTimeout(getId()).contains(market.getId())) continue;
			
			// use this for academy fleets also
			if (SharedData.getData().getMarketsWithoutTradeFleetSpawn().contains(market.getId())) continue;
			
			float distLY = Misc.getDistanceToPlayerLY(market.getPrimaryEntity());
			float mult = 1f - Math.min(0.99f, distLY / 10f);
			
			if (market.getFaction().isHostileTo(Factions.INDEPENDENT)) {
				mult *= 0.2f;
			}
			
			markets.add(market, market.getSize() * mult);
		}
		return markets.pick();
	}
	
	@Override
	public void reportBattleOccurred(MiscFleetRouteManager manager, CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		RouteData route = RouteManager.getInstance().getRoute(manager.getRouteSourceId(), fleet);
		if (route == null || !(route.getCustom() instanceof MiscRouteData)) return;
		
		if (route.isExpired()) return;
		if (!battle.isPlayerInvolved()) return;

		// player was involved, no the opposite side of the pilgrim fleet
		if (battle.getNonPlayerSideSnapshot().contains(fleet)) {
			MiscRouteData data = (MiscRouteData) route.getCustom();
			
			// possible TODO: some kind of response, depending?
		}
	}
	
	
	public String getStartingActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		String type = fleet.getMemoryWithoutUpdate().getString(ACADEMY_FLEET_TYPE);
		return "preparing for voyage to Galatia Academy";
	}

	public String getEndingActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		//return "orbiting " + data.from.getName();
		return "returned from voyage to Galatia Academy";
	}

	public String getTravelToDestActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return "traveling to Galatia Academy";
	}

	public String getTravelReturnActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return "traveling to " + data.from.getName();
	}

	public String getAtDestUnloadActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return "orbiting Galatia Academy";
	}

	public String getAtDestLoadActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return "orbiting Galatia Academy";
	}
	
}
