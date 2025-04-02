package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.util.Misc;

public class RouteManager implements FleetEventListener {
	public static final String KEY = "$core_routeManager";
	
	public static float IN_OUT_PHASE_DAYS = 3f;
	public static float IN_OUT_PHASE_FRACTION = 0.2f;
	
//	public static float DAYS_SINCE_SEEN_BEFORE_DESPAWN = 60f;
//	public static float DESPAWN_DIST_LY = 4.6f;
//	public static float SPAWN_DIST_LY = 3.5f;
	
	public static float DAYS_SINCE_SEEN_BEFORE_DESPAWN_IF_FAR = 30f;
	public static float DAYS_SINCE_SEEN_BEFORE_DESPAWN_IF_CLOSE = 60f;
	public static float DESPAWN_DIST_LY_FAR = 4f;
	public static float DESPAWN_DIST_LY_CLOSE = 3f;
	public static float SPAWN_DIST_LY = 1.6f;
	
	
	public static class OptionalFleetData {
		public Float strength; // extremely approximate, in fleet points but modified by quality and doctrine
		public Float quality;
		public Float fp; // actual fleet points
		public String factionId;
		public String fleetType;
		public Float damage; // 0 to 1, how much damage the fleet has sustained. spawner should use that info.
		
		public OptionalFleetData() {
		}
		public OptionalFleetData(MarketAPI market) {
			this(market, market.getFactionId());
		}
		public OptionalFleetData(MarketAPI market, String factionId) {
			quality = Misc.getShipQuality(market, factionId);
			this.factionId = factionId;
		}
		
		public float getStrengthModifiedByDamage() {
			if (strength == null) return 0f;
			float str = strength;
			if (damage != null) {
				str *= Math.max(0, 1f - damage);
			}
			return str;
		}
	}
	
	public static class RouteSegment {
		public Integer id; // generally ought to be unique within the route, but doesn't have to be
		public float elapsed = 0f;
		public float daysMax;
		public SectorEntityToken from;
		public SectorEntityToken to;
		public Object custom;
		
		public RouteSegment(Integer id, float daysMax, SectorEntityToken from) {
			this(id, daysMax, from, null, null);
		}
		public RouteSegment(float daysMax, SectorEntityToken from) {
			this(null, daysMax, from, null, null);
		}
		public RouteSegment(Integer id, float daysMax, SectorEntityToken from, Object custom) {
			this(id, daysMax, from, null, custom);
		}
		public RouteSegment(float daysMax, SectorEntityToken from, Object custom) {
			this(null, daysMax, from, null, custom);
		}
		public RouteSegment(Integer id, float daysMax, SectorEntityToken from, SectorEntityToken to) {
			this(id, daysMax, from, to, null);
		}
		public RouteSegment(Integer id, SectorEntityToken from, SectorEntityToken to) {
			this(id, 0f, from, to, null);
			if (from.getContainingLocation() != to.getContainingLocation()) {
				float dist = Misc.getDistance(from.getLocationInHyperspace(), to.getLocationInHyperspace());
				daysMax = dist / 1500f + IN_OUT_PHASE_DAYS * 2f; 
			} else {
				float dist = Misc.getDistance(from.getLocation(), to.getLocation());
				daysMax = dist / 1500f;
			}
		}
		public RouteSegment(float daysMax, SectorEntityToken from, SectorEntityToken to) {
			this(null, daysMax, from, to, null);
		}
		public RouteSegment(Integer id, float daysMax, SectorEntityToken from, SectorEntityToken to, Object custom) {
			this.id = id;
			this.daysMax = daysMax;
			this.from = from;
			this.to = to;
			this.custom = custom;
		}
		
		public LocationAPI getCurrentContainingLocation() {
			LocationAPI loc = null;
			if (from != null && to == null) {
				loc = from.getContainingLocation();
			} else if (from == null && to != null) {
				loc = to.getContainingLocation();
			} else {
				if (from.getContainingLocation() == to.getContainingLocation()) {
					loc = from.getContainingLocation();
				} else {
					if (getLeaveProgress() < 1) {
						loc = from.getContainingLocation();
					} else if (getTransitProgress() < 1) {
						loc = Global.getSector().getHyperspace();
					} else {
						loc = to.getContainingLocation();
					}
				}
			}
			return loc;
		}
		
		public LocationAPI getContainingLocationFrom() {
			if (from == null) return null;
			return from.getContainingLocation();

		}
		public LocationAPI getContainingLocationTo() {
			if (to == null) return null;
			return to.getContainingLocation();
		}
		
		public boolean isTravel() {
			return to != null;
		}
		
		public boolean isInSystem() {
			if (to == null && from != null && from.getContainingLocation() != null && !from.getContainingLocation().isHyperspace()) return true;
			
			if (from != null && !from.getContainingLocation().isHyperspace() && 
					from.getContainingLocation() == to.getContainingLocation()) {
				return true;
			}
			return false;
		}

		public boolean isFromSystemCenter() {
			return from != null && from.getContainingLocation() instanceof StarSystemAPI &&
						((StarSystemAPI)from.getContainingLocation()).getCenter() == from;
		}
		
		public boolean isToSystemCenter() {
			return to != null && to.getContainingLocation() instanceof StarSystemAPI &&
						((StarSystemAPI)to.getContainingLocation()).getCenter() == to;
		}
		
		public boolean hasLeaveSystemPhase() {
			if (isInSystem()) return false;
			if (isFromSystemCenter()) return false;
			if (from != null && from.getContainingLocation().isHyperspace()) return false;
			return true;
		}
		public boolean hasEnterSystemPhase() {
			if (isInSystem()) return false;
			if (isToSystemCenter()) return false;
			if (to != null && to.getContainingLocation().isHyperspace()) return false;
			return true;
		}
		
		public float getProgress() {
			if (daysMax <= 0f) return 1f;
			return elapsed / daysMax;
		}
		
		public float getEnterProgress() {
			float dur = Math.min(daysMax * IN_OUT_PHASE_FRACTION, IN_OUT_PHASE_DAYS);
			float f = 1f - Math.max(0, daysMax - elapsed) / dur;
			if (f > 1) f = 1;
			if (f < 0) f = 0;
			return f;
		}
		public float getLeaveProgress() {
			float dur = Math.min(daysMax * IN_OUT_PHASE_FRACTION, IN_OUT_PHASE_DAYS);
			float f = elapsed / dur;
			if (f > 1) f = 1;
			if (f < 0) f = 0;
			return f;
		}
		public float getTransitProgress() {
			float dur = Math.min(daysMax * IN_OUT_PHASE_FRACTION, IN_OUT_PHASE_DAYS);
			float max = daysMax;
			float e = elapsed;
			if (hasEnterSystemPhase()) {
				max -= dur;
			}
			if (hasLeaveSystemPhase()) {
				max -= dur;
				e -= dur;
			}
			float f = e / max;
			if (f > 1) f = 1;
			if (f < 0) f = 0;
			return f;
		}
		
		public SectorEntityToken getDestination() {
			if (to != null) return to;
			return from;
		}
		
		public SectorEntityToken getFrom() {
			return from;
		}
		public int getId() {
			if (id == null) return 0;
			return id;
		}
		
		
	}
	
	
	public static interface RouteFleetSpawner {
		CampaignFleetAPI spawnFleet(RouteData route);
		boolean shouldCancelRouteAfterDelayCheck(RouteData route);
		boolean shouldRepeat(RouteData route);
		void reportAboutToBeDespawnedByRouteManager(RouteData route);
	}
	
	public static class RouteData {
		protected OptionalFleetData extra = null;
		protected float delay = 0f;
		protected String source;
		protected MarketAPI market;
		protected Long seed;
		protected long timestamp;
		protected List<RouteSegment> segments = new ArrayList<RouteSegment>();
		protected CampaignFleetAPI activeFleet = null;
		protected float daysSinceSeenByPlayer = 1000f;
		protected float elapsed = 0f;
		protected Object custom;
		protected RouteSegment current;
		protected RouteFleetSpawner spawner;
		//protected Boolean suspended = null;
		/**
		 * "source" is a unique string ID for a given set of fleets. Useful to, for example,
		 * limit the number of fleets of a particular type being spawned.
		 * Use RouteManager.getNumRoutesFor(String source) to check number of routes with a given
		 * source that already exist. 
		 * @param source
		 * @param market
		 * @param seed
		 */
		public RouteData(String source, MarketAPI market, Long seed, OptionalFleetData extra) {
			this.source = source;
			this.market = market;
			this.seed = seed;
			this.extra = extra;
		}
		public OptionalFleetData getExtra() {
			return extra;
		}
		public void setExtra(OptionalFleetData extra) {
			this.extra = extra;
		}
		public MarketAPI getMarket() {
			return market;
		}
		public void goToAtLeastNext(RouteSegment from) {
			int index = segments.indexOf(current);
			int indexFrom = segments.indexOf(from);
			if (indexFrom < 0) return;
			if (indexFrom < index) return;
			
			if (indexFrom < segments.size() - 1) {
				current = segments.get(indexFrom + 1);
			} else {
				current.elapsed = current.daysMax;
			}
		}
		
		public void expire() {
			if (!segments.isEmpty()) {
				current = segments.get(segments.size() - 1);
			}
			if (current != null) {
				current.elapsed = current.daysMax;
			}
		}
		
//		public boolean isSuspended() {
//			return suspended != null && suspended == false;
//		}
//		/**
//		 * Set to true to prevent route points from changing the "current" assignment.
//		 * Useful when fleet is active and so route point advancement should be handled directly based on
//		 * what the fleet does.
//		 * @param suspended
//		 */
//		public void setSuspended(boolean suspended) {
//			if (!suspended) {
//				this.suspended = null;
//			} else {
//				this.suspended = suspended;
//			}
//		}
		public Random getRandom(int level) {
			if (seed == null) return new Random();
			return Misc.getRandom(seed, level);
		}
		public String getFactionId() {
			if (extra == null || extra.factionId == null) {
				if (market != null) return market.getFactionId();
				return null;
			}
			return extra.factionId;
		}
		public Float getQualityOverride() {
			if (extra == null) return null;
			return extra.quality;
		}
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
		public Random getRandom() {
			Random random = new Random();
			if (getSeed() != null) {
				random = new Random(getSeed());
			}
			return random;
		}
		
		public Long getSeed() {
			return seed;
		}
		public RouteSegment addSegment(RouteSegment segment) {
			if (segments.isEmpty()) current = segment;
			segments.add(segment);
			return segment;
		}
		public List<RouteSegment> getSegments() {
			return segments;
		}
		public void setCurrent(RouteSegment current) {
			this.current = current;
		}
		public CampaignFleetAPI getActiveFleet() {
			return activeFleet;
		}
		public float getDaysSinceSeenByPlayer() {
			return daysSinceSeenByPlayer;
		}
		public float getElapsed() {
			return elapsed;
		}
		public Object getCustom() {
			return custom;
		}
		public RouteSegment getCurrent() {
			return current;
		}
		public Integer getCurrentSegmentId() {
			if (current == null) return 0;
			return current.getId();
		}
		public int getCurrentIndex() {
			return segments.indexOf(current);
		}
		public RouteFleetSpawner getSpawner() {
			return spawner;
		}
		public String getSource() {
			return source;
		}
		
		public Vector2f getInterpolatedHyperLocation() {
			//if (true) return new Vector2f();
			if (current == null) return new Vector2f(100000000f, 0);
			
			if (current.isInSystem() || current.getContainingLocationTo() == null || current.to == null) {
				return current.from.getLocationInHyperspace();
			}
			
			//float p = current.elapsed / current.daysMax;
			float p = current.getTransitProgress();
			
			
			Vector2f from = current.getContainingLocationFrom().getLocation();
			if (current.getContainingLocationFrom().isHyperspace()) {
				from = current.getFrom().getLocation();
			}
			Vector2f to = current.getContainingLocationTo().getLocation();
			if (current.getContainingLocationTo().isHyperspace()) {
				to = current.to.getLocation();
			}
			
//			Vector2f interpLoc = Misc.interpolateVector(current.getContainingLocationFrom().getLocation(),
//														current.getContainingLocationTo().getLocation(),
//														p);
			Vector2f interpLoc = Misc.interpolateVector(from, to, p);
			return interpLoc;
		}
		
		
		public boolean isExpired() {
			if (segments.indexOf(current) == segments.size() - 1 && current.elapsed >= current.daysMax) {
				return true;
			}
			return false;
		}
		public void setCustom(Object custom) {
			this.custom = custom;
		}
		public float getDelay() {
			return delay;
		}
		public void setDelay(float delay) {
			this.delay = delay;
		}
		
	}
	
	public static RouteManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		if (test instanceof RouteManager) {
			return (RouteManager) test; 
		}
		
		RouteManager manager = new RouteManager();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, manager);
		return manager;
	}
	
	protected List<RouteData> routes = new ArrayList<RouteData>();
	protected transient Map<String, List<RouteData>> sourceToRoute = new LinkedHashMap<String, List<RouteData>>();
	
	
	Object readResolve() {
		sourceToRoute = new LinkedHashMap<String, List<RouteData>>();
		for (RouteData route : routes) {
			addToMap(route);
		}
		return this;
	}
	protected transient Map<LocationAPI, List<RouteData>> routesByLocation = null;
	
	public List<RouteData> getRoutesInLocation(LocationAPI location) {
		if (routesByLocation == null) {
			routesByLocation = new LinkedHashMap<LocationAPI, List<RouteData>>();
			for (RouteData route : routes) {
//				if (route.getSource().startsWith("FGI")) {
//					System.out.println("fewefwefw");
//				}
				RouteSegment current = route.current;
				if (current == null) continue;
				
				LocationAPI loc = null;
				if (current.from != null && current.to == null) {
					loc = current.from.getContainingLocation();
				} else if (current.from == null && current.to != null) {
					loc = current.to.getContainingLocation();
				} else {
					if (current.from.getContainingLocation() == current.to.getContainingLocation()) {
						loc = current.from.getContainingLocation();
					} else {
						if (current.getLeaveProgress() < 1) {
							loc = current.from.getContainingLocation();
						} else if (current.getTransitProgress() < 1) {
							loc = Global.getSector().getHyperspace();
						} else {
							loc = current.to.getContainingLocation();
						}
					}
				}
				
				if (loc != null) {
					List<RouteData> list = routesByLocation.get(loc);
					if (list == null) {
						list = new ArrayList<RouteData>();
						routesByLocation.put(loc, list);
					}
					list.add(route);
				}
			}
		}
		
		List<RouteData> list = routesByLocation.get(location);
		if (list == null) list = new ArrayList<RouteData>();
		return list;
	}
	
	
	public RouteData addRoute(String source, MarketAPI market, Long seed, OptionalFleetData extra, RouteFleetSpawner spawner) {
		return addRoute(source, market, seed, extra, spawner, null);
	}
	
	@Deprecated public void removeRote(RouteData route) {
		removeRoute(route);
	}
	public void removeRoute(RouteData route) {
		routes.remove(route);
		removeFromMap(route);
	}
	
	public RouteData addRoute(String source, MarketAPI market, Long seed, OptionalFleetData extra, RouteFleetSpawner spawner, Object custom) {
		routesByLocation = null;
		
		RouteData route = new RouteData(source, market, seed, extra);
		route.spawner = spawner;
		route.custom = custom;
		route.timestamp = Global.getSector().getClock().getTimestamp();
		routes.add(route);
		addToMap(route);
		//routes.clear();
		return route;
	}
	
	public int getNumRoutesFor(String source) {
		if (!sourceToRoute.containsKey(source)) return 0;
		return sourceToRoute.get(source).size();
	}
	
	public void addToMap(RouteData route) {
		if (route.source == null) return;
		
		List<RouteData> forSource = getRoutesForSource(route.source);
		forSource.add(route);
	}
	
	public void removeFromMap(RouteData route) {
		routesByLocation = null;
		
		if (route.source == null) return;
		
		List<RouteData> forSource = getRoutesForSource(route.source);
		forSource.remove(route);
		if (forSource.isEmpty()) {
			sourceToRoute.remove(route.source);
		}
	}
	
	public List<RouteData> getRoutesForSource(String source) {
		List<RouteData> forSource = sourceToRoute.get(source);
		if (forSource == null) {
			forSource = new ArrayList<RouteData>();
			sourceToRoute.put(source, forSource);
		}
		return forSource;
	}
	
	public RouteData getRoute(String source, CampaignFleetAPI fleet) {
		List<RouteData> forSource = sourceToRoute.get(source);
		if (forSource == null) {
			forSource = new ArrayList<RouteData>();
			sourceToRoute.put(source, forSource);
		}
		for (RouteData data : forSource) {
			if (data.activeFleet == fleet) return data;
		}
		return null;
	}
	
	
	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
//		sourceToRoute.get("salvage_aegea")
//		int empty = 0;
//		int total = 0;
//		for (RouteData curr : routes) {
//			total++;
//			//if (curr.activeFleet != null && curr.activeFleet.getFleetData().getMembersListCopy().isEmpty()) {
//			if (curr.activeFleet != null && !curr.activeFleet.isAlive()) {
//			//if (curr.activeFleet != null) {
//				empty++;
//			}
//		}
//		System.out.println("Empty: " + empty + ", total: " + total);
//		System.out.println("Routes: " + routes.size());
		
		routesByLocation = null;
		
		advanceRoutes(days);
		spawnAndDespawn();
	}
	
	protected void spawnAndDespawn() {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return;
		
		//System.out.println("Num routes: " + routes.size());
		int add = 0;
		int sub = 0;
		for (RouteData data : new ArrayList<RouteData>(routes)) {
			if (data.activeFleet != null && data.activeFleet.getContainingLocation() == player.getContainingLocation()) {
				VisibilityLevel level = data.activeFleet.getVisibilityLevelToPlayerFleet();
				if ((level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS ||
						level == VisibilityLevel.COMPOSITION_DETAILS) && 
						data.activeFleet.wasMousedOverByPlayer()) {
					data.daysSinceSeenByPlayer = 0f;
				}
			}
			
//			if (shouldDespawn(data)) {
//				System.out.println("wfwefwe");
//			}
			if (shouldDespawn(data)) {
				//shouldDespawn(data);
				data.spawner.reportAboutToBeDespawnedByRouteManager(data);
				data.activeFleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null);
				if (data.activeFleet.getContainingLocation() != null) {
					data.activeFleet.getContainingLocation().removeEntity(data.activeFleet);
				}
				data.activeFleet = null;
				sub++;
				
				//System.out.println("Despawn index: " + routes.indexOf(data));
				continue;
			}
			
//			if (shouldSpawn(data)) {
//				System.out.println("wefwef23f23");
//			}
			if (shouldSpawn(data)) {
				//shouldSpawn(data);
				data.activeFleet = data.spawner.spawnFleet(data);
				if (data.activeFleet != null) {
					data.activeFleet.addEventListener(this);
					add++;
				} else {
					data.expire();
				}
				
//				Vector2f interpLoc = data.getInterpolatedHyperLocation();
//				float distLY = Misc.getDistanceLY(interpLoc, player.getLocationInHyperspace());
//				float distLYActual = Misc.getDistanceLY(data.activeFleet.getLocation(), player.getLocationInHyperspace());
//				System.out.println("Dist: " + distLY + ", actual: " + distLYActual);
				//System.out.println("Spawn index: " + routes.indexOf(data));
				continue;
			}
			
		}
		
//		System.out.println("Add: " + add + ", sub: " + sub);
//		System.out.println("Total: " + Global.getSector().getHyperspace().getFleets().size());
	}
	
	protected boolean shouldSpawn(RouteData data) {
		if (data.delay > 0) return false;
		if (data.activeFleet != null) return false;
		//if (true) return true;
		
//		int index = data.getSegments().indexOf(data.getCurrent());
//		if (index <= 0 || data.getCurrent().elapsed < 8f) return false;
		//if (index <= 3 || data.getCurrent().elapsed < 1f) return false;
//		if (index < 5) return false;
		
		Vector2f interpLoc = data.getInterpolatedHyperLocation();
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		float distLY = Misc.getDistanceLY(interpLoc, player.getLocationInHyperspace());
		if (distLY < SPAWN_DIST_LY) return true;
		
		return false;
	}
	
	public static boolean isPlayerInSpawnRange(SectorEntityToken from) {
		float distLY = Misc.getDistanceLY(from.getLocationInHyperspace(), Global.getSector().getPlayerFleet().getLocationInHyperspace());
		if (distLY < SPAWN_DIST_LY) return true;
		return false;
	}
	
	
	protected boolean shouldDespawn(RouteData data) {
		if (data.activeFleet == null) return false;
		if (data.daysSinceSeenByPlayer < DAYS_SINCE_SEEN_BEFORE_DESPAWN_IF_FAR) return false;
		if (data.activeFleet.getBattle() != null) return false;
		if (data.activeFleet.isNoAutoDespawn()) return false;
		
		//if (true) return false;
		
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		float distLY = Misc.getDistanceLY(data.activeFleet.getLocationInHyperspace(), player.getLocationInHyperspace());
		
		if (distLY > DESPAWN_DIST_LY_FAR) return true;
		if (distLY > DESPAWN_DIST_LY_CLOSE &&
				data.daysSinceSeenByPlayer > DAYS_SINCE_SEEN_BEFORE_DESPAWN_IF_CLOSE) return true;
		
		return false;
	}
	
	protected void advanceRoutes(float days) {
		
//		int count = 0;
//		for (RouteData curr : routes) {
//			if (curr.activeFleet != null) {
//				count++;
//			}
//		}
//		System.out.println("Active: " + count);
		
		Iterator<RouteData> iter = routes.iterator();
		while (iter.hasNext()) {
			RouteData route = iter.next();
			//if (route.isSuspended()) continue;
			boolean delay = route.delay > 0;
			if (delay) {
				route.delay -= days;
				if (route.delay < 0) route.delay = 0;
				if (route.delay > 0) continue;
				
				if (route.spawner.shouldCancelRouteAfterDelayCheck(route)) {
					iter.remove();
					removeFromMap(route);
					continue;
				}
				
				route.timestamp = Global.getSector().getClock().getTimestamp();
			}
			
			if (route.current == null && route.segments.isEmpty()) {
				iter.remove();
				removeFromMap(route);
				continue;
			}
			
			if (route.current == null) route.current = route.segments.get(0);
			
			route.current.elapsed += days;
			route.daysSinceSeenByPlayer += days;
			route.elapsed += days;
			
			if (route.getActiveFleet() != null) continue;
			
			//if (route.isSuspended()) continue;
			
			if (route.current.elapsed >= route.current.daysMax) {
				int index = route.segments.indexOf(route.current);
				if (index < route.segments.size() - 1) {
					route.current = route.segments.get(index + 1);
				} else {
					if (route.spawner.shouldRepeat(route)) {
						route.current = null;
						for (RouteSegment segment : route.segments) {
							segment.elapsed = 0f;
						}
					} else {
						removeFromMap(route);
						iter.remove();
					}
				}
			}
		}
	}
	
	
	
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (reason == FleetDespawnReason.PLAYER_FAR_AWAY) {
			return;
		}
		//((CampaignFleetAPI)fleet).getName()
		boolean found = false;
		for (RouteData curr : routes) {
			if (curr.activeFleet != null && curr.activeFleet == fleet) {
				if (reason == FleetDespawnReason.PLAYER_FAR_AWAY) {
					//curr.setSuspended(false);
				} else {
					routes.remove(curr);
					removeFromMap(curr);
					found = true;
				}
				break;
			}
		}

// this can happen when route expires while fleet is active
//		if (!found) {
//			System.out.println("NOT FOUND FLEET BEING REMOVED");
//		}

	}
	
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		
	}

}


