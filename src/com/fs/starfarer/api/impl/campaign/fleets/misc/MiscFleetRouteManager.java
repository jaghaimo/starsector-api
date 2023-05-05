package com.fs.starfarer.api.impl.campaign.fleets.misc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class MiscFleetRouteManager extends BaseRouteFleetManager implements FleetEventListener {
	
	public static List<MiscFleetCreatorPlugin> CREATORS = new ArrayList<MiscFleetCreatorPlugin>();
	static {
		CREATORS.add(new MiscPilgrimFleetCreator());
		CREATORS.add(new MiscAcademyFleetCreator());
	}
	
	public static class MiscRouteData {
		public String creatorId;
		public boolean smuggling = false;
		public MarketAPI from;
		public SectorEntityToken to;
		public float size;
		public String factionId;
		public Object custom;
		public MiscRouteData(String creatorId) {
			this.creatorId = creatorId;
		}
		
	}
	
	
	public static final Integer ROUTE_SRC_LOAD = 1;
	public static final Integer ROUTE_TRAVEL_DST = 2;
	public static final Integer ROUTE_DST_UNLOAD = 5;
	public static final Integer ROUTE_DST_LOAD = 6;
	public static final Integer ROUTE_TRAVEL_BACK_WS = 7;
	public static final Integer ROUTE_RESUPPLY_BACK_WS = 8;
	public static final Integer ROUTE_TRAVEL_SRC = 9;
	public static final Integer ROUTE_SRC_UNLOAD = 10;
	
	public static final String SOURCE_ID = "misc_fleet_";
	public static Logger log = Global.getLogger(MiscFleetRouteManager.class);
	
	
	//protected TimeoutTracker<String> recentlySentPilgrims = new TimeoutTracker<String>();
	protected Map<String, TimeoutTracker<String>> recentlySent = new LinkedHashMap<String, TimeoutTracker<String>>();
	protected Random random = new Random();
	transient protected LinkedHashMap data = new LinkedHashMap();
	
	public MiscFleetRouteManager() {
		super(1f, 14f); // whatever values; overridden below
		
		float min = Global.getSettings().getFloatFromArray("miscSpawnInterval", 0);
		float max = Global.getSettings().getFloatFromArray("miscSpawnInterval", 1);
		interval = new IntervalUtil(min, max);
	}
	
	protected Object readResolve() {
		if (recentlySent == null) {
			recentlySent = new LinkedHashMap<String, TimeoutTracker<String>>();
		}
		if (data == null) {
			data = new LinkedHashMap();
		}
		return this;
	}
	
	
	
	public Random getRandom() {
		return random;
	}

	public TimeoutTracker<String> getTimeout(String creatorId) {
		TimeoutTracker<String> result = recentlySent.get(creatorId);
		if (result == null) {
			result = new TimeoutTracker<String>();
			recentlySent.put(creatorId, result);
		}
		return result;
	}

	@Override
	public void advance(float amount) {
		
		//super.advance(amount * 10f);
		super.advance(amount);
		
		float days = Global.getSector().getClock().convertToDays(amount);
		for (TimeoutTracker<String> curr : recentlySent.values()) {
			curr.advance(days);
		}
	}

	protected String getRouteSourceId() {
		return SOURCE_ID;
	}
	
	protected int getMaxFleets() {
		//if (true) return 1;
		int numMarkets = Global.getSector().getEconomy().getNumMarkets();
		int maxBasedOnMarkets = numMarkets * 1;
		return Math.min(maxBasedOnMarkets, Global.getSettings().getInt("maxMiscFleets"));
	}
	
	
	protected void addRouteFleetIfPossible() {
		
		WeightedRandomPicker<MiscFleetCreatorPlugin> picker = new WeightedRandomPicker<MiscFleetCreatorPlugin>(random);
		for (MiscFleetCreatorPlugin curr : CREATORS) {
			int count = 0;
			for (RouteData route : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
				if (route.getCustom() instanceof MiscRouteData) {
					MiscRouteData data = (MiscRouteData) route.getCustom();
					if (curr.getId().equals(data.creatorId)) {
						count++;
					}
				}
			}
			if (count >= curr.getMaxFleetsForThisCreator()) {
				continue;
			}
			
			picker.add(curr, curr.getFrequency());
		}
		
		MiscFleetCreatorPlugin creator = picker.pick();
		if (creator == null) return;
		
		MiscRouteData params = creator.createRouteParams(this, random);
		
		if (params != null) {
			
			MarketAPI from = params.from;
			SectorEntityToken to = params.to;
			log.info("Created misc [" + creator.getId() + "] " + from.getName() + " to " + to.getName());
			
			//Long seed = Misc.genRandomSeed();
			Long seed = random.nextLong();
			String id = getRouteSourceId();
			
			OptionalFleetData extra = new OptionalFleetData(from);
			extra.factionId = params.factionId;
			
			RouteData route = RouteManager.getInstance().addRoute(id, from, seed, extra, this);
			route.setCustom(params);
			
			float orbitDays = params.size * (0.75f + random.nextFloat() * 0.5f);
			orbitDays *= 30f;
			if (orbitDays < 1f) orbitDays = 1f;
			if (orbitDays > 3f) orbitDays = 3f;
			
			route.addSegment(new RouteSegment(ROUTE_SRC_LOAD, orbitDays, from.getPrimaryEntity()));
			route.addSegment(new RouteSegment(ROUTE_TRAVEL_DST, from.getPrimaryEntity(), to));
			route.addSegment(new RouteSegment(ROUTE_DST_UNLOAD, orbitDays * 0.5f, to));
			route.addSegment(new RouteSegment(ROUTE_DST_LOAD, orbitDays * 0.5f, to));
			route.addSegment(new RouteSegment(ROUTE_TRAVEL_SRC, to, from.getPrimaryEntity()));
			route.addSegment(new RouteSegment(ROUTE_SRC_UNLOAD, orbitDays, from.getPrimaryEntity()));
			
			float min = Global.getSettings().getFloatFromArray("miscSpawnTimeoutPerMarket", 0);
			float max = Global.getSettings().getFloatFromArray("miscSpawnTimeoutPerMarket", 1);
			float timeout = min + (max - min) * random.nextFloat();
			
			getTimeout(creator.getId()).add(from.getId(), timeout);
		}
	}

	
	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}
	
	public static MiscFleetCreatorPlugin getCreator(String id) {
		for (MiscFleetCreatorPlugin curr : CREATORS) {
			if (curr.getId().equals(id)) {
				return curr;
			}
		}
		return null;
	}
	
	
	public CampaignFleetAPI spawnFleet(RouteData route) {
		Random random = this.random;
		if (route.getSeed() != null) {
			random = new Random(route.getSeed());
		}
		
		MiscRouteData data = (MiscRouteData) route.getCustom();
		
		MiscFleetCreatorPlugin curr = getCreator(data.creatorId);
		if (curr != null) {
			CampaignFleetAPI fleet = curr.createFleet(this, route, random);
			if (fleet != null) {
				fleet.addEventListener(this);
				fleet.addScript(new MiscFleetAssignmentAI(fleet, route));
			}
			return fleet;
		}
		return null;
	}
	
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
		if (route == null || !(route.getCustom() instanceof MiscRouteData)) return;
		
		//if (route.isExpired()) return;
		if (!battle.isPlayerInvolved()) return;
		
		MiscRouteData data = (MiscRouteData) route.getCustom();
		
		MiscFleetCreatorPlugin curr = getCreator(data.creatorId);
		if (curr != null) {
			curr.reportBattleOccurred(this, fleet, primaryWinner, battle);
		}
	}

	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
		if (route == null || !(route.getCustom() instanceof MiscRouteData)) return;
		
		MiscRouteData data = (MiscRouteData) route.getCustom();
		
		MiscFleetCreatorPlugin curr = getCreator(data.creatorId);
		if (curr != null) {
			curr.reportFleetDespawnedToListener(this, fleet, reason, param);
		}
	}

	public boolean shouldRepeat(RouteData route) {
		return false;
	}
	
	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
		
	}

	public LinkedHashMap getData() {
		return data;
	}
	
	
}







