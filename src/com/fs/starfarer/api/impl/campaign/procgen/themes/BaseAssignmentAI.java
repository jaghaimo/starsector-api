package com.fs.starfarer.api.impl.campaign.procgen.themes;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.FleetAIFlags;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.Objectives;
import com.fs.starfarer.api.plugins.BuildObjectiveTypePicker;
import com.fs.starfarer.api.plugins.BuildObjectiveTypePicker.BuildObjectiveParams;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public abstract class BaseAssignmentAI implements EveryFrameScript {

	public interface FleetActionDelegate {
		boolean canRaid(CampaignFleetAPI fleet, MarketAPI market);
		String getRaidApproachText(CampaignFleetAPI fleet, MarketAPI market);
		String getRaidActionText(CampaignFleetAPI fleet, MarketAPI market);
		void performRaid(CampaignFleetAPI fleet, MarketAPI market);
		
		String getRaidPrepText(CampaignFleetAPI fleet, SectorEntityToken from);
		String getRaidInSystemText(CampaignFleetAPI fleet);
		String getRaidDefaultText(CampaignFleetAPI fleet);
	}
	
	protected CampaignFleetAPI fleet;
	protected Boolean done = null;
	protected Boolean giveInitial = true;
	protected FleetActionDelegate delegate = null;
	
	public BaseAssignmentAI() {
	}

	public BaseAssignmentAI(CampaignFleetAPI fleet) {
		this.fleet = fleet;
		giveInitialAssignments();
	}
	
	public FleetActionDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(FleetActionDelegate delegate) {
		this.delegate = delegate;
	}

	protected abstract void giveInitialAssignments();
	protected abstract void pickNext();

	public void advance(float amount) {
		if (fleet.getCurrentAssignment() == null) {
			pickNext();
		}
	}

	
	public boolean isDone() {
		return done != null;
	}
	
	public void setDone() {
		done = true;
	}

	public boolean runWhilePaused() {
		return false;
	}



	protected IntervalUtil raidTracker;
	protected IntervalUtil capTracker;
	protected IntervalUtil buildTracker;
	protected void checkRaid(float amount) {
		if (fleet.isInHyperspace()) return;
		
		if (raidTracker == null) {
			raidTracker = new IntervalUtil(0.3f, 0.7f);
		}
		raidTracker.advance(Misc.getDays(amount));
		if (!raidTracker.intervalElapsed()) return;
		
		checkColonyAction();
	}
	
	protected void checkCapture(float amount) {
		if (fleet.isInHyperspace()) return;
		
		if (capTracker == null) {
			 capTracker = new IntervalUtil(0.3f, 0.7f);
		}
		capTracker.advance(Misc.getDays(amount));
		if (!capTracker.intervalElapsed()) return;
		
		checkObjectiveAction(false);
	}
	
	protected void checkBuild(float amount) {
		if (fleet.isInHyperspace()) return;
		
		if (buildTracker == null) {
			buildTracker = new IntervalUtil(0.3f, 0.7f);
		}
		buildTracker.advance(Misc.getDays(amount));
		if (!buildTracker.intervalElapsed()) return;
		
		checkObjectiveAction(true);
	}
	
	protected void checkObjectiveAction(boolean build) {
		if (!canTakeAction()) return;
		
//		if (fleet.isInCurrentLocation()) {
//			System.out.println("fwewefe");
//		}
		SectorEntityToken closest = null;
		float minDist = Float.MAX_VALUE;
		
		if (build) {
			for (SectorEntityToken objective : fleet.getContainingLocation().getEntitiesWithTag(Tags.STABLE_LOCATION)) {
				// so that it's not continuously rebuilt if it keeps being salvaged by the player
				if (objective.getMemoryWithoutUpdate().getBoolean(MemFlags.RECENTLY_SALVAGED)) continue;
				
				float dist = Misc.getDistance(fleet, objective);
				if (dist < minDist) {
					closest = objective;
					minDist = dist;
				}
			}
		} else {
			for (SectorEntityToken objective : fleet.getContainingLocation().getEntitiesWithTag(Tags.OBJECTIVE)) {
				if (objective.getFaction() == fleet.getFaction()) continue;
				if (!objective.getFaction().isHostileTo(fleet.getFaction())) {
					boolean ownerHasColonyInSystem = false;
					for (MarketAPI curr : Misc.getMarketsInLocation(objective.getContainingLocation())) {
						if (curr.getFaction() == objective.getFaction() && 
								!curr.getFaction().isNeutralFaction()) {
							ownerHasColonyInSystem = true;
							break;
						}
					}
					if (ownerHasColonyInSystem) continue;
				}
				
				float dist = Misc.getDistance(fleet, objective);
				if (dist < minDist) {
					closest = objective;
					minDist = dist;
				}
			}
		}
		
		if (closest == null || minDist > 2000f) return;
		
		for (CampaignFleetAPI other : Misc.getNearbyFleets(closest, 2000f)) {
			if (other == fleet) continue;
			
			if (other.isHostileTo(fleet)) {
				if (other.getFleetPoints() > fleet.getFleetPoints() * 0.25f) return;
			}
			
			if (other.getFaction() == fleet.getFaction()) {
				float dist = Misc.getDistance(other, closest);
				if (dist < minDist) return;
			}
		}
		
		if (build) {
			BuildObjectiveParams params = new BuildObjectiveParams();
			params.faction = fleet.getFaction();
			params.fleet = fleet;
			params.stableLoc = closest;
			BuildObjectiveTypePicker pick = Global.getSector().getGenericPlugins().pickPlugin(BuildObjectiveTypePicker.class, params);
			String type = null;
			if (pick != null) {
				type = pick.pickObjectiveToBuild(params);
			}
			if (type != null) {
				giveBuildOrder(closest, type);
			}
		} else {
			giveCaptureOrder(closest);
		}
	}
	
	
	protected void giveCaptureOrder(final SectorEntityToken target) {
		clearTempAssignments(fleet);
		
		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
								MemFlags.FLEET_BUSY, TEMP_BUSY_REASON, true, (1.5f + (float) Math.random()));
		
		String name = ((CustomCampaignEntityAPI)target).getCustomEntitySpec().getNameInText();
		String capText = "taking control of " + name;
		String moveText = "moving to take control of " + name;
		
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(
							Misc.getAngleInDegrees(target.getLocation(), fleet.getLocation()));
		float holdRadius = fleet.getRadius() + target.getRadius() - 10;
		loc.scale(holdRadius);
		Vector2f.add(loc, target.getLocation(), loc);
		SectorEntityToken holdLoc = target.getContainingLocation().createToken(loc);
		
		holdLoc.setCircularOrbit(target,
				   Misc.getAngleInDegrees(target.getLocation(), fleet.getLocation()),
					holdRadius, 1000000f);
		fleet.getContainingLocation().addEntity(holdLoc);
		Misc.fadeAndExpire(holdLoc, 5f);
		
		fleet.addAssignmentAtStart(FleetAssignment.HOLD, holdLoc, 0.5f, capText, new Script() {
			public void run() {
				if (target.isAlive()) {
					Objectives o = new Objectives(target);
					o.control(fleet.getFaction().getId());
				}
				clearTempAssignments(fleet);
			}
		});
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (curr != null) {
			curr.setCustom(TEMP_ASSIGNMENT);
		}
		
		float dist = Misc.getDistance(target, fleet);
		if (dist > fleet.getRadius() + target.getRadius() + 300f) {
			
			fleet.addAssignmentAtStart(FleetAssignment.DELIVER_CREW, target, 3f, moveText, null);
			curr = fleet.getCurrentAssignment();
			if (curr != null) {
				curr.setCustom(TEMP_ASSIGNMENT);
			}
		}
	}
	
	
	protected void giveBuildOrder(final SectorEntityToken target, String type) {
		clearTempAssignments(fleet);
		
		CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec(type);
		
		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
								MemFlags.FLEET_BUSY, TEMP_BUSY_REASON, true, (1.5f + (float) Math.random()));
		
		String name = spec.getNameInText();
		String capText = "constructing " + name;
		String moveText = "moving to construct " + name;
		
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(
							Misc.getAngleInDegrees(target.getLocation(), fleet.getLocation()));
		float holdRadius = fleet.getRadius() + target.getRadius();
		loc.scale(holdRadius);
		Vector2f.add(loc, target.getLocation(), loc);
		SectorEntityToken holdLoc = target.getContainingLocation().createToken(loc); 
		
		holdLoc.setCircularOrbit(target,
				   Misc.getAngleInDegrees(target.getLocation(), fleet.getLocation()),
					holdRadius, 1000000f);
		fleet.getContainingLocation().addEntity(holdLoc);
		Misc.fadeAndExpire(holdLoc, 5f);
		
		fleet.addAssignmentAtStart(FleetAssignment.HOLD, holdLoc, 0.5f, capText, new Script() {
			public void run() {
				if (target.isAlive()) {
					// re-figure-out the type to avoid duplicates
					BuildObjectiveParams params = new BuildObjectiveParams();
					params.faction = fleet.getFaction();
					params.fleet = fleet;
					params.stableLoc = target;
					BuildObjectiveTypePicker pick = Global.getSector().getGenericPlugins().pickPlugin(BuildObjectiveTypePicker.class, params);
					String type = null;
					if (pick != null) {
						type = pick.pickObjectiveToBuild(params);
					}
					
					Objectives o = new Objectives(target);
					o.build(type, fleet.getFaction().getId());
				}
				clearTempAssignments(fleet);
			}
		});
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (curr != null) {
			curr.setCustom(TEMP_ASSIGNMENT);
		}
		
		float dist = Misc.getDistance(target, fleet);
		if (dist > fleet.getRadius() + target.getRadius() + 300f) {
			
			fleet.addAssignmentAtStart(FleetAssignment.DELIVER_CREW, target, 3f, moveText, null);
			curr = fleet.getCurrentAssignment();
			if (curr != null) {
				curr.setCustom(TEMP_ASSIGNMENT);
			}
		}

	}
	
	
	
	public static String TEMP_ASSIGNMENT = "temp_PAV4";
	public static String TEMP_BUSY_REASON = "temp_PAV4";
	protected void clearTempAssignments(CampaignFleetAPI fleet) {
		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
							   MemFlags.FLEET_BUSY, TEMP_BUSY_REASON, false, 0f);
		for (FleetAssignmentDataAPI curr : fleet.getAI().getAssignmentsCopy()) {
			if (TEMP_ASSIGNMENT.equals(curr.getCustom())) {
				fleet.getAI().removeAssignment(curr);
			}
		}
	}
	
	
	
	
	
	
	
	protected boolean canTakeAction() {
		if (!RouteManager.isPlayerInSpawnRange(fleet)) return false;
		
		if (fleet.getBattle() != null) return false;
		if (fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.FLEET_BUSY)) {
			return false;
		}
		
		if (fleet.isCurrentAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN)) return false;
		
		MemoryAPI mem = fleet.getMemoryWithoutUpdate();
		if (Misc.flagHasReason(mem, MemFlags.FLEET_BUSY, TEMP_BUSY_REASON)) return false;
			
		
		if (fleet.getAI() instanceof ModularFleetAIAPI) {
			ModularFleetAIAPI ai = (ModularFleetAIAPI) fleet.getAI();
			if (ai.getAssignmentModule().areAssignmentsFrozen()) return false;
		}
		
		CampaignFleetAPI pursueTarget = mem.getFleet(FleetAIFlags.PURSUIT_TARGET);
		CampaignFleetAPI fleeingFrom = mem.getFleet(FleetAIFlags.NEAREST_FLEEING_FROM);
		
		if (pursueTarget != null || fleeingFrom != null) {
			return false;
		}
		return true;
	}
	
	
	

	protected void checkColonyAction() {
		if (!canTakeAction()) return;
		
		
		MarketAPI closest = null;
		float minDist = Float.MAX_VALUE;
		
//		if (fleet.getFaction().getId().equals(Factions.DIKTAT)) {
//			System.out.println("wefwefwe");
//		}
		
		for (MarketAPI market : Misc.getMarketsInLocation(fleet.getContainingLocation())) {
			if (delegate != null) {
				if (!delegate.canRaid(fleet, market)) continue;
			} else {
				if (!market.getFaction().isHostileTo(fleet.getFaction())) continue;
			}
			
			float dist = Misc.getDistance(fleet, market.getPrimaryEntity());
			if (dist < minDist) {
				closest = market;
				minDist = dist;
			}
		}
		
		if (closest == null || minDist > 2000f) return;
		
		for (CampaignFleetAPI other : Misc.getNearbyFleets(closest.getPrimaryEntity(), 2000f)) {
			if (other == fleet) continue;
			
			if (other.isHostileTo(fleet)) {
				VisibilityLevel vis = other.getVisibilityLevelTo(fleet);
				boolean canSee = vis == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS || vis == VisibilityLevel.COMPOSITION_DETAILS;
				if (!canSee && other.getFaction() != fleet.getFaction()) continue;
				
				if (other.getAI() instanceof ModularFleetAIAPI) {
					ModularFleetAIAPI ai = (ModularFleetAIAPI) other.getAI();
					if (ai.isFleeing()) continue;
					if (ai.isMaintainingContact()) continue;
					
					if (ai.getTacticalModule().getTarget() == fleet) return;
					
					MemoryAPI mem = other.getMemoryWithoutUpdate();
					boolean smuggler = mem.getBoolean(MemFlags.MEMORY_KEY_SMUGGLER);
					boolean trader = mem.getBoolean(MemFlags.MEMORY_KEY_TRADE_FLEET);
					if (smuggler || trader) continue;
				}
				if (other.getFleetPoints() > fleet.getFleetPoints() * 0.25f || other.isStationMode()) return;
			}
			
			if (other.getFaction() == fleet.getFaction()) {
				if (other.getFleetPoints() > fleet.getFleetPoints()) return;
				if (other.getFleetPoints() == fleet.getFleetPoints()) {
					float dist = Misc.getDistance(other, closest.getPrimaryEntity());
					if (dist < minDist) return;
				}
			}
		}
		
		giveRaidOrder(closest);
	}
	
	
	protected void giveRaidOrder(final MarketAPI target) {
		clearTempAssignments(fleet);
		
		Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), 
								MemFlags.FLEET_BUSY, TEMP_BUSY_REASON, true, (1.5f + (float) Math.random()));
		
		String name = target.getName();
		String capText = "raiding " + name;
		String moveText = "moving to raid " + name;
		if (delegate != null) {
			String s = delegate.getRaidApproachText(fleet, target);
			if (s != null) moveText = s;
			
			s = delegate.getRaidActionText(fleet, target);
			if (s != null) capText = s;
		}
		
		Vector2f loc = Misc.getUnitVectorAtDegreeAngle(
							Misc.getAngleInDegrees(target.getPrimaryEntity().getLocation(), fleet.getLocation()));
		float holdRadius = fleet.getRadius() * 0.5f + target.getPrimaryEntity().getRadius();
		loc.scale(holdRadius);
		Vector2f.add(loc, target.getPrimaryEntity().getLocation(), loc);
		SectorEntityToken holdLoc = target.getContainingLocation().createToken(loc);
		holdLoc.setCircularOrbit(target.getPrimaryEntity(),
						   Misc.getAngleInDegrees(target.getPrimaryEntity().getLocation(), fleet.getLocation()),
							holdRadius, 1000000f);
		fleet.getContainingLocation().addEntity(holdLoc);
		Misc.fadeAndExpire(holdLoc, 5f);
		
		final int fpAtStart = fleet.getFleetPoints();
		//holdLoc = Global.getSector().getPlayerFleet();
		fleet.addAssignmentAtStart(FleetAssignment.HOLD, holdLoc, 0.5f, capText, new Script() {
			public void run() {
				if (fpAtStart == fleet.getFleetPoints()) {
					if (delegate != null) {
						delegate.performRaid(fleet, target);
					} else {
						new MarketCMD(target.getPrimaryEntity()).doGenericRaid(fleet.getFaction(),
																			   MarketCMD.getRaidStr(fleet));
					}
					clearTempAssignments(fleet);
				}
			}
		});
		FleetAssignmentDataAPI curr = fleet.getCurrentAssignment();
		if (curr != null) {
			curr.setCustom(TEMP_ASSIGNMENT);
		}
		
		float dist = Misc.getDistance(target.getPrimaryEntity(), fleet);
		//if (dist > fleet.getRadius() + target.getPrimaryEntity().getRadius() + 300f) {
		if (dist > fleet.getRadius() + target.getPrimaryEntity().getRadius()) {
			fleet.addAssignmentAtStart(FleetAssignment.DELIVER_CREW, holdLoc, 3f, moveText, null);
			curr = fleet.getCurrentAssignment();
			if (curr != null) {
				curr.setCustom(TEMP_ASSIGNMENT);
			}
		}
	}
	

	
	
}










