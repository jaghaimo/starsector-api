package com.fs.starfarer.api.impl.campaign.fleets.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.fleets.misc.MiscFleetRouteManager.MiscRouteData;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class MiscPilgrimFleetCreator extends BaseMiscFleetCreatorPlugin {

	public String SHRINE_LIST_KEY = "shrine_list_key";
	
	
	@Override
	public float getFrequency() {
		return Global.getSettings().getFloat("miscFleetPilgrimFrequency");
	}

	@Override
	public int getMaxFleetsForThisCreator() {
		return Global.getSettings().getInt("miscFleetPilgrimMaxFleets");
	}



	public List<SectorEntityToken> getShrines(MiscFleetRouteManager manager) {
		List<SectorEntityToken> shrines = (List<SectorEntityToken>) manager.getData().get(SHRINE_LIST_KEY);
		if (shrines == null) {
			shrines = Global.getSector().getEntitiesWithTag(Tags.LUDDIC_SHRINE);
			manager.getData().put(SHRINE_LIST_KEY, shrines);
		}
		return shrines;
	}
	
	
	@Override
	public MiscRouteData createRouteParams(MiscFleetRouteManager manager, Random random) {
		MarketAPI from = pickSourceMarket(manager);
		SectorEntityToken to = pickDestShrine(manager, from);
		
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
							 FleetTypes.SHRINE_PILGRIMS, data.from.getLocationInHyperspace());
		m.triggerSetFleetOfficers(OfficerNum.FC_ONLY, OfficerQuality.LOWER);
		m.triggerSetFleetSizeFraction(data.size * 0.5f * (0.5f + random.nextFloat() * 0.5f));
		m.triggerFleetSetNoFactionInName();
		m.triggerSetTraderFleet();
		m.triggerSetFleetComposition(0f, 0f, 0f, 1f, 0f);
		m.triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SOURCE_MARKET, data.from);
		m.triggerSetFleetMemoryValue("$destShrine", data.to.getId());
		m.triggerSetFleetMemoryValue(MemFlags.SHRINE_PILGRIM_FLEET, true);
		
		CampaignFleetAPI fleet = m.createFleet();
		fleet.removeScriptsOfClass(MissionFleetAutoDespawn.class);
		
		return fleet;
	}

	
	public MiscRouteData createData(MarketAPI from, SectorEntityToken to) {
		MiscRouteData data = new MiscRouteData(getId());
		data.from = from;
		data.to = to;
		
		if (from.getFaction().isHostileTo(Factions.LUDDIC_CHURCH)) {
			data.factionId = Factions.INDEPENDENT;
		} else {
			data.factionId = Factions.LUDDIC_CHURCH;
		}
		
		
		MarketAPI market = to.getMarket();
		boolean realMarket = market != null && !market.isPlanetConditionMarketOnly();
		if (realMarket && market.getFaction().isHostileTo(from.getFaction())) {
			if (market.getFaction().isHostileTo(data.factionId)) {
				data.smuggling = true;
			}
//			if (market.getFaction().isHostileTo(Factions.INDEPENDENT)) {
//				data.smuggling = true;
//			} else {
//				data.factionId = Factions.INDEPENDENT;
//			}
		}
		
		
		float sizeBasis;
		if (realMarket) {
			sizeBasis = market.getSize() + from.getSize();
		} else {
			sizeBasis = from.getSize() * 0.5f;
		}
		data.size = sizeBasis / 40f;
		
		return data;
	}
	
	public MarketAPI pickSourceMarket(MiscFleetRouteManager manager) {
		
		WeightedRandomPicker<MarketAPI> markets = new WeightedRandomPicker<MarketAPI>(manager.getRandom());
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (!market.hasSpaceport()) continue; // markets w/o spaceports don't launch fleets
			if (manager.getTimeout(getId()).contains(market.getId())) continue;
			if (market.getContainingLocation().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) continue;
			if (market.getFaction().isHostileTo(Factions.INDEPENDENT) &&
				market.getFaction().isHostileTo(Factions.LUDDIC_CHURCH)) {
				continue;
			}
			
			// use this for shrines also
			if (SharedData.getData().getMarketsWithoutTradeFleetSpawn().contains(market.getId())) continue;
			
			float distLY = Misc.getDistanceToPlayerLY(market.getPrimaryEntity());
			float mult = 1f - Math.min(0.99f, distLY / 10f);
			
			String fid = market.getFactionId();
			if (Factions.LUDDIC_CHURCH.equals(fid) ||
					Factions.LUDDIC_PATH.equals(fid) ||
					Factions.KOL.equals(fid)) {
				mult *= 10f;
			}
			
			markets.add(market, market.getSize() * mult);
			
//			if (market.getName().toLowerCase().equals("jannow")) {
//				markets.add(market, 100000f);
//			}
		}
		return markets.pick();
	}
	
	public SectorEntityToken pickDestShrine(MiscFleetRouteManager manager, MarketAPI from) {
		if (from == null) return null;
		
		WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>(manager.getRandom());
		
		for (SectorEntityToken shrine : new ArrayList<SectorEntityToken>(getShrines(manager))) {
			if (!shrine.isAlive()) continue;
			if (shrine.getContainingLocation().hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) continue;
			
			MarketAPI market = shrine.getMarket();
			boolean realMarket = market != null && !market.isPlanetConditionMarketOnly();
			if (realMarket) {
				if (market.hasCondition(Conditions.DECIVILIZED)) continue;
				if (market.getFaction().isHostileTo(Factions.INDEPENDENT) &&
						market.getFaction().isHostileTo(Factions.LUDDIC_CHURCH)) {
						continue;
					}
			}
			
			float mult = 1f;
			if (realMarket) {
				mult = 10f * market.getSize();
			} else {
				//mult *= 100f;
			}
			picker.add(shrine, mult);
		}
		
		return picker.pick();
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
			
			DelayedFleetEncounter e = new DelayedFleetEncounter(new Random(), "luddicPilgrims");
			e.setDelayShort();
			//e.setDelayNone();
			e.setLocationCoreOnly(true, Factions.LUDDIC_CHURCH);
			e.beginCreate();
			e.triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.DEFAULT, Factions.LUDDIC_CHURCH, FleetTypes.PATROL_MEDIUM, new Vector2f());
			e.triggerSetFleetSizeFraction(Math.min(1f, data.size * 3f));
			e.autoAdjustFleetTypeName();
			e.triggerSetPatrol();
			e.triggerSetStandardAggroInterceptFlags();
			e.triggerSetFleetGenericHailPermanent("PilgrimRevengeHail");
			e.endCreate();			
		}
	}
	
	
	public String getStartingActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return "preparing for pilgrimage to shrine on " + data.to.getName();
	}

	public String getEndingActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return "disembarking pilgrims at " + data.from.getName();
	}

	public String getTravelToDestActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return "taking pilgrims to shrine on " + data.to.getName();
	}

	public String getTravelReturnActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return "returning pilgrims to " + data.from.getName();
	}

	public String getAtDestUnloadActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return "disembarking pilgrims at shrine on " + data.to.getName();
	}

	public String getAtDestLoadActionText(CampaignFleetAPI fleet, RouteSegment segment, MiscRouteData data) {
		return "embarking pilgrims for return voyage to " + data.from.getName();
	}
	
}
