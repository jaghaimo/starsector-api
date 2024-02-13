package com.fs.starfarer.api.impl.campaign.intel.group;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI.TravelState;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.CountingMap;
import com.fs.starfarer.api.util.Misc;

/**
 * Uses a single RouteData as a placeholder to control the movement and spawning of multiple fleets
 * to make it easier to sync them up. Once the fleets have spawned, they will not auto-despawn and will 
 * carry out their actions until the action sequence is completed.
 * 
 * @author Alex
 *
 * Copyright 2023 Fractal Softworks, LLC
 */
/**
 * @author Alex
 *
 * Copyright 2023 Fractal Softworks, LLC
 */
public abstract class FleetGroupIntel extends BaseIntelPlugin implements RouteFleetSpawner {
	
	public static interface FGIEventListener {
		public void reportFGIAborted(FleetGroupIntel intel);
	}
	
	public static String ABORT_UPDATE  = "abort_update";
	public static String FLEET_LAUNCH_UPDATE  = "fleet_launch_update";
	
	public static final String KEY_SPAWN_FP = "$core_fgiSpawnFP";
	
	public static final String NEVER_STRAGGLER = "$core_fgiNeverStraggler";
	public static final String KEY_POTENTIAL_STRAGGLER = "$core_fgiMaybeStraggler";
	public static final String KEY_STRAGGLER_RETURN_COUNTDOWN = "$core_fgiStragglerReturnCountdown";

	
	public static boolean DEBUG = false;
	

	protected Random random = new Random();
	protected List<FGAction> actions = new ArrayList<FGAction>();
	protected RouteData route;
	protected RouteSegment prevSegment;
	protected List<CampaignFleetAPI> fleets = new ArrayList<CampaignFleetAPI>();
	protected boolean spawnedFleets = false;
	
	protected boolean doIncrementalSpawn = true;
	protected List<CampaignFleetAPI> spawning = new ArrayList<CampaignFleetAPI>();
	protected LocationAPI spawnLocation = null;
	protected float spawnDelay = 0f;
	protected float elapsed = 0f;
	
	protected boolean aborted = false;
	protected float totalFPSpawned = 0f;
	protected float fleetAbortsMissionFPFraction = 0.33f;
	protected float groupAbortsMissionFPFraction = 0.33f;
	protected SectorEntityToken returnLocation;
	protected FactionAPI faction;
	protected int approximateNumberOfFleets = 3;
	
	protected FGIEventListener listener;

	public FleetGroupIntel() {
		Global.getSector().addScript(this);
	}
	
	protected Object readResolve() {
		if (random == null) {
			random = new Random();
		}
		
		//random = new Random(142343232L);
		//System.out.println("RNG CHECK 1: " + random.nextLong());
		return this;
	}
	
	public float getETAUntil(String actionId) {
		return getETAUntil(actionId, false);
	}
	public float getETAUntil(String actionId, boolean untilEndOfAction) {
		float eta = getDelayRemaining();
		for (FGAction action : actions) {
			if (action.getId() != null && action.getId().equals(actionId)) {
				if (untilEndOfAction) {
					eta += action.getEstimatedDaysToComplete();
				}
				return eta;
			} else {
				eta += action.getEstimatedDaysToComplete();
			}
		}
		return 0f;
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
	}
	
	
	protected boolean sourceWasEverMilitaryMarket = false;
	protected boolean sendFleetLaunchUpdate = false;
	protected boolean isSourceFunctionalMilitaryMarket() {
		if (getSource() == null) return false;
		MarketAPI market = getSource().getMarket();
		if (market == null || market.isPlanetConditionMarketOnly()) return false;
		
		if (market.hasCondition(Conditions.DECIVILIZED)) {
			return false;
		}
		
		Industry b = market.getIndustry(Industries.MILITARYBASE);
		if (b == null) b = market.getIndustry(Industries.HIGHCOMMAND);
		if (b == null || b.isDisrupted() || !b.isFunctional()) {
			return false;
		}
		return true;
	}
	
	public boolean isInPreLaunchDelay() {
		return route != null && route.getElapsed() <= 0f && route.getDelay() > 0;
	}
	
	/**
	 * route needs to be created when this method is called.
	 * @param delay
	 */
	public void setPreFleetDeploymentDelay(float delay) {
		if (route != null) {
			route.setDelay(delay);
		}
	}
	
	public float getDelayRemaining() {
		if (!isInPreLaunchDelay() || route == null) return 0f;
		return route.getDelay();
	}
	
	public float getElapsed() {
		return elapsed;
	}

	public void setElapsed(float elapsed) {
		this.elapsed = elapsed;
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		elapsed += amount;
		
		if (isEnded() || isEnding() || isAborted()) return;
		if (route == null) return;

		if (isInPreLaunchDelay()) {
			sendFleetLaunchUpdate = true;
			boolean mil = isSourceFunctionalMilitaryMarket();
			sourceWasEverMilitaryMarket |= mil;
			if (!mil && sourceWasEverMilitaryMarket) {
				abort();
				return;
			}
			return;
		}
		
		if (sendFleetLaunchUpdate) {
			sendFleetLaunchUpdate = false;
			sendUpdateIfPlayerHasIntel(FLEET_LAUNCH_UPDATE, !isPlayerTargeted());
		}
		
		if (!isSucceeded() && !isEnding() && !isAborted() && shouldAbort()) {
			abort();
			return;
		}
		
		if (!spawnedFleets) {
			if (!isSucceeded() && route.getExtra() != null && route.getExtra().damage != null && 
					1f - route.getExtra().damage < getGroupAbortsMissionFPFraction()) {
				abort();
				return;
			}
			
			RouteSegment curr = route.getCurrent();
			if (curr != prevSegment || route.isExpired()) {
				if (prevSegment != null && prevSegment.custom instanceof FGAction) {
					FGAction action = (FGAction) prevSegment.custom;
					action.notifySegmentFinished(prevSegment);
					actions.remove(action);
					notifyActionFinished(action);
					
					if (route.isExpired()) {
						finish(false);
						return;
					}
				}
			}
			prevSegment = curr;
		} else {
			handleIncrementalSpawning(amount);
			pruneDestroyedOrDamagedFleetsAndAbortIfNeeded();
			
			if (actions.isEmpty()) {
				finish(false);
				return;
			}
			
			FGAction action = actions.get(0);
			if (action.isActionFinished()) {
				actions.remove(0);
				notifyActionFinished(action);
				return;
			}
			
			action.directFleets(amount);

			//System.out.println(action + " " + action.isActionFinished());
			if (action.isActionFinished()) {
				actions.remove(0);
				notifyActionFinished(action);
				return;
			}
		}
		
	}
	
	protected boolean shouldAbort() {
		return false;
	}
	
	protected boolean shouldSendIntelUpdateWhenActionFinished(FGAction action) {
		// presumably, !isFailed() is correct here but not 100% sure why -am
		if (action instanceof FGWaitAction && (isAborted() || !isFailed())) {
			return false;
		}
		
		if (action instanceof FGWaitAction) {
			FGWaitAction wait = (FGWaitAction) action;
			if (wait.getOrigDurDays() <= 0f) return false;
		}
		return action != null && action.getId() != null;
	}
	
	protected void notifyActionFinished(FGAction action) {
		if (action != null && shouldSendIntelUpdateWhenActionFinished(action)) {
			sendUpdateIfPlayerHasIntel(action.getId(), !isPlayerTargeted());
		}
	}
	
	
	protected void pruneDestroyedOrDamagedFleetsAndAbortIfNeeded() {
		if (isSucceeded()) {
			List<CampaignFleetAPI> remove = new ArrayList<CampaignFleetAPI>();
			for (CampaignFleetAPI fleet : fleets) {
				if (!fleet.isAlive()) {
					remove.add(fleet);
				}
			}
			fleets.removeAll(remove);
			return; // already returning at this point, still clean up destroyed fleets though
		}
		
		checkStragglers();
		
		List<CampaignFleetAPI> remove = new ArrayList<CampaignFleetAPI>();
		float totalFP = 0f;
		for (CampaignFleetAPI fleet : fleets) {
			float spawnFP = fleet.getMemoryWithoutUpdate().getFloat(KEY_SPAWN_FP);
			if (!fleet.isAlive()) {
				remove.add(fleet);
			} else if (fleet.getFleetPoints() <= spawnFP * getFleetAbortsMissionFPFraction()) {
				remove.add(fleet);
				giveReturnAssignments(fleet);
			} else {
				totalFP += fleet.getFleetPoints();
			}
		}
		
		fleets.removeAll(remove);

		if (totalFP < totalFPSpawned * getGroupAbortsMissionFPFraction()) {
			abort();
			return;
		}
		
	}
	
	protected void checkStragglers() {
		List<CampaignFleetAPI> remove = new ArrayList<CampaignFleetAPI>();
		CountingMap<LocationAPI> fleetLocs = new CountingMap<LocationAPI>();
		
		for (CampaignFleetAPI fleet : fleets) {
			fleetLocs.add(fleet.getContainingLocation());
		}
		
		LocationAPI withMostFleets = null;
		int maxCount = 0;
		for (LocationAPI loc : fleetLocs.keySet()) {
			int count = fleetLocs.getCount(loc);
			if (count > maxCount) {
				withMostFleets = loc;
				maxCount = count;
			}
		}
		
		if (withMostFleets == null) return;
		
		Vector2f com = new Vector2f();
		float weight = 0f;
		for (CampaignFleetAPI fleet : fleets) {
			if (fleet.getContainingLocation() != withMostFleets) continue;
			float w = fleet.getFleetPoints();
			Vector2f loc = new Vector2f(fleet.getLocation());
			loc.scale(w);
			Vector2f.add(com, loc, com);
			weight += w;
		}
		
		if (weight < 1f) weight = 1f;
		com.scale(1f / weight);
		
		FGAction action = getCurrentAction();
		boolean canBeStragglers = action != null && 
				(action instanceof FGTravelAction || action instanceof FGWaitAction);
		
		int maybeStragglers = 0;
		for (CampaignFleetAPI fleet : fleets) {
			boolean potentialStraggler = fleet.getContainingLocation() != withMostFleets;
			if (!potentialStraggler) {
				potentialStraggler |= Misc.getDistance(fleet.getLocation(), com) > 4000;
			}
			if (fleet.getMemoryWithoutUpdate().getBoolean(NEVER_STRAGGLER) || !canBeStragglers) {
				potentialStraggler = false;
			}
			
			MemoryAPI mem = fleet.getMemoryWithoutUpdate();
			if (mem.contains(KEY_POTENTIAL_STRAGGLER)) {
				maybeStragglers++;
			}
			if (!potentialStraggler && mem.contains(KEY_POTENTIAL_STRAGGLER)) {
				mem.unset(KEY_POTENTIAL_STRAGGLER);
				mem.unset(KEY_STRAGGLER_RETURN_COUNTDOWN);
			} else if (mem.contains(KEY_POTENTIAL_STRAGGLER) && !mem.contains(KEY_STRAGGLER_RETURN_COUNTDOWN)) {
				remove.add(fleet);
//				if (fleet.getName().toLowerCase().contains("tactistar")) {
//					System.out.println("fewfwef23r23r23r");
//				}
				giveReturnAssignments(fleet);
			} else if (potentialStraggler && !mem.contains(KEY_POTENTIAL_STRAGGLER)) {
				mem.set(KEY_POTENTIAL_STRAGGLER, true);
				mem.set(KEY_STRAGGLER_RETURN_COUNTDOWN, true, getPotentialStragglerCountdownDays());
			}
		}
		
		fleets.removeAll(remove);
		
		//System.out.println(getClass().getSimpleName() + " maybe stragglers: " + maybeStragglers + ", fleets: " + fleets.size());
	}
	
	protected float getPotentialStragglerCountdownDays() {
		//if (true) return 0.5f;
		return 30f;
	}
	
	protected boolean failedButNotDefeated = false;
	public boolean isFailedButNotDefeated() {
		return failedButNotDefeated;
	}

	public void setFailedButNotDefeated(boolean failedButNotDefeated) {
		this.failedButNotDefeated = failedButNotDefeated;
	}
	
	public void abort() {
		finish(true);
	}
	public void finish(boolean isAbort) {
		boolean wasEnding = isEnding();
		aborted = isAbort;
		endAfterDelay();
		
		if (!isAbort) {
			setFailedButNotDefeated(true);
		}
		
		if (spawnedFleets && !actions.isEmpty()) {
			if (!actions.get(0).isActionFinished()) {
				actions.get(0).setActionFinished(true);
			}
		}
		
		if (route != null) {
			route.expire();
			RouteManager.getInstance().removeRoute(route);
		}
		
		giveFleetsReturnAssignments();
		
		if (!wasEnding && isAbort) {
			sendUpdateIfPlayerHasIntel(ABORT_UPDATE, !isPlayerTargeted());
			
			if (listener != null) {
				listener.reportFGIAborted(this);
			}
		}
	}
	
	public boolean isSpawning() {
		return spawning != null && !spawning.isEmpty();
	}
	
	public boolean isAborted() {
		return aborted;
	}


	protected void giveFleetsReturnAssignments() {
		for (CampaignFleetAPI fleet : fleets) {
			giveReturnAssignments(fleet);
		}
	}
	
	protected void giveReturnAssignments(CampaignFleetAPI fleet) {
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true);
		if (returnLocation != null) {
			Misc.giveStandardReturnAssignments(fleet, returnLocation, null, true);
		} else {
			Misc.giveStandardReturnToSourceAssignments(fleet, true);
		}
	}
	
	
	protected void createRoute(String factionId, int approximateTotalDifficultyPoints,
								int approximateNumberOfFleets, Object custom) {
		OptionalFleetData extra = new OptionalFleetData();
		extra.strength = getApproximateStrengthForTotalDifficultyPoints(factionId, approximateTotalDifficultyPoints);
		extra.factionId = faction.getId(); // needed for WarSimScript etc
		route = RouteManager.getInstance().addRoute("FGI_" + getClass().getSimpleName(), null, 
							Misc.genRandomSeed(), extra, this, custom);
		
		for (FGAction action : actions) {
			int before = route.getSegments().size();
			action.addRouteSegment(route);
			int after = route.getSegments().size();
			for (int i = before; i < after; i++) {
				route.getSegments().get(i).custom = action;
			}
		}
		
		this.approximateNumberOfFleets = approximateNumberOfFleets;
	}
	
	public void setRoute(RouteData route) {
		this.route = route;
	}

	public CampaignFleetAPI spawnFleet(RouteData route) {
		//System.out.println("RNG CHECK 2: " + random.nextLong());
		
		RouteManager.getInstance().removeRoute(route);
		
		if (isSpawnedFleets()) return null;
		
		if (isEnded() || isEnding()) return null;
		if (route == null || route.getCurrent() == null) {
			abort();
			return null;
		}
		
		RouteSegment curr = route.getCurrent();
		
		while (!actions.isEmpty()) {
			FGAction action = actions.get(0);
			if (action == curr.custom) {
				break;
			}
			actions.remove(0);
		}
		
		if (actions.isEmpty()) {
			abort();
			return(null);
		}
		
		if (FleetGroupIntel.DEBUG) {
			System.out.println(getClass().getSimpleName() + ": about to spawn fleets");
		}
		
		spawnFleets();
		setSpawnedFleets(true);
		
		if (doIncrementalSpawn && route.getElapsed() < 0.1f) {
			spawning.addAll(fleets);
			fleets.clear();
			for (CampaignFleetAPI fleet : spawning) {
				if (spawnLocation == null) {
					spawnLocation = fleet.getContainingLocation();
				}
				fleet.getContainingLocation().removeEntity(fleet);
			}
			handleIncrementalSpawning(0.1f);
		} else {
			for (CampaignFleetAPI fleet : fleets) {
				int fp = fleet.getFleetPoints();
				fleet.getMemoryWithoutUpdate().set(KEY_SPAWN_FP, fp);
				totalFPSpawned += fp;
			}
		}
		
		
		actions.get(0).notifyFleetsSpawnedMidSegment(curr);
		
		// don't return any created fleets - route is removed anyway and we're managing the fleets directly
		return null; 
	}
	
	public void setNeverStraggler(CampaignFleetAPI fleet) {
		if (fleet == null) return;
		fleet.getMemoryWithoutUpdate().set(NEVER_STRAGGLER, true);
	}
	
	public void handleIncrementalSpawning(float amount) {
		if (spawning == null || spawning.isEmpty() || spawnLocation == null) return;
		float days = Misc.getDays(amount);
		spawnDelay -= days;
		
		if (spawnDelay <= 0) {
			spawnDelay = 0.25f + (float) Math.random() * 0.25f;
			
			CampaignFleetAPI fleet = spawning.remove(0);
			spawnLocation.addEntity(fleet);
			if (getSource() != null) {
				fleet.setLocation(getSource().getLocation().x, getSource().getLocation().y);
			}
			fleets.add(fleet);
			int fp = fleet.getFleetPoints();
			totalFPSpawned += fp;
			
			if (spawning.isEmpty()) {
				spawning = null;
				spawnLocation = null;
			}
		}
	}

	
	public boolean isDoIncrementalSpawn() {
		return doIncrementalSpawn;
	}

	public void setDoIncrementalSpawn(boolean doIncrementalSpawn) {
		this.doIncrementalSpawn = doIncrementalSpawn;
	}

	public float getTotalFPSpawned() {
		return totalFPSpawned;
	}
	

	public void setTotalFPSpawned(float totalFPSpawned) {
		this.totalFPSpawned = totalFPSpawned;
	}

	public void setSpawnedFleets(boolean spawnedFleets) {
		this.spawnedFleets = spawnedFleets;
	}

	public RouteSegment getSegmentForAction(FGAction action) {
		for (RouteSegment seg : getRoute().getSegments()) {
			if (seg.custom == action) return seg;
		}
		return null;
	}
	
	public void removeAction(String id) {
		FGAction action = getAction(id);
		if (action != null) {
			actions.remove(action);
		}
	}
	public FGAction getAction(String id) {
		for (FGAction curr : actions) {
			if (curr.getId() != null && curr.getId().equals(id)) {
				return curr;
			}
		}
		return null;
	}
	
	public boolean isCurrent(String id) {
		FGAction action = getAction(id);
		return action != null && action == getCurrentAction();
	}
	
	public FGAction getCurrentAction() {
		if (actions.isEmpty()) return null;
		return actions.get(0);
	}
	
	public boolean isSpawnedFleets() {
		return spawnedFleets;
	}

	public int getApproximateNumberOfFleets() {
		return approximateNumberOfFleets;
	}
	
	public void setApproximateNumberOfFleets(int approximateNumberOfFleets) {
		this.approximateNumberOfFleets = approximateNumberOfFleets;
	}

	public List<CampaignFleetAPI> getFleets() {
		return fleets;
	}

	public static TravelState getTravelState(RouteSegment segment) {
		if (segment.isInSystem()) {
			return TravelState.IN_SYSTEM;
		}
		
		if (segment.hasLeaveSystemPhase() && segment.getLeaveProgress() < 1f) {
			return TravelState.LEAVING_SYSTEM;
		}
		if (segment.hasEnterSystemPhase() && segment.getEnterProgress() > 0f) {
			return TravelState.ENTERING_SYSTEM;
		}
		
		return TravelState.IN_HYPER_TRANSIT;
	}
	
	public static LocationAPI getLocation(RouteSegment segment) {
		return getLocationForState(segment, getTravelState(segment));
	}
	
	public static LocationAPI getLocationForState(RouteSegment segment, TravelState state) {
		switch (state) {
		case ENTERING_SYSTEM: {
			if (segment.to != null) {
				return segment.to.getContainingLocation();
			}
			return segment.from.getContainingLocation();
		}
		case IN_HYPER_TRANSIT: return Global.getSector().getHyperspace();
		case IN_SYSTEM: return segment.from.getContainingLocation();
		case LEAVING_SYSTEM: return segment.from.getContainingLocation();
		}
		return null;
	}
	
	
	public static void setLocationAndCoordinates(CampaignFleetAPI fleet, RouteSegment current) {
		TravelState state = getTravelState(current);
		if (state == TravelState.LEAVING_SYSTEM) {
			float p = current.getLeaveProgress();
			SectorEntityToken jp = RouteLocationCalculator.findJumpPointToUse(fleet, current.from);
			if (jp == null) jp = current.from;
			RouteLocationCalculator.setLocation(fleet, p, current.from, jp);
		}
		else if (state == TravelState.ENTERING_SYSTEM) {
			float p = current.getEnterProgress();
			SectorEntityToken jp = RouteLocationCalculator.findJumpPointToUse(fleet, current.to);
			if (jp == null) jp = current.to;
			RouteLocationCalculator.setLocation(fleet, p, jp, current.to);
		}
		else if (state == TravelState.IN_SYSTEM) {
			float p = current.getTransitProgress();
			RouteLocationCalculator.setLocation(fleet, p, 
												current.from, current.to);
		}
		else if (state == TravelState.IN_HYPER_TRANSIT) {
			float p = current.getTransitProgress();
			SectorEntityToken t1 = Global.getSector().getHyperspace().createToken(
														   current.from.getLocationInHyperspace().x, 
														   current.from.getLocationInHyperspace().y);
			SectorEntityToken t2 = Global.getSector().getHyperspace().createToken(
														   current.to.getLocationInHyperspace().x, 
					   									   current.to.getLocationInHyperspace().y);				
			RouteLocationCalculator.setLocation(fleet, p, t1, t2);
		}
	}
	
	
	public List<FGAction> getActions() {
		return actions;
	}

	public void addAction(FGAction action, String id) {
		action.setId(id);
		addAction(action);
	}
	public void addAction(FGAction action) {
		action.setIntel(this);
		actions.add(action);
	}
	
	
	public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
		return false;
	}

	public boolean shouldRepeat(RouteData route) {
		return false;
	}

	public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
		// should never happen since this class removes the route and never returns the created fleets to the manager
	}

	public SectorEntityToken getReturnLocation() {
		return returnLocation;
	}

	public void setReturnLocation(SectorEntityToken returnLocation) {
		this.returnLocation = returnLocation;
	}
	
	
	public float getFleetAbortsMissionFPFraction() {
		return fleetAbortsMissionFPFraction;
	}

	public void setFleetAbortsMissionFPFraction(float fleetAbortsMissionFPFraction) {
		this.fleetAbortsMissionFPFraction = fleetAbortsMissionFPFraction;
	}

	public float getGroupAbortsMissionFPFraction() {
		return groupAbortsMissionFPFraction;
	}

	public void setGroupAbortsMissionFPFraction(float groupAbortsMissionFPFraction) {
		this.groupAbortsMissionFPFraction = groupAbortsMissionFPFraction;
	}
	
	@Override
	public FactionAPI getFactionForUIColors() {
		//return super.getFactionForUIColors();
		return faction;
	}

	public void setFaction(String factionId) {
		faction = Global.getSector().getFaction(factionId);
		
	}
	public void setFaction(FactionAPI faction) {
		this.faction = faction;
	}

	public FactionAPI getFaction() {
		return faction;
	}
	
	public RouteData getRoute() {
		return route;
	}

	public static float getApproximateStrengthForTotalDifficultyPoints(String factionId, int points) {
		float mult = 50f;
		if (factionId != null) {
			FactionAPI faction = Global.getSector().getFaction(factionId);
			if (faction.getCustomBoolean("pirateBehavior")) {
				mult = 35f;
			}
		}
		return points * mult;
	}
	
	/**
	 * Very approximately, the result is around 50 points of "effective strength" per point of difficulty.
	 * Lower (30-40) for pirates/Pathers, a bit >50 for some of the main factions.
	 * 
	 * Not a lot of difference for standard/quality/quantity, since those adjust size etc to try to stay even.
	 */
	public static void computeSampleFleetStrengths() {
		String factionId = null;
		factionId = Factions.LUDDIC_CHURCH;
		factionId = Factions.LUDDIC_PATH;
		factionId = Factions.TRITACHYON;
		factionId = Factions.DIKTAT;
		factionId = Factions.REMNANTS;
		factionId = Factions.PIRATES;
		factionId = Factions.PERSEAN;
		factionId = Factions.HEGEMONY;
		factionId = Factions.INDEPENDENT;
		
		System.out.println("---------------------------------");
		System.out.println("FACTION: " + factionId);
		
		System.out.println("Difficulty\tStd\tQuality\tQuantity");
		for (int j = 0; j < 1; j++) {
		for (int i = 1; i <= 10; i++) {
			
			Vector2f loc = new Vector2f();
			
			float strStandard = 0;
			float strQuality = 0;
			float strQuantity = 0;
			
			{
			FleetCreatorMission m = new FleetCreatorMission(new Random());
			m.beginFleet();
			m.createStandardFleet(i, factionId, loc);
			CampaignFleetAPI fleet = m.createFleet();
			if (fleet != null) {
				strStandard = fleet.getEffectiveStrength();
			}
			}

			{
			FleetCreatorMission m = new FleetCreatorMission(new Random());
			m.beginFleet();
			m.createQualityFleet(i, factionId, loc);
			CampaignFleetAPI fleet = m.createFleet();
			if (fleet != null) {
				strQuality = fleet.getEffectiveStrength();
			}
			}
			
			{
			FleetCreatorMission m = new FleetCreatorMission(new Random());
			m.beginFleet();
			m.createQuantityFleet(i, factionId, loc);
			CampaignFleetAPI fleet = m.createFleet();
			if (fleet != null) {
				strQuantity = fleet.getEffectiveStrength();
			}
			}
			
			
			System.out.println("" + i + "\t\t" + (int)strStandard + "\t" + (int)strQuality + "\t" + (int)strQuantity);
		}
		System.out.println("---------------------------------");
		}
	}

	protected abstract boolean isPlayerTargeted();
	protected abstract void spawnFleets();
	protected abstract SectorEntityToken getSource();
	protected abstract SectorEntityToken getDestination();
	protected abstract String getBaseName();

	protected abstract void addNonUpdateBulletPoints(TooltipMakerAPI info, Color tc, Object param,
													 ListInfoMode mode, float initPad);
	protected abstract void addUpdateBulletPoints(TooltipMakerAPI info, Color tc, Object param,
													 ListInfoMode mode, float initPad);
	
	protected void addStatusSection(TooltipMakerAPI info, float width, float height, float opad) {
		
	}
	protected void addAssessmentSection(TooltipMakerAPI info, float width, float height, float opad) {
		
	}
	protected void addBasicDescription(TooltipMakerAPI info, float width, float height, float opad) {
		
	}
	
	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_MILITARY);
		
		if (Misc.isHyperspaceAnchor(getDestination())) {
			StarSystemAPI system = Misc.getStarSystemForAnchor(getDestination());
			if (system != null && 
					!Misc.getMarketsInLocation(system, Factions.PLAYER).isEmpty()) {
				tags.add(Tags.INTEL_COLONIES);
			}
		}
		
		if (getDestination() != null && 
				getDestination().getContainingLocation() != null &&
				!Misc.getMarketsInLocation(getDestination().getContainingLocation(), Factions.PLAYER).isEmpty()) {
			tags.add(Tags.INTEL_COLONIES);
		}
		tags.add(getFaction().getId());
		return tags;
	}
	
	public String getSortString() {
		return "Fleet Group Movement";
	}
	
	public String getSuccessPostfix() {
		return " - Successful";
	}
	
	public String getFailurePostfix() {
		if (isInPreLaunchDelay()) {
			return " - Aborted";
		}
		if (isFailedButNotDefeated()) {
			return " - Failed";
		}
		return " - Defeated";
	}
	
	public String getName() {
		String base = getBaseName();
		if (isSucceeded()) {
			return base + getSuccessPostfix();
		} else if (isFailed() || isAborted()) {
			return base + getFailurePostfix();
		}
		//return base + " - Over";
		return base;
	}
	
	public boolean isSucceeded() {
		return false;
	}
	public boolean isFailed() {
		return isAborted();
	}
	
	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		
		info.setParaFontDefault();
		
		info.addPara(getName(), c, 0f);
		info.setParaFontDefault();
		addBulletPoints(info, mode);
	}
	
	@Override
	public String getIcon() {
		return getFaction().getCrest();
	}
	
	public String getSmallDescriptionTitle() {
		return getName();
	}
	
	@Override
	public IntelSortTier getSortTier() {
//		if (isPlayerTargeted() && false) {
//			return IntelSortTier.TIER_2;
//		}
		return super.getSortTier();
	}
	
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return getDestination();
	}
	
	public List<ArrowData> getArrowData(SectorMapAPI map) {
		
		SectorEntityToken src = getSource();
		SectorEntityToken dest = getDestination();
		
		if (src == null || dest == null || src.getContainingLocation() == dest.getContainingLocation()) {
			return null;
		}
		
		List<ArrowData> result = new ArrayList<ArrowData>();
		
		ArrowData arrow = new ArrowData(src, dest);
		arrow.color = getFactionForUIColors().getBaseUIColor();
		arrow.width = 20f;
		result.add(arrow);
		
		return result;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}
	
	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		float pad = 3f;
		float opad = 10f;
		
		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;
		
		Color tc = getBulletColorForMode(mode);

		
		bullet(info);
		Object param = getListInfoParam();
		boolean isUpdate = param != null;
		
		if (!isUpdate) {
			addNonUpdateBulletPoints(info, tc, param, mode, initPad);
		} else {
			addUpdateBulletPoints(info, tc, param, mode, initPad);
		}
		unindent(info);
	}
	
	protected void addFactionBulletPoint(TooltipMakerAPI info, Color tc, float initPad) {
		info.addPara("Faction: " + faction.getDisplayName(), initPad, tc,
			 	 faction.getBaseUIColor(), faction.getDisplayName());
	}
	
	protected void addArrivedBulletPoint(String destName, Color destHL, TooltipMakerAPI info, Color tc, float initPad) {
		LabelAPI label = info.addPara("Arrived at " + destName, tc, initPad);
		
		if (destHL != null) {
			String hl = getNameWithNoType(destName);
			label.setHighlightColor(destHL);
			label.highlightLast(hl);
		}
	}
	
	public String getNameWithNoType(String systemName) {
		String hl = systemName;
		if (hl != null) {
			if (hl.endsWith(" system")) {
				hl = hl.replaceAll(" system", "");
			}
			if (hl.endsWith(" nebula")) {
				hl = hl.replaceAll(" nebula", "");
			}
			if (hl.endsWith(" star system")) {
				hl = hl.replaceAll(" star system", "");
			}
		}
		return hl;
	}
	
	public static enum ETAType {
		ARRIVING,
		RETURNING,
		DEPARTURE,
		DEPLOYMENT,
	}
	
	protected void addETABulletPoints(String destName, Color destHL, boolean withDepartedText, float eta, 
									 ETAType type, TooltipMakerAPI info, Color tc, float initPad) {
		Color h = Misc.getHighlightColor();
		
		String hl = getNameWithNoType(destName);
		
		if (type == ETAType.DEPLOYMENT) {
			if ((int) eta <= 0) {
				info.addPara("Fleet deployment imminent", tc, initPad);
			} else {
				String days = (int)eta == 1 ? "day" : "days";
				info.addPara("Estimated %s " + days + " until fleet deployment",
						initPad, tc, h, "" + (int) eta);
			}
			return;
		}
		
		if (type == ETAType.DEPARTURE) {
			if ((int) eta <= 0) {
				info.addPara("Departure imminent", tc, initPad);
			} else {
				String days = (int)eta == 1 ? "day" : "days";
				info.addPara("Estimated %s " + days + " until departure",
						initPad, tc, h, "" + (int) eta);
			}
			return;
		}
		
		LabelAPI label = null;
		if (withDepartedText && eta <= 0) {
			// operations in same location as the "from"
			label = info.addPara("Operating in the " + destName, tc, initPad);
			
			if (destHL != null && label != null) {
				label.setHighlightColor(destHL);
				label.highlightLast(hl);
			}
		} else {
			if (withDepartedText) {
				String pre = "Departed for ";
				if (type == ETAType.RETURNING) {
					pre = "Returning to ";
				}
				label = info.addPara(pre + destName, tc, initPad);
				
				if (destHL != null && label != null) {
					label.setHighlightColor(destHL);
					label.setHighlight(hl);
				}
				initPad = 0f;
			}
			if ((int) eta > 0) {
				String days = (int)eta == 1 ? "day" : "days";
				String post = " until arrival";
				if (type == ETAType.RETURNING) {
					post = " until return";
				}
				if (!withDepartedText) {
					if (type == ETAType.RETURNING) post += " to " + destName;
					else if (type == ETAType.ARRIVING) post += " at " + destName;
				}
				label = info.addPara("Estimated %s " + days + post, initPad, tc, h, "" + (int) eta);
				
				if (!withDepartedText && destHL != null && label != null) {
					label.setHighlightColors(h, destHL);
					label.setHighlight("" + (int) eta, hl);
				}
			} else {
				String pre = "Arrival at ";
				if (type == ETAType.RETURNING) {
					pre = "Return to ";
				}
				label = info.addPara(pre + destName + " is imminent", tc, initPad);
				
				if (destHL != null && label != null) {
					label.setHighlightColor(destHL);
					label.highlightLast(hl);
				}
			}
		}
	}
	
	
	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color tc = Misc.getTextColor();
		float pad = 3f;
		float opad = 10f;
		
		addBasicDescription(info, width, height, opad);
		
		addAssessmentSection(info, width, height, opad);
		
		addStatusSection(info, width, height, opad);
		
		addBulletPoints(info, ListInfoMode.IN_DESC);
	}
	
	protected void showMarketsInDanger(TooltipMakerAPI info, float opad, float width, StarSystemAPI system, 
			List<MarketAPI> targets, String safeStr, String riskStr, String riskStrHighlight) {
		
		Color h = Misc.getHighlightColor();
		float raidStr  = getRoute().getExtra().getStrengthModifiedByDamage();
		float defenderStr = WarSimScript.getEnemyStrength(getFaction(), system, isPlayerTargeted());
		
		List<MarketAPI> safe = new ArrayList<MarketAPI>();
		List<MarketAPI> unsafe = new ArrayList<MarketAPI>();
		for (MarketAPI market : targets) {
			float defensiveStr = defenderStr + WarSimScript.getStationStrength(market.getFaction(), system, market.getPrimaryEntity());
			if (defensiveStr > raidStr * 1.25f) {
				safe.add(market);
			} else {
				unsafe.add(market);
			}
		}
		
		if (safe.size() == targets.size()) {
			info.addPara("However, all colonies " + safeStr + ", " +
						 "owing to their orbital defenses.", opad);
		} else {
			info.addPara("The following colonies " + riskStr, opad,
					Misc.getNegativeHighlightColor(), riskStrHighlight);
			
			FactionAPI f = Global.getSector().getPlayerFaction();
			addMarketTable(info, f.getBaseUIColor(), f.getDarkUIColor(), f.getBrightUIColor(), unsafe, width, opad);
		}
	}
	
	/**
	 * -1: fleet group is weaker
	 * 0: evenly matched
	 * 1: fleet group is stronger
	 * @param target
	 * @return
	 */
	public int getRelativeFGStrength(StarSystemAPI target) {
		float raidStr  = getRoute().getExtra().getStrengthModifiedByDamage();
		float defenderStr = 0f;
		if (target != null) defenderStr = WarSimScript.getEnemyStrength(getFaction(), target, isPlayerTargeted());
		
		if (raidStr < defenderStr * 0.75f) {
			return -1;
		} else if (raidStr < defenderStr * 1.25f) {
			return 0;
		} else {
			return 1;
		}
	}
	
	/**
	 * Returns true if the defenses in the target system are weaker.
	 * @return
	 */
	protected boolean addStrengthDesc(TooltipMakerAPI info, float opad, StarSystemAPI system,
				String forces, String outcomeFailure, String outcomeUncertain, String outcomeSuccess) {
		Color h = Misc.getHighlightColor();
		
		float raidStr  = getRoute().getExtra().getStrengthModifiedByDamage();
		float defenderStr = 0f;
		if (system != null) defenderStr = WarSimScript.getEnemyStrength(getFaction(), system, isPlayerTargeted());
		
		String strDesc = Misc.getStrengthDesc(raidStr);
		int numFleets = (int) getApproximateNumberOfFleets();
		String fleets = "fleets";
		if (numFleets == 1) fleets = "fleet";
		
		String defenderDesc = "";
		String defenderHighlight = "";
		Color defenderHighlightColor = h;
		
		boolean potentialDanger = false;
		String outcome = null;
		if (raidStr < defenderStr * 0.75f) {
			defenderDesc = "The defending fleets are superior";
			defenderHighlightColor = Misc.getPositiveHighlightColor();
			defenderHighlight = "superior";
			outcome = outcomeFailure;
		} else if (raidStr < defenderStr * 1.25f) {
			defenderDesc = "The defending fleets are evenly matched";
			defenderHighlightColor = h;
			defenderHighlight = "evenly matched";
			outcome = outcomeUncertain;
			potentialDanger = true;
		} else {
			defenderDesc = "The defending fleets are outmatched";
			defenderHighlightColor = Misc.getNegativeHighlightColor();
			defenderHighlight = "outmatched";
			outcome = outcomeSuccess;
			potentialDanger = true;
		}
		
		if (outcome != null) {
			defenderDesc += ", and " + outcome + ".";
		} else {
			defenderDesc += ".";
		}
		
		defenderDesc = " " + defenderDesc;
		
		if (system == null) defenderDesc = "";
		
		
		LabelAPI label = info.addPara("The " + forces + " are " +
						"projected to be %s and likely comprised of %s " + fleets + "." + defenderDesc,
				opad, h, strDesc, "" + numFleets);
		label.setHighlight(strDesc, "" + numFleets, defenderHighlight);
		label.setHighlightColors(h, h, defenderHighlightColor);
		
		return potentialDanger;
	}
	
	
	/**
	 * Returns true if the defenses in the target system are weaker.
	 * @return
	 */
	protected boolean addStrengthDesc(TooltipMakerAPI info, float opad, MarketAPI target,
				String forces, String outcomeFailure, String outcomeUncertain, String outcomeSuccess) {
		Color h = Misc.getHighlightColor();
		
		float raidStr  = getRoute().getExtra().getStrengthModifiedByDamage();
		float defenderStr = 0f;
		StarSystemAPI system = target.getStarSystem();
		if (system != null) defenderStr = WarSimScript.getEnemyStrength(getFaction(), system, isPlayerTargeted());
		
		defenderStr += WarSimScript.getStationStrength(target.getFaction(), system, target.getPrimaryEntity());
		
		String strDesc = Misc.getStrengthDesc(raidStr);
		int numFleets = (int) getApproximateNumberOfFleets();
		String fleets = "fleets";
		if (numFleets == 1) fleets = "fleet";
		
		String defenderDesc = "";
		String defenderHighlight = "";
		Color defenderHighlightColor = h;
		
		boolean potentialDanger = false;
		String outcome = null;
		if (raidStr < defenderStr * 0.75f) {
			defenderDesc = "The defending forces are superior";
			defenderHighlightColor = Misc.getPositiveHighlightColor();
			defenderHighlight = "superior";
			outcome = outcomeFailure;
		} else if (raidStr < defenderStr * 1.25f) {
			defenderDesc = "The defending forces are evenly matched";
			defenderHighlightColor = h;
			defenderHighlight = "evenly matched";
			outcome = outcomeUncertain;
			potentialDanger = true;
		} else {
			defenderDesc = "The defending forces are outmatched";
			defenderHighlightColor = Misc.getNegativeHighlightColor();
			defenderHighlight = "outmatched";
			outcome = outcomeSuccess;
			potentialDanger = true;
		}
		
		if (outcome != null) {
			defenderDesc += ", and " + outcome + ".";
		} else {
			defenderDesc += ".";
		}
		
		defenderDesc = " " + defenderDesc;
		
		if (system == null) defenderDesc = "";
		
		
		LabelAPI label = info.addPara("The " + forces + " are " +
						"projected to be %s and likely comprised of %s " + fleets + "." + defenderDesc,
				opad, h, strDesc, "" + numFleets);
		label.setHighlight(strDesc, "" + numFleets, defenderHighlight);
		label.setHighlightColors(h, h, defenderHighlightColor);
		
		return potentialDanger;
	}
	

	
	public FGIEventListener getListener() {
		return listener;
	}

	public void setListener(FGIEventListener listener) {
		this.listener = listener;
	}
	
	@Override
	public String getCommMessageSound() {
		if (isSendingUpdate()) {
			return getSoundStandardUpdate();
		}
		return getSoundMajorPosting();
	}
	
	
}




