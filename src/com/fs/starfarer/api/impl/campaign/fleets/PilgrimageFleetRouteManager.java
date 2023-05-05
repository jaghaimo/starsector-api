package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.fleets.PilgrimageFleetAssignmentAI.PilgrimageRouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
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
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

/**
 * Replaced by MiscFleetRouteManager.
 */
@Deprecated
public class PilgrimageFleetRouteManager extends BaseRouteFleetManager implements FleetEventListener {
	
//	public static int MIN_PILGRIMS = 5;
//	public static int MAX_PILGRIMS = 5;
	
	public static final Integer ROUTE_SRC_LOAD = 1;
	public static final Integer ROUTE_TRAVEL_DST = 2;
	public static final Integer ROUTE_DST_UNLOAD = 5;
	public static final Integer ROUTE_DST_LOAD = 6;
	public static final Integer ROUTE_TRAVEL_BACK_WS = 7;
	public static final Integer ROUTE_RESUPPLY_BACK_WS = 8;
	public static final Integer ROUTE_TRAVEL_SRC = 9;
	public static final Integer ROUTE_SRC_UNLOAD = 10;
	
	public static final String SOURCE_ID = "pilgrimage";
	public static Logger log = Global.getLogger(PilgrimageFleetRouteManager.class);
	
	
	protected TimeoutTracker<String> recentlySentPilgrims = new TimeoutTracker<String>();
	protected List<SectorEntityToken> shrines = null;
	
	public PilgrimageFleetRouteManager() {
		//super(0.2f, 0.3f);
		super(1f, 14f);
	}
	
	protected Object readResolve() {
		if (recentlySentPilgrims == null) {
			recentlySentPilgrims = new TimeoutTracker<String>();
		}
		return this;
	}

	@Override
	public void advance(float amount) {
		if (shrines == null) {
			shrines = Global.getSector().getEntitiesWithTag(Tags.LUDDIC_SHRINE);
		}
		//super.advance(amount * 10f);
		super.advance(amount);
		
		float days = Global.getSector().getClock().convertToDays(amount);
		recentlySentPilgrims.advance(days);
		
//		MarketAPI from = pickSourceMarket();
//		MarketAPI to = pickDestMarket(from);
	}

	protected String getRouteSourceId() {
		return SOURCE_ID;
	}
	
	protected int getMaxFleets() {
		//if (true) return 1;
		int numMarkets = Global.getSector().getEconomy().getNumMarkets();
		int maxBasedOnMarkets = numMarkets * 1;
		return Math.min(maxBasedOnMarkets, Global.getSettings().getInt("maxPilgrimageFleets"));
	}
	
	
	protected void addRouteFleetIfPossible() {
		MarketAPI from = pickSourceMarket();
		SectorEntityToken to = pickDestShrine(from);
		
//		from = Global.getSector().getEconomy().getMarket("chalcedon");
//		to = Global.getSector().getEntityById("beholder_station");
		
		if (from != null && to != null) {
			
			PilgrimageRouteData data = createData(from, to);
			if (data == null) return;
			
			log.info("Added shrine pilgrimage fleet route from " + from.getName() + " to " + to.getName());
			
			Long seed = Misc.genRandomSeed();
			String id = getRouteSourceId();
			
			OptionalFleetData extra = new OptionalFleetData(from);
			//float tier = data.size;
			//float stability = from.getStabilityValue();
			String factionId = from.getFactionId();
//			if (!from.getFaction().isHostileTo(Factions.INDEPENDENT) && 
//					!to.getFaction().isHostileTo(Factions.INDEPENDENT)) {
//				if ((float) Math.random() * 10f > stability + tier) {
//					factionId = Factions.INDEPENDENT;
//				}
//			}
//			if (data.smuggling) {
//				factionId = Factions.INDEPENDENT;
//			}
			extra.factionId = factionId;
			
			RouteData route = RouteManager.getInstance().addRoute(id, from, seed, extra, this);
			route.setCustom(data);
			
			float orbitDays = data.size * (0.75f + (float) Math.random() * 0.5f);
			orbitDays *= 30f;
			if (orbitDays < 1f) orbitDays = 1f;
			if (orbitDays > 3f) orbitDays = 3f;
			
			route.addSegment(new RouteSegment(ROUTE_SRC_LOAD, orbitDays, from.getPrimaryEntity()));
			route.addSegment(new RouteSegment(ROUTE_TRAVEL_DST, from.getPrimaryEntity(), to));
			route.addSegment(new RouteSegment(ROUTE_DST_UNLOAD, orbitDays * 0.5f, to));
			route.addSegment(new RouteSegment(ROUTE_DST_LOAD, orbitDays * 0.5f, to));
			route.addSegment(new RouteSegment(ROUTE_TRAVEL_SRC, to, from.getPrimaryEntity()));
			route.addSegment(new RouteSegment(ROUTE_SRC_UNLOAD, orbitDays, from.getPrimaryEntity()));
			
			recentlySentPilgrims.add(from.getId(), Global.getSettings().getFloat("minPilgrimSpawnIntervalPerMarket"));
		}
	}
	
	
	
	public MarketAPI pickSourceMarket() {
		//return Global.getSector().getEconomy().getMarket("jangala");
		//return Global.getSector().getEconomy().getMarket("sindria");
		//if (true) return Global.getSector().getEconomy().getMarket("chicomoztoc");
		
		WeightedRandomPicker<MarketAPI> markets = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			if (!market.hasSpaceport()) continue; // markets w/o spaceports don't launch fleets
			if (recentlySentPilgrims.contains(market.getId())) continue;
			
			
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
	
	public SectorEntityToken pickDestShrine(MarketAPI from) {
		if (from == null) return null;
		
		WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<SectorEntityToken>();
		
		for (SectorEntityToken shrine : new ArrayList<SectorEntityToken>(shrines)) {
			if (!shrine.isAlive()) continue;
			
			MarketAPI market = shrine.getMarket();
			boolean realMarket = market != null && !market.isPlanetConditionMarketOnly();
			if (realMarket && market.hasCondition(Conditions.DECIVILIZED)) continue;
			
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
	
	
	public static PilgrimageRouteData createData(MarketAPI from, SectorEntityToken to) {
		PilgrimageRouteData data = new PilgrimageRouteData();
		data.from = from;
		data.to = to;
		data.factionId = Factions.LUDDIC_CHURCH;
		
		MarketAPI market = to.getMarket();
		boolean realMarket = market != null && !market.isPlanetConditionMarketOnly();
		if (realMarket && market.getFaction().isHostileTo(from.getFaction())) {
			if (market.getFaction().isHostileTo(Factions.INDEPENDENT)) {
				data.smuggling = true;
			} else {
				data.factionId = Factions.INDEPENDENT;
			}
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
	
	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}
	
	
	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = new Random();
		if (route.getSeed() != null) {
			random = new Random(route.getSeed());
		}
		
		CampaignFleetAPI fleet = createPilgrimRouteFleet(route, random);
		if (fleet == null) return null;;
		
		//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_TRADE_FLEET, true);
		
		fleet.addEventListener(this);
		
		fleet.addScript(new PilgrimageFleetAssignmentAI(fleet, route));
		return fleet;
	}
	
	public static CampaignFleetAPI createPilgrimRouteFleet(RouteData route, Random random) {
		PilgrimageRouteData data = (PilgrimageRouteData) route.getCustom();

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

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
		if (route == null || !(route.getCustom() instanceof PilgrimageRouteData)) return;
		
		if (route.isExpired()) return;
		if (!battle.isPlayerInvolved()) return;

		// player was involved, on the opposite side of the pilgrim fleet
		if (battle.getNonPlayerSideSnapshot().contains(fleet)) {
			PilgrimageRouteData data = (PilgrimageRouteData) route.getCustom();
			
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

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		
	}

	public boolean shouldRepeat(RouteData route) {
		return false;
	}
	
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
		
	}
	
}







