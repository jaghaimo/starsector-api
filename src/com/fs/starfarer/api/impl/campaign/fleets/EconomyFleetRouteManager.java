package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript.LocationDanger;
import com.fs.starfarer.api.impl.campaign.econ.ShippingDisruption;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.CargoQuantityData;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.EconomyRouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.misc.TradeFleetDepartureIntel;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class EconomyFleetRouteManager extends BaseRouteFleetManager implements FleetEventListener {
	
	public static final Integer ROUTE_SRC_LOAD = 1;
	public static final Integer ROUTE_TRAVEL_DST = 2;
	public static final Integer ROUTE_TRAVEL_WS = 3;
	public static final Integer ROUTE_RESUPPLY_WS = 4;
	public static final Integer ROUTE_DST_UNLOAD = 5;
	public static final Integer ROUTE_DST_LOAD = 6;
	public static final Integer ROUTE_TRAVEL_BACK_WS = 7;
	public static final Integer ROUTE_RESUPPLY_BACK_WS = 8;
	public static final Integer ROUTE_TRAVEL_SRC = 9;
	public static final Integer ROUTE_SRC_UNLOAD = 10;
	
	public static final String SOURCE_ID = "econ";
	public static Logger log = Global.getLogger(EconomyFleetRouteManager.class);
	
	public static Map<LocationDanger, Float> DANGER_LOSS_PROB = new HashMap<LocationDanger, Float>();
	static {
		DANGER_LOSS_PROB.put(LocationDanger.NONE, 0.01f);
		DANGER_LOSS_PROB.put(LocationDanger.MINIMAL, 0.03f);
		DANGER_LOSS_PROB.put(LocationDanger.LOW, 0.07f);
		DANGER_LOSS_PROB.put(LocationDanger.MEDIUM, 0.1f);
		DANGER_LOSS_PROB.put(LocationDanger.HIGH, 0.15f);
		DANGER_LOSS_PROB.put(LocationDanger.EXTREME, 0.2f);
	}
	
	
	protected TimeoutTracker<String> recentlySentTradeFleet = new TimeoutTracker<String>();
	
	public EconomyFleetRouteManager() {
		//super(1f, 14f);
		super(0.2f, 0.3f);
	}
	
	protected Object readResolve() {
		if (recentlySentTradeFleet == null) {
			recentlySentTradeFleet = new TimeoutTracker<String>();
		}
		return this;
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Global.getSector().getClock().convertToDays(amount);
		recentlySentTradeFleet.advance(days);
		
//		MarketAPI from = pickSourceMarket();
//		MarketAPI to = pickDestMarket(from);
	}

	protected String getRouteSourceId() {
		return SOURCE_ID;
	}
	
	protected int getMaxFleets() {
		int numMarkets = Global.getSector().getEconomy().getNumMarkets();
		int maxBasedOnMarkets = numMarkets * 2;
		return Math.min(maxBasedOnMarkets, Global.getSettings().getInt("maxEconFleets"));
	}
	
	
	protected void addRouteFleetIfPossible() {
		MarketAPI from = pickSourceMarket();
		MarketAPI to = pickDestMarket(from);
		if (from != null && to != null) {
			
			EconomyRouteData data = createData(from, to);
			if (data == null) return;
			
			log.info("Added trade fleet route from " + from.getName() + " to " + to.getName());
			
			Long seed = Misc.genRandomSeed();
			String id = getRouteSourceId();
			
			OptionalFleetData extra = new OptionalFleetData(from);
			float tier = data.size;
			float stability = from.getStabilityValue();
			String factionId = from.getFactionId();
			if (!from.getFaction().isHostileTo(Factions.INDEPENDENT) && 
					!to.getFaction().isHostileTo(Factions.INDEPENDENT)) {
				if ((float) Math.random() * 10f > stability + tier) {
					factionId = Factions.INDEPENDENT;
				}
			}
			if (data.smuggling) {
				factionId = Factions.INDEPENDENT;
			}
			extra.factionId = factionId;
			
			RouteData route = RouteManager.getInstance().addRoute(id, from, seed, extra, this);
			route.setCustom(data);
			
			
			StarSystemAPI sysFrom = data.from.getStarSystem();
			StarSystemAPI sysTo = data.to.getStarSystem();
			
//			if (sysFrom.getName().startsWith("Rama") ||
//					sysTo.getName().startsWith("Rama")) {
//				System.out.println("32ff32f23");
//			}
			
			LocationDanger dFrom = WarSimScript.getDangerFor(factionId, sysFrom);
			LocationDanger dTo = WarSimScript.getDangerFor(factionId, sysTo);
			
			LocationDanger danger = dFrom.ordinal() > dTo.ordinal() ? dFrom : dTo;
			
			if (sysFrom != null && sysFrom.isCurrentLocation()) {
				// the player is in the from location, don't auto-lose the trade fleet
				// let it get destroyed by actual fleets, if it does
				danger = LocationDanger.NONE;
			}
			
//			if (danger != LocationDanger.NONE) {
//				System.out.println("efwe234523fwe " + danger.name());
//				dFrom = WarSimScript.getDangerFor(factionId, sysFrom);
//				dTo = WarSimScript.getDangerFor(factionId, sysTo);
//			}
			float pLoss = DANGER_LOSS_PROB.get(danger);
			if (data.smuggling) pLoss *= 0.5;
			if ((float) Math.random() < pLoss) {
				boolean returning = (float) Math.random() < 0.5f; 
				applyLostShipping(data, returning, true, true, true);
				RouteManager.getInstance().removeRoute(route);
				return;
			}
			
			
			//float distLY = Misc.getDistanceLY(from.getLocationInHyperspace(), to.getLocation());
			
			float orbitDays = 2f + (float) Math.random() * 3f;
			//float endDays = 8f + (float) Math.random() * 3f; // longer since includes time from jump-point to source
	
			//orbitDays = 1f;
			orbitDays = data.size * (0.75f + (float) Math.random() * 0.5f);
			
//			endDays = 0.5f;
//			float totalTravelTime = orbitDays + endDays + travelDays * 2f;

			//boolean inSystem = from.getContainingLocation() == to.getContainingLocation();
			
//			WaystationBonus ws = Misc.getWaystation(from, to);
//			if (ws != null && !data.smuggling) {
//				route.addSegment(new RouteSegment(ROUTE_SRC_LOAD, orbitDays, from.getPrimaryEntity()));
//				route.addSegment(new RouteSegment(ROUTE_TRAVEL_WS, from.getPrimaryEntity(), ws.market.getPrimaryEntity()));
//				route.addSegment(new RouteSegment(ROUTE_RESUPPLY_WS, orbitDays, ws.market.getPrimaryEntity()));
//				route.addSegment(new RouteSegment(ROUTE_TRAVEL_DST, ws.market.getPrimaryEntity(), to.getPrimaryEntity()));
//				route.addSegment(new RouteSegment(ROUTE_DST_UNLOAD, orbitDays * 0.5f, to.getPrimaryEntity()));
//				route.addSegment(new RouteSegment(ROUTE_DST_LOAD, orbitDays * 0.5f, to.getPrimaryEntity()));
//				route.addSegment(new RouteSegment(ROUTE_TRAVEL_BACK_WS, to.getPrimaryEntity(), ws.market.getPrimaryEntity()));
//				route.addSegment(new RouteSegment(ROUTE_RESUPPLY_BACK_WS, orbitDays, ws.market.getPrimaryEntity()));
//				route.addSegment(new RouteSegment(ROUTE_TRAVEL_SRC, ws.market.getPrimaryEntity(), from.getPrimaryEntity()));
//				route.addSegment(new RouteSegment(ROUTE_SRC_UNLOAD, orbitDays, from.getPrimaryEntity()));
//			} else {
				route.addSegment(new RouteSegment(ROUTE_SRC_LOAD, orbitDays, from.getPrimaryEntity()));
				route.addSegment(new RouteSegment(ROUTE_TRAVEL_DST, from.getPrimaryEntity(), to.getPrimaryEntity()));
				route.addSegment(new RouteSegment(ROUTE_DST_UNLOAD, orbitDays * 0.5f, to.getPrimaryEntity()));
				route.addSegment(new RouteSegment(ROUTE_DST_LOAD, orbitDays * 0.5f, to.getPrimaryEntity()));
				route.addSegment(new RouteSegment(ROUTE_TRAVEL_SRC, to.getPrimaryEntity(), from.getPrimaryEntity()));
				route.addSegment(new RouteSegment(ROUTE_SRC_UNLOAD, orbitDays, from.getPrimaryEntity()));
//			}
			
			setDelayAndSendMessage(route);
			
			recentlySentTradeFleet.add(from.getId(), Global.getSettings().getFloat("minEconSpawnIntervalPerMarket"));
		}
	}
	
	protected void setDelayAndSendMessage(RouteData route) {
		EconomyRouteData data = (EconomyRouteData) route.getCustom();
		
		float delay = 0.1f;
		delay = 10f;
		if (data.size <= 4f) {
			delay = 5f;
		} else if (data.size <= 6f) {
			delay = 10f;
		} else {
			delay = 15f;
		}
		delay *= 0.75f + (float) Math.random() * 0.5f;
		delay = (int) delay;
		route.setDelay(delay);

		if (!Factions.PLAYER.equals(route.getFactionId())) {
			// queues itself
			new TradeFleetDepartureIntel(route);
		}
	}
	

	
	
	public MarketAPI pickSourceMarket() {
		//return Global.getSector().getEconomy().getMarket("jangala");
		//return Global.getSector().getEconomy().getMarket("sindria");
		//if (true) return Global.getSector().getEconomy().getMarket("chicomoztoc");
		
		WeightedRandomPicker<MarketAPI> markets = new WeightedRandomPicker<MarketAPI>();
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
			if (market.isHidden()) continue;
			//float distLY = Misc.getDistanceToPlayerLY(market.getPrimaryEntity());
			if (!market.hasSpaceport()) continue; // markets w/o spaceports don't launch fleets
			if (SharedData.getData().getMarketsWithoutTradeFleetSpawn().contains(market.getId())) continue;
			
//			if (market.getId().equals("volturn")) {
//				return market;
//			}
			
			if (recentlySentTradeFleet.contains(market.getId())) continue;
			
			markets.add(market, market.getSize());
			
//			if (market.getName().toLowerCase().equals("jannow")) {
//				markets.add(market, 100000f);
//			}
		}
		return markets.pick();
	}
	
	public MarketAPI pickDestMarket(MarketAPI from) {
		//return Global.getSector().getEconomy().getMarket("asharu");
		//return Global.getSector().getEconomy().getMarket("chicomoztoc");
		//if (true) return Global.getSector().getEconomy().getMarket("sindria");
		if (from == null) return null;
		
		WeightedRandomPicker<MarketAPI> markets = new WeightedRandomPicker<MarketAPI>();
		
//		com.getCommodityMarketData().getMarkets()
//		for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
//			if ((from.getEconGroup() == null && market.getEconGroup() != null) ||
//					(from.getEconGroup() != null && !from.getEconGroup().equals(market.getEconGroup()))) {
//				continue;
//			}
//		}
		
		List<CommodityOnMarketAPI> relevant = new ArrayList<CommodityOnMarketAPI>();
		for (CommodityOnMarketAPI com : from.getAllCommodities()) {
			if (com.isNonEcon()) continue;
			
			int exported = Math.min(com.getAvailable(), com.getMaxSupply());
			int imported = Math.max(0, com.getMaxDemand() - exported);
			if (imported > 0 || exported > 0) {
				relevant.add(com);
			}
		}
		
		for (MarketAPI market : Global.getSector().getEconomy().getMarketsInGroup(from.getEconGroup())) {
			if (market.isHidden()) continue;
			if (!market.hasSpaceport()) continue; // markets w/o spaceports don't launch fleets
			if (SharedData.getData().getMarketsWithoutTradeFleetSpawn().contains(market.getId())) continue;
			if (market == from) continue;
			
			int shipping = Misc.getShippingCapacity(market, market.getFaction() == from.getFaction());
			if (shipping <= 0) continue;
			
			float w = 0f;
			for (CommodityOnMarketAPI com : relevant) {
				int exported = Math.min(com.getAvailable(), com.getMaxSupply());
				exported = Math.min(exported, shipping);
				int imported = Math.max(0, com.getMaxDemand() - exported);
				imported = Math.min(imported, shipping);
				
				CommodityOnMarketAPI other = market.getCommodityData(com.getId());
				exported = Math.min(exported, other.getMaxDemand() - other.getMaxSupply());
				if (exported < 0) exported = 0;
				imported = Math.min(imported, Math.min(other.getAvailable(), other.getMaxSupply()));
				
				w += imported;
				w += exported;
			}
			
			if (from.getFaction().isHostileTo(market.getFaction())) {
				w *= 0.25f;
			}
			markets.add(market, w);
		}
		
		return markets.pick();
		
//		for (CommodityOnMarketAPI com : from.getCommoditiesCopy()) {
//			
//			SupplierData sd = com.getSupplier();
//			if (sd != null) {
//				if (sd.getMarket() == from) continue;
//				if (SharedData.getData().getMarketsWithoutTradeFleetSpawn().contains(sd.getMarket().getId())) continue;
//				markets.add(sd.getMarket(), sd.getQuantity());
//			}
//			for (SupplierData curr : com.getExports()) {
//				if (curr.getMarket() == from) continue;
//				if (SharedData.getData().getMarketsWithoutTradeFleetSpawn().contains(sd.getMarket().getId())) continue;
//				markets.add(curr.getMarket(), curr.getQuantity());
//			}
//		}
//		return markets.pick();
		
	}
	
	public static EconomyRouteData createData(MarketAPI from, MarketAPI to) {
		EconomyRouteData smuggling = new EconomyRouteData();
		smuggling.from = from;
		smuggling.to = to;
		smuggling.smuggling = true;
		
		EconomyRouteData legal = new EconomyRouteData();
		legal.from = from;
		legal.to = to;
		legal.smuggling = false;
		
		float legalTotal = 0;
		float smugglingTotal = 0;
		
		
		List<CommodityOnMarketAPI> relevant = new ArrayList<CommodityOnMarketAPI>();
		for (CommodityOnMarketAPI com : from.getAllCommodities()) {
			if (com.isNonEcon()) continue;
			CommodityOnMarketAPI orig = com;
			int exported = Math.min(com.getAvailable(), com.getMaxSupply());
			if (!com.getCommodity().isPrimary()) {
				com = from.getCommodityData(com.getCommodity().getDemandClass());
			}
				
			int imported = Math.max(0, com.getMaxDemand() - exported);
			if (imported > 0 || exported > 0) {
				relevant.add(orig);
			}
		}
		
		int shipping = Misc.getShippingCapacity(from, to.getFaction() == from.getFaction());
		for (CommodityOnMarketAPI com : relevant) {
			CommodityOnMarketAPI orig = com;
			int exported = Math.min(com.getAvailable(), com.getMaxSupply());
			exported = Math.min(exported, shipping);
			
			if (!com.getCommodity().isPrimary()) {
				com = from.getCommodityData(com.getCommodity().getDemandClass());
			}
			
			int imported = Math.max(0, com.getMaxDemand() - exported);
			imported = Math.min(imported, shipping);
			if (orig != com) imported = 0;
			
			CommodityOnMarketAPI other = to.getCommodityData(com.getId());
			exported = Math.min(exported, other.getMaxDemand() - other.getMaxSupply());
			if (exported < 0) exported = 0;
			imported = Math.min(imported, Math.min(other.getAvailable(), other.getMaxSupply()));
			
			if (imported < 0) imported = 0;
			
			if (imported <= 0 && exported <= 0) continue;
			
			boolean illegal = com.getCommodityMarketData().getMarketShareData(from).isSourceIsIllegal() ||
							  com.getCommodityMarketData().getMarketShareData(to).isSourceIsIllegal() ||
							  from.getFaction().isHostileTo(to.getFaction());
			
			if (imported > exported) {
				if (illegal) {
					smuggling.addReturn(orig.getId(), imported);
					smugglingTotal += imported;
				} else {
					legal.addReturn(orig.getId(), imported);
					legalTotal += imported;
				}
			} else {
				if (illegal) {
					smuggling.addDeliver(orig.getId(), exported);
					smugglingTotal += exported;
				} else {
					legal.addDeliver(orig.getId(), exported);
					legalTotal += exported;
				}
			}
		}

		Comparator<CargoQuantityData> comp = new Comparator<CargoQuantityData>() {
			public int compare(CargoQuantityData o1, CargoQuantityData o2) {
				if (o1.getCommodity().isPersonnel() && !o2.getCommodity().isPersonnel()) {
					return 1;
				}
				if (o2.getCommodity().isPersonnel() && !o1.getCommodity().isPersonnel()) {
					return -1;
				}
				return o2.units - o1.units;
			}
		};
		Collections.sort(legal.cargoDeliver, comp); 
		Collections.sort(legal.cargoReturn, comp); 
		Collections.sort(smuggling.cargoDeliver, comp); 
		Collections.sort(smuggling.cargoReturn, comp);
		
		if (smugglingTotal <= 0 && legalTotal <= 0) return null;
		
		EconomyRouteData data = null;
		if ((float) Math.random() * (smugglingTotal + legalTotal) < smugglingTotal) {
			data = smuggling;
		} else {
			data = legal;
		}
		
		while (data.cargoDeliver.size() > 4) {
			data.cargoDeliver.remove(4);
		}
		while (data.cargoReturn.size() > 4) {
			data.cargoReturn.remove(4);
		}
		
//		data.cargoDeliver = data.cargoDeliver.subList(0, Math.min(data.cargoDeliver.size(), 4));
//		data.cargoReturn = data.cargoReturn.subList(0, Math.min(data.cargoReturn.size(), 4));
		
//		data.cargoDeliver.clear();
//		data.cargoReturn.clear();
//		data.addDeliver(Commodities.SHIPS, 5);
		
		float max = 0f;
		for (CargoQuantityData curr : data.cargoDeliver) {
			if (curr.units > max) max = curr.units;
		}
		for (CargoQuantityData curr : data.cargoReturn) {
			if (curr.units > max) max = curr.units;
		}
		
		int types = Math.max(data.cargoDeliver.size(), data.cargoReturn.size());
		if (types >= 3) {
			data.size++;
		}
		if (types >= 4) {
			data.size++;
		}
		
		data.size = max;
		
		
		return data;
	}
	
	

	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		String factionId = route.getFactionId();
		
		boolean smuggling = false;
		if (route.getCustom() instanceof EconomyRouteData) {
			smuggling = ((EconomyRouteData) route.getCustom()).smuggling;
		}
		
		if (factionId != null && route.getMarket() != null && !smuggling && 
				route.getMarket().getFaction().isHostileTo(factionId)) {
			return true;
		}
		return false;
	}
	
	
	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = new Random();
		if (route.getSeed() != null) {
			random = new Random(route.getSeed());
		}
		
		CampaignFleetAPI fleet = createTradeRouteFleet(route, random);
		if (fleet == null) return null;;
		
		//fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_TRADE_FLEET, true);
		
		fleet.addEventListener(this);
		
		fleet.addScript(new EconomyFleetAssignmentAI(fleet, route));
		return fleet;
	}
	
	
	public static String getFleetTypeIdForTier(float tier, boolean smuggling) {
		String type = FleetTypes.TRADE;
		if (tier <= 3) type = FleetTypes.TRADE_SMALL; 
		if (smuggling) {
			type = FleetTypes.TRADE_SMUGGLER;
		}
		return type;
	}

	public static CampaignFleetAPI createTradeRouteFleet(RouteData route, Random random) {
		EconomyRouteData data = (EconomyRouteData) route.getCustom();

		MarketAPI from = data.from;
		MarketAPI to = data.to;
		
//		if (from.getId().equals("umbra")) {
//			System.out.println("wefwefw");
//		}
		
		float tier = data.size;
		
		if (data.smuggling && tier > 4) {
			tier = 4;
		}
		
//		data.smuggling = false;
//		tier = 4;
		
//		float stability = from.getStabilityValue();
//		String factionId = from.getFactionId();
//		if (!from.getFaction().isHostileTo(Factions.INDEPENDENT) && 
//				!to.getFaction().isHostileTo(Factions.INDEPENDENT)) {
//			if ((float) Math.random() * 10f > stability + tier) {
//				factionId = Factions.INDEPENDENT;
//			}
//		}
//		
//		if (data.smuggling) {
//			factionId = Factions.INDEPENDENT;
//		}
		String factionId = route.getFactionId();

		float total = 0f;
		float fuel = 0f;
		float cargo = 0f;
		float personnel = 0f;
		//float ships = 0f;
		
		List<CargoQuantityData> all = new ArrayList<CargoQuantityData>();
		all.addAll(data.cargoDeliver);
		all.addAll(data.cargoReturn);
		for (CargoQuantityData curr : all) {
			CommoditySpecAPI spec = curr.getCommodity();
//			if (spec.getId().equals(Commodities.SHIPS)) {
//				ships = Math.max(ships, curr.units);
//			}
			if (spec.isMeta()) continue;
			
			total += curr.units;
			if (spec.hasTag(Commodities.TAG_PERSONNEL)) {
				personnel += curr.units;
			} else if (spec.getId().equals(Commodities.FUEL)) {
				fuel += curr.units;
			} else {
				cargo += curr.units;
			}
		}

//		System.out.println("Ships: " + ships);
		if (total < 1f) total = 1f;
		
		float fuelFraction = fuel / total;
		float personnelFraction = personnel / total;
		float cargoFraction = cargo / total;
		
//		fuelFraction = 0.33f;
//		personnelFraction = 0.33f;
//		cargoFraction = 0.33f;
		
		if (fuelFraction + personnelFraction + cargoFraction > 0) {
			float mult = 1f / (fuelFraction + personnelFraction + cargoFraction);
			fuelFraction *= mult;
			personnelFraction *= mult;
			cargoFraction *= mult;
		}
		
		log.info("Creating trade fleet of tier " + tier + " for market [" + from.getName() + "]");
		
		float stabilityFactor = 1f + from.getStabilityValue() / 20f;
		
		float combat = Math.max(1f, tier * stabilityFactor * 0.5f) * 10f;
		float freighter = tier * 2f * cargoFraction * 3f;
		float tanker = tier * 2f * fuelFraction * 3f;
		float transport = tier * 2f * personnelFraction * 3f;
		float liner = 0f;
		
		//float utility = 1f + tier * 0.25f;
		float utility = 0f;
		
		String type = getFleetTypeIdForTier(tier, data.smuggling);
		if (data.smuggling) {
			//combat *= 2f;
			freighter *= 0.5f;
			tanker *= 0.5f;
			transport *= 0.5f;
			liner *= 0.5f;
		}
		
		FleetParamsV3 params = new FleetParamsV3(
				from,
				null, // locInHyper
				factionId,
				route.getQualityOverride(), // qualityOverride
				type,
				combat, // combatPts
				freighter, // freighterPts 
				tanker, // tankerPts
				transport, // transportPts
				liner, // linerPts
				utility, // utilityPts
				0f //-0.5f // qualityBonus
		);
		params.timestamp = route.getTimestamp();
		params.onlyApplyFleetSizeToCombatShips = true;
		params.maxShipSize = 3;
		params.officerLevelBonus = -2;
		params.officerNumberMult = 0.5f;
		params.random = random;
		CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
		
		if (fleet == null || fleet.isEmpty()) return null;
		
		if (Misc.isPirateFaction(fleet.getFaction())) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF, true);
		}
		
		if (data.smuggling) {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SMUGGLER, true);
			Misc.makeLowRepImpact(fleet, "smuggler");
		} else {
			fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_TRADE_FLEET, true);
		}
		
		//cargoCap, fuelCap, personnelCap;
		data.cargoCap = fleet.getCargo().getMaxCapacity();
		data.fuelCap = fleet.getCargo().getMaxFuel();
		data.personnelCap = fleet.getCargo().getMaxPersonnel();
		
		//ShippingDisruption.getShippingDisruption(from).getShippingPenalty().addTemporaryModFlat(1f, "fwefwe", 1f);
		
		return fleet;
		
	}

	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		// already reduced by losses taken
		//System.out.println("Cargo: " + fleet.getCargo().getMaxCapacity());
		
		RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
		if (route == null || !(route.getCustom() instanceof EconomyRouteData)) return;
		
		if (route.isExpired()) return;
		
		EconomyRouteData data = (EconomyRouteData) route.getCustom();
		
		float cargoCap = fleet.getCargo().getMaxCapacity();
		float fuelCap = fleet.getCargo().getMaxFuel();
		float personnelCap = fleet.getCargo().getMaxPersonnel();
		
		float lossFraction = 0.34f;
		
		//boolean returning = route.getCurrentIndex() >= 3;
		boolean returning = false;
		if (route.getCurrent() != null && route.getCurrentSegmentId() >= ROUTE_DST_LOAD) {
			returning = true;
		}

		// whether it lost enough carrying capacity to count as an economic loss 
		// of that commodity at destination markets
		boolean lostCargo = data.cargoCap * lossFraction > cargoCap; 
		boolean lostFuel = data.fuelCap * lossFraction > fuelCap; 
		boolean lostPersonnel = data.personnelCap * lossFraction > personnelCap;
		
		// set to 0f so that the loss doesn't happen multiple times for a commodity
		if (lostCargo) data.cargoCap = 0f;
		if (lostFuel) data.fuelCap = 0f;
		if (lostPersonnel) data.personnelCap = 0f;
		
		applyLostShipping(data, returning, lostCargo, lostFuel, lostPersonnel);
		
		// if it's lost all 3 capacities, also trigger a general shipping capacity loss at market
		boolean allThreeLost = true;
		allThreeLost &= data.cargoCap <= 0f || lostCargo;
		allThreeLost &= data.fuelCap <= 0f || lostFuel;
		allThreeLost &= data.personnelCap <= 0f || lostPersonnel;
//		if (fullyLost) {
		
		boolean applyAccessLoss = allThreeLost;
		if (applyAccessLoss) {
			ShippingDisruption.getDisruption(data.from).addShippingLost(data.size);
			ShippingDisruption.getDisruption(data.from).notifyDisrupted(ShippingDisruption.ACCESS_LOSS_DURATION);
		}
		
	}
	
	
	public static void applyLostShipping(EconomyRouteData data, boolean returning, boolean cargo, boolean fuel, boolean personnel) {
		if (!cargo && !fuel && !personnel) return;
		
		int penalty = 1;
		int penalty2 = 2;
		if (!returning) {
			for (CargoQuantityData curr : data.cargoDeliver) {
				CommodityOnMarketAPI com = data.to.getCommodityData(curr.cargo);
				if (!fuel && com.isFuel()) continue;
				if (!personnel && com.isPersonnel()) continue;
				if (!cargo && !com.isFuel() && !com.isPersonnel()) continue;
				
				com.getAvailableStat().addTemporaryModFlat(
						ShippingDisruption.ACCESS_LOSS_DURATION,
						ShippingDisruption.COMMODITY_LOSS_PREFIX + Misc.genUID(), "Recent incoming shipment lost", -penalty2);
				
				ShippingDisruption.getDisruption(data.to).notifyDisrupted(ShippingDisruption.ACCESS_LOSS_DURATION);
			}
		}
		for (CargoQuantityData curr : data.cargoReturn) {
			CommodityOnMarketAPI com = data.from.getCommodityData(curr.cargo);
			if (!fuel && com.isFuel()) continue;
			if (!personnel && com.isPersonnel()) continue;
			if (!cargo && !com.isFuel() && !com.isPersonnel()) continue;
			
			com.getAvailableStat().addTemporaryModFlat(
					ShippingDisruption.ACCESS_LOSS_DURATION, 
					ShippingDisruption.COMMODITY_LOSS_PREFIX + Misc.genUID(), "Recent incoming shipment lost", -penalty2);
			
			ShippingDisruption.getDisruption(data.from).notifyDisrupted(ShippingDisruption.ACCESS_LOSS_DURATION);
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







